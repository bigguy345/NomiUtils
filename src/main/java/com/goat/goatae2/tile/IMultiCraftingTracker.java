package com.goat.goatae2.tile;

import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingLink;

import java.util.HashMap;

public interface IMultiCraftingTracker {

    HashMap<Integer, ICraftingJob> getActiveJobsMap();

    int getLastSlot();

    void setLinkP(int slot, ICraftingLink l);

    int getLinkSlot(ICraftingLink link);
}
