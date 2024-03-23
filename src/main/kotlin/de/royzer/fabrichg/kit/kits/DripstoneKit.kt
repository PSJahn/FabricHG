package de.royzer.fabrichg.kit.kits

import de.royzer.fabrichg.kit.cooldown.activateCooldown
import de.royzer.fabrichg.kit.kit
import de.royzer.fabrichg.util.higherBy
import kotlinx.coroutines.cancel
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.PointedDripstoneBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.DripstoneThickness
import net.silkmc.silk.core.entity.world
import net.silkmc.silk.core.task.mcCoroutineTask
import kotlin.time.Duration.Companion.milliseconds

fun createPointedDripstone(direction: Direction, dripstoneThickness: DripstoneThickness): BlockState {
    val dripstone = Blocks.POINTED_DRIPSTONE.defaultBlockState()
        .setValue(PointedDripstoneBlock.TIP_DIRECTION, direction)
        .setValue(PointedDripstoneBlock.THICKNESS, dripstoneThickness)
    return dripstone
}

fun createDripstonePosMap(original: BlockPos, overPlayer: Int): Map<BlockPos, BlockState> {
    return mapOf(
        original.higherBy(overPlayer) to createPointedDripstone(Direction.DOWN, DripstoneThickness.TIP_MERGE),
        original.higherBy(overPlayer - 1) to createPointedDripstone(Direction.DOWN, DripstoneThickness.TIP)
    )
}

val dripstoneKit = kit("Dripstone") {
    kitSelectorItem = Items.POINTED_DRIPSTONE.defaultInstance
    cooldown = 25.0

    kitItem {
        itemStack = kitSelectorItem.copy()

        onClickAtEntity { hgPlayer, kit, entity, interactionHand ->
            val world = entity.world

            hgPlayer.activateCooldown(kit)

            mcCoroutineTask(howOften = 35L, period = 175.milliseconds) {
                if (!entity.isAlive) {
                    this.coroutineContext.cancel()
                    return@mcCoroutineTask
                }
                val dripstoneHeight = 15
                val overPos = entity.onPos.higherBy(dripstoneHeight + 1)
                val blockMap = createDripstonePosMap(entity.onPos, dripstoneHeight)
                world.setBlockAndUpdate(overPos, Blocks.DRIPSTONE_BLOCK.defaultBlockState())
                blockMap.forEach { (pos, dripstone) ->
                    world.setBlockAndUpdate(pos, dripstone)
                }
                world.setBlockAndUpdate(overPos, Blocks.AIR.defaultBlockState())
            }
        }
    }
}