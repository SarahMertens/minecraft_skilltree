package com.sarah.skilltree.presentation.watcher;

import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/**
 * Something that looks at every online player once per second.
 *
 * <p>Fabric has no event for everything (eating, breeding, walking ...). For
 * those cases a watcher compares the current state of the player with what it
 * saw the second before, and reacts to the difference.</p>
 */
public interface PlayerWatcher {

    /** Called once per second for every player whose profile is loaded. */
    void everySecond(ServerPlayer player);

    /** Called when a player leaves, so the watcher can forget what it remembered. */
    void forget(UUID playerId);
}
