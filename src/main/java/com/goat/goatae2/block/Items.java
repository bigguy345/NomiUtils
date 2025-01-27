package com.goat.goatae2.block;

import com.goat.goatae2.GOATAE2;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class Items {

    public static final CreativeTabs GAE2_TAB = new CreativeTabs(GOATAE2.MODID) {
        @Nonnull
        @Override
        public ItemStack createIcon() {
            return new ItemStack(Blocks.DIAMOND_BLOCK);
        }
    };
}
