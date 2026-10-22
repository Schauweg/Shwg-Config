package dev.shwg.shwgconfig.util;


import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.item.enchantment.Enchantment;

/*? if >= 26 { */
//import net.minecraft.core.component.DataComponentPatch;
/*? } */

/*? if >= 1.21.2 { */
import net.minecraft.core.Holder;
 /*? } */



/*? if >= 1.21 { */
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
/*? } */

/*? if >= 1.19.3 { */
import net.minecraft.core.registries.BuiltInRegistries;
/*? } else { */
//import net.minecraft.core.Registry;
/*? } */

public class DisplayItemStack {

    /*? if >= 1.21 */
    private final List<Identifier> fakeEnchantments = new ArrayList<>();
    private final ItemStack stack;

    public DisplayItemStack(Item item) {
        this.stack = getStackSimple(item);
    }

    public static DisplayItemStack builder(Item item) {
        return new DisplayItemStack(item);
    }

    public DisplayItemStack withEnchantment (
            /*? if >= 1.21 { */
            ResourceKey<Enchantment> enchantment
            /*? } else { */
            //Enchantment enchantment
            /*? } */
    ) {
        /*? if >= 1.21 { */
        fakeEnchantments.add(RegistryHelper.getID(enchantment));
        /*? } else { */
        //stack.enchant(enchantment, 1);
        /*? } */
        return this;
    }

    public DisplayItemStack withDamage(int damage, int maxDamage) {
        /*? if >= 1.21 */
        stack.set(DataComponents.MAX_DAMAGE, maxDamage);
        stack.setDamageValue(damage);
        return this;
    }

    public DisplayItemStack withDamage(int maxDamage) {
        /*? if >= 1.21 */
        stack.set(DataComponents.MAX_DAMAGE, maxDamage);
        return this;
    }

    public ItemStack build() {
        /*? if >= 1.21 { */
        if (!this.fakeEnchantments.isEmpty()) {
            ListTag enchantmentList = new ListTag();
            for (Identifier location : fakeEnchantments) {
                enchantmentList.add(StringTag.valueOf(location.toString()));
            }

            CompoundTag customData = new CompoundTag();
            customData.put("shwgconfig:fake_enchantments", enchantmentList);

            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(customData));
            stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        }
        /*? } */
        return stack;
    }

    public static ItemStack getStackSimple(Item item, int count) {
        /*? if >= 26 { */
        /*return new ItemStack(
                Holder.direct(item),
                count,
                DataComponentPatch.builder()
                        .set(DataComponents.ITEM_MODEL, BuiltInRegistries.ITEM.getKey(item))
                        .set(DataComponents.MAX_STACK_SIZE, 64)
                        .build()
        );
        *//*? } else { */
        ItemStack stack = item.getDefaultInstance();
        stack.setCount(count);
        return stack;
         /*? } */
    }

    public static ItemStack getStackSimple(Item item) {
        return getStackSimple(item, 1);
    }

    public static ItemStack getStackSimple(Identifier id) {
        /*? if >= 1.21.2 {*/
        Optional<Holder.Reference<Item>> item = BuiltInRegistries.ITEM.get(id);
        if (item.isPresent()) {
            return getStackSimple(item.get().value());
        }
        return ItemStack.EMPTY;
        /*? } else if >= 1.19.3 { */
        //return getStackSimple(BuiltInRegistries.ITEM.get(id));
        /*? } else { */
        //return getStackSimple(Registry.ITEM.get(id));
        /*? } */
    }


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

}
