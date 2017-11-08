package com.github.puddingspudding.taodb;

import com.google.protobuf.ByteString;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 16 bytes.
 */
public final class EventIdUtil {

    public static EventId create() {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(Instant.now().getEpochSecond());
        buffer.putLong(ThreadLocalRandom.current().nextLong());
        return EventId
            .newBuilder()
            .setId(ByteString.copyFrom(buffer.array()))
            .build();
    }

    public static long getTimestamp(EventId eventId) {
        return eventId.getId().asReadOnlyByteBuffer().getLong();
    }

    public static String toString(EventId eventId) {
        ByteBuffer buffer = eventId.getId().asReadOnlyByteBuffer();
        return Long.toHexString(buffer.getLong()) + "-" + Long.toHexString(buffer.getLong());
    }

    public static EventId toEventId(String eventIdString) {
        String[] split = eventIdString.split("-");

        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(new BigInteger(split[0], 16).longValue());
        buffer.putLong(new BigInteger(split[1], 16).longValue());
        return EventId
            .newBuilder()
            .setId(ByteString.copyFrom(buffer.array()))
            .build();
    }

}
