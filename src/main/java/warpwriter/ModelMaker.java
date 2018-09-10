package warpwriter;

import squidpony.squidmath.LinnormRNG;
import squidpony.squidmath.NumberTools;
import squidpony.squidmath.StatefulRNG;

import java.io.InputStream;

import static squidpony.squidmath.LinnormRNG.determineBounded;
import static squidpony.squidmath.LinnormRNG.determineFloat;
import static squidpony.squidmath.MathExtras.clamp;
import static squidpony.squidmath.Noise.PointHash.hashAll;

/**
 * Created by Tommy Ettinger on 11/4/2017.
 */
public class ModelMaker {
    public LinnormRNG source;
    public StatefulRNG rng;
    private byte[][][] ship, shipLarge, warriorMale, sword0, spear0, shield0, shield1;
    private byte[][][][] rightHand, leftHand;
    private int xSize, ySize, zSize;
//    /**
//     *
//     * @param x
//     * @param y
//     * @param z
//     * @param state
//     * @return 64-bit hash of the x,y,z point with the given state
//     */
//    public static long hashAll(long x, long y, long z, long state) {
////        return TangleRNG.determine(x, TangleRNG.determine(y, TangleRNG.determine(z, state)));
//        state *= 0x9E3779B97F4A7C15L;
//        long other = 0x60642E2A34326F15L;
//        state ^= (other += (x ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
//        state = (state << 54 | state >>> 10);
//        state ^= (other += (y ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
//        state = (state << 54 | state >>> 10);
//        state ^= (other += (z ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
//        return ((state << 54 | state >>> 10) + (other ^ other >>> 29) ^ 0x9E3779B97F4A7C15L) * 0x94D049BB133111EBL;
//
////        return ((x = ((x *= 0x6C8E9CF570932BD5L) ^ x >>> 26 ^ 0x9183A1F4F348E683L) * (
////                ((y = ((y *= 0x6C8E9CF570932BD5L) ^ y >>> 26 ^ 0x9183A1F4F348E683L) * (
////                        ((z = ((z *= 0x6C8E9CF570932BD5L) ^ z >>> 26 ^ 0x9183A1F4F348E683L) * (
////                                state * 0x9E3779B97F4A7C15L
////                                        | 1L)) ^ z >>> 24)
////                                | 1L)) ^ y >>> 24) 
////                        | 1L)) ^ x >>> 24);
//
////        x *= (0xF34C283B73FE6A6DL);
////        state += (x << 45 | x >>> 19);
////        y *= (0x9183A1F4F348E683L);
////        state += (y << 3 | y >>> 61);
////        z *= (0xAFBB1BAE72936299L);
////        state += (z << 25 | z >>> 39);
////        return state ^ x + y + z;
//    }
//    /**
//     *
//     * @param x
//     * @param y
//     * @param z
//     * @param state
//     * @return 64-bit hash of the x,y,z,w point with the given state
//     */
//    public static long hashAll(long x, long y, long z, long w, long state) {
////        return TangleRNG.determine(x, TangleRNG.determine(y, TangleRNG.determine(z, TangleRNG.determine(w, state))));
//        state *= 0x9E3779B97F4A7C15L;
//        long other = 0x60642E2A34326F15L;
//        state ^= (other += (x ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
//        state = (state << 54 | state >>> 10);
//        state ^= (other += (y ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
//        state = (state << 54 | state >>> 10);
//        state ^= (other += (z ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
//        state = (state << 54 | state >>> 10);
//        state ^= (other += (w ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
//        return ((state << 54 | state >>> 10) + (other ^ other >>> 29) ^ 0x9E3779B97F4A7C15L) * 0x94D049BB133111EBL;
//
////        return ((x = ((x *= 0x6C8E9CF570932BD5L) ^ x >>> 26 ^ 0x9183A1F4F348E683L) * (
////                ((y = ((y *= 0x6C8E9CF570932BD5L) ^ y >>> 26 ^ 0x9183A1F4F348E683L) * (
////                        ((z = ((z *= 0x6C8E9CF570932BD5L) ^ z >>> 26 ^ 0x9183A1F4F348E683L) * (
////                                ((w = ((w *= 0x6C8E9CF570932BD5L) ^ w >>> 26 ^ 0x9183A1F4F348E683L) * (
////                                        state * 0x9E3779B97F4A7C15L
////                                                | 1L)) ^ w >>> 24)
////                                        | 1L)) ^ z >>> 24)
////                                | 1L)) ^ y >>> 24)
////                        | 1L)) ^ x >>> 24);
//
////        return ((x = ((x *= 0x734C283B73FE6A6DL) ^ x >>> 26 ^ 0x64F31432B4AA049BL) * (
////                ((y = ((y *= 0x5FCBBDE92C96E11DL) ^ y >>> 26 ^ 0x4E34944613628E73L) * (
////                        ((z = ((z *= 0x6C8E9CF570932BD5L) ^ z >>> 26 ^ 0x7F91620098C41B2BL) * (
////                                ((w = ((w *= 0x4DC7BD448464FE2DL) ^ w >>> 26 ^ 0x571DD04A962AC4A3L) * (
////                                        state * 0x9E3779B97F4A7C15L
////                                                | 1L)) ^ w >>> 24)
////                                        | 1L)) ^ z >>> 24)
////                                | 1L)) ^ y >>> 24)
////                        | 1L)) ^ x >>> 24);
//
//        // 0x9E3779B97F4A7C16L
//        // (0x734C283B73FE6A6DL + 0x9E3779B97F4A7C16L * 1L)
////        x *= (0xF34C283B73FE6A6DL);
////        state += (x << 45 | x >>> 19);
////        y *= (0x9183A1F4F348E683L);
////        state += (y << 3 | y >>> 61);
////        z *= (0xAFBB1BAE72936299L);
////        state += (z << 25 | z >>> 39);
////        w *= (0xCDF29567F1DDDEAFL);
////        state += (w << 47 | w >>> 17);
////        return state ^ x + y + z + w;
//        
//    }
    public ModelMaker()
    {
        this((long)((Math.random() - 0.5) * 4.503599627370496E15) ^ (long)((Math.random() - 0.5) * 2.0 * -9.223372036854776E18));
    }
    public ModelMaker(long seed)
    {
        source = new LinnormRNG(seed);
        rng = new StatefulRNG(source);
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
        
        rightHand = new byte[][][][]{sword0, spear0};
        leftHand = new byte[][][][]{shield0, shield1};
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
                if (c >= 0 && actualConn[0][i] != -1)
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
    
    public byte[][][] fullyRandom(boolean large)
    {
        final int side = large ? shipLarge.length : ship.length,
                high = large ? shipLarge[0][0].length : ship[0][0].length;
        byte[][][] voxels = new byte[side][side][high];
        byte mainColor = (byte)((rng.nextIntHasty(18) * 6) + rng.between(22, 25)),
                highlightColor = (byte)((rng.nextIntHasty(18) * 6) + rng.between(21, 24));
        for (int x = 0; x < side; x++) {
            for (int y = 0; y < side; y++) {
                for (int z = 0; z < high; z++) {
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
                adjustment = (int) (NumberTools.sin(changeAmount * (f + x * 0.6f) * 3.141592653589793f) * 1.5f);
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
    
    public byte[][][] warriorRandom()
    {
        byte[][][][] used = new byte[rng.maxIntOf(2, 4) + 1][][][];
        used[0] = rng.getRandomElement(rightHand);
        if(used.length > 1)
            used[1] = rng.getRandomElement(leftHand);
        return combine(warriorMale, used);
    }

//    private static final long[] masks = {
//            0x0000_0000_0000_0000L,
//            0x2148_1824_4281_8412L,
//            0x2148_1824_4281_8412L | 0x8412_2148_1824_4281L,
//            0x2148_1824_4281_8412L | 0x8412_2148_1824_4281L | 0x4281_8412_2148_1824L,
//    };

    private static final long[] masks = {
            0x0000_0000_0000_0000L,
            0x0148_0804_0281_0402L,
            0x2148_1824_4281_8412L | 0x8412_2148_1824_4281L,
            0x2148_1824_4281_8412L | 0x8412_2148_1824_4281L | 0x4281_8412_2148_1824L | 0x1020_4000_8010_2000L,
    };

    /**
     * Hamming-weight interpolation, like lerp but doesn't consistently change numerical closeness, instead changing
     * similarity of bit patterns.
     * @param start bit pattern to use when distance is 0
     * @param end bit pattern to use when distance is 4 (should never happen, but it approaches this)
     * @param distance 0, 1, 2, or 3; how far this is between start and end, with 3 at 3/4 between start and end
     * @return a long with a bit pattern between start and end
     */
    private static long herp(long start, long end, int distance)
    {
        return start ^ ((start ^ end) & masks[distance]);
    }
    public byte[][][] blobLargeRandom()
    {
        long seed = rng.nextLong(), current;
        xSize = 40;
        ySize = 40;
        zSize = 30;
        byte[][][] blob = new byte[xSize][ySize][zSize];
        final int halfY = ySize + 1 >> 1, // rounds up if odd
                smallYSize = ySize - 1;
        long[][][] hashes = new long[xSize][ySize][zSize];
        for (int x = 2; x < xSize; x+=4) {
            for (int y = 2; y < halfY + 4; y+=4) {
                for (int z = 0; z < zSize; z+=4) {
                    hashes[x][smallYSize - y][z] |= hashes[x][y][z] |= hashAll(x, y, z, seed) | 1L;
                }
            }
        }
        long x0y0z0, x1y0z0, x0y1z0, x1y1z0, x0y0z1, x1y0z1, x0y1z1, x1y1z1;
        int x0, x1, y0, y1, z0, z1, dx, dy, dz;
        for (int x = 2; x < xSize; x++) {
            x0 = (x - 2 & -4) + 2;
            x1 = x0 + 4 >= xSize ? 2 : x0 + 4;
            dx = x - 2 & 3;
            for (int y = 2; y < halfY; y++) {
                y0 = (y - 2 & -4) + 2;
                y1 = y0 + 4;
                dy = y - 2 & 3;
                for (int z = 0; z < zSize; z++) {
                    z0 = z & -4;
                    z1 = z0 + 4 >= zSize ? 0 : z0 + 4;
                    dz = z & 3;
                    if(hashes[x][y][z] == 0)
                    {
                        x0y0z0 = hashes[x0][y0][z0];
                        x1y0z0 = hashes[x1][y0][z0];
                        x0y1z0 = hashes[x0][y1][z0];
                        x1y1z0 = hashes[x1][y1][z0];
                        x0y0z1 = hashes[x0][y0][z1];
                        x1y0z1 = hashes[x1][y0][z1];
                        x0y1z1 = hashes[x0][y1][z1];
                        x1y1z1 = hashes[x1][y1][z1];                         
                        current =
                                herp(
                                        herp(
                                                herp(x0y0z0, x1y0z0, dx),
                                                herp(x0y1z0, x1y1z0, dx), dy),
                                        herp(
                                                herp(x0y0z1, x1y0z1, dx),
                                                herp(x0y1z1, x1y1z1, dx), dy), dz)
                        ;
                        hashes[x][y][z] = current |
                                (current & ~(-1L << ((y >> 1) + ((zSize - z) * 11 >> 4) + ((xSize >> 1) - Math.abs(x - (xSize >> 1)) >> 2)))) << 1;
                    }
                }
            }
        }
        for (int i = 0; i < 30; i++) {
            int rx = (int)((determineFloat(seed + 100L + i) + determineFloat(seed - 100L + i)) * xSize * 0.5f),
                    rz = (int)(determineFloat(seed + 1000L + i) * determineFloat(seed - 1000L + i) * zSize);
            hashes[rx][halfY-1][rz] |= 0xFFFFFFFFFFFFL | hashes[rx][halfY-1][rz] << 16;
            hashes[rx][halfY-2][rz] |= 0xFFFFFFFFFFFFL | hashes[rx][halfY-2][rz] << 16;
            hashes[rx][halfY-3][rz] |= 0xFFFFFFFFFFFFL | hashes[rx][halfY-3][rz] << 16;
            hashes[rx][halfY-4][rz] |= 0xFFFFFFFFFFFFL | hashes[rx][halfY-4][rz] << 16;
        }
        for (int i = 0; i < 20; i++) {
            int rx = (int)((determineFloat(seed + 100L + i) + determineFloat(seed - 100L + i)) * xSize * 0.5f),
                    rz = (int)(determineFloat(seed + 1000L + i) * determineFloat(seed - 1000L + i) * zSize);
            if(rx < xSize - 4) {
                hashes[rx + 1][halfY - 1][rz] |= 0xFFFFFFFFFFFFL | hashes[rx + 1][halfY - 1][rz] << 16;
                hashes[rx + 1][halfY - 2][rz] |= 0xFFFFFFFFFFFFL | hashes[rx + 1][halfY - 2][rz] << 16;
                hashes[rx + 1][halfY - 3][rz] |= 0xFFFFFFFFFFFFL | hashes[rx + 1][halfY - 3][rz] << 16;
                hashes[rx + 1][halfY - 4][rz] |= 0xFFFFFFFFFFFFL | hashes[rx + 1][halfY - 4][rz] << 16;

                hashes[rx + 2][halfY - 1][rz] |= 0xFFFFFFFFFFFFL | hashes[rx + 2][halfY - 1][rz] << 16;
                hashes[rx + 2][halfY - 2][rz] |= 0xFFFFFFFFFFFFL | hashes[rx + 2][halfY - 2][rz] << 16;
                hashes[rx + 2][halfY - 3][rz] |= 0xFFFFFFFFFFFFL | hashes[rx + 2][halfY - 3][rz] << 16;
                hashes[rx + 2][halfY - 4][rz] |= 0xFFFFFFFFFFFFL | hashes[rx + 2][halfY - 4][rz] << 16;

                hashes[rx + 3][halfY - 1][rz] |= 0xFFFFFFFFFFFFL | hashes[rx + 3][halfY - 1][rz] << 16;
                hashes[rx + 3][halfY - 2][rz] |= 0xFFFFFFFFFFFFL | hashes[rx + 3][halfY - 2][rz] << 16;
                hashes[rx + 3][halfY - 3][rz] |= 0xFFFFFFFFFFFFL | hashes[rx + 3][halfY - 3][rz] << 16;
                hashes[rx + 3][halfY - 4][rz] |= 0xFFFFFFFFFFFFL | hashes[rx + 3][halfY - 4][rz] << 16;

                hashes[rx + 4][halfY - 1][rz] |= 0xFFFFFFFFFFFFL | hashes[rx + 4][halfY - 1][rz] << 16;
                hashes[rx + 4][halfY - 2][rz] |= 0xFFFFFFFFFFFFL | hashes[rx + 4][halfY - 2][rz] << 16;
                hashes[rx + 4][halfY - 3][rz] |= 0xFFFFFFFFFFFFL | hashes[rx + 4][halfY - 3][rz] << 16;
                hashes[rx + 4][halfY - 4][rz] |= 0xFFFFFFFFFFFFL | hashes[rx + 4][halfY - 4][rz] << 16;
            }
            if(rx > 3) {
                hashes[rx - 1][halfY - 1][rz] |= 0xFFFFFFFFFFFFL | hashes[rx - 1][halfY - 1][rz] << 16;
                hashes[rx - 1][halfY - 2][rz] |= 0xFFFFFFFFFFFFL | hashes[rx - 1][halfY - 2][rz] << 16;
                hashes[rx - 1][halfY - 3][rz] |= 0xFFFFFFFFFFFFL | hashes[rx - 1][halfY - 3][rz] << 16;
                hashes[rx - 1][halfY - 4][rz] |= 0xFFFFFFFFFFFFL | hashes[rx - 1][halfY - 4][rz] << 16;

                hashes[rx - 2][halfY - 1][rz] |= 0xFFFFFFFFFFFFL | hashes[rx - 2][halfY - 1][rz] << 16;
                hashes[rx - 2][halfY - 2][rz] |= 0xFFFFFFFFFFFFL | hashes[rx - 2][halfY - 2][rz] << 16;
                hashes[rx - 2][halfY - 3][rz] |= 0xFFFFFFFFFFFFL | hashes[rx - 2][halfY - 3][rz] << 16;
                hashes[rx - 2][halfY - 4][rz] |= 0xFFFFFFFFFFFFL | hashes[rx - 2][halfY - 4][rz] << 16;

                hashes[rx - 3][halfY - 1][rz] |= 0xFFFFFFFFFFFFL | hashes[rx - 3][halfY - 1][rz] << 16;
                hashes[rx - 3][halfY - 2][rz] |= 0xFFFFFFFFFFFFL | hashes[rx - 3][halfY - 2][rz] << 16;
                hashes[rx - 3][halfY - 3][rz] |= 0xFFFFFFFFFFFFL | hashes[rx - 3][halfY - 3][rz] << 16;
                hashes[rx - 3][halfY - 4][rz] |= 0xFFFFFFFFFFFFL | hashes[rx - 3][halfY - 4][rz] << 16;

                hashes[rx - 4][halfY - 1][rz] |= 0xFFFFFFFFFFFFL | hashes[rx - 4][halfY - 1][rz] << 16;
                hashes[rx - 4][halfY - 2][rz] |= 0xFFFFFFFFFFFFL | hashes[rx - 4][halfY - 2][rz] << 16;
                hashes[rx - 4][halfY - 3][rz] |= 0xFFFFFFFFFFFFL | hashes[rx - 4][halfY - 3][rz] << 16;
                hashes[rx - 4][halfY - 4][rz] |= 0xFFFFFFFFFFFFL | hashes[rx - 4][halfY - 4][rz] << 16;
            }
            if(rz < zSize - 1) {
                hashes[rx][halfY - 1][rz + 1] |= 0xFFFFFFFFFFFFL | hashes[rx][halfY - 1][rz + 1] << 16;
                hashes[rx][halfY - 2][rz + 1] |= 0xFFFFFFFFFFFFL | hashes[rx][halfY - 2][rz + 1] << 16;
                hashes[rx][halfY - 3][rz + 1] |= 0xFFFFFFFFFFFFL | hashes[rx][halfY - 3][rz + 1] << 16;
                hashes[rx][halfY - 4][rz + 1] |= 0xFFFFFFFFFFFFL | hashes[rx][halfY - 4][rz + 1] << 16;
            }
            hashes[rx][halfY-5][rz] |= 0xFFFFFFFFFFFFL | hashes[rx][halfY-5][rz] << 16;
            hashes[rx][halfY-6][rz] |= 0xFFFFFFFFFFFFL | hashes[rx][halfY-6][rz] << 16;
            hashes[rx][halfY-7][rz] |= 0xFFFFFFFFFFFFL | hashes[rx][halfY-7][rz] << 16;
            hashes[rx][halfY-8][rz] |= 0xFFFFFFFFFFFFL | hashes[rx][halfY-8][rz] << 16;
        }
        final byte mainColor = (byte)((determineBounded(seed + 1L, 18) * 6) + determineBounded(seed + 22L, 3) + 22),
                highlightColor = (byte)((determineBounded(seed + 333L, 18) * 6) + determineBounded(seed + 4444L, 3) + 21);
        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < halfY; y++) {
                for (int z = 0; z < zSize; z++) {
                    current = hashes[x][y][z];
                    if (Long.bitCount(current) > 42) {
                            blob[x][smallYSize - y][z] = blob[x][y][z] =
                                            ((current >>> 32 & 0x3FL) < 11L) ? highlightColor : mainColor;
                    }
                }
            }
        }
        return Tools3D.largestPart(blob);
        //return blob;
        //return Tools3D.runCA(nextShip, 1);
    }
    public byte[][][][] animateBlobLargeRandom(int frames)
    {
        long seed = rng.nextLong(), current;
        xSize = 40;
        ySize = 40;
        zSize = 30;
        final float changeAmount = 2f / (frames);
        byte[][][][] blob = new byte[frames][xSize][ySize][zSize];
        byte[][][] blob0 = new byte[xSize][ySize][zSize];
        final int halfY = ySize + 1 >> 1, // rounds up if odd
                smallXSize = xSize - 1, smallYSize = ySize - 1, smallZSize = zSize - 1;
        long[][][] hashes = new long[xSize][ySize][zSize];
        for (int x = 2; x < xSize; x+=4) {
            for (int y = 2; y < halfY + 4; y+=4) {
                for (int z = 0; z < zSize; z+=4) {
                    hashes[x][smallYSize - y][z] |= hashes[x][y][z] |= hashAll(x, y, z, seed) | 1L;
                }
            }
        }
        long x0y0z0, x1y0z0, x0y1z0, x1y1z0, x0y0z1, x1y0z1, x0y1z1, x1y1z1;
        int x0, x1, y0, y1, z0, z1, dx, dy, dz, adj1, adj2, adj3, adj1_, adj2_, adj3_;
        for (int x = 2; x < xSize; x++) {
            x0 = (x - 2 & -4) + 2;
            x1 = x0 + 4 >= xSize ? 2 : x0 + 4;
            dx = x - 2 & 3;
            for (int y = 2; y < halfY; y++) {
                y0 = (y - 2 & -4) + 2;
                y1 = y0 + 4;
                dy = y - 2 & 3;
                for (int z = 0; z < zSize; z++) {
                    z0 = z & -4;
                    z1 = z0 + 4 >= zSize ? 0 : z0 + 4;
                    dz = z & 3;
                    if(hashes[x][y][z] == 0)
                    {
                        x0y0z0 = hashes[x0][y0][z0];
                        x1y0z0 = hashes[x1][y0][z0];
                        x0y1z0 = hashes[x0][y1][z0];
                        x1y1z0 = hashes[x1][y1][z0];
                        x0y0z1 = hashes[x0][y0][z1];
                        x1y0z1 = hashes[x1][y0][z1];
                        x0y1z1 = hashes[x0][y1][z1];
                        x1y1z1 = hashes[x1][y1][z1];
                        current =
                                herp(
                                        herp(
                                                herp(x0y0z0, x1y0z0, dx),
                                                herp(x0y1z0, x1y1z0, dx), dy),
                                        herp(
                                                herp(x0y0z1, x1y0z1, dx),
                                                herp(x0y1z1, x1y1z1, dx), dy), dz)
                        ;
                        hashes[x][y][z] = current |
                                (current & ~(-1L << ((y >> 1) + ((zSize - z) * 11 >> 4) + ((xSize >> 1) - Math.abs(x - (xSize >> 1)) >> 2)))) << 1;
                    }
                }
            }
        }
        for (int i = 0; i < 30; i++) {
            int rx = (int)((determineFloat(seed + 100L + i) + determineFloat(seed - 100L + i)) * xSize * 0.5f),
                    rz = (int)(determineFloat(seed + 1000L + i) * determineFloat(seed - 1000L + i) * zSize);
            hashes[rx][halfY-1][rz] |= 0xFFFFFFFFFFFFL | hashes[rx][halfY-1][rz] << 16;
            hashes[rx][halfY-2][rz] |= 0xFFFFFFFFFFFFL | hashes[rx][halfY-2][rz] << 16;
            hashes[rx][halfY-3][rz] |= 0xFFFFFFFFFFFFL | hashes[rx][halfY-3][rz] << 16;
            hashes[rx][halfY-4][rz] |= 0xFFFFFFFFFFFFL | hashes[rx][halfY-4][rz] << 16;
        }
        for (int i = 0; i < 20; i++) {
            int rx = (int)((determineFloat(seed + 100L + i) + determineFloat(seed - 100L + i)) * xSize * 0.5f),
                    rz = (int)(determineFloat(seed + 1000L + i) * determineFloat(seed - 1000L + i) * zSize);
            if(rx < xSize - 4) {
                hashes[rx + 1][halfY - 1][rz] |= 0xFFFFFFFFFFFFL | hashes[rx + 1][halfY - 1][rz] << 16;
                hashes[rx + 1][halfY - 2][rz] |= 0xFFFFFFFFFFFFL | hashes[rx + 1][halfY - 2][rz] << 16;
                hashes[rx + 1][halfY - 3][rz] |= 0xFFFFFFFFFFFFL | hashes[rx + 1][halfY - 3][rz] << 16;
                hashes[rx + 1][halfY - 4][rz] |= 0xFFFFFFFFFFFFL | hashes[rx + 1][halfY - 4][rz] << 16;

                hashes[rx + 2][halfY - 1][rz] |= 0xFFFFFFFFFFFFL | hashes[rx + 2][halfY - 1][rz] << 16;
                hashes[rx + 2][halfY - 2][rz] |= 0xFFFFFFFFFFFFL | hashes[rx + 2][halfY - 2][rz] << 16;
                hashes[rx + 2][halfY - 3][rz] |= 0xFFFFFFFFFFFFL | hashes[rx + 2][halfY - 3][rz] << 16;
                hashes[rx + 2][halfY - 4][rz] |= 0xFFFFFFFFFFFFL | hashes[rx + 2][halfY - 4][rz] << 16;

                hashes[rx + 3][halfY - 1][rz] |= 0xFFFFFFFFFFFFL | hashes[rx + 3][halfY - 1][rz] << 16;
                hashes[rx + 3][halfY - 2][rz] |= 0xFFFFFFFFFFFFL | hashes[rx + 3][halfY - 2][rz] << 16;
                hashes[rx + 3][halfY - 3][rz] |= 0xFFFFFFFFFFFFL | hashes[rx + 3][halfY - 3][rz] << 16;
                hashes[rx + 3][halfY - 4][rz] |= 0xFFFFFFFFFFFFL | hashes[rx + 3][halfY - 4][rz] << 16;

                hashes[rx + 4][halfY - 1][rz] |= 0xFFFFFFFFFFFFL | hashes[rx + 4][halfY - 1][rz] << 16;
                hashes[rx + 4][halfY - 2][rz] |= 0xFFFFFFFFFFFFL | hashes[rx + 4][halfY - 2][rz] << 16;
                hashes[rx + 4][halfY - 3][rz] |= 0xFFFFFFFFFFFFL | hashes[rx + 4][halfY - 3][rz] << 16;
                hashes[rx + 4][halfY - 4][rz] |= 0xFFFFFFFFFFFFL | hashes[rx + 4][halfY - 4][rz] << 16;
            }
            if(rx > 3) {
                hashes[rx - 1][halfY - 1][rz] |= 0xFFFFFFFFFFFFL | hashes[rx - 1][halfY - 1][rz] << 16;
                hashes[rx - 1][halfY - 2][rz] |= 0xFFFFFFFFFFFFL | hashes[rx - 1][halfY - 2][rz] << 16;
                hashes[rx - 1][halfY - 3][rz] |= 0xFFFFFFFFFFFFL | hashes[rx - 1][halfY - 3][rz] << 16;
                hashes[rx - 1][halfY - 4][rz] |= 0xFFFFFFFFFFFFL | hashes[rx - 1][halfY - 4][rz] << 16;

                hashes[rx - 2][halfY - 1][rz] |= 0xFFFFFFFFFFFFL | hashes[rx - 2][halfY - 1][rz] << 16;
                hashes[rx - 2][halfY - 2][rz] |= 0xFFFFFFFFFFFFL | hashes[rx - 2][halfY - 2][rz] << 16;
                hashes[rx - 2][halfY - 3][rz] |= 0xFFFFFFFFFFFFL | hashes[rx - 2][halfY - 3][rz] << 16;
                hashes[rx - 2][halfY - 4][rz] |= 0xFFFFFFFFFFFFL | hashes[rx - 2][halfY - 4][rz] << 16;

                hashes[rx - 3][halfY - 1][rz] |= 0xFFFFFFFFFFFFL | hashes[rx - 3][halfY - 1][rz] << 16;
                hashes[rx - 3][halfY - 2][rz] |= 0xFFFFFFFFFFFFL | hashes[rx - 3][halfY - 2][rz] << 16;
                hashes[rx - 3][halfY - 3][rz] |= 0xFFFFFFFFFFFFL | hashes[rx - 3][halfY - 3][rz] << 16;
                hashes[rx - 3][halfY - 4][rz] |= 0xFFFFFFFFFFFFL | hashes[rx - 3][halfY - 4][rz] << 16;

                hashes[rx - 4][halfY - 1][rz] |= 0xFFFFFFFFFFFFL | hashes[rx - 4][halfY - 1][rz] << 16;
                hashes[rx - 4][halfY - 2][rz] |= 0xFFFFFFFFFFFFL | hashes[rx - 4][halfY - 2][rz] << 16;
                hashes[rx - 4][halfY - 3][rz] |= 0xFFFFFFFFFFFFL | hashes[rx - 4][halfY - 3][rz] << 16;
                hashes[rx - 4][halfY - 4][rz] |= 0xFFFFFFFFFFFFL | hashes[rx - 4][halfY - 4][rz] << 16;
            }
            if(rz < zSize - 1) {
                hashes[rx][halfY - 1][rz + 1] |= 0xFFFFFFFFFFFFL | hashes[rx][halfY - 1][rz + 1] << 16;
                hashes[rx][halfY - 2][rz + 1] |= 0xFFFFFFFFFFFFL | hashes[rx][halfY - 2][rz + 1] << 16;
                hashes[rx][halfY - 3][rz + 1] |= 0xFFFFFFFFFFFFL | hashes[rx][halfY - 3][rz + 1] << 16;
                hashes[rx][halfY - 4][rz + 1] |= 0xFFFFFFFFFFFFL | hashes[rx][halfY - 4][rz + 1] << 16;
            }
            hashes[rx][halfY-5][rz] |= 0xFFFFFFFFFFFFL | hashes[rx][halfY-5][rz] << 16;
            hashes[rx][halfY-6][rz] |= 0xFFFFFFFFFFFFL | hashes[rx][halfY-6][rz] << 16;
            hashes[rx][halfY-7][rz] |= 0xFFFFFFFFFFFFL | hashes[rx][halfY-7][rz] << 16;
            hashes[rx][halfY-8][rz] |= 0xFFFFFFFFFFFFL | hashes[rx][halfY-8][rz] << 16;
        }
        final byte mainColor = (byte)((determineBounded(seed + 1L, 18) * 6) + determineBounded(seed + 22L, 3) + 22),
                highlightColor = (byte)((determineBounded(seed + 333L, 18) * 6) + determineBounded(seed + 4444L, 3) + 21);
        float edit = 0f;
        byte color;
        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < halfY; y++) {
                for (int z = 0; z < zSize; z++) {
                    current = hashes[x][y][z];
                    if (Long.bitCount(current) > 42) {
                        blob0[x][smallYSize - y][z] = blob0[x][y][z] =
                                ((current >>> 32 & 0x3FL) < 11L) ? highlightColor : mainColor;
                    }
                }
            }
        }
        blob0 = Tools3D.largestPart(blob0);
        for (int w = frames - 1; w >= 0; w--) {
            for (int x = 0; x < xSize; x++) {
                for (int y = 0; y < halfY; y++) {
                    for (int z = 0; z < zSize; z++) {
                        if ((color = blob0[x][y][z]) != 0) {
                            edit = changeAmount * (w + (Math.abs(x - (xSize >> 1)) + halfY - y + (z >> 1)) * 0.1f)
                                    * 3.141592653589793f;
                            adj1 = (int) (NumberTools.sin(edit) * 1.625f);
                            adj1_ = (int) (adj1 * 0.5f);
                            adj2 = (int) (NumberTools.sin(edit+1f) * 1.625f);
                            adj2_ = (int) (adj2 * 0.5f);
                            adj3 = (int) (NumberTools.sin(edit+2f) * 1.625f);
                            adj3_ = (int) (adj3 * 0.5f);
                            blob[w][clamp(x+adj1, 0,smallXSize)][clamp(smallYSize - y-adj2, 0, smallYSize)][clamp(z+adj3,0,smallZSize)]
                                    = blob[w][clamp(x+adj1, 0,smallXSize)][clamp(y+adj2, 0, smallYSize)][clamp(z+adj3,0,smallZSize)] =
                                    color;
                            if(blob[w][clamp(x+adj1_, 0,smallXSize)][clamp(smallYSize - y-adj2_, 0, smallYSize)][clamp(z+adj3_,0,smallZSize)] == 0)
                                blob[w][clamp(x+adj1_, 0,smallXSize)][clamp(smallYSize - y-adj2_, 0, smallYSize)][clamp(z+adj3_,0,smallZSize)] = color;
                            if(blob[w][clamp(x+adj1_, 0,smallXSize)][clamp(y+adj2_, 0, smallYSize)][clamp(z+adj3_,0,smallZSize)] == 0)
                                blob[w][clamp(x+adj1_, 0,smallXSize)][clamp(y+adj2_, 0, smallYSize)][clamp(z+adj3_,0,smallZSize)] = color;
                        }
                    }
                }
            }
        }
        return blob;
        //return blob;
        //return Tools3D.runCA(nextShip, 1);
    }

}
