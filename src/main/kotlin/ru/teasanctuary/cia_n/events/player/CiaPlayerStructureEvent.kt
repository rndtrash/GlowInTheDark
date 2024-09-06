package ru.teasanctuary.cia_n.events.player

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.generator.structure.StructureType
import java.util.*

data class CiaPlayerStructureEvent(val structure: StructureType, override val player: UUID, override val timestamp: Long) : CiaPlayerEvent(player, timestamp) {
    override fun toChatMessage(): Component {
        val offlinePlayer = Bukkit.getOfflinePlayer(player)
        return MiniMessage.miniMessage().deserialize("Игрок <#55ff55>${offlinePlayer.name}</#55ff55> нашёл структуру <u>${structure.key}</u>.")
    }
}
