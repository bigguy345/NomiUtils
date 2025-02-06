package com.goat.goatae2.block;

import com.goat.goatae2.GOATAE2;
import com.goat.goatae2.RegistryHandler;
import com.goat.goatae2.constants.Names;
import com.goat.goatae2.tile.TileDualLevelMaintainer;
import com.goat.goatae2.tile.TileFluidLevelMaintainer;
import com.goat.goatae2.tile.TileLevelMaintainer;
import net.minecraftforge.fml.common.registry.GameRegistry;

import static com.goat.goatae2.GOATAE2.AE2FC_LOADED;

public class Blocks {
    @GameRegistry.ObjectHolder(GOATAE2.MODID + ":" + Names.BLOCK_LEVEL_MAINTAINER)
    public static BlockLevelMaintainer LEVEL_MAINTAINER;

    @GameRegistry.ObjectHolder(GOATAE2.MODID + ":" + Names.BLOCK_FLUID_LEVEL_MAINTAINER)
    public static BlockFluidLevelMaintainer FLUID_LEVEL_MAINTAINER;

    @GameRegistry.ObjectHolder(GOATAE2.MODID + ":" + Names.BLOCK_DUAL_LEVEL_MAINTAINER)
    public static BlockDualLevelMaintainer DUAL_LEVEL_MAINTAINER;

    public static void init(RegistryHandler regHandler) {
        GameRegistry.registerTileEntity(TileLevelMaintainer.class, GOATAE2.resource(Names.BLOCK_LEVEL_MAINTAINER));
        regHandler.block(Names.BLOCK_LEVEL_MAINTAINER, new BlockLevelMaintainer());

        if (AE2FC_LOADED) {
            GameRegistry.registerTileEntity(TileFluidLevelMaintainer.class, GOATAE2.resource(Names.BLOCK_FLUID_LEVEL_MAINTAINER));
            regHandler.block(Names.BLOCK_FLUID_LEVEL_MAINTAINER, new BlockFluidLevelMaintainer());

            GameRegistry.registerTileEntity(TileDualLevelMaintainer.class, GOATAE2.resource(Names.BLOCK_DUAL_LEVEL_MAINTAINER));
            regHandler.block(Names.BLOCK_DUAL_LEVEL_MAINTAINER, new BlockDualLevelMaintainer());
        }
    }
}
