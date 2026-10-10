package com.sarah.skilltree.domain.model;

import java.util.Objects;
import java.util.Optional;

/**
 * What has to be done to finish a quest: an action, how often, and optionally a
 * specific target (for example "kill 1 monster" with target {@code minecraft:wither}).
 */
public final class Objective {

    private final ObjectiveType type;
    private final String target;
    private final int amount;

    private Objective(ObjectiveType type, String target, int amount) {
        this.type = Objects.requireNonNull(type, "type");
        if (amount < 1) {
            throw new IllegalArgumentException("Objective amount must be at least 1");
        }
        this.target = target;
        this.amount = amount;
    }

    /** An objective that counts every subject of the given type. */
    public static Objective of(ObjectiveType type, int amount) {
        return new Objective(type, null, amount);
    }

    /** An objective that only counts one specific subject, identified by its id. */
    public static Objective ofTarget(ObjectiveType type, String target, int amount) {
        return new Objective(type, Objects.requireNonNull(target, "target"), amount);
    }

    /** @return whether an event of this type about this subject counts for the objective */
    public boolean matches(ObjectiveType eventType, String subject) {
        if (type != eventType) {
            return false;
        }
        return target == null || target.equals(subject);
    }

    public ObjectiveType getType() {
        return type;
    }

    public Optional<String> getTarget() {
        return Optional.ofNullable(target);
    }

    public int getAmount() {
        return amount;
    }
}
