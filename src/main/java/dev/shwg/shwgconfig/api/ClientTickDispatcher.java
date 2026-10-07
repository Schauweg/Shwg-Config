package dev.shwg.shwgconfig.api;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import dev.shwg.shwgconfig.api.values.stringvalues.KeybindValue;
import dev.shwg.shwgconfig.gui.input.events.GenericKeyEvent;

/*? if fabric { */
/*import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
*//*? } */
/*? if neoforge { */
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
 /*? } */
/*? if forge { */
/*import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
//
/^? if >= 1.21.6 { ^/
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
/^? } else { ^/
//import net.minecraftforge.eventbus.api.SubscribeEvent;
/^? } ^/
import net.minecraftforge.fml.common.Mod;
*//*? } */

/**
 * Utility for cross-platform client-side event dispatching.
 * <p>
 * The two main methods are:
 * <ul>
 *   <li>{@link #registerWorld(Runnable)} - world tick</li>
 *   <li>{@link #registerGUI(Consumer)} - GUI key presses</li>
 * </ul>
 * <p>
 * All event wiring is set up once by ShwgConfig itself via {@link #init()} - you
 * don't need to call it, and shouldn't; it's only public because it's shared
 * across every mod using this library.
 */
public class ClientTickDispatcher {

    private static final List<Runnable> WORLD_LISTENERS = new ArrayList<>();
    private static final List<Consumer<GenericKeyEvent>> GUI_LISTENERS = new ArrayList<>();

    /**
     * Registers a callback that will be executed on every client tick
     * while the player is in the world.
     * <p>
     * This is the recommended way to perform actions that need to run
     * every single game tick (e.g. updating timers, syncing data,
     * running periodic tasks, etc.).
     * <p>
     * <b>Especially useful for:</b> consuming keybinds that only work
     * in the world (e.g. {@link KeybindValue#consumeClick()}).
     * <p>
     * Example usage (recommended for your config library):
     * <pre>{@code
     * ClientTickDispatcher.registerWorld(() -> {
     *     while (TEST_CONFIG.keybindValue.consumeClick()) {
     *         System.out.println("Consuming Click in World");
     *     }
     * });
     * }</pre>
     *
     * @param listener the {@link Runnable} to invoke on each client tick
     * @see #registerGUI(Consumer)
     */
    public static void registerWorld(Runnable listener) {
        WORLD_LISTENERS.add(listener);
    }

    /**
     * Registers a callback that will be executed whenever a key is
     * pressed while a Minecraft GUI screen is open.
     * <p>
     * The event is wrapped in {@link GenericKeyEvent} so the listener
     * can safely access key code, scancode, modifiers, etc. regardless
     * of whether the mod is running on Fabric or Forge/NeoForge.
     * <p>
     * This is the primary method for handling GUI-specific keybinds
     * from your config library or your mod's own GUI (e.g. hotkeys,
     * chat input, navigation, etc.).
     * <p>
     * <b>Especially useful for:</b> mod menus, config screens, or any
     * custom GUI that needs to react to key presses.
     * <p>
     * Example usage (recommended for your config library):
     * <pre>{@code
     * ClientTickDispatcher.registerGUI(keyEvent -> {
     *     if (keyEvent.matches(TEST_CONFIG.keybindValue)) {
     *         System.out.println("Key Pressed in GUI");
     *     }
     * });
     * }</pre>
     *
     * @param listener the {@link Consumer} that receives the
     *                 {@link GenericKeyEvent} for the key press
     * @see #registerWorld(Runnable)
     */
    public static void registerGUI(Consumer<GenericKeyEvent> listener) {
        GUI_LISTENERS.add(listener);
    }

    /*? if fabric { */
    /*/^*
     * Sets up Fabric's world-tick and screen key-press events. Called once by ShwgConfig's
     * own client initializer - not meant to be called by mod authors. Safe to call
     * again (it would just re-register the same listeners), but never necessary.<br>
     * You can add to the events using {@link #registerWorld(Runnable)} and {@link #registerGUI(Consumer)}.
     ^/
    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> worldTick());
        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            /^? if >= 1.21.9 { ^/
            ScreenKeyboardEvents.beforeKeyPress(screen).register((s, keyEvent) -> {
                guiKeyPress(new GenericKeyEvent(keyEvent));
            });
            /^? } else { ^/
            /^ScreenKeyboardEvents.beforeKeyPress(screen).register((s, keyCode, scancode, modifiers) -> {
                guiKeyPress(new GenericKeyEvent(keyCode, scancode, modifiers));
            });
            ^//^? } ^/
        });
    }
    *//*? } */

    /*? if neoforge { */
    /**
     * <p>Registers this class on NeoForge's shared client event bus and registers the {@link ClientTickEvent.Post} and {@link ScreenEvent.KeyPressed.Pre} events.
     * </p>
     * <p>Called once by ShwgConfig's own client initializer - not meant to be called by mod authors.
     * Safe to call again (it would just re-register the same listener), but never necessary.<br>
     * You can add to them using {@link #registerWorld(Runnable)} and {@link #registerGUI(Consumer)}.
     * </p>
     */
    public static void init() {
        NeoForge.EVENT_BUS.register(ClientTickDispatcher.class);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        worldTick();
    }

    @SubscribeEvent
    public static void onScreenKeyPress(ScreenEvent.KeyPressed.Pre event) {
        /*? if >= 1.21.9 { */
        guiKeyPress(new GenericKeyEvent(event.getKeyEvent()));
        /*? } else {*/
        //guiKeyPress(new GenericKeyEvent(event.getKeyCode(), event.getScanCode(), event.getModifiers()));
        /*? } */
    }
    /*? } */

    /*? if forge { */
    /*/^*
     * <p>Registers this class on Forge's shared client event bus and registers the {@link TickEvent.ClientTickEvent} and {@link ScreenEvent.KeyPressed} events.
     * </p>
     * <p>Called once by ShwgConfig's own client initializer - not meant to be called by mod authors.
     * Safe to call again (it would just re-register the same listener), but never necessary.<br>
     * You can add to them using {@link #registerWorld(Runnable)} and {@link #registerGUI(Consumer)}.
     * </p>
     ^/
    public static void init() {
        /^? if >= 1.21.6 { ^/
        TickEvent.ClientTickEvent.Post.BUS.addListener(ClientTickDispatcher::onClientTick);
        ScreenEvent.KeyPressed.Pre.BUS.addListener(ClientTickDispatcher::onScreenKeyPress);
        /^? } else {^/
        //MinecraftForge.EVENT_BUS.register(ClientTickDispatcher.class);
        /^? } ^/
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        worldTick();
    }

    @SubscribeEvent
    public static void onScreenKeyPress(ScreenEvent.KeyPressed event) {
        /^? if >= 1.21.9 { ^/
            guiKeyPress(new GenericKeyEvent(event.getInfo()));
        /^? } else {^/
            //guiKeyPress(new GenericKeyEvent(event.getKeyCode(), event.getScanCode(), event.getModifiers()));
        /^? } ^/
    }
    *//*? } */

    private static void worldTick() {
        for (Runnable listener : WORLD_LISTENERS) {
            listener.run();
        }
    }

    private static void guiKeyPress(GenericKeyEvent event) {
        for (Consumer<GenericKeyEvent> listener : GUI_LISTENERS) {
            listener.accept(event);
        }
    }
}
