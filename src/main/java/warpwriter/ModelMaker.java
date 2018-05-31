package warpwriter;

import squidpony.squidmath.LightRNG;
import squidpony.squidmath.NumberTools;
import squidpony.squidmath.StatefulRNG;

import java.io.InputStream;

import static squidpony.squidmath.LightRNG.determineBounded;
import static squidpony.squidmath.Noise.PointHash.hashAll;

/**
 * Created by Tommy Ettinger on 11/4/2017.
 */
public class ModelMaker {
    // separate from the rng so we can call skip(), if needed, or use GreasedRegion stuff
    public LightRNG light;
    public StatefulRNG rng;
    private byte[][][] ship, shipLarge, warriorMale, sword0, spear0, shield0, shield1;
    private int xSize, ySize, zSize;

    public ModelMaker()
    {
        this((long) (Math.random() * Long.MAX_VALUE));
    }
    public ModelMaker(long seed)
    {
        light = new LightRNG(seed);
        rng = new StatefulRNG(light);
        InputStream is = this.getClass().getResourceAsStream("/ship.vox");
        ship = VoxIO.readVox(new LittleEndianDataInputStream(is));
        if(ship == null) ship = new byte[12][12][8];
        is = this.getClass().getResourceAsStream("/ship_40_40_30.vox");
        shipLarge = VoxIO.readVox(new LittleEndianDataInputStream(is));
        if(shipLarge == null) shipLarge = new byte[40][40][30];
        xSize = ship.length;
        ySize = ship[0].length;
        zSize = ship[0][0].length;
        is = this.getClass().getResourceAsStream("/Warrior_Male_Attach.vox");
        warriorMale = VoxIO.readVox(new LittleEndianDataInputStream(is));
        if(warriorMale == null) warriorMale = new byte[12][12][8];
        is = this.getClass().getResourceAsStream("/Sword_1H_Attach.vox");
        sword0 = VoxIO.readVox(new LittleEndianDataInputStream(is));
        if(sword0 == null) sword0 = new byte[12][12][8];
        is = this.getClass().getResourceAsStream("/Spear_1H_Attach.vox");
        spear0 = VoxIO.readVox(new LittleEndianDataInputStream(is));
        if(spear0 == null) spear0 = new byte[12][12][8];
        is = this.getClass().getResourceAsStream("/Board_Shield_1H_Attach.vox");
        shield0 = VoxIO.readVox(new LittleEndianDataInputStream(is));
        if(shield0 == null) shield0 = new byte[12][12][8];
        is = this.getClass().getResourceAsStream("/Round_Shield_1H_Attach.vox");
        shield1 = VoxIO.readVox(new LittleEndianDataInputStream(is));
        if(shield1 == null) shield1 = new byte[12][12][8];
    }
    
    public byte[][][] combine(byte[][][] start, byte[][][]... additional)
    {
        final int xSize = start.length, ySize = start[0].length, zSize = start[0][0].length;
        byte[][][] next = Tools3D.deepCopy(start);
        int[] startConn = new int[16], nextConn = new int[16];
        int[][] actualConn = new int[3][16];
        int nx = -1, ny = -1, nz = -1;
        Tools3D.findConnectors(start, startConn);
        for (int i = 0; i < 16; i++) {
            int c = startConn[i];
            if (c < 0)
                actualConn[0][i] = actualConn[1][i] = actualConn[2][i] = -1;
            else {
                actualConn[0][i] = c / (ySize * zSize);
                actualConn[1][i] = (c / zSize) % ySize;
                actualConn[2][i] = c % zSize;
                next[actualConn[0][i]][actualConn[1][i]][actualConn[2][i]] =  0;
            }
        }
        for(byte[][][] n : additional)
        {
            Tools3D.findConnectors(n, nextConn);
            for (int i = 0; i < 16; i++) {
                int c = nextConn[i];
                if (c >= 0)
                {
                    nx = c / (ySize * zSize);
                    ny = (c / zSize) % ySize;
                    nz = c % zSize;
                    Tools3D.translateCopyInto(n, next, actualConn[0][i] - nx, actualConn[1][i] - ny, actualConn[2][i] - nz);
                    next[actualConn[0][i]][actualConn[1][i]][actualConn[2][i]] = 0;
                    break;
                }
            }

        }
        return next;
    }
    
