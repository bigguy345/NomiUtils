package com.goat.goatae2.network;

import com.goat.goatae2.GOATAE2;
import com.goat.goatae2.network.packet.JEIGhostSlotPacket;
import com.goat.goatae2.network.packet.OpenCraftingGUI;
import com.goat.goatae2.network.packet.SyncMaintainerGUIPacket;
import com.goat.goatae2.network.packet.UpdateMaintainerPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Hashtable;
import java.util.Map;

public final class PacketHandler {
    public static PacketHandler Instance;

    public Map<String, AbstractPacket> map = new Hashtable<>();
    public Map<String, FMLEventChannel> channels = new Hashtable<>();

    public PacketHandler() {

        map.put(UpdateMaintainerPacket.packetName, new UpdateMaintainerPacket());
        map.put(SyncMaintainerGUIPacket.packetName, new SyncMaintainerGUIPacket());
        map.put(JEIGhostSlotPacket.packetName, new JEIGhostSlotPacket());
        map.put(OpenCraftingGUI.packetName, new OpenCraftingGUI());

        this.register();
    }

    public void register() {
        FMLEventChannel eventChannel;
        for (String channel : map.keySet()) {
            eventChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(channel);
            eventChannel.register(this);
            channels.put(channel, eventChannel);
        }
    }

    @SubscribeEvent
    public void onServerPacket(FMLNetworkEvent.ServerCustomPacketEvent event) {
        try {
            map.get(event.getPacket().channel()).receiveData(event.getPacket().payload(), ((NetHandlerPlayServer) event.getHandler()).player);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onClientPacket(FMLNetworkEvent.ClientCustomPacketEvent event) {
        try {
            map.get(event.getPacket().channel()).receiveData(event.getPacket().payload(), GOATAE2.proxy.getClientPlayer());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendToPlayer(AbstractPacket pr, EntityPlayerMP player) {
        FMLProxyPacket packet = pr.generatePacket();
        if (packet != null && GOATAE2.side() == Side.SERVER) {
            channels.get(packet.channel()).sendTo(packet, player);
        }
    }

    public void sendToServer(AbstractPacket pr) {
        FMLProxyPacket packet = pr.generatePacket();
        if (packet != null) {
            packet.setTarget(Side.SERVER);
            channels.get(packet.channel()).sendToServer(packet);
        }
    }

    public void sendToTrackingPlayers(Entity entity, FMLProxyPacket packet) {
        if (packet != null && GOATAE2.side() == Side.SERVER) {
            // EntityTracker tracker = ((WorldServer) entity.worldObj).getEntityTracker();
            // tracker.func_151248_b(entity, packet); // Send packet to tracking players
        }
    }

    public void sendAround(Entity entity, double range, FMLProxyPacket packet) {
        if (packet != null && GOATAE2.side() == Side.SERVER) {
            channels.get(packet.channel()).sendToAllAround(packet, new NetworkRegistry.TargetPoint(entity.dimension, entity.posX, entity.posY, entity.posZ, range));
        }
    }

    public void sendToAll(FMLProxyPacket packet) {
        if (packet != null && GOATAE2.side() == Side.SERVER) {
            channels.get(packet.channel()).sendToAll(packet);
        }
    }
}
