package com.goat.goatae2.block;

import com.goat.goatae2.constants.GuiTypes;
import com.goat.goatae2.tile.TileDualLevelMaintainer;
import net.minecraft.block.material.Material;

public class BlockDualLevelMaintainer extends BlockLevelMaintainer {

    public BlockDualLevelMaintainer() {
        super(Material.IRON);
        setTileEntity(TileDualLevelMaintainer.class);
    }

    public int getGuiId() {
        return GuiTypes.DUAL_LEVEL_MAINTAINER_ID;
    }
}
