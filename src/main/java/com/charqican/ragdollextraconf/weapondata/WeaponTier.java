package com.charqican.ragdollextraconf.weapondata;

public enum WeaponTier {
	LIGHT(0.35, 1.0, 1.0),
	HEAVY(0.40, 0.5, 1.6),
	NOTAG(0.29, 0.8125, 1.0);

	public final double baseChance;
	public final double armorPenaltyRate;
	public final double launchMultiplierBonus;

	WeaponTier(double baseChance, double armorPenaltyRate, double launchMultiplierBonus) {
		this.baseChance = baseChance;
		this.armorPenaltyRate = armorPenaltyRate;
		this.launchMultiplierBonus = launchMultiplierBonus;
	}
}
