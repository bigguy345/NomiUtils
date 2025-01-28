package com.goat.goatae2;

import appeng.api.AEApi;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.util.item.AEItemStack;
import com.glodblock.github.loader.FCItems;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.UniversalBucket;
import net.minecraftforge.fml.common.Optional;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nullable;
import java.util.Objects;

import static com.goat.goatae2.GOATAE2.AE2FC_LOADED;

public class Utility {

    public static  <T extends IAEStack<T>> boolean isCraftable(IMEMonitor<T> monitor, T stack) {
        T inStock = monitor.getStorageList().findPrecise(stack);
        return inStock != null && inStock.isCraftable();
    }

    public static boolean isFluidCraftable(IMEMonitor<IAEItemStack> monitor, FluidStack fluid) {
        return isCraftable(monitor, getCorrectCraftingFluid(fluid));
    }

    @Unique
    public static IAEItemStack getCorrectCraftingItem(IAEItemStack item) {
        FluidStack fluid = Utility.dummy2fluid(item);
        if (fluid == null || item.getDefinition().getItem() instanceof UniversalBucket) {
            return item;
        } else if (AE2FC_LOADED) {
            return getCorrectCraftingFluid(fluid);
        }
        return item;
    }

    @Optional.Method(modid = "ae2fc")
    public static IAEItemStack getCorrectCraftingFluid(FluidStack fluid) {
        return Utility.asAeStack(fluid);
    }

    public static FluidStack dummy2fluid(ItemStack stack) {
        if (!stack.isEmpty() && stack.hasTagCompound()) {
            FluidStack fluid = FluidStack.loadFluidStackFromNBT(Objects.requireNonNull(stack.getTagCompound()));
            return fluid != null && fluid.amount > 0 ? fluid : null;
        } else {
            return null;
        }
    }

    public static FluidStack dummy2fluid(IAEItemStack stack) {
        return stack != null ? dummy2fluid(stack.getDefinition()) : null;
    }

    public static boolean isFluid(IAEItemStack stack) {
        FluidStack fluid = dummy2fluid(stack.getDefinition());
        return fluid != null && !(stack.getDefinition().getItem() instanceof UniversalBucket);
    }

    public static IStorageChannel<IAEFluidStack> getFluidChannel() {
        return AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class);
    }

    public static IStorageChannel<IAEItemStack> getItemChannel() {
        return AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
    }

    public static FluidStack getFcFluidStack(ItemStack stack) {
        if (!stack.isEmpty() && stack.hasTagCompound()) {
            NBTTagCompound tag = Objects.requireNonNull(stack.getTagCompound());
            if (!tag.hasKey("Fluid", 8)) {
                return null;
            } else {
                Fluid fluid = FluidRegistry.getFluid(tag.getString("Fluid"));
                if (fluid == null) {
                    return null;
                } else {
                    FluidStack fluidStack = new FluidStack(fluid, stack.getCount());
                    if (tag.hasKey("FluidTag", 10)) {
                        fluidStack.tag = tag.getCompoundTag("FluidTag");
                    }

                    return fluidStack;
                }
            }
        } else {
            return null;
        }
    }

    @Optional.Method(modid = "ae2fc")
    public static ItemStack asItemStack(@Nullable FluidStack fluid) {
        if (fluid != null && fluid.amount > 0) {
            ItemStack stack = new ItemStack(FCItems.FLUID_DROP, fluid.amount);

            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("Fluid", fluid.getFluid().getName());
            if (fluid.tag != null) {
                tag.setTag("FluidTag", fluid.tag);
            }

            stack.setTagCompound(tag);
            return stack;
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Optional.Method(modid = "ae2fc")
    public static IAEItemStack asAeStack(@Nullable IAEFluidStack fluid) {
        if (fluid != null && fluid.getStackSize() > 0L) {
            IAEItemStack stack = AEItemStack.fromItemStack(asItemStack(fluid.getFluidStack()));
            if (stack == null) {
                return null;
            } else {
                stack.setStackSize(fluid.getStackSize());
                return stack;
            }
        } else {
            return null;
        }
    }

    @Optional.Method(modid = "ae2fc")
    public static IAEItemStack asAeStack(@Nullable FluidStack fluid) {
        if (fluid != null && fluid.amount > 0) {
            IAEItemStack stack = AEItemStack.fromItemStack(asItemStack(fluid));
            if (stack == null) {
                return null;
            } else {
                stack.setStackSize(fluid.amount);
                return stack;
            }
        } else {
            return null;
        }
    }
}
