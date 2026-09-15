package com.example.data

import com.example.model.*
import java.util.UUID

object GameDatabase {

    fun getInitialSkillsForClass(heroClass: HeroClass): List<Skill> {
        return when (heroClass) {
            HeroClass.WARRIOR -> listOf(
                Skill(
                    id = "war_strike",
                    name = "Golpe Poderoso",
                    description = "Golpe pesado com a lâmina que causa alto dano físico.",
                    manaCost = 0,
                    power = 24,
                    range = 1,
                    targetType = SkillTarget.SINGLE_ENEMY,
                    damageType = DamageType.PHYSICAL,
                    iconKey = "sword"
                ),
                Skill(
                    id = "war_cleave",
                    name = "Golpe Circular (Clivar)",
                    description = "Gira a espada em arco atingindo todos os inimigos adjacentes.",
                    manaCost = 8,
                    power = 18,
                    range = 1,
                    aoeRadius = 1,
                    targetType = SkillTarget.ALL_ENEMIES,
                    damageType = DamageType.PHYSICAL,
                    cooldownMax = 2,
                    iconKey = "cleave"
                ),
                Skill(
                    id = "war_taunt",
                    name = "Provocação & Muralha",
                    description = "Força o foco dos inimigos e ganha bônus maciço de armadura por 2 turnos.",
                    manaCost = 10,
                    power = 0,
                    range = 3,
                    targetType = SkillTarget.SINGLE_ENEMY,
                    damageType = DamageType.PHYSICAL,
                    cooldownMax = 3,
                    iconKey = "shield"
                ),
                Skill(
                    id = "war_execute",
                    name = "Execução Heroica",
                    description = "Ataque devastador que causa o dobro do dano se o alvo estiver abaixo de 50% de HP.",
                    manaCost = 15,
                    power = 45,
                    range = 1,
                    targetType = SkillTarget.SINGLE_ENEMY,
                    damageType = DamageType.PHYSICAL,
                    cooldownMax = 4,
                    iconKey = "axe"
                )
            )
            HeroClass.MAGE -> listOf(
                Skill(
                    id = "mag_bolt",
                    name = "Projétil Arcano",
                    description = "Disparo veloz de pura energia mística.",
                    manaCost = 6,
                    power = 20,
                    range = 4,
                    targetType = SkillTarget.SINGLE_ENEMY,
                    damageType = DamageType.ARCANE,
                    iconKey = "spark"
                ),
                Skill(
                    id = "mag_fireball",
                    name = "Bola de Fogo",
                    description = "Detonação flamejante que incinera o alvo e causa queimadura.",
                    manaCost = 18,
                    power = 36,
                    range = 4,
                    aoeRadius = 1,
                    targetType = SkillTarget.AREA_GRID,
                    damageType = DamageType.FIRE,
                    cooldownMax = 2,
                    iconKey = "fire"
                ),
                Skill(
                    id = "mag_frost",
                    name = "Nova de Gelo",
                    description = "Explosão glacial que causa dano em área e congela os inimigos por 1 turno.",
                    manaCost = 16,
                    power = 22,
                    range = 3,
                    aoeRadius = 1,
                    targetType = SkillTarget.ALL_ENEMIES,
                    damageType = DamageType.FROST,
                    cooldownMax = 3,
                    iconKey = "ice"
                ),
                Skill(
                    id = "mag_lightning",
                    name = "Corrente de Relâmpagos",
                    description = "Raio violento que salta entre múltiplos inimigos causando choque elétrico.",
                    manaCost = 25,
                    power = 50,
                    range = 4,
                    targetType = SkillTarget.ALL_ENEMIES,
                    damageType = DamageType.ARCANE,
                    cooldownMax = 4,
                    iconKey = "bolt"
                )
            )
            HeroClass.CLERIC -> listOf(
                Skill(
                    id = "cle_smite",
                    name = "Golpe Sagrado",
                    description = "Ataque luminoso abençoado que causa dano bônus contra mortos-vivos.",
                    manaCost = 8,
                    power = 22,
                    range = 1,
                    targetType = SkillTarget.SINGLE_ENEMY,
                    damageType = DamageType.HOLY,
                    iconKey = "holy_smite"
                ),
                Skill(
                    id = "cle_heal",
                    name = "Prece de Cura",
                    description = "Canaliza a graça divina para restaurar a vida de um aliado.",
                    manaCost = 14,
                    power = 35,
                    range = 3,
                    targetType = SkillTarget.SINGLE_ALLY,
                    damageType = DamageType.HEAL,
                    cooldownMax = 1,
                    iconKey = "heal"
                ),
                Skill(
                    id = "cle_radiance",
                    name = "Radiância Divina",
                    description = "Emite um pulso de luz celestial que cura todos os aliados e fere criaturas das trevas.",
                    manaCost = 22,
                    power = 25,
                    range = 3,
                    targetType = SkillTarget.ALL_ENEMIES,
                    damageType = DamageType.HOLY,
                    cooldownMax = 3,
                    iconKey = "radiance"
                ),
                Skill(
                    id = "cle_sanctuary",
                    name = "Santuário Protetor",
                    description = "Cria uma barreira sagrada que absorve dano e purifica maldições.",
                    manaCost = 20,
                    power = 30,
                    range = 3,
                    targetType = SkillTarget.ALL_ALLIES,
                    damageType = DamageType.HEAL,
                    cooldownMax = 4,
                    iconKey = "barrier"
                )
            )
            HeroClass.ROGUE -> listOf(
                Skill(
                    id = "rog_stab",
                    name = "Punhalada Rápida",
                    description = "Ataque furtivo veloz com chance de crítico ampliada.",
                    manaCost = 4,
                    power = 26,
                    range = 1,
                    targetType = SkillTarget.SINGLE_ENEMY,
                    damageType = DamageType.PHYSICAL,
                    iconKey = "dagger"
                ),
                Skill(
                    id = "rog_poison",
                    name = "Lâmina Envenenada",
                    description = "Aplica toxina mortal na adaga causando veneno contínuo ao alvo.",
                    manaCost = 10,
                    power = 20,
                    range = 1,
                    targetType = SkillTarget.SINGLE_ENEMY,
                    damageType = DamageType.POISON,
                    cooldownMax = 2,
                    iconKey = "poison"
                ),
                Skill(
                    id = "rog_stealth",
                    name = "Passo das Sombras",
                    description = "Fica invisível, reposiciona-se no mapa e garante acerto crítico garantido no próximo turno.",
                    manaCost = 12,
                    power = 0,
                    range = 0,
                    targetType = SkillTarget.SELF,
                    damageType = DamageType.SHADOW,
                    cooldownMax = 3,
                    iconKey = "stealth"
                ),
                Skill(
                    id = "rog_assassinate",
                    name = "Assassinar",
                    description = "Golpe fatal nas artérias vitais. Dano massivo com 80% de chance de crítico.",
                    manaCost = 18,
                    power = 55,
                    range = 1,
                    targetType = SkillTarget.SINGLE_ENEMY,
                    damageType = DamageType.PHYSICAL,
                    cooldownMax = 4,
                    iconKey = "skull"
                )
            )
            HeroClass.RANGER -> listOf(
                Skill(
                    id = "ran_shot",
                    name = "Tiro Preciso",
                    description = "Disparo de longa distância com arco recurvo.",
                    manaCost = 5,
                    power = 22,
                    range = 5,
                    targetType = SkillTarget.SINGLE_ENEMY,
                    damageType = DamageType.PHYSICAL,
                    iconKey = "bow"
                ),
                Skill(
                    id = "ran_volley",
                    name = "Saraivada de Flechas",
                    description = "Chuva de projéteis cobrindo múltiplos alvos à distância.",
                    manaCost = 14,
                    power = 20,
                    range = 4,
                    aoeRadius = 1,
                    targetType = SkillTarget.ALL_ENEMIES,
                    damageType = DamageType.PHYSICAL,
                    cooldownMax = 2,
                    iconKey = "volley"
                ),
                Skill(
                    id = "ran_trap",
                    name = "Armadilha de Espinhos & Veneno",
                    description = "Posiciona armadilha no grid que imobiliza e envenena o primeiro a pisar.",
                    manaCost = 12,
                    power = 25,
                    range = 3,
                    targetType = SkillTarget.AREA_GRID,
                    damageType = DamageType.POISON,
                    cooldownMax = 3,
                    iconKey = "trap"
                ),
                Skill(
                    id = "ran_mark",
                    name = "Marca do Caçador",
                    description = "Marca uma criatura, reduzindo sua defesa e aumentando o dano recebido em 50%.",
                    manaCost = 10,
                    power = 10,
                    range = 5,
                    targetType = SkillTarget.SINGLE_ENEMY,
                    damageType = DamageType.PHYSICAL,
                    cooldownMax = 3,
                    iconKey = "target"
                )
            )
            HeroClass.BARD -> listOf(
                Skill(
                    id = "bar_anthem",
                    name = "Hino da Valentia",
                    description = "Canção inspiradora que aumenta o ataque e acerto crítico de todos os aliados.",
                    manaCost = 12,
                    power = 0,
                    range = 4,
                    targetType = SkillTarget.ALL_ALLIES,
                    damageType = DamageType.HOLY,
                    cooldownMax = 2,
                    iconKey = "lute"
                ),
                Skill(
                    id = "bar_shriek",
                    name = "Grito Discordante",
                    description = "Onda sônica ensurdecedora que atordoa e desorienta os inimigos.",
                    manaCost = 15,
                    power = 20,
                    range = 3,
                    targetType = SkillTarget.ALL_ENEMIES,
                    damageType = DamageType.ARCANE,
                    cooldownMax = 3,
                    iconKey = "sound"
                ),
                Skill(
                    id = "bar_haste",
                    name = "Marcha Acelerada",
                    description = "Aumenta a velocidade e concede Pontos de Ação extras a todo o grupo.",
                    manaCost = 18,
                    power = 0,
                    range = 4,
                    targetType = SkillTarget.ALL_ALLIES,
                    damageType = DamageType.ARCANE,
                    cooldownMax = 3,
                    iconKey = "speed"
                ),
                Skill(
                    id = "bar_lullaby",
                    name = "Balada da Calmaria",
                    description = "Melodia reconfortante que restaura sanidade e regenera MP do grupo.",
                    manaCost = 20,
                    power = 25,
                    range = 4,
                    targetType = SkillTarget.ALL_ALLIES,
                    damageType = DamageType.HEAL,
                    cooldownMax = 4,
                    iconKey = "harp"
                )
            )
        }
    }

