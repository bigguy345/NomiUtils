package com.goat.goatae2.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

import java.io.IOException;

public abstract class AbstractPacket {

    public final FMLProxyPacket generatePacket() {
        ByteBuf buf = Unpooled.buffer();
        try {
            sendData(buf);
            return new FMLProxyPacket(new PacketBuffer(buf), getChannel());
        } catch (Exception ignored) {
        }
        return null;
    }

    public abstract String getChannel();

    public abstract void sendData(ByteBuf out) throws IOException;

    //"player" on the server side is the client who sent this packet
    //"player" on the client side is the client player
    public abstract void receiveData(ByteBuf in, EntityPlayer player) throws IOException;
}
