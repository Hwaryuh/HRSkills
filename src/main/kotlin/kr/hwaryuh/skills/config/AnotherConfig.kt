package kr.hwaryuh.skills.config

import kr.hwaryuh.skills.Main
import org.bukkit.Sound
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File

class AnotherConfig(val plugin: Main) {
    private val configFiles = mutableMapOf<String, FileConfiguration>()

    data class SoundData(
        val key: String,
        val volume: Float,
        val pitch: Float
    )

    init {
        plugin.saveDefaultConfig()
        configFiles["config"] = plugin.config
        createConfig("passive-skills")
    }

    fun createConfig(fileName: String): FileConfiguration {
        if (configFiles.containsKey(fileName)) {
            return configFiles[fileName]!!
        }

        val file = File(plugin.dataFolder, "$fileName.yml")

        if (!file.exists()) {
            plugin.saveResource("$fileName.yml", false)
        }

        val fileConfig = YamlConfiguration.loadConfiguration(file)
        configFiles[fileName] = fileConfig
        return fileConfig
    }

    fun getConfig(fileName: String): FileConfiguration? {
        return configFiles[fileName]
    }

    fun saveConfig(fileName: String) {
        val config = configFiles[fileName] ?: return
        val file = File(plugin.dataFolder, "$fileName.yml")
        config.save(file)
    }

    fun reloadConfig(fileName: String) {
        val file = File(plugin.dataFolder, "$fileName.yml")
        if (file.exists()) {
            configFiles[fileName] = YamlConfiguration.loadConfiguration(file)
        }
    }

    fun saveAll() {
        configFiles.forEach { (fileName, config) ->
            val file = File(plugin.dataFolder, "$fileName.yml")
            config.save(file)
        }
    }

    fun reloadAll() {
        configFiles.clear()
        configFiles["config"] = plugin.config

        plugin.dataFolder.listFiles()?.forEach { file ->
            if (file.extension == "yml" && file.nameWithoutExtension != "config") {
                val fileName = file.nameWithoutExtension
                configFiles[fileName] = YamlConfiguration.loadConfiguration(file)
            }
        }
    }

    private fun getSoundDataList(path: String): List<SoundData> {
        val defaultSound = listOf(SoundData("minecraft:entity_player_attack_crit", 1.0f, 0.5f))

        val soundString = getConfig("passive-skills")?.getString(path)
            ?: return defaultSound

        return try {
            soundString.split(";").map { sound ->
                val parts = sound.trim().split(",")
                if (parts.size != 3) return defaultSound

                SoundData(
                    key = parts[0].trim(),
                    volume = parts[1].trim().toFloatOrNull() ?: 1.0f,
                    pitch = parts[2].trim().toFloatOrNull() ?: 0.5f
                )
            }
        } catch (e: Exception) {
            defaultSound
        }
    }

    fun playSound(player: Player, soundPath: String) {
        val soundDataList = getSoundDataList(soundPath)

        for (soundData in soundDataList) {
            try {
                if (soundData.key.startsWith("minecraft:")) {
                    val soundName = soundData.key.substringAfter("minecraft:")
                        .uppercase()
                        .replace(".", "_")
                    val sound = Sound.valueOf(soundName)
                    player.playSound(
                        player.location,
                        sound,
                        soundData.volume,
                        soundData.pitch
                    )
                } else {
                    player.playSound(
                        player.location,
                        soundData.key,
                        soundData.volume,
                        soundData.pitch
                    )
                }
            } catch (e: Exception) {
                plugin.logger.warning("사운드 재생 중 오류 발생 ($soundPath - ${soundData.key}): ${e.message}")
            }
        }
    }

    fun getAssassinClasses(): List<String> = getConfig("passive-skills")?.getStringList("classes.assassin") ?: listOf("Misty", "Isaac")

    fun getKnightClasses(): List<String> = getConfig("passive-skills")?.getStringList("classes.knight") ?: listOf("Beryl", "Skadi")

    fun getAssassinBehindAttackMultiplier(): Double = getConfig("passive-skills")?.getDouble("assassin.damage-multiplier", 1.5) ?: 1.5

    fun getAssassinBehindAttackAngle(): Double = getConfig("passive-skills")?.getDouble("assassin.minimum-angle", 90.0) ?: 90.0

    fun getKnightLeftClickSustain(): Long = getConfig("passive-skills")?.getLong("knight.click-sustain", 300) ?: 300

    fun getKnightParryAngle(): Double = getConfig("passive-skills")?.getDouble("knight.parry-angle", 90.0) ?: 90.0

    fun getKnightParryCooldowns(): Double = getConfig("passive-skills")?.getDouble("knight.parry-cooldown", 10.0) ?: 10.0

}