package ru.teasanctuary.gitd.events

import net.kyori.adventure.text.Component

abstract class BaseGitdEvent(open val timestamp: Long) {
    abstract fun toChatMessage(): Component
}