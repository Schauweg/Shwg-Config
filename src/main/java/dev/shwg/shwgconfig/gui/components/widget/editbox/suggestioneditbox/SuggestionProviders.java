package dev.shwg.shwgconfig.gui.components.widget.editbox.suggestioneditbox;

import dev.shwg.shwgconfig.gui.components.widget.editbox.suggestioneditbox.SuggestionEditBox.SuggestionProvider;
import dev.shwg.shwgconfig.util.DisplayItemStack;
import dev.shwg.shwgconfig.util.RegistryHelper;
import dev.shwg.shwgconfig.util.SuggestionSorting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Ready-made {@link SuggestionProvider}s for a {@link SuggestionEditBox}.
 *
 * <pre>{@code
 * SuggestionProviders.ITEMS                                   // every item, with its icon
 * SuggestionProviders.strings(List.of("Small", "Large"))     // a fixed list
 * SuggestionProviders.excluding(SuggestionProviders.ENCHANTMENTS, this::usedEnchantments)
 * }</pre>
 *
 * <p>Suggestions match the typed text against the display name and the saved value (the ID),
 * and are sorted by relevance. The registry providers read their IDs every time suggestions
 * open, so entries that appear later (e.g. enchantments of a server you joined) show up too.</p>
 */
public final class SuggestionProviders {

    private SuggestionProviders() {
    }

    /**
     * Every item, with its icon.
     */
    public static final SuggestionProvider ITEMS =
            registry(RegistryHelper::getItems, RegistryHelper::itemComponent, DisplayItemStack::getStackSimple);

    /**
     * Every block, with its item as icon (blocks without an item, like water, show none).
     */
    public static final SuggestionProvider BLOCKS =
            registry(RegistryHelper::getBlocks, RegistryHelper::itemComponent, DisplayItemStack::getStackSimple);

    /**
     * Every enchantment, with an enchanted book as icon. On 1.21+ this includes datapack and
     * server enchantments while a world is loaded; without a world, only vanilla ones.
     */
    public static final SuggestionProvider ENCHANTMENTS =
            registry(RegistryHelper::getEnchantments, RegistryHelper::enchantmentComponent, id -> enchantedBook());

    /**
     * Every biome. Includes datapack and server biomes while a world is loaded.
     */
    public static final SuggestionProvider BIOMES =
            registry(RegistryHelper::getBiomes, RegistryHelper::biomeComponent, null);

    /**
     * Every sound, including sounds added by resource packs.
     */
    public static final SuggestionProvider SOUNDS =
            registry(RegistryHelper::getSounds, RegistryHelper::soundComponent, null);

    /**
     * Every entity type, with its spawn egg as icon - found by name ({@code minecraft:pig} ->
     * {@code minecraft:pig_spawn_egg}), so entities without one (players, arrows, ...) show none.
     */
    public static final SuggestionProvider ENTITIES =
            registry(RegistryHelper::getEntityTypes, RegistryHelper::entityComponent, SuggestionProviders::spawnEgg);

    /**
     * Suggestions from a fixed list of strings; the string is both shown and saved.
     */
    public static SuggestionProvider strings(List<String> options) {
        return (query, overlay) -> {
            List<SuggestionEntry> results = new ArrayList<>();
            for (String option : options) {
                results.add(new StringSuggestionEntry(overlay, option));
            }
            return finish(results, query);
        };
    }

    /**
     * Suggestions for registry IDs - the ID is saved, {@code name} is shown.
     *
     * @param ids  read every time suggestions open
     * @param name the display name of an ID
     * @param icon the icon of an ID, or {@code null} for no icons
     */
    public static SuggestionProvider registry(Supplier<? extends Collection<Identifier>> ids,
                                              Function<Identifier, Component> name,
                                              @Nullable Function<Identifier, ItemStack> icon) {
        return (query, overlay) -> {
            List<SuggestionEntry> results = new ArrayList<>();
            for (Identifier id : ids.get()) {
                results.add(new RegistrySuggestionEntry(overlay, id, name, icon));
            }
            return finish(results, query);
        };
    }

    /**
     * {@code provider} without the entries whose saved value is in {@code excluded} - e.g. a list
     * row hiding what other rows already use. {@code excluded} is read every time suggestions open.
     */
    public static SuggestionProvider excluding(SuggestionProvider provider, Supplier<? extends Collection<String>> excluded) {
        return (query, overlay) -> {
            Collection<String> skip = excluded.get();
            return provider.getSuggestions(query, overlay).thenApply(entries -> {
                entries.removeIf(entry -> skip.contains(entry.saveValue()));
                return entries;
            });
        };
    }

    private static CompletableFuture<List<SuggestionEntry>> finish(List<SuggestionEntry> results, String query) {
        SuggestionSorting.filterByQuery(results, query);
        SuggestionSorting.sortByRelevance(results, query);
        return CompletableFuture.completedFuture(results);
    }

    private static final Map<Identifier, ItemStack> SPAWN_EGGS = new HashMap<>();

    private static ItemStack spawnEgg(Identifier entity) {
        return SPAWN_EGGS.computeIfAbsent(entity, id -> DisplayItemStack.getStackSimple(
                RegistryHelper.build(id.getNamespace(), id.getPath() + "_spawn_egg")));
    }

    private static @Nullable ItemStack enchantedBook;

    private static ItemStack enchantedBook() {
        if (enchantedBook == null) {
            enchantedBook = DisplayItemStack.getStackSimple(Items.ENCHANTED_BOOK);
        }
        return enchantedBook;
    }
}
