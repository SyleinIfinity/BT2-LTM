# BT2-LTM

## Quick Run Cheat Sheet

```powershell
# 1) Build server
mvn -f server/pom.xml clean package -DskipTests

# 2) Run server (terminal A)
mvn -f server/pom.xml exec:java

# 3) Build client
mvn -f client/pom.xml clean package -DskipTests

# 4) Run client (terminal B)
mvn -f client/pom.xml javafx:run
```

Notes:

- Start server first, then run client.
- Default endpoint: `127.0.0.1:5555`.
- If JavaFX Maven run fails, run `client.app.MainApp` directly from IDE.

Packaging & Quick Run

- Use `run.sh` (Linux/Mac) or `run.bat` (Windows) to build and run the server quickly.
- Configuration is in `config/server.properties` (port, limits, upload/log dirs).
- Logs are written to `logs/server.log` by default.
