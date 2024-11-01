package kr.hwaryuh.skills.placeholder

import kr.hwaryuh.skills.Main
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer

class PlaceholderAPIHook(private val plugin: Main): PlaceholderExpansion() {

    override fun getIdentifier(): String = "hsk"

    override fun getAuthor(): String = "useemeonascreen"

    override fun getVersion(): String = "1.0"

    override fun persist(): Boolean = true

    override fun onRequest(player: OfflinePlayer?, params: String): String? {
        if (player == null || !player.isOnline) return null

        return when {
            params.lowercase() == "stamina" -> plugin.staminaSystem.getAvailableStamina(player.uniqueId).toString()
            params.lowercase() == "max_stamina" -> "20.0"
            params.lowercase() == "dodge_cd" -> plugin.dodgeCooldown.getDodgeCD(player.uniqueId).toString()
            params.lowercase() == "dodge_max_cd" -> plugin.dodgeCooldown.getMaxDodgeCD().toString()
            params.lowercase() == "weight" -> plugin.staminaSystem.getWeight(player.player!!).toString()
            params.lowercase() == "max_weight" -> plugin.staminaSystem.getMaxWeight().toString()

            params.lowercase() == "knight_parry_cd" -> plugin.knightPassive.getRemainingCooldown(player.uniqueId).toString()

            else -> null
        }
    }
}