package com.charqican.ragdollextraconf.mixin;

import com.charqican.ragdollextraconf.RagdollReactionsExtraConfigurations;
import com.llamalad7.mixinextras.sugar.Local;
import dev.leo.ragdollreactions.physics.ExplosionReactionHandler;
import dev.leo.ragdollreactions.physics.ReactionLauncher;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.windcharge.AbstractWindCharge;
import net.minecraft.world.level.Explosion;

import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ExplosionReactionHandler.class, remap = false)
public abstract class PlayerExplosionHandlerMixin {

	private static final double MIN_ARMOR_CHANCE = 0.25;
	private static final double MAX_ARMOR_CHANCE = 1.0;
	private static final double ARMOR_REFERENCE = 20.0; // full diamond/netherite vanilla

	private static boolean ragdollextraconf$isWindChargeExplosion = false;

	@Inject(method = "onVanillaExplosion", at = @At("HEAD"))
	private static void ragdollextraconf$captureWindCharge(ServerLevel level, Explosion explosion,
			CallbackInfo ci) {
		ragdollextraconf$isWindChargeExplosion = explosion
				.getDirectSourceEntity() instanceof AbstractWindCharge;

		RagdollReactionsExtraConfigurations.LOGGER
				.info("[ragdollextraconf] captureWindCharge called, isWindCharge={}",
						ragdollextraconf$isWindChargeExplosion);
	}

	@Redirect(method = "triggerExplosion(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;DDDLjava/lang/String;Lnet/minecraft/server/level/ServerPlayer;)V", at = @At(value = "INVOKE", target = "Ldev/leo/ragdollreactions/physics/ReactionLauncher;launch(Lnet/minecraft/server/level/ServerPlayer;JLorg/joml/Vector3d;)Lorg/joml/Vector3d;"))
	private static Vector3d ragdollextraconf$maybeSkipWindChargeLaunch(ServerPlayer player, long gameTime,
			Vector3d launchVelocity) {
		RagdollReactionsExtraConfigurations.LOGGER.info("[ragdollextraconf] redirect called, flag={}",
				ragdollextraconf$isWindChargeExplosion);
		if (ragdollextraconf$isWindChargeExplosion) {
			double chance = ragdollextraconf$armorChance(player);
			boolean triggered = Math.random() < chance;
			RagdollReactionsExtraConfigurations.LOGGER.info(
					"[ragdollextraconf] windcharge player={} armor={} chance={} triggered={}",
					player.getGameProfile().getName(),
					player.getAttributeValue(Attributes.ARMOR),
					chance,
					triggered);
			if (!triggered) {
				return null;
			}
		}
		return ReactionLauncher.launch(player, gameTime, launchVelocity);
	}

	private static double ragdollextraconf$armorChance(ServerPlayer player) {
		double armorPoints = player.getAttributeValue(Attributes.ARMOR);
		double raw = MAX_ARMOR_CHANCE - (armorPoints / ARMOR_REFERENCE) * (MAX_ARMOR_CHANCE - MIN_ARMOR_CHANCE);
		return Math.min(MAX_ARMOR_CHANCE, Math.max(MIN_ARMOR_CHANCE, raw));
	}
}
