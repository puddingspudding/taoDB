package com.github.puddingspudding.taodb;

import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * TaoService.
 */
public class TaoService extends EventStoreServiceGrpc.EventStoreServiceImplBase {


    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getProperty("port"));
        String file = System.getProperty("file");

        TaoService service = new TaoService(
            Paths.get(file)
        );

        Server server = ServerBuilder
            .forPort(port)
            .addService(service)
            .build()
            .start();

        System.out.println("TaoService started");
        server
            .awaitTermination();
    }

    private final Storage storage ;
    public TaoService(Path file) throws Exception {

        this.storage = new IndexedProtobufFileStorage(file);
    }

    @Override
    public void add(Event event, StreamObserver<Event> responseObserver) {
        this.storage.add(event, responseObserver::onNext, responseObserver::onCompleted, responseObserver::onError);

    }

    @Override
    public void getById(EventId eventId, StreamObserver<Event> responseObserver) {
        this.storage.get(eventId, responseObserver::onNext, responseObserver::onCompleted, responseObserver::onError);
    }

    @Override
    public void getByTimestamp(Timestamp timestamp, StreamObserver<Event> responseObserver) {
        this.storage.get(timestamp, responseObserver::onNext, responseObserver::onCompleted, responseObserver::onError);
    }


}
