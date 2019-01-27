package warpwriter.model.fetch;

import warpwriter.Tools3D;
import warpwriter.model.Fetch;
import warpwriter.model.IFetch;
import warpwriter.model.IModel;
import warpwriter.model.ITemporal;

/**
 * An IModel that wraps a 4D byte array, with the outermost array storing normal 3D arrays that can be used as ordinary
 * models, and the index in that outermost array used for the animation frame. Uses the 3D arrays' bounds to implement
 * {@link #sizeX()} and its related methods and using its contents to implement {@link #bite()} (out-of-bounds requests
 * are handled by returning 0, and the current frame will affect which 3D array will be drawn).
 * <p>
 * Created by Tommy Ettinger on 1/26/2019.
 * @author Tommy Ettinger
 */
public class AnimatedArrayModel extends Fetch implements IModel, ITemporal {
    public byte[][][][] voxels;
    protected int frame = 0;
    
    public AnimatedArrayModel() {
        this(new byte[8][12][12][8]);
    }

    public AnimatedArrayModel(byte[][][][] voxels) {
        this.voxels = voxels;
    }

    public AnimatedArrayModel(IFetch fetch, int duration, int xSize, int ySize, int zSize) {
        voxels = new byte[duration][xSize][ySize][zSize];
        for (int x = 0; x < sizeX(); x++) {
            for (int y = 0; y < sizeY(); y++) {
                for (int z = 0; z < sizeZ(); z++) { 
                    voxels[0][x][y][z] = fetch.at(x, y, z);
                }
            }
        }
        for (int i = 1; i < duration; i++) {
            Tools3D.deepCopyInto(voxels[0], voxels[i]);
        }
    }

    public AnimatedArrayModel(IModel model) {
        this(model, 8, model.sizeX(), model.sizeY(), model.sizeZ());
    }

    @Override
    public int duration() {
        return voxels.length;
    }

    /**
     * This is a no-op; the duration cannot be changed after creation.
     * @param duration ignored
     * @return this for chaining
     */
    @Override
    public ITemporal setDuration(int duration) {
        return this;
    }

    @Override
    public int frame() {
        return frame;
    }

    @Override
    public ITemporal setFrame(int frame) {
        final int d = duration();
        this.frame = ((frame % d) + d) % d;
        return this;
    }

    /**
     * Gets the x size of the IModel, with requests for x limited between 0 (inclusive) to sizeX() (exclusive).
     *
     * @return the size of the x dimension of the IModel
     */
    @Override
    public int sizeX() {
        return voxels[0].length;
    }

    /**
     * Gets the y size of the IModel, with requests for y limited between 0 (inclusive) to sizeY() (exclusive).
     *
     * @return the size of the y dimension of the IModel
     */
    @Override
    public int sizeY() {
        return voxels[0][0].length;
    }

    /**
     * Gets the z size of the IModel, with requests for z limited between 0 (inclusive) to sizeZ() (exclusive).
     *
     * @return the size of the z dimension of the IModel
     */
    @Override
    public int sizeZ() {
        return voxels[0][0][0].length;
    }

    /**
     * Looks up a color index (a byte) from a 3D position as x,y,z int parameters. Index 0 is used to mean an
     * empty position with no color.
     *
     * @return a color index as a byte; 0 is empty, and this should usually be masked with {@code & 255} to get an index
     */
    @Override
    public Fetch fetch() {
        return deferFetch(bite());
    }

    @Override
    public byte bite() {
        int x = chainX(), y = chainY(), z = chainZ();
        return deferByte(inside(x, y, z) ? voxels[frame][x][y][z] : (byte) 0);
    }

    @Override
    public boolean outside(int x, int y, int z) {
        return x < 0 || y < 0 || z < 0 || x >= sizeX() || y >= sizeY() || z >= sizeZ();
    }

    @Override
    public boolean inside(int x, int y, int z) {
        return !outside(x, y, z);
    }
}
