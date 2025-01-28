package com.goat.goatae2.mixin.client.ae2.gui;

import appeng.client.gui.AEBaseGui;
import appeng.client.render.StackSizeRenderer;
import com.goat.goatae2.client.gui.GuiLevelMaintainer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AEBaseGui.class)
public abstract class MixinAEBaseGui extends GuiScreen {

    @Inject(method = "handleMouseClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/InventoryPlayer;setItemStack(Lnet/minecraft/item/ItemStack;)V", shift = At.Shift.AFTER), cancellable = true)
    private void handleBookmarkedGhost(Slot slot, int slotIdx, int mouseButton, ClickType clickType, CallbackInfo ci) {
        if ((AEBaseGui) (Object) (this) instanceof GuiLevelMaintainer)
            ci.cancel();
    }

    @Shadow
    @Final
    private StackSizeRenderer stackSizeRenderer;

    @Inject(method = "drawSlot", cancellable = true, at = @At(value = "INVOKE", target = "Lappeng/client/render/StackSizeRenderer;renderStackSize(Lnet/minecraft/client/gui/FontRenderer;Lappeng/api/storage/data/IAEItemStack;II)V", ordinal = 2, shift = At.Shift.BEFORE))
    private void onRenderingStackSize(Slot s, CallbackInfo ci) {
        if ((AEBaseGui) (Object) (this) instanceof GuiLevelMaintainer) {
            GuiLevelMaintainer gui = (GuiLevelMaintainer) (Object) (this);
            stackSizeRenderer.renderStackSize(this.fontRenderer, gui.tile.config.items[s.slotNumber], s.xPos, s.yPos);
            ci.cancel();
        }
    }
}
