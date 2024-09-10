package ru.teasanctuary.cia_n

import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerPortalEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.generator.structure.StructureType
import ru.teasanctuary.cia_n.events.player.*
import ru.teasanctuary.cia_n.events.player.health.*
import ru.teasanctuary.cia_n.events.player.social.*
import java.util.UUID

class EventLogger(private val plugin: CiaN) : Listener {
    private val pvpTimeouts = mutableMapOf<Pair<UUID, UUID>, Long>()
    private val damageTimeouts = mutableMapOf<UUID, Long>()
    private val structureTimeouts = mutableMapOf<Pair<UUID, StructureType>, Long>()
    private val playersTogether = mutableMapOf<Pair<UUID, UUID>, Boolean>()

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        plugin.pushEventServer(CiaPlayerJoinEvent(event.player.uniqueId, plugin.worldTime))
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        cleanUpPlayerTogether(event.player.uniqueId)
        plugin.pushEventServer(CiaPlayerLeaveEvent(event.player.uniqueId, plugin.worldTime))
    }

    @EventHandler
    fun onPlayerDamage(event: EntityDamageEvent) {
        val victim = event.entity as? Player
        if (victim == null || !victim.isValid) return

        val timestamp = plugin.worldTime
        val attacker = event.damageSource.causingEntity as? Player
        if (attacker != null && attacker.isValid) {
            val lastTime = pvpTimeouts[attacker.uniqueId to victim.uniqueId]
            if (lastTime == null || timestamp - lastTime > plugin.ciaNConfig.playerDamageEventDelay * CiaN.REAL_SECONDS_TO_GAME_TIME) {
                plugin.pushEvent(CiaPlayerVersusPlayerEvent(attacker.uniqueId, victim.uniqueId, timestamp))
                pvpTimeouts[attacker.uniqueId to victim.uniqueId] = timestamp
            }
        } else {
            val lastTime = damageTimeouts[victim.uniqueId]
            if (lastTime == null || timestamp - lastTime > plugin.ciaNConfig.playerDamageEventDelay * CiaN.REAL_SECONDS_TO_GAME_TIME) {
                plugin.pushEvent(CiaPlayerHurtEvent(victim.uniqueId, timestamp))
                damageTimeouts[victim.uniqueId] = timestamp
            }
        }

        val maxHealth = victim.getAttribute(Attribute.GENERIC_MAX_HEALTH)
        if (maxHealth != null && victim.health / maxHealth.value <= 0.10) {
            plugin.pushEvent(CiaPlayerLowHealthEvent(victim.uniqueId, timestamp))
        }
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        cleanUpPlayerTogether(event.player.uniqueId)
        plugin.pushEventServer(CiaPlayerDeathEvent(event.player.uniqueId, plugin.worldTime))
    }

    @EventHandler
    fun onPlayerEnterPortal(event: PlayerPortalEvent) {
        // Произошло перемещение между мирами
        plugin.pushEvent(CiaPlayerMoveWorldsEvent(event.to.world, event.player.uniqueId, plugin.worldTime))
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        // Нам не интересны наблюдатели и админы
        if (player.gameMode != GameMode.SURVIVAL) return

        // Проверка расстояний между игроками
        val onlinePlayers = Bukkit.getOnlinePlayers()
        for (onlinePlayer in onlinePlayers) {
            if (onlinePlayer == player) continue

            val arr = arrayOf(player.uniqueId, onlinePlayer.uniqueId).sorted()
            val sortedPair = Pair(arr[0], arr[1])
            val isNearby = playersTogether[sortedPair] ?: false
            val distance = player.location.distance(onlinePlayer.location)
            if (isNearby) {
                if (onlinePlayer.isDead || distance >= plugin.ciaNConfig.playerVisitPlayerDistance + plugin.ciaNConfig.playerLeavePlayerDistance) {
                    onPlayersAway(sortedPair)
                }
            } else {
                if (distance <= plugin.ciaNConfig.playerVisitPlayerDistance) {
                    onPlayersTogether(sortedPair)
                }
            }
        }

        val chunkStructures = event.to.chunk.structures
        val timestamp = plugin.worldTime
        for (generatedStructure in chunkStructures) {
            if (!generatedStructure.boundingBox.contains(player.location.toVector())) continue
            val kv = player.uniqueId to generatedStructure.structure.structureType
            val lastTime = structureTimeouts[kv]
            if (lastTime == null || timestamp - lastTime > plugin.ciaNConfig.playerEnterStructureDelay * CiaN.REAL_SECONDS_TO_GAME_TIME) {
                plugin.pushEvent(
                    CiaPlayerStructureEvent(
                        generatedStructure.structure.structureType, player.uniqueId, timestamp
                    )
                )
                structureTimeouts[kv] = timestamp
            }
        }
    }

    private fun onPlayersTogether(pair: Pair<UUID, UUID>) {
        playersTogether[pair] = true
        plugin.pushEvent(CiaPlayerNearPlayerEvent(pair.first, pair.second, plugin.worldTime))
    }

    private fun onPlayersAway(pair: Pair<UUID, UUID>, silent: Boolean = false) {
        playersTogether[pair] = false
        val event = CiaPlayerAwayFromPlayerEvent(pair.first, pair.second, plugin.worldTime)
        if (silent) plugin.pushEventServer(event)
        else plugin.pushEvent(event)
    }

    private fun cleanUpPlayerTogether(playerId: UUID) {
        for ((key, _) in playersTogether) {
            if (key.first == playerId || key.second == playerId) onPlayersAway(key, true)
        }
    }
}