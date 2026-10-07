package dev.shwg.shwgconfig.util;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class CustomKeyMapping extends KeyMapping {

    private final Consumer<InputConstants.Key> save;

    public CustomKeyMapping(String translationKey, InputConstants.Type type, int defaultKey,
                            Identifier category,
                            Consumer<InputConstants.Key> save) {
        super(translationKey, type, defaultKey, KeybindCategoryResolver.getOrCreateCategory(category));
        this.save = save;
    }

    @Override
    public void setKey(InputConstants.@NotNull Key key) {
        super.setKey(key);
        save.accept(key);
    }

}
