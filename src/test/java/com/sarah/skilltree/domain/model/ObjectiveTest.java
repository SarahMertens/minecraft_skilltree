package com.sarah.skilltree.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ObjectiveTest {

    @Test
    void objectiveWithoutTargetMatchesEverySubjectOfItsType() {
        Objective objective = Objective.of(ObjectiveType.KILL_MONSTER, 10);

        assertTrue(objective.matches(ObjectiveType.KILL_MONSTER, "minecraft:zombie"));
        assertTrue(objective.matches(ObjectiveType.KILL_MONSTER, "minecraft:wither"));
        assertFalse(objective.matches(ObjectiveType.MINE_ORE, "minecraft:zombie"));
    }

    @Test
    void objectiveWithTargetOnlyMatchesThatSubject() {
        Objective objective = Objective.ofTarget(ObjectiveType.KILL_MONSTER, "minecraft:wither", 1);

        assertTrue(objective.matches(ObjectiveType.KILL_MONSTER, "minecraft:wither"));
        assertFalse(objective.matches(ObjectiveType.KILL_MONSTER, "minecraft:zombie"));
    }

    @Test
    void amountMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> Objective.of(ObjectiveType.MINE_ORE, 0));
    }

    @Test
    void questNeedsAtLeastOneReward() {
        Quest.Builder builder = Quest.builder("empty", Branch.MINING, Objective.of(ObjectiveType.MINE_ORE, 1));

        assertThrows(IllegalStateException.class, builder::build);
    }
}
