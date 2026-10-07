package dev.shwg.shwgconfig.api.values.stringvalues;

import com.mojang.blaze3d.platform.InputConstants;
import dev.shwg.shwgconfig.util.CustomKeyMapping;
import dev.shwg.shwgconfig.api.KeybindRegistrar;
import dev.shwg.shwgconfig.util.InputUtil;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

/**
 * A config value that stores a keybind as its {@link InputConstants.Key} name string
 * (e.g. {@code "key.keyboard.g"}, {@code "key.mouse.left"}).
 *
 * <p>Internally creates and manages a {@link KeyMapping} that can be automatically registered
 * with the mod loader via {@link KeybindRegistrar}.
 *
 * <p><b>Category format:</b> always pass a {@code Identifier} such as
 * {@code "mymod:actions"}. The library converts this to the correct internal format per
 * MC version. You still need to add the matching translation key to your lang file: <br>
 * ex: {@code "key.category.mymod.actions": "MyMod Action Keybinds"}
 *
 * <p><b>Usage in config class:</b>
 * <pre>{@code
 * public final KeybindValue myAction = KeybindValue.ofKeyboard(
 *     "key.mymod.my_action",  // translation key for the binding name used for label and Minecraft's own Keybind screen
 *     actions,                // category as Identtifer
 *     InputConstants.KEY_G    // default key code
 * );
 * }</pre>
 *
 * <p><b>Checking the binding in a tick event:</b>
 * <pre>{@code
 * while (config.myAction.consumeClick()) {
 *     // do something
 * }
 * }</pre>
 */
public class KeybindValue extends StringValue {

    private final CustomKeyMapping keyMapping;
    private InputConstants.Key key;

    private KeybindValue(String translationKey, Identifier category, InputConstants.Key defaultKey) {
        super(defaultKey.getName());
        this.keyMapping = createKeyMapping(translationKey, category, defaultKey);
        /*? if fabric { */
        //KeybindRegistrar.registerImmediately(this.keyMapping);
        /*? } else { */
        KeybindRegistrar.enqueue(this.keyMapping);
         /*? } */
         this.key = defaultKey;
    }

    private CustomKeyMapping createKeyMapping(String translationKey, Identifier category, InputConstants.Key defaultKey) {
        return new CustomKeyMapping(
                translationKey,
                defaultKey.getType(),
                defaultKey.getValue(),
                category,
                this::saveOnUpdate
        );
    }

    /**
     * Creates a KeybindValue from a raw {@link InputConstants} key name string as the default.
     * This is also the exact format used when loading from JSON.
     *
     * @param translationKey  Translation key for the binding name (e.g. {@code "key.mymod.jump"}) also used for label and Minecraft's Keybind Screen.
     * @param category        Category as an Identifier (e.g. {@code "mymod:actions"})
     * @param defaultKeyName  Default key as a name string (e.g. {@code "key.keyboard.g"})
     */
    public static KeybindValue of(String translationKey, Identifier category, String defaultKeyName) {
        return new KeybindValue(translationKey, category, parseKey(defaultKeyName));
    }

    /**
     * Creates a KeybindValue with a {@link InputConstants} keyboard key code as the default.
     *
     * @param translationKey  Translation key for the binding name (e.g. {@code "key.mymod.jump"}) also used for label and Minecraft's Keybind Screen.
     * @param category        Category as an Identifier (e.g. {@code "mymod:actions"})
     * @param defaultKeyCode  Default key code (e.g. {@link InputConstants#KEY_G})
     */
    public static KeybindValue ofKeyboard(String translationKey, Identifier category, int defaultKeyCode) {
        return new KeybindValue(translationKey, category, InputConstants.Type.KEYSYM.getOrCreate(defaultKeyCode));
    }

    /**
     * Creates a KeybindValue with a mouse button as the default.
     *
     * @param translationKey      Translation key for the binding name (e.g. {@code "key.mymod.jump"}) also used for label and Minecraft's Keybind Screen.
     * @param category            Category as an Identifier (e.g. {@code "mymod:actions"})
     * @param defaultMouseButton  Default mouse button (e.g. {@link InputConstants#MOUSE_BUTTON_LEFT})
     */
    public static KeybindValue ofMouse(String translationKey, Identifier category, int defaultMouseButton) {
        return new KeybindValue(translationKey, category, InputConstants.Type.MOUSE.getOrCreate(defaultMouseButton));
    }

    /**
     * Creates a KeybindValue with no default key assigned.
     *
     * @param translationKey  Translation key for the binding name (e.g. {@code "key.mymod.jump"}) also used for label and Minecraft's Keybind Screen.
     * @param category        as an Identifier (e.g. {@code "mymod:actions"})
     */
    public static KeybindValue unbound(String translationKey, Identifier category) {
        return new KeybindValue(translationKey, category, InputConstants.UNKNOWN);
    }

    /**
     * Returns the underlying {@link KeyMapping}.
     * Use this to check clicks in a tick event:
     * <pre>{@code while (myValue.keyMapping().consumeClick()) { ... }}</pre>
     */
    public KeyMapping keyMapping() {
        return keyMapping;
    }

    /**
     * Returns whether the underlying {@link KeyMapping} is consuming the click.
     * @return Boolean whether inner KeyMapping consumes the click
     */
    public boolean consumeClick() {
        return keyMapping.consumeClick();
    }

    /**
     * Check if key is being pressed.
     * @return returns true of key is being pressed
     */
    public boolean isKeyDown() {
        return InputUtil.isKeyDown(key.getValue());
    }

    /**
     * Always reads directly from the KeyMapping so changes made via the vanilla Controls screen
     * are automatically reflected without any manual sync step.
     */
    @Override
    public String getValue() {
        return keyMapping.saveString();
    }

    private void saveOnUpdate(InputConstants.Key key) {
        if (!this.value.equals(key.getName())) {
            notifyListeners(key.getName());
            KeyMapping.resetMapping();
            this.value = key.getName();
            this.key = key;
        }
    }

    /**
     * Updates both the string value and the underlying KeyMapping, then calls
     * {@link KeyMapping#resetMapping()} to recalculate conflict states.
     *
     * @param newValue A valid {@link InputConstants} key name string (e.g. {@code "key.keyboard.r"}).
     *                 Invalid strings gracefully fall back to {@link InputConstants#UNKNOWN}.
     */
    @Override
    public void setValue(String newValue) {
        InputConstants.Key parsed = parseKey(newValue);
        keyMapping.setKey(parsed);
    }

    @Override
    protected boolean isValid(String value) {
        return value != null; // invalid key strings are handled gracefully in setValue via parseKey
    }

    private static InputConstants.Key parseKey(String keyName) {
        if (keyName == null || keyName.isEmpty()) return InputConstants.UNKNOWN;
        try {
            return InputConstants.getKey(keyName);
        } catch (Exception e) {
            return InputConstants.UNKNOWN;
        }
    }
}
