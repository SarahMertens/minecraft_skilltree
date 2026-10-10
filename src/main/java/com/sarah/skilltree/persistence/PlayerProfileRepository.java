package com.sarah.skilltree.persistence;

import com.sarah.skilltree.domain.model.PlayerProfile;
import com.sarah.skilltree.exception.ProfileStorageException;

import java.util.Optional;
import java.util.UUID;

/**
 * Stores and loads player profiles.
 *
 * <p>The domain only knows this interface. How and where profiles are stored
 * (JSON files today, maybe a database later) can change without touching the
 * services, and tests can replace it with a mock.</p>
 */
public interface PlayerProfileRepository {

    /**
     * @return the stored profile, or empty if the player has none yet
     * @throws ProfileStorageException if the storage cannot be read
     */
    Optional<PlayerProfile> findById(UUID playerId);

    /**
     * Stores the profile, replacing an earlier version.
     *
     * @throws ProfileStorageException if the storage cannot be written
     */
    void save(PlayerProfile profile);
}
