package dev.shwg.testmod.test.checks;

import dev.shwg.shwgconfig.gui.components.widget.editbox.suggestioneditbox.SuggestionEditBox.SuggestionProvider;
import dev.shwg.shwgconfig.gui.components.widget.editbox.suggestioneditbox.SuggestionEntry;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Pattern;

import static dev.shwg.testmod.test.AutoTestReport.require;

public final class CheckHelpers {

    private static final Pattern RAW_TRANSLATION_KEY = Pattern.compile("^[a-z0-9_]+\\.minecraft\\.[a-z0-9_./]+$");

    private CheckHelpers() {}

    public static void requireTranslated(Component name, String id) {
        String text = name.getString();
        require(!text.isBlank(), "name is blank");
        require(!text.equals(id), "fell back to the raw id");
        require(!RAW_TRANSLATION_KEY.matcher(text).matches(), "translation key not resolved: " + text);
    }

    public static List<SuggestionEntry> suggest(SuggestionProvider provider, String query) {
        return provider.getSuggestions(query, null).join();
    }

    public static @Nullable SuggestionEntry find(List<SuggestionEntry> entries, String id) {
        for (SuggestionEntry entry : entries) {
            if (entry.saveValue().equals(id)) return entry;
        }
        return null;
    }

    public static SuggestionEntry requireEntry(List<SuggestionEntry> entries, String id) {
        SuggestionEntry entry = find(entries, id);
        require(entry != null, id + " is missing");
        return entry;
    }
}
