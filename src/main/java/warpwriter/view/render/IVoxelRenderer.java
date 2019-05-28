package warpwriter.view.render;

public interface IVoxelRenderer {
    /**
     * @param transparency Anything from 0 for fully transparency to Byte.MAX_VALUE for fully opaque.
     * @return this
     */
    IVoxelRenderer setTransparency(byte transparency);

    byte transparency();
}
