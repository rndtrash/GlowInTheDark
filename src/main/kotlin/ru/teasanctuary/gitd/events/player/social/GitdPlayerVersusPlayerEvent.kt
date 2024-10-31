package ru.teasanctuary.gitd.events.player.social

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import ru.teasanctuary.gitd.events.player.GitdPlayerEvent
import java.util.*

data class GitdPlayerVersusPlayerEvent(val attacker: UUID, override val player: UUID, override val timestamp: Long) : GitdPlayerEvent(player, timestamp) {
    override fun toChatMessage(): Component {
        val offlineAttacker = Bukkit.getOfflinePlayer(attacker)
        val offlineVictim = Bukkit.getOfflinePlayer(player)
        return MiniMessage.miniMessage().deserialize("Игрок <#ff5555>${offlineAttacker.name}</#ff5555> ударил <#55ff55>${offlineVictim.name}</#55ff55>.")
    }
}
