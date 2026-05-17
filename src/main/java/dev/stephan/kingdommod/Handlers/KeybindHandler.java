package dev.stephan.kingdommod.Handlers;

import dev.stephan.kingdommod.config.HudEditScreen;
import dev.stephan.kingdommod.waypoint.WaypointRenderer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeybindHandler {

    private final KeyBinding sendCoordsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("Coords in chat", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "CommunityCraft"));
    private final KeyBinding clearWaypointsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("Remove all heads", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "CommunityCraft"));
    private final KeyBinding editHudKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("Open HUD Gui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "CommunityCraft"));

    public void registerKeybinds() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(MinecraftClient client) {
        while (sendCoordsKey.wasPressed()) CoordinateHandler.sendCoordinates();

        while (clearWaypointsKey.wasPressed()) WaypointRenderer.followWaypoints.clear();

        while (editHudKey.wasPressed()) client.setScreen(new HudEditScreen());
    }
}
