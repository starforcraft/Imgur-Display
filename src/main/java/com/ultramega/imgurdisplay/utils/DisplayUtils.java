package com.ultramega.imgurdisplay.utils;

import com.mojang.blaze3d.platform.NativeImage;
import com.ultramega.imgurdisplay.ImgurDisplay;
import net.minecraft.client.Minecraft;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadataNode;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DisplayUtils {
    public static byte[] fetchImageFromUrl(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "image/*");
        connection.connect();

        try (InputStream inputStream = connection.getInputStream()) {
            return inputStream.readAllBytes();
        } catch (IOException e) {
            System.err.println("Failed to fetch image from URL (" + url + "): " + e.getMessage());
            return null;
        }
    }

    public static BufferedImage decodeImage(byte[] byteArray) throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);

        return ImageIO.read(byteArrayInputStream);
    }

    public static List<BufferedImage> decodeGifFrames(byte[] gifData) throws IOException {
        List<BufferedImage> frames = new ArrayList<>();

        GifDecoder decoder = new GifDecoder();
        decoder.read(new ByteArrayInputStream(gifData));

        int frameCount = decoder.getFrameCount();
        for (int i = 0; i < frameCount; i++) {
            BufferedImage frame = decoder.getFrame(i);
            frames.add(frame);
        }

        return frames;
    }

    public static int getFrameDelayForFrame(byte[] gifData, int frameIndex) {
        try (InputStream gifInputStream = new ByteArrayInputStream(gifData)) {
            ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
            reader.setInput(ImageIO.createImageInputStream(gifInputStream));

            IIOMetadataNode root = (IIOMetadataNode) reader.getImageMetadata(frameIndex).getAsTree("javax_imageio_gif_image_1.0");

            IIOMetadataNode gceNode = (IIOMetadataNode) root.getElementsByTagName("GraphicControlExtension").item(0);

            String delayTime = gceNode.getAttribute("delayTime");
            reader.dispose();

            int delay = Integer.parseInt(delayTime) / 50;
            if (delay > 0) {
                return delay;
            }
        } catch (IOException e) {
            ImgurDisplay.LOGGER.error("Failed to get frame delay for GIF: {}", e.getMessage());
        }

        return 1;
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

    public static void saveFileToDisk(Minecraft minecraft, String imageId, BufferedImage image, byte[] gifData, String format) {
        File textureDir = new File(minecraft.gameDirectory, "config/" + ImgurDisplay.MODID + "/images");
        if (!textureDir.exists() && !textureDir.mkdirs()) {
            ImgurDisplay.LOGGER.error("Failed to create image directory for image with id: {}", imageId);
            return;
        }

        File file = new File(textureDir, DisplayUtils.encodeToHex(imageId) + "." + format.toLowerCase());
        if (format.equals("gif") && gifData != null) {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(gifData);
            } catch (IOException e) {
                ImgurDisplay.LOGGER.error("Failed to save GIF with id {}: {}", imageId, e.getMessage());
            }
        } else if (image != null) {
            try {
                ImageIO.write(image, format.toUpperCase(), file);
            } catch (IOException e) {
                ImgurDisplay.LOGGER.error("Failed to save image with id {}: {}", imageId, e.getMessage());
            }
        }
    }

    public static byte[] loadFileFromDisk(Minecraft minecraft, String imageId) {
        File textureDir = new File(minecraft.gameDirectory, "config/" + ImgurDisplay.MODID + "/images");

        String[] extensions = {"png", "jpg", "jpeg", "gif"};
        for (String extension : extensions) {
            File imageFile = new File(textureDir, DisplayUtils.encodeToHex(imageId) + "." + extension);
            if (imageFile.exists()) {
                try {
                    return Files.readAllBytes(imageFile.toPath());
                } catch (IOException e) {
                    ImgurDisplay.LOGGER.error("Failed to load image ({}.{}) from disk: {}", imageId, extension, e.getMessage());
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
