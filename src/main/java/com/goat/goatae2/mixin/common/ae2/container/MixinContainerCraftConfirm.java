package com.goat.goatae2.mixin.common.ae2.container;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.security.IActionHost;
import appeng.api.storage.ITerminalHost;
import appeng.container.AEBaseContainer;
import appeng.container.implementations.ContainerCraftConfirm;
import appeng.container.implementations.CraftingCPURecord;
import com.goat.goatae2.GOATAE2;
import com.goat.goatae2.constants.GuiTypes;
import com.goat.goatae2.tile.IMultiCraftingTracker;
import com.goat.goatae2.tile.TileLevelMaintainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Future;

@Mixin(value = ContainerCraftConfirm.class, remap = false)
public abstract class MixinContainerCraftConfirm extends AEBaseContainer {

    public MixinContainerCraftConfirm(InventoryPlayer ip, Object te) {
        super(ip, te);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void init(InventoryPlayer ip, ITerminalHost te, CallbackInfo ci) {
        if (getTarget() instanceof TileLevelMaintainer)
            ((TileLevelMaintainer) getTarget()).addListener(ip.player);
    }

    @Inject(method = "onContainerClosed", at = @At("RETURN"))
    public void onContainerClosed(EntityPlayer par1EntityPlayer, CallbackInfo ci) {
        if (getTarget() instanceof TileLevelMaintainer)
            ((TileLevelMaintainer) getTarget()).removeListener(par1EntityPlayer);
    }

    @Shadow
    private ICraftingJob result;
    @Shadow
    public boolean simulation = true;
    @Shadow
    @Final
    private ArrayList<CraftingCPURecord> cpus;

    @Shadow
    public int selectedCpu = -1;

    @Shadow
    private boolean cpuMatches(ICraftingCPU c) {
        return false;
    }

    @Shadow
    public void setJob(Future<ICraftingJob> job) {

    }

    @Shadow
    public abstract World getWorld();

    @Inject(method = "startJob", at = @At("HEAD"),cancellable = true)
    public void onStartJob(CallbackInfo ci) {
        if (getTarget() instanceof TileLevelMaintainer) {
            TileLevelMaintainer tile = (TileLevelMaintainer) getTarget();
            IGrid grid = ((IActionHost) tile).getActionableNode().getGrid();
            if (this.result != null && !simulation) {
                ICraftingGrid cc = grid.getCache(ICraftingGrid.class);
                ICraftingLink link = cc.submitJob(result, tile, selectedCpu == -1 ? null : getCPUs(cc).get(selectedCpu), true, tile.source);
                if (link == null) {
                    this.setJob(cc.beginCraftingJob(getWorld(), grid, tile.source, this.result.getOutput(), null));
                } else {
                    int id = tile.tempClickedSlot;
                    if (id > -1 && id < tile.config.size) {
                        ((IMultiCraftingTracker) tile.craftingTracker).setLinkP(id, link);
                        tile.config.isCrafting[id] = true;
                        tile.config.craftFailed[id] = false;
                        tile.config.failReason[id] = null;
                        tile.tempClickedSlot = -1;
                    }

                    BlockPos pos = tile.getPos();
                    EntityPlayer p = getInventoryPlayer().player;
                    p.openGui(GOATAE2.INSTANCE, GuiTypes.LEVEL_MAINTAINER_ID, p.getEntityWorld(), pos.getX(), pos.getY(), pos.getZ());
                }
            }
            ci.cancel();
        }
    }

    @Unique
    public ArrayList<ICraftingCPU> getCPUs(ICraftingGrid grid) {
        ArrayList<ICraftingCPU> cpus = new ArrayList<>();
        for (final ICraftingCPU c : grid.getCpus())
            if (this.cpuMatches(c))
                cpus.add(c);

        Collections.sort(this.cpus);
        return cpus;
    }
}
