package dev.stephan.kingdommod.ability;

import net.minecraft.item.ItemStack;

public class Ability {

    public final String id;
    public final ItemStack hudItem;
    public final String abilityName;
    public final int maxCooldownTicks;
    public int cooldownTicks = 0;
    public int hudX;
    public int hudY;
    public final TriggerType trigger;

    public Ability(String id, ItemStack hudItem, String abilityName, int maxCooldownTicks, int hudX, int hudY, TriggerType trigger) {
        this.id = id;
        this.hudItem = hudItem.copy();
        this.abilityName = abilityName;
        this.maxCooldownTicks = maxCooldownTicks;
        this.hudX = hudX;
        this.hudY = hudY;
        this.trigger = trigger;
    }

    public void tick() {
        if (cooldownTicks > 0) {
            cooldownTicks--;
        }
    }

    public void tryActivate() {
        if (cooldownTicks > 0) return;
        cooldownTicks = maxCooldownTicks;
    }

}