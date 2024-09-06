package ru.teasanctuary.cia_n.events.server

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import ru.teasanctuary.cia_n.events.BaseCiaEvent

data class CiaServerStopEvent(override val timestamp: Long) : BaseCiaEvent(timestamp) {
    companion object {
        private val msg = MiniMessage.miniMessage().deserialize("Сервер остановлен.")
    }

    override fun toChatMessage(): Component {
        return msg
    }
}
