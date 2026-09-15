package com.example.engine

import com.example.model.*
import java.util.UUID
import kotlin.math.abs
import kotlin.math.max
import kotlin.random.Random

data class AttackResult(
    val damage: Int,
    val isCrit: Boolean,
    val isMiss: Boolean,
    val hasAdvantage: Boolean,
    val hasDisadvantage: Boolean,
    val comboName: String?,
    val logs: List<CombatLogEntry>,
    val appliedStatus: ActiveStatus? = null,
    val diceRoll: ActiveDiceRoll? = null
)

object CombatEngine {

    fun rollD20(advantage: Boolean = false, disadvantage: Boolean = false): Pair<Int, String> {
        val roll1 = Random.nextInt(1, 21)
        if (advantage && !disadvantage) {
            val roll2 = Random.nextInt(1, 21)
            val best = max(roll1, roll2)
            return Pair(best, "D20: [$roll1, $roll2] com Vantagem -> $best")
        }
        if (disadvantage && !advantage) {
            val roll2 = Random.nextInt(1, 21)
            val worst = minOf(roll1, roll2)
            return Pair(worst, "D20: [$roll1, $roll2] com Desvantagem -> $worst")
        }
        return Pair(roll1, "D20: $roll1")
    }

    fun calculateHeroAttack(
        attacker: Hero,
        target: Enemy,
        skill: Skill,
        allParty: List<Hero>
    ): AttackResult {
        val isStealthed = attacker.statusEffects.any { it.type == StatusEffectType.STEALTHED }
        val isTargetTaunted = target.statusEffects.any { it.type == StatusEffectType.TAUNTED }
        val isTargetPoisoned = target.statusEffects.any { it.type == StatusEffectType.POISONED }
        val isTargetFrozen = target.statusEffects.any { it.type == StatusEffectType.FROZEN }
        val hasBardBuff = attacker.statusEffects.any { it.type == StatusEffectType.HASTED || it.type == StatusEffectType.BLESSED }

        // Grid distance
        val gridDistance = abs(attacker.combatGridX - target.combatGridX) + abs(attacker.combatGridY - target.combatGridY)
        val isMeleeRange = gridDistance <= 1

        // Check Flanking: Is there ANOTHER alive hero adjacent to this enemy?
        val isFlanking = isMeleeRange && allParty.any { ally ->
            ally.id != attacker.id && ally.isAlive &&
            (abs(ally.combatGridX - target.combatGridX) + abs(ally.combatGridY - target.combatGridY) <= 1)
        }

        // Advantage conditions
        val hasAdvantage = isStealthed || isTargetTaunted || isFlanking || attacker.heroClass == HeroClass.ROGUE
        val hasDisadvantage = attacker.hp < (attacker.maxHp * 0.2f)

        val (d20, rollText) = rollD20(hasAdvantage, hasDisadvantage)
        val logs = mutableListOf<CombatLogEntry>()

        if (d20 == 1) {
            // Critical miss
            logs.add(CombatLogEntry(UUID.randomUUID().toString(), "${attacker.name} usou ${skill.name}! $rollText - Falha Crítica (Errou)!", LogType.ATTACK))
            val diceRoll = ActiveDiceRoll(
                d20 = d20,
                modifier = 0,
                total = 1,
                isCrit = false,
                isMiss = true,
                advantage = hasAdvantage,
                disadvantage = hasDisadvantage,
                actionName = skill.name,
                actorName = attacker.name,
                targetName = target.name,
                damage = 0,
                comboName = null,
                outcomeDescription = "Falha Crítica no ataque! O golpe errou o alvo completamente."
            )
            return AttackResult(0, isCrit = false, isMiss = true, hasAdvantage, hasDisadvantage, null, logs, diceRoll = diceRoll)
        }

        val isCrit = (d20 >= 19) || (isStealthed && attacker.heroClass == HeroClass.ROGUE)
        var comboName: String? = null
        var comboMultiplier = 1.0f

        // Check Team Synergies & Tactical Combos
        if (isFlanking) {
            comboName = "Posição de Flanco Tático (+35% Dano de Flanqueamento)"
            comboMultiplier += 0.35f
        }
        if (attacker.heroClass == HeroClass.ROGUE && isTargetTaunted) {
            comboName = "Sinergia: Golpe pelas Costas no Alvo Provocado (+60% Dano)"
            comboMultiplier += 0.6f
        } else if (skill.damageType == DamageType.FIRE && isTargetPoisoned) {
            comboName = "Sinergia: Conflagração Tóxica (+70% Dano Flamejante)"
            comboMultiplier += 0.7f
        } else if (skill.damageType == DamageType.HOLY && isTargetFrozen) {
            comboName = "Sinergia: Estilhaço Sagrado (+50% Dano & Ruptura)"
            comboMultiplier += 0.5f
        } else if (hasBardBuff) {
            comboName = "Sinergia: Acorde Heroico do Bardo (+30% Dano)"
            comboMultiplier += 0.3f
        }

        // Base stat power
        val statPower = if (skill.damageType in listOf(DamageType.FIRE, DamageType.FROST, DamageType.ARCANE, DamageType.HOLY)) {
            attacker.totalMag
        } else {
            attacker.totalAtk
        }

        var rawDmg = ((statPower * 0.8f) + skill.power + (d20 * 0.5f)) * comboMultiplier
        if (isCrit) {
            rawDmg *= 1.8f
        }

        var defenseReduction = target.def * 0.4f
        if (target.isGuarding) {
            defenseReduction += 8f
        }

        val finalDamage = max(4, (rawDmg - defenseReduction).toInt())

        val critTag = if (isCrit) " ★ CRÍTICO!" else ""
        val comboTag = if (comboName != null) " [$comboName]" else ""

        logs.add(
            CombatLogEntry(
                UUID.randomUUID().toString(),
                "${attacker.name} desferiu ${skill.name} em ${target.name}! $rollText -> $finalDamage Dano$critTag$comboTag",
                if (isCrit) LogType.CRIT else if (comboName != null) LogType.COMBO else LogType.ATTACK
            )
        )

        // Status effects application
        var appliedStatus: ActiveStatus? = null
        if (skill.id == "mag_frost" || skill.name.contains("Gelo")) {
            appliedStatus = ActiveStatus(StatusEffectType.FROZEN, durationTurns = 1)
            logs.add(CombatLogEntry(UUID.randomUUID().toString(), "${target.name} foi CONGELADO por 1 turno!", LogType.STATUS))
        } else if (skill.id == "mag_fireball" || skill.name.contains("Fogo")) {
            appliedStatus = ActiveStatus(StatusEffectType.BURNING, durationTurns = 2, magnitude = 8)
            logs.add(CombatLogEntry(UUID.randomUUID().toString(), "${target.name} começou a QUEIMAR!", LogType.STATUS))
        } else if (skill.id == "rog_poison" || skill.id == "ran_trap") {
            appliedStatus = ActiveStatus(StatusEffectType.POISONED, durationTurns = 3, magnitude = 10)
            logs.add(CombatLogEntry(UUID.randomUUID().toString(), "${target.name} foi ENVENENADO!", LogType.STATUS))
        } else if (skill.id == "war_taunt") {
            appliedStatus = ActiveStatus(StatusEffectType.TAUNTED, durationTurns = 2)
            logs.add(CombatLogEntry(UUID.randomUUID().toString(), "${target.name} foi PROVOCADO por ${attacker.name}!", LogType.STATUS))
        }

        val diceRoll = ActiveDiceRoll(
            d20 = d20,
            modifier = (statPower * 0.4f).toInt(),
            total = d20 + (statPower * 0.4f).toInt(),
            isCrit = isCrit,
            isMiss = false,
            advantage = hasAdvantage,
            disadvantage = hasDisadvantage,
            actionName = skill.name,
            actorName = attacker.name,
            targetName = target.name,
            damage = finalDamage,
            comboName = comboName,
            outcomeDescription = if (isCrit) "Acerto Crítico fulminante causando destruição!" else "Acerto confirmado contra as defesas inimigas."
        )

        return AttackResult(
            damage = finalDamage,
            isCrit = isCrit,
            isMiss = false,
            hasAdvantage = hasAdvantage,
            hasDisadvantage = hasDisadvantage,
            comboName = comboName,
            logs = logs,
            appliedStatus = appliedStatus,
            diceRoll = diceRoll
        )
    }

