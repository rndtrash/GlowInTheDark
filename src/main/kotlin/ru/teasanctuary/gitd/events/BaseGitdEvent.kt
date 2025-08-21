package ru.teasanctuary.gitd.events

import net.kyori.adventure.text.Component
import org.bukkit.OfflinePlayer

abstract class BaseGitdEvent(
    /**
     * Время с момента генерации мира
     */
    open val timestamp: Long
) {
    companion object {
        fun playerTeleportMsg(player: OfflinePlayer) = "<u><click:run_command:/tp ${player.uniqueId}>${player.name}</click></u>"
    }

    abstract fun toChatMessage(): Component
}