package com.goat.goatae2.client.gui.components;

import appeng.client.gui.widgets.ITooltip;
import com.goat.goatae2.GOATAE2;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class GuiImgButtonGAE2 extends GuiButton implements ITooltip {
    public static final ResourceLocation STATES = GOATAE2.resource("textures/gui/states.png");

    private int iconIndex;
    private String name, toolTip;

    public GuiImgButtonGAE2(int x, int y, int iconIndex) {
        super(0, 0, 16, "");
        this.x = x;
        this.y = y;
        this.width = 16;
        this.height = 16;
        this.iconIndex = iconIndex;
    }//

    public GuiImgButtonGAE2 setText(String name, String toolTip) {
        this.name = name;
        this.toolTip = toolTip;
        return this;
    }

    @Override
    public void drawButton(final Minecraft par1Minecraft, final int par2, final int par3, float partial) {
        if (this.visible) {


            if (this.enabled) {
                GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            } else {
                GlStateManager.color(0.5f, 0.5f, 0.5f, 1.0f);
            }

            par1Minecraft.renderEngine.bindTexture(STATES);
            this.hovered = par2 >= this.x && par3 >= this.y && par2 < this.x + this.width && par3 < this.y + this.height;

            int TEXTURE_SIZE = 64;
            int uv_x = (iconIndex % 4) * 16;// (int) Math.floor((float)iconIndex / 16);
            int uv_y = (iconIndex / 4) * 16;


            Gui.drawModalRectWithCustomSizedTexture(this.x, this.y, TEXTURE_SIZE - 16, TEXTURE_SIZE - 16, width, height, TEXTURE_SIZE, TEXTURE_SIZE);
            Gui.drawModalRectWithCustomSizedTexture(this.x, this.y, uv_x , uv_y , width, height, TEXTURE_SIZE, TEXTURE_SIZE);
            this.mouseDragged(par1Minecraft, par2, par3);


            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    @Override
    public String getMessage() {
        final StringBuilder sb = new StringBuilder(toolTip);

        int i = sb.lastIndexOf("\n");
        if (i <= 0) {
            i = 0;
        }
        while (i + 30 < sb.length() && (i = sb.lastIndexOf(" ", i + 30)) != -1) {
            sb.replace(i, i + 1, "\n");
        }

        return name + '\n' + sb;
    }

    @Override
    public int xPos() {
        return this.x;
    }

    @Override
    public int yPos() {
        return this.y;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public boolean isVisible() {
        return this.visible;
    }
}
