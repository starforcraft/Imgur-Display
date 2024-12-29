package com.ultramega.imgurdisplay.registry;

import com.ultramega.imgurdisplay.ImgurDisplay;
import com.ultramega.imgurdisplay.entities.DisplayEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, ImgurDisplay.MODID);

    public static final Supplier<EntityType<DisplayEntity>> DISPLAY = ENTITY_TYPES.register("display",
        () -> EntityType.Builder.of(DisplayEntity::new, MobCategory.MISC)
            .sized(1F, 1F)
            .eyeHeight(0.0F)
            .clientTrackingRange(10)
            .setShouldReceiveVelocityUpdates(false)
            .updateInterval(Integer.MAX_VALUE)
            .build(ResourceLocation.fromNamespaceAndPath(ImgurDisplay.MODID, "display").toString()));
}
