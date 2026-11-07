package dev.shwg.testmod.test.checks;

import dev.shwg.shwgconfig.gui.components.widget.editbox.suggestioneditbox.SuggestionEditBox.SuggestionProvider;
import dev.shwg.shwgconfig.gui.components.widget.editbox.suggestioneditbox.SuggestionEntry;
import dev.shwg.shwgconfig.gui.components.widget.editbox.suggestioneditbox.SuggestionProviders;
import dev.shwg.shwgconfig.util.DisplayItemStack;
import dev.shwg.testmod.test.AutoTestReport;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static dev.shwg.testmod.test.AutoTestReport.require;
import static dev.shwg.testmod.test.checks.CheckHelpers.find;
import static dev.shwg.testmod.test.checks.CheckHelpers.requireEntry;
import static dev.shwg.testmod.test.checks.CheckHelpers.requireTranslated;
import static dev.shwg.testmod.test.checks.CheckHelpers.suggest;

public final class SuggestionProviderChecks implements CheckSuite {

    private record Case(String name, SuggestionProvider provider, String knownId, boolean translated, boolean hasIcon) {}

    private static final List<Case> CASES = List.of(
            new Case("items", SuggestionProviders.ITEMS, "minecraft:diamond", true, true),
            new Case("blocks", SuggestionProviders.BLOCKS, "minecraft:stone", true, true),
            new Case("enchantments", SuggestionProviders.ENCHANTMENTS, "minecraft:sharpness", true, true),
            new Case("biomes", SuggestionProviders.BIOMES, "minecraft:plains", true, false),
            new Case("sounds", SuggestionProviders.SOUNDS, "minecraft:block.note_block.pling", false, false),
            new Case("entities", SuggestionProviders.ENTITIES, "minecraft:pig", true, true)
    );

    @Override
    public String name() {
        return "suggestion-providers";
    }

    @Override
    public void run(AutoTestReport report) {
        List<String> counts = new ArrayList<>();

        for (Case c : CASES) {
            report.check(c.name + ": not empty", () ->
                    require(!suggest(c.provider, "").isEmpty(), "no suggestions at all"));

            report.check(c.name + ": contains " + c.knownId, () ->
                    requireEntry(suggest(c.provider, ""), c.knownId));

            report.check(c.name + ": found by its ID", () ->
                    require(find(suggest(c.provider, c.knownId), c.knownId) != null,
                            "searching '" + c.knownId + "' doesn't find it"));

            report.check(c.name + ": found by its name", () -> {
                SuggestionEntry entry = requireEntry(suggest(c.provider, ""), c.knownId);
                // The name in the current game language, so this works in any language.
                String name = entry.displayComponent().getString().toLowerCase(Locale.ROOT);
                require(find(suggest(c.provider, name), c.knownId) != null, "searching '" + name + "' doesn't find it");
            });

            if (c.translated) {
                report.check(c.name + ": name is translated", () ->
                        requireTranslated(requireEntry(suggest(c.provider, ""), c.knownId).displayComponent(), c.knownId));
            }

            report.check(c.name + ": icon " + (c.hasIcon ? "present" : "absent"), () -> {
                SuggestionEntry entry = requireEntry(suggest(c.provider, ""), c.knownId);
                require(entry.hasVisibleIcon() == c.hasIcon, "hasVisibleIcon() is " + entry.hasVisibleIcon());
            });

            counts.add(String.format("%-14s %s", c.name, count(c.provider)));
        }

        // The icon items the providers draw must exist on this version (entity icons: see registry-entities).
        report.check("icon stack: diamond", () ->
                require(!DisplayItemStack.getStackSimple(Items.DIAMOND).isEmpty(), "empty stack"));
        report.check("icon stack: enchanted book", () ->
                require(!DisplayItemStack.getStackSimple(Items.ENCHANTED_BOOK).isEmpty(), "empty stack"));

        report.section("SUGGESTION COUNTS", () -> counts);
    }

    private static String count(SuggestionProvider provider) {
        try {
            return String.valueOf(suggest(provider, "").size());
        } catch (Throwable t) {
            return "error: " + t;
        }
    }
}
