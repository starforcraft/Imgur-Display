package com.ultramega.imgurdisplay.network;

import com.ultramega.imgurdisplay.DisplayUtils;
import com.ultramega.imgurdisplay.entities.DisplayEntity;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.regex.Matcher;

public class ServerPayloadHandler {
    public static void handleDisplayUpdate(final DisplayUpdateData data, final IPayloadContext context) {
        context.enqueueWork(() -> {
            String imageId = data.imageIdOrUrl();

            if (data.updateImage() && imageId != null && !imageId.isEmpty()) {
                // TODO: add compatibility to links like https://imgur.com/a/xxx or https://imgur.com/gallery/xxx
                try {
                    Matcher matcher = DisplayUtils.extractIdFormatFromIdOrUrl(imageId);
                    if (matcher != null) {
                        imageId = matcher.group(1);
                        String format = matcher.group(2) != null ? matcher.group(2) : "png";

                        BufferedImage img = DisplayUtils.fetchImageFromUrl("https://i.imgur.com/" + imageId + "." + format);
                        if (img != null) {
                            PacketDistributor.sendToAllPlayers(new AddImageData(imageId, format));
                        } else {
                            imageId = "";
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Error processing image URL: " + e.getMessage());
                }
            }

            if (context.player().level() instanceof ServerLevel serverLevel) {
                if (serverLevel.getEntity(data.entityUUID()) instanceof DisplayEntity display) {
                    display.setImageID(imageId);
                    display.setStretched(data.isStretched());
                    display.setEditRestricted(data.isEditRestricted());
                    display.setShowHitbox(data.isShowHitbox());
                    display.combineDisplay();
                }
            }
        }).exceptionally(e -> null);
    }
}
