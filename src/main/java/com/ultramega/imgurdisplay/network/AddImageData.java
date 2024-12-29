package com.ultramega.imgurdisplay.network;

import com.ultramega.imgurdisplay.ImgurDisplay;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record AddImageData(String imageId, String format) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<AddImageData> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ImgurDisplay.MODID, "add_image"));
    public static final StreamCodec<ByteBuf, AddImageData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, AddImageData::imageId,
            ByteBufCodecs.STRING_UTF8, AddImageData::format,
            AddImageData::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
