package kr.hwaryuh.skills.dodge

import com.destroystokyo.paper.event.player.PlayerJumpEvent
import kr.hwaryuh.skills.Main
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.util.Vector
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class DodgeHandler(
    private val plugin: Main,
    private val staminaSystem: StaminaSystem
) : Listener {
    private val sneakingPlayers = ConcurrentHashMap<UUID, Long>()
    private val jumpingPlayers = ConcurrentHashMap<UUID, JumpRecord>()
    private val cooldowns = ConcurrentHashMap<UUID, Long>()

    data class JumpRecord(val location: Location, val direction: Vector)

    private val dodgeCooldown: Long
        get() = plugin.dodgeConfig.dodgeCooldown

    fun isOnCooldown(playerUUID: UUID): Boolean {
        val lastUsedTime = cooldowns[playerUUID] ?: return false
        return System.currentTimeMillis() - lastUsedTime < dodgeCooldown
    }

    private fun setCooldown(playerUUID: UUID) {
        cooldowns[playerUUID] = System.currentTimeMillis()
    }

    fun getDodgeCD(playerUUID: UUID): Long {
        val lastUsedTime = cooldowns[playerUUID] ?: return 0L
        val timePassed = System.currentTimeMillis() - lastUsedTime
        return if (timePassed >= dodgeCooldown) 0L else dodgeCooldown - timePassed
    }

    fun getMaxDodgeCD(): Long = dodgeCooldown

    @EventHandler
    fun onPlayerToggleSneak(event: PlayerToggleSneakEvent) {
        val playerUUID = event.player.uniqueId
        if (event.isSneaking) {
            sneakingPlayers[playerUUID] = System.currentTimeMillis()
        } else {
            sneakingPlayers.remove(playerUUID)
        }
    }

    @EventHandler
    fun onPlayerJump(event: PlayerJumpEvent) {
        val player = event.player
        val playerUUID = player.uniqueId

        if (!hasRecentlySneaked(playerUUID, 250L)) return
        if (isOnCooldown(playerUUID)) return
        if (!staminaSystem.canDodge(playerUUID, plugin.dodgeConfig.dodgeCost)) return
        if (!staminaSystem.useStamina(playerUUID, plugin.dodgeConfig.dodgeCost)) return

        recordJump(playerUUID, player.location, player.location.direction)
        setCooldown(playerUUID)

        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            removeJumpRecord(playerUUID)
        }, 10L)
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        val jumpRecord = getJumpRecord(player.uniqueId) ?: return

        val movement = event.to.toVector().subtract(jumpRecord.location.toVector())
        movement.y = 0.0

        if (movement.lengthSquared() > 0.01) {
            dodge(player, movement.normalize())
        } else {
            dodge(player, jumpRecord.direction)
        }

        removeJumpRecord(player.uniqueId)
    }

    private fun hasRecentlySneaked(playerUUID: UUID, threshold: Long): Boolean {
        val sneakTime = sneakingPlayers[playerUUID] ?: return false
        return System.currentTimeMillis() - sneakTime <= threshold
    }

    private fun recordJump(playerUUID: UUID, location: Location, direction: Vector) {
        jumpingPlayers[playerUUID] = JumpRecord(location, direction)
    }

    private fun removeJumpRecord(playerUUID: UUID) {
        jumpingPlayers.remove(playerUUID)
    }

    private fun getJumpRecord(playerUUID: UUID): JumpRecord? = jumpingPlayers[playerUUID]

    private fun dodge(player: Player, direction: Vector) {
        direction.y = 0.0
        val config = plugin.dodgeConfig
        player.velocity = direction.multiply(config.dodgeX).apply { y += config.dodgeY }

        player.world.spawnParticle(Particle.EXPLOSION_LARGE, player.location, 3)

        plugin.getEvadeDamageListener().playerEvade(player.uniqueId)
    }

    fun cleanup() {
        val currentTime = System.currentTimeMillis()
        sneakingPlayers.entries.removeIf { (_, time) -> currentTime - time > 5000 }
        cooldowns.entries.removeIf { (_, time) -> currentTime - time > dodgeCooldown }
    }
}