    fun createInitialParty(): List<Hero> {
        return listOf(
            Hero(
                id = "hero_warrior",
                name = "Sir Valerius",
                heroClass = HeroClass.WARRIOR,
                hp = 120, maxHp = 120,
                mp = 25, maxMp = 25,
                baseAtk = 18, baseDef = 14, baseMag = 4, baseSpd = 9,
                skills = getInitialSkillsForClass(HeroClass.WARRIOR),
                equippedWeapon = Item("wpn_1", "Espada Longa de Ferro", "Lâmina forjada em aço temperado com guarda ornamentada.", ItemType.WEAPON, Rarity.COMMON, atkBonus = 6, value = 40),
                equippedArmor = Item("arm_1", "Cota de Malha Pesada", "Proteção sólida de anéis entrelaçados.", ItemType.ARMOR, Rarity.COMMON, defBonus = 8, value = 50),
                equippedAccessory = Item("acc_1", "Anel do Guardião", "Aumenta a resistência e vida máxima.", ItemType.ACCESSORY, Rarity.UNCOMMON, hpBonus = 20, defBonus = 2, value = 75),
                posX = 2, posY = 2
            ),
            Hero(
                id = "hero_mage",
                name = "Eldrin Sombrastral",
                heroClass = HeroClass.MAGE,
                hp = 70, maxHp = 70,
                mp = 100, maxMp = 100,
                baseAtk = 6, baseDef = 5, baseMag = 22, baseSpd = 10,
                skills = getInitialSkillsForClass(HeroClass.MAGE),
                equippedWeapon = Item("wpn_2", "Cajado das Runas Gélidas", "Foco arcano com uma gema azul cintilante no topo.", ItemType.WEAPON, Rarity.UNCOMMON, magBonus = 12, value = 80),
                equippedArmor = Item("arm_2", "Túnica do Místico", "Tecido impregnado com fios condutores de mana.", ItemType.ARMOR, Rarity.COMMON, defBonus = 3, magBonus = 4, value = 45),
                equippedAccessory = Item("acc_2", "Amuleto de Lápis-Lazúli", "Canalizador que amplifica poder mágico.", ItemType.ACCESSORY, Rarity.UNCOMMON, magBonus = 6, value = 70),
                posX = 2, posY = 3
            ),
            Hero(
                id = "hero_cleric",
                name = "Irmã Lysandra",
                heroClass = HeroClass.CLERIC,
                hp = 95, maxHp = 95,
                mp = 80, maxMp = 80,
                baseAtk = 12, baseDef = 10, baseMag = 16, baseSpd = 8,
                skills = getInitialSkillsForClass(HeroClass.CLERIC),
                equippedWeapon = Item("wpn_3", "Maça da Aurora Dourada", "Arma sagrada abençoada com símbolos solares.", ItemType.WEAPON, Rarity.UNCOMMON, atkBonus = 8, magBonus = 6, value = 85),
                equippedArmor = Item("arm_3", "Armadura de Escamas Solenes", "Peitoral de escamas banhadas em oração.", ItemType.ARMOR, Rarity.COMMON, defBonus = 6, value = 50),
                equippedAccessory = Item("acc_3", "Relicário de Prata", "Guarda cinzas de mártires e purifica o espírito.", ItemType.ACCESSORY, Rarity.RARE, hpBonus = 15, magBonus = 5, value = 110),
                posX = 3, posY = 2
            ),
            Hero(
                id = "hero_rogue",
                name = "Vesper Sombra-Ágil",
                heroClass = HeroClass.ROGUE,
                hp = 80, maxHp = 80,
                mp = 40, maxMp = 40,
                baseAtk = 20, baseDef = 7, baseMag = 6, baseSpd = 15,
                skills = getInitialSkillsForClass(HeroClass.ROGUE),
                equippedWeapon = Item("wpn_4", "Adagas Gêmeas de Obsidiana", "Fio cirúrgico capaz de rasgar sombras.", ItemType.WEAPON, Rarity.RARE, atkBonus = 14, critBonus = 15, value = 120),
                equippedArmor = Item("arm_4", "Gibão de Couro Furtivo", "Tratado com óleo especial para passos silenciosos.", ItemType.ARMOR, Rarity.UNCOMMON, defBonus = 5, spdBonus = 3, value = 65),
                equippedAccessory = Item("acc_4", "Capa do Notívago", "Camufla a presença em locais escuros.", ItemType.ACCESSORY, Rarity.UNCOMMON, spdBonus = 4, critBonus = 5, value = 90),
                posX = 3, posY = 3
            )
        )
    }

