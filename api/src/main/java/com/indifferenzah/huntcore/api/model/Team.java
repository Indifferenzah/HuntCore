package com.indifferenzah.huntcore.api.model;

public enum Team {
    RUNNER,
    HUNTER,
    NONE;

    public String getColoredName() {
        switch (this) {
            case RUNNER: return "&b[RUNNER]";
            case HUNTER: return "&c[HUNTER]";
            default: return "&7-";
        }
    }

    public String getPrefix() {
        switch (this) {
            case RUNNER: return "&b[RUNNER]";
            case HUNTER: return "&c[HUNTER]";
            default: return "";
        }
    }
}
