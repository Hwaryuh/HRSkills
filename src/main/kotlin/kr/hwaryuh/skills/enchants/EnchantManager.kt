package kr.hwaryuh.skills.enchants

import com.willfp.ecoenchants.enchant.EcoEnchants
import org.bukkit.inventory.ItemStack

class EnchantManager {
    companion object {
        fun getEnchantLevel(item: ItemStack, enchantID: String): Int {
            val enchant = EcoEnchants.getByID(enchantID) ?: return 0
            return item.getEnchantmentLevel(enchant.enchantment)
        }

        fun hasEnchant(item: ItemStack, enchantID: String): Boolean {
            return getEnchantLevel(item, enchantID) > 0
        }

        // val hasAny = EnchantManager.hasAnyEnchant(item, "1", "2", "3")
        fun hasAnyEnchant(item: ItemStack, vararg enchantIDs: String): Boolean {
            return enchantIDs.any { hasEnchant(item, it) }
        }

        // val hasAll = EnchantManager.hasAllEnchants(item, "1", "2")
        fun hasAllEnchants(item: ItemStack, vararg enchantIDs: String): Boolean {
            return enchantIDs.all { hasEnchant(item, it) }
        }
    }
}