    fun getEnemiesForTheme(theme: EnvironmentTheme, isBossFight: Boolean = false): List<Enemy> {
        return when (theme) {
            EnvironmentTheme.FORGOTTEN_CATACOMBS -> {
                if (isBossFight) {
                    listOf(
                        Enemy(
                            id = UUID.randomUUID().toString(),
                            name = "Rei Esqueleto Eterno",
                            title = "Senhor dos Ossuários Milenares",
                            category = EnemyCategory.BOSS,
                            level = 4,
                            hp = 220, maxHp = 220,
                            atk = 24, def = 14, mag = 16, spd = 9,
                            xpReward = 250, goldReward = 180, isBoss = true,
                            skills = listOf(
                                Skill("sk_b1", "Golpe da Tumba", "Causa dano físico e aplica medo.", 0, 1, 28, 1, 0, SkillTarget.SINGLE_ENEMY, DamageType.PHYSICAL),
                                Skill("sk_b2", "Convocar Esqueletos", "Ergue soldados mortos das pilhas de ossos.", 10, 1, 15, 3, 1, SkillTarget.ALL_ENEMIES, DamageType.SHADOW)
                            ),
                            posX = 12, posY = 6
                        ),
                        Enemy(
                            id = UUID.randomUUID().toString(),
                            name = "Guarda de Elite dos Ossos",
                            title = "Protetor do Trono Fúnebre",
                            category = EnemyCategory.UNDEAD,
                            level = 3,
                            hp = 65, maxHp = 65,
                            atk = 16, def = 10, mag = 4, spd = 8,
                            xpReward = 60, goldReward = 30,
                            posX = 11, posY = 5
                        )
                    )
                } else {
                    listOf(
                        Enemy(
                            id = UUID.randomUUID().toString(),
                            name = "Esqueleto Guerreiro",
                            title = "Sentinela Fúnebre",
                            category = EnemyCategory.UNDEAD,
                            level = 1,
                            hp = 45, maxHp = 45,
                            atk = 14, def = 6, mag = 0, spd = 7,
                            xpReward = 40, goldReward = 20,
                            posX = 7, posY = 4
                        ),
                        Enemy(
                            id = UUID.randomUUID().toString(),
                            name = "Arqueiro dos Ossos",
                            title = "Atirador Cadavérico",
                            category = EnemyCategory.UNDEAD,
                            level = 1,
                            hp = 35, maxHp = 35,
                            atk = 15, def = 4, mag = 2, spd = 11,
                            xpReward = 45, goldReward = 25,
                            posX = 9, posY = 3
                        ),
                        Enemy(
                            id = UUID.randomUUID().toString(),
                            name = "Zumbi Pútrido",
                            title = "Corpo Decomposto",
                            category = EnemyCategory.UNDEAD,
                            level = 2,
                            hp = 60, maxHp = 60,
                            atk = 16, def = 8, mag = 0, spd = 5,
                            xpReward = 50, goldReward = 22,
                            posX = 8, posY = 7
                        )
                    )
                }
            }
            EnvironmentTheme.ROYAL_CRYPT -> {
                listOf(
                    Enemy(
                        id = UUID.randomUUID().toString(),
                        name = "Cavaleiro Espectral da Coroa",
                        title = "Guardião da Cripta Real",
                        category = EnemyCategory.UNDEAD,
                        level = 5,
                        hp = 110, maxHp = 110,
                        atk = 22, def = 16, mag = 10, spd = 10,
                        xpReward = 95, goldReward = 70,
                        posX = 8, posY = 5
                    ),
                    Enemy(
                        id = UUID.randomUUID().toString(),
                        name = "Aparição dos Vitrais",
                        title = "Espírito Vingativo",
                        category = EnemyCategory.UNDEAD,
                        level = 4,
                        hp = 70, maxHp = 70,
                        atk = 12, def = 6, mag = 22, spd = 13,
                        xpReward = 85, goldReward = 60,
                        posX = 10, posY = 4
                    )
                )
            }
            EnvironmentTheme.ABANDONED_DUNGEON -> {
                listOf(
                    Enemy(
                        id = UUID.randomUUID().toString(),
                        name = "Torturador Cego",
                        title = "Executor das Masmorras",
                        category = EnemyCategory.CULTIST,
                        level = 6,
                        hp = 130, maxHp = 130,
                        atk = 26, def = 12, mag = 4, spd = 8,
                        xpReward = 110, goldReward = 85,
                        posX = 8, posY = 5
                    ),
                    Enemy(
                        id = UUID.randomUUID().toString(),
                        name = "Gárgula de Correntes",
                        title = "Sentinela Animada",
                        category = EnemyCategory.SUBTERRANEAN,
                        level = 6,
                        hp = 95, maxHp = 95,
                        atk = 20, def = 18, mag = 8, spd = 9,
                        xpReward = 100, goldReward = 75,
                        posX = 10, posY = 6
                    )
                )
            }
            EnvironmentTheme.NATURAL_CAVERNS -> {
                listOf(
                    Enemy(
                        id = UUID.randomUUID().toString(),
                        name = "Aranha Gigante Bioluminescente",
                        title = "Predadora das Teias Venenosas",
                        category = EnemyCategory.SUBTERRANEAN,
                        level = 8,
                        hp = 140, maxHp = 140,
                        atk = 25, def = 10, mag = 14, spd = 16,
                        xpReward = 140, goldReward = 95,
                        posX = 7, posY = 4
                    ),
                    Enemy(
                        id = UUID.randomUUID().toString(),
                        name = "Verme de Pedra Escavador",
                        title = "Horror das Fendas",
                        category = EnemyCategory.SUBTERRANEAN,
                        level = 9,
                        hp = 180, maxHp = 180,
                        atk = 28, def = 20, mag = 6, spd = 7,
                        xpReward = 160, goldReward = 110,
                        posX = 11, posY = 6
                    )
                )
            }
            EnvironmentTheme.SUBTERRANEAN_TEMPLE -> {
                listOf(
                    Enemy(
                        id = UUID.randomUUID().toString(),
                        name = "Sacerdote das Runas Malditas",
                        title = "Invocador do Abismo",
                        category = EnemyCategory.CULTIST,
                        level = 12,
                        hp = 160, maxHp = 160,
                        atk = 18, def = 14, mag = 34, spd = 12,
                        xpReward = 220, goldReward = 160,
                        posX = 8, posY = 4
                    ),
                    Enemy(
                        id = UUID.randomUUID().toString(),
                        name = "Demônio Invocado",
                        title = "Criatura da Dimensão Profana",
                        category = EnemyCategory.CULTIST,
                        level = 13,
                        hp = 210, maxHp = 210,
                        atk = 35, def = 18, mag = 22, spd = 11,
                        xpReward = 280, goldReward = 190,
                        posX = 11, posY = 6
                    )
                )
            }
            EnvironmentTheme.ANCIENT_RUINS -> {
                listOf(
                    Enemy(
                        id = UUID.randomUUID().toString(),
                        name = "Titã Golem de Pedra",
                        title = "Colosso Protetor dos Segredos",
                        category = EnemyCategory.BOSS,
                        level = 16,
                        hp = 380, maxHp = 380,
                        atk = 42, def = 30, mag = 18, spd = 6,
                        xpReward = 500, goldReward = 400, isBoss = true,
                        posX = 10, posY = 5
                    )
                )
            }
        }
    }

