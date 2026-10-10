package com.sarah.skilltree.presentation;

import com.sarah.skilltree.domain.catalog.DefaultQuests;
import com.sarah.skilltree.domain.catalog.DefaultSkills;
import com.sarah.skilltree.domain.catalog.QuestCatalog;
import com.sarah.skilltree.domain.catalog.SkillCatalog;
import com.sarah.skilltree.domain.service.ProfileService;
import com.sarah.skilltree.domain.service.QuestService;
import com.sarah.skilltree.domain.service.SkillService;
import com.sarah.skilltree.persistence.JsonPlayerProfileRepository;
import com.sarah.skilltree.persistence.PlayerProfileRepository;

import java.nio.file.Path;
import java.util.UUID;

/**
 * Builds the three layers and connects them (the "composition root").
 *
 * <p>Profiles are stored inside the world folder, and in singleplayer a new
 * server starts every time a world is opened. That is why the services are
 * created per server and not once for the whole game.</p>
 */
public final class ServiceProvider {

    private final SkillCatalog skillCatalog = DefaultSkills.createCatalog();
    private final QuestCatalog questCatalog = DefaultQuests.createCatalog();
    private SkillTreeServices services;

    /** Creates the services for a server that stores its profiles in the given directory. */
    public void start(Path storageDirectory) {
        PlayerProfileRepository repository = new JsonPlayerProfileRepository(storageDirectory);
        ProfileService profileService = new ProfileService(repository);
        services = new SkillTreeServices(
                profileService,
                new SkillService(skillCatalog, profileService),
                new QuestService(questCatalog, profileService));
    }

    public void stop() {
        services = null;
    }

    public boolean isRunning() {
        return services != null;
    }

    /** @throws IllegalStateException if no server is running */
    public SkillTreeServices services() {
        if (services == null) {
            throw new IllegalStateException("The skill tree services are only available while a server is running");
        }
        return services;
    }

    /** @return whether a server is running and the profile of this player is loaded */
    public boolean isReadyFor(UUID playerId) {
        return services != null && services.profiles().isLoaded(playerId);
    }

    public SkillCatalog skillCatalog() {
        return skillCatalog;
    }
}
