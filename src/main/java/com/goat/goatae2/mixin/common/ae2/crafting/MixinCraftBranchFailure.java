package com.goat.goatae2.mixin.common.ae2.crafting;

import appeng.api.storage.data.IAEItemStack;
import appeng.crafting.CraftBranchFailure;
import com.goat.goatae2.tile.ICraftBranchFailure;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = CraftBranchFailure.class, remap = false)
public class MixinCraftBranchFailure implements ICraftBranchFailure {

    @Shadow
    @Final
    private IAEItemStack missing;

    @Override
    public IAEItemStack getItem() {
        return missing;
    }
}
