package dev.shwg.testmod.config;

import dev.shwg.shwgconfig.gui.components.controller.list.ListRow;
import dev.shwg.shwgconfig.gui.components.widget.editbox.suggestioneditbox.SuggestionEditBox;
import dev.shwg.shwgconfig.gui.components.widget.editbox.suggestioneditbox.SuggestionProviders;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/** One item per row; items already used in another row are not suggested. */
public class ItemListRow extends ListRow<String> {

    public ItemListRow(@Nullable String initial) {
        super(initial);
        addWidget(SuggestionEditBox.builder(font(), Component.empty(), SuggestionProviders.excluding(SuggestionProviders.ITEMS, this::otherValues))
                .value(initial)
                .applyValue(this::commit)
                .size(160, 20)
                .build());
    }
}
