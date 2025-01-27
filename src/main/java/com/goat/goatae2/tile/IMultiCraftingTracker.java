package com.goat.goatae2.tile;

import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingLink;

import java.util.HashMap;
import java.util.concurrent.Future;

public interface IMultiCraftingTracker {

    Future<ICraftingJob>[] getJobs();
    
    int getJobId(Future<ICraftingJob> job);

    HashMap<Integer, ICraftingJob> getActiveJobsMap();

    int getLastSlot();
    
    int getLinkSlot(ICraftingLink link);
}
