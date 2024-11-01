package kr.hwaryuh.skills.config

import kr.hwaryuh.skills.Main
import org.bukkit.Material
import org.bukkit.configuration.file.FileConfiguration

class DodgeConfig(private val plugin: Main) {
    private lateinit var config: FileConfiguration

    var dodgeCooldown: Long = 0
    var dodgeCost: Float = 0f
    var dodgeX: Double = 0.0
    var dodgeY: Double = 0.0
    var regenerationRate: Long = 0
    var regeneration: Float = 0f
    var evadeTime: Long = 0
    var maxEvadeCount: Int = 5
    var armorWeights: Map<Material, Float> = mapOf()

    init {
        loadConfig()
    }

    fun loadConfig() {
        plugin.saveDefaultConfig()
        plugin.reloadConfig()
        config = plugin.config

        dodgeCooldown = config.getLong("dodge-cooldown")
        dodgeCost = config.getDouble("dodge-stamina-cost").toFloat()
        dodgeX = config.getDouble("dodge-x")
        dodgeY = config.getDouble("dodge-y")
        regenerationRate = config.getLong("stamina-regeneration-rate")
        regeneration = config.getDouble("stamina-regeneration").toFloat()
        evadeTime = config.getLong("evade-damage-time")
        maxEvadeCount = config.getInt("max-evade-count")
        armorWeights = loadArmorWeights()
    }

    private fun loadArmorWeights(): Map<Material, Float> {
        val weights = mutableMapOf<Material, Float>()
        val armorTypes = listOf("leather", "chainmail", "iron", "golden", "diamond", "netherite")
        val armorPieces = listOf("helmet", "chestplate", "leggings", "boots")

        for (type in armorTypes) {
            val section = config.getConfigurationSection(type) ?: continue
            for (piece in armorPieces) {
                val material = Material.getMaterial("${type.uppercase()}_${piece.uppercase()}") ?: continue
                weights[material] = section.getDouble(piece).toFloat()
            }
        }
        return weights
    }
}