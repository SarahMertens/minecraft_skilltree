package com.sarah.skilltree.exception;

/** Thrown when a player wants to spend more skill points than they own. */
public class InsufficientSkillPointsException extends SkillTreeException {

    private final int required;
    private final int available;

    public InsufficientSkillPointsException(int required, int available) {
        super("Not enough skill points: " + required + " required, " + available + " available");
        this.required = required;
        this.available = available;
    }

    public int getRequired() {
        return required;
    }

    public int getAvailable() {
        return available;
    }
}
