package com.app.promanage.model;

public enum TaskStatus {
    OPEN(1),
    IN_PROGRESS(2),
    DONE(3);

    private final int value;

    TaskStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static TaskStatus fromValue(int value) {
        for (TaskStatus status : values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid TaskStatus value: " + value);
    }
}
