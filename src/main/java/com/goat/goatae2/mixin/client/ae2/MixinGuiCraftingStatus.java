package com.goat.goatae2.mixin.client.ae2;

import appeng.client.gui.implementations.GuiCraftingStatus;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.container.implementations.ContainerCraftingStatus;
import com.goat.goatae2.block.Blocks;
import com.goat.goatae2.network.PacketHandler;
import com.goat.goatae2.network.packet.OpenCraftingGUI;
import com.goat.goatae2.tile.TileLevelMaintainer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiCraftingStatus.class, remap = false)
public class MixinGuiCraftingStatus {

    @Shadow
    private ItemStack myIcon;
    @Shadow
    @Final
    private ContainerCraftingStatus status;

    @Shadow
    private GuiTabButton originalGuiBtn;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void init(CallbackInfo ci) {
        Object target = this.status.getTarget();
        if (target instanceof TileLevelMaintainer) {
            this.myIcon = new ItemStack(Blocks.LEVEL_MAINTAINER);
        }
    }

    @Inject(method = "actionPerformed", at = @At(value = "INVOKE", target = "Lappeng/client/gui/implementations/GuiCraftingCPU;actionPerformed(Lnet/minecraft/client/gui/GuiButton;)V", shift = At.Shift.AFTER), remap = true, cancellable = true)
    protected void actionPerformed(GuiButton btn, CallbackInfo ci) {
        if (btn == this.originalGuiBtn && this.status.getTarget() instanceof TileLevelMaintainer) {
            PacketHandler.Instance.sendToServer(new OpenCraftingGUI(2));
            ci.cancel();
        }
    }
}
