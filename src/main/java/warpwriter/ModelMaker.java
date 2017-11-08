package warpwriter;

import squidpony.squidmath.StatefulRNG;
import squidpony.squidmath.ThrustAltRNG;

/**
 * Created by Tommy Ettinger on 11/4/2017.
 */
public class ModelMaker {
    // separate from the rng so we can call skip(), if needed, or use GreasedRegion stuff
    public ThrustAltRNG thrust;
    public StatefulRNG rng;
    public ModelMaker()
    {
        thrust = new ThrustAltRNG();
        rng = new StatefulRNG(thrust);
    }
    public ModelMaker(long seed)
    {
        thrust = new ThrustAltRNG(seed);
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
        int ctr = 0;
        do {
            ctr = 0;
            for (int x = 0; x < 12; x++) {
                for (int y = 0; y < 6; y++) {
                    for (int z = 1; z < 7; z++) {
                        if (y > (x <= 4 ? 2 - x : (x >> 1) - 2)) {
                            if ((voxels[x][11 - y][z] = voxels[x][y][z] =
                                    // + (60 - (x + 1) * (12 - x) + 6 - y) * 47
                                    (rng.nextIntHasty((11 - y * 2) * 23 +
                                            (Math.abs(x - 4) + 1) * (9 - y) * 15 +
                                            Math.abs(z - 3) * 255 +
                                            ((Math.abs(x - 4) + 3) * (Math.abs(z - 3) + 2) * (8 - y)) * 23) < 540) ?
                                            (rng.next(9) < 10) ? 2
                                                    : (rng.next(5) < 3) ? highlightColor : mainColor
                                            : 0) != 0) ctr++;
                        } else {
                            voxels[x][11 - y][z] = voxels[x][y][z] = 0;
                        }
                    }
                }
            }
        }while (ctr < 58);
        return Tools3D.runCA(voxels, 2);
    }
}
