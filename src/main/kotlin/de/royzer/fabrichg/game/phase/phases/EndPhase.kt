package de.royzer.fabrichg.game.phase.phases

import de.royzer.fabrichg.TEXT_BLUE
import de.royzer.fabrichg.TEXT_GRAY
import de.royzer.fabrichg.data.hgplayer.HGPlayer
import de.royzer.fabrichg.data.hgplayer.hgPlayer
import de.royzer.fabrichg.game.GamePhaseManager
import de.royzer.fabrichg.game.broadcastComponent
import de.royzer.fabrichg.game.combatlog.combatloggedPlayers
import de.royzer.fabrichg.game.phase.GamePhase
import de.royzer.fabrichg.game.phase.PhaseType
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.silkmc.silk.core.logging.logInfo
import net.silkmc.silk.core.text.literalText

class EndPhase(private val winner: HGPlayer?) : GamePhase() {

    val endTime by lazy { GamePhaseManager.timer.get() }

    override fun init() {
        GamePhaseManager.server.motd = "${GamePhaseManager.MOTD_STRING}\nCURRENT GAME PHASE: \u00A74END"
        combatloggedPlayers.forEach { (_, u) -> u.cancel() }
        endTime
        GamePhaseManager.resetTimer()
        with(winner?.serverPlayer ?: return) {
            connection.send(ClientboundPlayerAbilitiesPacket(abilities.also {
                it.flying = true
                it.mayfly = true
            }))
            addEffect(MobEffectInstance(MobEffects.GLOWING, -1, 0, false, false))
        }
        winner.updateStats(wins = 1)
    }

    override fun tick(timer: Int) {
        broadcastComponent(winnerText(winner))
//        if (timer == maxPhaseTime - 1) {
//            GamePhaseManager.server.playerList.players.forEach {
//                it.connection.disconnect(literalText("Der Server startet neu") { color = 0xFF0000 })
//            }
//        }
        if (timer >= maxPhaseTime) {
            logInfo("Spiel endet")
            logInfo("Sieger: ${winner?.name}, Kills: ${winner?.kills}")
            GamePhaseManager.server.halt(false)
            return
        }
    }

    override val phaseType = PhaseType.END
    override val maxPhaseTime = 20
    override val nextPhase: GamePhase? = null
}

fun winnerText(winner: HGPlayer?): Component {
    if (winner == null) return literalText("Kein Sieger?")
    return literalText {
        color = TEXT_GRAY
        text(winner.name) {
            color = TEXT_BLUE
            underline = true
        }
        text(" hat gewonnen!")
        hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, literalText {
            text("Kills: ${winner.kills}\n") {
                color = 0x00FF51
            }
            text("Kit(s): ") {
                color = 0x42FF51
                text(winner.kits.joinToString { it.name })
            }
        })
    }
}