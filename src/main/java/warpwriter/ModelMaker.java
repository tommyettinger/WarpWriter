package warpwriter;

import squidpony.squidmath.NumberTools;
import squidpony.squidmath.StatefulRNG;
import squidpony.squidmath.ThrustAltRNG;

import java.io.InputStream;

import static squidpony.squidmath.Noise.PointHash.hash256;
import static squidpony.squidmath.Noise.PointHash.hashAll;
import static squidpony.squidmath.ThrustAltRNG.determineBounded;

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
        if(ship == null) ship = new byte[14][14][8];
        xSize = ship.length;
        ySize = ship[0].length;
        zSize = ship[0][0].length;

    }
    public byte[][][] fullyRandom()
    {
        byte[][][] voxels = new byte[14][14][8];
        byte mainColor = (byte)((rng.nextIntHasty(22) << 3) + rng.between(11, 14)),
                highlightColor = (byte)((rng.nextIntHasty(22) << 3) + rng.between(12, 14));
        for (int x = 1; x < 13; x++) {
            for (int y = 1; y < 13; y++) {
                for (int z = 0; z < 8; z++) {
                    voxels[x][y][z] = (rng.next(5) == 0) ? highlightColor : mainColor; //(rng.next(4) < 7) ? 0 :
                }
            }
        }
        return voxels;
    }
    public byte[][][] fishRandom()
    {
        byte[][][] voxels = new byte[14][14][8];
        int ctr;
        long state = rng.getState(), current = determineBounded(state + 1L, 22);
        final byte mainColor = (byte)((current << 3) + determineBounded(state + 22L, 4) + 11),
                highlightColor = (byte)(((current + 7 + determineBounded(state + 333L, 14)) % 22 << 3) + determineBounded(state + 4444L, 3) + 12);
        do {
            final long seed = rng.nextLong();
            ctr = 0;
            for (int x = 0; x < 12; x++) {
                for (int y = 1; y < 6; y++) {
                    for (int z = 0; z < 8; z++) {
                        if (y > (Math.abs(x - 6) < 2 ? 4 - (seed >>> (63 - (seed & 1))) : 4)) {
                            //current = hashAll(x >> 1, y >> 1, z >> 1, seed);
                            current = hashAll(x, y, z, seed);
                            if ((voxels[x+1][12 - y][z] = voxels[x+1][y+1][z] =
                                    // + (60 - (x + 1) * (12 - x) + 6 - y) * 47
                                    (determineBounded(current, //(11 - y * 2) * 23 +
                                            (Math.abs(x - 6) + 1) * (1 + Math.abs(z - 4)) * 15 +
                                                    (5 - y) * 355 +
                                            ((Math.abs(x - 7) + 3) * (Math.abs(z - 4) + 2) * (7 - y)) * 21) < 555f) ?
                                            ((current & 0x3F) < 11 ? highlightColor : mainColor)
                                            : 0) != 0) ctr++;
                        } else {
                            voxels[x+1][12 - y][z] = voxels[x+1][y+1][z] = 0;
                        }
                    }
                }
            }
        }while (ctr < 45);
        voxels = Tools3D.largestPart(Tools3D.runCA(voxels, 1));
        for (int x = 12; x >= 6; x--) {
            for (int z = 7; z >= 2; z--) {
                for (int y = 1; y < 6; y++) {
                    if(voxels[x][y - 1][z] != 0) break;
                    if (voxels[x][y][z] != 0 && hash256(x, y, z, state) < 255) {
                        voxels[x][13 - y][z] = voxels[x][y][z] = 9;
                        voxels[x][14 - y][z] = voxels[x][y - 1][z] = 9;
                        voxels[x + 1][14 - y][z] = voxels[x + 1][y][z] = 9;     // intentionally asymmetrical
                        voxels[x + 1][13 - y][z] = voxels[x + 1][y - 1][z] = 4; // intentionally asymmetrical
                        if(x <= 11) {
                            voxels[x + 2][13 - y][z] = voxels[x + 2][y][z] = 0;
                            voxels[x + 2][14 - y][z] = voxels[x + 2][y - 1][z] = 0;
                        }
                        for (int i = z + 1; i < 8; i++) {
                            voxels[x][14 - y][i] = voxels[x][y][i] = 0;
                            voxels[x][14 - y][i] = voxels[x][y - 1][i] = 0;
                            voxels[x + 1][13 - y][i] = voxels[x + 1][y][i] = 0;
                            voxels[x + 1][14 - y][i] = voxels[x + 1][y - 1][i] = 0;
                            if(x <= 11) {
                                voxels[x + 2][13 - y][i] = voxels[x + 2][y][i] = 0;
                                voxels[x + 2][14 - y][i] = voxels[x + 2][y - 1][i] = 0;
                            }
                        }
                        for (int z2 = z - 2; z2 > 0; z2--) {
                            for (int x2 = 12; x2 >= x - 1; x2--) {
                                if (voxels[x2][6][z2] != 0) {
                                    voxels[x2+1][6][z2] = voxels[x2+1][7][z2] = voxels[x2][6][z2] = voxels[x2][7][z2] = highlightColor;
                                    return voxels;
                                }
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
                adjustment = (int) (NumberTools.sin(changeAmount * (f + x * 0.6f) * Math.PI) * 1.5f);
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
        final byte mainColor = (byte)((determineBounded(seed + 1L, 22) << 3) + determineBounded(seed + 22L, 4) + 11),
                highlightColor = (byte)((determineBounded(seed + 333L, 22) << 3) + determineBounded(seed + 4444L, 3) + 12),
                cockpitColor = (byte)(-101 - (determineBounded(seed + 55555L, 7) << 3));
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
                        current = hashAll(x + (x | z) >> 2, y + (y | z) >> 1, z, color, seed); // x * 3 >> 2
                        if (color > 0 && color < 8 && (current & 0x3f) > 3) { // checks bottom 6 bits
                            nextShip[x][smallYSize - y][z] = nextShip[x][y][z] = (byte) (cockpitColor + (z - 4));//9;
                        } else {
                            nextShip[x][smallYSize - y][z] = nextShip[x][y][z] =
                                    // checks another 6 bits, starting after discarding 6 bits from the bottom
                                    ((current >>> 6 & 0x3FL) < 45L)
                                            ? 0
                                            // checks another 6 bits, starting after discarding 12 bits from the bottom
                                            : ((current >>> 12 & 0x3FL) < 40L) ? (byte)(10 + (current & 3))
                                            // checks another 6 bits, starting after discarding 18 bits from the bottom
                                            : ((current >>> 18 & 0x3FL) < 8L) ? highlightColor : mainColor;
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
    public byte[][][][] animateShip(byte[][][] spaceship, final int frameCount)
    {
        final int xSize = spaceship.length, ySize = spaceship[0].length, zSize = spaceship[0][0].length;
        byte[][][][] frames = new byte[frameCount][xSize][ySize][zSize];
        float changeAmount = 2f / (frameCount);
        int adjustment;
        for (int f = 0; f < frameCount; f++) {
            adjustment = (int) (NumberTools.sway(changeAmount * f + 0.5f) * 1.75f) + 1;
            for (int x = 1; x < xSize - 1; x++) {
                for (int y = 1; y < ySize - 1; y++) {
                    System.arraycopy(spaceship[x][y], 1, frames[f][x][y], adjustment, 6);
                }
            }
        }
        return frames;
    }

}
