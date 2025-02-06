package com.goat.goatae2.block;

import com.goat.goatae2.constants.GuiTypes;
import com.goat.goatae2.tile.TileFluidLevelMaintainer;
import net.minecraft.block.material.Material;

public class BlockFluidLevelMaintainer extends BlockLevelMaintainer {

    public BlockFluidLevelMaintainer() {
        super(Material.IRON);
        setTileEntity(TileFluidLevelMaintainer.class);
    }

    public int getGuiId() {
        return GuiTypes.FLUID_LEVEL_MAINTAINER_ID;
    }
}
