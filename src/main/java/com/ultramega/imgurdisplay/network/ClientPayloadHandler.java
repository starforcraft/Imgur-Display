package com.ultramega.imgurdisplay.network;

import com.ultramega.imgurdisplay.DisplayUtils;
import com.ultramega.imgurdisplay.ImageCache;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class ClientPayloadHandler {
    public static void handleAddImage(final AddImageData data, final IPayloadContext context) {
        context.enqueueWork(() -> {
            try {
                if (data.imageId() != null && !data.imageId().isEmpty()) {
                    BufferedImage img = DisplayUtils.fetchImageFromUrl("https://i.imgur.com/" + data.imageId() + "." + data.format());
                    if (img != null) {
                        ImageCache.instance().addTexture(data.imageId(), img, data.format());
                    }
                }
            } catch (IOException e) {
                System.err.println("Error adding image: " + e.getMessage());
            }
        }).exceptionally(e -> null);
    }
}
