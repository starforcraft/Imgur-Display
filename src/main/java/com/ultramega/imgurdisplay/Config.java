package com.ultramega.imgurdisplay;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = ImgurDisplay.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.IntValue DISPLAY_MAX_SIZE = BUILDER
            .comment("The max size of the display")
            .defineInRange("displayMaxSize", 8, 1, 64);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static int displayMaxSize;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        displayMaxSize = DISPLAY_MAX_SIZE.get();
    }
}
