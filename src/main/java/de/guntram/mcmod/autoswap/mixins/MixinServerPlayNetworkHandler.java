package de.guntram.mcmod.autoswap.mixins;

import de.guntram.mcmod.autoswap.AutoSwap;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author shadowfacts
 */
@Mixin(ServerPlayNetworkHandler.class)
public abstract class MixinServerPlayNetworkHandler {

	@Shadow
	private ServerPlayerEntity player;

	private ItemStack heldStackBeforeAttack = ItemStack.EMPTY;

	@Inject(
			method = "onPlayerInteractEntity",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/network/ServerPlayerEntity;attack(Lnet/minecraft/entity/Entity;)V"
			)
	)
	private void beforePlayerAttack(CallbackInfo cb) {
		this.heldStackBeforeAttack = player.getInventory().getMainHandStack().copy();
	}

	@Inject(
			method = "onPlayerInteractEntity",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/network/ServerPlayerEntity;attack(Lnet/minecraft/entity/Entity;)V",
					shift = At.Shift.AFTER
			)
	)
	private void afterPlayerAttack(CallbackInfo cb) {
		AutoSwap.INSTANCE.afterAttack(player.getInventory(), heldStackBeforeAttack);
		heldStackBeforeAttack = ItemStack.EMPTY;
	}


}
