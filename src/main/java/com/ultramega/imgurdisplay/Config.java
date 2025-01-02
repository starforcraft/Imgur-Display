package com.ultramega.imgurdisplay;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue DISPLAY_MAX_SIZE = BUILDER
            .comment("The max size of the display")
            .defineInRange("displayMaxSize", 8, 1, 64);

    static final ModConfigSpec SPEC = BUILDER.build();
}
