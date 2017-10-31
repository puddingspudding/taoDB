package com.github.puddingspudding.taodb;

import com.google.protobuf.Timestamp;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.time.Instant;
import java.util.Iterator;
import java.util.UUID;

/**
 * Created by pudding on 31.10.17.
 */
public class Consumer {

    public static void main(String[] args) throws Exception {


        for (int x = 0; x < Integer.valueOf(args[0]); x++) {
            Thread.sleep(5);
            new Thread(() -> {
                ReplicatedEventStoreServiceGrpc.ReplicatedEventStoreServiceBlockingStub service = ReplicatedEventStoreServiceGrpc.newBlockingStub(
                    ManagedChannelBuilder.forAddress("192.168.0.143", 7778).usePlaintext(true).build()
                );
                try {
                    Event latestEvent = null;
                    while (latestEvent == null) {

                        Timestamp timestamp = Timestamp
                            .newBuilder()
                            .setSeconds(Instant.now().getEpochSecond()
                            ).build();
                        Iterator<Event> iterator = service.getByTimestamp(timestamp);

                        while (iterator.hasNext()) {
                            latestEvent = iterator.next();
                            System.out.println(latestEvent);
                        }
                    }

                    while (true) {
                        Iterator<Event> it = service.getById(latestEvent.getId());
                        while (it.hasNext()) {
                            latestEvent = it.next();
                            System.out.println(latestEvent);
                        }
                        Thread.sleep(1000);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

        }

        Thread.sleep(10000000);

    }

}
