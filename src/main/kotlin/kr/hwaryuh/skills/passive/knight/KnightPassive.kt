package kr.hwaryuh.skills.passive.knight

import kr.hwaryuh.skills.Main
import kr.hwaryuh.skills.config.AnotherConfig
import kr.hwaryuh.skills.passive.PassiveSkillManager
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerAnimationEvent
import org.bukkit.event.player.PlayerAnimationType
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.acos

class KnightPassive(private val plugin: Main, private val passiveManager: PassiveSkillManager, private val config: AnotherConfig) : Listener {
    private val recentLeftClick = ConcurrentHashMap<UUID, Double>()
    private val parryCooldowns = ConcurrentHashMap<UUID, Double>()

    init {
        startCleanupTask()
    }

    private fun startCleanupTask() {
        object : BukkitRunnable() {
            override fun run() {
                cleanup()
            }
        }.runTaskTimer(plugin, 20L * 180L, 20L * 180L) // 3분
    }

    private fun cleanup() {
        val currentTime = System.currentTimeMillis() / 1000.0
        recentLeftClick.entries.removeIf { (_, timestamp) -> currentTime - timestamp > config.getKnightLeftClickSustain() / 1000.0 }
        parryCooldowns.entries.removeIf { (_, timestamp) -> currentTime - timestamp > config.getKnightParryCooldowns() }
    }

    @EventHandler
    fun onPlayerAnimation(event: PlayerAnimationEvent) {
        if (event.animationType == PlayerAnimationType.ARM_SWING) {
            if (passiveManager.isKnightWithMainWeapon(event.player)) {
                recordLeftClick(event.player.uniqueId)
            }
        }
    }

    @EventHandler
    fun onEntityDamage(event: EntityDamageByEntityEvent) {
        val victim = event.entity
        val attacker = event.damager

        if (victim !is Player) return
        if (!passiveManager.isKnightWithMainWeapon(victim)) return
        if (isOnCooldown(victim.uniqueId)) return

        val damageDirection = when {
            attacker is Projectile -> attacker.velocity.multiply(-1).normalize()
            else -> attacker.location.toVector()
                .subtract(victim.location.toVector())
                .normalize()
        }

        if (wasRecentlyClicked(victim.uniqueId) && isInParryAngle(victim, damageDirection)) {
            event.isCancelled = true
            config.playSound(victim, "knight.parry-sound")
            setParryCooldown(victim.uniqueId)

            if (attacker is Projectile) {
                attacker.velocity = attacker.velocity.multiply(-1)
            }
        }
    }

    private fun isInParryAngle(player: Player, damageDirection: Vector): Boolean {
        val playerDirection = player.location.direction
        val angle = getAngleBetweenVectors(playerDirection, damageDirection)
        return angle <= (config.getKnightParryAngle() / 2)
    }

    private fun getAngleBetweenVectors(v1: Vector, v2: Vector): Double {
        val dot = v1.dot(v2)
        val v1Magnitude = v1.length()
        val v2Magnitude = v2.length()

        return Math.toDegrees(acos(dot / (v1Magnitude * v2Magnitude)).coerceIn(-1.0, 1.0))
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val uuid = event.player.uniqueId
        removePlayer(uuid)
    }

    private fun recordLeftClick(uuid: UUID) {
        recentLeftClick[uuid] = System.currentTimeMillis() / 1000.0
    }

    private fun wasRecentlyClicked(uuid: UUID): Boolean {
        val lastClick = recentLeftClick[uuid] ?: return false
        val timeDiff = System.currentTimeMillis() / 1000.0 - lastClick
        return timeDiff <= config.getKnightLeftClickSustain() / 1000.0
    }

    private fun setParryCooldown(uuid: UUID) {
        parryCooldowns[uuid] = System.currentTimeMillis() / 1000.0
    }

    private fun isOnCooldown(uuid: UUID): Boolean {
        val lastParry = parryCooldowns[uuid] ?: return false
        return System.currentTimeMillis() / 1000.0 - lastParry < config.getKnightParryCooldowns()
    }

    fun getRemainingCooldown(uuid: UUID): Double {
        val lastParry = parryCooldowns[uuid] ?: return 0.0
        val timePassed = System.currentTimeMillis() / 1000.0 - lastParry
        return maxOf(0.0, config.getKnightParryCooldowns() - timePassed)
    }

    private fun removePlayer(uuid: UUID) {
        recentLeftClick.remove(uuid)
        parryCooldowns.remove(uuid)
    }

    fun shutdown() {
        recentLeftClick.clear()
        parryCooldowns.clear()
    }
}