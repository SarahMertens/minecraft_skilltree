package com.sarah.skilltree.client;

import com.sarah.skilltree.client.screen.SkillTreeScreen;
import com.sarah.skilltree.config.SkillTreeConfig;
import com.sarah.skilltree.presentation.network.SkillTreeSyncPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

/**
 * Entry point for the client side: the key binding, the screen and receiving
 * data from the server. This code never runs on a dedicated server.
 */
public class SkillTreeClient implements ClientModInitializer {

    private final ClientState state = new ClientState();
    private KeyMapping openScreenKey;

    @Override
    public void onInitializeClient() {
        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath(SkillTreeConfig.MOD_ID, SkillTreeConfig.KEY_CATEGORY));
        openScreenKey = KeyBindingHelper.registerKeyBinding(
                new KeyMapping(SkillTreeConfig.KEY_OPEN_SCREEN, GLFW.GLFW_KEY_K, category));

        ClientPlayNetworking.registerGlobalReceiver(SkillTreeSyncPayload.TYPE,
                (payload, context) -> context.client().execute(() -> state.update(payload)));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> state.clear());
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(Minecraft client) {
        while (openScreenKey.consumeClick()) {
            if (client.player != null && client.screen == null) {
                client.setScreen(new SkillTreeScreen(state, openScreenKey));
            }
        }
    }
}
