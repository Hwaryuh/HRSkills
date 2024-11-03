package kr.hwaryuh.skills.passive.assassin

import kr.hwaryuh.skills.passive.PassiveSkillManager
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import io.lumine.mythic.bukkit.events.MythicDamageEvent
import kr.hwaryuh.skills.config.AnotherConfig

class AssassinPassive(
    private val passiveManager: PassiveSkillManager,
    private val config: AnotherConfig
) : Listener {

    @EventHandler
    fun onEntityDamage(event: EntityDamageByEntityEvent) {
        val attacker = event.damager
        if (attacker !is Player) return

        if (!passiveManager.isAssassin(attacker)) return

        if (isBehindAttack(attacker, event.entity)) {
            val originalDamage = event.damage
            val multiplier = config.getAssassinBehindAttackMultiplier() - 1.0 // 1.5 -> 0.5
            val additionalDamage = originalDamage * multiplier

            event.damage = originalDamage + additionalDamage
            config.playSound(attacker, "assassin.behind-attack-sound")
        }
    }

    @EventHandler
    fun onMythicDamage(event: MythicDamageEvent) {
        val attacker = event.caster.entity.bukkitEntity
        if (attacker !is Player) return

        if (!passiveManager.isAssassin(attacker)) return

        if (isBehindAttack(attacker, event.target.bukkitEntity)) {
            val originalDamage = event.damage
            val multiplier = config.getAssassinBehindAttackMultiplier() - 1.0 // 1.5 -> 0.5
            val additionalDamage = originalDamage * multiplier

            event.setDamage(originalDamage + additionalDamage)
            config.playSound(attacker, "assassin.behind-attack-sound")
        }
    }

    private fun isBehindAttack(attacker: Player, target: org.bukkit.entity.Entity): Boolean {
        val targetEyeLocation = target.location.add(0.0, target.height / 2.0, 0.0)
        val attackerEyeLocation = attacker.eyeLocation
        val targetDirection = target.location.direction
        val attackerDirection = attackerEyeLocation.toVector().subtract(targetEyeLocation.toVector()).normalize()
        val angle = Math.toDegrees(attackerDirection.angle(targetDirection).toDouble())

        return angle >= config.getAssassinBehindAttackAngle()
    }
}