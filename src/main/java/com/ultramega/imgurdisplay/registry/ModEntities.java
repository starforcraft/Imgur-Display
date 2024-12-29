package com.ultramega.imgurdisplay.registry;

import com.ultramega.imgurdisplay.ImgurDisplay;
import com.ultramega.imgurdisplay.entities.DisplayEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ImgurDisplay.MODID);

    public static final RegistryObject<EntityType<DisplayEntity>> DISPLAY = ENTITY_TYPES.register("display",
        () -> EntityType.Builder.of(DisplayEntity::new, MobCategory.MISC)
            .sized(1F, 1F)
            .clientTrackingRange(10)
            .setShouldReceiveVelocityUpdates(false)
            .updateInterval(Integer.MAX_VALUE)
            .build(new ResourceLocation(ImgurDisplay.MODID, "display").toString()));
}
