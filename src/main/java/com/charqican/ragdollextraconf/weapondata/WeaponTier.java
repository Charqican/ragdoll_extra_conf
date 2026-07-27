package com.charqican.ragdollextraconf.weapondata;

// healthMult(frac) = floorHealth + (1 - floorHealth) * (1 - exp(-frac / tau))
// armorMult(armor) = floorArmor + (1 - floorArmor) * exp(-armorDecay * armor)
public enum WeaponTier {
	LIGHT(0.32, 0.10, 0.08, 0.30, 0.10, 1.0),
	HEAVY(0.26, 0.02, 0.35, 0.30, 0.10, 1.6),
	NOTAG(0.29, 0.6, 0.15, 0.30, 0.10, 1.0);

	public final double baseChance;
	// armor increase exponential decay
	public final double armorDecay;
	public final double floorArmor;
	// damage fraction decay multiplier
	public final double tau;
	public final double floorHealth;
	public final double launchMultiplierBonus;

	WeaponTier(double baseChance, double armorDecay, double floorArmor, double tau, double floorHealth,
			double launchMultiplierBonus) {
		this.baseChance = baseChance;
		this.armorDecay = armorDecay;
		this.floorArmor = floorArmor;
		this.tau = tau;
		this.floorHealth = floorHealth;
		this.launchMultiplierBonus = launchMultiplierBonus;
	}
}
