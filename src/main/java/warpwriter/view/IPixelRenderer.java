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
}
