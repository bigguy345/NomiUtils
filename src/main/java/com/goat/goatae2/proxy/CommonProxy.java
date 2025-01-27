package com.goat.goatae2.proxy;

import appeng.api.config.Upgrades;
import appeng.api.definitions.IItemDefinition;
import appeng.core.features.ItemDefinition;

import com.goat.goatae2.GOATAE2;
import com.goat.goatae2.RegistryHandler;
import com.goat.goatae2.block.Blocks;
import com.goat.goatae2.client.gui.GuiHandler;
import com.goat.goatae2.constants.Names;
import com.goat.goatae2.tile.TileLevelMaintainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.Objects;

public class CommonProxy {

    public final RegistryHandler regHandler = createRegistryHandler();

    public RegistryHandler createRegistryHandler() {
        return new RegistryHandler();
    }

    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(regHandler);
        Blocks.init(regHandler);

        GameRegistry.registerTileEntity(TileLevelMaintainer.class, GOATAE2.resource(Names.BLOCK_LEVEL_MAINTAINER));
        NetworkRegistry.INSTANCE.registerGuiHandler(GOATAE2.INSTANCE, new GuiHandler());

    }

    public void init(FMLInitializationEvent event) {
        regHandler.onInit();
    }

    public void postInit(FMLPostInitializationEvent event) {
        Upgrades.FUZZY.registerItem(new ItemStack(Blocks.LEVEL_MAINTAINER), 1);

    }

    private static IItemDefinition createItemDefn(Item item) {
        return new ItemDefinition(Objects.requireNonNull(item.getRegistryName()).toString(), item);
    }
    public EntityPlayer getClientPlayer() {
        return null;
    }
}