    val sampleQuests: List<Quest> = listOf(
        Quest(
            id = "quest_1",
            title = "O Segredo das Catacumbas Esquecidas",
            description = "O sacerdote da cidade solicita a recuperação do Santo Cálice de Prata nas profundezas do ossuário milenar antes que o culto o profanize.",
            theme = EnvironmentTheme.FORGOTTEN_CATACOMBS,
            goalText = "Explore as catacumbas, abra o sarcófago central e derrote o Rei Esqueleto.",
            rewardGold = 250,
            rewardXp = 400
        ),
        Quest(
            id = "quest_2",
            title = "A Coroa Perdida da Cripta Real",
            description = "Aventureiros anteriores relatam que as efígies dos reis esculpidas em pedra guardam gemas que restauram o equilíbrio das marés mágicas.",
            theme = EnvironmentTheme.ROYAL_CRYPT,
            goalText = "Encontre os 3 mosaicos antigos e derrote o Cavaleiro Espectral da Coroa.",
            rewardGold = 380,
            rewardXp = 600
        ),
        Quest(
            id = "quest_3",
            title = "O Cárcere dos Torturados",
            description = "Gritos fantasmagóricos emanam das celas de ferro da Masmorra Abandonada. Liberte as almas aprisionadas e elimine os carrascos profanos.",
            theme = EnvironmentTheme.ABANDONED_DUNGEON,
            goalText = "Desative os poços de correntes e derrote o Carrasco Executor.",
            rewardGold = 500,
            rewardXp = 850
        ),
        Quest(
            id = "quest_4",
            title = "A Noite dos Fungos Azuis",
            description = "Colha os esporos luminescentes nas Cavernas Naturais para curar a praga do reino enquanto escapa das aranhas gigantes.",
            theme = EnvironmentTheme.NATURAL_CAVERNS,
            goalText = "Colete 4 amostras nos lagos subterrâneos e derrote a Matriarca Peçonhenta.",
            rewardGold = 650,
            rewardXp = 1100
        ),
        Quest(
            id = "quest_5",
            title = "O Ritual no Templo Subterrâneo",
            description = "Interrompa a invocação demoníaca no grande altar de runas sagradas antes que o portal do abismo se rompa.",
            theme = EnvironmentTheme.SUBTERRANEAN_TEMPLE,
            goalText = "Purifique os altares arcanos e destrua o Grão-Sacerdote Demoníaco.",
            rewardGold = 900,
            rewardXp = 1500
        ),
        Quest(
            id = "quest_6",
            title = "O despertar do Titã nas Ruínas Antigas",
            description = "O maquinário colossal de eras passadas voltou a girar sob a terra. Desative o núcleo de força do Colosso de Pedra.",
            theme = EnvironmentTheme.ANCIENT_RUINS,
            goalText = "Decifre os pergaminhos da biblioteca perdida e derrote o Titã Golem.",
            rewardGold = 1500,
            rewardXp = 2500
        )
    )

