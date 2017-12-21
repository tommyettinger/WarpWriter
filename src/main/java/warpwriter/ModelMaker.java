package warpwriter;

import squidpony.squidmath.NumberTools;
import squidpony.squidmath.StatefulRNG;
import squidpony.squidmath.ThrustAltRNG;

import java.io.InputStream;

import static squidpony.squidmath.ThrustAltRNG.determineBounded;
import static squidpony.squidmath.WhirlingNoise.hashAll;

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
        ship = VoxIO.readVox(new LittleEndianDataInputStream(is));
        if(ship == null) ship = new byte[12][12][8];
        xSize = ship.length;
        ySize = ship[0].length;
        zSize = ship[0][0].length;

    }
    public byte[][][] fullyRandom()
    {
        byte[][][] voxels = new byte[14][14][8];
        byte mainColor = (byte)((rng.nextIntHasty(22) << 3) + rng.between(10, 13)),
                highlightColor = (byte)((rng.nextIntHasty(22) << 3) + rng.between(11, 13));
        for (int x = 1; x < 13; x++) {
            for (int y = 1; y < 13; y++) {
                for (int z = 0; z < 8; z++) {
                    voxels[x][y][z] = (rng.next(4) < 7) ? 0 : (rng.next(5) == 0) ? highlightColor : mainColor;
                }
            }
        }
        return voxels;
    }
    public byte[][][] fishRandom()
    {
        byte[][][] voxels = new byte[14][14][8];
        int ctr;
        long current, state = rng.getState();
        do {
            final long seed = rng.nextLong();
            final byte mainColor = (byte)((determineBounded(seed + 1L, 22) << 3) + determineBounded(seed + 22L, 4) + 10),
                    highlightColor = (byte)((determineBounded(seed + 333L, 22) << 3) + determineBounded(seed + 4444L, 3) + 11);
            ctr = 0;
            for (int x = 0; x < 12; x++) {
                for (int y = 1; y < 6; y++) {
                    for (int z = 0; z < 8; z++) {
                        if (y > (Math.abs(x - 6) < 2 ? 4 - (seed >>> (63 - (seed & 1))) : 4)) {
                            current = hashAll(x >> 1, y >> 1, z >> 1, seed);
                            //current = WhirlingNoise.hashAll(x, y, z, seed);
                            if ((voxels[x+1][12 - y][z] = voxels[x+1][y+1][z] =
                                    // + (60 - (x + 1) * (12 - x) + 6 - y) * 47
                                    (determineBounded(current, //(11 - y * 2) * 23 +
                                            (Math.abs(x - 6) + 1) * Math.abs(z - 4) * 15 +
                                                    (5 - y) * 355 +
                                            ((Math.abs(x - 7) + 3) * (Math.abs(z - 4) + 2) * (7 - y)) * 23) < 520) ?
                                            ((current & 0x3F) < 11 ? highlightColor : mainColor)
                                            : 0) != 0) ctr++;
                        } else {
                            voxels[x+1][12 - y][z] = voxels[x+1][y+1][z] = 0;
                        }
                    }
                }
            }
        }while (ctr < 73);
        voxels = Tools3D.runCA(voxels, 2);
        for (int x = 11; x >= 8; x--) {
            for (int z = 6; z >= 3; z--) {
                for (int y = 2; y <= 5; y++) {
                    if(voxels[x][y - 1][z] != 0 || voxels[x][y][z + 1] != 0) break;
                    if (voxels[x][y][z] != 0 && (hashAll(x, y, z, state) & 0xFFL) < 185) {
                        voxels[x][13 - y][z] = voxels[x][y][z] = 8;
                        voxels[x][14 - y][z] = voxels[x][y - 1][z] = 8;
                        voxels[x + 1][14 - y][z] = voxels[x + 1][y][z] = 8;     // intentionally asymmetrical
                        voxels[x + 1][13 - y][z] = voxels[x + 1][y - 1][z] = 4; // intentionally asymmetrical
                        if(x < 10) {
                            voxels[x + 2][13 - y][z] = voxels[x + 2][y][z] = 0;
                            voxels[x + 2][14 - y][z] = voxels[x + 2][y - 1][z] = 0;
                        }
                        for (int i = z + 1; i < 8; i++) {
                            voxels[x][14 - y][i] = voxels[x][y][i] = 0;
                            voxels[x][14 - y][i] = voxels[x][y - 1][i] = 0;
                            voxels[x + 1][13 - y][i] = voxels[x + 1][y][i] = 0;
                            voxels[x + 1][14 - y][i] = voxels[x + 1][y - 1][i] = 0;
                            if(x < 10) {
                                voxels[x + 2][13 - y][i] = voxels[x + 2][y][i] = 0;
                                voxels[x + 2][14 - y][i] = voxels[x + 2][y - 1][i] = 0;
                            }
                        }
                        return voxels;
                    }
                }
            }
        }
        return voxels;
    }

    public byte[][][][] animateFish(byte[][][] fish, final int frameCount)
    {
        final int xSize = fish.length, ySize = fish[0].length, zSize = fish[0][0].length;
        byte[][][][] frames = new byte[frameCount][xSize][ySize][zSize];
        float changeAmount = 2f / (frameCount);
        int adjustment;
        for (int f = 0; f < frameCount; f++) {
            for (int x = 1; x < xSize - 1; x++) {
                adjustment = (int) (NumberTools.sway(changeAmount * (f + x * 0.6f) + 0.5f) * 1.99f);
                for (int y = 1; y < ySize - 1; y++) {
                    System.arraycopy(fish[x][y], 0, frames[f][x][y + adjustment], 0, zSize);
                }
            }
        }
        return frames;
    }

    public byte[][][] shipRandom()
    {
        long seed = rng.nextLong(), current;
        byte[][][] nextShip = new byte[xSize][ySize][zSize];
        final int halfY = ySize >> 1, smallYSize = ySize - 1;
        byte color;
        final byte mainColor = (byte)((determineBounded(seed + 1L, 22) << 3) + determineBounded(seed + 22L, 4) + 10),
                highlightColor = (byte)((determineBounded(seed + 333L, 22) << 3) + determineBounded(seed + 4444L, 3) + 11);

        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < halfY; y++) {
                for (int z = 0; z < zSize; z++) {
                    color = ship[x][y][z];
                    if (color != 0) {
                        // this 4-input-plus-state hash is really a slight modification on ThrustAltRNG.determine(), but
                        // it mixes the x, y, and z inputs more thoroughly than other techniques do, and we then use
                        // different sections of the random bits for different purposes. This helps reduce the possible
                        // issues from using rng.next(5) and rng.next(6) all over if the bits those use have a pattern.
                        // In the original model, all voxels of the same color will be hashed with similar behavior but
                        // any with different colors will get unrelated values.
                        current = hashAll(x * 3 >> 2, y, z, color, seed);
                        if (color > 0 && color < 8 && (current & 0x3f) > 3) { // checks bottom 6 bits
                            nextShip[x][smallYSize - y][z] = nextShip[x][y][z] = 11;
                        } else {
                            nextShip[x][smallYSize - y][z] = nextShip[x][y][z] =
                                    // checks another 6 bits, starting after discarding 6 bits from the bottom
                                    ((current >>> 6 & 0x3F) < 53)
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
