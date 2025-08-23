package com.goat.goatae2;

import com.goat.goatae2.tile.greg.MetaTileEntityDualBus;
import gregtech.api.GTValues;
import gregtech.api.recipes.ModHandler;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.common.metatileentities.MetaTileEntities;

public class GregTechTiles {

    public static MetaTileEntityDualBus DUAL_INPUT_BUS;

    public static void init() {

        int tier = GTValues.IV;
        String voltageName = GTValues.VN[tier].toLowerCase();

        DUAL_INPUT_BUS = new MetaTileEntityDualBus(GOATAE2.resource("dual_bus.import." + voltageName), tier, 4, false);
        MetaTileEntities.registerMetaTileEntity(11250 + tier, DUAL_INPUT_BUS);
    }

    public static void recipeInit() {
        int tier = DUAL_INPUT_BUS.getTier();
        ModHandler.addShapedRecipe("dual_input_bus", DUAL_INPUT_BUS.getStackForm(), new Object[]{"AB ", "   ", "   ", 'A', MetaTileEntities.ITEM_IMPORT_BUS[tier].getStackForm(), 'B', MetaTileEntities.FLUID_IMPORT_HATCH[tier].getStackForm()});
    }

    public static final SimpleOverlayRenderer DUAL_INPUT_OVERLAY;

    static {
        DUAL_INPUT_OVERLAY = new SimpleOverlayRenderer("overlay/machine/overlay_dual_input");
    }
}
