# TaoDB
Database for [Event Sourcing](https://www.google.de/search?q=event+sourcing)
## Features
- Append only / Immutable
- Ordered Events
- Accessible via [gRPC](https://grpc.io). See [TaoDB.proto](https://github.com/puddingspudding/taoDB/blob/master/src/main/proto/).
- Replication

## Design
In event sourcing, the requirements for the event store are to be append only and and able to provide all events since a specific timestamp or after a specfic event.

### File
TaoDB appends incomming events, protobuf encoded, at the end of a file.
In order to find all events after a given event id or since a timestamp an index (map from timestamp to file position) is held. The index is updated every second.

### Event
An event consists of an Id and data (arbitrary bytes). The Id is a combination of timestamp (in seconds) and an [UUID](https://en.wikipedia.org/wiki/Universally_unique_identifier).


## ToDo
- Deployment
  - Start/Stop
  - Configuration
  - Error Logging


## Build
```
mvn clean package
```

## Run Master
```
java \
    -Dport=7777 \
    -Dfile=mydata.db \
    -cp target/tao-db-1.0-SNAPSHOT-jar-with-dependencies.jar com.github.puddingspudding.taodb.TaoService
```

## Run Replication
```
java \
    -Dport=7778 \
    -Dfile=replicated_mydata.db \
    -DmasterHost=127.0.0.1 \
    -DmasterPort=7777 \
    -cp target/tao-db-1.0-SNAPSHOT-jar-with-dependencies.jar com.github.puddingspudding.taodb.TaoReplicationService
```