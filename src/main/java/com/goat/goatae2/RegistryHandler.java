package com.goat.goatae2;

import appeng.block.AEBaseItemBlock;
import appeng.block.AEBaseTileBlock;
import appeng.core.features.ActivityState;
import appeng.core.features.BlockStackSrc;
import appeng.tile.AEBaseTile;
import com.goat.goatae2.block.Blocks;
import com.goat.goatae2.block.Items;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

import static com.goat.goatae2.GOATAE2.AE2FC_LOADED;

public class RegistryHandler {

    protected final List<Pair<String, Block>> blocks = new ArrayList<>();
    protected final List<Pair<String, Item>> items = new ArrayList<>();

    public void block(String name, Block block) {
        blocks.add(Pair.of(name, block));
    }

    public void item(String name, Item item) {
        items.add(Pair.of(name, item));
    }

    @SubscribeEvent
    public void onRegisterBlocks(RegistryEvent.Register<Block> event) {
        for (Pair<String, Block> entry : blocks) {
            String key = entry.getLeft();
            Block block = entry.getRight();
            block.setRegistryName(key);
            block.setTranslationKey(GOATAE2.MODID + "." + key);
            block.setCreativeTab(Items.GAE2_TAB);
            event.getRegistry().register(block);
        }
    }

    @SubscribeEvent
    public void onRegisterItems(RegistryEvent.Register<Item> event) {
        // TODO some way to handle blocks with custom ItemBlock
        for (Pair<String, Block> entry : blocks) {
            event.getRegistry().register(initItem(entry.getLeft(), new AEBaseItemBlock(entry.getRight())));
        }
        for (Pair<String, Item> entry : items) {
            event.getRegistry().register(initItem(entry.getLeft(), entry.getRight()));
        }
    }

    @SubscribeEvent
    public void onRegisterItemModels(ModelRegistryEvent ev) {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(Blocks.LEVEL_MAINTAINER), 0, new ModelResourceLocation(Blocks.LEVEL_MAINTAINER.getRegistryName(), "inventory"));
        if (AE2FC_LOADED) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(Blocks.FLUID_LEVEL_MAINTAINER), 0, new ModelResourceLocation(Blocks.FLUID_LEVEL_MAINTAINER.getRegistryName(), "inventory"));
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(Blocks.DUAL_LEVEL_MAINTAINER), 0, new ModelResourceLocation(Blocks.DUAL_LEVEL_MAINTAINER.getRegistryName(), "inventory"));
        }
    }

    private static Item initItem(String key, Item item) {
        item.setRegistryName(key);
        item.setTranslationKey(GOATAE2.MODID + "." + key);
        item.setCreativeTab(Items.GAE2_TAB);
        return item;
    }

    public void onInit() {
        for (Pair<String, Block> entry : blocks) {
            // respects registry overrides, i guess
            Block block = ForgeRegistries.BLOCKS.getValue(GOATAE2.resource(entry.getKey()));
            if (block instanceof AEBaseTileBlock) {
                AEBaseTile.registerTileItem(((AEBaseTileBlock) block).getTileEntityClass(), new BlockStackSrc(block, 0, ActivityState.Enabled));
            }
        }
    }
}