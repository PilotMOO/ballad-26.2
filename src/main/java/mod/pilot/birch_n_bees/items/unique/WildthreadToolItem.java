package mod.pilot.birch_n_bees.items.unique;

import mod.pilot.birch_n_bees.items.BirchItems;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class WildthreadToolItem extends BuildableToolBase{
    private static final int craftTime = 3000, checkpoint = craftTime / 3;

    public WildthreadToolItem(Properties properties) {
        super(properties, craftTime);
    }

    @Override
    public void fillValidHeads() {
        validHeads = new ToolHead[1];
        validHeads[0] = new ToolHead(BirchItems.STRIPPED_WILDFLOWER_TWINE.get(), new ItemStack(BirchItems.WILDTHREAD.get()));
    }

    @Override
    public void onUseTick(@NotNull Level level, @NotNull LivingEntity livingEntity, @NotNull ItemStack stack, int remainingUseDuration) {
        super.onUseTick(level, livingEntity, stack, remainingUseDuration);

        int damage = getDamage(stack);
        if (damage == craftTime - checkpoint || damage == checkpoint) {
            setHead(stack, ToolHead.invalid());
            stack.setDamageValue(damage - 1);
        }
    }

    public void trySelectHead(LivingEntity user, ItemStack stack) {
        ItemStack offhand = user.getOffhandItem();
        if (user.getMainHandItem().equals(stack)) {
            ToolHead toolHead;
            if (offhand.isEmpty() && user instanceof Player p) {
                if (stack.getDamageValue() < getMaxDamage(stack)) {
                    messagePlayer(p, "birch_n_bees.message.no_thread_2");
                } else messagePlayer(p, "birch_n_bees.message.no_thread_1");
            }
            else if ((toolHead = findTool(offhand)).isValid()) {
                setHead(stack, toolHead);
                offhand.shrink(1);
                user.playSound(SoundEvents.COBWEB_HIT, 1.0F, 1.5F);
            } else if (user instanceof Player p) messagePlayer(p, "birch_n_bees.message.isnt_thread");
        } else if (offhand.equals(stack) && user instanceof Player p) {
            messagePlayer(p, "birch_n_bees.message.no_offhand_thread");
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder,
                                @NotNull TooltipFlag flag) {
        ToolHead toolHead = getHead(stack);
        if (toolHead.isValid()) {
            tooltipAdder.accept(Component.translatable("item.birch_n_bees.buildable_tool.winding_thread"));
        } else {
            tooltipAdder.accept(Component.translatable("item.birch_n_bees.buildable_tool.no_head_thread"));
        }
    }
}
