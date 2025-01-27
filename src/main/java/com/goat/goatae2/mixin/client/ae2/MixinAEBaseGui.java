package com.goat.goatae2.mixin.client.ae2;

import appeng.client.gui.AEBaseGui;
import com.goat.goatae2.client.gui.GuiLevelMaintainer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AEBaseGui.class)
public abstract class MixinAEBaseGui {

    @Inject(method = "handleMouseClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/InventoryPlayer;setItemStack(Lnet/minecraft/item/ItemStack;)V", shift = At.Shift.AFTER), cancellable = true)
    private void handleBookmarkedGhost(Slot slot, int slotIdx, int mouseButton, ClickType clickType, CallbackInfo ci) {
        if ((AEBaseGui) (Object) (this) instanceof GuiLevelMaintainer)
            ci.cancel();
        
    }
}
