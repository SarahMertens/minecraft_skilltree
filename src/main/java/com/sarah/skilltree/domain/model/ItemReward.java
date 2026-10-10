package com.sarah.skilltree.domain.model;

import java.util.Objects;

/** A stack of items, identified by item id (for example {@code minecraft:diamond}). */
public record ItemReward(String itemId, int amount) implements Reward {

    public ItemReward {
        Objects.requireNonNull(itemId, "itemId");
        if (amount < 1) {
            throw new IllegalArgumentException("A reward must give at least 1 item");
        }
    }
}
