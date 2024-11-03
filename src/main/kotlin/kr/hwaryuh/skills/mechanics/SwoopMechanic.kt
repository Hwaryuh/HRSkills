package kr.hwaryuh.skills.mechanics

import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.ITargetedEntitySkill
import io.lumine.mythic.api.skills.SkillMetadata
import io.lumine.mythic.api.skills.SkillResult
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

class SwoopMechanic(config: MythicLineConfig) : ITargetedEntitySkill {
    private val blocks: Double = config.getDouble(arrayOf("block", "b"), 1.0)

    override fun castAtEntity(data: SkillMetadata, target: AbstractEntity): SkillResult {
        val caster = data.caster.entity.bukkitEntity
        if (caster !is Player) return SkillResult.INVALID_TARGET

        val targetEntity = target.bukkitEntity
        if (targetEntity is Player) return SkillResult.INVALID_TARGET

        tpBehindEntity(caster, targetEntity)
        return SkillResult.SUCCESS
    }

    private fun tpBehindEntity(player: Player, entity: Entity) {
        val direction = entity.location.direction
        val behindLocation = entity.location.clone().subtract(direction.multiply(blocks))

        behindLocation.direction = direction
        player.teleport(behindLocation)
    }
}