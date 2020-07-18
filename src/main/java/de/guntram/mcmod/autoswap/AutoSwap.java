/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.mcmod.autoswap;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;

/**
 *
 * @author gbl
 */
public class AutoSwap implements ModInitializer {

    public static AutoSwap INSTANCE;
    
	@Override
    public void onInitialize() {
        INSTANCE = this;
	}

	public void  endBreakBlock(PlayerInventory playerInv, ItemStack heldStackAtBeginningOfBreak) {
        ItemStack currentlySelected = playerInv.getMainHandStack();
		if (currentlySelected.isEmpty() && !heldStackAtBeginningOfBreak.isEmpty()) {
            for (int index=0; index<playerInv.size(); index++) {
                if (ItemStack.areItemsEqualIgnoreDamage(heldStackAtBeginningOfBreak, playerInv.getStack(index))) {
                    playerInv.swapSlotWithHotbar(index);
                    return;
                }
            }
		}
	}

	public void  afterUseOnBlock(ServerPlayerEntity player, PlayerInventory playerInv, 
        ItemStack heldStackBeforeUse, Hand hand) {
        ItemStack heldStack = player.getStackInHand(hand);
		if (!player.isCreative() && heldStack.isEmpty()) {
            for (int index=0; index<playerInv.size(); index++) {
                if (ItemStack.areItemsEqualIgnoreDamage(heldStackBeforeUse, playerInv.getStack(index))) {
                    ItemStack newStack = playerInv.removeStack(index);
                    player.setStackInHand(hand, newStack);
                    EquipmentSlot slot;
                    if (hand == Hand.MAIN_HAND) {
                        slot = EquipmentSlot.MAINHAND;
                    } else {
                        slot = EquipmentSlot.OFFHAND;
                    }
                    ArrayList<Pair<EquipmentSlot, ItemStack>> list =  new ArrayList<>();
                    list.add(new Pair(slot, newStack));
                    player.networkHandler.sendPacket(new EntityEquipmentUpdateS2CPacket(player.getEntityId(),
                            list));
                    return;
                }
            }
        }
	}

	public void  afterAttack(PlayerInventory playerInv, ItemStack heldStackBeforeAttack) {
        ItemStack heldStack = playerInv.getMainHandStack();
		if (!heldStackBeforeAttack.isEmpty() && heldStack.isEmpty()) {
            for (int index=0; index<playerInv.size(); index++) {
                if (ItemStack.areItemsEqualIgnoreDamage(heldStackBeforeAttack, playerInv.getStack(index))) {
                    playerInv.swapSlotWithHotbar(index);
                    return;
                }
			}
		}
	}
}
