package com.ultramega.imgurdisplay.network;

import com.ultramega.imgurdisplay.ImgurDisplay;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = ImgurDisplay.MODID, bus = EventBusSubscriber.Bus.MOD)
public class PayloadEvent {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(ImgurDisplay.MODID).versioned("1.0");
        registrar.playToClient(
                AddImageData.TYPE,
                AddImageData.STREAM_CODEC,
                ClientPayloadHandler::handleAddImage
        );

        registrar.playToServer(
                DisplayUpdateData.TYPE,
                DisplayUpdateData.STREAM_CODEC,
                ServerPayloadHandler::handleDisplayUpdate
        );
    }
}
