package com.dragonkv;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.stream.Collectors;

/** Parses and runs interactive console commands. */
public final class CommandHandler {
    private final DragonKV database;

    public CommandHandler(DragonKV database) {
        this.database = database;
    }

    /** @return false when the user requests EXIT. */
    public boolean handle(String input) {
        if (input == null || input.isBlank()) {
            return true;
        }

        String trimmed = input.trim();
        int firstSpace = trimmed.indexOf(' ');
        String command = (firstSpace < 0 ? trimmed : trimmed.substring(0, firstSpace))
                .toUpperCase(Locale.ROOT);
        String arguments = firstSpace < 0 ? "" : trimmed.substring(firstSpace + 1).trim();

        try {
            switch (command) {
                case "SET" -> {
                    String[] pair = splitKeyAndValue(arguments);
                    database.set(pair[0], pair[1]);
                    System.out.println("OK");
                }
                case "GET" -> {
                    requireArguments(arguments, "Usage: GET <key>");
                    String value = database.get(arguments);
                    System.out.println(value == null ? "(nil)" : value);
                }
                case "DELETE" -> {
                    requireArguments(arguments, "Usage: DELETE <key>");
                    System.out.println(database.delete(arguments) ? "OK" : "(not found)");
                }
                case "EXISTS" -> {
                    requireArguments(arguments, "Usage: EXISTS <key>");
                    System.out.println(database.exists(arguments));
                }
                case "KEYS" -> {
                    List<String> keys = database.keys();
                    System.out.println(keys.isEmpty() ? "(empty)" :
                            keys.stream().collect(Collectors.joining(System.lineSeparator())));
                }
                case "TTL" -> {
                    String[] parts = arguments.split("\\s+", 2);
                    if (parts.length != 2) {
                        throw new IllegalArgumentException("Usage: TTL <key> <seconds>");
                    }
                    long seconds = Long.parseLong(parts[1]);
                    String value = database.get(parts[0]);
                    if (value == null) {
                        System.out.println("(not found)");
                    } else {
                        database.setWithTtl(parts[0], value, seconds);
                        System.out.println("OK");
                    }
                }
                case "SAVE" -> {
                    database.save();
                    System.out.println("Saved " + database.size() + " key(s).");
                }
                case "HELP" -> printHelp();
                case "EXIT", "QUIT" -> {
                    database.save();
                    System.out.println("Goodbye!");
                    return false;
                }
                default -> System.out.println("Unknown command. Type HELP.");
            }
        } catch (IllegalArgumentException | IOException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
        return true;
    }

    private static String[] splitKeyAndValue(String arguments) {
        int separator = arguments.indexOf(' ');
        if (separator <= 0 || separator == arguments.length() - 1) {
            throw new IllegalArgumentException("Usage: SET <key> <value>");
        }
        String key = arguments.substring(0, separator).trim();
        String value = arguments.substring(separator + 1).trim();
        if (key.isEmpty() || value.isEmpty()) {
            throw new IllegalArgumentException("Usage: SET <key> <value>");
        }
        return new String[]{key, value};
    }

    private static void requireArguments(String arguments, String message) {
        if (arguments.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    private static void printHelp() {
        System.out.println("""
                Available commands:
                  SET <key> <value>   Store a value
                  GET <key>           Read a value
                  DELETE <key>        Remove a key
                  EXISTS <key>        Check whether a key exists
                  KEYS                List all keys
                  TTL <key> <seconds> Set expiration time
                  SAVE                Save data to dragonkv.db
                  HELP                Show this help
                  EXIT                Save and exit
                """);
    }

    public static void run(DragonKV database) {
        CommandHandler handler = new CommandHandler(database);
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("DragonKV - In-Memory Key-Value Database");
            System.out.println("Type HELP to see available commands.");
            boolean running = true;
            while (running) {
                System.out.print("dragonkv> ");
                if (!scanner.hasNextLine()) {
                    break;
                }
                running = handler.handle(scanner.nextLine());
            }
        }
    }
}
