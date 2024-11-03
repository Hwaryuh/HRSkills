package kr.hwaryuh.skills.dodge.data

import kr.hwaryuh.skills.dodge.DodgeHandler
import kr.hwaryuh.skills.dodge.StaminaSystem
import org.bukkit.scheduler.BukkitRunnable

class CleanupTask(private val dodgeHandler: DodgeHandler, private val staminaSystem: StaminaSystem) : BukkitRunnable() {

    override fun run() {
        try {
            dodgeHandler.cleanup()
            staminaSystem.cleanup()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}