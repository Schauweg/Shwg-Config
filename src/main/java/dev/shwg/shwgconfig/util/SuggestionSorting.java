package dev.shwg.shwgconfig.util;

import dev.shwg.shwgconfig.gui.components.widget.editbox.suggestioneditbox.SuggestionEntry;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class SuggestionSorting {
    private SuggestionSorting() {}

    public static void sortByRelevance(List<? extends SuggestionEntry> entries, String query) {
        String lower = query.toLowerCase(Locale.ROOT);
        entries.sort(
                Comparator.<SuggestionEntry>comparingInt(e -> e.relevanceTier(lower))
                        .thenComparing(SuggestionEntry::sortingString, String.CASE_INSENSITIVE_ORDER)
        );
    }

    public static void filterByQuery(List<? extends SuggestionEntry> entries, String query) {
        String lower = query.toLowerCase(Locale.ROOT);
        entries.removeIf(entry -> !entry.matchesQuery(lower));
    }
}
