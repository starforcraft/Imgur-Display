package com.ultramega.imgurdisplay.network;

import com.ultramega.imgurdisplay.DisplayUtils;
import com.ultramega.imgurdisplay.ImageCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.function.Supplier;

public class AddImageMessage {
    private final String imageId;
    private final String format;

    public AddImageMessage(String imageId, String format) {
        this.imageId = imageId;
        this.format = format;
    }

    public static AddImageMessage decode(FriendlyByteBuf buf) {
        return new AddImageMessage(buf.readUtf(), buf.readUtf());
    }

    public static void encode(AddImageMessage message, FriendlyByteBuf buf) {
        buf.writeUtf(message.imageId).writeUtf(message.format);
    }

    public static void handle(AddImageMessage message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            try {
                if (message.imageId != null && !message.imageId.isEmpty()) {
                    BufferedImage img = DisplayUtils.fetchImageFromUrl("https://i.imgur.com/" + message.imageId + "." + message.format);
                    if (img != null) {
                        ImageCache.instance().addTexture(message.imageId, img, message.format);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error adding image: " + e.getMessage());
            }
        });

        ctx.get().setPacketHandled(true);
    }
}
