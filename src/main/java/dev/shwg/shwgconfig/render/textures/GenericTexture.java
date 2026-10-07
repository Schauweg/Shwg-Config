package dev.shwg.shwgconfig.render.textures;

import dev.shwg.shwgconfig.util.RegistryHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/*? if >= 1.19.3 { */
import net.minecraft.core.registries.BuiltInRegistries;
/*? } else { */
//import net.minecraft.core.Registry;
/*? } */

/**
 * Represents a general texture that can be drawn in the GUI.
 *
 * <p>This class is intended for textures that are <strong>not</strong> GUI sprites
 * (e.g. block textures, item textures, custom images, etc.).</p>
 *
 * <p>For GUI sprites located in {@code textures/gui/sprites/}, use {@link GenericSprite} instead.</p>
 */
public class GenericTexture {

    private final Identifier sprite; // atlas path (< 1.20.2) or sprite ID (>= 1.20.2)
    private final int atlasX, atlasY; // UV offset within the atlas
    private final int regionWidth, regionHeight; // sprite size within the atlas
    private final int atlasWidth, atlasHeight;  // full atlas dimensions, e.g. 256×256

    public static GenericTexture changedVanillaTexture(String atlasPath, int atlasX, int atlasY, int regionWidth, int regionHeight, int atlasWidth, int atlasHeight, String texturePath) {
        return new GenericTexture("minecraft", atlasPath, atlasX, atlasY, regionWidth, regionHeight, atlasWidth, atlasHeight, texturePath);
    }

    public static GenericTexture vanillaTexture(String texturePath, int width, int height) {
        return GenericTexture.customTexture("minecraft", texturePath, width, height);
    }

    public static GenericTexture customTexture(String namespace, String spritePath, int width, int height) {
        return new GenericTexture(namespace, spritePath, 0,0, width, height, width, height, spritePath);
    }

    public static GenericTexture fromBlock(Block block) {
        String rootPath = "textures/block/";
        /*? if >= 1.19.3 { */
        Identifier id = BuiltInRegistries.BLOCK.getKey(block);
        /*? } else { */
        //Identifier id = Registry.BLOCK.getKey(block);
        /*? } */
        String namespace = id.getNamespace();
        String path = id.getPath();
        return customTexture(namespace, rootPath + path + ".png", 16, 16);
    }

    public static GenericTexture fromItem(Item item) {
        String rootPath = "textures/item/";
        /*? if >= 1.19.3 { */
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        /*? } else { */
        //Identifier id = Registry.ITEM.getKey(item);
        /*? } */
        String namespace = id.getNamespace();
        String path = id.getPath();
        return customTexture(namespace, rootPath + path + ".png", 16, 16);
    }

    public GenericTexture(String namespace, String atlasPath, int atlasX, int atlasY, int regionWidth, int regionHeight, int atlasWidth, int atlasHeight, String texturePath) {
        this.atlasX = atlasX;
        this.atlasY = atlasY;
        this.regionWidth = regionWidth;
        this.regionHeight = regionHeight;
        this.atlasWidth = atlasWidth;
        this.atlasHeight = atlasHeight;

        /*? if >= 1.21 {*/
        this.sprite = RegistryHelper.build(namespace, texturePath);
        /*? } else if > 1.20.1 {*/
        //this.sprite = RegistryHelper.build(namespace, texturePath);
        /*? } else {*/
        //this.sprite = RegistryHelper.build(namespace, atlasPath);
         /*? } */
    }

    public Identifier getSprite() {
        return sprite;
    }

    public int atlasX() {
        return atlasX;
    }

    public int atlasY() {
        return atlasY;
    }

    public int regionWidth() {
        return regionWidth;
    }

    public int regionHeight() {
        return regionHeight;
    }

    public int atlasWidth() {
        return atlasWidth;
    }

    public int atlasHeight() {
        return atlasHeight;
    }

}
