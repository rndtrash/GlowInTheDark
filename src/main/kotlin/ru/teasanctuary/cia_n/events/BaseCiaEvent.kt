package ru.teasanctuary.cia_n.events

import net.kyori.adventure.text.Component

abstract class BaseCiaEvent(open val timestamp: Long) {
    abstract fun toChatMessage(): Component
}