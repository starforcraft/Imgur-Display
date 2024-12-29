package com.ultramega.imgurdisplay.network;

import com.ultramega.imgurdisplay.DisplayUtils;
import com.ultramega.imgurdisplay.ImgurDisplay;
import com.ultramega.imgurdisplay.entities.DisplayEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DisplayUpdateMessage {
    private final UUID entityUUID;
    private final String imageIdOrUrl;
    private final boolean updateImage;
    private final boolean isStretched;
    private final boolean isEditRestricted;
    private final boolean isShowHitbox;

    public DisplayUpdateMessage(UUID entityUUID, String imageIdOrUrl, boolean updateImage, boolean isStretched, boolean isEditRestricted, boolean isShowHitbox) {
        this.entityUUID = entityUUID;
        this.imageIdOrUrl = imageIdOrUrl;
        this.updateImage = updateImage;
        this.isStretched = isStretched;
        this.isEditRestricted = isEditRestricted;
        this.isShowHitbox = isShowHitbox;
    }

    public static DisplayUpdateMessage decode(FriendlyByteBuf buf) {
        return new DisplayUpdateMessage(buf.readUUID(), buf.readUtf(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean());
    }

    public static void encode(DisplayUpdateMessage message, FriendlyByteBuf buf) {
        buf.writeUUID(message.entityUUID).writeUtf(message.imageIdOrUrl).writeBoolean(message.updateImage).writeBoolean(message.isStretched).writeBoolean(message.isEditRestricted).writeBoolean(message.isShowHitbox);
    }

    public static void handle(DisplayUpdateMessage message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            String imageId = message.imageIdOrUrl;

            if (message.updateImage && imageId != null && !imageId.isEmpty()) {
                // TODO: add compatibility to links like https://imgur.com/a/xxx or https://imgur.com/gallery/xxx
                try {
                    Matcher matcher = extractIdFormatFromIdOrUrl(imageId);
                    if (matcher != null) {
                        imageId = matcher.group(1);
                        String format = matcher.group(2) != null ? matcher.group(2) : "png";

                        BufferedImage img = DisplayUtils.fetchImageFromUrl("https://i.imgur.com/" + imageId + "." + format);
                        if (img != null) {
                            ImgurDisplay.NETWORK_HANDLER.sendToAll(new AddImageMessage(imageId, format));
                        } else {
                            imageId = "";
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Error processing image URL: " + e.getMessage());
                }
            }

            if (ctx.get().getSender().serverLevel().getEntity(message.entityUUID) instanceof DisplayEntity display) {
                display.setImageID(imageId);
                display.setStretched(message.isStretched);
                display.setEditRestricted(message.isEditRestricted);
                display.setShowHitbox(message.isShowHitbox);
                display.combineDisplay();
            }
        });

        ctx.get().setPacketHandled(true);
    }

    private static Matcher extractIdFormatFromIdOrUrl(String idOrUrl) {
        if (idOrUrl == null || idOrUrl.isEmpty()) {
            return null;
        }

        String regex = "(?:(?:https?://)?i\\.imgur\\.com/)?([a-zA-Z0-9]+)(?:\\.(\\w+))?";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(idOrUrl);

        return matcher.matches() ? matcher : null;
    }
}
