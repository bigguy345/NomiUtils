package com.goat.goatae2.client.gui.impl;

import appeng.api.config.FuzzyMode;
import appeng.api.config.Upgrades;
import appeng.core.localization.GuiText;
import com.goat.goatae2.GOATAE2;
import com.goat.goatae2.container.slot.ContainerDualLevelMaintainer;
import com.goat.goatae2.tile.TileLevelMaintainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiDualLevelMaintainer extends GuiLevelMaintainer {
    public GuiDualLevelMaintainer(InventoryPlayer ipl, TileLevelMaintainer tile) {
        super(new ContainerDualLevelMaintainer(ipl, tile));
        this.cont = (ContainerDualLevelMaintainer) inventorySlots;
        this.tile = tile;
        this.xSize = 246;
        this.ySize = 251;
    }

    private static final ResourceLocation BG = GOATAE2.resource("textures/gui/dual_level_maintainer.png");

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        fontRenderer.drawString(localize("dual_level_maintainer"), 8, 6, 0x404040);
        fontRenderer.drawString(localize("maintainer.items"), 8, 18, 0x404040);
        fontRenderer.drawString(localize("maintainer.fluids"), 8, ySize - 143, 0x404040);
        fontRenderer.drawString(localize("maintainer.toCraft"), 183, ySize - 210, 0x404040);
        fontRenderer.drawString(localize("maintainer.whenUnder"), 183, ySize - 182, 0x404040);
        fontRenderer.drawString(GuiText.inventory.getLocal(), 8, ySize - 94, 0x404040);
        

        if (this.fuzzyMode != null) {
            this.fuzzyMode.setVisibility(tile.getInstalledUpgrades(Upgrades.FUZZY) > 0);
            FuzzyMode fzMode = cont.getFuzzyMode();
            this.fuzzyMode.set(fzMode);
        }
    }

    
    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(BG);
        this.drawTexturedModalRect(offsetX, offsetY, 0, 0, 256, 251);

        threshold.drawTextBox();
        batchSize.drawTextBox();
    
    }
}
