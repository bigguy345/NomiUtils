package com.goat.goatae2.network.packet;

import appeng.container.implementations.ContainerCraftingStatus;
import appeng.core.AppEng;
import appeng.core.sync.GuiBridge;
import com.goat.goatae2.GOATAE2;
import com.goat.goatae2.constants.GuiTypes;
import com.goat.goatae2.container.ContainerLevelMaintainer;
import com.goat.goatae2.network.AbstractPacket;
import com.goat.goatae2.tile.TileLevelMaintainer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;

public class OpenCraftingGUI extends AbstractPacket {
    public static final String packetName = "OpenCraftingGUI";

    public OpenCraftingGUI() {
    }

    private int gui;

    public OpenCraftingGUI(int gui) {
        this.gui = gui;
    }

    @Override
    public String getChannel() {
        return packetName;
    }

    @Override
    public void sendData(ByteBuf buf) throws IOException {
        buf.writeInt(gui);
    }

    @Override
    public void receiveData(ByteBuf buf, EntityPlayer p) throws IOException {
        int gui = buf.readInt();
        if (gui == 1) {
            if (p.openContainer instanceof ContainerLevelMaintainer)
                openCraftingStatusGUI(p, ((ContainerLevelMaintainer) p.openContainer).getTile().getPos());
        } else if (gui == 2) {
            Object target = ((ContainerCraftingStatus) p.openContainer).getTarget();
            if (target instanceof TileLevelMaintainer) {
                TileLevelMaintainer tile = (TileLevelMaintainer) target;
                BlockPos pos = tile.getPos();
                p.openGui(GOATAE2.INSTANCE, GuiTypes.LEVEL_MAINTAINER_ID, p.getEntityWorld(), pos.getX(), pos.getY(), pos.getZ());
            }
        }
    }

    private void openCraftingStatusGUI(EntityPlayer p, BlockPos pos) {
        p.openGui(AppEng.instance(), GuiBridge.GUI_CRAFTING_STATUS.ordinal() << 4 | 8, p.getEntityWorld(), pos.getX(), pos.getY(), pos.getZ());
    }
}
