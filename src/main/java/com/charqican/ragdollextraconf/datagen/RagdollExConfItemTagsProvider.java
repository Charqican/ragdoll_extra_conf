package com.charqican.ragdollextraconf.datagen;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import com.charqican.ragdollextraconf.RagdollReactionsExtraConfigurations;

import com.charqican.ragdollextraconf.registry.RagdollExConfTags;

public class RagdollExConfItemTagsProvider extends ItemTagsProvider {

	public RagdollExConfItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
			CompletableFuture<TagLookup<Block>> blockTags, ExistingFileHelper existingFileHelper) {
		super(output, lookupProvider, blockTags, RagdollReactionsExtraConfigurations.MODID, existingFileHelper);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		tag(RagdollExConfTags.HEAVY_WEAPONS_MINECRAFT).add(
				Items.WOODEN_AXE, Items.STONE_AXE, Items.STONE_AXE, Items.IRON_AXE, Items.GOLDEN_AXE,
				Items.DIAMOND_AXE, Items.NETHERITE_AXE, Items.MACE);
		tag(RagdollExConfTags.LIGHT_WEAPONS_MINECRAFT).add(
				Items.WOODEN_AXE, Items.STONE_SWORD, Items.IRON_SWORD, Items.GOLDEN_SWORD,
				Items.DIAMOND_SWORD, Items.NETHERITE_SWORD, Items.TRIDENT);
	}
}
