package com.sarah.skilltree.presentation.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The Lumberjack skill: chopping one log brings down the rest of the tree.
 *
 * <p>Works in two steps. First the connected logs are collected with a
 * breadth-first search, without changing the world. Only if natural leaves
 * touch those logs is it a tree, and are the logs broken. A log cabin has no
 * natural leaves, so a player cannot wreck their own house by accident.</p>
 */
public final class TreeFeller {

    private TreeFeller() {
    }

    /** @return whether the player is chopping in a way that should fell the tree */
    public static boolean canFell(ServerPlayer player) {
        return !player.isShiftKeyDown() && player.getMainHandItem().is(ItemTags.AXES);
    }

    /**
     * Fells the tree that the (already broken) log at {@code origin} belonged to.
     *
     * @param maxLogs the most logs to break on top of the one the player chopped
     * @return the number of extra logs that were broken
     */
    public static int fell(ServerLevel level, ServerPlayer player, BlockPos origin, int maxLogs) {
        List<BlockPos> logs = findTree(level, origin, maxLogs);
        int felled = 0;
        for (BlockPos log : logs) {
            ItemStack axe = player.getMainHandItem();
            if (!axe.is(ItemTags.AXES)) {
                break; // the axe broke on the way
            }
            if (level.destroyBlock(log, true, player)) {
                axe.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                felled++;
            }
        }
        return felled;
    }

    /** @return the logs connected to {@code origin}, or an empty list if it is not a natural tree */
    private static List<BlockPos> findTree(ServerLevel level, BlockPos origin, int maxLogs) {
        List<BlockPos> logs = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        boolean hasNaturalLeaves = false;

        visited.add(origin);
        queue.add(origin);
        while (!queue.isEmpty() && logs.size() < maxLogs) {
            BlockPos current = queue.poll();
            for (BlockPos mutableNeighbour : BlockPos.betweenClosed(current.offset(-1, -1, -1), current.offset(1, 1, 1))) {
                BlockPos neighbour = mutableNeighbour.immutable();
                if (!visited.add(neighbour)) {
                    continue;
                }
                BlockState state = level.getBlockState(neighbour);
                if (state.is(BlockTags.LOGS)) {
                    if (logs.size() < maxLogs) {
                        logs.add(neighbour);
                        queue.add(neighbour);
                    }
                } else if (isNaturalLeaves(state)) {
                    hasNaturalLeaves = true;
                }
            }
        }
        return hasNaturalLeaves ? logs : List.of();
    }

    private static boolean isNaturalLeaves(BlockState state) {
        if (state.is(BlockTags.WART_BLOCKS)) {
            return true; // the "leaves" of the huge fungi in the Nether
        }
        return state.getBlock() instanceof LeavesBlock && !state.getValue(LeavesBlock.PERSISTENT);
    }
}
