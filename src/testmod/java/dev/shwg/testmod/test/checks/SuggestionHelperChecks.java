package dev.shwg.testmod.test.checks;

import dev.shwg.shwgconfig.gui.components.widget.editbox.suggestioneditbox.SuggestionEditBox.SuggestionProvider;
import dev.shwg.shwgconfig.gui.components.widget.editbox.suggestioneditbox.SuggestionEntry;
import dev.shwg.shwgconfig.gui.components.widget.editbox.suggestioneditbox.SuggestionProviders;
import dev.shwg.testmod.test.AutoTestReport;
import dev.shwg.testmod.test.CheckSuite;

import java.util.List;

import static dev.shwg.testmod.test.AutoTestReport.require;
import static dev.shwg.testmod.test.CheckHelpers.find;
import static dev.shwg.testmod.test.CheckHelpers.suggest;

/** The {@link SuggestionProviders} helpers: {@code strings(...)}, {@code excluding(...)} and search by ID. */
public final class SuggestionHelperChecks implements CheckSuite {

    @Override
    public String name() {
        return "suggestion-helpers";
    }

    @Override
    public void run(AutoTestReport report) {
        report.check("strings() filters by the query", () -> {
            List<SuggestionEntry> result = suggest(SuggestionProviders.strings(List.of("alpha", "beta", "gamma")), "et");
            require(result.size() == 1 && result.get(0).saveValue().equals("beta"), "expected only 'beta' but got " + ids(result));
        });

        report.check("strings() with an empty query returns everything", () -> {
            List<SuggestionEntry> result = suggest(SuggestionProviders.strings(List.of("alpha", "beta", "gamma")), "");
            require(result.size() == 3, "expected 3 entries but got " + ids(result));
        });

        report.check("excluding() hides only the excluded entry", () -> {
            SuggestionProvider withoutDiamond = SuggestionProviders.excluding(SuggestionProviders.ITEMS, () -> List.of("minecraft:diamond"));
            List<SuggestionEntry> result = suggest(withoutDiamond, "minecraft:diamond");
            require(find(result, "minecraft:diamond") == null, "diamond is still suggested");
            require(find(result, "minecraft:diamond_sword") != null, "other entries are gone too");
        });

        report.check("excluding() reads the excluded values every time", () -> {
            List<String> excluded = new java.util.ArrayList<>();
            SuggestionProvider provider = SuggestionProviders.excluding(SuggestionProviders.ITEMS, () -> excluded);
            require(find(suggest(provider, "minecraft:diamond"), "minecraft:diamond") != null, "diamond missing before excluding it");
            excluded.add("minecraft:diamond");
            require(find(suggest(provider, "minecraft:diamond"), "minecraft:diamond") == null, "diamond still there after excluding it");
        });

        report.check("search by ID part (namespace)", () ->
                require(find(suggest(SuggestionProviders.ITEMS, "minecraft:"), "minecraft:diamond") != null,
                        "searching 'minecraft:' doesn't find minecraft:diamond"));
    }

    private static List<String> ids(List<SuggestionEntry> entries) {
        return entries.stream().map(SuggestionEntry::saveValue).toList();
    }
}
