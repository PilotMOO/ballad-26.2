package mod.pilot.birch_n_bees.events;

import mod.pilot.birch_n_bees.ABOBAB;
import mod.pilot.birch_n_bees.mixins.HudAccessor;
import mod.pilot.birch_n_bees.systems.dynamic_player_inventory.DynamicInventory;
import mod.pilot.birch_n_bees.systems.dynamic_player_inventory.DynamicInventoryToken;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = ABOBAB.MOD_ID, value = Dist.CLIENT)
public class ClientEvent {
    @SubscribeEvent
    public static void applyTokenOnScreenOpen(ScreenEvent.Opening event){
        if (event.isCanceled()) return;

        Screen screen = event.getScreen();
        Minecraft mc = screen.getMinecraft();
        if (mc.player != null) {
            DynamicInventoryToken.applyOnlyToMenu(mc.player);
        }
    }

    @SubscribeEvent
    public static void uiRenderInterceptPre(RenderGuiLayerEvent.Pre event){
        if (event.isCanceled()) return;

        if (event.getName().equals(VanillaGuiLayers.HOTBAR)){
            GuiGraphicsExtractor ext = event.getGuiGraphics();
            //All of this is redundant because the scissors work differently in this version
            /*int width = ext.guiWidth();
            int height = ext.guiHeight();
            //the sprite width of the GUI is 182, so take center of the screen and step over 91 pixels (182 / 2)
            int left = (width / 2) - 91;
            //Same idea here, sprite is 22 pixels tall, so take bottom of screen and step up 22 pixels
            int top = height - 22;
            //Enable scissors to cut out the old hotbar
            //ext.enableScissor(left, top, left + 182, height);
            */

            ext.enableScissor(0, 0, 1, 1);
        }
    }

    public static final Identifier MODULAR_HOTBAR_FRAME
            = Identifier.fromNamespaceAndPath(ABOBAB.MOD_ID, "textures/gui/hotbar/mod_hotbar_frame.png");
    public static final Identifier MODULAR_HOTBAR_BASE
            = Identifier.fromNamespaceAndPath(ABOBAB.MOD_ID, "textures/gui/hotbar/mod_hotbar_base.png");
    public static final Identifier VANILLA_HOTBAR_SELECTION_SPRITE
            = Identifier.withDefaultNamespace("textures/gui/sprites/hud/hotbar_selection.png");
    public static final Identifier VANILLA_HOTBAR_OFFHAND_LEFT_SPRITE
            = Identifier.withDefaultNamespace("textures/gui/sprites/hud/hotbar_offhand_left.png");
    public static final Identifier VANILLA_HOTBAR_OFFHAND_RIGHT_SPRITE
            = Identifier.withDefaultNamespace("textures/gui/sprites/hud/hotbar_offhand_right.png");
    @SubscribeEvent
    public static void uiRenderInterceptPost(RenderGuiLayerEvent.Post event){
        /*if (true) return;*/

        if (event.getName().equals(VanillaGuiLayers.HOTBAR)){
            GuiGraphicsExtractor ext = event.getGuiGraphics();
            ext.disableScissor(); //Disable the scissors since we enabled it in the pre event

            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            if (player != null){
                DynamicInventory inventory = (DynamicInventory)player.getInventory();
                DynamicInventoryToken token = DynamicInventoryToken.get(player);
                if (token.hotbarSlots > 0){
                    int width = ext.guiWidth();
                    int center = width / 2;
                    int height = ext.guiHeight();

                    int countToRender = Math.clamp(token.hotbarSlots, 0, 9);
                    int leftOffset = ((countToRender * 20) / 2) + 1;
                    int topOffset = height - 22;

                    for (int i = 0; i < countToRender; i++){
                        ext.blit(RenderPipelines.GUI_TEXTURED, MODULAR_HOTBAR_FRAME,
                                center - leftOffset, topOffset,
                                0f, 0f,
                                22, 22, 22, 22);
                        leftOffset -= 20;
                    }
                    leftOffset = ((countToRender * 20) / 2) + 1;
                    for (int i = 0; i < countToRender; i++){
                        ext.blit(RenderPipelines.GUI_TEXTURED, MODULAR_HOTBAR_BASE,
                                (center - leftOffset) + 1, topOffset + 1,
                                0f, 0f,
                                20, 20, 20, 20);
                        leftOffset -= 20;
                    }

                    int highlightSlot = Math.min(inventory.getSelectedSlot(), token.hotbarSlots - 1);
                    leftOffset = (((countToRender * 20) / 2) + 1);
                    int selectionX = (center - (leftOffset - (20 * highlightSlot))) - 1;
                    ext.blit(RenderPipelines.GUI_TEXTURED, VANILLA_HOTBAR_SELECTION_SPRITE,
                            selectionX, topOffset - 1,
                            0f, 0f,
                            24, 23, 24, 23);
                    HudAccessor hud = (HudAccessor)mc.gui.hud;

                    int seed = 1;
                    for (int i = 0; i < countToRender; i++){
                        hud.callExtractSlot(ext,
                                (center - leftOffset) + 3, height - 19,
                                event.getPartialTick(), player, inventory.getItem(i), seed++);
                        leftOffset -= 20;
                    }
                }
                if (token.offhand){
                    ItemStack offhand;
                    if (!(offhand = player.getOffhandItem()).isEmpty()) {
                        int width = ext.guiWidth();
                        int center = width / 2;
                        int height = ext.guiHeight();

                        boolean leftHandForOffhand = player.getMainArm().getOpposite() == HumanoidArm.LEFT;
                        int hotbarCount = Math.clamp(token.hotbarSlots, 0, 9);
                        int x;
                        int y = height - 23;
                        if (hotbarCount == 0) {
                            x = center - (leftHandForOffhand ? 29 : 0);
                        } else {
                            x = center;
                            if (leftHandForOffhand) x -= (((hotbarCount * 20) / 2) + 30);
                            else x += (((hotbarCount * 20) / 2) + 1);
                        }
                        ext.blit(RenderPipelines.GUI_TEXTURED,
                                leftHandForOffhand ? VANILLA_HOTBAR_OFFHAND_LEFT_SPRITE : VANILLA_HOTBAR_OFFHAND_RIGHT_SPRITE,
                                x, y,
                                0f, 0f,
                                29, 24, 29, 24);
                        HudAccessor hud = (HudAccessor)mc.gui.hud;
                        hud.callExtractSlot(ext,
                                x + (leftHandForOffhand ? 3 : 10), height - 19,
                                event.getPartialTick(), player, offhand, hotbarCount + 1);
                    }
                }
            }
        }
    }
}
