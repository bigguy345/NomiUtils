package com.goat.goatae2.container.slot;

import appeng.container.slot.SlotFake;
import com.goat.goatae2.container.ContainerLevelMaintainer;
import com.goat.goatae2.tile.TileLevelMaintainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.items.IItemHandler;

public class ContainerDualLevelMaintainer extends ContainerLevelMaintainer {
    public ContainerDualLevelMaintainer(InventoryPlayer ip, TileLevelMaintainer tile) {
        super(ip, tile, false);
    }

    public void initSlots(InventoryPlayer ip) {
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

        this.bindPlayerInventory(ip, 0, 169);
    }
}
