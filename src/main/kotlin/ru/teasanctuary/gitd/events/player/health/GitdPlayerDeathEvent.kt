package ru.teasanctuary.gitd.events.player.health

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import ru.teasanctuary.gitd.events.player.GitdPlayerEvent
import java.util.*

data class GitdPlayerDeathEvent(override val player: UUID, override val timestamp: Long) : GitdPlayerEvent(player, timestamp) {
    override fun toChatMessage(): Component {
        val offlineVictim = Bukkit.getOfflinePlayer(player)
        return MiniMessage.miniMessage().deserialize("Игрок <#55ff55>${offlineVictim.name}</#55ff55> умер.")
    }
}
