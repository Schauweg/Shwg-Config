package dev.shwg.testmod.config;

import dev.shwg.shwgconfig.gui.components.controller.list.ListRow;
import dev.shwg.shwgconfig.gui.components.widget.editbox.suggestioneditbox.SuggestionEditBox;
import dev.shwg.shwgconfig.gui.components.widget.editbox.suggestioneditbox.SuggestionEntry;
import dev.shwg.shwgconfig.gui.deferred.overlay.ListWidgetOverlay;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/** One item per row; items already used in another row are not suggested. */
public class ItemListRow extends ListRow<String> {

    public ItemListRow(@Nullable String initial) {
        super(initial);
        addWidget(SuggestionEditBox.builder(font(), Component.empty(), this::suggestions)
                .value(initial)
                .applyValue(this::commit)
                .size(160, 20)
                .build());
    }

    private CompletableFuture<List<SuggestionEntry>> suggestions(String query, ListWidgetOverlay<? extends SuggestionEntry> overlay) {
        List<String> used = otherValues();
        return SuggestionEditBox.getItemSuggestions(query, overlay).thenApply(entries -> {
            entries.removeIf(entry -> used.contains(entry.saveValue()));
            return entries;
        });
    }
}
