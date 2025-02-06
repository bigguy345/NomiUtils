package com.goat.goatae2.tile;

import com.goat.goatae2.block.Blocks;
import com.goat.goatae2.constants.GuiTypes;
import net.minecraft.item.ItemStack;

public class TileDualLevelMaintainer extends TileLevelMaintainer {
    public TileDualLevelMaintainer() {
        super();
    }

    protected void init() {
        rows = 9;
        columns = 6;
    }

    public ItemStack getIcon() {
        return new ItemStack(Blocks.DUAL_LEVEL_MAINTAINER);
    }

    public int getGuiId() {
        return GuiTypes.DUAL_LEVEL_MAINTAINER_ID;
    }
}
