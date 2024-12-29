package com.ultramega.imgurdisplay.network;

import com.ultramega.imgurdisplay.ImgurDisplay;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record DisplayUpdateData(UUID entityUUID, String imageIdOrUrl, boolean updateImage, boolean isStretched, boolean isEditRestricted, boolean isShowHitbox) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<DisplayUpdateData> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ImgurDisplay.MODID, "display_update"));
    public static final StreamCodec<ByteBuf, DisplayUpdateData> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, DisplayUpdateData::entityUUID,
            ByteBufCodecs.STRING_UTF8, DisplayUpdateData::imageIdOrUrl,
            ByteBufCodecs.BOOL, DisplayUpdateData::updateImage,
            ByteBufCodecs.BOOL, DisplayUpdateData::isStretched,
            ByteBufCodecs.BOOL, DisplayUpdateData::isEditRestricted,
            ByteBufCodecs.BOOL, DisplayUpdateData::isShowHitbox,
            DisplayUpdateData::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
