package dev.shwg.shwgconfig.api;

import dev.shwg.shwgconfig.api.values.stringvalues.KeybindValue;
import dev.shwg.shwgconfig.util.KeybindCategoryResolver;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

import java.util.*;

/*? if neoforge {*/
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
/*?}*/
/*? if forge {*/
//import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
/*?}*/
/*? if fabric {*/
/*/^? if >= 26 { ^/
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
/^? } else { ^/
//import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
/^? } ^/
*//*? } */

/*~ if forgeLike '{@code #registerAll}' -> '{@link #registerAll}', '{@code #enqueue}' -> '{@link #enqueue}' { */
/**
 * Registers {@link KeyMapping}s created by {@link KeybindValue}
 * with the correct mod loader API.
 *
 * <p><b>On Fabric, this is fully automatic</b> - every {@link KeybindValue} registers itself
 * immediately when constructed. You don't need to call anything from this class.
 * </p>
 * <p><b>On NeoForge and Forge</b>, keybind registration is tied to a per-mod event that only
 * your own mod's listeners receive, so one manual step is required: every {@link KeybindValue}
 * constructor queues itself via {@link #enqueue} and you call {@link #registerAll} once from
 * a listener on your mod's own event bus.
 * </p>
 *
 * <b style="font-size:1.2em">NeoForge</b>
 * <pre>{@code
 * public MyMod(IEventBus modEventBus, ModContainer modContainer) {
 *     ...
 *     modEventBus.addListener(KeybindRegistrar::registerAll);
 * }
 * }</pre>
 *
 * <b style="font-size:1.2em">Forge</b>
 * <pre>{@code
 * public MyMod() {
 *     ...
 *     FMLJavaModLoadingContext.get().getModEventBus().addListener(KeybindRegistrar::registerAll);
 * }
 * }</pre>
 *
 * <p><b style="font-size:1.2em">Category format</b>
 * Always pass category as a {@link Identifier}: {@code "mymod:keybinds"}.
 * It gets converted to the correct internal format per MC version automatically.
 * Add the translation key to your lang file:
 * </p>
 * {@code "key.category.mymod.keybinds": "My Mod Keybinds"}
 */
/*~ } */
public class KeybindRegistrar {

    /*? if >= 1.21.9 && forgeLike {*/
    private static final Set<Identifier> REGISTERED_CATEGORIES = new HashSet<>();
    /*?}*/

    private static final List<KeyMapping> PENDING = new ArrayList<>();

    /*? if forgeLike {*/
    public static void enqueue(KeyMapping keyMapping) {
        if (!PENDING.contains(keyMapping)) {
            PENDING.add(keyMapping);
        }
    }
    /*?}*/
    // Registration

    /*? if fabric { */
    /*/^*
     * Registers a KeyMapping with Fabric immediately. Called automatically by every
     * KeybindValue - mod authors never need to call this themselves.
     ^/
    public static void registerImmediately(KeyMapping keyMapping) {
        /^~ if >=26 'KeyBindingHelper.registerKeyBinding' -> 'KeyMappingHelper.registerKeyMapping' ^/
        KeyMappingHelper.registerKeyMapping(keyMapping);
    }
    *//*? } else if neoforge { */
    /**
     * Registers all pending KeyMappings and categories via the NeoForge event.
     * Annotate calling method with @SubscribeEvent on the mod event bus. <br>
     * Call inside Mod Constructor: <br>
     * {@code modEventBus.addListener(KeybindRegistrar::registerAll);}
     */
    public static void registerAll(RegisterKeyMappingsEvent event) {
        //? if >= 1.21.9 {
        for (Map.Entry<Identifier, KeyMapping.Category> entry : KeybindCategoryResolver.CATEGORY_CACHE.entrySet()) {
            if (REGISTERED_CATEGORIES.add(entry.getKey())) {
                event.registerCategory(entry.getValue());
            }
        }
        //?}
        for (KeyMapping km : PENDING) {
            event.register(km);
        }
        PENDING.clear();
    }
    /*? } else if forge { */
    /*/^*
     * Registers all pending KeyMappings via the Forge event.
     * Annotate calling method with @SubscribeEvent on the mod event bus. <br>
     * Call inside Mod Constructor: <br>
     * {@code FMLJavaModLoadingContext.get().getModEventBus().addListener(KeybindRegistrar::registerAll);}
     ^/
    public static void registerAll(RegisterKeyMappingsEvent event) {
        for (KeyMapping km : PENDING) {
            event.register(km);
        }
        PENDING.clear();
    }
    *//*? } */

    /** Returns all not-yet-registered KeyMappings. Useful for debugging. */
    public static List<KeyMapping> getPending() {
        return List.copyOf(PENDING);
    }
}