    fun shoveEnemy(hero: Hero, enemy: Enemy, hitWall: Boolean, hitTrap: Boolean): AttackResult {
        val (d20, rollText) = rollD20(advantage = hero.heroClass == HeroClass.WARRIOR)
        val logs = mutableListOf<CombatLogEntry>()

        if (d20 == 1) {
            logs.add(CombatLogEntry(UUID.randomUUID().toString(), "${hero.name} tentou empurrar ${enemy.name}, mas tropeçou! ($rollText)", LogType.ATTACK))
            val diceRoll = ActiveDiceRoll(
                d20 = d20, modifier = 0, total = 1, isCrit = false, isMiss = true,
                advantage = false, disadvantage = false, actionName = "Empurrão Tático",
                actorName = hero.name, targetName = enemy.name, damage = 0,
                outcomeDescription = "O empurrão falhou miseravelmente."
            )
            return AttackResult(0, isCrit = false, isMiss = true, false, false, null, logs, diceRoll = diceRoll)
        }

        var baseDmg = 8 + (hero.totalAtk * 0.3f).toInt()
        var combo = "Empurrão Tático: Inimigo recuou 1 quadro"

        if (hitTrap) {
            baseDmg += 22
            combo += " + Caiu em Armadilha de Espinhos!"
            logs.add(CombatLogEntry(UUID.randomUUID().toString(), "${enemy.name} foi projetado contra uma ARMADILHA DE ESPINHOS!", LogType.CRIT))
        } else if (hitWall) {
            baseDmg += 14
            combo += " + Esmagado contra a Muralha de Pedra!"
            logs.add(CombatLogEntry(UUID.randomUUID().toString(), "${enemy.name} chocou-se violentamente contra a parede de pedra!", LogType.COMBO))
        }

        logs.add(CombatLogEntry(UUID.randomUUID().toString(), "${hero.name} empurrou ${enemy.name}! $rollText -> $baseDmg Dano de Impacto.", LogType.ATTACK))

        val diceRoll = ActiveDiceRoll(
            d20 = d20, modifier = 4, total = d20 + 4, isCrit = d20 >= 19, isMiss = false,
            advantage = hero.heroClass == HeroClass.WARRIOR, disadvantage = false,
            actionName = "Empurrão Tático", actorName = hero.name, targetName = enemy.name,
            damage = baseDmg, comboName = combo, outcomeDescription = combo
        )

        return AttackResult(baseDmg, isCrit = d20 >= 19, isMiss = false, false, false, combo, logs, diceRoll = diceRoll)
    }

