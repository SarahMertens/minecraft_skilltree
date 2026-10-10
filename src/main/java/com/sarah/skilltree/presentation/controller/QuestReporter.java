package com.sarah.skilltree.presentation.controller;

import com.sarah.skilltree.config.SkillTreeConfig;
import com.sarah.skilltree.domain.dto.request.QuestProgressRequest;
import com.sarah.skilltree.domain.dto.response.CompletedQuestResponse;
import com.sarah.skilltree.domain.dto.response.QuestProgressResponse;
import com.sarah.skilltree.domain.dto.response.RewardResponse;
import com.sarah.skilltree.domain.model.ObjectiveType;
import com.sarah.skilltree.exception.SkillTreeException;
import com.sarah.skilltree.presentation.ServiceProvider;
import com.sarah.skilltree.presentation.message.Messages;
import com.sarah.skilltree.presentation.network.ClientSynchronizer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * The single door through which game events reach the quest system.
 *
 * <p>Every listener and watcher calls {@link #report}; what happens next
 * (counting, rewards, messages, updating the client) is decided here once.</p>
 */
public final class QuestReporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SkillTreeConfig.MOD_ID);
    /** Subject for events that are not about one specific thing, such as walking. */
    public static final String NO_SUBJECT = "";

    private final ServiceProvider provider;
    private final ClientSynchronizer synchronizer;

    public QuestReporter(ServiceProvider provider, ClientSynchronizer synchronizer) {
        this.provider = Objects.requireNonNull(provider, "provider");
        this.synchronizer = Objects.requireNonNull(synchronizer, "synchronizer");
    }

    public void report(ServerPlayer player, ObjectiveType type, String subject, int amount) {
        if (amount < 1 || !provider.isReadyFor(player.getUUID())) {
            return;
        }
        try {
            QuestProgressResponse response = provider.services().quests()
                    .recordProgress(new QuestProgressRequest(player.getUUID(), type, subject, amount));
            for (CompletedQuestResponse completed : response.completed()) {
                RewardGranter.grant(player, completed.rewards());
                announce(player, completed);
            }
            if (!response.completed().isEmpty()) {
                synchronizer.sync(player);
            }
        } catch (SkillTreeException e) {
            // A quest problem must never interrupt the game action that caused it.
            LOGGER.error("Could not record quest progress for {}", player.getName().getString(), e);
        }
    }

    private void announce(ServerPlayer player, CompletedQuestResponse completed) {
        MutableComponent rewards = Component.empty();
        for (int i = 0; i < completed.rewards().size(); i++) {
            RewardResponse reward = completed.rewards().get(i);
            if (i > 0) {
                rewards.append(", ");
            }
            rewards.append(Messages.reward(reward));
        }
        player.sendSystemMessage(Component.translatable("skilltree.message.quest_completed",
                        Messages.questName(completed.questId()).withStyle(ChatFormatting.GOLD),
                        rewards.withStyle(ChatFormatting.GREEN))
                .withStyle(ChatFormatting.YELLOW));
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 0.6f, 1.0f);
    }
}
