package com.goat.goatae2.tile;

import appeng.api.config.*;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.networking.GridFlags;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkStorageEvent;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.IConfigManager;
import appeng.fluids.util.AEFluidStack;
import appeng.helpers.MultiCraftingTracker;
import appeng.helpers.Reflected;
import appeng.me.GridAccessException;
import appeng.me.helpers.MachineSource;
import appeng.parts.automation.UpgradeInventory;
import appeng.tile.grid.AENetworkTile;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;
import appeng.util.item.AEItemStack;
import com.goat.goatae2.Utility;
import com.goat.goatae2.util.DummyAdaptor;
import com.google.common.collect.ImmutableSet;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.UniversalBucket;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.items.IItemHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.goat.goatae2.GOATAE2.AE2FC_LOADED;

public class TileLevelMaintainer extends AENetworkTile implements ICraftingRequester, ITickable, ITerminalHost, IConfigManagerHost, IUpgradeableHost, IAEAppEngInventory {

    public int tick;
    public final ItemMaintainerInventory config = new ItemMaintainerInventory(this, 9 * 6);
    public final IActionSource source;

    public final MultiCraftingTracker craftingTracker = new MultiCraftingTracker(this, config.size);
    public final UpgradeInventory upgrades;

    protected int getUpgradeSlots() {
        return 1;
    }

    public final List<EntityPlayer> playersMonitored = new ArrayList<>();

    public boolean isOpened;
    private final IConfigManager manager;

    public int tempClickedSlot = -1; //temporarily saves GuiLevelMaintainer selected slot for GuiCraftConfirm

    public void addListener(EntityPlayer player) {
        playersMonitored.add(player);
        isOpened = !playersMonitored.isEmpty();
    }

    public void removeListener(EntityPlayer player) {
        playersMonitored.remove(player);
        isOpened = !playersMonitored.isEmpty();
    }

    @Reflected
    public TileLevelMaintainer() {
        getProxy().setIdlePowerUsage(2D);
        getProxy().setFlags(GridFlags.REQUIRE_CHANNEL);
        this.source = new MachineSource(this);
        this.manager = new ConfigManager(this);

        this.manager.registerSetting(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL);
        this.manager.registerSetting(Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE);
        this.upgrades = new UpgradeInventory(this, this.getUpgradeSlots()) {
            @Override
            public int getMaxInstalled(Upgrades upgrades) {
                return upgrades == Upgrades.FUZZY ? 1 : 0;
            }
        };
    }

    public <T extends IAEStack<T>> boolean canInsert(IMEMonitor<T> monitor, T stack) {
        T remaining = monitor.injectItems(stack, Actionable.SIMULATE, source);
        return remaining == null || remaining.getStackSize() <= 0;
    }

    public void doCraftCycle() {
        if (!getProxy().isActive())
            return;

        try {
            for (int i = 0; i < config.size; i++) {
                IAEItemStack item = this.config.items[i];
                int threshold = config.thresholds[i];
                int batchSize = config.batchSizes[i];
                if (item != null && batchSize > 0 && threshold > 0) {
                    FluidStack fluid = Utility.dummy2fluid(item);
                    if (fluid == null || item.getDefinition().getItem() instanceof UniversalBucket) {
                        IMEMonitor<IAEItemStack> itemMonitor = getItemMonitor();
                        IAEItemStack inStock = itemMonitor.extractItems(item, Actionable.SIMULATE, this.source);
                        //   System.out.println(inStock.getStackSize());
                        if (inStock == null || inStock.getStackSize() < threshold)
                            if (canInsert(itemMonitor, item.copy().setStackSize(batchSize))) {
                                this.craftingTracker.handleCrafting(i, batchSize, item, DummyAdaptor.INSTANCE, getWorld(), getProxy().getGrid(), getProxy().getCrafting(), this.source);
                                this.craftingTracker.handleCrafting(i, batchSize, item, DummyAdaptor.INSTANCE, getWorld(), getProxy().getGrid(), getProxy().getCrafting(), this.source);
                            }
                    } else if (AE2FC_LOADED) {
                        craftFluids(item, fluid, i, batchSize, threshold);
                    }
                }
                if ((batchSize < 1 || item != null && threshold < item.getStackSize()) && config.craftFailed[i])
                    config.craftFailed[i] = false;
            }
            markForUpdate();
        } catch (GridAccessException e) {
            //Ignore
        }
    }

