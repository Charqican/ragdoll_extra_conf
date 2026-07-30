package com.charqican.ragdollextraconf.mixin;

import com.charqican.ragdollextraconf.RagdollReactionsExtraConfigurations;
import com.charqican.ragdollextraconf.registry.RagdollExConfTags;
import com.charqican.ragdollextraconf.weapondata.WeaponTier;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import dev.leo.ragdollreactions.physics.HitReactionHandler;
import dev.leo.ragdollreactions.physics.ReactionLauncher;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = HitReactionHandler.class, remap = false)
public abstract class PlayerDamageReactionHandlerMixin {

	private static final double MIN_CHANCE = 0.001;
	private static final double MAX_CHANCE = 0.35;
	private static final float MIN_DAMAGE_FOR_LAUNCH = 4.0F; // 2 corazones, piso duro independiente de config
	private static final float MIN_DAMAGE_FOR_LAUNCH_PROJECTILE = 4.0F;

	@Inject(method = "onPlayerDamaged", at = @At("HEAD"), cancellable = true)
	private static void ragdollextraconf$enforceMinDamage(ServerPlayer player, DamageSource source, float damage,
			CallbackInfo ci) {
		Entity sourceEntity = source.getDirectEntity();
		boolean isProjectile = sourceEntity instanceof Projectile;
		float threshold = isProjectile ? MIN_DAMAGE_FOR_LAUNCH_PROJECTILE : MIN_DAMAGE_FOR_LAUNCH;

		if (damage < threshold) {
			ci.cancel();
		}
	}

	// Neutralize the if min damage for players, this way we don't have to deal with
	// configs
	@ModifyExpressionValue(method = "onPlayerDamaged", at = @At(value = "INVOKE", target = "Ldev/leo/ragdollreactions/config/ReactionSettings$Hit;minDamage()D"))
	private static double ragdollextraconf$neutralizeMinDamage(double original) {
		return 0.0;
	}

	@Redirect(method = "onPlayerDamaged", at = @At(value = "INVOKE", target = "Ldev/leo/ragdollreactions/physics/ReactionLauncher;launch(Lnet/minecraft/server/level/ServerPlayer;JLorg/joml/Vector3d;)Lorg/joml/Vector3d;"))
	private static Vector3d ragdollextraconf$rollChance(ServerPlayer launchPlayer, long gameTime,
			Vector3d launchVelocity,
			ServerPlayer damagedPlayer, DamageSource source, float damage) {

		RagdollReactionsExtraConfigurations.LOGGER.info(
				"[ragdollextraconf] rollChance called, damage={}", damage);
		WeaponTier tier = ragdollextraconf$resolveWeaponTier(source);
		double armorMult = ragdollextraconf$armorMultiplier(damagedPlayer, tier);
		double healthMult = ragdollextraconf$healthFractionMultiplier(damagedPlayer, damage, tier);
		double chance = ragdollextraconf$calculateChance(armorMult, healthMult, tier.baseChance);

		boolean triggered = Math.random() < chance;
		RagdollReactionsExtraConfigurations.LOGGER.info(
				"[ragdollextraconf] hit player={} tier={} armorMult={} healthMult={} chance={} triggered={}",
				damagedPlayer.getGameProfile().getName(), tier, armorMult, healthMult, chance,
				triggered);

		if (!triggered) {
			return null;
		}
		return ReactionLauncher.launch(launchPlayer, gameTime, launchVelocity);
	}

	private static WeaponTier ragdollextraconf$resolveWeaponTier(DamageSource source) {
		ItemStack weapon = source.getWeaponItem();
		if (weapon == null || weapon.isEmpty()) {
			return WeaponTier.NOTAG;
		}
		if (weapon.is(RagdollExConfTags.HEAVY_WEAPONS)) {
			return WeaponTier.HEAVY;
		}
		if (weapon.is(RagdollExConfTags.LIGHT_WEAPONS)) {
			return WeaponTier.LIGHT;
		}
		return WeaponTier.NOTAG;
	}

	private static double ragdollextraconf$armorMultiplier(ServerPlayer player, WeaponTier tier) {
		double armorPoints = player.getAttributeValue(Attributes.ARMOR);
		return tier.floorArmor + (1.0 - tier.floorArmor) * Math.exp(-tier.armorDecay * armorPoints);
	}

	private static double ragdollextraconf$healthFractionMultiplier(ServerPlayer player, float damage,
			WeaponTier tier) {
		double maxHealth = player.getMaxHealth();
		if (maxHealth <= 0.0) {
			return tier.floorHealth;
		}
		double frac = damage / maxHealth;
		return tier.floorHealth + (1.0 - tier.floorHealth) * (1.0 - Math.exp(-frac / tier.tau));
	}

	private static double ragdollextraconf$calculateChance(double armorMult, double healthMult, double base) {
		double initialValue = base * armorMult * healthMult;
		return Math.min(MAX_CHANCE, Math.max(MIN_CHANCE, initialValue));
	}

	// private static Vector3d ragdollextraconf$rollProjectileChance(ServerPlayer
	// launchPlayer, long gameTime,
	// ServerPlayer damagedPlayer, DamageSource source, float damage, Projectile
	// projectile) {
	//
	// }
}
