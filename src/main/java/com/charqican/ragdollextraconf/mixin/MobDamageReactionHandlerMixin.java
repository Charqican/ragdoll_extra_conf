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
	private static final double MIN_CHANCE = 0.001;
	private static final double MAX_CHANCE = 0.35;

	// intercept boolean if on healthfraction
	@ModifyExpressionValue(method = "onMobDamaged", at = @At(value = "INVOKE", target = "Ldev/leo/ragdollreactions/physics/MobDamageReactionHandler;requiredDamageForRemainingHealth(Lnet/minecraft/world/entity/LivingEntity;F)D"))
	private static double ragdollextraconf$neutralizeHealthGate(double original) {
		return 0.0;
	}

	@ModifyConstant(method = "onMobDamaged", constant = @Constant(doubleValue = 0.3))
	private static double ragdollextraconf$modifyRagdollChance(double originalChance, LivingEntity mob,
			DamageSource source, float damage) {
		WeaponTier tier = ragdollextraconf$resolveWeaponTier(source);
		double armorMult = ragdollextraconf$armorMultiplier(mob, tier);
		double healthMult = ragdollextraconf$healthFractionMultiplier(mob, damage, tier);
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

	private static double ragdollextraconf$armorMultiplier(LivingEntity mob, WeaponTier tier) {
		double armorPoints = mob.getAttributeBaseValue(Attributes.ARMOR);
		return tier.floorArmor + (1.0 - tier.floorArmor) * Math.exp(-tier.armorDecay * armorPoints);
	}

	private static double ragdollextraconf$healthFractionMultiplier(LivingEntity mob, float damage,
			WeaponTier tier) {
		double maxHealth = mob.getMaxHealth();
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

}
