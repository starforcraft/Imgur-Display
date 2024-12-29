package com.ultramega.imgurdisplay;

import com.ultramega.imgurdisplay.entities.DisplayRenderer;
import com.ultramega.imgurdisplay.network.ModNetworkHandler;
import com.ultramega.imgurdisplay.registry.ModCreativeTabs;
import com.ultramega.imgurdisplay.registry.ModEntities;
import com.ultramega.imgurdisplay.registry.ModItems;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ImgurDisplay.MODID)
public class ImgurDisplay {
    public static final String MODID = "imgurdisplay";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public static final ModNetworkHandler NETWORK_HANDLER = new ModNetworkHandler();

    public ImgurDisplay() {
        MinecraftForge.EVENT_BUS.register(this);

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEntities.ENTITY_TYPES.register(bus);
        ModCreativeTabs.CREATIVE_MODE_TAB.register(bus);
        ModItems.ITEMS.register(bus);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup));
    }

    @SubscribeEvent
    public void commonSetup(final FMLCommonSetupEvent event) {
        ImgurDisplay.NETWORK_HANDLER.register();
    }

    @SubscribeEvent
    public void clientSetup(final FMLClientSetupEvent event) {
        EntityRenderers.register(ModEntities.DISPLAY.get(), DisplayRenderer::new);
    }
}
