package com.sarah.skilltree.domain.model;

import com.sarah.skilltree.exception.InsufficientSkillPointsException;
import com.sarah.skilltree.exception.MaxLevelReachedException;
import com.sarah.skilltree.exception.PrerequisiteNotMetException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerProfileTest {

    private static final Skill MINING = Skill.root("mining", Branch.MINING, 3, 2);
    private static final Skill PROSPECTOR =
            Skill.child("prospector", Branch.MINING, 2, 1, new SkillRequirement("mining", 2));

    private PlayerProfile profile;

    @BeforeEach
    void setUp() {
        profile = new PlayerProfile(UUID.randomUUID());
    }

    @Nested
    @DisplayName("upgrading a skill")
    class UpgradeSkill {

        @Test
        void raisesTheLevelAndPaysTheCost() {
            profile.addSkillPoints(5);

            int newLevel = profile.upgradeSkill(MINING);

            assertEquals(1, newLevel);
            assertEquals(1, profile.getSkillLevel("mining"));
            assertEquals(3, profile.getSkillPoints());
        }

        @Test
        void failsWithoutEnoughPointsAndChangesNothing() {
            profile.addSkillPoints(1);

            InsufficientSkillPointsException exception =
                    assertThrows(InsufficientSkillPointsException.class, () -> profile.upgradeSkill(MINING));

            assertEquals(2, exception.getRequired());
            assertEquals(1, exception.getAvailable());
            assertEquals(0, profile.getSkillLevel("mining"));
            assertEquals(1, profile.getSkillPoints());
        }

        @Test
        void failsAtMaximumLevel() {
            profile.addSkillPoints(100);
            for (int i = 0; i < MINING.getMaxLevel(); i++) {
                profile.upgradeSkill(MINING);
            }

            assertThrows(MaxLevelReachedException.class, () -> profile.upgradeSkill(MINING));
            assertEquals(3, profile.getSkillLevel("mining"));
            assertEquals(94, profile.getSkillPoints());
        }

        @Test
        void failsWhenThePrerequisiteIsNotHighEnough() {
            profile.addSkillPoints(100);
            profile.upgradeSkill(MINING); // level 1, but 2 is required

            PrerequisiteNotMetException exception =
                    assertThrows(PrerequisiteNotMetException.class, () -> profile.upgradeSkill(PROSPECTOR));

            assertEquals("mining", exception.getRequiredSkillId());
            assertEquals(2, exception.getRequiredLevel());
            assertEquals(0, profile.getSkillLevel("prospector"));
        }

        @Test
        void succeedsOnceThePrerequisiteIsMet() {
            profile.addSkillPoints(100);
            profile.upgradeSkill(MINING);
            profile.upgradeSkill(MINING);

            assertTrue(profile.hasUnlocked(PROSPECTOR));
            assertEquals(1, profile.upgradeSkill(PROSPECTOR));
        }

        @Test
        void refundGivesBackEveryPointThatWasSpent() {
            profile.addSkillPoints(10);
            profile.upgradeSkill(MINING);
            profile.upgradeSkill(MINING);

            int refund = profile.refundSkill(MINING);

            assertEquals(4, refund);
            assertEquals(10, profile.getSkillPoints());
            assertEquals(0, profile.getSkillLevel("mining"));
            assertEquals(0, profile.refundSkill(MINING), "a second refund gives nothing");
        }
    }

    @Nested
    @DisplayName("quest progress")
    class QuestProgress {

        private final Quest first = Quest.builder("first", Branch.MINING, Objective.of(ObjectiveType.MINE_ORE, 10))
                .skillPoints(1).build();
        private final Quest second = Quest.builder("second", Branch.MINING, Objective.of(ObjectiveType.MINE_ORE, 5))
                .skillPoints(1).after("first").build();
        private final Quest repeatable = Quest.builder("again", Branch.MINING, Objective.of(ObjectiveType.MINE_ORE, 4))
                .skillPoints(1).repeatable().build();

        @Test
        void countsUpWithoutCompletingTooEarly() {
            assertEquals(0, profile.advanceQuest(first, 9));

            assertEquals(9, profile.getQuestProgress("first"));
            assertFalse(profile.hasCompletedQuest("first"));
        }

        @Test
        void completesExactlyOnceAndStopsCounting() {
            assertEquals(1, profile.advanceQuest(first, 25));
            assertEquals(0, profile.advanceQuest(first, 25));

            assertEquals(10, profile.getQuestProgress("first"), "progress never exceeds the goal");
            assertEquals(1, profile.getQuestCompletions("first"));
        }

        @Test
        void ignoresProgressForALockedQuest() {
            assertEquals(0, profile.advanceQuest(second, 5));
            assertEquals(0, profile.getQuestProgress("second"));

            profile.advanceQuest(first, 10);

            assertEquals(1, profile.advanceQuest(second, 5));
        }

        @Test
        void repeatableQuestStartsOverAndKeepsTheSurplus() {
            int completions = profile.advanceQuest(repeatable, 9); // 2 x 4, 1 left over

            assertEquals(2, completions);
            assertEquals(1, profile.getQuestProgress("again"));
            assertEquals(2, profile.getQuestCompletions("again"));
            assertTrue(repeatable.isActiveFor(profile));
        }

        @Test
        void rejectsProgressBelowOne() {
            assertThrows(IllegalArgumentException.class, () -> profile.advanceQuest(first, 0));
        }
    }

    @Test
    void skillPointsCannotBecomeNegative() {
        assertThrows(IllegalArgumentException.class, () -> profile.addSkillPoints(-1));
    }
}
