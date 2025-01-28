package com.goat.goatae2.network.packet;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.security.IActionHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerOpenContext;
import appeng.container.implementations.ContainerCraftConfirm;
import appeng.core.AppEng;
import appeng.core.sync.GuiBridge;
import appeng.me.helpers.PlayerSource;
import appeng.util.item.AEItemStack;
import com.goat.goatae2.GOATAE2;
import com.goat.goatae2.Utility;
import com.goat.goatae2.constants.GuiTypes;
import com.goat.goatae2.container.ContainerLevelMaintainer;
import com.goat.goatae2.network.AbstractPacket;
import com.goat.goatae2.tile.TileLevelMaintainer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.io.IOException;
import java.util.concurrent.Future;

public class OpenCraftingGUI extends AbstractPacket {
    public static final String packetName = "OpenCraftingGUI";
    private Type type;

    public static enum Type {
        LEVEL_MAINTAINER, CRAFTING_STATUS, CRAFTING_CONFIRM
    }

    public OpenCraftingGUI() {
    }

    private NBTTagCompound data;

    public OpenCraftingGUI(Type type, NBTTagCompound data) {
        this.data = data;
        this.type = type;
    }

    @Override
    public String getChannel() {
        return packetName;
    }

    @Override
    public void sendData(ByteBuf buf) throws IOException {
        buf.writeInt(type.ordinal());
        buf.writeBoolean(data == null);
        if (data != null)
            ByteBufUtils.writeTag(buf, data);
    }

    @Override
    public void receiveData(ByteBuf buf, EntityPlayer p) throws IOException {
        type = Type.values()[buf.readInt()];
        boolean dataNull = buf.readBoolean();
        if (!dataNull)
            data = ByteBufUtils.readTag(buf);

        if (type == Type.LEVEL_MAINTAINER) {
            Object target = ((AEBaseContainer) p.openContainer).getTarget();
            if (target instanceof TileLevelMaintainer) {
                TileLevelMaintainer tile = (TileLevelMaintainer) target;
                BlockPos pos = tile.getPos();
                p.openGui(GOATAE2.INSTANCE, GuiTypes.LEVEL_MAINTAINER_ID, p.getEntityWorld(), pos.getX(), pos.getY(), pos.getZ());
            }
        } else if (type == Type.CRAFTING_STATUS) {
            openGui(GuiBridge.GUI_CRAFTING_STATUS, p, ((ContainerLevelMaintainer) p.openContainer).getTile().getPos());
        } else if (type == Type.CRAFTING_CONFIRM) {
            AEBaseContainer baseContainer = (AEBaseContainer) p.openContainer;
            ContainerOpenContext context = baseContainer.getOpenContext();
            if (context != null) {
                TileLevelMaintainer tile = (TileLevelMaintainer) context.getTile();
                int slotId = data.getInteger("slotId");
                tile.tempClickedSlot = slotId;
                
                IAEItemStack target = Utility.getCorrectCraftingItem(AEItemStack.fromNBT(data));
                target.setStackSize(tile.config.batchSizes[slotId]);

                IGrid g = ((IActionHost) tile).getActionableNode().getGrid();
                if (g == null || target == null) {
                    return;
                }
                
                try {
                    ICraftingGrid cg = tile.getProxy().getCrafting();
                    Future<ICraftingJob> futureJob = cg.beginCraftingJob(tile.getWorld(), g, new PlayerSource(p, tile), target, null);

                    openGui(GuiBridge.GUI_CRAFTING_CONFIRM, p, tile.getPos());
                    if (p.openContainer instanceof ContainerCraftConfirm) {
                        ContainerCraftConfirm ccc = (ContainerCraftConfirm) p.openContainer;
                        boolean heldShift = data.getBoolean("shiftDown");
                        ccc.setAutoStart(heldShift);
                        ccc.setJob(futureJob);
                    }
                } catch (Throwable var14) {

                }
            }
        }
    }

    private void openGui(GuiBridge type, EntityPlayer p, BlockPos pos) {
        p.openGui(AppEng.instance(), type.ordinal() << 4 | 8, p.getEntityWorld(), pos.getX(), pos.getY(), pos.getZ());
    }
}
