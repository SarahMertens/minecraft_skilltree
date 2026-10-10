package com.sarah.skilltree;

import com.sarah.skilltree.config.SkillTreeConfig;
import com.sarah.skilltree.presentation.ServiceProvider;
import com.sarah.skilltree.presentation.command.SkillTreeCommand;
import com.sarah.skilltree.presentation.controller.QuestReporter;
import com.sarah.skilltree.presentation.controller.SkillController;
import com.sarah.skilltree.presentation.effect.LootEffects;
import com.sarah.skilltree.presentation.event.GameEventListener;
import com.sarah.skilltree.presentation.event.PlayerLifecycleListener;
import com.sarah.skilltree.presentation.network.ClientSynchronizer;
import com.sarah.skilltree.presentation.network.ServerNetworking;
import com.sarah.skilltree.presentation.watcher.BreedingWatcher;
import com.sarah.skilltree.presentation.watcher.EatingWatcher;
import com.sarah.skilltree.presentation.watcher.PlayerWatcher;
import com.sarah.skilltree.presentation.watcher.StatisticWatcher;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Entry point of the mod: Fabric calls {@link #onInitialize()} once while the
 * game starts.
 *
 * <p>Nothing happens here except creating the objects of the presentation
 * layer and handing each one what it needs through its constructor
 * (dependency injection by hand). There are no global variables.</p>
 */
public class SkillTreeMod implements ModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SkillTreeConfig.MOD_ID);

    @Override
    public void onInitialize() {
        ServiceProvider provider = new ServiceProvider();
        ClientSynchronizer synchronizer = new ClientSynchronizer(provider);
        QuestReporter questReporter = new QuestReporter(provider, synchronizer);
        SkillController skillController = new SkillController(provider, synchronizer);
        List<PlayerWatcher> watchers = List.of(
                new StatisticWatcher(questReporter),
                new EatingWatcher(provider),
                new BreedingWatcher(provider));

        ServerNetworking.registerPayloadTypes();
        ServerNetworking.registerReceivers(skillController, synchronizer);
        new PlayerLifecycleListener(provider, skillController, watchers).register();
        new GameEventListener(provider, questReporter).register();
        new LootEffects(provider).register();
        new SkillTreeCommand(provider, skillController).register();

        LOGGER.info("{} loaded: {} skills ready", SkillTreeConfig.MOD_NAME, provider.skillCatalog().getAll().size());
    }
}
