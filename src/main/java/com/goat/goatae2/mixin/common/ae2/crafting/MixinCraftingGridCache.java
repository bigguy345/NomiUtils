package com.goat.goatae2.mixin.common.ae2.crafting;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingCallback;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.data.IAEItemStack;
import appeng.crafting.CraftingJob;
import appeng.me.cache.CraftingGridCache;
import com.goat.goatae2.tile.IMultiCraftingTracker;
import com.goat.goatae2.tile.TileLevelMaintainer;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.Future;

@Mixin(value = CraftingGridCache.class, remap = false)
public class MixinCraftingGridCache {

    @Inject(method = "beginCraftingJob", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/ExecutorService;submit(Ljava/lang/Runnable;Ljava/lang/Object;)Ljava/util/concurrent/Future;", shift = At.Shift.BEFORE))
    private void captureJobs(World world, IGrid grid, IActionSource actionSrc, IAEItemStack slotItem, ICraftingCallback cb, CallbackInfoReturnable<Future<ICraftingJob>> cir, @Local(name = "job") LocalRef<CraftingJob> iJob) {
        if (actionSrc.machine().get() instanceof TileLevelMaintainer) {
            IMultiCraftingTracker tr = (IMultiCraftingTracker) ((TileLevelMaintainer) actionSrc.machine().get()).craftingTracker;
            tr.getActiveJobsMap().put(tr.getLastSlot(), iJob.get());
        }
    }

}
