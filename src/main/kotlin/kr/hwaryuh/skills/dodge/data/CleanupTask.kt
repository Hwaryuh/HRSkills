package kr.hwaryuh.skills.dodge.data

import kr.hwaryuh.skills.dodge.DodgeHandler
import kr.hwaryuh.skills.dodge.DodgeCooldown
import kr.hwaryuh.skills.dodge.StaminaSystem
import org.bukkit.scheduler.BukkitRunnable

class CleanupTask(private val dodgeHandler: DodgeHandler, private val dodgeCooldown: DodgeCooldown, private val staminaSystem: StaminaSystem) : BukkitRunnable() {

    override fun run() {
        try {
            dodgeHandler.cleanup()
            dodgeCooldown.cleanup()
            staminaSystem.cleanup()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}