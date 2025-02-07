package com.goat.goatae2.block;

import appeng.block.AEBaseTileBlock;
import com.goat.goatae2.GOATAE2;
import com.goat.goatae2.constants.GuiTypes;
import com.goat.goatae2.tile.TileLevelMaintainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
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

public class BlockLevelMaintainer extends AEBaseTileBlock {

    //  public static final PropertyDirection facingProperty = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
    public static PropertyBool ONLINE = PropertyBool.create("online");

    public BlockLevelMaintainer(Material mat) {
        super(mat);
        this.setDefaultState(getDefaultState().withProperty(ONLINE, Boolean.valueOf(false)));
    }

    public BlockLevelMaintainer() {
        this(Material.IRON);
        setTileEntity(TileLevelMaintainer.class);
    }

    @Override
    protected IProperty[] getAEStates() {
        return new IProperty[]{ONLINE};
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
                player.openGui(GOATAE2.INSTANCE, getGuiId(), world, pos.getX(), pos.getY(), pos.getZ());
            }
            return true;
        }

        return super.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ);
    }

    public int getGuiId() {
        return GuiTypes.LEVEL_MAINTAINER_ID;
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        TileLevelMaintainer te = this.getTileEntity(worldIn, pos);
        boolean powered = te != null && te.isActive();
        return super.getActualState(state, worldIn, pos).withProperty(ONLINE, powered);
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
