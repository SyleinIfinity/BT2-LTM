@echo off
REM PACK: Build and run server quickly (Windows)
mvn -f server\pom.xml clean package -DskipTests
mvn -f server\pom.xml exec:java -Dexec.mainClass=server.ServerMain
pause
