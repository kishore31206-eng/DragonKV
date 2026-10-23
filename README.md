
# DragonKV
### A High-Performance In-Memory Key-Value Database Using Java

DragonKV is a small educational key-value database written in Java. It keeps data in memory for fast access and supports optional expiration times and file persistence.

> DragonKV is an independent learning project inspired by in-memory databases. It is not the official DragonflyDB software and does not claim performance parity with production databases.

## Features

- `SET` — store or replace a key
- `GET` — retrieve a value
- `DELETE` — remove a key
- `EXISTS` — check whether a key exists
- `KEYS` — list stored keys
- `TTL` — set a key's lifetime in seconds
- `SAVE` — save current data to a local file
- `HELP` and `EXIT`
- Thread-safe in-memory storage using `ConcurrentHashMap`
- Persistence using a simple Base64-encoded text file
- Unit tests with JUnit 5

## Requirements

- Java Development Kit (JDK) 17 or later
- Maven 3.8 or later

Check installations:

```bash
java -version
mvn -version
```

## Build and run

From the DragonKV project directory:

```bash
mvn test
mvn package
mvn exec:java -Dexec.mainClass="com.dragonkv.Main"
```

## Example session

```text
DragonKV - In-Memory Key-Value Database
Type HELP to see available commands.
dragonkv> SET user Kishore
OK
dragonkv> GET user
Kishore
dragonkv> TTL session 30
OK
dragonkv> EXISTS user
true
dragonkv> KEYS
user
session
dragonkv> SAVE
Saved 2 key(s).
dragonkv> EXIT
Goodbye!
```

Commands are case-insensitive. Keys and values containing spaces can be passed as the remainder of the command after the key.

## Command reference

| Command | Usage | Description |
|---|---|---|
| SET | `SET key value` | Add or update a value |
| GET | `GET key` | Read a value |
| DELETE | `DELETE key` | Remove a key |
| EXISTS | `EXISTS key` | Check if a key exists |
| KEYS | `KEYS` | List non-expired keys |
| TTL | `TTL key seconds` | Set expiration time; seconds must be positive |
| SAVE | `SAVE` | Persist data to `dragonkv.db` |
| HELP | `HELP` | Show commands |
| EXIT | `EXIT` | Save and close |

## Data persistence

Data is stored in `dragonkv.db` in the current working directory when `SAVE` or `EXIT` is used. This is a simple educational persistence format, not a production-grade transaction log. Avoid abruptly terminating the program if you need the latest changes saved.

## Project structure

```text
src/main/java/com/dragonkv/   Application source code
src/test/java/com/dragonkv/   Unit tests
docs/ARCHITECTURE.md          Design notes
pom.xml                       Maven configuration
```

## Roadmap

- Add a TCP server and client protocol
- Add benchmarks against a baseline implementation
- Add snapshots and crash-safe persistence
- Add metrics and more integration tests

## License

MIT. See [LICENSE](LICENSE).
