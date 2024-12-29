package com.ultramega.imgurdisplay.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class PlaceholderEditBox extends EditBox {
    private final String placeholderText;

    public PlaceholderEditBox(Font font, int x, int y, int width, int height, String placeholderText) {
        super(font, x, y, width, height, Component.empty());
        this.placeholderText = placeholderText;
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);

        if(!isFocused() && getValue().isEmpty()) {
            graphics.drawString(this.font, placeholderText, this.getX() + 4, this.getY() + (this.height - 8) / 2, 4210752);
        }
    }
}
