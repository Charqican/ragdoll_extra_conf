package com.charqican.ragdollextraconf.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import com.charqican.ragdollextraconf.RagdollReactionsExtraConfigurations;

public final class RagdollExConfDataGenerators {
	private RagdollExConfDataGenerators() {
	}

	public static void gatherData(GatherDataEvent event) {
		DataGenerator generator = event.getGenerator();

		BlockTagsProvider blockTags = new BlockTagsProvider(
				generator.getPackOutput(), event.getLookupProvider(),
				RagdollReactionsExtraConfigurations.MODID, event.getExistingFileHelper()) {
			@Override
			protected void addTags(HolderLookup.Provider provider) {
				// vacío a propósito: este addon no genera tags de bloque,
				// solo existe para satisfacer la dependencia de ItemTagsProvider
			}
		};
		generator.addProvider(event.includeServer(), blockTags);

		var itemTags = new RagdollExConfItemTagsProvider(
				generator.getPackOutput(), event.getLookupProvider(),
				blockTags.contentsGetter(), event.getExistingFileHelper());
		generator.addProvider(event.includeServer(), itemTags);
	}
}
