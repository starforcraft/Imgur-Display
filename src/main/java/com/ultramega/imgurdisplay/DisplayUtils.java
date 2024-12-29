package com.ultramega.imgurdisplay;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DisplayUtils {
    public static BufferedImage fetchImageFromUrl(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "image/*");
        connection.connect();

        try (InputStream inputStream = connection.getInputStream()) {
            return ImageIO.read(inputStream);
        } catch (IOException e) {
            System.err.println("Failed to fetch image from URL (" + url + "): " + e.getMessage());
            return null;
        }
    }

    public static NativeImage toNativeImage(BufferedImage image) {
        NativeImage nativeImage = new NativeImage(NativeImage.Format.RGBA, image.getWidth(), image.getHeight(), false);
        ColorModel colorModel = image.getColorModel();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Object elements = image.getRaster().getDataElements(x, y, null);

                int abgr = (colorModel.getAlpha(elements) << 24) |
                        (colorModel.getBlue(elements) << 16) |
                        (colorModel.getGreen(elements) << 8) |
                        colorModel.getRed(elements);

                nativeImage.setPixelRGBA(x, y, abgr);
            }
        }
        return nativeImage;
    }

    public static String encodeToHex(String imageId) {
        StringBuilder hexBuilder = new StringBuilder();
        for (char c : imageId.toCharArray()) {
            hexBuilder.append(String.format("%02x", (int) c));
        }
        return hexBuilder.toString();
    }

    public static void saveImageToDisk(Minecraft minecraft, String imageId, BufferedImage image, String format) {
        File textureDir = new File(minecraft.gameDirectory, "config/" + ImgurDisplay.MODID + "/images");
        if (!textureDir.exists() && !textureDir.mkdirs()) {
            ImgurDisplay.LOGGER.error("Failed to create image directory for image with id: {}", imageId);
            return;
        }

        File imageFile = new File(textureDir, DisplayUtils.encodeToHex(imageId) + "." + format.toLowerCase());
        try {
            ImageIO.write(image, format.toUpperCase(), imageFile);
        } catch (IOException e) {
            ImgurDisplay.LOGGER.error("Failed to save image with id {}: {}", imageId, e.getMessage());
        }
    }

    public static BufferedImage loadImageFromDisk(Minecraft minecraft, String imageId) {
        File textureDir = new File(minecraft.gameDirectory, "config/" + ImgurDisplay.MODID + "/images");

        String[] extensions = {"png", "jpg", "jpeg"};
        for (String extension : extensions) {
            File imageFile = new File(textureDir, DisplayUtils.encodeToHex(imageId) + "." + extension);
            if (imageFile.exists()) {
                try {
                    return ImageIO.read(imageFile);
                } catch (IOException e) {
                    ImgurDisplay.LOGGER.error("Failed to load image with id {}: {}", imageId, e.getMessage());
                }
            }
        }

        return null;
    }

    public static Matcher extractIdFormatFromIdOrUrl(String idOrUrl) {
        if (idOrUrl == null || idOrUrl.isEmpty()) {
            return null;
        }

        String regex = "(?:(?:https?://)?i\\.imgur\\.com/)?([a-zA-Z0-9]+)(?:\\.(\\w+))?";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(idOrUrl);

        return matcher.matches() ? matcher : null;
    }
}