    public byte[][][] fullyRandom()
    {
        byte[][][] voxels = new byte[12][12][8];
        byte mainColor = (byte)((rng.nextIntHasty(18) * 6) + rng.between(22, 25)),
                highlightColor = (byte)((rng.nextIntHasty(18) * 6) + rng.between(21, 24));
        for (int x = 0; x < 12; x++) {
            for (int y = 0; y < 12; y++) {
                for (int z = 0; z < 8; z++) {
                    voxels[x][y][z] = (rng.next(5) == 0) ? highlightColor : mainColor; //(rng.next(4) < 7) ? 0 :
                }
            }
        }
        return voxels;
    }
    public byte[][][] fishRandom()
    {
        byte[][][] voxels = new byte[12][12][8];
        int ctr;
        long state = rng.getState(), current = determineBounded(state + 1L, 18);
        final byte mainColor = (byte)((current * 6) + determineBounded(state + 22L, 3) + 22),
                highlightColor = (byte)(((current + 4 + determineBounded(state + 333L, 10)) % 18) * 6 + determineBounded(state + 4444L, 3) + 21);
        do {
            final long seed = rng.nextLong();
            ctr = 0;
            for (int x = 0; x < 12; x++) {
                for (int y = 1; y < 6; y++) {
                    for (int z = 0; z < 8; z++) {
                        if (y > (Math.abs(x - 6) < 2 ? 4 - (seed >>> (63 - (seed & 1))) : 3)) {
                            //current = hashAll(x >> 1, y >> 1, z >> 1, seed);
                            current = hashAll(x, y, z, seed);
                            if ((voxels[x][11 - y][z] = voxels[x][y][z] =
                                    // + (60 - (x + 1) * (12 - x) + 6 - y) * 47
                                    (determineBounded(current, //(11 - y * 2) * 23 +
                                            (Math.abs(x - 6) + 1) * (1 + Math.abs(z - 4)) * 15 +
                                                    (5 - y) * 355 +
                                            ((Math.abs(x - 7) + 3) * (Math.abs(z - 4) + 2) * (7 - y)) * 21) < 555f) ?
                                            ((current & 0x3F) < 11 ? highlightColor : mainColor)
                                            : 0) != 0) ctr++;
                        } else {
                            voxels[x][11 - y][z] = voxels[x][y][z] = 0;
                        }
                    }
                }
            }
        }while (ctr < 45);
        voxels = Tools3D.largestPart(Tools3D.runCA(voxels, 1));
        for (int x = 10; x >= 5; x--) {
            for (int z = 7; z >= 2; z--) {
                for (int y = 1; y < 5; y++) {
                    if(y != 0 && voxels[x][y - 1][z] != 0) break;
                    if (voxels[x][y][z] != 0) {
                        voxels[x][12 - y][z] = voxels[x][y - 1][z] = 30;
                        voxels[x][11 - y][z] = voxels[x][y    ][z] = 30;
                        voxels[x + 1][12 - y][z] = voxels[x + 1][y    ][z] = 30;     // intentionally asymmetrical
                        voxels[x + 1][11 - y][z] = voxels[x + 1][y - 1][z] = 4; // intentionally asymmetrical
                        if(x <= 9) {
                            voxels[x + 2][12 - y][z] = voxels[x + 2][y - 1][z] = 0;
                            voxels[x + 2][11 - y][z] = voxels[x + 2][y    ][z] = 0;
                        }
                        for (int i = z + 1; i < 8; i++) {
                            voxels[x][12 - y][i] = voxels[x][y - 1][i] = 0;
                            voxels[x][11 - y][i] = voxels[x][y    ][i] = 0;
                            voxels[x + 1][12 - y][i] = voxels[x + 1][y - 1][i] = 0;
                            voxels[x + 1][11 - y][i] = voxels[x + 1][y    ][i] = 0;
                            if(x <= 9) {
                                voxels[x + 2][12 - y][i] = voxels[x + 2][y - 1][i] = 0;
                                voxels[x + 2][11 - y][i] = voxels[x + 2][y    ][i] = 0;
                            }
                        }
                        for (int z2 = z - 2; z2 > 0; z2--) {
                            for (int x2 = 10; x2 >= x - 1; x2--) {
                                if (voxels[x2][6][z2] != 0) {
                                    voxels[x2+1][6][z2] = voxels[x2+1][5][z2] = voxels[x2][6][z2] = voxels[x2][5][z2] = highlightColor;
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
            for (int x = 0; x < xSize; x++) {
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
        xSize = ship.length;
        ySize = ship[0].length;
        zSize = ship[0][0].length;
        byte[][][] nextShip = new byte[xSize][ySize][zSize];
        final int halfY = ySize >> 1, smallYSize = ySize - 1;
        byte color;
        final byte mainColor = (byte)((determineBounded(seed + 1L, 18) * 6) + determineBounded(seed + 22L, 3) + 22),
                highlightColor = (byte)((determineBounded(seed + 333L, 18) * 6) + determineBounded(seed + 4444L, 3) + 21),
                cockpitColor = (byte)(84 + (determineBounded(seed + 55555L, 6) * 6));
        int xx, yy;
        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < halfY; y++) {
                for (int z = 0; z < zSize; z++) {
                    color = ship[x][y][z];
                    if (color != 0) {
                        // this 4-input-plus-state hash is really a slight modification on LightRNG.determine(), but
                        // it mixes the x, y, and z inputs more thoroughly than other techniques do, and we then use
                        // different sections of the random bits for different purposes. This helps reduce the possible
                        // issues from using rng.next(5) and rng.next(6) all over if the bits those use have a pattern.
                        // In the original model, all voxels of the same color will be hashed with similar behavior but
                        // any with different colors will get unrelated values.
                        xx = x + 1;
                        yy = y + 1;
                        current = hashAll(xx + (xx | z) >> 2, yy + (yy | z) >> 1, z, color, seed);
                        if (color > 0 && color < 8 /* && (current & 0x3f) > 3 */ && z >= 2) { // checks bottom 6 bits
                            nextShip[x][smallYSize - y][z] = nextShip[x][y][z] = (byte) (cockpitColor - (z - 2 >> 1));//9;
                        } else {
                            nextShip[x][smallYSize - y][z] = nextShip[x][y][z] =
                                    // checks another 6 bits, starting after discarding 6 bits from the bottom
                                    ((current >>> 6 & 0x3FL) < 45L)
                                            ? 0
                                            // checks another 6 bits, starting after discarding 12 bits from the bottom
                                            : ((current >>> 12 & 0x3FL) < 40L) ? (byte)(18 + (current & 7))
                                            // checks another 6 bits, starting after discarding 18 bits from the bottom
                                            : ((current >>> 18 & 0x3FL) < 8L) ? highlightColor : mainColor;
                        }
                    }
                }
            }
        }
        return nextShip;
        //return Tools3D.runCA(nextShip, 1);
    }

    public byte[][][] shipLargeRandom()
    {
        long seed = rng.nextLong(), current, paint;
        xSize = shipLarge.length;
        ySize = shipLarge[0].length;
        zSize = shipLarge[0][0].length;
        byte[][][] nextShip = new byte[xSize][ySize][zSize];
        final int halfY = ySize >> 1, smallYSize = ySize - 1;
        int color;
        final byte mainColor = (byte)((determineBounded(seed + 1L, 18) * 6) + determineBounded(seed + 22L, 3) + 22),
                highlightColor = (byte)((determineBounded(seed + 333L, 18) * 6) + determineBounded(seed + 4444L, 3) + 21),
                cockpitColor = (byte)(84 + (determineBounded(seed + 55555L, 6) * 6));
        int xx, yy, zz;
        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < halfY; y++) {
                for (int z = 0; z < zSize; z++) {
                    color = (shipLarge[x][y][z] & 255);
                    if (color != 0) {
                        // this 4-input-plus-state hash is really a slight modification on LightRNG.determine(), but
                        // it mixes the x, y, and z inputs more thoroughly than other techniques do, and we then use
                        // different sections of the random bits for different purposes. This helps reduce the possible
                        // issues from using rng.next(5) and rng.next(6) all over if the bits those use have a pattern.
                        // In the original model, all voxels of the same color will be hashed with similar behavior but
                        // any with different colors will get unrelated values.
                        xx = x + 1;
                        yy = y + 1;
                        zz = z / 3;
                        current = hashAll(xx + (xx | zz) >> 3, (yy + (yy | zz)) / 3, zz, color, seed);
                        paint = hashAll((xx + (xx | z)) / 7, (yy + (yy | z)) / 5, z, color, seed);
                        if (color < 8) { // checks bottom 6 bits
                            if((current >>> 6 & 0x7L) != 0)
                            nextShip[x][smallYSize - y][z] = nextShip[x][y][z] = (byte) (cockpitColor - (z + 6 >> 3));//9;
                        } else {
                            nextShip[x][smallYSize - y][z] = nextShip[x][y][z] =
                                    // checks another 6 bits, starting after discarding 6 bits from the bottom
                                    ((current >>> 6 & 0x1FFL) < color * 6)
                                            ? 0
                                            // checks another 6 bits, starting after discarding 12 bits from the bottom
                                            : ((paint >>> 12 & 0x3FL) < 40L) ? (byte)(18 + (paint & 7))
                                            // checks another 6 bits, starting after discarding 18 bits from the bottom
                                            : ((paint >>> 18 & 0x3FL) < 8L) ? highlightColor : mainColor;
                        }
                    }
                }
            }
        }
        return Tools3D.largestPart(nextShip);
        //return nextShip;
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
            for (int x = 0; x < xSize; x++) {
                for (int y = 0; y < ySize; y++) {
                    System.arraycopy(spaceship[x][y], 1, frames[f][x][y], adjustment, zSize - 2);
                }
            }
        }
        return frames;
    }

}
