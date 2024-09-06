package ru.teasanctuary.cia_n.events.player

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import ru.teasanctuary.cia_n.events.BaseCiaEvent
import java.util.*

data class CiaPlayerLeaveEvent(override val player: UUID, override val timestamp: Long) : CiaPlayerEvent(player, timestamp) {
    override fun toChatMessage(): Component {
        val offlinePlayer = Bukkit.getOfflinePlayer(player)
        return MiniMessage.miniMessage().deserialize("Игрок <#55ff55>${offlinePlayer.name}</#55ff55> вышел из сервера.")
    }
}
