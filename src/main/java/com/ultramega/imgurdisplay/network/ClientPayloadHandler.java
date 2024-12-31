package com.ultramega.imgurdisplay.network;

import com.ultramega.imgurdisplay.utils.DisplayUtils;
import com.ultramega.imgurdisplay.ImageCache;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Optional;

public class ClientPayloadHandler {
    public static void handleAddImage(final AddImageData data, final IPayloadContext context) {
        context.enqueueWork(() -> {
            try {
                if (data.imageId() != null && !data.imageId().isEmpty()) {
                    byte[] imgBytes = DisplayUtils.fetchImageFromUrl("https://i.imgur.com/" + data.imageId() + "." + data.format());
                    if (imgBytes != null) {
                        if (!data.format().equals("gif")) {
                            BufferedImage img = DisplayUtils.decodeImage(imgBytes);
                            if (img != null) {
                                ImageCache.instance().addTexture(data.imageId(), img, null, data.format());
                            }
                        } else {
                            ImageCache.instance().addTexture(data.imageId(), null, imgBytes, data.format());
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Error adding image: " + e.getMessage());
            }
        }).exceptionally(e -> null);
    }
}