    val allLootPool: List<Item> = listOf(
        Item("loot_1", "Lâmina da Cripta Profana", "Espada rúnica encantada com chamas espectrais.", ItemType.WEAPON, Rarity.EPIC, atkBonus = 22, critBonus = 12, value = 240),
        Item("loot_2", "Cajado das Estrelas Caídas", "Madeira petrificada com núcleo de cristal estelar.", ItemType.WEAPON, Rarity.EPIC, magBonus = 26, value = 260),
        Item("loot_3", "Arco do Caçador Abissal", "Feito de ossos de dragão negro com corda de seda de aranha.", ItemType.WEAPON, Rarity.RARE, atkBonus = 18, spdBonus = 4, value = 190),
        Item("loot_4", "Armadura de Placas de Titânio", "Forjada pelos artífices das ruínas antigas.", ItemType.ARMOR, Rarity.EPIC, defBonus = 18, hpBonus = 40, value = 300),
        Item("loot_5", "Manto do Arcano Imortal", "Tecido etéreo que desvia projéteis mágicos.", ItemType.ARMOR, Rarity.RARE, defBonus = 8, magBonus = 14, value = 210),
        Item("loot_6", "Anel do Olho do Dragão", "Concede visão na escuridão profunda e bônus crítico.", ItemType.ACCESSORY, Rarity.LEGENDARY, atkBonus = 8, critBonus = 20, spdBonus = 5, value = 500),
        Item("loot_7", "Amuleto da Luz Eterna", "Protege contra perda de sanidade e amplifica cura.", ItemType.ACCESSORY, Rarity.EPIC, hpBonus = 35, magBonus = 12, value = 350),
        Item("loot_8", "Gema de Rubi Flamejante", "Pode ser encrustada para adicionar dano de fogo aos ataques.", ItemType.GEM, Rarity.RARE, atkBonus = 6, value = 150),
        Item("loot_9", "Gema de Safira Gélida", "Aumenta poder mágico e chance de congelamento.", ItemType.GEM, Rarity.RARE, magBonus = 8, value = 150),
        Item("pot_1", "Poção de Cura Maior", "Restaura 70 pontos de vida instantaneamente.", ItemType.CONSUMABLE, Rarity.UNCOMMON, value = 35, effectTag = "HEAL_70"),
        Item("pot_2", "Elixir de Mana Puro", "Restaura 60 pontos de mana.", ItemType.CONSUMABLE, Rarity.UNCOMMON, value = 30, effectTag = "MANA_60"),
        Item("pot_3", "Frasco de Tochas Alquímicas", "Adiciona 3 tochas de longa queima ao inventário.", ItemType.CONSUMABLE, Rarity.COMMON, value = 20, effectTag = "TORCH_3")
    )
}
