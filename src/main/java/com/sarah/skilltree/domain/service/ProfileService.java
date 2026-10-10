package com.sarah.skilltree.domain.service;

import com.sarah.skilltree.domain.model.PlayerProfile;
import com.sarah.skilltree.exception.ProfileStorageException;
import com.sarah.skilltree.persistence.PlayerProfileRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Keeps the profiles of online players in memory and decides when they are
 * loaded from and saved to the repository.
 *
 * <p>Reading a file for every broken block would be far too slow, so a profile
 * is loaded once when the player joins and written back when they leave or
 * when the world is saved.</p>
 */
public class ProfileService {

    private final PlayerProfileRepository repository;
    private final Map<UUID, PlayerProfile> loadedProfiles = new HashMap<>();

    public ProfileService(PlayerProfileRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    /**
     * Makes sure the profile of a player is in memory. Call when a player joins.
     *
     * @throws ProfileStorageException if the stored profile cannot be read
     */
    public void load(UUID playerId) {
        getProfile(playerId);
    }

    /**
     * Saves the profile of a player and removes it from memory. Call when a player leaves.
     *
     * @throws ProfileStorageException if the profile cannot be written
     */
    public void unload(UUID playerId) {
        PlayerProfile profile = loadedProfiles.get(playerId);
        if (profile == null) {
            return;
        }
        repository.save(profile);
        loadedProfiles.remove(playerId);
    }

    /**
     * Saves all loaded profiles but keeps them in memory.
     *
     * @throws ProfileStorageException if at least one profile cannot be written
     *                                 (the other profiles are still saved)
     */
    public void saveAll() {
        ProfileStorageException firstFailure = null;
        for (PlayerProfile profile : List.copyOf(loadedProfiles.values())) {
            try {
                repository.save(profile);
            } catch (ProfileStorageException e) {
                if (firstFailure == null) {
                    firstFailure = e;
                }
            }
        }
        if (firstFailure != null) {
            throw firstFailure;
        }
    }

    public boolean isLoaded(UUID playerId) {
        return loadedProfiles.containsKey(playerId);
    }

    /**
     * Only for the other services in this package: the profile itself never
     * leaves the domain, the presentation layer works with DTOs.
     */
    PlayerProfile getProfile(UUID playerId) {
        return loadedProfiles.computeIfAbsent(playerId,
                id -> repository.findById(id).orElseGet(() -> new PlayerProfile(id)));
    }
}
