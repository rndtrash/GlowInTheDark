package ru.teasanctuary.gitd.events.player

import ru.teasanctuary.gitd.events.BaseGitdEvent
import java.util.*

abstract class GitdPlayerEvent(open val player: UUID, override val timestamp: Long) : BaseGitdEvent(timestamp)