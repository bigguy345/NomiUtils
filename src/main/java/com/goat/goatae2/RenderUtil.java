package com.goat.goatae2;

import appeng.api.storage.data.IAEItemStack;
import appeng.core.AEConfig;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

import javax.annotation.Nullable;

public class RenderUtil {
    public static void renderStackSize(FontRenderer fontRenderer, IAEItemStack aeStack, int xPos, int yPos, @Nullable String text) {
        if (!aeStack.getDefinition().isEmpty()) {
            if (text != null) {
                float scaleFactor = AEConfig.instance().useTerminalUseLargeFont() ? 0.85F : 0.5F;
                float inverseScaleFactor = 1.0F / scaleFactor;
                int offset = AEConfig.instance().useTerminalUseLargeFont() ? 0 : -1;
                fontRenderer.setUnicodeFlag(false);
                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                GlStateManager.disableBlend();
                GlStateManager.pushMatrix();
                GlStateManager.scale(scaleFactor, scaleFactor, scaleFactor);
                int X = (int) (((float) xPos + (float) offset + 16.0F - (float) fontRenderer.getStringWidth(text) * scaleFactor) * inverseScaleFactor);
                int Y = (int) (((float) yPos + (float) offset + 16.0F - 7.0F * scaleFactor) * inverseScaleFactor);
                fontRenderer.drawStringWithShadow(text, (float) X, (float) Y, 16777215);
                GlStateManager.popMatrix();
                GlStateManager.enableLighting();
                GlStateManager.enableDepth();
                GlStateManager.enableBlend();
            }
        }
    }
}