    @Optional.Method(modid = "ae2fc")
    private void craftFluids(IAEItemStack item, FluidStack fluid, int slotId, int batchSize, int threshold) throws GridAccessException {
        IMEMonitor<IAEFluidStack> fluidMonitor = getFluidMonitor();
        IAEFluidStack afs = AEFluidStack.fromFluidStack(fluid);
        IAEFluidStack inStock = fluidMonitor.getStorageList().findPrecise(afs);
        if (inStock == null || inStock.getStackSize() < threshold) {
            if (canInsert(fluidMonitor, afs.copy().setStackSize(batchSize))) {
                this.craftingTracker.handleCrafting(slotId, batchSize, Utility.asAeStack(fluid), DummyAdaptor.INSTANCE, getWorld(), getProxy().getGrid(), getProxy().getCrafting(), this.source);
                this.craftingTracker.handleCrafting(slotId, batchSize, item, DummyAdaptor.INSTANCE, getWorld(), getProxy().getGrid(), getProxy().getCrafting(), this.source);
            }
        }
    }

    @Override
    protected boolean readFromStream(ByteBuf data) throws IOException {
        boolean changed = super.readFromStream(data);
        changed = config.readFromStream(data, changed);
        //  facing = EnumFacing.byHorizontalIndex(data.readInt());
        return changed;
    }

    @Override
    protected void writeToStream(ByteBuf data) throws IOException {
        super.writeToStream(data);
        config.writeToStream(data);

        //        if (facing != null) 
        //            data.writeInt(facing.getHorizontalIndex());

    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        config.readFromNBT(data.getCompoundTag("config"));
        craftingTracker.readFromNBT(data);
        this.manager.readFromNBT(data);
        this.upgrades.readFromNBT(data, "upgrades");
        //        if (data.hasKey("facing")) {
        //            facing = EnumFacing.byHorizontalIndex(data.getInteger("facing"));
        //        } else {
        //            facing = EnumFacing.NORTH;
        //        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);

        NBTTagCompound c = new NBTTagCompound();
        config.writeToNBT(c);
        data.setTag("config", c);
        craftingTracker.writeToNBT(data);
        this.manager.writeToNBT(data);
        this.upgrades.writeToNBT(data, "upgrades");
        // data.setInteger("facing",facing.getHorizontalIndex());
        return data;
    }

    public IMEMonitor<IAEFluidStack> getFluidMonitor() {
        return getProxy().getNode().getGrid().<IStorageGrid>getCache(IStorageGrid.class).getInventory(Utility.getFluidChannel());
    }

    public IMEMonitor<IAEItemStack> getItemMonitor() {
        return getProxy().getNode().getGrid().<IStorageGrid>getCache(IStorageGrid.class).getInventory(Utility.getItemChannel());
    }

