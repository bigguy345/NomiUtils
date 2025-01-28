package com.goat.goatae2.client.gui;

import appeng.api.config.ActionItems;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiNumberBox;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.container.interfaces.IJEIGhostIngredients;
import appeng.container.slot.IJEITargetSlot;
import appeng.container.slot.SlotFake;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.helpers.InventoryAction;
import appeng.util.item.AEItemStack;
import com.goat.goatae2.GOATAE2;
import com.goat.goatae2.RenderUtil;
import com.goat.goatae2.Utility;
import com.goat.goatae2.client.gui.components.GuiImgButtonGAE2;
import com.goat.goatae2.container.ContainerLevelMaintainer;
import com.goat.goatae2.container.slot.FluidSlot;
import com.goat.goatae2.network.PacketHandler;
import com.goat.goatae2.network.packet.JEIGhostSlotPacket;
import com.goat.goatae2.network.packet.OpenCraftingGUI;
import com.goat.goatae2.network.packet.SyncMaintainerGUIPacket;
import com.goat.goatae2.network.packet.UpdateMaintainerPacket;
import com.goat.goatae2.tile.ItemMaintainerInventory;
import com.goat.goatae2.tile.TileLevelMaintainer;
import io.netty.buffer.ByteBuf;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;

public class GuiLevelMaintainer extends AEBaseGui implements IJEIGhostIngredients {

    protected final Map<IGhostIngredientHandler.Target<?>, Object> mapTargetSlot = new HashMap<>();
    private final ContainerLevelMaintainer cont;
    public TileLevelMaintainer tile;
    private Slot selectedSlot;
    private GuiTabButton craftingStatus;
    private GuiImgButton clear, fuzzyMode;
    private GuiImgButtonGAE2 run;

    private GuiNumberBox threshold, batchSize;
    private boolean thresholdEnabled, batchEnabled;

