package com.sarah.skilltree.presentation.event;

import com.sarah.skilltree.config.SkillTreeConfig;
import com.sarah.skilltree.domain.model.ObjectiveType;
import com.sarah.skilltree.presentation.ServiceProvider;
import com.sarah.skilltree.presentation.controller.QuestReporter;
import com.sarah.skilltree.presentation.effect.TreeFeller;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

/**
 * Listens to things players do in the world (break a block, kill a mob, change
 * dimension) and translates them into quest progress and active skills.
 */
public final class GameEventListener {

    private final ServiceProvider provider;
    private final QuestReporter reporter;

    public GameEventListener(ServiceProvider provider, QuestReporter reporter) {
        this.provider = Objects.requireNonNull(provider, "provider");
        this.reporter = Objects.requireNonNull(reporter, "reporter");
    }

    public void register() {
        PlayerBlockBreakEvents.AFTER.register(this::onBlockBroken);
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register(this::onEntityKilled);
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(this::onDimensionChanged);
    }

    private void onBlockBroken(Level level, Player breaker, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        // Creative players break everything instantly; that should not finish quests.
        if (!(breaker instanceof ServerPlayer player) || player.isCreative()
                || !provider.isReadyFor(player.getUUID())) {
            return;
        }
        String blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();

        if (state.is(ConventionalBlockTags.ORES)) {
            reporter.report(player, ObjectiveType.MINE_ORE, blockId, 1);
        } else if (isStone(state)) {
            reporter.report(player, ObjectiveType.MINE_STONE, blockId, 1);
        } else if (state.is(BlockTags.LOGS)) {
            int logs = 1 + fellTree(player, pos);
            reporter.report(player, ObjectiveType.CHOP_LOG, blockId, logs);
        } else if (state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state)) {
            reporter.report(player, ObjectiveType.HARVEST_CROP, blockId, 1);
        }
    }

    private boolean isStone(BlockState state) {
        return state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(BlockTags.BASE_STONE_NETHER)
                || state.is(ConventionalBlockTags.STONES) || state.is(ConventionalBlockTags.COBBLESTONES);
    }

    /** @return the number of extra logs felled by the Lumberjack skill (0 without the skill) */
    private int fellTree(ServerPlayer player, BlockPos origin) {
        int level = provider.services().skills()
                .getSkillLevel(player.getUUID(), SkillTreeConfig.SKILL_WOODCUTTING);
        if (level < 1 || !TreeFeller.canFell(player)) {
            return 0;
        }
        int[] maxLogsByLevel = SkillTreeConfig.WOODCUTTING_MAX_LOGS_BY_LEVEL;
        int maxLogs = maxLogsByLevel[Math.min(level, maxLogsByLevel.length) - 1];
        return TreeFeller.fell(player.level(), player, origin, maxLogs);
    }

    private void onEntityKilled(ServerLevel level, Entity killer, LivingEntity victim, DamageSource source) {
        if (!(killer instanceof ServerPlayer player) || !(victim instanceof Enemy)
                || !provider.isReadyFor(player.getUUID())) {
            return;
        }
        String entityId = BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType()).toString();
        reporter.report(player, ObjectiveType.KILL_MONSTER, entityId, 1);

        int looterLevel = provider.services().skills()
                .getSkillLevel(player.getUUID(), SkillTreeConfig.SKILL_MOB_DROPS);
        if (looterLevel > 0) {
            ExperienceOrb.award(level, victim.position(), looterLevel * SkillTreeConfig.MOB_DROPS_BONUS_XP_PER_LEVEL);
        }
    }

    private void onDimensionChanged(ServerPlayer player, ServerLevel origin, ServerLevel destination) {
        String dimensionId = destination.dimension().identifier().toString();
        reporter.report(player, ObjectiveType.ENTER_DIMENSION, dimensionId, 1);
    }
}
