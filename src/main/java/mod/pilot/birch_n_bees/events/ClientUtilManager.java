package mod.pilot.birch_n_bees.events;

import mod.pilot.birch_n_bees.ABOBAB;
import mod.pilot.birch_n_bees.blocks.BirchBlocks;
import mod.pilot.birch_n_bees.blocks.block_entities.client.WildflowerBasketRenderer;
import mod.pilot.birch_n_bees.entity.BirchEntities;
import mod.pilot.birch_n_bees.entity.mob.client.*;
import mod.pilot.birch_n_bees.entity.projectiles.client.OvergrownArrowRenderer;
import mod.pilot.birch_n_bees.entity.projectiles.client.SplinterProjectileRenderer;
import mod.pilot.birch_n_bees.entity.projectiles.client.WildflowerPopperRenderer;
import mod.pilot.birch_n_bees.systems.dynamic_player_inventory.DynamicInventoryToken;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

@EventBusSubscriber(modid = ABOBAB.MOD_ID,/* bus = EventBusSubscriber.Bus.MOD,*/ value = Dist.CLIENT)
public class ClientUtilManager {
    @SubscribeEvent
    public static void registerLayers(final EntityRenderersEvent.RegisterRenderers event){
        event.registerEntityRenderer(BirchEntities.SPLINTER_PROJECTILE.get(), SplinterProjectileRenderer::new);
        event.registerEntityRenderer(BirchEntities.WILDFLOWER_POPPER_PROJECTILE.get(), WildflowerPopperRenderer::new);
        event.registerEntityRenderer(BirchEntities.OVERGROWN_ARROW.get(), OvergrownArrowRenderer::new);

        event.registerEntityRenderer(BirchEntities.SPLINTERING.get(), (context) -> new SplinteringRenderer<>(context, new SplinteringModel()));
        event.registerEntityRenderer(BirchEntities.NESTHEAD.get(), (context) -> new NestHeadRenderer<>(context, new NestHeadModel()));
        event.registerEntityRenderer(BirchEntities.BLOOMING_REMAINS.get(), (context) -> new BloomingRemainsRenderer<>(context, new BloomingRemainsModel()));


        event.registerBlockEntityRenderer(BirchBlocks.WILDFLOWER_BASKET_ENTITY.get(), WildflowerBasketRenderer::new);
    }

    @SubscribeEvent
    public static void register(RegisterClientPayloadHandlersEvent event) {
        event.register(
                DynamicInventoryToken.TokenReapplyRequest.PACKET_TYPE,
                DynamicInventoryToken::receiveReapplyRequestOnClient
        );
    }
}
