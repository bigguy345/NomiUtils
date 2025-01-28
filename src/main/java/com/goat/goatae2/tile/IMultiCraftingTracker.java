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
    

    void setJobP(int slot, Future<ICraftingJob> l);

    void setLinkP(int slot, ICraftingLink l);

    int getLinkSlot(ICraftingLink link);
}
