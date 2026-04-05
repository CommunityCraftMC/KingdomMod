package dev.stephan.kingdommod.ability;

import dev.stephan.kingdommod.KingdomMod;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AbilityManager {

    public static final List<Ability> ABILITIES = new ArrayList<>();
    public static final Map<Integer, Box> trackedArrows = new HashMap<>();

    public static void addAbility(Ability ability) {
        ABILITIES.add(ability);
    }

    public static Optional<String> getAbilityName(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return Optional.empty();

        LoreComponent lore = stack.get(DataComponentTypes.LORE);
        if (lore == null) return Optional.empty();

        for (Text line : lore.lines()) {
            String plain = line.getString();
            if (plain.startsWith("Ability:")) {
                String abilityName = plain.substring("Ability:".length()).trim();
                if (!abilityName.isEmpty()) return Optional.of(abilityName);
            }
        }

        return Optional.empty();
    }

    public static boolean hasAbility(ItemStack stack, String abilityName) {
        return getAbilityName(stack).map(a -> a.equals(abilityName)).orElse(false);
    }

    public static void RegisterAbility() {

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            ItemStack held = client.player.getMainHandStack();
            AbilityManager.tickAll();

            boolean leftClickPressed = client.options.attackKey.isPressed();
            boolean rightClickPressed = client.options.useKey.isPressed();

            AbilityManager.tryActivate(client, held, leftClickPressed, rightClickPressed);
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null || client.player == null) return;

            client.world.getEntitiesByClass(ArrowEntity.class,
                    client.player.getBoundingBox().expand(128),
                    arrow -> {
                        Entity owner = arrow.getOwner();
                        return owner != null && owner.getUuid().equals(client.player.getUuid());
                    }
            ).forEach(arrow -> trackedArrows.put(arrow.getId(), arrow.getBoundingBox().expand(1.1)));

            trackedArrows.keySet()
                    .removeIf(id -> client.world.getEntityById(id) == null);
        });

        HudLayerRegistrationCallback.EVENT.register(layeredDrawer -> layeredDrawer.attachLayerAfter(
                IdentifiedLayer.MISC_OVERLAYS,
                Identifier.of("kingdommod", "ability_cooldowns"),
                (context, tickDelta) -> {
                    if (!KingdomMod.config.ShowAbilityCooldown) return;
                    MinecraftClient mc = MinecraftClient.getInstance();
                    if (mc.player == null) return;

                    int backgroundColor = new Color(220, 220, 220, 100).getRGB();
                    int ringColor = new Color(80, 80, 80, 180).getRGB();
                    int progressColor = new Color(120, 255, 120, 80).getRGB();

                    for (Ability ability : AbilityManager.ABILITIES) {
                        boolean hasItem = mc.player.getInventory().main.stream()
                                .anyMatch(s -> !s.isEmpty() && hasAbility(s, ability.abilityName));

                        if (!hasItem) continue;

                        ItemStack stack = ability.hudItem.copy();
                        int percent = ability.cooldownTicks / 20;

                        if (percent > 0) {
                            AbilityRenderer.drawAbilityCooldown(context, ability, stack, backgroundColor, ringColor, progressColor, 14, 16, 2, percent);
                        }
                    }
                }
        ));

        register(
                "molten_battle_axe",
                Items.DIAMOND_AXE,
                "Throw-Axe",
                20 * 90,
                50,
                50,
                TriggerType.RIGHT_CLICK
        );

        register(
                "ice_bow",
                Items.BOW,
                "Slow Arrow",
                20 * 120,
                100,
                100,
                TriggerType.SHOOT_BOW
        );
    }

    private static void register(String id, Item item, String abilityName, int cooldown, int x, int y, TriggerType trigger) {
        Ability ability = new Ability(
                id,
                new ItemStack(item),
                abilityName,
                cooldown,
                x,
                y,
                trigger
        );

        ability.hudX = KingdomMod.config.getAbilityHudX(id, x);
        ability.hudY = KingdomMod.config.getAbilityHudY(id, y);

        addAbility(ability);
    }

    public static void tickAll() {
        for (Ability ability : ABILITIES) {
            ability.tick();
        }
    }

    public static void tryActivate(MinecraftClient client, ItemStack held, boolean leftClickPressed, boolean rightClickPressed) {
        if (client.player == null) return;

        for (Ability ability : ABILITIES) {
            switch (ability.trigger) {
                case LEFT_CLICK -> {
                    if (leftClickPressed && hasAbility(held, ability.abilityName)) {
                        ability.tryActivate();
                    }
                }
                case RIGHT_CLICK -> {
                    if (rightClickPressed && held != null
                            && hasAbility(held, ability.abilityName)) {
                        ability.tryActivate();
                    }
                }
                case SNEAK -> {
                    boolean hasAbilityArmor = false;
                    for (ItemStack armorStack : client.player.getInventory().armor) {
                        if (armorStack != null && hasAbility(armorStack, ability.abilityName)) {
                            hasAbilityArmor = true;
                            break;
                        }
                    }

                    if (client.options.sneakKey.isPressed() && hasAbilityArmor) {
                        ability.tryActivate();
                    }
                }
            }
        }
    }
}