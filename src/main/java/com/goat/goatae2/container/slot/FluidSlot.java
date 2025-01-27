package com.goat.goatae2.container.slot;

import appeng.api.storage.data.IAEFluidStack;
import appeng.container.slot.IJEITargetSlot;
import appeng.container.slot.SlotFake;
import appeng.fluids.items.FluidDummyItem;
import appeng.fluids.util.AEFluidStack;
import com.goat.goatae2.Utility;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.Objects;

public class FluidSlot extends SlotFake implements IJEITargetSlot {

    public FluidSlot(IItemHandler inv, int idx, int x, int y) {
        super(inv, idx, x, y);
    }

    public void putStack(ItemStack is) {
        if (is.isEmpty()) {
            super.putStack(is);
        } else if (is.getItem() instanceof FluidDummyItem) {
            FluidStack fluid = Utility.dummy2fluid(is);
            IAEFluidStack afs = AEFluidStack.fromFluidStack(fluid).setStackSize(is.getCount());
            ItemStack stackRep = afs.asItemStackRepresentation();
            stackRep.setCount(is.getCount());
            super.putStack(stackRep);
            getContainer().detectAndSendChanges();
        } else if (is.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            IFluidTankProperties[] tanks = Objects.requireNonNull(is.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)).getTankProperties();
            for (IFluidTankProperties tank : tanks) {
                AEFluidStack fluid = AEFluidStack.fromFluidStack(tank.getContents());
                super.putStack(fluid.asItemStackRepresentation());
            }
            getContainer().detectAndSendChanges();
        } else
            super.putStack(ItemStack.EMPTY);
    }

    @Override
    public int getSlotStackLimit() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getItemStackLimit(@Nonnull ItemStack stack) {
        return Integer.MAX_VALUE;
    }
}