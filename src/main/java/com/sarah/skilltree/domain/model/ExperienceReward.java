package com.sarah.skilltree.domain.model;

/** Experience levels, handed out by the game. */
public record ExperienceReward(int amount) implements Reward {

    public ExperienceReward {
        if (amount < 1) {
            throw new IllegalArgumentException("A reward must give at least 1 experience level");
        }
    }
}
