package kr.hwaryuh.skills.dodge

import kr.hwaryuh.skills.Main
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import kr.hwaryuh.skills.enchants.EnchantManager
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class StaminaSystem(private val plugin: Main) {
    private val stamina = ConcurrentHashMap<UUID, Float>()
    private val maxWeight = 20f
    private val maxStamina = 20f
    private var regenerationTask: BukkitRunnable? = null

    init {
        startRegenerationTask()
    }

    private fun getStamina(playerUUID: UUID): Float {
        return stamina.getOrDefault(playerUUID, maxStamina)
    }

    fun getAvailableStamina(playerUUID: UUID): Float {
        val player = plugin.server.getPlayer(playerUUID) ?: return 0f
        return availableStamina(player, getStamina(playerUUID))
    }

    fun useStamina(playerUUID: UUID, cost: Float): Boolean {
        val player = plugin.server.getPlayer(playerUUID) ?: return false
        val availableStam = availableStamina(player, getStamina(playerUUID))

        if (availableStam >= cost) {
            stamina[playerUUID] = getStamina(playerUUID) - cost
            return true
        }
        return false
    }

    fun resetStamina(playerUUID: UUID) {
        stamina[playerUUID] = maxStamina
    }

    private fun regenerateStamina(playerUUID: UUID) {
        val currentStamina = getStamina(playerUUID)
        if (currentStamina < maxStamina) {
            stamina[playerUUID] = minOf(currentStamina + plugin.dodgeConfig.regeneration, maxStamina)
        }
    }

    fun restartRegenerationTask() {
        stopRegenerationTask()
        startRegenerationTask()
    }

    private fun stopRegenerationTask() {
        try {
            regenerationTask?.cancel()
        } catch (e: Exception) {
            plugin.logger.warning("Failed to stop regeneration task: ${e.message}")
        } finally {
            regenerationTask = null
        }
    }

    private fun startRegenerationTask() {
        regenerationTask = object : BukkitRunnable() {
            override fun run() {
                stamina.keys.forEach { regenerateStamina(it) }
            }
        }.apply {
            val rate = plugin.dodgeConfig.regenerationRate
            runTaskTimer(plugin, rate, rate)
        }
    }

    private fun availableStamina(player: Player, currentStamina: Float): Float {
        val weight = calculateWeight(player)
        return maxOf(0f, currentStamina - weight)
    }

    fun canDodge(playerUUID: UUID, dodgeCost: Float): Boolean {
        val player = plugin.server.getPlayer(playerUUID) ?: return false
        if (player.foodLevel <= 6) return false
        return availableStamina(player, getStamina(playerUUID)) >= dodgeCost
    }

    fun getWeight(player: Player): Float = calculateWeight(player)

    fun getMaxWeight(): Float = maxWeight

    private fun calculateWeight(player: Player): Float {
        return player.inventory.armorContents.filterNotNull().sumOf { armorPiece ->
            calculateArmor(armorPiece).toDouble()
        }.toFloat()
    }

    private fun calculateArmor(armorPiece: ItemStack): Float {
        val heaviness = plugin.dodgeConfig.armorWeights[armorPiece.type] ?: 0f
        val lightnessLevel = getLightnessLevel(armorPiece)
        return maxOf(0f, heaviness - lightnessLevel * 1.0f)
    }

    private fun getLightnessLevel(item: ItemStack): Int {
        return EnchantManager.getEnchantLevel(item, "lightness")
    }

    fun cleanup() {
        stamina.keys.removeIf { playerUUID ->
            plugin.server.getPlayer(playerUUID)?.isOnline != true
        }
    }
}