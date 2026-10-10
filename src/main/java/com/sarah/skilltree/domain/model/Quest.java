package com.sarah.skilltree.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Definition of a quest. Immutable: a player's progress lives in the
 * {@link PlayerProfile}.
 *
 * <p>Built with a {@link Builder} because most quests only use a few of the
 * optional parts (prerequisite, bonus rewards, repeatable).</p>
 */
public final class Quest {

    private final String id;
    private final Branch branch;
    private final Objective objective;
    private final List<Reward> rewards;
    private final String prerequisiteQuestId;
    private final boolean repeatable;

    private Quest(Builder builder) {
        this.id = builder.id;
        this.branch = builder.branch;
        this.objective = builder.objective;
        this.rewards = List.copyOf(builder.rewards);
        this.prerequisiteQuestId = builder.prerequisiteQuestId;
        this.repeatable = builder.repeatable;
    }

    public static Builder builder(String id, Branch branch, Objective objective) {
        return new Builder(id, branch, objective);
    }

    /**
     * A quest is unlocked once the quest before it in the chain is finished.
     * Quests without a prerequisite are unlocked from the start.
     */
    public boolean isUnlockedFor(PlayerProfile profile) {
        return prerequisiteQuestId == null || profile.hasCompletedQuest(prerequisiteQuestId);
    }

    /** @return whether progress still counts: unlocked and not finished for good */
    public boolean isActiveFor(PlayerProfile profile) {
        return isUnlockedFor(profile) && (repeatable || !profile.hasCompletedQuest(id));
    }

    public String getId() {
        return id;
    }

    public Branch getBranch() {
        return branch;
    }

    public Objective getObjective() {
        return objective;
    }

    public List<Reward> getRewards() {
        return rewards;
    }

    public Optional<String> getPrerequisiteQuestId() {
        return Optional.ofNullable(prerequisiteQuestId);
    }

    public boolean isRepeatable() {
        return repeatable;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Quest quest && id.equals(quest.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Quest[" + id + "]";
    }

    /** Step-by-step construction of a {@link Quest}. */
    public static final class Builder {

        private final String id;
        private final Branch branch;
        private final Objective objective;
        private final List<Reward> rewards = new ArrayList<>();
        private String prerequisiteQuestId;
        private boolean repeatable;

        private Builder(String id, Branch branch, Objective objective) {
            this.id = Objects.requireNonNull(id, "id");
            this.branch = Objects.requireNonNull(branch, "branch");
            this.objective = Objects.requireNonNull(objective, "objective");
            if (id.isBlank()) {
                throw new IllegalArgumentException("Quest id must not be blank");
            }
        }

        public Builder reward(Reward reward) {
            rewards.add(Objects.requireNonNull(reward, "reward"));
            return this;
        }

        /** Shortcut for the most common reward. */
        public Builder skillPoints(int amount) {
            return reward(new SkillPointReward(amount));
        }

        public Builder after(String prerequisiteQuestId) {
            this.prerequisiteQuestId = Objects.requireNonNull(prerequisiteQuestId, "prerequisiteQuestId");
            return this;
        }

        public Builder repeatable() {
            this.repeatable = true;
            return this;
        }

        public Quest build() {
            if (rewards.isEmpty()) {
                throw new IllegalStateException("Quest " + id + " must have at least one reward");
            }
            return new Quest(this);
        }
    }
}
