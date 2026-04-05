package dev.stephan.kingdommod.Handlers;

import com.mojang.brigadier.arguments.StringArgumentType;

import dev.stephan.kingdommod.waypoint.FollowManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;

import java.util.Collections;
import java.util.List;

public class CommandHandler {

    public void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {

            dispatcher.register(ClientCommandManager.literal("follow")
                            .then(ClientCommandManager.argument("player", StringArgumentType.word())
                                    .suggests((context, builder) -> {
                                        MinecraftClient client = MinecraftClient.getInstance();
                                        if (client.getNetworkHandler() != null) {
                                            List<String> playerNames = client.getNetworkHandler()
                                                    .getPlayerList()
                                                    .stream()
                                                    .map(entry -> entry.getProfile().getName())
                                                    .toList();
                                            return CommandSource.suggestMatching(playerNames, builder);
                                        }
                                        return CommandSource.suggestMatching(Collections.emptyList(), builder);
                                    })
                                    .executes(context -> {
                                        String ign = StringArgumentType.getString(context, "player");
                                        FollowManager.followPlayer(ign);
                                        return 1;
                                    })
                            )
            );


            dispatcher.register(ClientCommandManager.literal("unfollow")
                            .then(ClientCommandManager.argument("ign", StringArgumentType.string())
                                    .suggests((context, builder) -> {
                                        FollowManager.getFollowers().forEach(builder::suggest);
                                        return builder.buildFuture();
                                    })
                                    .executes(context -> {
                                        String ign = StringArgumentType.getString(context, "ign");
                                        FollowManager.unfollowPlayer(ign);
                                        return 1;
                                    })
                            )
            );

            dispatcher.register(ClientCommandManager.literal("unfollowall")
                    .executes(context -> {
                        FollowManager.unfollowAllPlayers();
                        return 1;
                    }));
        });
    }
}
