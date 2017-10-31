package com.github.puddingspudding.taodb;

import com.google.protobuf.Timestamp;

import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * TaoReplicationService.
 */
public class TaoReplicationService extends ReplicatedEventStoreServiceGrpc.ReplicatedEventStoreServiceImplBase {

    public static void main(String[] args) throws Exception {
        int port = Integer.valueOf(System.getProperty("port"));
        Path file = Paths.get(System.getProperty("file"));

        String replicationHost = System.getProperty("masterHost");
        int replicationPort = Integer.valueOf(System.getProperty("masterPort"));

        TaoReplicationService service = new TaoReplicationService(replicationHost, replicationPort, file);

        Server server = ServerBuilder
            .forPort(port)
            .addService(service)
            .build()
            .start();

        System.out.println("TaoReplicationService started");
        server
            .awaitTermination();
    }

    private final Storage storage;

    private volatile EventStoreServiceGrpc.EventStoreServiceBlockingStub stub;
    private final Supplier<EventStoreServiceGrpc.EventStoreServiceBlockingStub> stubFactory;

    public TaoReplicationService(String host, int port, Path file) throws Exception {
        this.storage = new IndexedProtobufFileStorage(file);

        this.stubFactory = () -> {
            return EventStoreServiceGrpc.newBlockingStub(
                ManagedChannelBuilder
                    .forAddress(host, port)
                    .usePlaintext(true)
                    .build()
            ).withWaitForReady();
        };
        this.stub = stubFactory.get();

        Executors
            .newSingleThreadScheduledExecutor()
            .scheduleAtFixedRate(this::run, 1, 1, TimeUnit.SECONDS);

    }

    private void run() {
        try {
            Optional<EventId> eventId = storage.latestEventId();

            Iterator<Event> events = eventId
                .map(this.stub::getById)
                .orElseGet(() -> this.stub.getByTimestamp(Timestamp.getDefaultInstance()));
            while (events.hasNext()) {
                storage.add(
                    events.next(),
                    empty -> {},
                    () -> {},
                    Throwable::printStackTrace
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.stubFactory.get();
        }
    }


    @Override
    public void getById(EventId eventId, StreamObserver<Event> responseObserver) {
        this.storage.get(
            eventId,
            responseObserver::onNext,
            responseObserver::onCompleted,
            responseObserver::onError
        );
    }

    @Override
    public void getByTimestamp(Timestamp timestamp, StreamObserver<Event> responseObserver) {
        this.storage.get(
            timestamp,
            responseObserver::onNext,
            responseObserver::onCompleted,
            responseObserver::onError
        );
    }
}
