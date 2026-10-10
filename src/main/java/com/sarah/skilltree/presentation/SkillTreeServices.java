package com.sarah.skilltree.presentation;

import com.sarah.skilltree.domain.service.ProfileService;
import com.sarah.skilltree.domain.service.QuestService;
import com.sarah.skilltree.domain.service.SkillService;

/** The services of the domain, bundled so they can be handed around as one object. */
public record SkillTreeServices(ProfileService profiles, SkillService skills, QuestService quests) {
}
