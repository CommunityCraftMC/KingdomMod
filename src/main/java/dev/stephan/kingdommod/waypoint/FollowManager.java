package dev.stephan.kingdommod.waypoint;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class FollowManager {

    private static final List<String> followers = new ArrayList<>();

    public static void followPlayer(String ign) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        followers.add(ign);
        client.player.sendMessage(Text.literal("Now following: " + ign).formatted(Formatting.GREEN), false);
    }

    public static void unfollowPlayer(String ign) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        followers.remove(ign);
        WaypointRenderer.followWaypoints.removeIf(wp -> wp.getPlayerName().equals(ign));
        client.player.sendMessage(Text.literal("Stopped following: " + ign).formatted(Formatting.GREEN), false);
    }

    public static void unfollowAllPlayers() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        followers.clear();
        WaypointRenderer.followWaypoints.clear();
        client.player.sendMessage(Text.literal("Cleared following list").formatted(Formatting.GREEN), false);
    }

    public static boolean isFollowing(String ign) {
        return followers.contains(ign);
    }

    public static List<String> getFollowers() {
        return followers;
    }

    public static void addFollowWaypoint(Waypoint waypoint) {
        boolean found = false;
        for (int i = 0; i < WaypointRenderer.followWaypoints.size(); i++) {
            Waypoint existing = WaypointRenderer.followWaypoints.get(i);
            if (existing.getPlayerName().equals(waypoint.getPlayerName())) {
                WaypointRenderer.followWaypoints.set(i, waypoint);
                found = true;
                break;
            }
        }
        if (!found) WaypointRenderer.followWaypoints.add(waypoint);
    }
}
