package warpwriter.view;

/**
 * @author Ben McLean
 */
public interface IPixelRenderer {
    /**
     * Recommended implementation: return drawRect(x, y, 1, 1, color);
     */
    IPixelRenderer drawPixel(int x, int y, int color);

    IPixelRenderer drawRect(int x, int y, int sizeX, int sizeY, int color);

    /**
     * Recommended implementation: return drawRectVerticalFace(x, y, 1, 1, voxel);
     */
    IPixelRenderer drawPixelVerticalFace(int x, int y, byte voxel);

    IPixelRenderer drawRectVerticalFace(int x, int y, int sizeX, int sizeY, byte voxel);

    /**
     * Recommended implementation: return drawRectLeftFace(x, y, 1, 1, voxel);
     */
    IPixelRenderer drawPixelLeftFace(int x, int y, byte voxel);

    IPixelRenderer drawRectLeftFace(int x, int y, int sizeX, int sizeY, byte voxel);

    /**
     * Recommended implementation: return drawRectRightFace(x, y, 1, 1, voxel);
     */
    IPixelRenderer drawPixelRightFace(int x, int y, byte voxel);

    IPixelRenderer drawRectRightFace(int x, int y, int sizeX, int sizeY, byte voxel);
}
