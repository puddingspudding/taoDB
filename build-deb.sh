#!/usr/bin/env bash
mvn clean package \
&& mkdir -p /home/travis/build/puddingspudding/taoDB/src/main/deb/usr/lib/taodb \
&& mv /home/travis/build/puddingspudding/taoDB/target/taodb-*-jar-with-dependencies.jar /home/travis/build/puddingspudding/taoDB/src/main/deb/usr/lib/taodb/taodb.jar \
&& cd /home/travis/build/puddingspudding/taoDB/src/main/deb/ \
&& dpkg -b ./ taodb.deb


