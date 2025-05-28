package com.app.promanage.model;

public enum Role {
    ADMIN(1),
    MANAGER(5),
    USER(10);

    private final int level;

    Role(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public boolean isAtLeast(Role other) {
        return this.level <= other.level;
    }
}
