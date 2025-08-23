package com.goat.goatae2;

import com.goat.goatae2.network.PacketHandler;
import com.goat.goatae2.proxy.CommonProxy;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = GOATAE2.MODID, name = GOATAE2.NAME, version = GOATAE2.VERSION, dependencies = "required-after:appliedenergistics2")
public class GOATAE2 {
    public static final String MODID = "goatae2";
    public static final String NAME = "goat's ae2";
    public static final String VERSION = "1.0.0";

    @Mod.Instance(MODID)
    public static GOATAE2 INSTANCE;

    public static final Logger LOGGER = LogManager.getLogger(MODID);

    @SidedProxy(clientSide = "com.goat.goatae2.proxy.ClientProxy", serverSide = "com.goat.goatae2.proxy.CommonProxy")
    public static CommonProxy proxy;

    public static boolean AE2FC_LOADED = Loader.isModLoaded("ae2fc");
    public static boolean GT_LOADED = Loader.isModLoaded("gregtech");

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent preinit) {
        MinecraftForge.EVENT_BUS.register(this);
        proxy.preInit(preinit);

        if (GT_LOADED)
            GregTechTiles.init();
        
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);

        if (GT_LOADED)
            GregTechTiles.recipeInit();
        
    }

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
        PacketHandler.Instance = new PacketHandler();
    }

    public static ResourceLocation resource(String path) {
        return new ResourceLocation(MODID, path);
    }

    public static Side side() {
        return FMLCommonHandler.instance().getEffectiveSide();
    }
}