package kr.hwaryuh.skills.dodge

import kr.hwaryuh.skills.Main
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class DodgeCooldown(private val plugin: Main) {
    private val cooldowns = ConcurrentHashMap<UUID, Long>()
    private val dodgeCooldown: Long
        get() = plugin.dodgeConfig.dodgeCooldown

    fun isOnCooldown(playerUUID: UUID): Boolean {
        val lastUsedTime = cooldowns[playerUUID] ?: return false
        return System.currentTimeMillis() - lastUsedTime < dodgeCooldown
    }

    fun setCooldown(playerUUID: UUID) {
        cooldowns[playerUUID] = System.currentTimeMillis()
    }

    fun getDodgeCD(playerUUID: UUID): Long {
        val lastUsedTime = cooldowns[playerUUID] ?: return 0L
        val timePassed = System.currentTimeMillis() - lastUsedTime
        return if (timePassed >= dodgeCooldown) 0L else dodgeCooldown - timePassed
    }

    fun getMaxDodgeCD(): Long = dodgeCooldown

    fun cleanup() {
        val currentTime = System.currentTimeMillis()
        cooldowns.entries.removeIf { (_, time) -> currentTime - time > dodgeCooldown }
    }
}