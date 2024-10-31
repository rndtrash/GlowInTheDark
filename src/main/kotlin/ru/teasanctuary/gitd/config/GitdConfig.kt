package ru.teasanctuary.gitd.config

import org.bukkit.configuration.serialization.ConfigurationSerializable

class GitdConfig(
    /**
     * Задержка между сообщениями о получении игроком урона
     */
    val playerDamageEventDelay: Long,
    /**
     * Задержка между сообщениями о вхождении игрока в структуру
     */
    val playerEnterStructureDelay: Long,
    /**
     * Расстояние между двумя игроками, которая считается за сближение
     */
    val playerVisitPlayerDistance: Int,
    /**
     * Добавочное расстояние между игроками, предотвращающее событие посещения-расхождения от малейшего чиха
     */
    val playerLeavePlayerDistance: Int
) : ConfigurationSerializable {
    companion object {
        const val DEFAULT_PLAYER_DAMAGE_EVENT_DELAY = 30L
        const val DEFAULT_PLAYER_ENTER_STRUCTURE_DELAY = 60L
        const val DEFAULT_PLAYER_VISIT_PLAYER_DISTANCE = 32 // 2 чанка
        const val DEFAULT_PLAYER_LEAVE_PLAYER_DISTANCE = 16 // +1 чанк
    }

    constructor(config: Map<String, Object>) : this(
        config["player-damage-event-delay"] as? Long ?: DEFAULT_PLAYER_DAMAGE_EVENT_DELAY,
        config["player-enter-structure-delay"] as? Long ?: DEFAULT_PLAYER_ENTER_STRUCTURE_DELAY,
        config["player-visit-player-distance"] as? Int ?: DEFAULT_PLAYER_VISIT_PLAYER_DISTANCE,
        config["player-leave-player-distance"] as? Int ?: DEFAULT_PLAYER_LEAVE_PLAYER_DISTANCE
    )

    constructor() : this(
        DEFAULT_PLAYER_DAMAGE_EVENT_DELAY,
        DEFAULT_PLAYER_ENTER_STRUCTURE_DELAY,
        DEFAULT_PLAYER_VISIT_PLAYER_DISTANCE,
        DEFAULT_PLAYER_LEAVE_PLAYER_DISTANCE
    )

    override fun serialize(): MutableMap<String, Any> {
        return mutableMapOf(
            Pair("player-damage-event-delay", playerDamageEventDelay),
            Pair("player-enter-structure-delay", playerEnterStructureDelay),
            Pair("player-visit-player-distance", playerVisitPlayerDistance),
            Pair("player-leave-player-distance", playerLeavePlayerDistance)
        )
    }
}
