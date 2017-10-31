package com.github.puddingspudding.taodb;

import com.google.protobuf.BoolValue;
import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.time.Instant;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by pudding on 31.10.17.
 */
public class Producer {


    public static void main(String[] args) throws Exception {

        int sleep = Integer.valueOf(System.getProperty("sleep"));
        int threads = Integer.valueOf(System.getProperty("threads"));
        int bytes = Integer.valueOf(System.getProperty("bytes"));


        AtomicLong counter = new AtomicLong(0);

        Executors
            .newSingleThreadScheduledExecutor()
            .scheduleAtFixedRate(() -> {
                System.out.println(counter.getAndSet(0));
            }, 0, 1, TimeUnit.SECONDS);

        for (int x = 0; x < threads; x++) {
            new Thread(() -> {
                try {
                    EventStoreServiceGrpc.EventStoreServiceBlockingStub service = EventStoreServiceGrpc.newBlockingStub(
                        ManagedChannelBuilder.forAddress("192.168.0.143", 7777).usePlaintext(true).build()
                    ).withWaitForReady();
                    while (true) {
                        EventId eventId = EventId
                            .newBuilder()
                            .setUuid(UUID.randomUUID().toString())
                            .setTimestamp(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()).build())
                            .build();
                        Event event = Event
                            .newBuilder()
                            .setId(eventId)
                            .setData(ByteString.copyFrom(new byte[bytes]))
                            .build();
                        service.add(event);
                        counter.incrementAndGet();
                        Thread.sleep(sleep);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }


    }

}
