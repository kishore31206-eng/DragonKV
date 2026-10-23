package com.dragonkv;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DragonKVTest {
    @TempDir
    Path tempDir;

    @Test
    void storesAndRetrievesValue() {
        DragonKV db = new DragonKV(tempDir.resolve("test.db"));
        db.set("name", "Kishore");
        assertEquals("Kishore", db.get("name"));
    }

    @Test
    void deletesValue() {
        DragonKV db = new DragonKV(tempDir.resolve("test.db"));
        db.set("city", "Dharmapuri");
        assertTrue(db.delete("city"));
        assertNull(db.get("city"));
        assertFalse(db.delete("city"));
    }

    @Test
    void expiresValues() throws InterruptedException {
        DragonKV db = new DragonKV(tempDir.resolve("test.db"));
        db.setWithTtl("temporary", "value", 1);
        assertEquals("value", db.get("temporary"));
        Thread.sleep(1100);
        assertNull(db.get("temporary"));
    }

    @Test
    void savesAndLoadsValues() throws IOException {
        Path file = tempDir.resolve("test.db");
        DragonKV first = new DragonKV(file);
        first.set("language", "Java");
        first.save();

        DragonKV second = new DragonKV(file);
        second.load();
        assertEquals("Java", second.get("language"));
    }

    @Test
    void rejectsEmptyKey() {
        DragonKV db = new DragonKV(tempDir.resolve("test.db"));
        assertThrows(IllegalArgumentException.class, () -> db.set(" ", "value"));
    }
}
