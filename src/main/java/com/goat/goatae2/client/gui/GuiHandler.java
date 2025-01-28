package com.goat.goatae2.client.gui;

import appeng.container.AEBaseContainer;
import appeng.container.ContainerOpenContext;
import com.goat.goatae2.client.gui.impl.GuiLevelMaintainer;
import com.goat.goatae2.constants.GuiTypes;
import com.goat.goatae2.container.ContainerLevelMaintainer;
import com.goat.goatae2.tile.TileLevelMaintainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nullable;

public class GuiHandler implements IGuiHandler {
    public static final int LEVEL_MAINTAINER_ID = 1;

    @Nullable
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == GuiTypes.LEVEL_MAINTAINER_ID) { // Match the GUI ID
            TileEntity te = world.getTileEntity(new BlockPos(x, y, z)); // Locate the TileEntity
            if (te instanceof TileLevelMaintainer) { // Ensure it has an inventory
                TileLevelMaintainer tile = (TileLevelMaintainer) te;
                ContainerLevelMaintainer cont = new ContainerLevelMaintainer(player.inventory, tile);
                AEBaseContainer bc = cont;
                bc.setOpenContext(new ContainerOpenContext(tile));
                bc.getOpenContext().setWorld(world);
                bc.getOpenContext().setX(x);
                bc.getOpenContext().setY(y);
                bc.getOpenContext().setZ(z);
                return cont;
            }
        }
        return null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == GuiTypes.LEVEL_MAINTAINER_ID) { // Match the GUI ID
            TileEntity te = world.getTileEntity(new BlockPos(x, y, z)); // Locate the TileEntity
            if (te instanceof TileLevelMaintainer) { // Ensure it has an inventory
                TileLevelMaintainer tile = (TileLevelMaintainer) te;
                return new GuiLevelMaintainer(player.inventory, tile);
            }
        }
        return null;
    }
}
