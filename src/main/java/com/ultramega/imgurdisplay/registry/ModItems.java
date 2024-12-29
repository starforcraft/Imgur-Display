package com.ultramega.imgurdisplay.registry;

import com.ultramega.imgurdisplay.ImgurDisplay;
import com.ultramega.imgurdisplay.items.DisplayItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ImgurDisplay.MODID);

    public static final RegistryObject<Item> DISPLAY = ITEMS.register("display", () -> new DisplayItem(new Item.Properties()));
}
