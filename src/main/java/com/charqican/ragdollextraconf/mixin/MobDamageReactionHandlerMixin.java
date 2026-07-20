package com.charqican.ragdollextraconf.mixin;

import dev.leo.ragdollreactions.physics.MobDamageReactionHandler;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.At;

import com.charqican.ragdollextraconf.RagdollReactionsExtraConfigurations;
import com.charqican.ragdollextraconf.registry.RagdollExConfTags;
import com.charqican.ragdollextraconf.weapondata.WeaponTier;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

@Mixin(value = MobDamageReactionHandler.class, remap = false)
public abstract class MobDamageReactionHandlerMixin {
	private static final double ARMOR_MAX = 30.0;
	private static final double ARMOR_FLOOR = 0.25;
	private static final double HEALTH_FRACTION_REFERENCE = 0.3;
	private static final double HEALTH_FRACTION_FLOOR = 0.15;
	private static final double MIN_CHANCE = 0.01;

	// intercept boolean if on healthfraction
	@ModifyExpressionValue(method = "onMobDamaged", at = @At(value = "INVOKE", target = "Ldev/leo/ragdollreactions/physics/MobDamageReactionHandler;requiredDamageForRemainingHealth(Lnet/minecraft/world/entity/LivingEntity;F)D"))
	private static double ragdollextraconf$neutralizeHealthGate(double original) {
		return 0.0;
	}

	@ModifyConstant(method = "onMobDamaged", constant = @Constant(doubleValue = 0.3))
	private static double ragdollextraconf$modifyRagdollChance(double originalChance, LivingEntity mob,
			DamageSource source, float damage) {
		WeaponTier tier = ragdollextraconf$resolveWeaponTier(source);
		double armorMult = ragdollextraconf$armorMultiplier(mob, tier.armorPenaltyRate);
		double healthMult = ragdollextraconf$healthFractionMultiplier(mob, damage);
		double chance = ragdollextraconf$calculateChance(armorMult, healthMult, tier.baseChance);
		RagdollReactionsExtraConfigurations.LOGGER.info(
				"[ragdollextraconf] tier={} armorMult={} healthMult={} chance={}",
				tier, armorMult, healthMult, chance);
		return chance;
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

	private static double ragdollextraconf$armorMultiplier(LivingEntity mob, double armorPenalty) {
		double armorPoints = mob.getAttributeBaseValue(Attributes.ARMOR);
		double initialValue = Math.min(1.0, 1.0 - (armorPoints / ARMOR_MAX) * armorPenalty);
		return Math.max(ARMOR_FLOOR, initialValue);
	}

	private static double ragdollextraconf$healthFractionMultiplier(LivingEntity mob, float damage) {
		double maxHealth = mob.getMaxHealth();
		if (maxHealth <= 0.0) {
			return HEALTH_FRACTION_FLOOR;
		}
		double initialValue = Math.min(1.0, (damage / maxHealth) / HEALTH_FRACTION_REFERENCE);
		return Math.max(HEALTH_FRACTION_FLOOR, initialValue);
	}

	private static double ragdollextraconf$calculateChance(double armorMult, double healthMult,
			double tierBaseChance) {
		double initialValue = Math.min(1.0, tierBaseChance * armorMult * healthMult);
		return Math.max(MIN_CHANCE, initialValue);
	}

}
