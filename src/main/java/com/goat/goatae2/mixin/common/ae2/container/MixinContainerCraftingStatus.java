package com.goat.goatae2.mixin.common.ae2.container;

import appeng.api.storage.ITerminalHost;
import appeng.container.implementations.ContainerCraftingCPU;
import appeng.container.implementations.ContainerCraftingStatus;
import com.goat.goatae2.tile.TileLevelMaintainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ContainerCraftingStatus.class, remap = false)
public class MixinContainerCraftingStatus extends ContainerCraftingCPU {

    public MixinContainerCraftingStatus(InventoryPlayer ip, Object te) {
        super(ip, te);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void init(InventoryPlayer ip, ITerminalHost te, CallbackInfo ci) {
        if (getTarget() instanceof TileLevelMaintainer)
            ((TileLevelMaintainer) getTarget()).addListener(ip.player);
    }

    @Unique
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        if (getTarget() instanceof TileLevelMaintainer)
            ((TileLevelMaintainer) getTarget()).removeListener(player);
    }
}