    public GuiLevelMaintainer(InventoryPlayer ipl, TileLevelMaintainer tile) {
        super(new ContainerLevelMaintainer(ipl, tile));
        this.cont = (ContainerLevelMaintainer) inventorySlots;
        this.tile = tile;
        this.xSize = 246;
        this.ySize = 251;

        for (int x = 0; x < tile.config.size; ++x) {
            if (tile.config.items[x] != null) {
                tile.config.items[x].setCraftable(tile.config.isCraftable[x]);
            }
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        int y = guiTop + 8;
        this.buttonList.add(this.craftingStatus = new GuiTabButton(this.guiLeft + 154, this.guiTop, 2 + 11 * 16, GuiText.CraftingStatus.getLocal(), this.itemRender));

        this.buttonList.add(this.run = new GuiImgButtonGAE2(this.guiLeft - 18, y, 0).setText("Run", "Runs crafting cycle"));
        this.buttonList.add(this.clear = new GuiImgButton(this.guiLeft - 18, y += 20, Settings.ACTIONS, ActionItems.CLOSE));
        this.buttonList.add(this.fuzzyMode = new GuiImgButton(this.guiLeft - 18, y += 20, Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL));


        this.threshold = new GuiNumberBox(this.fontRenderer, this.guiLeft + 184, this.guiTop + 80, 62, this.fontRenderer.FONT_HEIGHT, Long.class);
        this.threshold.setEnableBackgroundDrawing(false);
        this.threshold.setMaxStringLength(10);
        this.threshold.setTextColor(0xFFFFFF);
        this.threshold.setVisible(true);
        setEnabled(threshold, selectedSlot != null);


        this.batchSize = new GuiNumberBox(this.fontRenderer, this.guiLeft + 184, this.guiTop + 52, 62, this.fontRenderer.FONT_HEIGHT, Long.class);
        this.batchSize.setEnableBackgroundDrawing(false);
        this.batchSize.setMaxStringLength(10);
        this.batchSize.setTextColor(0xFFFFFF);
        this.batchSize.setVisible(true);
        setEnabled(batchSize, selectedSlot != null);
    }

    @Override
    protected void renderHoveredToolTip(int x, int y) {
        Slot slot = null;

        if (this.mc.player.inventory.getItemStack().isEmpty() && (slot = this.getSlotUnderMouse()) != null && slot.getHasStack() && slot instanceof SlotFake) {
            int id = slot.slotNumber;
            ItemMaintainerInventory inv = cont.getTile().config;
            IAEItemStack slotItem = inv.items[id];
            long stackSize = !inv.inSystemStock[id] ? 0 : slotItem.getStackSize();
            boolean isFluid = Utility.isFluid(slotItem);

            List<String> tooltip = getItemToolTip(slot.getStack());
            tooltip.add(TextFormatting.GRAY + localize("maintainer.stored") + stackSize + (isFluid ? " mB" : ""));

            if (inv.isCrafting[id])
                tooltip.add(TextFormatting.GREEN + localize("maintainer.isCrafting"));
            else if (inv.craftFailed[id]) {
                tooltip.add(TextFormatting.RED + localize("maintainer.craftFailed"));
                if (inv.failReason[id] != null)
                    tooltip.add(TextFormatting.RED + localize(inv.failReason[id]));
            }

            this.drawHoveringText(tooltip, x, y);
        } else {
            super.renderHoveredToolTip(x, y);
        }
    }

    public String localize(String unlocalized) {
        return I18n.translateToLocal(unlocalized);
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        if (thresholdEnabled && mouseOverBox(threshold, mouseX, mouseY)) {
            int newValue = scrollOnBox(threshold);
            if (newValue != -1)
                PacketHandler.Instance.sendToServer(new UpdateMaintainerPacket(selectedSlot.slotNumber, newValue, UpdateMaintainerPacket.Type.SET_THRESHOLD));
        }

        if (batchEnabled && mouseOverBox(batchSize, mouseX, mouseY)) {
            int newValue = scrollOnBox(batchSize);
            if (newValue != -1)
                PacketHandler.Instance.sendToServer(new UpdateMaintainerPacket(selectedSlot.slotNumber, newValue, UpdateMaintainerPacket.Type.SET_BATCH_SIZE));
        }
    }

    public int getWheelIncrement() {
        if (selectedSlot instanceof FluidSlot)
            return isCtrlKeyDown() ? 100000 : isShiftKeyDown() ? 10000 : isAltKeyDown() ? 1 : 1000;

        return isCtrlKeyDown() ? 512 : isShiftKeyDown() ? 64 : 1;
    }

    public int scrollOnBox(GuiNumberBox box) {
        int wheel = Mouse.getDWheel();
        if (wheel != 0) {
            int value = parseInt(box.getText());

            int increment = getWheelIncrement();
            long newValue = (long) value + (wheel > 0 ? increment : -increment);
            newValue = Math.max(0, newValue);
            if (newValue > Integer.MAX_VALUE)
                newValue = Integer.MAX_VALUE;

            box.setText(newValue + "");
            return (int) newValue;
        }
        return -1;
    }

    public int parseInt(String number) {
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException e) {
            long max = 0;
            try {
                max = Long.parseLong(number);
            } catch (NumberFormatException ex) {
            }
            return max > Integer.MAX_VALUE ? Integer.MAX_VALUE : 0;
        }
    }

    public void drawZeroSlot(Slot s, ItemStack item) {
        int i = s.xPos;
        int j = s.yPos;

        GlStateManager.enableDepth();
        this.itemRender.renderItemAndEffectIntoGUI(this.mc.player, item, i, j);
        this.itemRender.renderItemOverlayIntoGUI(this.fontRenderer, item, i, j, null);
        RenderUtil.renderStackSize(this.fontRenderer, AEItemStack.fromItemStack(item).setStackSize(0), i, j, "0");
    }

