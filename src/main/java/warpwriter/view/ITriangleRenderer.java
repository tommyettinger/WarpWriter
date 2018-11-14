package warpwriter.view;

/**
 * @author Ben McLean
 */
public interface ITriangleRenderer {

    /**
     * Draws a triangle 3 high and 2 wide pointing left.
     *
     * @return this
     */
    ITriangleRenderer drawLeftTriangle(int x, int y, int color);

    /**
     * Draws a triangle 3 high and 2 wide pointing right.
     *
     * @return this
     */
    ITriangleRenderer drawRightTriangle(int x, int y, int color);
}
