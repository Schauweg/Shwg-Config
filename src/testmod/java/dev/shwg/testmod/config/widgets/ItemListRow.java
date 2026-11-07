package dev.shwg.testmod.config.widgets;

import dev.shwg.shwgconfig.gui.components.controller.list.ListRow;
import dev.shwg.shwgconfig.gui.components.widget.editbox.suggestioneditbox.SuggestionEditBox;
import dev.shwg.shwgconfig.gui.components.widget.editbox.suggestioneditbox.SuggestionProviders;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

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
