package ru.teasanctuary.gitd.events.server

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import ru.teasanctuary.gitd.events.BaseGitdEvent

data class GitdServerStopEvent(override val timestamp: Long) : BaseGitdEvent(timestamp) {
    companion object {
        private val msg = MiniMessage.miniMessage().deserialize("Сервер остановлен.")
    }

    override fun toChatMessage(): Component {
        return msg
    }
}
