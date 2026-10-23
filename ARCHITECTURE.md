# DragonKV Architecture

## Overview

DragonKV has four main layers:

1. **CommandHandler** reads a command from the console and validates its arguments.
2. **DragonKV** exposes database operations and stores entries in a `ConcurrentHashMap`.
3. **Entry** holds a value and its expiration timestamp.
4. **PersistenceManager** reads and writes a simple local snapshot file.

## Data flow

```text
User command
    |
    v
CommandHandler
    |
    v
DragonKV API
    |
    +----> ConcurrentHashMap<String, Entry>
    |
    +----> PersistenceManager ----> dragonkv.db
```

## Expiration

An entry has an expiration timestamp in epoch milliseconds. A timestamp of `0` means no expiration. On read, expired entries are removed and treated as missing.

## Thread safety

`ConcurrentHashMap` allows concurrent access to the in-memory map. Individual operations are thread-safe, but a multi-key snapshot is not a fully transactional database snapshot.

## Limitations

- The console interface is local; this version does not expose a network protocol.
- Persistence happens when `SAVE` or `EXIT` is run, not after every mutation.
- The persistence format is intended for learning and is not hardened against corrupted files or concurrent writers.
- This is a learning project, not a production database.
