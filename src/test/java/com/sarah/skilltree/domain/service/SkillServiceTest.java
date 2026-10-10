package com.sarah.skilltree.domain.service;

import com.sarah.skilltree.domain.catalog.SkillCatalog;
import com.sarah.skilltree.domain.dto.request.GrantSkillPointsRequest;
import com.sarah.skilltree.domain.dto.request.UpgradeSkillRequest;
import com.sarah.skilltree.domain.dto.response.ResetSkillsResponse;
import com.sarah.skilltree.domain.dto.response.SkillResponse;
import com.sarah.skilltree.domain.dto.response.SkillState;
import com.sarah.skilltree.domain.dto.response.SkillTreeResponse;
import com.sarah.skilltree.domain.dto.response.UpgradeSkillResponse;
import com.sarah.skilltree.domain.model.Branch;
import com.sarah.skilltree.domain.model.PlayerProfile;
import com.sarah.skilltree.domain.model.Skill;
import com.sarah.skilltree.domain.model.SkillRequirement;
import com.sarah.skilltree.exception.InsufficientSkillPointsException;
import com.sarah.skilltree.exception.UnknownSkillException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SkillServiceTest {

    private static final UUID PLAYER = UUID.randomUUID();

    @Mock
    private ProfileService profileService;

    private SkillService skillService;
    private PlayerProfile profile;

    @BeforeEach
    void setUp() {
        SkillCatalog catalog = new SkillCatalog(List.of(
                Skill.root("mining", Branch.MINING, 2, 2),
                Skill.child("prospector", Branch.MINING, 3, 1, new SkillRequirement("mining", 1))));
        skillService = new SkillService(catalog, profileService);
        profile = new PlayerProfile(PLAYER);
    }

    @Test
    void upgradeReturnsTheNewLevelAndRemainingPoints() {
        profile.addSkillPoints(5);
        when(profileService.getProfile(PLAYER)).thenReturn(profile);

        UpgradeSkillResponse response = skillService.upgradeSkill(new UpgradeSkillRequest(PLAYER, "mining"));

        assertEquals("mining", response.skillId());
        assertEquals(1, response.newLevel());
        assertEquals(2, response.maxLevel());
        assertEquals(3, response.remainingSkillPoints());
    }

    @Test
    void upgradeOfUnknownSkillFailsBeforeTouchingAnyProfile() {
        assertThrows(UnknownSkillException.class,
                () -> skillService.upgradeSkill(new UpgradeSkillRequest(PLAYER, "flying")));

        verifyNoInteractions(profileService);
    }

    @Test
    void upgradePassesOnTheRulesOfTheProfile() {
        when(profileService.getProfile(PLAYER)).thenReturn(profile); // 0 points

        assertThrows(InsufficientSkillPointsException.class,
                () -> skillService.upgradeSkill(new UpgradeSkillRequest(PLAYER, "mining")));
    }

    @Test
    void skillTreeShowsLockedAvailableAndMaxedStates() {
        profile.addSkillPoints(4);
        when(profileService.getProfile(PLAYER)).thenReturn(profile);

        SkillTreeResponse before = skillService.getSkillTree(PLAYER);
        assertEquals(SkillState.AVAILABLE, find(before, "mining").state());
        assertEquals(SkillState.LOCKED, find(before, "prospector").state());

        skillService.upgradeSkill(new UpgradeSkillRequest(PLAYER, "mining"));
        skillService.upgradeSkill(new UpgradeSkillRequest(PLAYER, "mining"));

        SkillTreeResponse after = skillService.getSkillTree(PLAYER);
        assertEquals(0, after.skillPoints());
        assertEquals(SkillState.MAXED, find(after, "mining").state());
        assertEquals(SkillState.AVAILABLE, find(after, "prospector").state());
    }

    @Test
    void skillTreeDescribesThePrerequisite() {
        when(profileService.getProfile(PLAYER)).thenReturn(profile);

        SkillTreeResponse tree = skillService.getSkillTree(PLAYER);

        assertFalse(find(tree, "mining").hasPrerequisite());
        assertTrue(find(tree, "prospector").hasPrerequisite());
        assertEquals("mining", find(tree, "prospector").prerequisiteSkillId());
        assertEquals(1, find(tree, "prospector").prerequisiteLevel());
    }

    @Test
    void grantAddsPointsAndReturnsTheNewTotal() {
        profile.addSkillPoints(2);
        when(profileService.getProfile(PLAYER)).thenReturn(profile);

        assertEquals(7, skillService.grantSkillPoints(new GrantSkillPointsRequest(PLAYER, 5)));
        assertEquals(7, skillService.getSkillPoints(PLAYER));
    }

    @Test
    void resetRefundsAllSkills() {
        profile.addSkillPoints(5);
        when(profileService.getProfile(PLAYER)).thenReturn(profile);
        skillService.upgradeSkill(new UpgradeSkillRequest(PLAYER, "mining"));     // -2
        skillService.upgradeSkill(new UpgradeSkillRequest(PLAYER, "prospector")); // -1

        ResetSkillsResponse response = skillService.resetSkills(PLAYER);

        assertEquals(3, response.refundedSkillPoints());
        assertEquals(5, response.totalSkillPoints());
        assertEquals(0, skillService.getSkillLevel(PLAYER, "mining"));
        assertEquals(0, skillService.getSkillLevel(PLAYER, "prospector"));
    }

    private SkillResponse find(SkillTreeResponse tree, String skillId) {
        return tree.skills().stream().filter(skill -> skill.id().equals(skillId)).findFirst().orElseThrow();
    }
}
