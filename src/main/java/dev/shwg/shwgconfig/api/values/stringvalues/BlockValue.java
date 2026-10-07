package dev.shwg.shwgconfig.api.values.stringvalues;

import dev.shwg.shwgconfig.util.RegistryHelper;
import net.minecraft.world.level.block.Block;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;

/**
 * A config value that holds a Block and is saved as a string.
 * Used in Block search fields.
 */
public class BlockValue extends StringValue {

    private final Block defaultBlock;
    private Block block;


    public BlockValue(Block defaultValue) {
        super(fromBlock(defaultValue));
        this.defaultBlock = defaultValue;
        block = defaultBlock;
    }

    /**
     * Creates a new BlockStringValue with the given default value.
     *
     * @param defaultValue default {@link Block}, best to use {@link Blocks}
     * @return returns the {@link BlockValue}
     */
    public static BlockValue of(Block defaultValue) {
        return new BlockValue(defaultValue);
    }

    @Override
    public void setValue(String newValue) {
        super.setValue(newValue);
        block = fromString(newValue, defaultBlock);
    }

    /**
     * Set the new value
     *
     * @param block set the value via a {@link Block}
     */
    public void setValue(Block block) {
        super.setValue(fromBlock(block));
        this.block = block;
    }

    @Override
    public String getDisplayName() {
        return RegistryHelper.blockComponent(block).getString();
    }

    /**
     * Get the current saved {@link Block} value
     *
     * @return current {@link Block} value
     */
    public Block getBlock() {
        return block;
    }

    /**
     * Get the default {@link Block} value
     *
     * @return default {@link Block} value
     */
    public Block getDefaultBlock() {
        return defaultBlock;
    }

    private static String fromBlock(Block block) {
        return RegistryHelper.getID(block).toString();
    }

    private static Block fromString(String blockID, Block defaultBlock) {
        Identifier id = RegistryHelper.tryBuild(blockID);
        if (id == null) {
            return defaultBlock;
        }
        return RegistryHelper.getBlock(id);
    }
}
