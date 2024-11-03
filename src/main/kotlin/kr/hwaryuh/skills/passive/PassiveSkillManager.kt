package kr.hwaryuh.skills.passive

import kr.hwaryuh.skills.Main
import net.Indyuce.mmocore.api.player.PlayerData
import org.bukkit.entity.Player
import io.lumine.mythic.lib.api.item.NBTItem
import kr.hwaryuh.skills.config.AnotherConfig

class PassiveSkillManager(private val plugin: Main, private val config: AnotherConfig) {

    fun isAssassin(player: Player): Boolean = isClassMatch(player, config.getAssassinClasses())

    fun isKnightWithMainWeapon(player: Player): Boolean {
        if (!isClassMatch(player, config.getKnightClasses())) return false
        return hasMainWeapon(player)
    }

    private fun isClassMatch(player: Player, classes: List<String>): Boolean {
        return try {
            val playerData = PlayerData.get(player)
            val playerClass = playerData.profess.id.uppercase()
            classes.any { it.uppercase() == playerClass }
        } catch (e: Exception) {
            plugin.logger.warning("클래스를 확인할 수 없습니다. (${player.name}: ${e.message})")
            false
        }
    }

    private fun hasMainWeapon(player: Player): Boolean {
        val holdItem = player.inventory.itemInMainHand
        if (holdItem.type.isAir) return false

        val nbtItem = NBTItem.get(holdItem)
        return nbtItem.getString("MMOITEMS_WEAPON_ROLE").uppercase() == "MAIN"
    }
}