package com.sarah.skilltree.domain.model;

/**
 * Something a player receives for finishing a quest.
 *
 * <p>Sealed: the compiler knows every kind of reward, so a {@code switch} over
 * a reward must handle all of them - adding a new kind cannot be forgotten
 * somewhere.</p>
 */
public sealed interface Reward permits SkillPointReward, ExperienceReward, ItemReward {

    /** @return how much of the reward is given (points, levels or items) */
    int amount();
}
