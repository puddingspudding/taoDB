package com.github.puddingspudding.taodb;

import java.util.function.Consumer;

/**
 * Just a helper for grpcs StreamObserver to enable lambdas. XOXO
 */
public class StreamObserver {

    public static <T> io.grpc.stub.StreamObserver<T> create(
        Consumer<T> onNext,
        Runnable onEnd,
        Consumer<Throwable> onError
    ) {
        return new io.grpc.stub.StreamObserver<T>() {
            @Override
            public void onNext(T t) {
                onNext.accept(t);
            }

            @Override
            public void onError(Throwable throwable) {
                onError.accept(throwable);
            }

            @Override
            public void onCompleted() {
                onEnd.run();
            }
        };
    }

}
