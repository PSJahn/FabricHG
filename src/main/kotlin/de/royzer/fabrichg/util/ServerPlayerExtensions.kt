package de.royzer.fabrichg.util

import de.royzer.fabrichg.kit.events.kititem.isKitItem
import de.royzer.fabrichg.mixins.entity.EntityAcessor
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.silkmc.silk.core.kotlin.ticks
import net.silkmc.silk.core.task.mcCoroutineTask

fun ServerPlayer.giveOrDropItem(item: ItemStack) {
    if (!inventory.add(item)) {
        val itemEntity: ItemEntity? = drop(item, false)
        itemEntity?.setNoPickUpDelay()
        itemEntity?.setTarget(this.uuid)
    }
}

fun ServerPlayer.giveOrDropItems(items: List<ItemStack>) {
    items.forEach {
        this.giveOrDropItem(it)
    }
}

fun ServerPlayer.forceGiveItem(item: ItemStack) {
    if (inventory.add(item)) {
        return
    }
    mcCoroutineTask(delay = 5.ticks) {
        this@forceGiveItem.forceGiveItem(item)
    }
}

val ServerPlayer.recraft: Int
    get() {
        var rc = 0.0
        inventory.forEach {
            when (it.item) {
                Items.COCOA_BEANS -> rc += it.count * 1
                Items.RED_MUSHROOM -> rc += it.count * 0.5
                Items.BROWN_MUSHROOM -> rc += it.count * 0.5
                Items.CACTUS -> rc += it.count * 0.5
                Items.PINK_PETALS -> rc += it.count * 0.125
            }
        }
        return rc.toInt()
    }

fun ServerPlayer.armorValue(): Double {
    var value = 0.0
    inventory.armor.forEach { armor ->
        val name = armor.hoverName.string.lowercase()
        when {
            name.contains("leather") -> value += 3.0
            name.contains("gold") -> value += 6.0
            name.contains("chain") -> value += 6.0
            name.contains("iron") -> value += 9.0
            name.contains("diamond") -> value += 15.0
            name.contains("netherite") -> value += 17.0
        }
    }
    return value
}

fun ServerPlayer.inventoryValue(): Double {
    val soupValue = 0.3

    var value = armorValue() + (recraft * soupValue)

    val goldValue = 1.5
    val ironValue = 2.5
    val diamondValue = 5.0

    inventory.forEach { item ->
        value += when (item.item) {
            Items.GOLD_NUGGET -> goldValue / 9
            Items.GOLD_INGOT -> goldValue
            Items.GOLD_BLOCK -> goldValue * 9
            Items.GOLDEN_SWORD -> goldValue * 3
            Items.GOLDEN_PICKAXE -> goldValue * 3
            Items.GOLDEN_AXE -> goldValue * 3

            Items.IRON_NUGGET -> ironValue / 9
            Items.IRON_INGOT -> ironValue
            Items.IRON_BLOCK -> ironValue * 9
            Items.IRON_SWORD -> ironValue * 3
            Items.IRON_PICKAXE -> ironValue * 3
            Items.IRON_AXE -> ironValue * 3

            Items.DIAMOND -> diamondValue
            Items.DIAMOND_BLOCK -> diamondValue * 9
            Items.DIAMOND_SWORD -> diamondValue * 3
            Items.DIAMOND_PICKAXE -> diamondValue * 3
            Items.DIAMOND_AXE -> diamondValue * 3

            Items.MUSHROOM_STEW -> soupValue

            else -> 0.0
        } * item.count
    }

    return value
}

fun ServerPlayer.dropInventoryItemsWithoutKitItems() {
    inventory.forEach { item ->
        if (item.isKitItem) return@forEach
        if (item.item == Items.ANVIL) return@forEach

        spawnAtLocation(item)
    }


    inventory.clearContent()
}

fun ServerPlayer.sendEntityDataUpdate(forEntity: LivingEntity, changeFlag: Int? = null, changeValue: Boolean? = null) {
    val entityData = forEntity.entityData ?: return
    val forEntityAccessor = forEntity as EntityAcessor
    val beforeFlag = changeFlag?.let { forEntity.getBrainBusting(it) }

    if (changeFlag != null && changeValue != null) {
        forEntityAccessor.setBrainBusting(changeFlag, changeValue)
    }

    val packedDirty = entityData.packDirty()

    if (packedDirty != null) connection.send(ClientboundSetEntityDataPacket(forEntity.id, packedDirty))

    if (changeFlag != null && changeValue != null && beforeFlag != null) forEntityAccessor.setBrainBusting(changeFlag, beforeFlag)

    entityData.packDirty()
}