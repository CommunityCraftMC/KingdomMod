package dev.stephan.kingdommod.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.HashMap;
import java.util.Map;

@Config(name = "kingdommod")
public class KingdomModConfig implements ConfigData {

    public boolean SwimAnimation = true;
    public boolean ShowRepairCost = false;
    public boolean ShowAbilityCooldown = false;
    public boolean AutoFollow = false;
    @ConfigEntry.BoundedDiscrete(min = 10, max = 500)
    public int FollowWaypointScale = 100;

    @ConfigEntry.Gui.Excluded
    public Map<String, HudPos> abilityHudPositions = new HashMap<>();

    public int getAbilityHudX(String id, int fallback) {
        return abilityHudPositions
                .getOrDefault(id, new HudPos(fallback, fallback))
                .x;
    }

    public int getAbilityHudY(String id, int fallback) {
        return abilityHudPositions
                .getOrDefault(id, new HudPos(fallback, fallback))
                .y;
    }

    public void setAbilityHud(String id, int x, int y) {
        abilityHudPositions.put(id, new HudPos(x, y));
    }

    public static class HudPos {
        public int x;
        public int y;

        public HudPos(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
