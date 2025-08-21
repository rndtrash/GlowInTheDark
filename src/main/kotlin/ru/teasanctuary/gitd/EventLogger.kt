package ru.teasanctuary.gitd

import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.attribute.Attribute
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerGameModeChangeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerPortalEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.generator.structure.StructureType
import ru.teasanctuary.gitd.events.player.*
import ru.teasanctuary.gitd.events.player.health.*
import ru.teasanctuary.gitd.events.player.social.*
import java.util.UUID
import kotlin.math.abs

class EventLogger(private val plugin: Gitd) : Listener {
    private val pvpTimeouts = mutableMapOf<Pair<UUID, UUID>, Long>()
    private val damageTimeouts = mutableMapOf<UUID, Long>()
    private val lowHealthTimeouts = mutableMapOf<UUID, Long>()
    private val structureTimeouts = mutableMapOf<Pair<UUID, StructureType>, Long>()
    private val playersTogether = mutableMapOf<Pair<UUID, UUID>, Boolean>()

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        plugin.pushEventServer(GitdPlayerJoinEvent(event.player.uniqueId, plugin.worldTime))
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        cleanUpPlayerTogether(event.player.uniqueId)
        plugin.pushEventServer(GitdPlayerLeaveEvent(event.player.uniqueId, plugin.worldTime))
    }

    @EventHandler
    fun onPlayerDamage(event: EntityDamageEvent) {
        // Событие вызывается для любой сущности, но нас интересуют только игроки
        if (event.entityType != EntityType.PLAYER) return
        val victim = event.entity as? Player
        if (victim == null || !victim.isValid) return

        // При использовании щита наносится нулевой урон
        if (abs(event.finalDamage) <= 0.01) return

        val timestamp = plugin.worldTime
        val attacker = event.damageSource.causingEntity as? Player
        if (attacker != null && attacker.isValid) {
            val lastTime = pvpTimeouts[attacker.uniqueId to victim.uniqueId]
            if (lastTime == null || timestamp - lastTime > plugin.gitdConfig.playerDamageEventDelay * Gitd.REAL_SECONDS_TO_GAME_TIME) {
                plugin.pushEvent(GitdPlayerVersusPlayerEvent(attacker.uniqueId, victim.uniqueId, timestamp))
                pvpTimeouts[attacker.uniqueId to victim.uniqueId] = timestamp
            }
        } else {
            val lastTime = damageTimeouts[victim.uniqueId]
            if (lastTime == null || timestamp - lastTime > plugin.gitdConfig.playerDamageEventDelay * Gitd.REAL_SECONDS_TO_GAME_TIME) {
                plugin.pushEvent(GitdPlayerHurtEvent(victim.uniqueId, timestamp))
                damageTimeouts[victim.uniqueId] = timestamp
            }
        }

        val maxHealth = victim.getAttribute(Attribute.MAX_HEALTH)
        val finalHealth = victim.health - event.finalDamage
        if (maxHealth != null && finalHealth / maxHealth.value <= 0.25) {
            val lastTime = lowHealthTimeouts[victim.uniqueId]
            if (lastTime == null || timestamp - lastTime > plugin.gitdConfig.playerDamageEventDelay * Gitd.REAL_SECONDS_TO_GAME_TIME) {
                plugin.pushEvent(GitdPlayerLowHealthEvent(victim.uniqueId, timestamp))
                lowHealthTimeouts[victim.uniqueId] = timestamp
            }
        }
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        cleanUpPlayerTogether(event.player.uniqueId)
        plugin.pushEventServer(GitdPlayerDeathEvent(event.player.uniqueId, plugin.worldTime))
    }

    @EventHandler
    fun onPlayerGameModeChange(event: PlayerGameModeChangeEvent) {
        if (event.newGameMode != GameMode.SURVIVAL) {
            cleanUpPlayerTogether(event.player.uniqueId)
        }
    }

    @EventHandler
    fun onPlayerEnterPortal(event: PlayerPortalEvent) {
        // Нам не интересны наблюдатели и админы
        if (event.player.gameMode != GameMode.SURVIVAL) return

        // Произошло перемещение между мирами
        plugin.pushEvent(GitdPlayerMoveWorldsEvent(event.to.world, event.player.uniqueId, plugin.worldTime))
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        // Нам не интересны наблюдатели и админы
        if (player.gameMode != GameMode.SURVIVAL) return

        // Проверка расстояний между игроками
        val onlinePlayers = Bukkit.getOnlinePlayers()
        for (otherPlayer in onlinePlayers) {
            if (otherPlayer == player) continue

            val arr = arrayOf(player.uniqueId, otherPlayer.uniqueId).sorted()
            val sortedPair = Pair(arr[0], arr[1])
            val isNearby = playersTogether[sortedPair] ?: false
            val isInSameWorlds = player.world == otherPlayer.world
            if (isNearby) {
                if (otherPlayer.isDead || !isInSameWorlds || player.location.distance(otherPlayer.location) >= plugin.gitdConfig.playerVisitPlayerDistance + plugin.gitdConfig.playerLeavePlayerDistance) {
                    onPlayersAway(player.uniqueId, otherPlayer.uniqueId)
                }
            } else {
                if (isInSameWorlds && player.location.distance(otherPlayer.location) <= plugin.gitdConfig.playerVisitPlayerDistance) {
                    onPlayersTogether(player.uniqueId, otherPlayer.uniqueId)
                }
            }
        }

        val chunkStructures = event.to.chunk.structures
        val timestamp = plugin.worldTime
        for (generatedStructure in chunkStructures) {
            if (!generatedStructure.boundingBox.contains(player.location.toVector())) continue
            val kv = player.uniqueId to generatedStructure.structure.structureType
            val lastTime = structureTimeouts[kv]
            if (lastTime == null || timestamp - lastTime > plugin.gitdConfig.playerEnterStructureDelay * Gitd.REAL_SECONDS_TO_GAME_TIME) {
                plugin.pushEvent(
                    GitdPlayerStructureEvent(
                        generatedStructure.structure.structureType, player.uniqueId, timestamp
                    )
                )
                structureTimeouts[kv] = timestamp
            }
        }
    }

    private fun onPlayersTogether(first: UUID, second: UUID) {
        val arr = arrayOf(first, second).sorted()
        val sortedPair = Pair(arr[0], arr[1])
        if (playersTogether[sortedPair] == true) return

        playersTogether[sortedPair] = true
        plugin.pushEvent(GitdPlayerNearPlayerEvent(first, second, plugin.worldTime))
    }

    private fun onPlayersAway(first: UUID, second: UUID, silent: Boolean = false) {
        val arr = arrayOf(first, second).sorted()
        val sortedPair = Pair(arr[0], arr[1])
        if (playersTogether[sortedPair] == false) return

        playersTogether[sortedPair] = false
        val event = GitdPlayerAwayFromPlayerEvent(first, second, plugin.worldTime)
        if (silent) plugin.pushEventServer(event)
        else plugin.pushEvent(event)
    }

    private fun cleanUpPlayerTogether(playerId: UUID) {
        for ((key, _) in playersTogether) {
            if (key.first == playerId || key.second == playerId) onPlayersAway(key.first, key.second, true)
        }
    }
}