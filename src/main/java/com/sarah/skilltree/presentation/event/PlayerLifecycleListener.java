package com.sarah.skilltree.presentation.event;

import com.sarah.skilltree.config.SkillTreeConfig;
import com.sarah.skilltree.exception.ProfileStorageException;
import com.sarah.skilltree.presentation.ServiceProvider;
import com.sarah.skilltree.presentation.controller.SkillController;
import com.sarah.skilltree.presentation.watcher.PlayerWatcher;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Follows the life of the server and its players: when to create the services,
 * when to load and save profiles, and when to run the per-second watchers.
 */
public final class PlayerLifecycleListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(SkillTreeConfig.MOD_ID);

    private final ServiceProvider provider;
    private final SkillController skillController;
    private final List<PlayerWatcher> watchers;

    public PlayerLifecycleListener(ServiceProvider provider, SkillController skillController,
                                   List<PlayerWatcher> watchers) {
        this.provider = Objects.requireNonNull(provider, "provider");
        this.skillController = Objects.requireNonNull(skillController, "skillController");
        this.watchers = List.copyOf(watchers);
    }

    public void register() {
        ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStarting);
        ServerLifecycleEvents.AFTER_SAVE.register((server, flush, force) -> saveAll());
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> onServerStopped());
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> onJoin(handler.getPlayer()));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> onLeave(handler.getPlayer()));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> onRespawn(newPlayer, alive));
        ServerTickEvents.END_SERVER_TICK.register(this::onTick);
    }

    private void onServerStarting(MinecraftServer server) {
        provider.start(server.getWorldPath(LevelResource.ROOT).resolve(SkillTreeConfig.STORAGE_DIRECTORY));
    }

    private void onServerStopped() {
        saveAll();
        provider.stop();
    }

    private void saveAll() {
        if (!provider.isRunning()) {
            return;
        }
        try {
            provider.services().profiles().saveAll();
        } catch (ProfileStorageException e) {
            LOGGER.error("Could not save all skill tree profiles", e);
        }
    }

    private void onJoin(ServerPlayer player) {
        if (!provider.isRunning()) {
            return;
        }
        try {
            provider.services().profiles().load(player.getUUID());
        } catch (ProfileStorageException e) {
            // Playing on without a profile would overwrite the stored one later.
            // Refusing the player keeps their progress safe until the problem is fixed.
            LOGGER.error("Could not load the skill tree profile of {}", player.getName().getString(), e);
            player.connection.disconnect(Component.translatable("skilltree.error.profile_load"));
            return;
        }
        skillController.refresh(player);
    }

    private void onLeave(ServerPlayer player) {
        UUID playerId = player.getUUID();
        watchers.forEach(watcher -> watcher.forget(playerId));
        if (!provider.isRunning()) {
            return;
        }
        try {
            provider.services().profiles().unload(playerId);
        } catch (ProfileStorageException e) {
            LOGGER.error("Could not save the skill tree profile of {}", player.getName().getString(), e);
        }
    }

    private void onRespawn(ServerPlayer player, boolean alive) {
        // Dying creates a new player entity, which starts without the skill bonuses.
        skillController.refresh(player);
        if (!alive) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private void onTick(MinecraftServer server) {
        if (!provider.isRunning() || server.getTickCount() % SkillTreeConfig.STATISTIC_POLL_INTERVAL_TICKS != 0) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.isAlive() && provider.isReadyFor(player.getUUID())) {
                watchers.forEach(watcher -> watcher.everySecond(player));
            }
        }
    }
}
