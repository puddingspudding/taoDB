# TaoDB
Database for Event Sourcing accessible via gRPC.

## Features
- 

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