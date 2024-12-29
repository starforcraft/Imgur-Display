package com.ultramega.imgurdisplay.gui;

import com.ultramega.imgurdisplay.ImageCache;
import com.ultramega.imgurdisplay.ImgurDisplay;
import com.ultramega.imgurdisplay.network.DisplayUpdateMessage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.ImageWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.widget.ExtendedButton;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class DisplayScreen extends Screen {
    private static final ResourceLocation BACKGROUND = new ResourceLocation(ImgurDisplay.MODID, "textures/gui/display.png");
    private static final ResourceLocation ERROR_ICON = new ResourceLocation("textures/gui/unseen_notification.png");
    private static final ResourceLocation CHECKBOX_TEXTURE = new ResourceLocation("textures/gui/checkbox.png");
    public static final int SCREEN_WIDTH = 176;
    public static final int SCREEN_HEIGHT = 146;

    private final UUID entityUUID;
    private final String imageId;
    private final boolean imageNotFound;

    private PlaceholderEditBox placeholderEditBox;
    private boolean isStretched;
    private boolean isEditRestricted;
    private boolean isShowHitbox;

    private int leftPos;
    private int topPos;

    public DisplayScreen(UUID entityUUID, String imageId, boolean isStretched, boolean isEditRestricted, boolean isShowHitbox) {
        super(Component.translatable("item.imgurdisplay.display"));
        this.entityUUID = entityUUID;
        this.imageId = imageId;
        this.imageNotFound = ImageCache.instance().getImageLocation(imageId) == null;
        this.isStretched = isStretched;
        this.isEditRestricted = isEditRestricted;
        this.isShowHitbox = isShowHitbox;
    }

    @Override
    protected void init() {
        this.leftPos = (this.width - SCREEN_WIDTH) / 2;
        this.topPos = (this.height - SCREEN_HEIGHT) / 2;

        placeholderEditBox = new PlaceholderEditBox(minecraft.font, leftPos + 5, topPos + 20, SCREEN_WIDTH - 10, 20, Component.translatable("gui.imgurdisplay.imgur_url_id").getString());
        placeholderEditBox.setValue(imageId);
        placeholderEditBox.setMaxLength(100);
        this.addRenderableWidget(placeholderEditBox);

        ImageWidget errorWidget = new ImageWidget(leftPos + SCREEN_WIDTH - 15, topPos + 15, 16, 16, ERROR_ICON);
        errorWidget.visible = imageNotFound && !placeholderEditBox.getValue().isEmpty();
        errorWidget.setTooltip(Tooltip.create(Component.translatable("gui.imgurdisplay.no_image_found")));
        this.addRenderableWidget(errorWidget);

        this.addRenderableWidget(new Checkbox(leftPos + 5, topPos + 45, 20, 20, Component.translatable("gui.imgurdisplay.stretch"), isStretched) {
            @Override
            public void onPress() {
                super.onPress();
                isStretched = selected();
            }
        });
        this.addRenderableWidget(new Checkbox(leftPos + 5, topPos + 70, 20, 20, Component.translatable("gui.imgurdisplay.restrict_editing"), isEditRestricted) {
            @Override
            public void onPress() {
                super.onPress();
                isEditRestricted = selected();
            }
        });
        this.addRenderableWidget(new Checkbox(leftPos + 5, topPos + 95, 20, 20, Component.translatable("gui.imgurdisplay.show_hitbox"), isShowHitbox) {
            @Override
            public void onPress() {
                super.onPress();
                isShowHitbox = selected();
            }
        });

        this.addRenderableWidget(new ExtendedButton(leftPos + (SCREEN_WIDTH - 80) / 2, topPos + 120, 80, 20, Component.translatable("gui.done"), (context) -> {
            this.saveData();
            this.onClose();
        }));
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.pose().pushPose();
        graphics.pose().translate((float)leftPos, (float)topPos, 0.0F);
        this.renderLabels(graphics);
        graphics.pose().popPose();
    }

    private void renderLabels(GuiGraphics graphics) {
        graphics.drawString(this.font, this.title, (SCREEN_WIDTH - this.font.width(this.title)) / 2, 6, 4210752, false);
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics graphics) {
        super.renderBackground(graphics);

        graphics.blit(BACKGROUND, leftPos, topPos, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
    }

    @Override
    public void onClose() {
        saveData();
        super.onClose();
    }

    private void saveData() {
        String newImageIdOrUrl = placeholderEditBox.getValue();
        boolean updateImage = !newImageIdOrUrl.equals(imageId);
        ImgurDisplay.NETWORK_HANDLER.sendToServer(new DisplayUpdateMessage(entityUUID, newImageIdOrUrl, updateImage, isStretched, isEditRestricted, isShowHitbox));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
