package com.github.puddingspudding.taodb;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;
import java.util.function.Consumer;

/**
 * IndexedProtobufFileStorage.
 */
public class IndexedProtobufFileStorage implements Storage {

    private static final int READ_CHANNELS = 1000;

    private final Path file;
    private final SortedMap<Long, Long> index = new ConcurrentSkipListMap<>(Comparator.naturalOrder());
    private final FileChannel writeFileChannel;
    private final Queue<FileChannel> readFileChannels = new ArrayBlockingQueue<>(READ_CHANNELS);
    private final FileChannel indexFileChannel;
    private volatile EventId latestEvenId;

    public IndexedProtobufFileStorage(Path file) throws Exception {
        this.file = file;
        this.writeFileChannel = FileChannel.open(this.file, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        this.indexFileChannel = FileChannel.open(file, StandardOpenOption.READ);

        updateIndex(true);

        for (int x = 0; x < READ_CHANNELS; x++) {
            readFileChannels.add(FileChannel.open(this.file, StandardOpenOption.READ));
        }

        Executors
            .newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r);
                t.setDaemon(true);
                t.setName("taodb.index.updater");
                return t;
            })
            .scheduleAtFixedRate(this::updateIndex, 1, 1, TimeUnit.SECONDS);
    }

    private void updateIndex() {
        this.updateIndex(false);
    }

    private void updateIndex(boolean updateLatestEventId) {
        try {
            InputStream inputStream = Channels.newInputStream(indexFileChannel);
            while (inputStream.available() > 0) {
                long pos = indexFileChannel.position();
                Event event = Event.parseDelimitedFrom(inputStream);
                if (updateLatestEventId) {
                    this.latestEvenId = event.getId();
                }
                index.putIfAbsent(event.getId().getTimestamp().getSeconds(), pos);
            }
        } catch (Exception e) {
            // do error stuff
            e.printStackTrace();
        }
    }

    @Override
    public void add(Event event, Consumer<Event> onNext, Runnable onEnd, Consumer<Throwable> onError) {
        try {
            long pos = this.writeFileChannel.position();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            event.writeDelimitedTo(baos);
            this.writeFileChannel.write(
                ByteBuffer.wrap(baos.toByteArray())
            );
            this.latestEvenId = event.getId();
            this.index.putIfAbsent(event.getId().getTimestamp().getSeconds(), pos);
            onNext.accept(event);
            onEnd.run();
        } catch (Exception e) {
            e.printStackTrace();
            onError.accept(e);
        }
    }

    @Override
    public void get(EventId eventId, Consumer<Event> onNext, Runnable onEnd, Consumer<Throwable> onError) {
        FileChannel fileChannel = null;
        while ((fileChannel = readFileChannels.poll()) == null) {
            try {
                System.out.println("no channel available. Sleeping...");
                Thread.sleep(100);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            long pos = Math.max(0, getPositionFromTimestamp(index, eventId.getTimestamp().getSeconds()));
            fileChannel.position(pos);
            long maxPos = fileChannel.size();
            InputStream inputStream = Channels.newInputStream(fileChannel);
            boolean found = false;
            while (fileChannel.position() < maxPos) {
                Event event = Event.parseDelimitedFrom(inputStream);
                if (event.getId().getUuid().equals(eventId.getUuid())) {
                    found = true;
                } else if (found) {
                    onNext.accept(event);
                }
            }
            onEnd.run();
        } catch (Exception e) {
            e.printStackTrace();
            onError.accept(e);
        } finally {
            this.readFileChannels.offer(fileChannel);
        }
    }

    @Override
    public void get(Timestamp timestamp, Consumer<Event> onNext, Runnable onEnd, Consumer<Throwable> onError) {
        FileChannel fileChannel = null;
        while ((fileChannel = readFileChannels.poll()) == null) {
            try {
                System.out.println("no channel available. Sleeping...");
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            long pos = getPositionFromTimestamp(index, timestamp.getSeconds());
            if (pos != -1) {
                fileChannel.position(pos);
                long maxPos = fileChannel.size();
                InputStream inputStream = Channels.newInputStream(fileChannel);
                while (fileChannel.position() < maxPos) {
                    Event event = Event.parseDelimitedFrom(inputStream);
                    if (event.getId().getTimestamp().getSeconds() >= timestamp.getSeconds()) {
                        onNext.accept(event);
                    }
                }
            }
            onEnd.run();
        } catch (Exception e) {
            e.printStackTrace();
            onError.accept(e);
        } finally {
            this.readFileChannels.offer(fileChannel);
        }
    }

    @Override
    public Optional<EventId> latestEventId() {
        return Optional.ofNullable(this.latestEvenId);
    }

    private static long getPositionFromTimestamp(SortedMap<Long, Long> index, long timestamp) {
        long position = 0;
        for (Map.Entry<Long, Long> e : index.entrySet()) {
            if (e.getKey() > timestamp) {
                break;
            }
            position = e.getValue();
        }
        return position;
    }

}
