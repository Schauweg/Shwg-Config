package dev.shwg.shwgconfig.util;


import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;

import net.minecraft.core.Registry;

/*? if >= 1.19.3 { */
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
/*? } else { */
//import net.minecraft.data.BuiltinRegistries;
/*? } */

public class RegistryHelper {

    public static Identifier getID(ResourceKey<?> resourceKey) {
        /*? if >= 1.21.11 { */
        return resourceKey.identifier();
        /*? } else { */
        //return resourceKey.location();
        /*? } */
    }

    public static Identifier build(String namespace, String path) {
        /*? if >= 1.21 {*/
        return Identifier.fromNamespaceAndPath(namespace, path);
        /*? } else {*/
        //return Identifier.tryBuild(namespace, path);
        /*? } */
    }

    public static Identifier tryBuild(String id) {
        /*? if >= 1.21 { */
        return Identifier.tryBySeparator(id, ':');
        /*? } else {*/
        //return Identifier.of(id, ':');
        /*? } */
    }

    @SuppressWarnings("unchecked")
    public static <T> List<Identifier> getRegistryEntries(Class<?> constantsClass) {
        List<Identifier> results = new ArrayList<>();
        Registry<T> classRegistry = null;

        for (Field field : constantsClass.getFields()) {
            try {
                Object value = field.get(null);
                if (value == null) continue;

                if (value instanceof ResourceKey<?> key) {
                    results.add(getID(key));
                } else {
                    if (classRegistry == null) {
                        classRegistry = discoverRegistry(value);
                    }
                    if (classRegistry != null) {
                        Identifier id = classRegistry.getKey((T) value);
                        if (id != null) results.add(id);
                    }
                }
            } catch (IllegalAccessException | ClassCastException ignored) {}
        }
        return results;
    }

    private static <T> Registry<T> discoverRegistry(Object value) {

        /*~ if >= 1.19.3 'net.minecraft.data.BuiltinRegistries' -> 'net.minecraft.core.registries.BuiltInRegistries' { */
        Registry<T> found = searchRegistryClass(net.minecraft.core.registries.BuiltInRegistries.class, value);
        /*~ } */
        if (found != null) return found;
        return searchRegistryClass(Registry.class, value);
    }

    @SuppressWarnings("unchecked")
    private static <T> Registry<T> searchRegistryClass(Class<?> registryClass, Object value) {
        for (Field field : registryClass.getFields()) {
            try {
                Object o = field.get(null);
                if (o instanceof Registry<?> registry) {
                    Registry<T> reg = (Registry<T>) registry;
                    Identifier key = reg.getKey((T) value);
                    if (key != null && reg.containsKey(key)) {
                        /*? if >= 1.21.2 { */
                        if (reg.getValue(key) == value) return reg;
                        /*? } else { */
                        //if (reg.get(key) == value) return reg;
                        /*? } */
                    }
                }
            } catch (IllegalAccessException | ClassCastException ignored) {}
        }
        return null;
    }

