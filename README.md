# [WIP] TaoDB
Database for [Event Sourcing](https://www.google.de/search?q=event+sourcing)
## Features
- Append only / Immutable
- Ordered Events
- Accessible via [gRPC](https://grpc.io). See [TaoDB.proto](https://github.com/puddingspudding/taoDB/blob/master/src/main/proto/).
- Replication

## Requirements
- Java 9

## Design
In event sourcing, the requirements for the event store are to be append only and and able to provide all events since a specific timestamp or after a specfic event.

### File
TaoDB appends incomming events, protobuf encoded, at the end of a file.
In order to find all events after a given event id or since a timestamp an index (map from timestamp to file position) is held. The index is updated every second.

### Event
An event consists of an Id and data (arbitrary bytes). The Id is a combination of timestamp (in seconds) and an [UUID](https://en.wikipedia.org/wiki/Universally_unique_identifier).


## ToDo
- Deployment
  - Package
  - ~Start/Stop~
  - Configuration (in progress)
  - Error Logging


# Run

## Status
```
$ taodb status
```

![taodb status]
(https://github.com/puddingspudding/taoDB/tree/master/docs/status.png)

## Start/Stop
```
$ taodb start service1
```
```
$ taodb stop service1
```



## Config
```
# Services
taodb.services=service1,service2

# Replication Services
taodb.replication.services=service1repli,service2repli

# Service 1
service1.network.port=6666
service1.storage.path=/var/lib/taodb/service1.db

# Service 2
service2.network.port=7777
service2.storage.path=/var/lib/taodb/service2.db

# Server 1 Replication
service1repli.network.port=6667
service1repli.storage.path=/var/lib/taodb/service1_repli.db
service1repli.master.host=127.0.0.1
service1repli.master.port=6666

# Service 2 Replication
service2repli.network.port=7778
service2repli.storage.path=/var/lib/taodb/service2_repli.db
service2repli.master.host=127.0.0.1
service2repli.master.port=7777
```

## Build
```
mvn clean package
```

