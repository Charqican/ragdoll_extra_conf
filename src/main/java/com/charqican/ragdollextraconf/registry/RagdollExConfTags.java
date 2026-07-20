package com.charqican.ragdollextraconf.registry;

import com.charqican.ragdollextraconf.RagdollReactionsExtraConfigurations;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class RagdollExConfTags {
	public static final TagKey<Item> HEAVY_WEAPONS = itemTag("heavy_weapons");
	public static final TagKey<Item> LIGHT_WEAPONS = itemTag("light_weapons");
	public static final TagKey<Item> LIGHT_WEAPONS_MINECRAFT = itemTag("light_weapons/minecraft");
	public static final TagKey<Item> HEAVY_WEAPONS_MINECRAFT = itemTag("heavy_weapons/minecraft");

	private static TagKey<Item> itemTag(String name) {
		return TagKey.create(Registries.ITEM,
				ResourceLocation.fromNamespaceAndPath(RagdollReactionsExtraConfigurations.MODID, name));
	}
}
