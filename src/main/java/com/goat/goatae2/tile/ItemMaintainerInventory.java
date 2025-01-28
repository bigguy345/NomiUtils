package com.goat.goatae2.tile;

import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.AELog;
import appeng.fluids.util.AEFluidStack;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import com.goat.goatae2.Utility;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.UniversalBucket;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.items.IItemHandlerModifiable;

public class ItemMaintainerInventory implements IItemHandlerModifiable {

    public int size;
    private final TileLevelMaintainer owner;
    public final IAEItemStack[] items;
    public int[] thresholds;
    public int[] batchSizes;
    public boolean[] inSystemStock;
    public boolean[] isCraftable;
    public boolean[] isCrafting;
    public boolean[] craftFailed;
    public String[] failReason;

    public ItemMaintainerInventory(TileLevelMaintainer owner, int s) {
        this.owner = owner;
        this.size = s;
        this.items = new IAEItemStack[s];
        this.thresholds = new int[s];
        this.batchSizes = new int[s];
        this.inSystemStock = new boolean[s];
        isCraftable = new boolean[s];
        isCrafting = new boolean[s];
        craftFailed = new boolean[s];
        failReason = new String[s];
    }

    public void setThreshold(int id, int amount) {
        if (id < 0 || amount < 0) {
            return;
        }
        this.thresholds[id] = amount;
        owner.markForUpdate();
        owner.markDirty();
    }

    public void setBatchSize(int id, int amount) {
        if (id < 0 || amount < 0) {
            return;
        }
        this.batchSizes[id] = amount;
        owner.markForUpdate();
        owner.markDirty();
    }

    @Override
    public int getSlots() {
        return size;
    }

    public ItemStack getStackInSlot(int var1) {
        return items[var1] == null ? ItemStack.EMPTY : items[var1].createItemStack();
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return null;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return null;
    }

