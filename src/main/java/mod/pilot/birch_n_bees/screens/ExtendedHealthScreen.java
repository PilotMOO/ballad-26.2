package mod.pilot.birch_n_bees.screens;

import mod.pilot.birch_n_bees.ABOBAB;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class ExtendedHealthScreen extends AbstractContainerScreen<ExtendedHealthMenu> {
    public static final Identifier UI_BACKGROUND = Identifier.fromNamespaceAndPath(ABOBAB.MOD_ID,
            "textures/gui/container/extended_health/health_ui_background.png");
    public static final Identifier ARMOR_SLOTS = Identifier.fromNamespaceAndPath(ABOBAB.MOD_ID,
            "textures/gui/container/extended_health/health_ui_armor_slots.png");

    public ExtendedHealthScreen(ExtendedHealthMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 256, 256);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        int xo = (this.width - this.imageWidth) / 2;
        int yo = (this.height - this.imageHeight) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, UI_BACKGROUND, xo, yo,
                0.0F, 0.0F, this.imageWidth, this.imageHeight,
                256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, ARMOR_SLOTS, xo - 25, yo + 142,
                0.0F, 0.0F, 96, 96,
                96, 96);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int xm, int ym) {
        //maybe add this later
        //super.extractLabels(graphics, xm, ym);
    }
}
