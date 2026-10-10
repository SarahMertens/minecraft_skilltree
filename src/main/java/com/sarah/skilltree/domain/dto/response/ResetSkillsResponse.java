package com.sarah.skilltree.domain.dto.response;

/** Result of resetting all skills of a player. */
public record ResetSkillsResponse(int refundedSkillPoints, int totalSkillPoints) {
}
