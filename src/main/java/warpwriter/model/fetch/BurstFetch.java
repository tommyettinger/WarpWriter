package warpwriter.model.fetch;

import warpwriter.model.Fetch;
import warpwriter.model.FetchModel;

public class BurstFetch extends Fetch {
    protected FetchModel debrisSource;

    public BurstFetch set(FetchModel debrisSource) {
        this.debrisSource = debrisSource;
        return this;
    }

    public FetchModel getModel() {
        return debrisSource;
    }

    protected int centerX = 0, centerY = 0, centerZ = 0, time = 0;

    public BurstFetch setX(int x) {
        this.centerX = x;
        return this;
    }

    public BurstFetch setY(int y) {
        this.centerY = y;
        return this;
    }

    public BurstFetch setZ(int z) {
        this.centerZ = z;
        return this;
    }

    public BurstFetch setCenter(int x, int y, int z) {
        return setX(x).setY(y).setZ(z);
    }

    public BurstFetch setTime(int time) {
        this.time = time;
        return this;
    }

    public int x() {
        return centerX;
    }

    public int y() {
        return centerY;
    }

    public int z() {
        return centerZ;
    }

    public int time() {
        return time;
    }

    @Override
    public Fetch fetch() {
        if (false) { // if there's debris at this cordinate at this time
            //return debrisSource.setChain(/*WHERE THE DEBRIS COMES FROM*/);
        }
        // if there's no debris at this coordinate at this time
        return getNextFetch();
    }
}