    @Override
    public int getSlotLimit(int slot) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return IItemHandlerModifiable.super.isItemValid(slot, stack);
    }

    public void updateSystemStackSizes() {
        if (!Platform.isServer())
            return;

        boolean proxyActive = owner.getProxy().isActive();
        for (int i = 0; i < items.length; i++) {
            IAEItemStack item = items[i];
            if (item != null) {
                long stockNumber = getSystemStackSize(i, item, proxyActive);
                item.setStackSize(stockNumber == 0 ? 1 : stockNumber);
                inSystemStock[i] = stockNumber > 0;
            }
        }
        owner.markForUpdate();
    }

    private long getSystemStackSize(int slotId, IAEItemStack item, boolean proxyActive) {
        if (!proxyActive)
            return 0;

        long stockNumber = 0;
        FluidStack fluid = Utility.dummy2fluid(item.getDefinition());
        if (fluid == null || item.getDefinition().getItem() instanceof UniversalBucket) {
            if (owner.getInstalledUpgrades(Upgrades.FUZZY) > 0) {
                FuzzyMode fzMode = (FuzzyMode) owner.getConfigManager().getSetting(Settings.FUZZY_MODE);
                for (IAEItemStack inStock : owner.getItemMonitor().getStorageList().findFuzzy(AEItemStack.fromItemStack(item.getDefinition()), fzMode)) {
                    if (inStock != null) {
                        isCraftable[slotId] = inStock.isCraftable();
                        stockNumber += inStock.getStackSize();
                    }
                }
            } else {
                IAEItemStack inStock = owner.getItemMonitor().getStorageList().findPrecise(AEItemStack.fromItemStack(item.getDefinition()));
                if (inStock != null) {
                    isCraftable[slotId] = inStock.isCraftable();
                    stockNumber += inStock.getStackSize();
                }
            }
        } else {
            IAEFluidStack inStock = owner.getFluidMonitor().getStorageList().findPrecise(AEFluidStack.fromFluidStack(fluid));
            isCraftable[slotId] = Utility.isFluidCraftable(owner.getItemMonitor(), fluid);
            if (inStock != null)
                stockNumber = inStock.getStackSize();
        }
        return stockNumber;
    }

    public void setStackInSlot(int slot, ItemStack newItemStack) {
        IAEItemStack item = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(newItemStack);
        items[slot] = item;
        if (item != null && !item.getDefinition().isEmpty())
            item.setCraftable(isCraftable[slot]);
        
        if (Platform.isServer() && item != null && !item.getDefinition().isEmpty()) {
            long stockNumber = getSystemStackSize(slot, item, owner.getProxy().isActive());
            item.setStackSize(stockNumber == 0 ? 1 : stockNumber);
            inSystemStock[slot] = stockNumber > 0;

            owner.markForUpdate();
        }
    }

    public void writeToNBT(NBTTagCompound target) {
        if (target == null)
            return;

        for (int x = 0; x < this.size; ++x) {
            try {
                if (this.items[x] != null) {
                    NBTTagCompound c = new NBTTagCompound();
                    this.items[x].writeToNBT(c);
                    c.setInteger("threshold", thresholds[x]);
                    c.setInteger("batchSize", batchSizes[x]);
                    c.setBoolean("inStock", inSystemStock[x]);
                    c.setBoolean("isCraftable", isCraftable[x]);
                    c.setBoolean("isCrafting", isCrafting[x]);
                    c.setBoolean("craftFailed", craftFailed[x]);
                    if (failReason[x] != null)
                        c.setString("failReason", failReason[x]);
                    target.setTag("#" + x, c);
                }
            } catch (Exception var4) {
            }
        }
    }

    public void readFromNBT(NBTTagCompound target) {
        if (target == null)
            return;

        for (int x = 0; x < this.size; ++x) {
            try {
                NBTTagCompound c = target.hasKey("#" + x) ? target.getCompoundTag("#" + x) : null;
                if (c != null) {
                    this.items[x] = AEItemStack.fromNBT(c);
                    thresholds[x] = c.getInteger("threshold");
                    batchSizes[x] = c.getInteger("batchSize");
                    inSystemStock[x] = c.getBoolean("inStock");
                    isCraftable[x] = c.getBoolean("isCraftable");
                    isCrafting[x] = c.getBoolean("isCrafting");
                    craftFailed[x] = c.getBoolean("craftFailed");
                    failReason[x] = c.hasKey("failReason") ? c.getString("failReason") : null;
                }
            } catch (Exception var4) {
                AELog.debug(var4);
            }
        }
    }

    public void writeToStream(ByteBuf data) {
        for (int x = 0; x < size; x++) {
            if (items[x] != null) {
                data.writeInt(x);
                ByteBufUtils.writeItemStack(data, getStackInSlot(x));
                data.writeInt(thresholds[x]);
                data.writeInt(batchSizes[x]);
                data.writeBoolean(inSystemStock[x]);
                data.writeBoolean(isCraftable[x]);
                data.writeBoolean(isCrafting[x]);
                data.writeBoolean(craftFailed[x]);
                data.writeBoolean(failReason[x] == null);
                if (failReason[x] != null)
                    ByteBufUtils.writeUTF8String(data, failReason[x]);
            }
        }
    }

    public boolean readFromStream(ByteBuf data, boolean changed) {
        for (int x = 0; x < size; x++) {
            int order = data.readInt();
            ItemStack stack = ByteBufUtils.readItemStack(data);
            if (!ItemStack.areItemStacksEqual(stack, getStackInSlot(order))) {
                setStackInSlot(order, stack);
                changed = true;
            }
            thresholds[order] = data.readInt();
            batchSizes[order] = data.readInt();
            inSystemStock[order] = data.readBoolean();
            isCraftable[order] = data.readBoolean();
            items[order].setCraftable(isCraftable[order]);
            isCrafting[order] = data.readBoolean();
            craftFailed[order] = data.readBoolean();
            boolean failReasonNull = data.readBoolean();
            if (!failReasonNull)
                failReason[order] = ByteBufUtils.readUTF8String(data);
        }
        return changed;
    }
}
