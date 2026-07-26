package mod.pilot.birch_n_bees.items.unique;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mod.pilot.birch_n_bees.util.BirchDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;


public abstract class BuildableToolBase extends Item {
    public BuildableToolBase(Properties properties, int craftTime) {
        super(properties.component(BirchDataComponents.TOOL_HEAD.get(), ToolHead.invalid())
                .component(BirchDataComponents.LAST_USE_DURABILITY.get(), -1)
                .component(DataComponents.MAX_DAMAGE, craftTime).component(DataComponents.DAMAGE, craftTime)
                .component(DataComponents.MAX_STACK_SIZE, 1));
        //fillValidHeads();
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
        if (getHead(stack).isValid()) {
            int lastUse = stack.getOrDefault(BirchDataComponents.LAST_USE_DURABILITY, -1);
            return lastUse == -1 ? getMaxDamage(stack) : lastUse;
        } else return 0;
    }

    @Override
    public @NotNull ItemUseAnimation getUseAnimation(@NotNull ItemStack stack) {
        return ItemUseAnimation.DRINK;
    }

    public ToolHead[] validHeads;
    public abstract void fillValidHeads();
    public ToolHead findTool(ItemStack stack) {
        if (stack.isEmpty()) return ToolHead.invalid();
        for (ToolHead toolHead : validHeads) {
            if (stack.is(toolHead.head())) return toolHead;
        }
        return ToolHead.invalid();
    }

    public void setHead(ItemStack stack, ToolHead toolHead) {
        stack.set(BirchDataComponents.TOOL_HEAD.get(), toolHead);
    }
    public ToolHead getHead(ItemStack stack) {
        return stack.get(BirchDataComponents.TOOL_HEAD.get());
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ToolHead toolHead = getHead(stack);
        if (toolHead.isValid()){
            System.out.println("TOOL HEAD [" + toolHead + "] is VALID, trying to start using...");
            stack.set(BirchDataComponents.LAST_USE_DURABILITY.get(), getDamage(stack));
            player.startUsingItem(hand);
            return InteractionResult.SUCCESS;
        }
        else {
            System.out.println("TOOL HEAD [" + toolHead + "] is NOT VALID, trying to select head...");
            trySelectHead(player, stack);
            player.stopUsingItem();
            return InteractionResult.CONSUME;
        }
    }

    @Override
    public void onUseTick(@NotNull Level level, @NotNull LivingEntity livingEntity, @NotNull ItemStack stack, int remainingUseDuration) {
        ToolHead toolHead = getHead(stack);
        if (toolHead.isValid()){
            int damage = stack.getDamageValue();
            stack.setDamageValue(damage - 1);
            if (damage % 20 == 19) livingEntity.playSound(SoundEvents.ITEM_FRAME_ADD_ITEM, 1f, 0.5f);
        } else trySelectHead(livingEntity, stack);
    }
    public void trySelectHead(LivingEntity user, ItemStack stack) {
        ItemStack offhand = user.getOffhandItem();
        if (user.getMainHandItem().equals(stack)) {
            ToolHead toolHead;
            if (offhand.isEmpty() && user instanceof Player p)
                messagePlayer(p, "birch_n_bees.message.empty_offhand");
            else if ((toolHead = findTool(offhand)).isValid()) {
                setHead(stack, toolHead);
                offhand.shrink(1);
                user.playSound(SoundEvents.ANVIL_LAND, 1.0F, 1.5F);
            } else if (user instanceof Player p) messagePlayer(p, "birch_n_bees.message.invalid_toolhead");
        } else if (offhand.equals(stack) && user instanceof Player p) {
            messagePlayer(p, "birch_n_bees.message.no_offhand");
        }
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity livingEntity) {
        ToolHead toolHead = getHead(stack);
        if (toolHead.isValid()) {
            livingEntity.playSound(SoundEvents.ITEM_PICKUP, 0.5f, 1.5f);
            return toolHead.result().copy();
        }
        return stack;
        /*else {
            stack.setDamageValue(getMaxDamage(stack));
            return stack;
        }*/
    }

    static void messagePlayer(Player player, String translatable){
        player.sendOverlayMessage(Component.translatable(translatable));
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        ToolHead toolHead = getHead(stack);
        if (toolHead.isValid()){
            return Component.translatable("item.birch_n_bees.buildable_tool.incomplete").append(toolHead.result().getHoverName());
        }
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder,
                                @NotNull TooltipFlag flag) {
        ToolHead toolHead = getHead(stack);
        if (toolHead.isValid()) {
            tooltipAdder.accept(Component.translatable("item.birch_n_bees.buildable_tool.tool_head")
                    .append(toolHead.head.getName(toolHead.head.getDefaultInstance())));
            tooltipAdder.accept(Component.translatable("item.birch_n_bees.buildable_tool.will_build")
                    .append(toolHead.result.getHoverName()));
        } else {
            tooltipAdder.accept(Component.translatable("item.birch_n_bees.buildable_tool.no_head"));
            tooltipAdder.accept(Component.translatable("item.birch_n_bees.buildable_tool.can_build_line1"));
            MutableComponent line2 = Component.translatable("item.birch_n_bees.buildable_tool.can_build_line2");
            for (ToolHead vHead : validHeads) {
                Component head = vHead.head.getName(vHead.head.getDefaultInstance());
                Component result = vHead.result.getHoverName();
                tooltipAdder.accept(result.copy().append(line2.withColor(11184810)).append(head));
            }
        }
    }

    public record ToolHead(Item head, ItemStack result){
        public static final ToolHead INVALID = new ToolHead(null, ItemStack.EMPTY);
        public static ToolHead invalid(){return INVALID;}
        public boolean isValid() { return head != null && !result.isEmpty(); }

        public static final Codec<ToolHead> CODEC = RecordCodecBuilder.create(
                (toolHead) -> toolHead.group(
                        Item.CODEC.fieldOf("tool_head")
                                .forGetter((o) -> o.head.builtInRegistryHolder()),
                        ItemStack.CODEC.fieldOf("result").forGetter(ToolHead::result)).apply(toolHead,
                        (head, result) -> new ToolHead(head.value(), result)));
        public static final StreamCodec<RegistryFriendlyByteBuf, ToolHead> STREAM_CODEC =
                new StreamCodec<>() {
            @Override
            public @NotNull ToolHead decode(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
                Item item = Item.byId(registryFriendlyByteBuf.readVarInt());
                ItemStack itemStack = ItemStack.STREAM_CODEC.decode(registryFriendlyByteBuf);
                return new ToolHead(item, itemStack);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf o, ToolHead toolHead) {
                o.writeVarInt(Item.getId(toolHead.head));
                ItemStack.STREAM_CODEC.encode(o, toolHead.result);
            }
        };
    }
}
