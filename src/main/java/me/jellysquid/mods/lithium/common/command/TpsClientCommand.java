package me.jellysquid.mods.lithium.common.command;

import com.mojang.brigadier.CommandDispatcher;
import me.jellysquid.mods.lithium.common.LithiumMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LithiumMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class TpsClientCommand {

    private TpsClientCommand() {
    }

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("tps")
                .executes(context -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();
                    if (networkHandler != null) {
                        networkHandler.sendChatCommand("tps");
                    } else if (client.player != null) {
                        client.player.sendMessage(Text.literal("TPS command is not available right now."), false);
                    }
                    return 1;
                }));
    }
}