    /**
     * A registry from the loaded world - it includes entries added by datapacks or the server.
     * {@code null} without a world (e.g. on the title screen).
     */
    private static <T> @Nullable Registry<T> clientRegistry(ResourceKey<? extends Registry<? extends T>> key) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return null;
        /*? if >= 1.21.2 { */
        return level.registryAccess().lookup(key).orElse(null);
        /*? } else { */
        //return level.registryAccess().registry(key).orElse(null);
        /*? } */
    }

    /** The translation of "{prefix}.{namespace}.{path}", or the plain ID if there is none. */
    private static Component translatedOrId(String prefix, Identifier id) {
        String key = prefix + "." + id.getNamespace() + "." + id.getPath().replace('/', '.');
        return Language.getInstance().has(key) ? Component.translatable(key) : Component.literal(id.toString());
    }

    // --- Enchantments

    public static Set<Identifier> getEnchantments() {
        /*? if >= 1.21 { */
        // Vanilla enchantments are always known; with a world loaded, datapack / server ones are added.
        Set<Identifier> result = new LinkedHashSet<>(getRegistryEntries(Enchantments.class));
        Registry<Enchantment> registry = clientRegistry(Registries.ENCHANTMENT);
        if (registry != null) result.addAll(registry.keySet());
        return result;
        /*? } else if >= 1.19.3 { */
        //return BuiltInRegistries.ENCHANTMENT.keySet();
        /*? } else { */
        //return Registry.ENCHANTMENT.keySet();
        /*? } */
    }

    public static Component enchantmentComponent(Identifier id) {
        Enchantment enchantment = getEnchantment(id);
        if (enchantment != null) {
            /*? if >= 1.21 { */
            return enchantment.description();
            /*? } else { */
            //return Component.translatable(enchantment.getDescriptionId());
            /*? } */
        }
        return translatedOrId("enchantment", id);
    }

    public static Enchantment getEnchantment(Identifier id) {
        /*? if >= 1.21.2 { */
        Registry<Enchantment> registry = clientRegistry(Registries.ENCHANTMENT);
        return registry == null ? null : registry.get(id).map(Holder.Reference::value).orElse(null);
        /*? } else if >= 1.21 { */
        /*Registry<Enchantment> registry = clientRegistry(Registries.ENCHANTMENT);
        return registry == null ? null : registry.get(id);
        *//*? } else if >= 1.19.3 { */
        //return BuiltInRegistries.ENCHANTMENT.get(id);
        /*? } else { */
        //return Registry.ENCHANTMENT.get(id);
        /*? } */
    }

    // --- Biomes

    /*? if >= 1.19.3 { */
    private static final ResourceKey<Registry<Biome>> BIOME_REGISTRY = Registries.BIOME;
    /*? } else { */
    //private static final ResourceKey<Registry<Biome>> BIOME_REGISTRY = Registry.BIOME_REGISTRY;
    /*? } */

    public static Set<Identifier> getBiomes() {
        // Vanilla biomes are always known; with a world loaded, datapack / server biomes are added.
        Set<Identifier> result = new LinkedHashSet<>(getRegistryEntries(Biomes.class));
        Registry<Biome> registry = clientRegistry(BIOME_REGISTRY);
        if (registry != null) result.addAll(registry.keySet());
        return result;
    }

    public static Component biomeComponent(Identifier biome) {
        return translatedOrId("biome", biome);
    }

    // --- Sounds

    /** Every sound the sound manager knows, including sounds added by resource packs. */
    public static Set<Identifier> getSounds() {
        return new LinkedHashSet<>(Minecraft.getInstance().getSoundManager().getAvailableSounds());
    }

    /** Sounds have no translated names, so this is the plain ID. */
    public static Component soundComponent(Identifier sound) {
        return Component.literal(sound.toString());
    }

    // --- Items, blocks, entities

    public static Set<Identifier> getItems() {
        /*? if >= 1.19.3 { */
        return BuiltInRegistries.ITEM.keySet();
        /*? } else { */
        //return Registry.ITEM.keySet();
        /*? } */
    }

    public static Set<Identifier> getBlocks() {
        /*? if >= 1.19.3 { */
        return BuiltInRegistries.BLOCK.keySet();
        /*? } else { */
        //return Registry.BLOCK.keySet();
        /*? } */
    }

    public static Set<Identifier> getEntityTypes() {
        /*? if >= 1.19.3 { */
        return BuiltInRegistries.ENTITY_TYPE.keySet();
        /*? } else { */
        //return Registry.ENTITY_TYPE.keySet();
        /*? } */
    }

    public static Component component(String prefix, Identifier identifier) {
        return Component.translatable(prefix + "." + identifier.getNamespace() + "." + identifier.getPath().replace('/', '.'));
    }

    public static Component itemComponent(Item item){
        return itemComponent(getID(item));
    }

    public static Component itemComponent(Identifier item) {
        Item i = getItem(item);
        if (i == null) return Component.literal(item.toString());

        /*? if >= 1.21.2 { */
        return Component.translatable(i.getDescriptionId());
        /*? } else { */
        //return i.getDescription();
        /*? } */
    }

    public static Component blockComponent(Block block) {
        return blockComponent(getID(block));
    }

    public static Component blockComponent(Identifier block) {
        Block i = getBlock(block);
        if (i == null) return Component.literal(block.toString());

        /*? if >= 1.21.2 { */
        return Component.translatable(i.getDescriptionId());
        /*? } else { */
        //return i.getName();
        /*? } */
    }

    public static Component entityComponent(EntityType<?> entityType) {
        return entityComponent(getID(entityType));
    }

    public static Component entityComponent(Identifier entityType) {
        EntityType<?> i = getEntityType(entityType);
        if (i == null) return Component.literal(entityType.toString());

        /*? if >= 1.21.2 { */
        return Component.translatable(i.getDescriptionId());
        /*? } else { */
        //return i.getDescription();
        /*? } */
    }
    
    public static Item getItem(Identifier item) {
        /*? if >= 1.21.2 { */
        Optional<Holder.Reference<Item>> i = BuiltInRegistries.ITEM.get(item);
        //noinspection OptionalIsPresent
        if (i.isPresent()) {
            return i.get().value();
        }
        return null;
        /*? } else if >= 1.19.3 { */
        //return BuiltInRegistries.ITEM.get(item);
        /*? } else { */
        //return Registry.ITEM.get(item);
        /*? } */
    }

    public static Block getBlock(Identifier block) {
        /*? if >= 1.21.2 { */
        Optional<Holder.Reference<Block>> i = BuiltInRegistries.BLOCK.get(block);
        //noinspection OptionalIsPresent
        if (i.isPresent()) {
            return i.get().value();
        }
        return null;
        /*? } else if >= 1.19.3 { */
        //return BuiltInRegistries.BLOCK.get(block);
        /*? } else { */
        //return Registry.BLOCK.get(block);
        /*? } */
    }

    public static EntityType<?> getEntityType(Identifier entityType) {
        /*? if >= 1.21.2 { */
        Optional<Holder.Reference<EntityType<?>>> i = BuiltInRegistries.ENTITY_TYPE.get(entityType);
        //noinspection OptionalIsPresent
        if (i.isPresent()) {
            return i.get().value();
        }
        return null;
        /*? } else if >= 1.19.3 { */
        //return BuiltInRegistries.ENTITY_TYPE.get(entityType);
        /*? } else { */
        //return Registry.ENTITY_TYPE.get(entityType);
        /*? } */
    }

    public static Identifier getID(Item item) {
        /*? if >= 1.19.3 { */
        return BuiltInRegistries.ITEM.getKey(item);
        /*? } else { */
        //return Registry.ITEM.getKey(item);
        /*? } */
    }

    public static Identifier getID(Block block) {
        /*? if >= 1.19.3 { */
        return BuiltInRegistries.BLOCK.getKey(block);
        /*? } else { */
        //return Registry.BLOCK.getKey(block);
        /*? } */
    }

    public static Identifier getID(EntityType<?> entityType) {
        /*? if >= 1.19.3 { */
        return BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        /*? } else { */
        //return Registry.ENTITY_TYPE.getKey(entityType);
        /*? } */
    }

    /*? if < 1.21 { */
    /*public static Identifier getID(Enchantment enchantment) {
        /^? if >= 1.19.3 { ^/
        return BuiltInRegistries.ENCHANTMENT.getKey(enchantment);
        /^? } else { ^/
        //return Registry.ENCHANTMENT.getKey(enchantment);
        /^? } ^/
    }
    *//*? } */
}
