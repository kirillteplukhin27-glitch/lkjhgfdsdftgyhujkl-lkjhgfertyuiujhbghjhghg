package com.example.toggleinteract;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Files;
import java.nio.file.Path;

public class ToggleInteractClient implements ClientModInitializer {
    /** Режим 1: блоки ставятся на сундуки/печки/верстаки без шифта. */
    public static boolean disabled = false;
    /** Режим 2: полёт без скольжения. */
    public static boolean noDrift = false;

    private static KeyMapping interactKey;
    private static KeyMapping flightKey;

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("toggleinteract.txt");
    }

    private static void loadState() {
        try {
            Path p = configPath();
            if (!Files.exists(p)) return;
            for (String line : Files.readAllLines(p)) {
                String[] kv = line.trim().split("=");
                if (kv.length != 2) continue;
                if (kv[0].equals("interact")) disabled = kv[1].equals("on");
                if (kv[0].equals("flight")) noDrift = kv[1].equals("on");
            }
        } catch (Exception ignored) {}
    }

    private static void saveState() {
        try {
            Files.writeString(configPath(),
                    "interact=" + (disabled ? "on" : "off") + "\n"
                  + "flight=" + (noDrift ? "on" : "off") + "\n");
        } catch (Exception ignored) {}
    }

    @Override
    public void onInitializeClient() {
        loadState();

        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath("toggleinteract", "main"));

        interactKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.toggleinteract.toggle",
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, category));

        flightKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.toggleinteract.flight",
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, category));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            stopFlightDrift(mc);

            while (interactKey.consumeClick()) {
                disabled = !disabled;
                saveState();
                msg(mc, disabled ? "§6Блоки без шифта: ВКЛ" : "§7Блоки без шифта: ВЫКЛ");
            }
            while (flightKey.consumeClick()) {
                noDrift = !noDrift;
                saveState();
                msg(mc, noDrift ? "§6Полёт без скольжения: ВКЛ" : "§7Полёт без скольжения: ВЫКЛ");
            }
        });
    }

    private static void msg(Minecraft mc, String text) {
        if (mc.player != null) mc.player.displayClientMessage(Component.literal(text), true);
    }

    /** Убирает скольжение (инерцию) при полёте в творческом режиме. */
    private static void stopFlightDrift(Minecraft mc) {
        if (!noDrift || mc.player == null) return;
        if (!mc.player.getAbilities().flying) return;

        Input in = mc.player.input.keyPresses;
        Vec3 v = mc.player.getDeltaMovement();
        double x = v.x, y = v.y, z = v.z;

        if (!in.forward() && !in.backward() && !in.left() && !in.right()) {
            x = 0;
            z = 0;
        }
        if (!in.jump() && !in.shift()) {
            y = 0;
        }
        mc.player.setDeltaMovement(x, y, z);
    }
}
