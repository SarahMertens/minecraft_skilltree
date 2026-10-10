package com.sarah.skilltree.presentation.effect;

import com.sarah.skilltree.config.SkillTreeConfig;
import com.sarah.skilltree.presentation.ServiceProvider;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalEntityTypeTags;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The skills that change what drops: Prospector (double ore drops), Smelter
 * (ores drop smelted) and Looter (double mob drops).
 *
 * <p>Hooks into the moment the game has rolled the drops of a loot table and
 * edits that list. This way the bonuses work together with enchantments like
 * Fortune and Looting instead of replacing them.</p>
 */
public final class LootEffects {

    private final ServiceProvider provider;

    public LootEffects(ServiceProvider provider) {
        this.provider = Objects.requireNonNull(provider, "provider");
    }

    public void register() {
        LootTableEvents.MODIFY_DROPS.register(this::modifyDrops);
    }

    private void modifyDrops(Holder<LootTable> table, LootContext context, List<ItemStack> drops) {
        if (drops.isEmpty() || !provider.isRunning()) {
            return;
        }
        if (table.value().getParamSet() == LootContextParamSets.BLOCK) {
            modifyBlockDrops(context, drops);
        } else if (table.value().getParamSet() == LootContextParamSets.ENTITY) {
            modifyMobDrops(context, drops);
        }
    }

    // ── Prospector and Smelter ──────────────────────────────────────────────

    private void modifyBlockDrops(LootContext context, List<ItemStack> drops) {
        BlockState state = context.getOptionalParameter(LootContextParams.BLOCK_STATE);
        Entity breaker = context.getOptionalParameter(LootContextParams.THIS_ENTITY);
        if (state == null || !state.is(ConventionalBlockTags.ORES)
                || !(breaker instanceof ServerPlayer player) || !provider.isReadyFor(player.getUUID())) {
            return;
        }
        double smeltChance = chance(player, SkillTreeConfig.SKILL_SMELTING, SkillTreeConfig.SMELTING_CHANCE_PER_LEVEL);
        double doubleChance = chance(player, SkillTreeConfig.SKILL_PROSPECTOR,
                SkillTreeConfig.PROSPECTOR_CHANCE_PER_LEVEL);
        RandomSource random = context.getRandom();
        List<ItemStack> extraDrops = new ArrayList<>();

        for (int i = 0; i < drops.size(); i++) {
            ItemStack drop = drops.get(i);
            // With Silk Touch the ore block itself drops. Leaving that alone
            // prevents "place it again, mine it again" from printing ores.
            if (drop.is(state.getBlock().asItem())) {
                continue;
            }
            if (random.nextDouble() < smeltChance) {
                drop = smelt(context.getLevel(), drop);
                drops.set(i, drop);
            }
            if (random.nextDouble() < doubleChance) {
                extraDrops.add(drop.copy());
            }
        }
        drops.addAll(extraDrops);
    }

    /** @return what a furnace would make of the stack, or the stack itself if it cannot be smelted */
    private ItemStack smelt(ServerLevel level, ItemStack stack) {
        SingleRecipeInput input = new SingleRecipeInput(stack);
        return level.recipeAccess().getRecipeFor(RecipeType.SMELTING, input, level)
                .map(recipe -> recipe.value().assemble(input, level.registryAccess()))
                .filter(result -> !result.isEmpty())
                .map(result -> result.copyWithCount(stack.getCount()))
                .orElse(stack);
    }

    // ── Looter ──────────────────────────────────────────────────────────────

    private void modifyMobDrops(LootContext context, List<ItemStack> drops) {
        Entity victim = context.getOptionalParameter(LootContextParams.THIS_ENTITY);
        Player killer = context.getOptionalParameter(LootContextParams.LAST_DAMAGE_PLAYER);
        if (!(victim instanceof LivingEntity) || victim instanceof Player
                || victim.getType().is(ConventionalEntityTypeTags.BOSSES)
                || !(killer instanceof ServerPlayer player) || !provider.isReadyFor(player.getUUID())) {
            return;
        }
        double doubleChance = chance(player, SkillTreeConfig.SKILL_MOB_DROPS,
                SkillTreeConfig.MOB_DROPS_CHANCE_PER_LEVEL);
        if (context.getRandom().nextDouble() < doubleChance) {
            List<ItemStack> copies = drops.stream().map(ItemStack::copy).toList();
            drops.addAll(copies);
        }
    }

    private double chance(ServerPlayer player, String skillId, double chancePerLevel) {
        return provider.services().skills().getSkillLevel(player.getUUID(), skillId) * chancePerLevel;
    }
}
