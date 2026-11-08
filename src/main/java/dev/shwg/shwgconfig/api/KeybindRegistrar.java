package dev.shwg.shwgconfig.api;

import dev.shwg.shwgconfig.ShwgConfig;
import dev.shwg.shwgconfig.api.values.stringvalues.KeybindValue;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

/*? if >= 1.21.9 && forgeLike {*/
/*import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
*//*? } */

/*? if neoforge {*/
//import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
/*?}*/
/*? if forge {*/
//import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
/*?}*/
/*? if fabric {*/
/*? if >= 26 { */
//import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
/*? } else { */
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
/*? } */
/*? } */

/*~ if forgeLike '{@code #register}' -> '{@link #register}' { */
/**
 * Registers {@link KeyMapping}s created by {@link KeybindValue}
 * with the correct mod loader API.
 *
 * <p><b>On Fabric</b>, every {@link KeybindValue} registers itself
 * immediately when constructed.
 * </p>
 * <p><b>On NeoForge and Forge</b>, keybinds and their categories are registered through
 * {@code RegisterKeyMappingsEvent}. This is set up automatically by
 * {@code ShwgConfig.neoForgeInit} / {@code ShwgConfig.forgeInit}, which call
 * {@code #register} for your config.
 * </p>
 * <p>On every loader, you don't need to call anything from this class.</p>
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
    //private static final Set<Identifier> REGISTERED_CATEGORIES = ConcurrentHashMap.newKeySet();
    /*?}*/

    /*? if fabric { */
    public static void registerImmediately(KeyMapping keyMapping) {
        /*~ if >=26 'KeyBindingHelper.registerKeyBinding' -> 'KeyMappingHelper.registerKeyMapping' */
        KeyBindingHelper.registerKeyBinding(keyMapping);
    }
    /*? } else if forgeLike { */
    /*public static void register(RegisterKeyMappingsEvent event, ConfigBase config) {
        for (KeybindValue value : config.getKeybindValues()) {
            KeyMapping keyMapping = value.keyMapping();
            //? if >= 1.21.9 {
            KeyMapping.Category category = keyMapping.getCategory();
            if (REGISTERED_CATEGORIES.add(category.id())) {
                /^? if forge { ^/
                //KeyMapping.Category.register(category.id());
                /^? } else { ^/
                event.registerCategory(category);
                /^? } ^/
            }
            //?}
            event.register(keyMapping);
        }
    }
    *//*? } */
}
