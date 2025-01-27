package com.goat.goatae2.mixin.common.ae2;

import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.data.IAEItemStack;
import appeng.crafting.CraftBranchFailure;
import appeng.crafting.CraftingJob;
import appeng.util.Platform;
import com.glodblock.github.loader.FCItems;
import com.goat.goatae2.Utility;
import com.goat.goatae2.tile.ICraftBranchFailure;
import com.goat.goatae2.tile.IMultiCraftingTracker;
import com.goat.goatae2.tile.TileLevelMaintainer;
import com.google.common.base.Stopwatch;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Optional;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.goat.goatae2.GOATAE2.AE2FC_LOADED;

@Mixin(value = CraftingJob.class, remap = false)
public class MixinCraftingJob {
    @Shadow
    @Final
    private IActionSource actionSrc;

    @Inject(method = "run", at = @At(value = "INVOKE", target = "Lappeng/crafting/CraftingJob;logCraftingJob(Ljava/lang/String;Lcom/google/common/base/Stopwatch;)V", ordinal = 1, shift = At.Shift.BEFORE))
    //  @Redirect(method = "run", at = @At(value = "INVOKE", target = "Lappeng/crafting/CraftingJob;logCraftingJob(Ljava/lang/String;Lcom/google/common/base/Stopwatch;)V"))
    public void onSucess(CallbackInfo ci) {
        //   logCraftingJob(type, stopwatch);
        logCrafting("success", null);
    }

    @Inject(method = "run", at = @At(value = "INVOKE", target = "Lappeng/crafting/CraftingJob;logCraftingJob(Ljava/lang/String;Lcom/google/common/base/Stopwatch;)V", ordinal = 2, shift = At.Shift.BEFORE))
    public void onFailed1(CallbackInfo ci, @Local(ordinal = 0) LocalRef<CraftBranchFailure> ex) {
        logCrafting("failed", ex.get());
    }

    @Inject(method = "run", at = @At(value = "INVOKE", target = "Lappeng/crafting/CraftingJob;logCraftingJob(Ljava/lang/String;Lcom/google/common/base/Stopwatch;)V", ordinal = 3, shift = At.Shift.BEFORE))
    public void onFailed2(CallbackInfo ci, @Local(ordinal = 0) LocalRef<CraftBranchFailure> ex) {
        logCrafting("failed", ex.get());
    }

    @Unique
    public void logCrafting(String type, CraftBranchFailure e) {
        if (actionSrc.machine().get() instanceof TileLevelMaintainer) {
            CraftingJob thi = (CraftingJob) (Object) this;

            TileLevelMaintainer tile = (TileLevelMaintainer) actionSrc.machine().get();
            IMultiCraftingTracker tr = (IMultiCraftingTracker) tile.craftingTracker;
            HashMap<Integer, ICraftingJob> jobs = tr.getActiveJobsMap();

            Iterator<Map.Entry<Integer, ICraftingJob>> it = jobs.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Integer, ICraftingJob> pair = it.next();

                if (pair.getValue() == thi) {
                    int id = pair.getKey();
                    if (type.contains("success")) {
                        tile.config.isCrafting[id] = true;
                        tile.config.craftFailed[id] = false;
                        tile.config.failReason[id] = null;
                    } else if (type.contains("failed")) {
                        tile.config.craftFailed[id] = true;
                        tile.config.failReason[id] = getFailReason(pair.getValue(), e);
                    }
                    it.remove(); //success and failed jobs are only removed here
                }
            }
        }
    }

    public String getFailReason(ICraftingJob job, CraftBranchFailure ex) {
        if (ex == null)
            return null;

        IAEItemStack failedItem = ((ICraftBranchFailure) ex).getItem();
        IAEItemStack expectedItem = job.getOutput();
        boolean itemsEqual = expectedItem.getItem() == failedItem.getItem();

        if (AE2FC_LOADED && isFluidDrops(failedItem)) {
            return getLiquidFailReason(failedItem, expectedItem);
        }


        if (itemsEqual && expectedItem.getStackSize() == failedItem.getStackSize())
            return "maintainer.failReason.invalidPattern";

        if (!itemsEqual)
            return "maintainer.failReason.missingIngredients";

        return null;
    }

    @Unique
    @Optional.Method(modid = "ae2fc")
    private String getLiquidFailReason(IAEItemStack failedItem, IAEItemStack expectedItem) {
        FluidStack failed = Utility.getFcFluidStack(failedItem.getDefinition());
        FluidStack expected = Utility.getFcFluidStack(expectedItem.getDefinition());

        boolean fluidsEqual = failed.equals(expected);

        if (fluidsEqual && expectedItem.getStackSize() == failedItem.getStackSize())
            return "maintainer.failReason.invalidPattern";

        if (!fluidsEqual)
            return "maintainer.failReason.missingIngredients";
        return null;
    }

    @Unique
    @Optional.Method(modid = "ae2fc")
    private boolean isFluidDrops(IAEItemStack item) {
        return item.getItem() == FCItems.FLUID_DROP;
    }

    @Shadow
    private void logCraftingJob(String type, Stopwatch timer) {

    }
}
