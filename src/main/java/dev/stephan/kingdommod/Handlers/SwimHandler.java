package dev.stephan.kingdommod.Handlers;

import dev.stephan.kingdommod.KingdomMod;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;

public class SwimHandler {

    public void registerSwimListener() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(MinecraftClient client) {
        ClientWorld world = client.world;
        if (world == null || !KingdomMod.config.SwimAnimation) return;

        ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();
        String serverAddress = networkHandler != null && networkHandler.getServerInfo() != null
                ? networkHandler.getServerInfo().address
                : "localhost";

        if (serverAddress != null && (serverAddress.endsWith("communitycraft.nl") || "localhost".equalsIgnoreCase(serverAddress))) {
            disableSwimming(world);
        }
    }

    private static void disableSwimming(ClientWorld world) {
        for (PlayerEntity player : world.getPlayers()) {
            if (player.isSwimming()) {
                player.setSwimming(false);
                player.setPose(EntityPose.STANDING);
            }
        }
    }
}
