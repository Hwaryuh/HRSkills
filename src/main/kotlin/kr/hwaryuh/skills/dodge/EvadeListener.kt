package kr.hwaryuh.skills.dodge

import kr.hwaryuh.skills.Main
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerRespawnEvent
import java.util.UUID

private data class EvadeState(
    val endTime: Long,
    var evadedCount: Int = 0
)

class EvadeListener(
    private val plugin: Main,
    private val staminaSystem: StaminaSystem
) : Listener {
    private val evadingPlayers = mutableMapOf<UUID, EvadeState>()
    private val maxEvade: Int
        get() = plugin.dodgeConfig.maxEvadeCount

    @EventHandler
    fun onPlayerDamage(event: EntityDamageEvent) {
        val player = event.entity as? Player ?: return
        val currentTime = System.currentTimeMillis()

        if (!isPlayerEvading(player.uniqueId, currentTime)) return

        val evadeState = evadingPlayers[player.uniqueId]!!

        if (evadeState.evadedCount < maxEvade) {
            if (event.cause != EntityDamageEvent.DamageCause.FIRE_TICK &&
                event.cause != EntityDamageEvent.DamageCause.WITHER &&
                event.cause != EntityDamageEvent.DamageCause.POISON &&
                event.cause != EntityDamageEvent.DamageCause.DROWNING &&
                event.cause != EntityDamageEvent.DamageCause.SUFFOCATION &&
                event.cause != EntityDamageEvent.DamageCause.STARVATION) {

                event.isCancelled = true
                evadeState.evadedCount++

                player.playSound(player.location, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, -2.0f)
                // player.playSound(player.location, "hdg_sounds:ysl.dodge_parry", 1.0f, 1.0f)

                if (evadeState.evadedCount >= maxEvade) {
                    evadingPlayers.remove(player.uniqueId)
                }
            }

            if (event is EntityDamageByEntityEvent) {
                if (event.damager is Projectile) { event.damager.remove() }
            }
        }
    }

    @EventHandler
    fun onPlayerRespawn(event: PlayerRespawnEvent) {
        staminaSystem.resetStamina(event.player.uniqueId)
    }

    fun playerEvade(playerUUID: UUID) {
        val evadeTime = plugin.dodgeConfig.evadeTime
        val endTime = System.currentTimeMillis() + evadeTime
        evadingPlayers[playerUUID] = EvadeState(endTime)

        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            evadingPlayers.remove(playerUUID)
        }, evadeTime / 50)
    }

    private fun isPlayerEvading(playerUUID: UUID, currentTime: Long): Boolean {
        val state = evadingPlayers[playerUUID] ?: return false
        return currentTime < state.endTime
    }
}