    public void drawSlot(Slot slot) {
        ItemStack stack = slot.getStack();
        boolean draw = slot instanceof SlotFake && !stack.isEmpty();
        if (!draw) {
            super.drawSlot(slot);
            return;
        }

        boolean isZero = false;
        boolean inStock = tile.config.inSystemStock[slot.slotNumber];
        if (!inStock)
            isZero = true;

        if (isZero)
            drawZeroSlot(slot, stack);
        else
            super.drawSlot(slot);

        boolean craftFailed = tile.config.craftFailed[slot.slotNumber];
        boolean selected = slot.equals(selectedSlot);
        if (craftFailed || selected) { //draw outline
            int x = slot.xPos, y = slot.yPos;
            int width = 16, height = 16;
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            GlStateManager.disableTexture2D();
            GlStateManager.disableDepth();

            if (craftFailed && selected)
                GlStateManager.color(1f, 0, 1f, 1);
            else if (selected)
                GlStateManager.color(0.35f, 1, 0.71f, 1);
            else
                GlStateManager.color(1, 0, 0, 1);
            GL11.glLineWidth(2);

            buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
            buffer.pos(x, y, 0).endVertex();            // Bottom-left corner
            buffer.pos(x + width, y, 0).endVertex();    // Bottom-right corner
            buffer.pos(x + width, y + height, 0).endVertex(); // Top-right corner
            buffer.pos(x, y + height, 0).endVertex();   // Top-left corner
            tessellator.draw();

            GlStateManager.color(1, 1, 1, 1);
            GlStateManager.enableTexture2D();
        }

        boolean isCrafting = tile.config.isCrafting[slot.slotNumber];
        if (isCrafting) { //draw isCrafting/failed circle
            mc.getTextureManager().bindTexture(LEVEL_MAINTAINER);
            GlStateManager.disableDepth();
            GlStateManager.pushMatrix();
            this.drawTexturedModalRect(slot.xPos, slot.yPos, 256 - 16, 256 - 16, 16, 16);
            GlStateManager.popMatrix();
            GlStateManager.enableDepth();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int btn) throws IOException {
        super.mouseClicked(mouseX, mouseY, btn);

        if (thresholdEnabled && mouseOverBox(threshold, mouseX, mouseY)) {
            if (btn == 1)
                threshold.setText("");
            threshold.setFocused(true);
            batchSize.setFocused(false);
        }
        if (batchEnabled && mouseOverBox(batchSize, mouseX, mouseY)) {
            if (btn == 1)
                batchSize.setText("");
            batchSize.setFocused(true);
            threshold.setFocused(false);
        }
    }

    private boolean mouseOverBox(GuiNumberBox box, int mouseX, int mouseY) {
        return mouseX >= box.x && mouseX <= box.x + box.width && mouseY >= box.y && mouseY <= box.y + box.height;
    }

    @Override
    protected void keyTyped(final char character, final int key) throws IOException {
        if (!this.checkHotbarKeys(key)) {
            GuiNumberBox focused = threshold.isFocused() ? threshold : batchSize.isFocused() ? batchSize : null;
            if ((key == 211 || key == 205 || key == 203 || key == 14 || character == '-' || Character.isDigit(character)) && focused != null && focused.textboxKeyTyped(character, key)) {
                String text = focused.getText();

                boolean fixed = false;
                while (text.startsWith("0") && text.length() > 1) {
                    text = text.substring(1);
                    fixed = true;
                }

                if (fixed) {
                    focused.setText(text);
                }

                if (text.isEmpty()) {
                    text = "0";
                }

                int value = parseInt(text);
                focused.setText(value + "");

                UpdateMaintainerPacket.Type type = threshold.isFocused() ? UpdateMaintainerPacket.Type.SET_THRESHOLD : UpdateMaintainerPacket.Type.SET_BATCH_SIZE;
                PacketHandler.Instance.sendToServer(new UpdateMaintainerPacket(selectedSlot.slotNumber, value, type));
            } else {
                super.keyTyped(character, key);
            }
        }
    }

    protected void handleMouseClick(Slot slot, int slotIdx, int mouseButton, ClickType clickType) {
        if (getBookmarkedIngredient() != null) {
            super.handleMouseClick(slot, slotIdx, mouseButton, clickType);
            return;
        } else if (slot instanceof SlotFake || slot instanceof FluidSlot) {

            ItemStack slotItem = slot.getStack();
            boolean empty = slotItem.isEmpty();

            int id = slot.slotNumber;
            if (this.mc.gameSettings.keyBindPickBlock.isActiveAndMatches(mouseButton - 100) || isAltKeyDown() && mouseButton == 0) {
                if (!empty && tile.config.isCraftable[id]) {
                    NBTTagCompound compound = new NBTTagCompound();
                    tile.config.items[id].writeToNBT(compound);
                    compound.setInteger("slotId", tile.tempClickedSlot = id);
                    compound.setBoolean("shiftDown", isShiftKeyDown());
                    PacketHandler.Instance.sendToServer(new OpenCraftingGUI(OpenCraftingGUI.Type.CRAFTING_CONFIRM, compound));
                }
                return;
            }

            selectedSlot = slot;
            setEnabled(threshold, !empty);
            setEnabled(batchSize, !empty);


            boolean handEmpty = mc.player.inventory.getItemStack().isEmpty();
            InventoryAction action = null;
            if (mouseButton == 0 && (empty || !handEmpty) || mouseButton == 1 && !empty)
                action = InventoryAction.PICKUP_OR_SET_DOWN;

            if (action != null) {
                PacketInventoryAction p = new PacketInventoryAction(action, slotIdx, mouseButton);
                NetworkHandler.instance().sendToServer(p);
            }

            return;
        }
        super.handleMouseClick(slot, slotIdx, mouseButton, clickType);
    }

    @Override
    public List<IGhostIngredientHandler.Target<?>> getPhantomTargets(Object ingredient) {
        mapTargetSlot.clear();

        FluidStack fluidStack = null;
        ItemStack itemStack = ItemStack.EMPTY;

        if (ingredient instanceof ItemStack) {
            itemStack = (ItemStack) ingredient;
            fluidStack = FluidUtil.getFluidContained(itemStack);
        } else if (ingredient instanceof FluidStack) {
            fluidStack = (FluidStack) ingredient;
        }

        if (!(ingredient instanceof ItemStack) && !(ingredient instanceof FluidStack)) {
            return Collections.emptyList();
        }

        List<IGhostIngredientHandler.Target<?>> targets = new ArrayList<>();

        List<IJEITargetSlot> slots = new ArrayList<>();
        if (!this.inventorySlots.inventorySlots.isEmpty()) {
            for (Slot slot : this.inventorySlots.inventorySlots) {
                if (slot instanceof SlotFake && (!itemStack.isEmpty()) || slot instanceof FluidSlot && fluidStack != null) {
                    slots.add((IJEITargetSlot) slot);
                }
            }
        }

        for (IJEITargetSlot slot : slots) {
            ItemStack finalItemStack = itemStack;
            FluidStack finalFluidStack = fluidStack;
            IGhostIngredientHandler.Target<Object> targetItem = new IGhostIngredientHandler.Target<Object>() {
                @Nonnull
                @Override
                public Rectangle getArea() {
                    if (slot instanceof FluidSlot && ((FluidSlot) slot).isSlotEnabled() && finalFluidStack != null) {
                        return new Rectangle(getGuiLeft() + ((FluidSlot) slot).xPos, getGuiTop() + ((FluidSlot) slot).yPos, 16, 16);
                    } else if (slot instanceof SlotFake && !(slot instanceof FluidSlot) && ((SlotFake) slot).isSlotEnabled()) {
                        return new Rectangle(getGuiLeft() + ((SlotFake) slot).xPos, getGuiTop() + ((SlotFake) slot).yPos, 16, 16);
                    } else
                        return new Rectangle();
                }

                @Override
                public void accept(@Nonnull Object ingredient) {
                    selectedSlot = getSlotUnderMouse();
                    ItemStack item = finalFluidStack != null ? FluidUtil.getFilledBucket(finalFluidStack) : finalItemStack;
                    PacketHandler.Instance.sendToServer(new JEIGhostSlotPacket(selectedSlot.slotNumber, 0, AEItemStack.fromItemStack(item)));
                }
            };
            targets.add(targetItem);
            mapTargetSlot.putIfAbsent(targetItem, slot);
        }
        return targets;
    }

    @Override
    public Map<IGhostIngredientHandler.Target<?>, Object> getFakeSlotTargetMap() {
        return mapTargetSlot;
    }

    public void sync(ByteBuf buf) {
        int id = buf.readInt();
        long value = buf.readLong();
        SyncMaintainerGUIPacket.Type type = SyncMaintainerGUIPacket.Type.values()[buf.readInt()];

        if (type == SyncMaintainerGUIPacket.Type.TOGGLE_BOXES) {
            boolean on = value == 1;
            setEnabled(threshold, on);
            setEnabled(batchSize, on);
        } else if (type == SyncMaintainerGUIPacket.Type.CLEAR_SLOT) {
            ItemMaintainerInventory inv = tile.config;
            inv.thresholds[id] = inv.batchSizes[id] = 0;
            inv.inSystemStock[id] = inv.isCraftable[id] = inv.isCrafting[id] = inv.craftFailed[id] = false;
            inv.failReason[id] = null;
        } else if (type == SyncMaintainerGUIPacket.Type.CLEAR_ALL_SLOTS) {
            ItemMaintainerInventory inv = tile.config;
            for (int x = 0; x < inv.size; ++x) {
                inv.thresholds[x] = inv.batchSizes[x] = 0;
                inv.inSystemStock[x] = inv.isCraftable[id] = inv.isCrafting[x] = inv.craftFailed[x] = false;
                inv.failReason[x] = null;
            }
        }
    }

    public void setEnabled(GuiNumberBox box, boolean bo) {
        box.setEnabled(bo);
        if (!bo) {
            box.setFocused(false);
            box.setText("");
        } else if (selectedSlot != null) {
            threshold.setText(tile.config.thresholds[selectedSlot.slotNumber] + "");
            batchSize.setText(tile.config.batchSizes[selectedSlot.slotNumber] + "");
        }


        if (box.equals(threshold))
            thresholdEnabled = bo;
        if (box.equals(batchSize))
            batchEnabled = bo;
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        fontRenderer.drawString("Level Maintainer", 8, 6, 0x404040);
        fontRenderer.drawString("Items", 8, 18, 0x404040);
        fontRenderer.drawString("Fluids", 8, ySize - 143, 0x404040);
        fontRenderer.drawString("To Craft", 183, ySize - 210, 0x404040);
        fontRenderer.drawString("When Under", 183, ySize - 182, 0x404040);
        fontRenderer.drawString(GuiText.inventory.getLocal(), 8, ySize - 94, 0x404040);

        if (this.fuzzyMode != null) {
            FuzzyMode fzMode = cont.getFuzzyMode();
            this.fuzzyMode.set(fzMode);
        }
    }

    private static final ResourceLocation LEVEL_MAINTAINER = GOATAE2.resource("textures/gui/level_maintainer.png");

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(LEVEL_MAINTAINER);
        this.drawTexturedModalRect(offsetX, offsetY, 0, 0, 256, this.ySize);

        threshold.drawTextBox();
        batchSize.drawTextBox();

        if (this.fuzzyMode != null) {
            this.fuzzyMode.setVisibility(tile.getInstalledUpgrades(Upgrades.FUZZY) > 0);
        }
    }

    @Override
    protected void actionPerformed(final GuiButton btn) throws IOException {
        super.actionPerformed(btn);


        if (btn == this.craftingStatus)
            PacketHandler.Instance.sendToServer(new OpenCraftingGUI(OpenCraftingGUI.Type.CRAFTING_STATUS, null));

        if (btn == this.run)
            PacketHandler.Instance.sendToServer(new UpdateMaintainerPacket(0, 0, UpdateMaintainerPacket.Type.RUN));

        if (btn == this.clear)
            PacketHandler.Instance.sendToServer(new UpdateMaintainerPacket(0, 0, UpdateMaintainerPacket.Type.CLEAR_ALL));

        if (btn == this.fuzzyMode)
            PacketHandler.Instance.sendToServer(new UpdateMaintainerPacket(0, 0, UpdateMaintainerPacket.Type.SET_FUZZY));
    }
}
