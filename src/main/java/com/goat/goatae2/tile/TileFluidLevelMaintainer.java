package com.goat.goatae2.tile;

import com.goat.goatae2.block.Blocks;
import com.goat.goatae2.constants.GuiTypes;
import net.minecraft.item.ItemStack;

public class TileFluidLevelMaintainer extends TileLevelMaintainer {

    public TileFluidLevelMaintainer() {
        super();
    }

    public ItemStack getIcon() {
        return new ItemStack(Blocks.FLUID_LEVEL_MAINTAINER);
    }

    public int getGuiId() {
        return GuiTypes.FLUID_LEVEL_MAINTAINER_ID;
    }
}
