package ru.teasanctuary.gitd

import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.event.HandlerList
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.bukkit.plugin.java.JavaPlugin
import ru.teasanctuary.gitd.config.GitdConfig
import ru.teasanctuary.gitd.events.BaseGitdEvent
import ru.teasanctuary.gitd.events.server.GitdServerStartEvent
import ru.teasanctuary.gitd.events.server.GitdServerStopEvent
import java.util.logging.Level

class Gitd : JavaPlugin() {
    companion object {
        /**
         * Преобразование реальных секунд к игровым.
         *
         * Пример: World.getGameTime / REAL_SECONDS_TO_GAME_TIME
         */
        const val REAL_SECONDS_TO_GAME_TIME: Long = 20
    }

    /**
     * Разрешение на получение логов. По-умолчанию сообщения отправляются всем операторам в сети, а также
     * на консоль сервера.
     */
    private val permissionObserve = Permission("gitd.observe", PermissionDefault.OP)

    /**
     * Главный мир, название которого указано в файле конфигурации самого сервера.
     */
    // TODO: Очень хреновое решение, надо будет потом доработать
    lateinit var defaultWorld: World

    /**
     * Конфигурация плагина.
     */
    lateinit var gitdConfig: GitdConfig

    /**
     * Время с момента генерации мира по-умолчанию.
     */
    val worldTime: Long
        get() = defaultWorld.gameTime

    /**
     * Выводит событие в красивом виде для операторов в сети, и в читаемом машиной формате для лога сервера.
     */
    fun pushEvent(event: BaseGitdEvent) {
        Bukkit.broadcast(event.toChatMessage(), permissionObserve.name)
        pushEventServer(event)
    }

    /**
     * Выводит сообщение только в консоль сервера. Применимо для событий, повторяющих те, что уже есть в игре.
     *
     * Например, подключение игрока или смерть.
     */
    fun pushEventServer(event: BaseGitdEvent) {
        logger.log(Level.INFO, event.toString())
    }

    override fun onEnable() {
        defaultWorld = Bukkit.getServer().worlds[0]

        ConfigurationSerialization.registerClass(GitdConfig::class.java)
        gitdConfig = config.getSerializable(
            "gitd", GitdConfig::class.java, GitdConfig(mapOf())
        ) ?: GitdConfig()

        Bukkit.getPluginManager().addPermission(permissionObserve)

        Bukkit.getPluginManager().registerEvents(EventLogger(this), this)

        pushEvent(GitdServerStartEvent(worldTime))
    }

    override fun onDisable() {
        pushEvent(GitdServerStopEvent(worldTime))

        config.set("gitd", gitdConfig)
        saveConfig()

        HandlerList.unregisterAll(this)
    }
}
