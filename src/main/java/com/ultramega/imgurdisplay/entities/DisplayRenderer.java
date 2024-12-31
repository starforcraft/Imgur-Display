package com.ultramega.imgurdisplay.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.ultramega.imgurdisplay.ImageCache;
import com.ultramega.imgurdisplay.ImgurDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.awt.geom.Point2D;

@OnlyIn(Dist.CLIENT)
public class DisplayRenderer extends EntityRenderer<DisplayEntity> {
    private static final ResourceLocation EMPTY_DISPLAY = ResourceLocation.fromNamespaceAndPath(ImgurDisplay.MODID, "textures/item/empty_display.png");
    private static final ResourceLocation DISPLAY_SIDE = ResourceLocation.fromNamespaceAndPath(ImgurDisplay.MODID, "textures/item/display_side.png");

    private static final float THICKNESS = 1F / 16F;
    private static final float BORDER_THICKNESS = 2F / 16F;

    private static Minecraft mc;

    public DisplayRenderer(EntityRendererProvider.Context context) {
        super(context);
        mc = Minecraft.getInstance();
    }

    @Override
    public void render(DisplayEntity entity, float entityYaw, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {
        renderImage(entity.getImageID(), entity.isStretched(), entity.getGifFrameCount() > 0, entity.getGifFrameIndex(), entity.getGifFrameDelay(), entity.getFacing(), entity.getDisplayWidth(), entity.getDisplayHeight(), poseStack, buffer, packedLight);
        if (entity.isShowHitbox()) renderBoundingBox(entity, poseStack, buffer);
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    public static void renderImage(String imageId, boolean isStretched, boolean isGif, int frameIndex, int frameDelay, Direction facing, float width, float height, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        matrixStack.pushPose();

        float imageRatio = 1F;
        boolean hasImage = false;
        ResourceLocation location = EMPTY_DISPLAY;

        if(!imageId.isEmpty()) {
            ResourceLocation newLocation;
            if (!isGif) {
                newLocation = ImageCache.instance().getImage(imageId);
            } else {
                newLocation = ImageCache.instance().getGif(imageId, frameIndex / frameDelay);
            }

            if(newLocation != null) {
                location = newLocation;
                Point2D size = ImageCache.instance().getSize(imageId);
                if (size != null) {
                    imageRatio = (float) size.getX() / (float) size.getY();
                    hasImage = true;
                }
            }
        }

        if (!hasImage) {
            isStretched = true;
        }

        matrixStack.translate(-0.5D, 0D, -0.5D);

        rotate(facing, matrixStack);

        float frameRatio = width / height;

        float ratio = imageRatio / frameRatio;
        float ratioX;
        float ratioY;

        if (isStretched) {
            ratioX = 0F;
            ratioY = 0F;
        } else {
            if (ratio >= 1F) {
                ratioY = (1F - 1F / ratio) / 2F;
                ratioX = 0F;
            } else {
                ratioX = (1F - ratio) / 2F;
                ratioY = 0F;
            }

            ratioX *= width;
            ratioY *= height;
        }

        VertexConsumer builderFront = buffer.getBuffer(RenderType.entityCutout(location));

        if (!hasImage) {
            // Front, Main Frame
            vertex(builderFront, matrixStack, ratioX + BORDER_THICKNESS, ratioY + BORDER_THICKNESS, THICKNESS, BORDER_THICKNESS, 1F - BORDER_THICKNESS, packedLight);
            vertex(builderFront, matrixStack, width - ratioX - BORDER_THICKNESS, ratioY + BORDER_THICKNESS, THICKNESS, 1F - BORDER_THICKNESS, 1F - BORDER_THICKNESS, packedLight);
            vertex(builderFront, matrixStack, width - ratioX - BORDER_THICKNESS, height - ratioY - BORDER_THICKNESS, THICKNESS, 1F - BORDER_THICKNESS, 0F + BORDER_THICKNESS, packedLight);
            vertex(builderFront, matrixStack, ratioX + BORDER_THICKNESS, height - ratioY - BORDER_THICKNESS, THICKNESS, BORDER_THICKNESS, 0F + BORDER_THICKNESS, packedLight);

            // Front, Borders Top
            vertex(builderFront, matrixStack, 0.0F, height - BORDER_THICKNESS, THICKNESS, 0F, 0F + BORDER_THICKNESS, packedLight);
            vertex(builderFront, matrixStack, width, height - BORDER_THICKNESS, THICKNESS, 1F, 0F + BORDER_THICKNESS, packedLight);
            vertex(builderFront, matrixStack, width, height, THICKNESS, 1F, 0F, packedLight);
            vertex(builderFront, matrixStack, 0.0F, height, THICKNESS, 0F, 0F, packedLight);

            // Front, Borders Bottom
            vertex(builderFront, matrixStack, 0.0F, 0.0F, THICKNESS, 0F, 1F, packedLight);
            vertex(builderFront, matrixStack, width, 0.0F, THICKNESS, 1F, 1F, packedLight);
            vertex(builderFront, matrixStack, width, BORDER_THICKNESS, THICKNESS, 1F, 1F - BORDER_THICKNESS, packedLight);
            vertex(builderFront, matrixStack, 0.0F, BORDER_THICKNESS, THICKNESS, 0F, 1F - BORDER_THICKNESS, packedLight);

            // Front, Borders Left
            vertex(builderFront, matrixStack, 0.0F, BORDER_THICKNESS, THICKNESS, 0F, 1F - BORDER_THICKNESS, packedLight);
            vertex(builderFront, matrixStack, BORDER_THICKNESS, BORDER_THICKNESS, THICKNESS, BORDER_THICKNESS, 1F - BORDER_THICKNESS, packedLight);
            vertex(builderFront, matrixStack, BORDER_THICKNESS, height - BORDER_THICKNESS, THICKNESS, BORDER_THICKNESS, 0F + BORDER_THICKNESS, packedLight);
            vertex(builderFront, matrixStack, 0.0F, height - BORDER_THICKNESS, THICKNESS, 0F, 0F + BORDER_THICKNESS, packedLight);

            // Front, Borders Right
            vertex(builderFront, matrixStack, width - BORDER_THICKNESS, BORDER_THICKNESS, THICKNESS, 1F - BORDER_THICKNESS, 1F - BORDER_THICKNESS, packedLight);
            vertex(builderFront, matrixStack, width, BORDER_THICKNESS, THICKNESS, 1F, 1F - BORDER_THICKNESS, packedLight);
            vertex(builderFront, matrixStack, width, height - BORDER_THICKNESS, THICKNESS, 1F, 0F + BORDER_THICKNESS, packedLight);
            vertex(builderFront, matrixStack, width - BORDER_THICKNESS, height - BORDER_THICKNESS, THICKNESS, 1F - BORDER_THICKNESS, 0F + BORDER_THICKNESS, packedLight);
        } else {
            // Front
            vertex(builderFront, matrixStack, 0F + ratioX, ratioY, THICKNESS, 0F, 1F, packedLight);
            vertex(builderFront, matrixStack, width - ratioX, ratioY, THICKNESS, 1F, 1F, packedLight);
            vertex(builderFront, matrixStack, width - ratioX, height - ratioY, THICKNESS, 1F, 0F, packedLight);
            vertex(builderFront, matrixStack, ratioX, height - ratioY, THICKNESS, 0F, 0F, packedLight);
        }

        VertexConsumer builderSide = buffer.getBuffer(RenderType.entityCutout(DISPLAY_SIDE));

        // Left
        vertex(builderSide, matrixStack, 0F + ratioX, 0F + ratioY, 0F, 1F, 0F + ratioY, packedLight);
        vertex(builderSide, matrixStack, 0F + ratioX, 0F + ratioY, THICKNESS, 1F - THICKNESS, 0F + ratioY, packedLight);
        vertex(builderSide, matrixStack, 0F + ratioX, height - ratioY, THICKNESS, 1F - THICKNESS, 1F - ratioY, packedLight);
        vertex(builderSide, matrixStack, 0F + ratioX, height - ratioY, 0F, 1F, 1F - ratioY, packedLight);

        // Right
        vertex(builderSide, matrixStack, width - ratioX, 0F + ratioY, 0F, 0F, 0F + ratioY, packedLight);
        vertex(builderSide, matrixStack, width - ratioX, height - ratioY, 0F, 0F, 1F - ratioY, packedLight);
        vertex(builderSide, matrixStack, width - ratioX, height - ratioY, THICKNESS, THICKNESS, 1F - ratioY, packedLight);
        vertex(builderSide, matrixStack, width - ratioX, 0F + ratioY, THICKNESS, THICKNESS, 0F + ratioY, packedLight);

        // Top
        vertex(builderSide, matrixStack, 0F + ratioX, height - ratioY, 0F, 0F + ratioX, 1F, packedLight);
        vertex(builderSide, matrixStack, 0F + ratioX, height - ratioY, THICKNESS, 0F + ratioX, 1F - THICKNESS, packedLight);
        vertex(builderSide, matrixStack, width - ratioX, height - ratioY, THICKNESS, 1F - ratioX, 1F - THICKNESS, packedLight);
        vertex(builderSide, matrixStack, width - ratioX, height - ratioY, 0F, 1F - ratioX, 1F, packedLight);

        // Bottom
        vertex(builderSide, matrixStack, 0F + ratioX, 0F + ratioY, 0F, 0F + ratioX, 0F, packedLight);
        vertex(builderSide, matrixStack, width - ratioX, 0F + ratioY, 0F, 1F - ratioX, 0F, packedLight);
        vertex(builderSide, matrixStack, width - ratioX, 0F + ratioY, THICKNESS, 1F - ratioX, THICKNESS, packedLight);
        vertex(builderSide, matrixStack, 0F + ratioX, 0F + ratioY, THICKNESS, 0F + ratioX, THICKNESS, packedLight);

        VertexConsumer builderBack = buffer.getBuffer(RenderType.entityCutout(EMPTY_DISPLAY));

        // Back
        vertex(builderBack, matrixStack, width - ratioX, 0F + ratioY, 0F, 1F - ratioX, 0F + ratioY, packedLight);
        vertex(builderBack, matrixStack, 0F + ratioX, 0F + ratioY, 0F, 0F + ratioX, 0F + ratioY, packedLight);
        vertex(builderBack, matrixStack, 0F + ratioX, height - ratioY, 0F, 0F + ratioX, 1F - ratioY, packedLight);
        vertex(builderBack, matrixStack, width - ratioX, height - ratioY, 0F, 1F - ratioX, 1F - ratioY, packedLight);

        matrixStack.popPose();
    }

    private static void vertex(VertexConsumer builder, PoseStack matrixStack, float x, float y, float z, float u, float v, int packetLight) {
        PoseStack.Pose stack = matrixStack.last();
        Matrix4f matrix4f = stack.pose();
        builder.addVertex(matrix4f, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packetLight)
                .setNormal(stack, 0F, 0F, -1F);
    }

    private static void renderBoundingBox(DisplayEntity entity, PoseStack poseStack, MultiBufferSource buffer) {
        if (!(mc.hitResult instanceof EntityHitResult) || ((EntityHitResult) mc.hitResult).getEntity() != entity) {
            return;
        }
        if (mc.options.hideGui) {
            return;
        }

        poseStack.pushPose();

        AABB hitBox = entity.getBoundingBox().move(-entity.getX(), -entity.getY(), -entity.getZ());
        LevelRenderer.renderLineBox(poseStack, buffer.getBuffer(RenderType.lines()), hitBox, 0F, 0F, 0F, 0.4F);

        poseStack.popPose();
    }

    public static void rotate(Direction facing, PoseStack matrixStack) {
        switch (facing) {
            case NORTH:
                matrixStack.translate(1D, 0D, 1D);
                matrixStack.mulPose(Axis.YP.rotationDegrees(180F));
                break;
            case SOUTH:
                break;
            case EAST:
                matrixStack.translate(0D, 0D, 1D);
                matrixStack.mulPose(Axis.YP.rotationDegrees(90F));
                break;
            case WEST:
                matrixStack.translate(1D, 0D, 0D);
                matrixStack.mulPose(Axis.YP.rotationDegrees(270F));
                break;
        }
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull DisplayEntity entity) {
        return EMPTY_DISPLAY;
    }
}
