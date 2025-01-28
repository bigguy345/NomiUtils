package com.goat.goatae2.network.packet;

import com.goat.goatae2.client.gui.impl.GuiLevelMaintainer;
import com.goat.goatae2.network.AbstractPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import java.io.IOException;

public class SyncMaintainerGUIPacket extends AbstractPacket {
    public static final String packetName = "MaintainerSync";

    public static enum Type {
        TOGGLE_BOXES, CLEAR_SLOT, CLEAR_ALL_SLOTS, UPDATE_STACKSIZE
    }

    private int index;
    private long value;

    private Type type;

    public SyncMaintainerGUIPacket() {
    }

    public SyncMaintainerGUIPacket(int id, long value, Type type) {
        this.index = id;
        this.value = value;
        this.type = type;
    }

    @Override
    public String getChannel() {
        return packetName;
    }

    @Override
    public void sendData(ByteBuf buf) throws IOException {
        buf.writeInt(index);
        buf.writeLong(value);
        buf.writeInt(type.ordinal());
    }

    @Override
    public void receiveData(ByteBuf buf, EntityPlayer player) throws IOException {
        if (Minecraft.getMinecraft().currentScreen instanceof GuiLevelMaintainer)
            ((GuiLevelMaintainer) Minecraft.getMinecraft().currentScreen).sync(buf);
    }
}
