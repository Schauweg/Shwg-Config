package dev.shwg.shwgconfig.api.values.stringvalues;

import dev.shwg.shwgconfig.util.RegistryHelper;
import net.minecraft.world.item.Item;

import net.minecraft.resources.Identifier;


/**
 * A config value that holds an Item and is saved as a string.
 * Used in Item search fields.
 */
public class ItemValue extends StringValue {

    private final Item defaultItem;
    private Item item;

    public ItemValue(Item defaultValue) {
        super(fromItem(defaultValue));
        this.defaultItem = defaultValue;
        item = defaultItem;
    }

    /**
     * Creates a new ItemStringValue with the given default.
     * @param defaultValue default Item.
     */
    public static ItemValue of(Item defaultValue) {
        return new ItemValue(defaultValue);
    }

    @Override
    public void setValue(String newValue) {
        super.setValue(newValue);
        item = fromString(newValue, defaultItem);
    }

    public void setValue(Item item) {
        super.setValue(fromItem(item));
        this.item = item;
    }

    public Item getItem() {
        return item;
    }

    public Item getDefaultItem() {
        return defaultItem;
    }

    @Override
    public String getDisplayName() {
        return RegistryHelper.itemComponent(item).getString();
    }

    private static String fromItem(Item item) {
        return RegistryHelper.getID(item).toString();
    }

    private static Item fromString(String itemID, Item defaultItem) {
        Identifier id = RegistryHelper.tryBuild(itemID);
        if (id == null) {
            return defaultItem;
        }
        return RegistryHelper.getItem(id);
    }
}
