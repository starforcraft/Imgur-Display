package com.ultramega.imgurdisplay.registry;

import com.ultramega.imgurdisplay.ImgurDisplay;
import com.ultramega.imgurdisplay.items.DisplayItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ImgurDisplay.MODID);

    public static final DeferredItem<Item> DISPLAY = ITEMS.register("display", () -> new DisplayItem(new Item.Properties()));
}
