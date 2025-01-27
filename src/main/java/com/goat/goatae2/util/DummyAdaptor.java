package com.goat.goatae2.util;

import appeng.api.config.FuzzyMode;
import appeng.util.InventoryAdaptor;
import appeng.util.inv.IInventoryDestination;
import appeng.util.inv.ItemSlot;
import java.util.Iterator;
import net.minecraft.item.ItemStack;

public class DummyAdaptor extends InventoryAdaptor {
    public static final DummyAdaptor INSTANCE = new DummyAdaptor();

    public DummyAdaptor() {
    }

    public ItemStack removeItems(int i, ItemStack itemStack, IInventoryDestination iInventoryDestination) {
        return itemStack;
    }

    public ItemStack simulateRemove(int i, ItemStack itemStack, IInventoryDestination iInventoryDestination) {
        return itemStack;
    }

    public ItemStack removeSimilarItems(int i, ItemStack itemStack, FuzzyMode fuzzyMode, IInventoryDestination iInventoryDestination) {
        return itemStack;
    }

    public ItemStack simulateSimilarRemove(int i, ItemStack itemStack, FuzzyMode fuzzyMode, IInventoryDestination iInventoryDestination) {
        return itemStack;
    }

    public ItemStack addItems(ItemStack itemStack) {
        return ItemStack.EMPTY;
    }

    public ItemStack simulateAdd(ItemStack itemStack) {
        return ItemStack.EMPTY;
    }

    public boolean containsItems() {
        return true;
    }

    public boolean hasSlots() {
        return false;
    }

    public Iterator<ItemSlot> iterator() {
        return null;
    }
}