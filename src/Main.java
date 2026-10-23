package com.dragonkv;

import java.io.IOException;

/** Application entry point. */
public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        DragonKV database = new DragonKV();
        try {
            database.load();
        } catch (IOException ex) {
            System.err.println("Could not load dragonkv.db: " + ex.getMessage());
            System.err.println("Starting with an empty database.");
        }
        CommandHandler.run(database);
    }
}
