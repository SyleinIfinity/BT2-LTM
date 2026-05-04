#!/usr/bin/env bash
# PACK: Build and run server quickly (Linux/Mac)
mvn -f server/pom.xml clean package -DskipTests
mvn -f server/pom.xml exec:java -Dexec.mainClass=server.ServerMain
