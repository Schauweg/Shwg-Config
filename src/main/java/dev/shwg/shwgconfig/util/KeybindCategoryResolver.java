package dev.shwg.shwgconfig.util;

import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public class KeybindCategoryResolver {

    /*? if >= 1.21.9 { */
    public static final Map<Identifier, KeyMapping.Category> CATEGORY_CACHE = new HashMap<>();
    /*?} */

    /*~ if >= 1.21.9 '{@code KeyMapping.Category}' -> '{@link KeyMapping.Category}' { */
    /**
     * Converts a {@code Identifier} to the correct category type for the
     * current MC version, creating and caching new categories as needed.<br>
     * It's advisable to use your modID as the namespace and for the path the name of the category
     *
     * <ul>
     *   <li>&lt; 1.21.9: returns the derived String translation key {@code "key.category.namespace.path"}
     *   <li>&ge; 1.21.9: returns a {@link KeyMapping.Category} registered from the Identifier
     * </ul>
     *
     * <p>The Category translation key will look like this in all Minecraft versions:<br>
     * {@code "key.category.namespace.path"}
     * <p>For example the Identifier {@code shwgconfig:keybinds} will result in:<br>
     * {@code "key.category.shwgconfig.keybinds"}
     *
     */
    /*~ } */
    /*? if >= 1.21.9 { */
    public static KeyMapping.Category getOrCreateCategory(Identifier identifier) {
        return CATEGORY_CACHE.computeIfAbsent(identifier, key -> new KeyMapping.Category(identifier));
    }
    /*?} else {*/
    /*public static String getOrCreateCategory(Identifier resourceLocation) {
        return "key.category." + resourceLocation.getNamespace() + "." + resourceLocation.getPath();
    }
    *//*?}*/
}
