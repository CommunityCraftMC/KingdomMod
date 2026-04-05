package dev.stephan.kingdommod.waypoint;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

import java.io.Serializable;

public record Waypoint(GameProfile player, BlockPos pos) implements Serializable {

    private static final MinecraftClient client = MinecraftClient.getInstance();

    public String getPlayerName() {
        return player.getName();
    }

    public int getX() {
        if (client.player == null) return 0;
        return (int) (pos.getX() / client.player.getWorld().getDimension().coordinateScale());
    }

    public int getZ() {
        if (client.player == null) return 0;
        return (int) (pos.getZ() / client.player.getWorld().getDimension().coordinateScale());
    }

    public int getY() {
        return pos.getY();
    }

}
