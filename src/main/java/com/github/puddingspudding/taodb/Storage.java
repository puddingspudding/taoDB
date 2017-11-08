package com.github.puddingspudding.taodb;

import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import io.netty.util.concurrent.CompleteFuture;

import java.util.Optional;
import java.util.function.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Storage.
 */
interface Storage {

    void add(Event event, Consumer<Event> onNext, Runnable onEnd, Consumer<Throwable> onError);

    void get(EventId eventId, Consumer<Event> onNext, Runnable onEnd, Consumer<Throwable> onError);

    void get(Timestamp timestamp, Consumer<Event> onNext, Runnable onEnd, Consumer<Throwable> onError);

    /**
     * Returns single event for given event id.
     *
     * @param eventId event id
     * @param onNext called if event was found
     * @param onError called if event not found or any other error
     */
    void get(EventId eventId, Consumer<Event> onNext, Consumer<Throwable> onError);

    /**
     * @return latest written event id.
     */
    Optional<EventId> latestEventId();

}
