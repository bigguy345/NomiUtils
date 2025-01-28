package com.goat.goatae2.mixin.client.ae2.gui;

import appeng.api.storage.ITerminalHost;
import appeng.client.gui.implementations.GuiCraftAmount;
import com.goat.goatae2.tile.TileLevelMaintainer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.InventoryPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiCraftAmount.class, remap = false)
public class MixinGuiCraftAmount {

    @Shadow
    private GuiTextField amountToCraft;

    @Unique
    private TileLevelMaintainer tile;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void init(InventoryPlayer inventoryPlayer, ITerminalHost te, CallbackInfo ci) {
        if (te instanceof TileLevelMaintainer)
            tile = (TileLevelMaintainer) te;
    }

    @Inject(method = "initGui", at = @At("RETURN"), remap = true)
    public void init(CallbackInfo ci) {
        if (tile != null && tile.tempClickedSlot != -1) {
            amountToCraft.setText(tile.config.batchSizes[tile.tempClickedSlot] + "");
            tile.tempClickedSlot = -1;
        }
    }
}
