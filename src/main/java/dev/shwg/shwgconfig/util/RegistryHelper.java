package dev.shwg.shwgconfig.util;


import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import net.minecraft.core.Registry;

/*? if >= 1.19.3 { */
import net.minecraft.core.registries.BuiltInRegistries;
/*? } else { */
//import net.minecraft.data.BuiltinRegistries;
/*? } */

public class RegistryHelper {

    // Tools
    public static final int WOOD_TOOL_MAX_DAMAGE = 59;
    public static final int STONE_TOOL_MAX_DAMAGE = 131;
    public static final int COPPER_TOOL_MAX_DAMAGE = 190;
    public static final int IRON_TOOL_MAX_DAMAGE = 250;
    public static final int GOLD_TOOL_MAX_DAMAGE = 32;
    public static final int DIAMOND_TOOL_MAX_DAMAGE = 1561;
    public static final int NETHERITE_TOOL_MAX_DAMAGE = 2031;

    // Helmets
    public static final int LEATHER_HELMET_MAX_DAMAGE = 55;
    public static final int COPPER_HELMET_MAX_DAMAGE = 121;
    public static final int CHAINMAIL_HELMET_MAX_DAMAGE = 165;
    public static final int IRON_HELMET_MAX_DAMAGE = 165;
    public static final int GOLD_HELMET_MAX_DAMAGE = 77;
    public static final int DIAMOND_HELMET_MAX_DAMAGE = 363;
    public static final int NETHERITE_HELMET_MAX_DAMAGE = 407;

    // Chestplates
    public static final int LEATHER_CHESTPLATE_MAX_DAMAGE = 80;
    public static final int COPPER_CHESTPLATE_MAX_DAMAGE = 176;
    public static final int CHAINMAIL_CHESTPLATE_MAX_DAMAGE = 240;
    public static final int IRON_CHESTPLATE_MAX_DAMAGE = 240;
    public static final int GOLD_CHESTPLATE_MAX_DAMAGE = 112;
    public static final int DIAMOND_CHESTPLATE_MAX_DAMAGE = 528;
    public static final int NETHERITE_CHESTPLATE_MAX_DAMAGE = 592;

    // Leggings
    public static final int LEATHER_LEGGINGS_MAX_DAMAGE = 75;
    public static final int COPPER_LEGGINGS_MAX_DAMAGE = 165;
    public static final int CHAINMAIL_LEGGINGS_MAX_DAMAGE = 225;
    public static final int IRON_LEGGINGS_MAX_DAMAGE = 225;
    public static final int GOLD_LEGGINGS_MAX_DAMAGE = 105;
    public static final int DIAMOND_LEGGINGS_MAX_DAMAGE = 495;
    public static final int NETHERITE_LEGGINGS_MAX_DAMAGE = 555;

    // Boots
    public static final int LEATHER_BOOTS_MAX_DAMAGE = 65;
    public static final int COPPER_BOOTS_MAX_DAMAGE = 143;
    public static final int CHAINMAIL_BOOTS_MAX_DAMAGE = 195;
    public static final int IRON_BOOTS_MAX_DAMAGE = 195;
    public static final int GOLD_BOOTS_MAX_DAMAGE = 91;
    public static final int DIAMOND_BOOTS_MAX_DAMAGE = 429;
    public static final int NETHERITE_BOOTS_MAX_DAMAGE = 481;

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
                        if (reg.get(key) == value) {
                            return reg;
                        }
                    }
                }
            } catch (IllegalAccessException | ClassCastException ignored) {}
        }
        return null;
    }


    public static List<Identifier> getEnchantments() {

        return null;
    }

    public static List<Identifier> getBiomes() {

        return null;
    }

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

    public static Component enchantmentComponent(Identifier enchantment) {
        return component("enchantment", enchantment);
    }



}
