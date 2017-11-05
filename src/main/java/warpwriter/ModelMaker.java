package warpwriter;

import squidpony.squidmath.StatefulRNG;
import squidpony.squidmath.ThrustRNG;

/**
 * Created by Tommy Ettinger on 11/4/2017.
 */
public class ModelMaker {
    // separate from the rng so we can call skip(), if needed, or use GreasedRegion stuff
    public ThrustRNG thrust;
    public StatefulRNG rng;
    public ModelMaker()
    {
        thrust = new ThrustRNG();
        rng = new StatefulRNG(thrust);
    }
    public ModelMaker(long seed)
    {
        thrust = new ThrustRNG(seed);
        rng = new StatefulRNG(thrust);
    }
    public byte[][][] fullyRandom()
    {
        byte[][][] voxels = new byte[12][12][8];
        byte mainColor = (byte)((rng.nextIntHasty(22) << 3) + rng.between(10, 13)),
                highlightColor = (byte)((rng.nextIntHasty(22) << 3) + rng.between(11, 13));
        for (int x = 0; x < 12; x++) {
            for (int y = 0; y < 12; y++) {
                for (int z = 0; z < 8; z++) {
                    voxels[x][y][z] = (rng.next(4) < 7) ? 0 : (rng.next(5) == 0) ? highlightColor : mainColor;
                }
            }
        }
        return voxels;
    }
    public byte[][][] shipRandom()
    {
        byte[][][] voxels = new byte[12][12][8];
        byte mainColor = (byte)((rng.nextIntHasty(22) << 3) + rng.between(10, 13)),
                highlightColor = (byte)((rng.nextIntHasty(22) << 3) + rng.between(11, 13));
        for (int x = 0; x < 12; x++) {
            for (int y = 0; y < 6; y++) {
                if(y > (x <= 3 ? 2 - x : (x >> 1) - 2)) {
                    for (int z = 2; z < 7; z++) {
                        voxels[x][11 - y][z] = voxels[x][y][z] =
                                // + (60 - (x + 1) * (12 - x) + 6 - y) * 47
                                (rng.nextIntHasty((6 - y) * 67 + (Math.abs(x - 3) + 1) * (9 - y) * 21 + Math.abs(z - 4) * 37 + ( (60 - (x + 1) * (12 - x)) * (Math.abs(z - 4) + 2) * (8 - y)) * 5 + 10) < 320) ?
                                        (rng.next(4) == 0) ? highlightColor : mainColor
                                        : 0;
                    }
                }
            }
        }
        return voxels;
    }
}
