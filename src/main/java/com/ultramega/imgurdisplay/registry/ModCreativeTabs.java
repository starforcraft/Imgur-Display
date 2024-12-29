package com.ultramega.imgurdisplay.registry;

import com.ultramega.imgurdisplay.ImgurDisplay;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {
	public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ImgurDisplay.MODID);

	public static final RegistryObject<CreativeModeTab> TAB_DISPLAY = CREATIVE_MODE_TAB.register(ImgurDisplay.MODID, () -> CreativeModeTab.builder().title(Component.translatable("itemGroup." + ImgurDisplay.MODID)).icon(() -> new ItemStack(ModItems.DISPLAY.get())).displayItems((featureFlags, output) -> {
		output.accept(ModItems.DISPLAY.get());
	}).build());
}
