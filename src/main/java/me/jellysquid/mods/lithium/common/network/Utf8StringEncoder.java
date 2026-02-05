package me.jellysquid.mods.lithium.common.network;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;

final class Utf8StringEncoder {
    private static final ThreadLocal<CharsetEncoder> ENCODER = ThreadLocal.withInitial(() ->
            StandardCharsets.UTF_8.newEncoder());
    private static final ThreadLocal<byte[]> BUFFER = ThreadLocal.withInitial(() -> new byte[256]);

    private Utf8StringEncoder() {
    }

    static ByteBuffer encode(String value) {
        CharsetEncoder encoder = ENCODER.get();
        int maxBytes = (int) Math.ceil(encoder.maxBytesPerChar() * value.length());
        byte[] buffer = BUFFER.get();
        if (buffer.length < maxBytes) {
            int newSize = Math.max(maxBytes, buffer.length * 2);
            buffer = new byte[newSize];
            BUFFER.set(buffer);
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
        byteBuffer.clear();

        encoder.reset();
        encoder.encode(CharBuffer.wrap(value), byteBuffer, true);
        encoder.flush(byteBuffer);

        byteBuffer.flip();
        return byteBuffer;
    }
}