    fun calculateEnemyAttack(enemy: Enemy, targetHero: Hero): AttackResult {
        val (d20, rollText) = rollD20()
        val logs = mutableListOf<CombatLogEntry>()

        if (d20 == 1) {
            logs.add(CombatLogEntry(UUID.randomUUID().toString(), "${enemy.name} tentou golpear ${targetHero.name}, mas errou miseravelmente! ($rollText)", LogType.ATTACK))
            val diceRoll = ActiveDiceRoll(
                d20 = d20, modifier = 0, total = 1, isCrit = false, isMiss = true,
                advantage = false, disadvantage = false, actionName = "Ataque Inimigo",
                actorName = enemy.name, targetName = targetHero.name, damage = 0,
                outcomeDescription = "O inimigo errou o golpe!"
            )
            return AttackResult(0, isCrit = false, isMiss = true, false, false, null, logs, diceRoll = diceRoll)
        }

        val isCrit = d20 >= 20
        var rawDmg = (enemy.atk * 0.9f) + (d20 * 0.4f)
        if (isCrit) rawDmg *= 1.7f

        // Defense calculation (Guard reduces by 50%)
        var defReduction = targetHero.totalDef * 0.5f
        if (targetHero.isGuarding) {
            defReduction += 12f
            logs.add(CombatLogEntry(UUID.randomUUID().toString(), "${targetHero.name} bloqueou parte do impacto com sua Postura Defensiva!", LogType.STATUS))
        }

        val finalDamage = max(3, (rawDmg - defReduction).toInt())

        val critText = if (isCrit) " ★ CRÍTICO!" else ""
        logs.add(
            CombatLogEntry(
                UUID.randomUUID().toString(),
                "${enemy.name} atacou ${targetHero.name} causando $finalDamage de dano!$critText",
                if (isCrit) LogType.CRIT else LogType.ATTACK
            )
        )

        val diceRoll = ActiveDiceRoll(
            d20 = d20, modifier = (enemy.atk * 0.3f).toInt(), total = d20 + (enemy.atk * 0.3f).toInt(),
            isCrit = isCrit, isMiss = false, advantage = false, disadvantage = false,
            actionName = enemy.currentIntent?.type?.title ?: "Ataque Inimigo",
            actorName = enemy.name, targetName = targetHero.name, damage = finalDamage,
            outcomeDescription = if (isCrit) "Ataque crítico devastador do adversário!" else "Impacto contra a armadura do herói."
        )

        return AttackResult(finalDamage, isCrit, false, false, false, null, logs, diceRoll = diceRoll)
    }

