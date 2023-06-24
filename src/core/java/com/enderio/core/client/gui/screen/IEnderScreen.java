package com.enderio.core.client.gui.screen;

import com.enderio.core.client.gui.IIcon;
import com.enderio.core.common.util.Vector2i;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public interface IEnderScreen {

    default Screen getScreen() {
        return (Screen) this;
    }

    static void renderIcon(GuiGraphics guiGraphics, Vector2i pos, IIcon icon) {
        if (!icon.shouldRender())
            return;

        RenderSystem.enableBlend();
        guiGraphics.blit(icon.getTextureLocation(), pos.x(), pos.y(), icon.getRenderSize().x(), icon.getRenderSize().y(), icon.getTexturePosition().x(),
            icon.getTexturePosition().y(), icon.getIconSize().x(), icon.getIconSize().y(), icon.getTextureSize().x(), icon.getTextureSize().y());
        RenderSystem.disableBlend();
    }

    default void renderSimpleArea(GuiGraphics guiGraphics, Vector2i pos, Vector2i pos2) {
        guiGraphics.fill(pos.x(), pos.y(), pos2.x(), pos2.y(), 0xFF8B8B8B);
        guiGraphics.fill(pos.x(), pos.y(), pos2.x() - 1, pos2.y() - 1, 0xFF373737);
        guiGraphics.fill(pos.x() + 1, pos.y() + 1, pos2.x(), pos2.y(), 0xFFFFFFFF);
        guiGraphics.fill(pos.x() + 1, pos.y() + 1, pos2.x() - 1, pos2.y() - 1, 0xFF8B8B8B);
    }

    default void renderIconBackground(GuiGraphics guiGraphics, Vector2i pos, IIcon icon) {
        renderSimpleArea(guiGraphics, pos, pos.add(icon.getRenderSize()).expand(2));
    }

    default void renderTooltipAfterEverything(GuiGraphics guiGraphics, Component pText, int pMouseX, int pMouseY) {
        addTooltip(new LateTooltipData(guiGraphics, pText, pMouseX, pMouseY));
    }

    void addTooltip(LateTooltipData data);

    class LateTooltipData {
        private final GuiGraphics guiGraphics;
        private final Component text;
        private final int mouseX;
        private final int mouseY;

        LateTooltipData(GuiGraphics guiGraphics, Component text, int mouseX, int mouseY) {
            this.guiGraphics = guiGraphics;
            this.text = text;
            this.mouseX = mouseX;
            this.mouseY = mouseY;
        }

        public GuiGraphics getGuiGraphics() {
            return guiGraphics;
        }

        public Component getText() {
            return text;
        }

        public int getMouseX() {
            return mouseX;
        }

        public int getMouseY() {
            return mouseY;
        }
    }
}