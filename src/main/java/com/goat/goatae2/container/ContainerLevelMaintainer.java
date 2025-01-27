package com.goat.goatae2.container;

import appeng.api.config.FuzzyMode;
import appeng.api.config.Settings;
import appeng.container.AEBaseContainer;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.IOptionalSlotHost;
import appeng.container.slot.SlotFake;
import appeng.container.slot.SlotRestrictedInput;
import appeng.helpers.InventoryAction;
import appeng.util.Platform;
import com.goat.goatae2.container.slot.FluidSlot;
import com.goat.goatae2.network.PacketHandler;
import com.goat.goatae2.network.packet.SyncMaintainerGUIPacket;
import com.goat.goatae2.tile.ItemMaintainerInventory;
import com.goat.goatae2.tile.TileLevelMaintainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class ContainerLevelMaintainer extends AEBaseContainer implements IOptionalSlotHost {

    private final TileLevelMaintainer tile;
    @GuiSync(1)
    public FuzzyMode fzMode = FuzzyMode.IGNORE_ALL;

    public ContainerLevelMaintainer(InventoryPlayer ip, TileLevelMaintainer tile) {
        super(ip, tile);
        this.tile = tile;
        tile.addListener(ip.player);


        IItemHandler inv = tile.getInventoryHandler();
        final int xo = 8;
        final int yo = 23 + 6;

        for (int y = 0; y < 7; y++) {
            for (int x = 0; x < 9; x++) {
                if (y < 4) {
                    this.addSlotToContainer(new SlotFake(inv, y * 9 + x, xo + x * 18, yo + y * 18));
                } else if (y > 4 && y < 7) {
                    this.addSlotToContainer(new FluidSlot(inv, (y - 1) * 9 + x, xo + x * 18, yo + y * 18));
                }
            }
        }
        final IItemHandler upgrades = tile.getInventoryByName("upgrades");
        this.addSlotToContainer((new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 0, 188, 9, this.getInventoryPlayer()) {
            public void putStack(ItemStack stack) {
                super.putStack(stack);
                if (Platform.isServer()) {
                    tile.config.updateSystemStackSizes();
                    getContainer().detectAndSendChanges();
                }
            }
        }).setNotDraggable());

        tile.config.updateSystemStackSizes();
        this.bindPlayerInventory(ip, 0, 169);
    }

    public TileLevelMaintainer getTile() {
        return tile;
    }

    @Override
    public void doAction(EntityPlayerMP player, InventoryAction action, int slotId, long id) {
        if (action != InventoryAction.PICKUP_OR_SET_DOWN)
            return;
        Slot slot = getSlot(slotId);
        ItemStack oldStack = slot.getStack().copy();
        if (slot instanceof FluidSlot) {
            ItemStack hand = player.inventory.getItemStack();
            slot.putStack(hand.isEmpty() ? ItemStack.EMPTY : hand.copy());
        } else {
            super.doAction(player, action, slotId, id);
        }
        ItemStack newStack = slot.getStack().copy();
        if (!ItemStack.areItemStacksEqual(oldStack, newStack)) { //changed
            if (newStack.isEmpty()) {
                ItemMaintainerInventory inv = tile.config;
                inv.setThreshold(slotId, 0);
                inv.setBatchSize(slotId, 0);
                inv.inSystemStock[slotId] = inv.isCrafting[slotId] = inv.craftFailed[slotId] = false;
                inv.failReason[slotId] = null;
                
                PacketHandler.Instance.sendToPlayer(new SyncMaintainerGUIPacket(slotId, 0, SyncMaintainerGUIPacket.Type.CLEAR_SLOT), player);
            }
        }


        boolean slotEmpty = slot.getStack().isEmpty();
        PacketHandler.Instance.sendToPlayer(new SyncMaintainerGUIPacket(0, slotEmpty ? 0 : 1, SyncMaintainerGUIPacket.Type.TOGGLE_BOXES), player);


        tile.markForUpdate();
        detectAndSendChanges();
    }

    public void clear(EntityPlayer player) {
        ItemMaintainerInventory inv = (ItemMaintainerInventory) tile.getInventoryHandler();
        for (int x = 0; x < inv.size; ++x) {
            if (getSlot(x) != null && !getSlot(x).getStack().isEmpty()) {
                inv.setStackInSlot(x, ItemStack.EMPTY);
                inv.thresholds[x] = inv.batchSizes[x] = 0;
                inv.inSystemStock[x] = inv.isCrafting[x] = inv.craftFailed[x] = false;
                inv.failReason[x] = null;
            }
        }

        PacketHandler.Instance.sendToPlayer(new SyncMaintainerGUIPacket(0, 0, SyncMaintainerGUIPacket.Type.TOGGLE_BOXES), (EntityPlayerMP) player);
        PacketHandler.Instance.sendToPlayer(new SyncMaintainerGUIPacket(0, 0, SyncMaintainerGUIPacket.Type.CLEAR_ALL_SLOTS), (EntityPlayerMP) player);
        tile.markForUpdate();
        this.detectAndSendChanges();
    }

    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (Platform.isServer()) {
            fzMode = (FuzzyMode) tile.getConfigManager().getSetting(Settings.FUZZY_MODE);
        }
    }

    public FuzzyMode getFuzzyMode() {
        return this.fzMode;
    }

    public void setFuzzyMode(final FuzzyMode fzMode) {
        this.fzMode = fzMode;
    }

    @Override
    public boolean isSlotEnabled(int i) {
        return true;
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
//        tile.doCraftCycle();
        tile.removeListener(player);
    }
}
