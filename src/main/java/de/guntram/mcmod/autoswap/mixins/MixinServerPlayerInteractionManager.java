package de.guntram.mcmod.autoswap.mixins;

import de.guntram.mcmod.autoswap.AutoSwap;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


/**
 * @author shadowfacts
 */
@Mixin(ServerPlayerInteractionManager.class)
public abstract class MixinServerPlayerInteractionManager {

	@Shadow
	private ServerPlayerEntity player;

	@Shadow
	abstract boolean isCreative();

	private ItemStack heldStackAtBeginningOfBreak = ItemStack.EMPTY;
	private ItemStack heldStackAtBeginningOfUse = ItemStack.EMPTY;

	@Inject(method = "tryBreakBlock", at = @At("HEAD"))
	private void beginTryBreakBlock(BlockPos pos, CallbackInfoReturnable<Void> cb) {
		heldStackAtBeginningOfBreak = player.getInventory().getMainHandStack().copy();
	}

	@Inject(method = "tryBreakBlock", at = @At("RETURN"))
	private void endTryBreakBlock(BlockPos pos, CallbackInfoReturnable<Void> cb) {
		AutoSwap.INSTANCE.endBreakBlock(player.getInventory(), heldStackAtBeginningOfBreak);
		heldStackAtBeginningOfBreak = ItemStack.EMPTY;
	}

	@Inject(
			method = "interactBlock",
			at = @At(value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;")
	)
	private void beforeUseOnBlock(ServerPlayerEntity player, World world, ItemStack heldStack, Hand hand, BlockHitResult result, CallbackInfoReturnable<ActionResult> cb) {
		heldStackAtBeginningOfUse = heldStack.copy();
	}

	@Inject(
			method = "interactBlock",
			at = @At(
					value = "INVOKE_ASSIGN",
					target = "Lnet/minecraft/item/ItemStack;useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;")
	)
	private void afterUseOnBlock(ServerPlayerEntity player, World world, ItemStack heldStack, Hand hand, BlockHitResult result, CallbackInfoReturnable<ActionResult> cb) {
		if (!isCreative() /* && cb.getReturnValue() == ActionResult.SUCCESS */) {
			AutoSwap.INSTANCE.afterUseOnBlock(this.player, player.getInventory(), heldStackAtBeginningOfUse, hand);
		}
		heldStackAtBeginningOfUse = ItemStack.EMPTY;
	}

}
