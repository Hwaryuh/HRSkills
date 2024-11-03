package kr.hwaryuh.skills.mechanics

import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class MechanicManager : Listener {
    @EventHandler
    fun onMythicMechanicLoad(event: MythicMechanicLoadEvent) {
        if (event.mechanicName.equals("swoop", ignoreCase = true)) event.register(SwoopMechanic(event.config))
    }
}