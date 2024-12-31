package com.ultramega.imgurdisplay;

import com.mojang.blaze3d.platform.NativeImage;
import com.ultramega.imgurdisplay.utils.DisplayUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class ImageCache {
    private final List<DisplayImage> imageCache = Collections.synchronizedList(new ArrayList<>());

    public void addTexture(String imageId, BufferedImage image, byte[] gifData, String format) {
        boolean alreadyPresent = imageCache.stream()
            .map(DisplayImage::imageId)
            .anyMatch(id -> id.contains(imageId));
        if (alreadyPresent) {
            return;
        }

        DisplayUtils.saveFileToDisk(Minecraft.getInstance(), imageId, image, gifData, format);
        if (format.equals("gif") && gifData != null) {
            loadGif(imageId, gifData);
        } else if (image != null) {
            loadStaticImage(imageId, image);
        }
    }

    public ResourceLocation getImage(String imageId) {
        ResourceLocation location = getImageLocation(imageId);

        if (location == null) {
            byte[] imgBytes = DisplayUtils.loadFileFromDisk(Minecraft.getInstance(), imageId);
            if (imgBytes != null) {
                try {
                    BufferedImage image = DisplayUtils.decodeImage(imgBytes);
                    if (image != null) {
                        return loadStaticImage(imageId, image);
                    }
                } catch (IOException ignored) {
                }
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

    public boolean imageExists(String imageId) {
        return imageCache.stream()
                .anyMatch(image -> image.imageId.equals(imageId));
    }

    public ResourceLocation getGif(String imageId, int frame) {
        List<ResourceLocation> location = getGifLocations(imageId);

        if (location == null) {
            byte[] gifData = DisplayUtils.loadFileFromDisk(Minecraft.getInstance(), imageId);
            if (gifData != null) {
                List<ResourceLocation> frameLocations = loadGif(imageId, gifData);
                if (frameLocations != null && frameLocations.size() >= frame) {
                    return frameLocations.get(frame);
                }
            }
        }

        if (location != null) {
            return location.get(frame);
        } else {
            return null;
        }
    }

    public List<ResourceLocation> getGifLocations(String imageId) {
        return imageCache.stream()
                .filter(image -> image.imageId.equals(imageId))
                .findFirst()
                .map(DisplayImage::frameLocations)
                .orElse(null);
    }

    private ResourceLocation loadStaticImage(String imageId, BufferedImage image) {
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath(ImgurDisplay.MODID, "textures/image/" + DisplayUtils.encodeToHex(imageId));
        NativeImage img = DisplayUtils.toNativeImage(image);
        imageCache.add(new DisplayImage(location, imageId, img.getWidth(), img.getHeight()));
        Minecraft.getInstance().getEntityRenderDispatcher().textureManager.register(location, new DynamicTexture(img));

        return location;
    }

    private List<ResourceLocation> loadGif(String imageId, byte[] gifData) {
        try {
            List<BufferedImage> frames = DisplayUtils.decodeGifFrames(gifData);
            if (frames.isEmpty()) {
                ImgurDisplay.LOGGER.error("No frames found in GIF with id: {}", imageId);
                return null;
            }

            int width = 0;
            int height = 0;
            List<ResourceLocation> frameLocations = new ArrayList<>();
            for (int i = 0; i < frames.size(); i++) {
                BufferedImage frame = frames.get(i);
                ResourceLocation location = ResourceLocation.fromNamespaceAndPath(ImgurDisplay.MODID, "textures/image/" + DisplayUtils.encodeToHex(imageId) + "_frame" + i);
                NativeImage img = DisplayUtils.toNativeImage(frame);
                width = img.getWidth();
                height = img.getHeight();
                Minecraft.getInstance().getEntityRenderDispatcher().textureManager.register(location, new DynamicTexture(img));
                frameLocations.add(location);
            }

            imageCache.add(new DisplayImage(frameLocations, imageId, width, height));

            return frameLocations;
        } catch (Exception e) {
            ImgurDisplay.LOGGER.error("Failed to load GIF with id {}: {}", imageId, e.getMessage());
            return null;
        }
    }

    public Point2D getSize(String imageId) {
        return imageCache.stream()
                .filter(image -> image.imageId.equals(imageId))
                .findFirst()
                .map(image -> new Point2D.Float(image.width, image.height))
                .orElse(null);
    }

    public static ImageCache instance;

    public static ImageCache instance() {
        if (instance == null) {
            instance = new ImageCache();
        }
        return instance;
    }

    record DisplayImage(ResourceLocation location,// Static images
                        List<ResourceLocation> frameLocations, // GIFs
                        String imageId,
                        int width,
                        int height) {
        public DisplayImage(ResourceLocation location, String imageId, int width, int height) {
            this(location, null, imageId, width, height);
        }

        public DisplayImage(List<ResourceLocation> frameLocations, String imageId, int width, int height) {
            this(null, frameLocations, imageId, width, height);
        }
    }
}
