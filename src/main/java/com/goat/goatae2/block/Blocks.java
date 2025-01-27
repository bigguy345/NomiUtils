package com.goat.goatae2.block;

import com.goat.goatae2.GOATAE2;
import com.goat.goatae2.RegistryHandler;
import com.goat.goatae2.constants.Names;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class Blocks {

    @GameRegistry.ObjectHolder(GOATAE2.MODID + ":" + Names.BLOCK_LEVEL_MAINTAINER)
    public static BlockLevelMaintainer LEVEL_MAINTAINER;

    public static void init(RegistryHandler regHandler) {
        regHandler.block(Names.BLOCK_LEVEL_MAINTAINER, new BlockLevelMaintainer());
    }
}
