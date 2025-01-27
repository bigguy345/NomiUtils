package com.goat.goatae2.network.packet;

import appeng.api.config.Settings;
import appeng.api.util.IConfigManager;
import appeng.util.Platform;
import com.goat.goatae2.container.ContainerLevelMaintainer;
import com.goat.goatae2.network.AbstractPacket;
import com.goat.goatae2.tile.TileLevelMaintainer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

import java.io.IOException;

public class UpdateMaintainerPacket extends AbstractPacket {
    public static final String packetName = "UpdateMaintainer";

    public static enum Type {
        SET_THRESHOLD, SET_BATCH_SIZE, GUI_SYNC, RUN, CLEAR_ALL, SET_FUZZY
    }

    private int index;
    private int amount;
    private Type type;

    public UpdateMaintainerPacket() {
    }

    public UpdateMaintainerPacket(int id, int value, UpdateMaintainerPacket.Type type) {
        this.index = id;
        this.amount = value;
        this.type = type;
    }

    @Override
    public String getChannel() {
        return packetName;
    }

    @Override
    public void sendData(ByteBuf buf) throws IOException {
        buf.writeInt(index);
        buf.writeInt(amount);
        buf.writeInt(type.ordinal());
    }

    @Override
    public void receiveData(ByteBuf buf, EntityPlayer player) throws IOException {
        index = buf.readInt();
        amount = buf.readInt();
        type = Type.values()[buf.readInt()];


        if (player.openContainer instanceof ContainerLevelMaintainer) {
            ContainerLevelMaintainer container = (ContainerLevelMaintainer) player.openContainer;
            TileLevelMaintainer tile = container.getTile();

            if (type == Type.SET_THRESHOLD)
                tile.config.setThreshold(index, amount);
            else if (type == Type.SET_BATCH_SIZE)
                tile.config.setBatchSize(index, amount);
            else if (type == Type.RUN)
                tile.doCraftCycle();
            else if (type == Type.CLEAR_ALL)
                container.clear(player);
            else if (type == Type.SET_FUZZY) {
                IConfigManager cm = tile.getConfigManager();
                Enum<?> newState = Platform.rotateEnum(cm.getSetting(Settings.FUZZY_MODE), amount == 1 ? true : false, Settings.FUZZY_MODE.getPossibleValues());
                cm.putSetting(Settings.FUZZY_MODE, newState);
                tile.config.updateSystemStackSizes();
            }
        }
    }
}
