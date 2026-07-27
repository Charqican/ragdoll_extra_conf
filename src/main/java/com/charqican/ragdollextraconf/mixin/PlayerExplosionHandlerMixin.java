package com.charqican.ragdollextraconf.mixin;

import dev.leo.ragdollreactions.physics.ExplosionReactionHandler;
import dev.leo.ragdollreactions.physics.ReactionLauncher;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;

import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import com.charqican.ragdollextraconf.RagdollReactionsExtraConfigurations;
import com.charqican.ragdollextraconf.registry.RagdollExConfTags;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

@Mixin(value = ExplosionReactionHandler.class, remap = false)
public abstract class PlayerExplosionHandlerMixin {

	private static final double MIN_ARMOR_CHANCE = 0.25;
	private static final double MAX_ARMOR_CHANCE = 1.0;
	private static final double ARMOR_REFERENCE = 20.0;

	// local variable to inject code
	private static boolean ragdollextraconf$isWindChargeExplosion = false;
	// WARNING: highly suceptible to api changes
	/*
	 * PLAYER EXPLOSION HANDLER
	 *
	 */

	// reset flag
	@Inject(method = "onVanillaExplosion", at = @At("HEAD"))
	private static void ragdollextraconf$resetWindChargeFlag(ServerLevel level, Explosion explosion,
			CallbackInfo ci) {
		ragdollextraconf$isWindChargeExplosion = false;
	}

	// capture isWindCharge variable
	@Inject(method = "onVanillaExplosion", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Explosion;getDirectSourceEntity()Lnet/minecraft/world/entity/Entity;"))
	private static void ragdollextraconf$captureWindCharge(ServerLevel level, Explosion explosion, CallbackInfo ci,
			@Local(ordinal = 0) boolean isWindCharge) {
		ragdollextraconf$isWindChargeExplosion = isWindCharge;
	}

	// intercept ReactionLauncher call & add chance to ragdoll activation
	@Redirect(method = "triggerExplosion", at = @At(value = "INVOKE", target = "Lpath/to/ReactionLauncher;launch(Lnet/minecraft/server/level/ServerPlayer;JLcom/example/Vector3d;)Lcom/example/Vector3d;"))
	private static Vector3d ragdollextraconf$maybeSkipWindChargeLaunch(ServerPlayer player, long gameTime,
			Vector3d launchVelocity) {
		if (ragdollextraconf$isWindChargeExplosion) {
			double chance = ragdollextraconf$armorChance(player);
			if (Math.random() >= chance) {
				return null;
			}
		}
		return ReactionLauncher.launch(player, gameTime, launchVelocity);
	}

	// private function to calculate random chance, this use lineal decay with armor
	// points
	private static double ragdollextraconf$armorChance(ServerPlayer player) {
		double armorPoints = player.getAttributeValue(Attributes.ARMOR);
		double raw = MAX_ARMOR_CHANCE - (armorPoints / ARMOR_REFERENCE) * (MAX_ARMOR_CHANCE - MIN_ARMOR_CHANCE);
		return Math.min(MAX_ARMOR_CHANCE, Math.max(MIN_ARMOR_CHANCE, raw));
	}
}
