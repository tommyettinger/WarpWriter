package warpwriter.fetch;

/**
 * An abstraction for any types that allow querying a 3D position to get a color index (as a byte).
 * <br>
 * Created by Tommy Ettinger on 10/11/2018.
 */
public interface IFetch {
    /**
     * Looks up a color index (a byte) from a 3D position as x,y,z int parameters. Index 0 is used to mean an
     * empty position with no color.
     * @param x x position to look up; depending on angle, can be forward/back or left/right
     * @param y y position to look up; depending on angle, can be left/right or forward/back
     * @param z z position to look up; almost always up/down
     * @return a color index as a byte; 0 is empty, and this should usually be masked with {@code & 255} to get an index
     */
    byte at(int x, int y, int z);
}
