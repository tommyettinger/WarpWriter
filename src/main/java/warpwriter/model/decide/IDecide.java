package warpwriter.model.decide;

/**
 * An abstraction for any types that allow querying a 3D position to get a decision. (as a boolean)
 * <p>
 * Created by Ben McLean on 2018-10-26
 *
 * @author Ben McLean
 */
public interface IDecide {
    /**
     * Looks up a decision (a boolean) from a 3D position as x,y,z int parameters.
     *
     * @param x x position to look up; depending on angle, can be forward/back or left/right
     * @param y y position to look up; depending on angle, can be left/right or forward/back
     * @param z z position to look up; almost always up/down
     * @return a decision
     */
    boolean bool(int x, int y, int z);
}
