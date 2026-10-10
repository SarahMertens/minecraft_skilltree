package com.sarah.skilltree.domain.dto.response;

import java.util.List;

/** The whole skill tree as seen by one player. */
public record SkillTreeResponse(int skillPoints, List<SkillResponse> skills) {

    public SkillTreeResponse {
        skills = List.copyOf(skills);
    }
}
