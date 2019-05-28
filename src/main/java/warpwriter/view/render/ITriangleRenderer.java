package warpwriter.view.render;

/**
 * An ITriangleRenderer understands how to draw triangles where each triangle represents half of one of the diamonds making up one of the three visible faces of a voxel cube from an isometric perspective.
 *
 * @author Ben McLean
 */
public interface ITriangleRenderer extends IVoxelRenderer {
    /**
     * Draws a triangle 3 high and 2 wide pointing left
     *
     * @return this
     **/
    ITriangleRenderer drawLeftTriangle(int x, int y, int color);

    /**
     * Draws a triangle 3 high and 2 wide pointing right
     *
     * @return this
     **/
    ITriangleRenderer drawRightTriangle(int x, int y, int color);

    /**
     * Draws a triangle 3 high and 2 wide pointing left, representing the visible vertical face of voxel
     *
     * @return this
     */
    ITriangleRenderer drawLeftTriangleVerticalFace(int x, int y, byte voxel, int vx, int vy, int vz);

    /**
     * Draws a triangle 3 high and 2 wide pointing left, representing the left face of voxel
     *
     * @return this
     */
    ITriangleRenderer drawLeftTriangleLeftFace(int x, int y, byte voxel, int vx, int vy, int vz);

    /**
     * Draws a triangle 3 high and 2 wide pointing left, representing the right face of voxel
     *
     * @return this
     */
    ITriangleRenderer drawLeftTriangleRightFace(int x, int y, byte voxel, int vx, int vy, int vz);

    /**
     * Draws a triangle 3 high and 2 wide pointing right representing the visible vertical face of voxel
     *
     * @return this
     */
    ITriangleRenderer drawRightTriangleVerticalFace(int x, int y, byte voxel, int vx, int vy, int vz);

    /**
     * Draws a triangle 3 high and 2 wide pointing right representing the left face of voxel
     *
     * @return this
     */
    ITriangleRenderer drawRightTriangleLeftFace(int x, int y, byte voxel, int vx, int vy, int vz);

    /**
     * Draws a triangle 3 high and 2 wide pointing right representing the right face of voxel
     *
     * @return this
     */
    ITriangleRenderer drawRightTriangleRightFace(int x, int y, byte voxel, int vx, int vy, int vz);

    /**
     * Draws a triangle 3 high and 2 wide pointing left, representing the visible vertical face of voxel
     *
     * @return this
     */
    ITriangleRenderer drawLeftTriangleVerticalFace(int x, int y, byte voxel, int depth, int vx, int vy, int vz);

    /**
     * Draws a triangle 3 high and 2 wide pointing left, representing the left face of voxel
     *
     * @return this
     */
    ITriangleRenderer drawLeftTriangleLeftFace(int x, int y, byte voxel, int depth, int vx, int vy, int vz);

    /**
     * Draws a triangle 3 high and 2 wide pointing left, representing the right face of voxel
     *
     * @return this
     */
    ITriangleRenderer drawLeftTriangleRightFace(int x, int y, byte voxel, int depth, int vx, int vy, int vz);

    /**
     * Draws a triangle 3 high and 2 wide pointing right representing the visible vertical face of voxel
     *
     * @return this
     */
    ITriangleRenderer drawRightTriangleVerticalFace(int x, int y, byte voxel, int depth, int vx, int vy, int vz);

    /**
     * Draws a triangle 3 high and 2 wide pointing right representing the left face of voxel
     *
     * @return this
     */
    ITriangleRenderer drawRightTriangleLeftFace(int x, int y, byte voxel, int depth, int vx, int vy, int vz);

    /**
     * Draws a triangle 3 high and 2 wide pointing right representing the right face of voxel
     *
     * @return this
     */
    ITriangleRenderer drawRightTriangleRightFace(int x, int y, byte voxel, int depth, int vx, int vy, int vz);

}
