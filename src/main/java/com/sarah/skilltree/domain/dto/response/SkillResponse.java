package com.sarah.skilltree.domain.dto.response;

import com.sarah.skilltree.domain.model.Branch;

/**
 * One skill as seen by one player.
 *
 * @param prerequisiteSkillId id of the parent skill, or an empty string for a root skill
 * @param prerequisiteLevel   level the parent skill needs, or 0 for a root skill
 */
public record SkillResponse(
        String id,
        Branch branch,
        int level,
        int maxLevel,
        int costPerLevel,
        String prerequisiteSkillId,
        int prerequisiteLevel,
        SkillState state
) {

    public boolean hasPrerequisite() {
        return !prerequisiteSkillId.isEmpty();
    }
}
