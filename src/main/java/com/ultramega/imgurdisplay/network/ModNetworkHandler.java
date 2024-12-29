package com.ultramega.imgurdisplay.network;

import com.ultramega.imgurdisplay.ImgurDisplay;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetworkHandler {
    private final String protocolVersion = Integer.toString(1);
    private final SimpleChannel handler = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(ImgurDisplay.MODID, "main_channel"))
            .clientAcceptedVersions(protocolVersion::equals)
            .serverAcceptedVersions(protocolVersion::equals)
            .networkProtocolVersion(() -> protocolVersion)
            .simpleChannel();

    public void register() {
        int id = 0;
        this.handler.registerMessage(id++, DisplayUpdateMessage.class, DisplayUpdateMessage::encode, DisplayUpdateMessage::decode, DisplayUpdateMessage::handle);
        this.handler.registerMessage(id++, AddImageMessage.class, AddImageMessage::encode, AddImageMessage::decode, AddImageMessage::handle);
    }

    public void sendToServer(Object message) {
        this.handler.sendToServer(message);
    }

    public void sendToAll(Object message) {
        this.handler.send(PacketDistributor.ALL.noArg(), message);
    }
}
