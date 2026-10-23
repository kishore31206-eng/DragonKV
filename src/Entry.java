package com.dragonkv;

/** A stored value with an optional expiration timestamp. */
public final class Entry {
    private final String value;
    private final long expiresAtMillis;

    public Entry(String value, long expiresAtMillis) {
        this.value = value;
        this.expiresAtMillis = expiresAtMillis;
    }

    public String getValue() {
        return value;
    }

    public long getExpiresAtMillis() {
        return expiresAtMillis;
    }

    public boolean isExpired(long nowMillis) {
        return expiresAtMillis > 0 && nowMillis >= expiresAtMillis;
    }
}
