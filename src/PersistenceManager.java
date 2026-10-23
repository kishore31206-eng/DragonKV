package com.dragonkv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Saves and loads a simple snapshot file. Each field is Base64 encoded. */
public final class PersistenceManager {
    private final Path file;

    public PersistenceManager(Path file) {
        this.file = file;
    }

    public void save(Map<String, Entry> data) throws IOException {
        Path absolute = file.toAbsolutePath();
        Path parent = absolute.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Path temp = absolute.resolveSibling(absolute.getFileName() + ".tmp");
        try (BufferedWriter writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
            long now = System.currentTimeMillis();
            for (Map.Entry<String, Entry> item : data.entrySet()) {
                Entry entry = item.getValue();
                if (entry.isExpired(now)) {
                    continue;
                }
                String key = encode(item.getKey());
                String value = encode(entry.getValue());
                writer.write(key + "\t" + value + "\t" + entry.getExpiresAtMillis());
                writer.newLine();
            }
        }
        try {
            Files.move(temp, absolute, StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException atomicMoveNotSupported) {
            Files.move(temp, absolute, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public Map<String, Entry> load() throws IOException {
        Map<String, Entry> loaded = new ConcurrentHashMap<>();
        if (!Files.exists(file)) {
            return loaded;
        }
        long now = System.currentTimeMillis();
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                String[] fields = line.split("\\t", -1);
                if (fields.length != 3) {
                    throw new IOException("Invalid database format at line " + lineNumber);
                }
                try {
                    String key = decode(fields[0]);
                    String value = decode(fields[1]);
                    long expiresAt = Long.parseLong(fields[2]);
                    if (key.isBlank()) {
                        throw new IllegalArgumentException("Empty key");
                    }
                    Entry entry = new Entry(value, expiresAt);
                    if (!entry.isExpired(now)) {
                        loaded.put(key, entry);
                    }
                } catch (IllegalArgumentException ex) {
                    throw new IOException("Invalid database entry at line " + lineNumber, ex);
                }
            }
        }
        return loaded;
    }

    private static String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String value) {
        return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
    }
}
