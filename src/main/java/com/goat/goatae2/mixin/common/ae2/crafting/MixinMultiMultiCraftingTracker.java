package com.goat.goatae2.mixin.common.ae2.crafting;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.data.IAEItemStack;
import appeng.helpers.MultiCraftingTracker;
import appeng.util.InventoryAdaptor;
import com.goat.goatae2.tile.IMultiCraftingTracker;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.concurrent.Future;

@Mixin(value = MultiCraftingTracker.class, remap = false)
public abstract class MixinMultiMultiCraftingTracker implements IMultiCraftingTracker {

    @Shadow
    private Future<ICraftingJob>[] jobs;

    @Unique
    private HashMap<Integer, ICraftingJob> jobsMap = new HashMap<>();

    @Unique
    private int lastSlot;

    public Future<ICraftingJob>[] getJobs() {
        return jobs;
    }

    @Override
    public HashMap<Integer, ICraftingJob> getActiveJobsMap() {
        return jobsMap;
    }

    @Override
    public int getLastSlot() {
        return lastSlot;
    }

    @Override
    public void setLinkP(int slot, ICraftingLink l) {
        this.setLink(slot, l);
    }

    @Shadow
    int getSlot(ICraftingLink link) {
        return 0;
    }

    @Shadow
    private void setLink(int slot, ICraftingLink l) {

    }

    @Override
    public int getLinkSlot(ICraftingLink link) {
        return getSlot(link);
    }

    @Inject(method = "handleCrafting", at = @At(value = "INVOKE", target = "Lappeng/helpers/MultiCraftingTracker;setJob(ILjava/util/concurrent/Future;)V", ordinal = 1, shift = At.Shift.BEFORE))
    public void captureJobs(int x, long itemToCraft, IAEItemStack ais, InventoryAdaptor d, World w, IGrid g, ICraftingGrid cg, IActionSource mySrc, CallbackInfoReturnable<Boolean> cir) {
        lastSlot = x;
    }

    @Inject(method = "setJob", at = @At("HEAD"))
    private void captureJobs(int slot, Future<ICraftingJob> l, CallbackInfo ci) {
        if (l != null) {
            try {
                jobsMap.put(slot, l.get());
            } catch (Exception e) {
            }
        }
    }
}
