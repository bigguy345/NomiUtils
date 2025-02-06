package com.goat.goatae2.client.gui;

import appeng.container.AEBaseContainer;
import appeng.container.ContainerOpenContext;
import com.goat.goatae2.client.gui.impl.GuiDualLevelMaintainer;
import com.goat.goatae2.client.gui.impl.GuiLevelMaintainer;
import com.goat.goatae2.constants.GuiTypes;
import com.goat.goatae2.container.ContainerLevelMaintainer;
import com.goat.goatae2.container.slot.ContainerDualLevelMaintainer;
import com.goat.goatae2.tile.TileLevelMaintainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nullable;

public class GuiHandler implements IGuiHandler {

    @Nullable
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(new BlockPos(x, y, z)); // Locate the TileEntity
        boolean levelMaintainer = ID == GuiTypes.DUAL_LEVEL_MAINTAINER_ID || ID == GuiTypes.FLUID_LEVEL_MAINTAINER_ID || ID == GuiTypes.LEVEL_MAINTAINER_ID;
        if (levelMaintainer) { // Match the GUI ID
            if (te instanceof TileLevelMaintainer) { // Ensure it has an inventory
                TileLevelMaintainer tile = (TileLevelMaintainer) te;
                ContainerLevelMaintainer cont = null;
                if (ID == GuiTypes.DUAL_LEVEL_MAINTAINER_ID)
                    cont = new ContainerDualLevelMaintainer(player.inventory, tile);
                else
                    cont = new ContainerLevelMaintainer(player.inventory, tile, ID == GuiTypes.FLUID_LEVEL_MAINTAINER_ID);

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
        boolean levelMaintainer = ID == GuiTypes.DUAL_LEVEL_MAINTAINER_ID || ID == GuiTypes.FLUID_LEVEL_MAINTAINER_ID || ID == GuiTypes.LEVEL_MAINTAINER_ID;
        if (levelMaintainer) { // Match the GUI ID
            TileEntity te = world.getTileEntity(new BlockPos(x, y, z)); // Locate the TileEntity
            if (te instanceof TileLevelMaintainer) { // Ensure it has an inventory
                TileLevelMaintainer tile = (TileLevelMaintainer) te;
                if (ID == GuiTypes.DUAL_LEVEL_MAINTAINER_ID)
                    return new GuiDualLevelMaintainer(player.inventory, tile);
                else
                    return new GuiLevelMaintainer(player.inventory, tile, ID == GuiTypes.FLUID_LEVEL_MAINTAINER_ID);
            }
        }
        return null;
    }
}
