package com.goat.goatae2.mixin.client.ae2.gui;

import appeng.api.storage.ITerminalHost;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.implementations.GuiCraftConfirm;
import appeng.core.localization.GuiText;
import com.goat.goatae2.network.PacketHandler;
import com.goat.goatae2.network.packet.OpenCraftingGUI;
import com.goat.goatae2.tile.TileLevelMaintainer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiCraftConfirm.class, remap = false)
public class MixinGuiCraftConfirm extends AEBaseGui {

    public MixinGuiCraftConfirm(Container container) {
        super(container);
    }

    @Shadow
    public void drawFG(int i, int i1, int i2, int i3) {

    }

    @Shadow
    public void drawBG(int i, int i1, int i2, int i3) {

    }

    @Shadow
    private GuiButton cancel;

    @Unique
    private boolean isMaintainer;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void init(InventoryPlayer inventoryPlayer, ITerminalHost te, CallbackInfo ci) {
        if (te instanceof TileLevelMaintainer) {
            isMaintainer = true;
        }
    }

    @Inject(method = "initGui", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 2, shift = At.Shift.BEFORE), remap = true)
    protected void actionPerformed(CallbackInfo ci) {
        if (isMaintainer)
            this.cancel = new GuiButton(0, this.guiLeft + 6, this.guiTop + this.ySize - 25, 50, 20, GuiText.Cancel.getLocal());
    }

    @Inject(method = "actionPerformed", at = @At("HEAD"), remap = true, cancellable = true)
    protected void actionPerformed(GuiButton btn, CallbackInfo ci) {
        if (isMaintainer && btn == cancel) {
            PacketHandler.Instance.sendToServer(new OpenCraftingGUI(OpenCraftingGUI.Type.LEVEL_MAINTAINER, null));
            ci.cancel();
        }
    }
}
