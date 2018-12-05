package warpwriter.view;

/**
 * @author Ben McLean
 */
public interface IPixelRenderer {
    /**
     * Recommended implementation: return drawRect(x, y, 1, 1, color);
     */
    IPixelRenderer drawPixel(int x, int y, int color);

    IPixelRenderer drawRect(int x, int y, int xSize, int ySize, int color);

    IPixelRenderer drawPixelVerticalFace(int x, int y, byte voxel);

    IPixelRenderer drawPixelLeftFace(int x, int y, byte voxel);

    IPixelRenderer drawPixelRightFace(int x, int y, byte voxel);
}
