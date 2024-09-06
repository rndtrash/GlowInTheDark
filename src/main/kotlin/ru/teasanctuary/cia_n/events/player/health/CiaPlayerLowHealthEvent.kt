package ru.teasanctuary.cia_n.events.player.health

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import ru.teasanctuary.cia_n.events.player.CiaPlayerEvent
import java.util.*

data class CiaPlayerLowHealthEvent(override val player: UUID, override val timestamp: Long) : CiaPlayerEvent(player, timestamp) {
    override fun toChatMessage(): Component {
        val offlineVictim = Bukkit.getOfflinePlayer(player)
        return MiniMessage.miniMessage().deserialize("<#ffff55>У игрока <#55ff55>${offlineVictim.name}</#55ff55> низкий уровень здоровья.</#ffff55>")
    }
}
