package dev.shwg.shwgconfig.render.textures;

/*? if < 1.20.2 { */
/*import com.google.gson.JsonObject;
import net.minecraft.server.packs.resources.Resource;
*//*? } */

/**
 * A GUI sprite that uses nine-slice (nine-patch) scaling.
 * Corners stay fixed while edges and center stretch or repeat.
 */
public class NineSlicedSprite extends GenericSprite {

    private final int left, top, right, bottom;
    private final boolean stretchInner;

    public NineSlicedSprite(String namespace, String atlasPath, int atlasX, int atlasY, int regionWidth, int regionHeight, int atlasWidth, int atlasHeight, String spritePath, int left, int top, int right, int bottom, boolean stretchInner) {
        super(namespace, atlasPath, atlasX, atlasY, regionWidth, regionHeight, atlasWidth, atlasHeight, spritePath);
        this.left = Math.max(0, left);
        this.top = Math.max(0, top);
        this.right = Math.max(0, right);
        this.bottom = Math.max(0, bottom);
        this.stretchInner = stretchInner;
    }

    public static NineSlicedSprite constantBorder(String namespace, String atlasPath, int atlasX, int atlasY, int regionWidth, int regionHeight, int atlasWidth, int atlasHeight, String spritePath, int border) {
        return constantBorder(namespace, atlasPath, atlasX, atlasY, regionWidth, regionHeight, atlasWidth, atlasHeight, spritePath, border, false);
    }

    public static NineSlicedSprite constantBorder(String namespace, String atlasPath, int atlasX, int atlasY, int regionWidth, int regionHeight, int atlasWidth, int atlasHeight, String spritePath, int border, boolean stretchInner) {
        return distinctBorder(namespace, atlasPath, atlasX, atlasY, regionWidth, regionHeight, atlasWidth, atlasHeight, spritePath, border, border, border, border, stretchInner);
    }

    public static NineSlicedSprite distinctBorder(String namespace, String atlasPath, int atlasX, int atlasY, int regionWidth, int regionHeight, int atlasWidth, int atlasHeight, String spritePath, int left, int top, int right, int bottom) {
        return distinctBorder(namespace, atlasPath, atlasX, atlasY, regionWidth, regionHeight, atlasWidth, atlasHeight, spritePath, left, top, right, bottom, false);
    }

    public static NineSlicedSprite distinctBorder(String namespace, String atlasPath, int atlasX, int atlasY, int regionWidth, int regionHeight, int atlasWidth, int atlasHeight, String spritePath, int left, int top, int right, int bottom, boolean stretchInner) {
        return new NineSlicedSprite(namespace, atlasPath, atlasX, atlasY, regionWidth, regionHeight, atlasWidth, atlasHeight, spritePath, left, top, right, bottom, stretchInner);
    }

    public static NineSlicedSprite fromMcMeta(String namespace, String path) {
        /*? if >= 1.20.2 { */
        return buildNineSlice(namespace, path);
        /*? } else { */
        /*path = "textures/gui/sprites/" + path + ".png";
        Resource mcMeta = getMcMetaResource(namespace, path);
        if (mcMeta == null) {
            throw new IllegalArgumentException("No mcmeta file found for " + namespace + ":" + path);
        }
        JsonObject scaling = getScaling(mcMeta);
        return buildNineSlice(namespace, path, scaling);
        *//*? } */
    }

    public int getLeft() {
        return left;
    }

    public int getTop() {
        return top;
    }

    public int getRight() {
        return right;
    }

    public int getBottom() {
        return bottom;
    }

    public boolean stretchInner() {
        return stretchInner;
    }
}
