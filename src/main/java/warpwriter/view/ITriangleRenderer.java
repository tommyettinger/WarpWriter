package warpwriter.view;

/**
 * @author Ben McLean
 */
public interface ITriangleRenderer {
    /**
     * Draws a triangle 3 high and 2 wide pointing left, representing the visible vertical face of voxel
     *
     * @return this
     */
    ITriangleRenderer drawLeftTriangleVerticalFace(int x, int y, byte voxel);

    /**
     * Draws a triangle 3 high and 2 wide pointing left, representing the left face of voxel
     *
     * @return this
     */
    ITriangleRenderer drawLeftTriangleLeftFace(int x, int y, byte voxel);

    /**
     * Draws a triangle 3 high and 2 wide pointing left, representing the right face of voxel
     *
     * @return this
     */
    ITriangleRenderer drawLeftTriangleRightFace(int x, int y, byte voxel);

    /**
     * Draws a triangle 3 high and 2 wide pointing right representing the visible vertical face of voxel
     *
     * @return this
     */
    ITriangleRenderer drawRightTriangleVerticalFace(int x, int y, byte voxel);

    /**
     * Draws a triangle 3 high and 2 wide pointing right representing the left face of voxel
     *
     * @return this
     */
    ITriangleRenderer drawRightTriangleLeftFace(int x, int y, byte voxel);

    /**
     * Draws a triangle 3 high and 2 wide pointing right representing the right face of voxel
     *
     * @return this
     */
    ITriangleRenderer drawRightTriangleRightFace(int x, int y, byte voxel);
}
