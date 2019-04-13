package warpwriter.view.render;

/**
 * An IRectangleRenderer understands how to draw rectangles representing the three visible faces of a voxel cube.
 *
 * @author Ben McLean
 */
public interface IRectangleRenderer {
    IRectangleRenderer rect(int x, int y, int sizeX, int sizeY, int color);

    IRectangleRenderer rectVertical(int x, int y, int sizeX, int sizeY, byte voxel);

    IRectangleRenderer rectLeft(int x, int y, int sizeX, int sizeY, byte voxel);

    IRectangleRenderer rectRight(int x, int y, int sizeX, int sizeY, byte voxel);

    IRectangleRenderer rectVertical(int px, int py, int sizeX, int sizeY, byte voxel, int depth, int vx, int vy, int vz);

    IRectangleRenderer rectLeft(int px, int py, int sizeX, int sizeY, byte voxel, int depth, int vx, int vy, int vz);

    IRectangleRenderer rectRight(int px, int py, int sizeX, int sizeY, byte voxel, int depth, int vx, int vy, int vz);

}
