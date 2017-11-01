#!/usr/bin/env bash
mvn clean package \
&& mkdir -p /home/travis/build/puddingspudding/taoDB/src/main/deb/usr/lib/ \
&& mv /home/travis/build/puddingspudding/taoDB/target/tao-db-1.0-SNAPSHOT-jar-with-dependencies.jar /home/travis/build/puddingspudding/taoDB/src/main/deb/usr/lib/taodb/taodb.jar \
&& cd /home/travis/build/puddingspudding/taoDB/src/main/deb/ \
&& dpkg -b ./ taodb.deb


