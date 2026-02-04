package me.jellysquid.mods.lithium.common.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import me.jellysquid.mods.lithium.common.LithiumMod;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;

@Mod.EventBusSubscriber(modid = LithiumMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class TpsCommand {

    private TpsCommand() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("tps")
                .requires(source -> source.hasPermissionLevel(0))
                .executes(context -> {
                    MinecraftServer server = context.getSource().getServer();
                    double mspt = getAverageMspt(server);
                    double tps = Math.min(20.0, 1000.0 / Math.max(mspt, 1.0));
                    String message = String.format(Locale.ROOT, "TPS: %.2f (MSPT: %.2f)", tps, mspt);
                    context.getSource().sendFeedback(() -> Text.literal(message), false);
                    return 1;
                }));
    }

    private static double getAverageMspt(MinecraftServer server) {
        try {
            Method method = server.getClass().getMethod("getAverageTickTime");
            Object value = method.invoke(server);
            if (value instanceof Number number) {
                return number.doubleValue();
            }
        } catch (ReflectiveOperationException ignored) {
            // Fall through to field access.
        }

        long[] tickTimes = getTickTimes(server, "tickTimes");
        if (tickTimes.length == 0) {
            tickTimes = getTickTimes(server, "recentTickTimes");
        }
        if (tickTimes.length == 0) {
            return 50.0;
        }

        long total = 0L;
        int count = 0;
        for (long time : tickTimes) {
            if (time > 0L) {
                total += time;
                count++;
            }
        }
        if (count == 0) {
            return 50.0;
        }

        // tickTimes are in nanoseconds in Mojang server.
        return (total / (double) count) / 1_000_000.0;
    }

    private static long[] getTickTimes(MinecraftServer server, String fieldName) {
        try {
            Field field = server.getClass().getDeclaredField(fieldName);
            if (!field.canAccess(server)) {
                return new long[0];
            }
            Object value = field.get(server);
            if (value instanceof long[] longArray) {
                return longArray;
            }
        } catch (ReflectiveOperationException ignored) {
            // ignore
        }
        return new long[0];
    }
}
