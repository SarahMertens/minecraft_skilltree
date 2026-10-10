package com.sarah.skilltree.presentation.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.sarah.skilltree.config.SkillTreeConfig;
import com.sarah.skilltree.domain.dto.request.GrantSkillPointsRequest;
import com.sarah.skilltree.domain.dto.response.QuestResponse;
import com.sarah.skilltree.domain.dto.response.QuestState;
import com.sarah.skilltree.domain.dto.response.ResetSkillsResponse;
import com.sarah.skilltree.domain.dto.response.SkillResponse;
import com.sarah.skilltree.domain.dto.response.SkillState;
import com.sarah.skilltree.domain.dto.response.SkillTreeResponse;
import com.sarah.skilltree.domain.model.Skill;
import com.sarah.skilltree.presentation.ServiceProvider;
import com.sarah.skilltree.presentation.controller.SkillController;
import com.sarah.skilltree.presentation.message.Messages;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;

/**
 * The {@code /skilltree} chat command. Everything the screen can do is also
 * possible here, plus two commands for server operators.
 *
 * <pre>
 * /skilltree                              show points and skills
 * /skilltree upgrade &lt;skill&gt;              raise a skill by one level
 * /skilltree quests                       show the quest log
 * /skilltree admin addpoints &lt;player&gt; &lt;n&gt;  (operators) give skill points
 * /skilltree admin reset &lt;player&gt;         (operators) refund all skills
 * </pre>
 */
public final class SkillTreeCommand {

    private static final String ARG_SKILL = "skill";
    private static final String ARG_PLAYER = "player";
    private static final String ARG_AMOUNT = "amount";
    private static final SimpleCommandExceptionType NOT_READY =
            new SimpleCommandExceptionType(Component.translatable("skilltree.error.generic"));

    private final ServiceProvider provider;
    private final SkillController skillController;

    public SkillTreeCommand(ServiceProvider provider, SkillController skillController) {
        this.provider = Objects.requireNonNull(provider, "provider");
        this.skillController = Objects.requireNonNull(skillController, "skillController");
    }

    public void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) -> register(dispatcher));
    }

    private void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(SkillTreeConfig.COMMAND_ROOT)
                .executes(this::showSkills)
                .then(Commands.literal("upgrade")
                        .then(Commands.argument(ARG_SKILL, StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                        provider.skillCatalog().getAll().stream().map(Skill::getId), builder))
                                .executes(this::upgrade)))
                .then(Commands.literal("quests")
                        .executes(this::showQuests))
                .then(Commands.literal("admin")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.literal("addpoints")
                                .then(Commands.argument(ARG_PLAYER, EntityArgument.player())
                                        .then(Commands.argument(ARG_AMOUNT, IntegerArgumentType.integer(
                                                        1, SkillTreeConfig.MAX_ADMIN_POINT_GRANT))
                                                .executes(this::addPoints))))
                        .then(Commands.literal("reset")
                                .then(Commands.argument(ARG_PLAYER, EntityArgument.player())
                                        .executes(this::reset)))));
    }

    private int showSkills(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = readyPlayer(context.getSource().getPlayerOrException());
        SkillTreeResponse tree = provider.services().skills().getSkillTree(player.getUUID());

        MutableComponent message = Component.translatable("skilltree.command.skills.header", tree.skillPoints())
                .withStyle(ChatFormatting.GOLD);
        for (SkillResponse skill : tree.skills()) {
            message.append("\n").append(Component.translatable("skilltree.command.skills.line",
                            Messages.skillName(skill.id()), skill.level(), skill.maxLevel(), skill.costPerLevel())
                    .withStyle(colorOf(skill.state())));
        }
        context.getSource().sendSuccess(() -> message, false);
        return tree.skills().size();
    }

    private int showQuests(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = readyPlayer(context.getSource().getPlayerOrException());

        MutableComponent message = Component.translatable("skilltree.command.quests.header")
                .withStyle(ChatFormatting.GOLD);
        int shown = 0;
        for (QuestResponse quest : provider.services().quests().getQuests(player.getUUID())) {
            if (quest.state() != QuestState.ACTIVE) {
                continue; // the chat stays short; the screen shows everything
            }
            message.append("\n").append(Component.translatable("skilltree.command.quests.line",
                            Messages.questName(quest.id()), quest.progress(), quest.required())
                    .withStyle(ChatFormatting.WHITE));
            shown++;
        }
        context.getSource().sendSuccess(() -> message, false);
        return shown;
    }

    private int upgrade(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = readyPlayer(context.getSource().getPlayerOrException());
        return skillController.upgrade(player, StringArgumentType.getString(context, ARG_SKILL)) ? 1 : 0;
    }

    private int addPoints(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = readyPlayer(EntityArgument.getPlayer(context, ARG_PLAYER));
        int amount = IntegerArgumentType.getInteger(context, ARG_AMOUNT);

        int total = provider.services().skills()
                .grantSkillPoints(new GrantSkillPointsRequest(target.getUUID(), amount));
        skillController.refresh(target);
        context.getSource().sendSuccess(() -> Component.translatable("skilltree.command.admin.addpoints",
                amount, target.getDisplayName(), total), true);
        return total;
    }

    private int reset(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = readyPlayer(EntityArgument.getPlayer(context, ARG_PLAYER));

        ResetSkillsResponse response = provider.services().skills().resetSkills(target.getUUID());
        skillController.refresh(target);
        context.getSource().sendSuccess(() -> Component.translatable("skilltree.command.admin.reset",
                target.getDisplayName(), response.refundedSkillPoints()), true);
        return response.refundedSkillPoints();
    }

    private ServerPlayer readyPlayer(ServerPlayer player) throws CommandSyntaxException {
        if (!provider.isReadyFor(player.getUUID())) {
            throw NOT_READY.create();
        }
        return player;
    }

    private ChatFormatting colorOf(SkillState state) {
        return switch (state) {
            case LOCKED -> ChatFormatting.DARK_GRAY;
            case AVAILABLE -> ChatFormatting.WHITE;
            case MAXED -> ChatFormatting.GREEN;
        };
    }
}
