package com.example.data

import android.content.Context
import com.example.model.*
import org.json.JSONArray
import org.json.JSONObject

/** Dados persistidos entre sessões (progresso do grupo + preferências). */
data class SaveData(
    val party: List<Hero>,
    val resources: PartyResources,
    val inventory: List<Item>,
    val soundEnabled: Boolean,
    val hapticsEnabled: Boolean,
    val completedQuestIds: Set<String>,
    val totalVictories: Int
)

/**
 * Salvamento local leve com SharedPreferences + org.json (nativo do Android).
 * Sem reflexão, portanto 100% compatível com R8/minificação.
 */
class SaveManager(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun save(data: SaveData) {
        try {
            val root = JSONObject()
            root.put("version", SAVE_VERSION)
            root.put("party", JSONArray().apply { data.party.forEach { put(heroToJson(it)) } })
            root.put("resources", resourcesToJson(data.resources))
            root.put("inventory", JSONArray().apply { data.inventory.forEach { put(itemToJson(it)) } })
            root.put("soundEnabled", data.soundEnabled)
            root.put("hapticsEnabled", data.hapticsEnabled)
            root.put("completedQuests", JSONArray().apply { data.completedQuestIds.forEach { put(it) } })
            root.put("totalVictories", data.totalVictories)
            // apply() grava em disco de forma assíncrona: não bloqueia a thread principal.
            prefs.edit().putString(KEY_SAVE, root.toString()).apply()
        } catch (_: Exception) {
        }
    }

    fun load(): SaveData? {
        val raw = prefs.getString(KEY_SAVE, null) ?: return null
        return try {
            val root = JSONObject(raw)
            val partyArr = root.getJSONArray("party")
            val party = (0 until partyArr.length()).map { heroFromJson(partyArr.getJSONObject(it)) }
            if (party.isEmpty()) return null
            val invArr = root.optJSONArray("inventory") ?: JSONArray()
            val inventory = (0 until invArr.length()).mapNotNull { itemFromJson(invArr.optJSONObject(it)) }
            val questsArr = root.optJSONArray("completedQuests") ?: JSONArray()
            val quests = (0 until questsArr.length()).map { questsArr.getString(it) }.toSet()
            SaveData(
                party = party,
                resources = resourcesFromJson(root.optJSONObject("resources")),
                inventory = inventory,
                soundEnabled = root.optBoolean("soundEnabled", true),
                hapticsEnabled = root.optBoolean("hapticsEnabled", true),
                completedQuestIds = quests,
                totalVictories = root.optInt("totalVictories", 0)
            )
        } catch (_: Exception) {
            null
        }
    }

    fun clear() {
        prefs.edit().remove(KEY_SAVE).apply()
    }

    private fun heroToJson(h: Hero): JSONObject = JSONObject().apply {
        put("id", h.id)
        put("name", h.name)
        put("heroClass", h.heroClass.name)
        put("level", h.level)
        put("xp", h.xp)
        put("maxXp", h.maxXp)
        put("hp", h.hp)
        put("maxHp", h.maxHp)
        put("mp", h.mp)
        put("maxMp", h.maxMp)
        put("maxAp", h.maxAp)
        put("baseAtk", h.baseAtk)
        put("baseDef", h.baseDef)
        put("baseMag", h.baseMag)
        put("baseSpd", h.baseSpd)
        put("critRate", h.critRate)
        put("sanity", h.sanity)
        put("maxSanity", h.maxSanity)
        put("skillPoints", h.skillPoints)
        put("isAlive", h.isAlive)
        h.equippedWeapon?.let { put("weapon", itemToJson(it)) }
        h.equippedArmor?.let { put("armor", itemToJson(it)) }
        h.equippedAccessory?.let { put("accessory", itemToJson(it)) }
    }

    private fun heroFromJson(o: JSONObject): Hero {
        val heroClass = HeroClass.valueOf(o.getString("heroClass"))
        val hp = o.getInt("hp")
        return Hero(
            id = o.getString("id"),
            name = o.getString("name"),
            heroClass = heroClass,
            level = o.optInt("level", 1),
            xp = o.optInt("xp", 0),
            maxXp = o.optInt("maxXp", 100),
            hp = hp,
            maxHp = o.getInt("maxHp"),
            mp = o.getInt("mp"),
            maxMp = o.getInt("maxMp"),
            ap = o.optInt("maxAp", 2),
            maxAp = o.optInt("maxAp", 2),
            baseAtk = o.getInt("baseAtk"),
            baseDef = o.getInt("baseDef"),
            baseMag = o.getInt("baseMag"),
            baseSpd = o.getInt("baseSpd"),
            critRate = o.optInt("critRate", 10),
            sanity = o.optInt("sanity", 100),
            maxSanity = o.optInt("maxSanity", 100),
            skills = GameDatabase.getInitialSkillsForClass(heroClass),
            equippedWeapon = itemFromJson(o.optJSONObject("weapon")),
            equippedArmor = itemFromJson(o.optJSONObject("armor")),
            equippedAccessory = itemFromJson(o.optJSONObject("accessory")),
            skillPoints = o.optInt("skillPoints", 0),
            isAlive = o.optBoolean("isAlive", hp > 0)
        )
    }

    private fun itemToJson(i: Item): JSONObject = JSONObject().apply {
        put("id", i.id)
        put("name", i.name)
        put("description", i.description)
        put("type", i.type.name)
        put("rarity", i.rarity.name)
        put("atkBonus", i.atkBonus)
        put("defBonus", i.defBonus)
        put("magBonus", i.magBonus)
        put("hpBonus", i.hpBonus)
        put("spdBonus", i.spdBonus)
        put("critBonus", i.critBonus)
        put("value", i.value)
        i.effectTag?.let { put("effectTag", it) }
        i.socketedGem?.let { put("socketedGem", it) }
    }

    private fun itemFromJson(o: JSONObject?): Item? {
        if (o == null) return null
        return try {
            Item(
                id = o.getString("id"),
                name = o.getString("name"),
                description = o.optString("description", ""),
                type = ItemType.valueOf(o.getString("type")),
                rarity = Rarity.valueOf(o.getString("rarity")),
                atkBonus = o.optInt("atkBonus"),
                defBonus = o.optInt("defBonus"),
                magBonus = o.optInt("magBonus"),
                hpBonus = o.optInt("hpBonus"),
                spdBonus = o.optInt("spdBonus"),
                critBonus = o.optInt("critBonus"),
                value = o.optInt("value", 10),
                effectTag = if (o.has("effectTag")) o.getString("effectTag") else null,
                socketedGem = if (o.has("socketedGem")) o.getString("socketedGem") else null
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun resourcesToJson(r: PartyResources): JSONObject = JSONObject().apply {
        put("gold", r.gold)
        put("rations", r.rations)
        put("torches", r.torches)
        put("lockpicks", r.lockpicks)
        put("healingPotions", r.healingPotions)
        put("manaElixirs", r.manaElixirs)
        put("arcaneGems", r.arcaneGems)
    }

    private fun resourcesFromJson(o: JSONObject?): PartyResources {
        if (o == null) return PartyResources()
        val d = PartyResources()
        return PartyResources(
            gold = o.optInt("gold", d.gold),
            rations = o.optInt("rations", d.rations),
            torches = o.optInt("torches", d.torches),
            lockpicks = o.optInt("lockpicks", d.lockpicks),
            healingPotions = o.optInt("healingPotions", d.healingPotions),
            manaElixirs = o.optInt("manaElixirs", d.manaElixirs),
            arcaneGems = o.optInt("arcaneGems", d.arcaneGems)
        )
    }

    companion object {
        private const val PREFS_NAME = "crypts_dungeons_save"
        private const val KEY_SAVE = "save_v1"
        private const val SAVE_VERSION = 1
    }
}
