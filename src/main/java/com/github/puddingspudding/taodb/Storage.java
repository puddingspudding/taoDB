package com.github.puddingspudding.taodb;

import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import io.netty.util.concurrent.CompleteFuture;

import java.util.Optional;
import java.util.function.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Created by pudding on 31.10.17.
 */
interface Storage {

    void add(Event event, Consumer<Empty> onNext, Runnable onEnd, Consumer<Throwable> onError);

    void get(EventId eventId, Consumer<Event> onNext, Runnable onEnd, Consumer<Throwable> onError);

    void get(Timestamp timestamp, Consumer<Event> onNext, Runnable onEnd, Consumer<Throwable> onError);

    Optional<EventId> latestEventId();

}
