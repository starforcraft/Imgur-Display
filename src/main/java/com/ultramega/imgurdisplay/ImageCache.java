package com.ultramega.imgurdisplay;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ImageCache {
    private final List<DisplayImage> imageCache = Collections.synchronizedList(new ArrayList<>());

    public void addTexture(String imageId, BufferedImage image, String format) {
        boolean alreadyPresent = imageCache.stream()
            .map(DisplayImage::imageId)
            .anyMatch(id -> id.contains(imageId));
        if (alreadyPresent) {
            return;
        }

        DisplayUtils.saveImageToDisk(Minecraft.getInstance(), imageId, image, format);
        loadToMinecraft(imageId, image);
    }

    public ResourceLocation getImage(String imageId) {
        ResourceLocation location = getImageLocation(imageId);

        if (location == null) {
            BufferedImage image = DisplayUtils.loadImageFromDisk(Minecraft.getInstance(), imageId);
            if (image != null) {
                return loadToMinecraft(imageId, image);
            }
        }

        return location;
    }

    public ResourceLocation getImageLocation(String imageId) {
        return imageCache.stream()
                .filter(image -> image.imageId.equals(imageId))
                .findFirst()
                .map(DisplayImage::location)
                .orElse(null);
    }

    private ResourceLocation loadToMinecraft(String imageId, BufferedImage image) {
        ResourceLocation location = new ResourceLocation(ImgurDisplay.MODID, "textures/image/" + DisplayUtils.encodeToHex(imageId));
        DynamicTexture texture = new DynamicTexture(DisplayUtils.toNativeImage(image));
        imageCache.add(new DisplayImage(texture, location, imageId));
        Minecraft.getInstance().getEntityRenderDispatcher().textureManager.register(location, texture);

        return location;
    }

    public NativeImage getNativeImage(String imageId) {
        return imageCache.stream()
                .filter(image -> image.imageId.equals(imageId))
                .findFirst()
                .map(DisplayImage::texture)
                .map(DynamicTexture::getPixels)
                .orElse(null);
    }

    public static ImageCache instance;

    public static ImageCache instance() {
        if (instance == null) {
            instance = new ImageCache();
        }
        return instance;
    }

    record DisplayImage(DynamicTexture texture, ResourceLocation location, String imageId) {
    }
}
