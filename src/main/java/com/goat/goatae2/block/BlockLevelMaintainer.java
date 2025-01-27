package com.goat.goatae2.block;

import appeng.block.AEBaseTileBlock;
import com.goat.goatae2.GOATAE2;
import com.goat.goatae2.constants.GuiTypes;
import com.goat.goatae2.tile.TileLevelMaintainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class BlockLevelMaintainer extends AEBaseTileBlock {

    public static final PropertyDirection facingProperty = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

    public BlockLevelMaintainer() {
        super(Material.IRON);
        setTileEntity(TileLevelMaintainer.class);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (player.isSneaking()) {
            return super.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ);
        }
        TileLevelMaintainer tile = getTileEntity(world, pos);
        if (tile != null) {
            if (!world.isRemote) {
                tile.markForUpdate();
                player.openGui(GOATAE2.INSTANCE, GuiTypes.LEVEL_MAINTAINER_ID, world, pos.getX(), pos.getY(), pos.getZ());
            }
            return true;
        }

        return super.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty[]{facingProperty}, new IUnlistedProperty[]{});
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof TileLevelMaintainer) {
            //            if (((TileFluidLevelMaintainer) tileEntity).facing != null)
            //            {
            //                return state.withProperty(facingProperty,((TileFluidLevelMaintainer) tileEntity).facing);
            //            }
        }
        return state;
    }

    @Override
    public void onBlockPlacedBy(World w, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack is) {
        super.onBlockPlacedBy(w, pos, state, placer, is);
        TileEntity tileEntity = w.getTileEntity(pos);
        if (tileEntity instanceof TileLevelMaintainer) {
            // ((TileFluidLevelMaintainer) tileEntity).facing = placer.getHorizontalFacing().getOpposite();
        }
    }

    @Override
    public boolean rotateBlock(World w, BlockPos pos, EnumFacing axis) {
        //        FluidCraft.log.log(Level.INFO,axis.getOpposite());
        TileEntity tileEntity = w.getTileEntity(pos);
        if (tileEntity instanceof TileLevelMaintainer) {
            // EnumFacing facing = ((TileFluidLevelMaintainer) tileEntity).facing;
            //  ((TileFluidLevelMaintainer) tileEntity).facing = facing.rotateY();
            //  w.setBlockState(pos,this.blockState.getBaseState().withProperty(facingProperty,facing.rotateY()));
            return true;
        }
        return super.rotateBlock(w, pos, axis);
    }
}
