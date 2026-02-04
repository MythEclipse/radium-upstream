package me.jellysquid.mods.lithium.mixin.network;

import io.netty.buffer.ByteBuf;
import me.jellysquid.mods.lithium.common.network.LinearPacketBuffer;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(PacketByteBuf.class)
public abstract class PacketByteBufMixin extends ByteBuf {

    /**
     * @author Antigravity
     * @reason Redirect to optimized VarInt implementation.
     */
    @Overwrite
    public PacketByteBuf writeVarInt(int value) {
        LinearPacketBuffer.writeVarInt(this, value);
        return (PacketByteBuf) (Object) this;
    }

    /**
     * @author Antigravity
     * @reason Redirect to optimized String implementation.
     */
    @Overwrite
    public PacketByteBuf writeString(String string, int maxLength) {
        LinearPacketBuffer.writeString((PacketByteBuf) (Object) this, string, maxLength);
        return (PacketByteBuf) (Object) this;
    }
}
