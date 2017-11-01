#!/usr/bin/env bash
mvn clean package \
&& mv target/tao-db-1.0-SNAPSHOT-jar-with-dependencies.jar src/main/deb/usr/lib/taodb/taodb.jar \
&& cd src/main/deb/ \
&& dpkg -b ./ taodb.deb


