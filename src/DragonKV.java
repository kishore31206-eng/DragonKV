package com.dragonkv;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/** Thread-safe, in-memory key-value store with optional TTL and snapshots. */
public class DragonKV {
    private final ConcurrentHashMap<String, Entry> data = new ConcurrentHashMap<>();
    private final PersistenceManager persistenceManager;

    public DragonKV() {
        this(Path.of("dragonkv.db"));
    }

    public DragonKV(Path databaseFile) {
        this.persistenceManager = new PersistenceManager(databaseFile);
    }

    public void set(String key, String value) {
        validateKey(key);
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null.");
        }
        data.put(key, new Entry(value, 0));
    }

    public void setWithTtl(String key, String value, long ttlSeconds) {
        validateKey(key);
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null.");
        }
        if (ttlSeconds <= 0) {
            throw new IllegalArgumentException("TTL must be greater than zero.");
        }
        long ttlMillis;
        try {
            ttlMillis = Math.multiplyExact(ttlSeconds, 1000L);
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("TTL is too large.", ex);
        }
        long expiresAt;
        try {
            expiresAt = Math.addExact(System.currentTimeMillis(), ttlMillis);
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("TTL is too large.", ex);
        }
        data.put(key, new Entry(value, expiresAt));
    }

    public String get(String key) {
        validateKey(key);
        Entry entry = data.get(key);
        if (entry == null) {
            return null;
        }
        if (entry.isExpired(System.currentTimeMillis())) {
            data.remove(key, entry);
            return null;
        }
        return entry.getValue();
    }

    public boolean delete(String key) {
        validateKey(key);
        return data.remove(key) != null;
    }

    public boolean exists(String key) {
        return get(key) != null;
    }

    public List<String> keys() {
        long now = System.currentTimeMillis();
        List<String> result = new ArrayList<>();
        data.forEach((key, entry) -> {
            if (entry.isExpired(now)) {
                data.remove(key, entry);
            } else {
                result.add(key);
            }
        });
        Collections.sort(result);
        return result;
    }

    public int size() {
        return keys().size();
    }

    public void save() throws IOException {
        persistenceManager.save(data);
    }

    public void load() throws IOException {
        data.clear();
        data.putAll(persistenceManager.load());
    }

    private static void validateKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Key cannot be empty.");
        }
    }
}