    fun generateEnemyIntent(enemy: Enemy, aliveHeroes: List<Hero>): EnemyIntent {
        if (aliveHeroes.isEmpty()) {
            return EnemyIntent(EnemyIntentType.ATTACK, "Vagueia sem alvo")
        }

        // Check if a hero has taunted
        val tauntedHero = aliveHeroes.find { h -> enemy.statusEffects.any { it.type == StatusEffectType.TAUNTED } }
        val target = tauntedHero ?: aliveHeroes.minByOrNull { it.hp } ?: aliveHeroes.first()

        return if (enemy.isBoss) {
            val roll = Random.nextInt(100)
            when {
                roll < 45 -> EnemyIntent(
                    EnemyIntentType.HEAVY_SMASH,
                    "Preparando Golpe Esmagador em ${target.name}!",
                    target.name,
                    estimatedDamage = (enemy.atk * 1.5f).toInt()
                )
                roll < 75 -> EnemyIntent(
                    EnemyIntentType.ARCANE_SPELL,
                    "Canalizando Magia Sombria em Área!",
                    target.name,
                    estimatedDamage = (enemy.mag * 1.3f).toInt()
                )
                else -> EnemyIntent(
                    EnemyIntentType.DEFENSIVE_GUARD,
                    "Erguendo Barreira Óssea (+Defesa e Resistência)",
                    null,
                    estimatedDamage = 0
                )
            }
        } else {
            val roll = Random.nextInt(100)
            when {
                roll < 60 -> EnemyIntent(
                    EnemyIntentType.ATTACK,
                    "Mirando ataque direto em ${target.name}",
                    target.name,
                    estimatedDamage = (enemy.atk * 0.9f).toInt()
                )
                roll < 85 -> EnemyIntent(
                    EnemyIntentType.HEAVY_SMASH,
                    "Carregando golpe forte em ${target.name}!",
                    target.name,
                    estimatedDamage = (enemy.atk * 1.3f).toInt()
                )
                else -> EnemyIntent(
                    EnemyIntentType.DEFENSIVE_GUARD,
                    "Adotando guarda defensiva",
                    null,
                    estimatedDamage = 0
                )
            }
        }
    }
}

