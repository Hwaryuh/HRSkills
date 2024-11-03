package kr.hwaryuh.skills

import kr.hwaryuh.skills.dodge.data.CleanupTask
import kr.hwaryuh.skills.config.DodgeConfig
import kr.hwaryuh.skills.config.PassiveConfig
import kr.hwaryuh.skills.dodge.*
import kr.hwaryuh.skills.passive.PassiveSkillManager
import kr.hwaryuh.skills.passive.assassin.AssassinPassive
import kr.hwaryuh.skills.passive.knight.KnightPassive
import kr.hwaryuh.skills.placeholder.PlaceholderAPIHook
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {
    lateinit var dodgeConfig: DodgeConfig private set
    lateinit var passiveConfig: PassiveConfig private set
    lateinit var staminaSystem: StaminaSystem private set
    lateinit var passiveSkillManager: PassiveSkillManager private set
    lateinit var knightPassive: KnightPassive private set
    lateinit var assassinPassive: AssassinPassive private set
    lateinit var dodgeHandler: DodgeHandler private set
    private lateinit var evadeListener: EvadeListener

    companion object {
        lateinit var instance: Main private set
    }

    override fun onEnable() {
        instance = this

        dodgeConfig = DodgeConfig(this)
        passiveConfig = PassiveConfig(this)
        passiveSkillManager = PassiveSkillManager(this, passiveConfig)

        initializeComponents()
        registerEventListeners()

        CleanupTask(dodgeHandler, staminaSystem).runTaskTimer(this, 6000L, 6000L) // 5분

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) PlaceholderAPIHook(this).register()

        getCommand("hsk")?.apply {
            setExecutor(this@Main)
            permission = "hsk.reload"
            permissionMessage(null)
        }
    }

    private fun initializeComponents() {
        staminaSystem = StaminaSystem(this)
        evadeListener = EvadeListener(this, staminaSystem)
        dodgeHandler = DodgeHandler(this, staminaSystem)
        knightPassive = KnightPassive(this, passiveSkillManager, passiveConfig)
        assassinPassive = AssassinPassive(passiveSkillManager, passiveConfig)
    }

    private fun registerEventListeners() {
        server.pluginManager.apply {
            registerEvents(dodgeHandler, this@Main)
            registerEvents(evadeListener, this@Main)
            registerEvents(knightPassive, this@Main)
            registerEvents(assassinPassive, this@Main)
        }
    }

    private fun reloadPluginConfig() {
        dodgeConfig.loadConfig()
        passiveConfig.reloadConfig("passive-skills")
        staminaSystem.restartRegenerationTask()

        HandlerList.unregisterAll(this)

        evadeListener = EvadeListener(this, staminaSystem)
        dodgeHandler = DodgeHandler(this, staminaSystem)
        assassinPassive = AssassinPassive(passiveSkillManager, passiveConfig)
        knightPassive = KnightPassive(this, passiveSkillManager, passiveConfig)

        server.pluginManager.apply {
            registerEvents(dodgeHandler, this@Main)
            registerEvents(evadeListener, this@Main)
            registerEvents(assassinPassive, this@Main)
            registerEvents(knightPassive, this@Main)
        }
    }

    fun getEvadeDamageListener(): EvadeListener = evadeListener

    override fun onDisable() {
        saveConfig()
        passiveConfig.saveAll()
        knightPassive.shutdown()
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isNotEmpty() && args[0].equals("reload", ignoreCase = true)) {
            if (!sender.hasPermission("hsk.reload")) {
                sender.sendMessage("§c알 수 없는 명령어입니다.")
                return true
            } else {
                reloadPluginConfig()
                sender.sendMessage("§aReloaded Skill Core.")
                return true
            }
        }
        return false
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
        if (sender !is Player || !sender.hasPermission("hsk.reload")) return mutableListOf()

        if (command.name.equals("hsk", ignoreCase = true)) {
            if (args.size == 1) {
                return mutableListOf("reload").filter { it.startsWith(args[0], ignoreCase = true) }.toMutableList()
            }
        }
        return mutableListOf()
    }
}