package de.royzer.fabrichg.mixinskt

import de.royzer.fabrichg.bots.HGBot
import de.royzer.fabrichg.data.hgplayer.hgPlayer
import de.royzer.fabrichg.game.GamePhaseManager
import de.royzer.fabrichg.game.phase.PhaseType
import de.royzer.fabrichg.gulag.GulagManager
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.LivingEntity
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

object LivingEntityMixinKt {
    fun onDamage(source: DamageSource, amount: Float, entity: LivingEntity, cir: CallbackInfoReturnable<Boolean>) {
        val level = entity.level()

        if (level == GulagManager.gulagLevel) {
            if (!GulagManager.isFighting(entity)) {
                cir.returnValue = false
                return
            }
        }

        if (entity is ServerPlayer || entity is HGBot) {
            if (GamePhaseManager.currentPhaseType != PhaseType.INGAME) cir.returnValue = false
        } else if (GamePhaseManager.currentPhaseType == PhaseType.LOBBY || GamePhaseManager.currentPhaseType == PhaseType.END)
            cir.returnValue = false
    }
}