package com.goat.goatae2.network.packet;

import appeng.api.storage.data.IAEItemStack;
import appeng.helpers.InventoryAction;
import appeng.util.item.AEItemStack;
import com.goat.goatae2.container.ContainerLevelMaintainer;
import com.goat.goatae2.network.AbstractPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import java.io.IOException;

public class JEIGhostSlotPacket extends AbstractPacket {
    public static final String packetName = "JEIGhostSlotPacket";

    @Override
    public String getChannel() {
        return packetName;
    }

    private int slot;
    private int amount;
    IAEItemStack item;

    public JEIGhostSlotPacket() {
    }

    public JEIGhostSlotPacket(int id, int value, IAEItemStack item) {
        this.slot = id;
        this.amount = value;
        this.item = item;
    }

    @Override
    public void sendData(ByteBuf buf) throws IOException {
        buf.writeInt(slot);
        buf.writeInt(amount);
        item.writeToPacket(buf);
    }

    @Override
    public void receiveData(ByteBuf buf, EntityPlayer player) throws IOException {
        slot = buf.readInt();
        amount = buf.readInt();
        item = AEItemStack.fromPacket(buf);

        if (player.openContainer instanceof ContainerLevelMaintainer) {
            ContainerLevelMaintainer container = (ContainerLevelMaintainer) player.openContainer;
            ItemStack hand = player.inventory.getItemStack();
            player.inventory.setItemStack(item.getDefinition());
            container.doAction((EntityPlayerMP) player, InventoryAction.PICKUP_OR_SET_DOWN, slot, 0);
            player.inventory.setItemStack(hand);
        }
    }
}
