package com.goat.goatae2;

import appeng.api.AEApi;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;
import com.glodblock.github.loader.FCItems;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.UniversalBucket;

import javax.annotation.Nullable;
import java.util.Objects;

public class Utility {

    public static FluidStack dummy2fluid(ItemStack stack) {
        if (!stack.isEmpty() && stack.hasTagCompound()) {
            NBTTagCompound cmpd = stack.getTagCompound();
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

    public static FluidStack getFluidStack(ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() == FCItems.FLUID_DROP && stack.hasTagCompound()) {
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

    public static ItemStack newStack(@Nullable FluidStack fluid) {
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

    public static IAEItemStack asAEStack(@Nullable IAEFluidStack fluid) {
        if (fluid != null && fluid.getStackSize() > 0L) {
            IAEItemStack stack = AEItemStack.fromItemStack(newStack(fluid.getFluidStack()));
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

    public static IAEItemStack asAEStack(@Nullable FluidStack fluid) {
        if (fluid != null && fluid.amount > 0) {
            IAEItemStack stack = AEItemStack.fromItemStack(newStack(fluid));
            if (stack == null) {
                return null;
            } else {
                stack.setStackSize((long) fluid.amount);
                return stack;
            }
        } else {
            return null;
        }
    }
}
