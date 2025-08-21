package ru.teasanctuary.gitd.events.player.social

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import ru.teasanctuary.gitd.events.player.GitdPlayerEvent
import java.util.*

data class GitdPlayerNearPlayerEvent(val otherPlayer: UUID, override val player: UUID, override val timestamp: Long) :
    GitdPlayerEvent(player, timestamp) {
    override fun toChatMessage(): Component {
        val offlinePlayer = Bukkit.getOfflinePlayer(player)
        val offlineOtherPlayer = Bukkit.getOfflinePlayer(otherPlayer)
        return MiniMessage.miniMessage()
            .deserialize("Игрок <#ffff55>${playerTeleportMsg(offlinePlayer)}</#ffff55> сейчас рядом с <#55ffff>${playerTeleportMsg(offlineOtherPlayer)}</#55ffff>.")
    }
}