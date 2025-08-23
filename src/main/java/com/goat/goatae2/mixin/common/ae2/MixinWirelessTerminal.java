package com.goat.goatae2.mixin.common.ae2;

import appeng.api.AEApi;
import appeng.api.implementations.tiles.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IMachineSet;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.DimensionalCoord;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.me.GridAccessException;
import appeng.me.cluster.implementations.QuantumCluster;
import appeng.tile.networking.TileWireless;
import appeng.tile.qnb.TileQuantumBridge;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

/*
 Fix random Wireless Out Of Range 
 */
@Mixin(value = WirelessTerminalGuiObject.class, remap = false)
public abstract class MixinWirelessTerminal {

    @Shadow
    private double sqRange;

    @Shadow
    private double myRange;

    @Shadow
    private IGrid targetGrid;

    @Shadow
    private IMEMonitor<IAEItemStack> itemStorage;

    @Shadow
    private IWirelessAccessPoint myWap;

    @Shadow
    private QuantumCluster myQC;

    @Shadow
    @Final
    private EntityPlayer myPlayer;

    /**
     * @author
     * @reason
     */
    @Overwrite
    public boolean rangeCheck() {
        this.sqRange = this.myRange = Double.MAX_VALUE;
        if (this.targetGrid != null && this.itemStorage != null) {
            if (this.myQC != null && this.myQC.getCenter().getProxy().isActive()) {
                myRange = 1;
                return true;
            } else if (this.myWap != null) {
                boolean bo = this.myWap.getGrid() == this.targetGrid ? this.testWap(this.myWap) : false;

                return bo;
            } else {
                IMachineSet tw = this.targetGrid.getMachines(TileWireless.class);
                this.myWap = null;
                this.myQC = null;

                for (IGridNode n : tw) {
                    IWirelessAccessPoint wap = (IWirelessAccessPoint) n.getMachine();
                    if (this.testWap(wap)) {
                        this.myWap = wap;
                    }
                }

                if (this.myWap != null) {
                    return true;
                } else {
                    for (IGridNode n : this.targetGrid.getMachines(TileQuantumBridge.class)) {
                        TileQuantumBridge tqb = (TileQuantumBridge) n.getMachine();
                        if (tqb.getCluster() != null) {
                            TileQuantumBridge center = ((QuantumCluster) tqb.getCluster()).getCenter();
                            if (center != null && center.getInternalInventory().getStackInSlot(1).isItemEqual((ItemStack) AEApi.instance().definitions().materials().cardQuantumLink().maybeStack(1).get())) {
                                this.myQC = (QuantumCluster) tqb.getCluster();
                                this.myRange = (double) 1.0F;
                                return true;
                            }
                        }
                    }

                    boolean bo = this.myWap != null || this.myQC != null;
                    return bo;
                }
            }
        } else {
            return false;
        }
    }

    private boolean testWap(IWirelessAccessPoint wap) {
        double rangeLimit = wap.getRange();
        rangeLimit *= rangeLimit;
        DimensionalCoord dc = wap.getLocation();
        if (dc.getWorld() == this.myPlayer.world) {
            double offX = (double) dc.x - this.myPlayer.posX;
            double offY = (double) dc.y - this.myPlayer.posY;
            double offZ = (double) dc.z - this.myPlayer.posZ;
            double r = offX * offX + offY * offY + offZ * offZ;
            if (r < rangeLimit && this.sqRange > r && wap.isActive()) {
                this.sqRange = r;
                this.myRange = Math.sqrt(r);
                return true;
            }
        }

        return false;
    }
}
