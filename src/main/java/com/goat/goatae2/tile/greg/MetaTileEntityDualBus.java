package com.goat.goatae2.tile.greg;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.goat.goatae2.GregTechTiles;
import gregtech.api.capability.*;
import gregtech.api.capability.impl.*;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.GhostCircuitSlotWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.util.GTHashMaps;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockNotifiablePart;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MetaTileEntityDualBus extends MetaTileEntityMultiblockNotifiablePart implements IMultiblockAbilityPart<IItemHandlerModifiable>, IControllable, IGhostSlotConfigurable {
    protected @Nullable GhostCircuitItemStackHandler circuitInventory;
    private IItemHandlerModifiable actualImportItems;
    private boolean workingEnabled = true;
    private boolean autoCollapse;

    private final int numSlots;
    private final int tankSize;
    private final FluidTankList fluidTankList;

    public MetaTileEntityDualBus(ResourceLocation metaTileEntityId, int tier, int numSlots, boolean isExportHatch) {
        super(metaTileEntityId, tier, isExportHatch);
        this.initializeInventory();

        this.numSlots = numSlots;
        this.tankSize = 8000 * (1 << Math.min(9, tier)) / (this.numSlots == 4 ? 4 : 8);
        FluidTank[] fluidsHandlers = new FluidTank[this.numSlots];

        for (int i = 0; i < fluidsHandlers.length; ++i) {
            fluidsHandlers[i] = new NotifiableFluidTank(this.tankSize, this, isExportHatch);
        }

        this.fluidTankList = new FluidTankList(false, fluidsHandlers);
        this.initializeInventory();
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityDualBus(this.metaTileEntityId, this.getTier(), this.numSlots, this.isExportHatch);
    }

    protected void initializeInventory() {
        if (this.fluidTankList == null)
            return;

        super.initializeInventory();

        if (this.hasGhostCircuitInventory()) {
            this.circuitInventory = new GhostCircuitItemStackHandler(this);
            this.circuitInventory.addNotifiableMetaTileEntity(this);
            this.actualImportItems = new ItemHandlerList(Arrays.asList(super.getImportItems(), this.circuitInventory));
        } else {
            this.actualImportItems = null;
        }
    }

    public IItemHandlerModifiable getImportItems() {
        return this.actualImportItems == null ? super.getImportItems() : this.actualImportItems;
    }

    public void addToMultiBlock(MultiblockControllerBase controllerBase) {
        super.addToMultiBlock(controllerBase);
        if (this.hasGhostCircuitInventory() && this.actualImportItems instanceof ItemHandlerList) {
            for (IItemHandler handler : ((ItemHandlerList) this.actualImportItems).getBackingHandlers()) {
                if (handler instanceof INotifiableHandler) {
                    INotifiableHandler notifiable = (INotifiableHandler) handler;
                    notifiable.addNotifiableMetaTileEntity(controllerBase);
                    notifiable.addToNotifiedList(this, handler, this.isExportHatch);
                }
            }
        }

        registerFluidAbility(getAbilityList(controllerBase, MultiblockAbility.IMPORT_FLUIDS));
    }

    public void removeFromMultiBlock(MultiblockControllerBase controllerBase) {
        super.removeFromMultiBlock(controllerBase);
        if (this.hasGhostCircuitInventory() && this.actualImportItems instanceof ItemHandlerList) {
            for (IItemHandler handler : ((ItemHandlerList) this.actualImportItems).getBackingHandlers()) {
                if (handler instanceof INotifiableHandler) {
                    INotifiableHandler notifiable = (INotifiableHandler) handler;
                    notifiable.removeNotifiableMetaTileEntity(controllerBase);
                }
            }
        }
    }

    public void update() {
        super.update();
        if (!this.getWorld().isRemote && this.getOffsetTimer() % 5L == 0L) {
            MultiblockControllerBase cont = getController();
            if (this.workingEnabled) {
                if (this.isExportHatch) {
                    this.pushItemsIntoNearbyHandlers(new EnumFacing[]{this.getFrontFacing()});
                } else {
                    this.pullItemsFromNearbyHandlers(new EnumFacing[]{this.getFrontFacing()});
                    this.pullFluidsFromNearbyHandlers(new EnumFacing[]{this.getFrontFacing()});
                }
            }

            if (this.isAutoCollapse()) {
                IItemHandlerModifiable inventory = this.isExportHatch ? this.getExportItems() : super.getImportItems();
                if (this.isExportHatch) {
                    if (!this.getNotifiedItemOutputList().contains(inventory)) {
                        return;
                    }
                } else if (!this.getNotifiedItemInputList().contains(inventory)) {
                    return;
                }

                collapseInventorySlotContents(inventory);
            }
        }
    }

    public void setWorkingEnabled(boolean workingEnabled) {
        this.workingEnabled = workingEnabled;
        World world = this.getWorld();
        if (world != null && !world.isRemote) {
            this.writeCustomData(GregtechDataCodes.WORKING_ENABLED, (buf) -> buf.writeBoolean(workingEnabled));
        }
    }

    public boolean isWorkingEnabled() {
        return this.workingEnabled;
    }

    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        return (T) (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE ? GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this) : super.getCapability(capability, side));
    }

    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (this.shouldRenderOverlay()) {
            SimpleOverlayRenderer renderer = this.isExportHatch ? Textures.PIPE_OUT_OVERLAY : Textures.PIPE_IN_OVERLAY;
            renderer.renderSided(this.getFrontFacing(), renderState, translation, pipeline);
            SimpleOverlayRenderer overlay = this.isExportHatch ? Textures.ITEM_HATCH_OUTPUT_OVERLAY : GregTechTiles.DUAL_INPUT_OVERLAY;
            overlay.renderSided(this.getFrontFacing(), renderState, translation, pipeline);
        }
    }

    private int getInventorySize() {
        int tier = 3;
        int sizeRoot = 1 + Math.min(9, tier);
        return sizeRoot * sizeRoot;
    }

    protected IItemHandlerModifiable createExportItemHandler() {
        return (IItemHandlerModifiable) (this.isExportHatch ? new NotifiableItemStackHandler(this, this.getInventorySize(), this.getController(), true) : new GTItemStackHandler(this, 0));
    }

    protected IItemHandlerModifiable createImportItemHandler() {
        return (IItemHandlerModifiable) (this.isExportHatch ? new GTItemStackHandler(this, 0) : new NotifiableItemStackHandler(this, this.getInventorySize(), this.getController(), false));
    }

    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(this.workingEnabled);
        buf.writeBoolean(this.autoCollapse);
    }

    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.workingEnabled = buf.readBoolean();
        this.autoCollapse = buf.readBoolean();
    }

    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("workingEnabled", this.workingEnabled);
        data.setBoolean("autoCollapse", this.autoCollapse);
        if (this.circuitInventory != null && !this.isExportHatch) {
            this.circuitInventory.write(data);
        }

        return data;
    }

    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.hasKey("workingEnabled")) {
            this.workingEnabled = data.getBoolean("workingEnabled");
        }

        if (data.hasKey("autoCollapse")) {
            this.autoCollapse = data.getBoolean("autoCollapse");
        }

        if (this.circuitInventory != null && !this.isExportHatch) {
            this.circuitInventory.read(data);
        }
    }

    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.TOGGLE_COLLAPSE_ITEMS) {
            this.autoCollapse = buf.readBoolean();
        } else if (dataId == GregtechDataCodes.WORKING_ENABLED) {
            this.workingEnabled = buf.readBoolean();
        }
    }

    protected ModularUI createUI(EntityPlayer entityPlayer) {
        int rowSize = (int) Math.sqrt((double) this.getInventorySize());
        return this.createUITemplate(entityPlayer, rowSize).build(this.getHolder(), entityPlayer);
    }

    private ModularUI.Builder createUITemplate(EntityPlayer player, int gridSize) {

        int backgroundWidth = gridSize > 6 ? 176 + (gridSize - 6) * 18 : 176;
        int center = backgroundWidth / 2;
        int gridStartX = center - gridSize * 9;
        int inventoryStartX = center - 9 - 72;
        int inventoryStartY = 18 + 18 * gridSize + 12;
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, backgroundWidth, 18 + 18 * gridSize + 94).label(10, 5, this.getMetaFullName());

        for (int y = 0; y < gridSize; ++y) {
            for (int x = 0; x < gridSize; ++x) {
                int index = y * gridSize + x;
                builder.widget((new SlotWidget(this.isExportHatch ? this.exportItems : this.importItems, index, gridStartX + x * 18, 18 + y * 18, true, !this.isExportHatch)).setBackgroundTexture(new IGuiTexture[]{GuiTextures.SLOT}));
            }
        }

        for (int y = 0; y < gridSize; ++y) {
            //builder.widget(new TankWidget(fluidTank, 69, 52, 18, 18)).setAlwaysShowFull(true).setDrawHoveringText(false);
            builder.widget((new TankWidget(this.fluidTankList.getTankAt(y), gridStartX + 5 * 18, 18 + y * 18, 18, 18)).setBackgroundTexture(new IGuiTexture[]{GuiTextures.FLUID_SLOT}).setContainerClicking(true, !this.isExportHatch).setAlwaysShowFull(true));

            // builder.widget((new SlotWidget(this.isExportHatch ? this.exportItems : this.importItems, 0, gridStartX + 5 * 18, 18 + y * 18, true, !this.isExportHatch)).setBackgroundTexture(new IGuiTexture[]{GuiTextures.SLOT}));
        }

        if (this.hasGhostCircuitInventory() && this.circuitInventory != null) {
            int circuitX = gridSize > 6 ? gridStartX + gridSize * 18 + 9 : inventoryStartX + 14;
            int circuitY = gridSize * 11;
            SlotWidget circuitSlot = (new GhostCircuitSlotWidget(this.circuitInventory, 0, circuitX, circuitY)).setBackgroundTexture(new IGuiTexture[]{GuiTextures.SLOT, this.getCircuitSlotOverlay()});
            builder.widget(circuitSlot.setConsumer(this::getCircuitSlotTooltip));
        }

        return builder.bindPlayerInventory(player.inventory, GuiTextures.SLOT, inventoryStartX, inventoryStartY);
    }

    public boolean hasGhostCircuitInventory() {
        return !this.isExportHatch;
    }

    protected TextureArea getCircuitSlotOverlay() {
        return GuiTextures.INT_CIRCUIT_OVERLAY;
    }

    protected void getCircuitSlotTooltip(SlotWidget widget) {
        String configString;
        if (this.circuitInventory != null && this.circuitInventory.getCircuitValue() != -1) {
            configString = String.valueOf(this.circuitInventory.getCircuitValue());
        } else {
            configString = (new TextComponentTranslation("gregtech.gui.configurator_slot.no_value", new Object[0])).getFormattedText();
        }

        widget.setTooltipText("gregtech.gui.configurator_slot.tooltip", new Object[]{configString});
    }

    private static void collapseInventorySlotContents(IItemHandlerModifiable inventory) {
        Object2IntMap<ItemStack> inventoryContents = GTHashMaps.fromItemHandler(inventory, true);
        List<ItemStack> inventoryItemContents = new ArrayList();
        ObjectIterator var3 = inventoryContents.object2IntEntrySet().iterator();

        while (var3.hasNext()) {
            Object2IntMap.Entry<ItemStack> e = (Object2IntMap.Entry) var3.next();
            ItemStack stack = (ItemStack) e.getKey();
            int count = e.getIntValue();

            for (int maxStackSize = stack.getMaxStackSize(); count >= maxStackSize; count -= maxStackSize) {
                ItemStack copy = stack.copy();
                copy.setCount(maxStackSize);
                inventoryItemContents.add(copy);
            }

            if (count > 0) {
                ItemStack copy = stack.copy();
                copy.setCount(count);
                inventoryItemContents.add(copy);
            }
        }

        for (int i = 0; i < inventory.getSlots(); ++i) {
            ItemStack stackToMove;
            if (i >= inventoryItemContents.size()) {
                stackToMove = ItemStack.EMPTY;
            } else {
                stackToMove = (ItemStack) inventoryItemContents.get(i);
            }

            inventory.setStackInSlot(i, stackToMove);
        }
    }

    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        boolean isAttached = false;
        if (this.isAttachedToMultiBlock()) {
            this.setAutoCollapse(!this.autoCollapse);
            isAttached = true;
        }

        if (!this.getWorld().isRemote) {
            if (isAttached) {
                if (this.autoCollapse) {
                    playerIn.sendStatusMessage(new TextComponentTranslation("gregtech.bus.collapse_true", new Object[0]), true);
                } else {
                    playerIn.sendStatusMessage(new TextComponentTranslation("gregtech.bus.collapse_false", new Object[0]), true);
                }
            } else {
                playerIn.sendStatusMessage(new TextComponentTranslation("gregtech.bus.collapse.error", new Object[0]), true);
            }
        }

        return true;
    }

    public boolean isAutoCollapse() {
        return this.autoCollapse;
    }

    public void setAutoCollapse(boolean inverted) {
        this.autoCollapse = inverted;
        if (!this.getWorld().isRemote) {
            if (this.autoCollapse) {
                if (this.isExportHatch) {
                    this.addNotifiedOutput(this.getExportItems());
                } else {
                    this.addNotifiedInput(super.getImportItems());
                }
            }

            this.writeCustomData(GregtechDataCodes.TOGGLE_COLLAPSE_ITEMS, (packetBuffer) -> packetBuffer.writeBoolean(this.autoCollapse));
            this.notifyBlockUpdate();
            this.markDirty();
        }
    }

    public void setGhostCircuitConfig(int config) {
        if (this.circuitInventory != null && this.circuitInventory.getCircuitValue() != config) {
            this.circuitInventory.setCircuitValue(config);
            if (!this.getWorld().isRemote) {
                this.markDirty();
            }
        }
    }

    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        if (this.isExportHatch) {
            tooltip.add(I18n.format("gregtech.machine.item_bus.export.tooltip", new Object[0]));
        } else {
            tooltip.add(I18n.format("goatae2.machine.dual_bus.import.tooltip", new Object[0]));
        }
        tooltip.add(I18n.format("gregtech.universal.tooltip.item_storage_capacity", new Object[]{this.getInventorySize()}));
        tooltip.add(I18n.format("gregtech.universal.tooltip.fluid_storage_capacity_mult", new Object[]{this.numSlots, this.tankSize}));
        tooltip.add(I18n.format("gregtech.universal.enabled", new Object[0]));
    }

    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers", new Object[0]));
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.auto_collapse", new Object[0]));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing", new Object[0]));
        super.addToolUsages(stack, world, tooltip, advanced);
    }
    
    protected FluidTankList createImportFluidHandler() {
        return this.isExportHatch ? new FluidTankList(false, new IFluidTank[0]) : this.fluidTankList;
    }

    protected FluidTankList createExportFluidHandler() {
        return this.isExportHatch ? this.fluidTankList : new FluidTankList(false, new IFluidTank[0]);
    }

    public MultiblockAbility<IItemHandlerModifiable> getAbility() {
        return this.isExportHatch ? MultiblockAbility.EXPORT_ITEMS : MultiblockAbility.IMPORT_ITEMS;
    }

    //    public void registerAbilities(List<IFluidTank> abilityList) {
    //        abilityList.addAll(this.fluidTankList.getFluidTanks());
    //    }

    //    public void registerAbilities(List<IItemHandlerModifiable> abilityList) {
    //        if (this.hasGhostCircuitInventory() && this.actualImportItems != null) {
    //            abilityList.add(this.isExportHatch ? this.exportItems : this.actualImportItems);
    //        } else {
    //            abilityList.add(this.isExportHatch ? this.exportItems : this.importItems);
    //        }
    //    }

    @Override
    public void registerAbilities(List abilityList) {
        if (this.hasGhostCircuitInventory() && this.actualImportItems != null) {
            abilityList.add(this.isExportHatch ? this.exportItems : this.actualImportItems);
        } else {
            abilityList.add(this.isExportHatch ? this.exportItems : this.importItems);
        }
    }

    public void registerFluidAbility(List abilityList) {
        if (abilityList != null)
            abilityList.addAll(fluidTankList.getFluidTanks());
    }

    /// ////////////////////////////////////////////////////////////
    /// ////////////////////////////////////////////////////////////
    // Ability list reflections
    private static Field multiblockAbilitiesField;

    static {
        try {
            multiblockAbilitiesField = MultiblockControllerBase.class.getDeclaredField("multiblockAbilities");
            multiblockAbilitiesField.setAccessible(true);
        } catch (Exception e) {
            System.out.println("Failed to register fluid handler of Dual Input Bus");
            e.printStackTrace();
        }
    }

    private static Map<MultiblockAbility<Object>, List<Object>> getAbilitiesMap(MultiblockControllerBase controller) {
        try {
            return (Map<MultiblockAbility<Object>, List<Object>>) multiblockAbilitiesField.get(controller);
        } catch (Exception e) {
        }
        return null;
    }

    public static <T> List<T> getAbilityList(MultiblockControllerBase controller, MultiblockAbility<T> ability) {
        Map<MultiblockAbility<Object>, List<Object>> map = getAbilitiesMap(controller);
        return map == null ? null : (List<T>) map.computeIfAbsent((MultiblockAbility<Object>) ability, k -> new java.util.ArrayList<>());
    }
}
