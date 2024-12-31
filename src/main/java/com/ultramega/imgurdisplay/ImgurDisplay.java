package com.ultramega.imgurdisplay;

import com.ultramega.imgurdisplay.entities.DisplayRenderer;
import com.ultramega.imgurdisplay.registry.ModCreativeTabs;
import com.ultramega.imgurdisplay.registry.ModEntityTypes;
import com.ultramega.imgurdisplay.registry.ModItems;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ImgurDisplay.MODID)
public class ImgurDisplay {
    public static final String MODID = "imgurdisplay";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public ImgurDisplay(IEventBus modEventBus, ModContainer modContainer) {
        ModEntityTypes.ENTITY_TYPES.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TAB.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC, MODID + "/" + MODID + "-server.toml");
    }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(final FMLClientSetupEvent event) {
            EntityRenderers.register(ModEntityTypes.DISPLAY.get(), DisplayRenderer::new);;
        }
    }
}
