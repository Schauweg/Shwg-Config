package dev.shwg.shwgconfig.render.textures;

/**
 * A GUI sprite that stretches to fit the target area (default behavior if no .mcmeta is present).
 */
public class StretchedSprite extends GenericSprite {

    public StretchedSprite(String namespace, String atlasPath, int atlasX, int atlasY, int regionWidth, int regionHeight, int atlasWidth, int atlasHeight, String spritePath) {
        super(namespace, atlasPath, atlasX, atlasY, regionWidth, regionHeight, atlasWidth, atlasHeight, spritePath);
    }
}
