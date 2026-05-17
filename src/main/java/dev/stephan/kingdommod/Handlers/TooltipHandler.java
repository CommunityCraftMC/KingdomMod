package dev.stephan.kingdommod.Handlers;

import dev.stephan.kingdommod.KingdomMod;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.component.DataComponentTypes;

public class TooltipHandler {

    private final int MAX_ANVIL_COST = 39;

    public void registerRepairTooltip() {
        ItemTooltipCallback.EVENT.register(this::addRepairCostTooltip);
    }

    private void addRepairCostTooltip(ItemStack stack, Item.TooltipContext ctx, TooltipType type, java.util.List<Text> texts) {
        if (!KingdomMod.config.ShowRepairCost || !stack.isDamageable()) return;

        int cost = getRepairCost(stack);
        if (cost > MAX_ANVIL_COST) {
            texts.add(Text.literal("Verwachte fix prijs: Too Expensive!").formatted(Formatting.RED));
        } else {
            texts.add(Text.literal("Verwachte fix prijs: " + cost + " levels").formatted(Formatting.GREEN));
        }
    }

    private int getRepairCost(ItemStack stack) {
        Integer repairCost = stack.get(DataComponentTypes.REPAIR_COST);
        int estimated = calculateEstimatedRepairCost(stack, repairCost != null ? repairCost : 0);
        return Math.max(1, estimated);
    }

    private int calculateEstimatedRepairCost(ItemStack stack, int baseCost) {
        int missing = stack.getDamage();
        int max = stack.getMaxDamage();
        int diamonds = (int) Math.ceil((double) missing / (max * 0.25));
        return baseCost + diamonds;
    }
}
