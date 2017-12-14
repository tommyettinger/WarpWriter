package warpwriter;

import squidpony.squidmath.StatefulRNG;
import squidpony.squidmath.ThrustAltRNG;
import squidpony.squidmath.WhirlingNoise;

import java.io.InputStream;

/**
 * Created by Tommy Ettinger on 11/4/2017.
 */
public class ModelMaker {
    // separate from the rng so we can call skip(), if needed, or use GreasedRegion stuff
    public ThrustAltRNG thrust;
    public StatefulRNG rng;
    private byte[][][] ship;
    private final int xSize, ySize, zSize;
    public ModelMaker()
    {
        this((long) (Math.random() * Long.MAX_VALUE));
    }
    public ModelMaker(long seed)
    {
        thrust = new ThrustAltRNG(seed);
        rng = new StatefulRNG(thrust);
        InputStream is = this.getClass().getResourceAsStream("/ship.vox");
        ship = VoxReader.readVox(new LittleEndianDataInputStream(is));
        if(ship == null) ship = new byte[12][12][8];
        xSize = ship.length;
        ySize = ship[0].length;
        zSize = ship[0][0].length;

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
    public byte[][][] fishRandom()
    {
        byte[][][] voxels = new byte[12][12][8];
        final byte mainColor = (byte)((rng.nextIntHasty(22) << 3) + rng.between(10, 13)),
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
    public byte[][][] shipRandom()
    {
        long seed = rng.nextLong(), current;
        byte[][][] nextShip = new byte[xSize][ySize][zSize];
        final int halfY = ySize >> 1, smallYSize = ySize - 1;
        byte color;
        final byte mainColor = (byte)((ThrustAltRNG.determineBounded(seed + 1L, 22) << 3) + ThrustAltRNG.determineBounded(seed + 22L, 4) + 10),
                highlightColor = (byte)((ThrustAltRNG.determineBounded(seed + 333L, 22) << 3) + ThrustAltRNG.determineBounded(seed + 4444L, 3) + 11);

        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < halfY; y++) {
                for (int z = 0; z < zSize; z++) {
                    color = ship[x][y][z];
                    if (color != 0) {
                        // this 3-input-plus-state hash is really a slight modification on ThrustAltRNG.determine(), but
                        // it mixes the x, y, and z inputs more thoroughly than other techniques do, and we then use
                        // different sections of the random bits for different purposes, which helps reduce the possible
                        // issues from using rng.next(5) and rng.next(6) all over if the bits those use have a pattern.
                        current = WhirlingNoise.hashAll(x * 3 >> 2, y, z, seed);
                        if (color > 0 && color < 8 && (current & 0x3f) > 5) { // checks bottom 6 bits
                            nextShip[x][smallYSize - y][z] = nextShip[x][y][z] = 11;
                        } else {
                            nextShip[x][smallYSize - y][z] = nextShip[x][y][z] =
                                    // checks another 6 bits, starting after discarding 6 bits from the bottom
                                    ((current >>> 6 & 0x3F) < 47)
                                            ? 0
                                            // checks another 6 bits, starting after discarding 12 bits from the bottom
                                            : ((current >>> 12 & 0x3F) < 11) ? highlightColor : mainColor;
//                            if(rng.next(8) < 3) // occasional random asymmetry
//                            {
//                                if(rng.nextBoolean())
//                                    nextShip[x][y][z] = highlightColor;
//                                else
//                                    nextShip[x][smallYSize - y][z] = highlightColor;
//                            }
                        }
                    }
                }
            }
        }
        return nextShip;
        //return Tools3D.runCA(nextShip, 1);
    }
}
