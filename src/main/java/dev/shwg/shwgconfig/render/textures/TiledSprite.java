package dev.shwg.shwgconfig.render.textures;

/**
 * A GUI sprite that tiles/repeats in both directions.
 */
public class TiledSprite extends GenericSprite {

    public TiledSprite(String namespace, String atlasPath, int atlasX, int atlasY, int regionWidth, int regionHeight, int atlasWidth, int atlasHeight, String spritePath) {
        super(namespace, atlasPath, atlasX, atlasY, regionWidth, regionHeight, atlasWidth, atlasHeight, spritePath);
    }
}
