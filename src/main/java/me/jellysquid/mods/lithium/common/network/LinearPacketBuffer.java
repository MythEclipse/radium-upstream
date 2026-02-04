package me.jellysquid.mods.lithium.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketByteBuf;

/**
 * LinearPacketBuffer provides a more efficient way to write common Minecraft
 * data types
 * by reducing the overhead of Netty's buffer checks.
 */
public class LinearPacketBuffer {

    private LinearPacketBuffer() {
    }

    /**
     * Efficiently writes a VarInt to a buffer.
     * This is a hot path for every Minecraft packet.
     */
    public static void writeVarInt(ByteBuf buf, int value) {
        while ((value & -128) != 0) {
            buf.writeByte(value & 127 | 128);
            value >>>= 7;
        }
        buf.writeByte(value);
    }

    /**
     * Optimized string writing for short strings (common in packets).
     */
    public static void writeString(PacketByteBuf buf, String string, int maxLength) {
        if (string.length() > maxLength) {
            throw new IllegalArgumentException(
                    "String too big (was " + string.length() + " characters, max " + maxLength + ")");
        } else {
            byte[] bytes = string.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            writeVarInt(buf, bytes.length);
            buf.writeBytes(bytes);
        }
    }
}
