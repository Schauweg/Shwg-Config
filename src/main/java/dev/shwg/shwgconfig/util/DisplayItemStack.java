package dev.shwg.shwgconfig.util;


import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.item.enchantment.Enchantment;

/*? if >= 26 { */
import net.minecraft.core.component.DataComponentPatch;
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
        return new ItemStack(
                Holder.direct(item),
                count,
                DataComponentPatch.builder()
                        .set(DataComponents.ITEM_MODEL, BuiltInRegistries.ITEM.getKey(item))
                        .set(DataComponents.MAX_STACK_SIZE, 64)
                        .build()
        );
        /*? } else { */
        /*ItemStack stack = item.getDefaultInstance();
        stack.setCount(count);
        return stack;
         *//*? } */
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
}
