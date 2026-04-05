package dev.stephan.kingdommod.Handlers;

import com.mojang.authlib.GameProfile;
import dev.stephan.kingdommod.KingdomMod;
import dev.stephan.kingdommod.waypoint.FollowManager;
import dev.stephan.kingdommod.waypoint.Waypoint;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CoordinateHandler {

    private static final Pattern COORD_PATTERN = Pattern.compile("\\[x:\\s*(-?\\d+),\\s*y:\\s*(-?\\d+),\\s*z:\\s*(-?\\d+)]");

    public void register() {
        ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signedMessage, sender, params, time) -> {
            handleCoordinateMessage(message);
            return true;
        });

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> handleCoordinateMessage(message));
    }

    public static void handleCoordinateMessage(Text message) {
        String raw = message.getString();
        Matcher matcher = COORD_PATTERN.matcher(raw);
        if (!matcher.find()) return;

        int x = Integer.parseInt(matcher.group(1));
        int y = Integer.parseInt(matcher.group(2));
        int z = Integer.parseInt(matcher.group(3));

        String name = extractIGN(raw);
        if (name == null) return;

        GameProfile profile = getProfile(name);
        if (FollowManager.isFollowing(name) || KingdomMod.config.AutoFollow) {
            FollowManager.addFollowWaypoint(new Waypoint(profile, new BlockPos(x, y, z)));
        }
    }

    public static String extractIGN(String messageString) {
        Pattern newFormatPattern = Pattern.compile("\\(To\\s+([A-Za-z0-9_]+)\\)");
        Matcher newFormatMatcher = newFormatPattern.matcher(messageString);
        if (newFormatMatcher.find()) return newFormatMatcher.group(1);

        if (messageString.contains("->")) {
            String[] parts = messageString.split("->");
            if (parts.length < 2) return null;
            return parts[0].replace("[PRIVATE]", "").trim().split(" ")[0].trim();
        } else {
            Pattern ignPattern = Pattern.compile("(?:\\[.*?])*\\s*([A-Za-z0-9_]+):");
            Matcher matcher = ignPattern.matcher(messageString);
            if (matcher.find()) return matcher.group(1);
        }
        return null;
    }

    public static GameProfile getProfile(String name) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();

        if (networkHandler != null) {
            for (PlayerListEntry entry : networkHandler.getPlayerList()) {
                if (entry.getProfile().getName().equalsIgnoreCase(name)) return entry.getProfile();
            }
        }
        return new GameProfile(Uuids.getOfflinePlayerUuid(name), name);
    }

    public static void sendCoordinates() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        double x = client.player.getX();
        double y = client.player.getY();
        double z = client.player.getZ();
        String message = String.format("[x:%d, y:%d, z:%d]", Math.round(x), Math.round(y), Math.round(z));
        client.player.networkHandler.sendChatMessage(message);
    }
}
