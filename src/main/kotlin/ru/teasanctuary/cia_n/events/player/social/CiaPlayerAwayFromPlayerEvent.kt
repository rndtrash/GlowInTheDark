package ru.teasanctuary.cia_n.events.player.social

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import ru.teasanctuary.cia_n.events.player.CiaPlayerEvent
import java.util.*

data class CiaPlayerAwayFromPlayerEvent(
    val otherPlayer: UUID, override val player: UUID, override val timestamp: Long
) : CiaPlayerEvent(player, timestamp) {
    override fun toChatMessage(): Component {
        val offlinePlayer = Bukkit.getOfflinePlayer(player)
        val offlineOtherPlayer = Bukkit.getOfflinePlayer(otherPlayer)
        return MiniMessage.miniMessage()
            .deserialize("Игрок <#ffff55>${offlinePlayer.name}</#ffff55> отошёл от <#55ffff>${offlineOtherPlayer.name}</#55ffff>.")
    }
}