    @MENetworkEventSubscribe
    public void onStorageUpdate(MENetworkStorageEvent event) {
        if (isOpened)
            config.updateSystemStackSizes();
    }

    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        return this.craftingTracker.getRequestedJobs();
    }

    @Override
    public void jobStateChange(ICraftingLink link) {
        int id = ((IMultiCraftingTracker) craftingTracker).getLinkSlot(link);
        if ((link.isCanceled() || link.isDone()) && id > -1 && id < config.size)
            config.isCrafting[id] = false;

        this.craftingTracker.jobStateChange(link);
    }

    @Override
    public IAEItemStack injectCraftedItems(ICraftingLink link, IAEItemStack items, Actionable mode) {
        if (getProxy().isActive()) {
            IEnergyGrid energy = null;
            try {
                energy = this.getProxy().getEnergy();
            } catch (GridAccessException e) {
            }

            double power = (double) items.getStackSize() / 1000.0;
            if (energy != null && energy.extractAEPower(power, mode, PowerMultiplier.CONFIG) > power - 0.01) {
                ItemStack inputStack = items.getCachedItemStack(items.getStackSize());
                FluidStack fluid = Utility.getFcFluidStack(inputStack);

                if (fluid == null) {
                    IMEMonitor<IAEItemStack> itemGrid = getItemMonitor();
                    if (itemGrid != null) {
                        IAEItemStack remaining;
                        if (mode == Actionable.SIMULATE) {
                            remaining = itemGrid.injectItems(AEItemStack.fromItemStack(inputStack), Actionable.SIMULATE, this.source);
                            items.setCachedItemStack(inputStack);
                        } else {
                            remaining = itemGrid.injectItems(AEItemStack.fromItemStack(inputStack), Actionable.MODULATE, this.source);
                            if (remaining == null || remaining.getStackSize() <= 0L) {
                                ItemStack tmp = remaining != null ? remaining.getDefinition() : null;
                                items.setCachedItemStack(tmp);
                            }
                        }
                        if ((remaining != null ? remaining.getDefinition() : null) == inputStack) {
                            return items;
                        }

                        return remaining;
                    }
                } else if (AE2FC_LOADED) {
                    injectCraftedFluids(items, inputStack, fluid, mode);
                }
            }
        }


        return items;
    }

    @Optional.Method(modid = "ae2fc")
    private IAEItemStack injectCraftedFluids(IAEItemStack items, ItemStack inputStack, FluidStack fluid, Actionable mode) {
        IMEMonitor<IAEFluidStack> fluidGrid = this.getFluidMonitor();
        IAEFluidStack remaining;
        if (mode == Actionable.SIMULATE) {
            remaining = fluidGrid.injectItems(AEFluidStack.fromFluidStack(fluid), Actionable.SIMULATE, this.source);
            items.setCachedItemStack(inputStack);
        } else {
            remaining = fluidGrid.injectItems(AEFluidStack.fromFluidStack(fluid), Actionable.MODULATE, this.source);
            if (remaining == null || remaining.getStackSize() <= 0L) {
                ItemStack tmp = Utility.asAeStack(remaining) != null ? Utility.asAeStack(remaining).getDefinition() : null;
                items.setCachedItemStack(tmp);
            }
        }

        if (Utility.asItemStack(remaining != null ? remaining.getFluidStack() : null) == inputStack) {
            return items;
        }

        return Utility.asAeStack(remaining);
    }

    @Override
    public void update() {
        if (!getWorld().isRemote) {

            // if (tick % (20 * 2) == 0 && !isOpened)
            //  doCraftCycle();
            tick++;
        }
    }

    @Override
    public IConfigManager getConfigManager() {
        return manager;
    }

    public IItemHandler getInventoryHandler() {
        return this.config;
    }

    @Override
    public IItemHandler getInventoryByName(String name) {
        if (name.equals("upgrades")) {
            return this.upgrades;
        }

        return null;
    }

    @Override
    public <T extends IAEStack<T>> IMEMonitor<T> getInventory(IStorageChannel<T> iStorageChannel) {
        return null;
    }

    @Override
    public void updateSetting(IConfigManager iConfigManager, Enum anEnum, Enum anEnum1) {

    }

    @Override
    public int getInstalledUpgrades(Upgrades upgrades) {
        return this.upgrades.getInstalledUpgrades(upgrades);
    }

    @Override
    public void onChangeInventory(IItemHandler inv, int i, InvOperation invOperation, ItemStack itemStack, ItemStack itemStack1) {
        if (inv == this.upgrades) {
        }
    }
}
    
