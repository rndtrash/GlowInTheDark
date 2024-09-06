package ru.teasanctuary.cia_n.events.player

import ru.teasanctuary.cia_n.events.BaseCiaEvent
import java.util.*

abstract class CiaPlayerEvent(open val player: UUID, override val timestamp: Long) : BaseCiaEvent(timestamp)