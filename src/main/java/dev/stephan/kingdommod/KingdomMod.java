package dev.stephan.kingdommod;

import dev.stephan.kingdommod.Handlers.*;
import dev.stephan.kingdommod.ability.AbilityManager;
import dev.stephan.kingdommod.config.KingdomModConfig;
import dev.stephan.kingdommod.waypoint.WaypointRenderer;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;

public class KingdomMod implements ClientModInitializer {

    public static KingdomModConfig config;

    @Override
    public void onInitializeClient() {
        AutoConfig.register(KingdomModConfig.class, GsonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(KingdomModConfig.class).getConfig();

        AbilityManager.RegisterAbility();
        WaypointRenderer.init();

        new CommandHandler().registerCommands();
        new KeybindHandler().registerKeybinds();
        new SwimHandler().registerSwimListener();
        new CoordinateHandler().register();
        new TooltipHandler().registerRepairTooltip();
    }
}
