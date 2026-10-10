package com.sarah.skilltree.client;

import com.sarah.skilltree.presentation.network.SkillTreeSyncPayload;

import java.util.Optional;

/**
 * What the client knows about the player's skill tree: simply the last
 * snapshot the server sent. The client never calculates anything itself.
 */
public final class ClientState {

    private SkillTreeSyncPayload latest;

    public void update(SkillTreeSyncPayload payload) {
        latest = payload;
    }

    /** Forgets everything, for example after leaving a world. */
    public void clear() {
        latest = null;
    }

    /** @return the last snapshot, or empty if the server has not sent one (yet) */
    public Optional<SkillTreeSyncPayload> latest() {
        return Optional.ofNullable(latest);
    }
}
