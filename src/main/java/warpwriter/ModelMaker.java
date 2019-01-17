package warpwriter;

import squidpony.squidmath.FastNoise;
import squidpony.squidmath.GWTRNG;
import squidpony.squidmath.LinnormRNG;
import squidpony.squidmath.NumberTools;
import warpwriter.model.color.Colorizer;
import warpwriter.model.nonvoxel.LittleEndianDataInputStream;

import java.io.InputStream;

import static squidpony.squidmath.DiverRNG.determineBounded;
import static squidpony.squidmath.DiverRNG.determineFloat;
import static squidpony.squidmath.MathExtras.clamp;
//import static squidpony.squidmath.Noise.PointHash.hashAll;

/**
 * Created by Tommy Ettinger on 11/4/2017.
 */
public class ModelMaker {
    public final boolean RINSED_PALETTE = true;
    public final byte EYE_DARK = RINSED_PALETTE ? 22 : 30;
    public final byte EYE_LIGHT = 17;
    public GWTRNG rng;
    private byte[][][] ship, shipLarge, warriorMale, sword0, spear0, shield0, shield1;
    private byte[][][][] rightHand, leftHand;
    private int xSize, ySize, zSize;

    private Colorizer colorizer;

    public ModelMaker()
    {
        this((long)((Math.random() - 0.5) * 4.503599627370496E15) ^ (long)((Math.random() - 0.5) * 2.0 * -9.223372036854776E18), Colorizer.AuroraColorizer);
    }
    public ModelMaker(long seed)
    {
        this(seed, Colorizer.AuroraColorizer);
    }
    public ModelMaker(long seed, Colorizer colorizer)
    {
        rng = new GWTRNG(seed);
        InputStream is = this.getClass().getResourceAsStream("/ship.vox");
        ship = VoxIO.readVox(new LittleEndianDataInputStream(is));
        if(ship == null) ship = new byte[12][12][8];
        is = this.getClass().getResourceAsStream("/ship_40_40_30.vox");
        shipLarge = VoxIO.readVox(new LittleEndianDataInputStream(is));
        if(shipLarge == null) shipLarge = new byte[40][40][30];
        xSize = ship.length;
        ySize = ship[0].length;
        zSize = ship[0][0].length;
        
        this.colorizer = colorizer;
        
        is = this.getClass().getResourceAsStream((RINSED_PALETTE ? "/Rinsed_" : "/")  + "Warrior_Male_Attach.vox");
        warriorMale = VoxIO.readVox(new LittleEndianDataInputStream(is));
        if(warriorMale == null) warriorMale = new byte[12][12][8];
        is = this.getClass().getResourceAsStream((RINSED_PALETTE ? "/Rinsed_" : "/")  + "Sword_1H_Attach.vox");
        sword0 = VoxIO.readVox(new LittleEndianDataInputStream(is));
        if(sword0 == null) sword0 = new byte[12][12][8];
        is = this.getClass().getResourceAsStream((RINSED_PALETTE ? "/Rinsed_" : "/")  + "Spear_1H_Attach.vox");
        spear0 = VoxIO.readVox(new LittleEndianDataInputStream(is));
        if(spear0 == null) spear0 = new byte[12][12][8];
        is = this.getClass().getResourceAsStream((RINSED_PALETTE ? "/Rinsed_" : "/")  + "Board_Shield_1H_Attach.vox");
        shield0 = VoxIO.readVox(new LittleEndianDataInputStream(is));
        if(shield0 == null) shield0 = new byte[12][12][8];
        is = this.getClass().getResourceAsStream((RINSED_PALETTE ? "/Rinsed_" : "/")  + "Round_Shield_1H_Attach.vox");
        shield1 = VoxIO.readVox(new LittleEndianDataInputStream(is));
        if(shield1 == null) shield1 = new byte[12][12][8];
        
        rightHand = new byte[][][][]{sword0, spear0};
        leftHand = new byte[][][][]{shield0, shield1};
    }
    public Colorizer getColorizer() {
        return colorizer;
    }

    public void setColorizer(Colorizer colorizer) {
        this.colorizer = colorizer;
    }

    /**
     * Gets a 64-bit point hash of a 3D point (x, y, and z are all longs) and a state/seed as a long. This point hash
     * has just about the best speed of any algorithms tested, and though its quality is almost certainly bad for
     * traditional uses of hashing (such as hash tables), it's sufficiently random to act as a positional RNG.
     * <p>
     * This uses a technique related to the one used by Martin Roberts for his golden-ratio-based sub-random sequences,
     * where each axis is multiplied by a different constant, and the choice of constants depends on the number of axes
     * but is always related to a generalized form of golden ratios, repeatedly dividing 1.0 by the generalized ratio.
     * See <a href="http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">Roberts' article</a>
     * for some more information on how he uses this, but we do things differently because we want random-seeming
     * results instead of separated sub-random results.
     * <p>
     * Should be very similar to {@link squidpony.squidmath.Noise.HastyPointHash#hashAll(long, long, long, long)},
     * if not identical. We have a version here that gets a hash between 0 and a bound, as well.
     * @param x x position; any long
     * @param y y position; any long
     * @param z z position; any long
     * @param s the state; any long
     * @return 64-bit hash of the x,y,z point with the given state
     */
    public static long hashAll(long x, long y, long z, long s) {
        z += s * 0xDB4F0B9175AE2165L;
        y += z * 0xBBE0563303A4615FL;
        x += y * 0xA0F2EC75A1FE1575L;
        s += x * 0x89E182857D9ED689L;
        return ((s = (s ^ s >>> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) ^ s >>> 25);
    }
    /**
     * Gets a 64-bit point hash of a 4D point (x, y, z, and w are all longs) and a state/seed as a long. This point
     * hash has just about the best speed of any algorithms tested, and though its quality is almost certainly bad for
     * traditional uses of hashing (such as hash tables), it's sufficiently random to act as a positional RNG.
     * <p>
     * This uses a technique related to the one used by Martin Roberts for his golden-ratio-based sub-random sequences,
     * where each axis is multiplied by a different constant, and the choice of constants depends on the number of axes
     * but is always related to a generalized form of golden ratios, repeatedly dividing 1.0 by the generalized ratio.
     * See <a href="http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">Roberts' article</a>
     * for some more information on how he uses this, but we do things differently because we want random-seeming
     * results instead of separated sub-random results.
     * <p>
     * Should be very similar to {@link squidpony.squidmath.Noise.HastyPointHash#hashAll(long, long, long, long, long)},
     * if not identical. We have a version here that gets a hash between 0 and a bound, as well.
     * @param x x position; any long
     * @param y y position; any long
     * @param z z position; any long
     * @param w w position (often time); any long
     * @param s the state; any long
     * @return 64-bit hash of the x,y,z,w point with the given state
     */
    public static long hashAll(long x, long y, long z, long w, long s) {
        w += s * 0xE19B01AA9D42C633L;
        z += w * 0xC6D1D6C8ED0C9631L;
        y += z * 0xAF36D01EF7518DBBL;
        x += y * 0x9A69443F36F710E7L;
        s += x * 0x881403B9339BD42DL;
        return ((s = (s ^ s >>> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) ^ s >>> 25);
    }
    /**
     * Gets a bounded int point hash of a 3D point (x, y, and z are all longs) and a state/seed as a long. This point
     * hash has just about the best speed of any algorithms tested, and though its quality is almost certainly bad for
     * traditional uses of hashing (such as hash tables), it's sufficiently random to act as a positional RNG.
     * <p>
     * This uses a technique related to the one used by Martin Roberts for his golden-ratio-based sub-random sequences,
     * where each axis is multiplied by a different constant, and the choice of constants depends on the number of axes
     * but is always related to a generalized form of golden ratios, repeatedly dividing 1.0 by the generalized ratio.
     * See <a href="http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">Roberts' article</a>
     * for some more information on how he uses this, but we do things differently because we want random-seeming
     * results instead of separated sub-random results.
     * <p>
     * Should be very similar to {@link squidpony.squidmath.Noise.HastyPointHash#hashAll(long, long, long, long)}, but
     * gets a hash between 0 and a bound, instead of a 64-bit long.
     * @param x x position; any long
     * @param y y position; any long
     * @param z z position; any long
     * @param s the state; any long
     * @param bound outer exclusive bound; may be negative
     * @return an int between 0 (inclusive) and bound (exclusive) dependent on the position and state
     */
    public static int hashBounded(long x, long y, long z, long s, int bound)
    {
        z += s * 0xDB4F0B9175AE2165L;
        y += z * 0xBBE0563303A4615FL;
        x += y * 0xA0F2EC75A1FE1575L;
        s += x * 0x89E182857D9ED689L;
        return (int)((bound * (((s = (s ^ s >>> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) ^ s >>> 25) & 0xFFFFFFFFL)) >> 32);
    }

    /**
     * Gets a bounded int point hash of a 4D point (x, y, z, and w are all longs) and a state/seed as a long. This point
     * hash has just about the best speed of any algorithms tested, and though its quality is almost certainly bad for
     * traditional uses of hashing (such as hash tables), it's sufficiently random to act as a positional RNG.
     * <p>
     * This uses a technique related to the one used by Martin Roberts for his golden-ratio-based sub-random sequences,
     * where each axis is multiplied by a different constant, and the choice of constants depends on the number of axes
     * but is always related to a generalized form of golden ratios, repeatedly dividing 1.0 by the generalized ratio.
     * See <a href="http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">Roberts' article</a>
     * for some more information on how he uses this, but we do things differently because we want random-seeming
     * results instead of separated sub-random results.
     * <p>
     * Should be very similar to {@link squidpony.squidmath.Noise.HastyPointHash#hashAll(long, long, long, long, long)},
     * but gets a hash between 0 and a bound, instead of a 64-bit long.
     * @param x x position; any long
     * @param y y position; any long
     * @param z z position; any long
     * @param w w position (often time); any long
     * @param s the state; any long
     * @param bound outer exclusive bound; may be negative
     * @return an int between 0 (inclusive) and bound (exclusive) dependent on the position and state
     */
    public static int hashBounded(long x, long y, long z, long w, long s, int bound)
    {
        w += s * 0xE19B01AA9D42C633L;
        z += w * 0xC6D1D6C8ED0C9631L;
        y += z * 0xAF36D01EF7518DBBL;
        x += y * 0x9A69443F36F710E7L;
        s += x * 0x881403B9339BD42DL;
        return (int)((bound * (((s = (s ^ s >>> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) ^ s >>> 25) & 0xFFFFFFFFL)) >> 32);
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
        byte mainColor = (byte)(RINSED_PALETTE ? rng.nextSignedInt(30) * 8 + rng.between(17, 20) : rng.nextSignedInt(18) * 6 + rng.between(22, 25)),
                highlightColor = (byte)(RINSED_PALETTE ? rng.nextSignedInt(30) * 8 + rng.between(18, 21) : rng.nextSignedInt(18) * 6 + rng.between(21, 24));
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
        long state = rng.getState();
        long current = (RINSED_PALETTE) ? determineBounded(state+1L, 30):  determineBounded(state + 1L, 18);
        final byte mainColor = (byte)((RINSED_PALETTE) 
                ? (current * 8) + determineBounded(state + 22L, 4) + 18 
                : (current * 6) + determineBounded(state + 22L, 3) + 22),
                highlightColor = (byte)((RINSED_PALETTE)
                        ? ((current + 4 + determineBounded(state + 333L, 10)) % 30) * 8 + determineBounded(state + 4444L, 4) + 18
                        : ((current + 4 + determineBounded(state + 333L, 10)) % 18) * 6 + determineBounded(state + 4444L, 3) + 21);
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
                        voxels[x][12 - y][z] = voxels[x][y - 1][z] = EYE_DARK;
                        voxels[x][11 - y][z] = voxels[x][y    ][z] = EYE_DARK;
                        voxels[x + 1][12 - y][z] = voxels[x + 1][y    ][z] = EYE_DARK;     // intentionally asymmetrical
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
    private static final int[] RINSED_COCKPIT_COLORS = {19, 20, 21, 22, 23, 24, 25, 26, 27};
    public byte[][][] shipRandom()
    {
        xSize = ship.length;
        ySize = ship[0].length;
        zSize = ship[0][0].length;
        byte[][][] nextShip = new byte[xSize][ySize][zSize];
        final int halfY = ySize >> 1, smallYSize = ySize - 1;
        long seed = rng.nextLong(), current;
        final byte mainColor = (byte)((RINSED_PALETTE)
                ? determineBounded(seed + 1L, 30) * 8 + determineBounded(seed + 22L, 4) + 18
                : determineBounded(seed + 1L, 18) * 6 +  + determineBounded(seed + 22L, 3) + 22),
                highlightColor = (byte)((RINSED_PALETTE)
                        ? ((determineBounded(seed + 333L, 30))) * 8 + determineBounded(seed + 4444L, 4) + 17
                        : ((determineBounded(seed + 333L, 18))) * 6 + determineBounded(seed + 4444L, 3) + 21),
                cockpitColor = (byte)((RINSED_PALETTE) 
                        ? RINSED_COCKPIT_COLORS[determineBounded(seed + 55555L, 6)] * 8 + 19 
                        : 84 + (determineBounded(seed + 55555L, 6) * 6));
        byte color;
//        final byte mainColor = (byte)((determineBounded(seed + 1L, 18) * 6) + determineBounded(seed + 22L, 3) + 22),
//                highlightColor = (byte)((determineBounded(seed + 333L, 18) * 6) + determineBounded(seed + 4444L, 3) + 21),
//                cockpitColor = (byte)(84 + (determineBounded(seed + 55555L, 6) * 6));
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
                                            : ((current >>> 12 & 0x3FL) < 40L) ? (byte)(
                                            (RINSED_PALETTE) ? 18 + (current & 3) : 18 + (current & 7))
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
        xSize = shipLarge.length;
        ySize = shipLarge[0].length;
        zSize = shipLarge[0][0].length;
        byte[][][] nextShip = new byte[xSize][ySize][zSize];
        final int halfY = ySize >> 1, smallYSize = ySize - 1;
        int color;
        long seed = rng.nextLong(), current, paint;
        final byte mainColor = (byte)((RINSED_PALETTE)
                ? determineBounded(seed + 1L, 30) * 8 + determineBounded(seed + 22L, 4) + 18
                : determineBounded(seed + 1L, 18) * 6 +  + determineBounded(seed + 22L, 3) + 22),
                highlightColor = (byte)((RINSED_PALETTE)
                        ? ((determineBounded(seed + 333L, 30))) * 8 + determineBounded(seed + 4444L, 4) + 17
                        : ((determineBounded(seed + 333L, 18))) * 6 + determineBounded(seed + 4444L, 3) + 21),
                cockpitColor = (byte)((RINSED_PALETTE)
                        ? RINSED_COCKPIT_COLORS[determineBounded(seed + 55555L, 6)] * 8 + 22
                        : 84 + (determineBounded(seed + 55555L, 6) * 6));
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
                                            : ((paint >>> 12 & 0x3FL) < 40L) ? (byte)(
                                            (RINSED_PALETTE) ? 18 + (paint & 3) : 18 + (paint & 7))
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
//    /**
//     * Use <a href="https://i.imgur.com/CrI1LyU.png">This image with Aurora hex codes</a> for reference.
//     */
//    private static final byte[] AURORA_COCKPIT_COLORS = {0x69, 0x6A, 0x6B, 0x6C, 0x6D, 0x6E, 0x6F, 0x70, 0x71, 0x72,
//            0x71, 0x72, 0x73, 0x78, 0x79, 0x7A, 0x7B,
//            (byte) 0xBC, (byte) 0xBE, (byte) 0xBF, (byte) 0xC1, (byte) 0xC3, (byte) 0xC4, (byte) 0xC5,
//            (byte) 0xC6, (byte) 0xC7, (byte) 0xCC, (byte) 0xCD, (byte) 0xCF };

    /**
     * Uses the {@link #getColorizer() colorizer} this was constructed with, with the default
     * {@link Colorizer#AuroraColorizer}.
     * @return a 3D byte array storing a spaceship
     */
    public byte[][][] shipLargeRandomColorized()
    {
        xSize = shipLarge.length;
        ySize = shipLarge[0].length;
        zSize = shipLarge[0][0].length;
        byte[][][] nextShip = new byte[xSize][ySize][zSize];
        final int halfY = ySize >> 1, smallYSize = ySize - 1;
        int color;
        long seed = rng.nextLong(), current, paint;
        final byte mainColor = colorizer.darken(colorizer.getReducer().randomColorIndex(rng)),
                //Dimmer.AURORA_RAMPS[colorizer.randomColorIndex(rng) & 255][2],
                highlightColor = colorizer.brighten(colorizer.getReducer().randomColorIndex(rng)),
                //Dimmer.AURORA_RAMPS[Dimmer.AURORA_RAMPS[colorizer.randomColorIndex(rng) & 255][0] & 255][0],
                cockpitColor = colorizer.darken(colorizer.reduce((0x40 + determineBounded(seed + 0x11111L, 0x70) << 24)
                        | (0xA0 + determineBounded(seed + 0x22222L, 0x60) << 16)
                        | (0xC0 + determineBounded(seed + 0x33333L, 0x40) << 8) | 0xFF));
        //AURORA_COCKPIT_COLORS[determineBounded(seed + 55555L, AURORA_COCKPIT_COLORS.length)];
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
                                nextShip[x][smallYSize - y][z] = nextShip[x][y][z] =
                                        cockpitColor;
                            //Dimmer.AURORA_RAMPS[cockpitColor & 255][3 - (z + 6 >> 3) & 3];
                        } else {
                            nextShip[x][smallYSize - y][z] = nextShip[x][y][z] =
                                    // checks another 6 bits, starting after discarding 6 bits from the bottom
                                    ((current >>> 6 & 0x1FFL) < color * 6)
                                            ? 0
                                            // checks another 6 bits, starting after discarding 12 bits from the bottom
                                            : ((paint >>> 12 & 0x3FL) < 40L)
                                            ? colorizer.grayscale()[determineBounded(paint, colorizer.grayscale().length - 2) + 1]
                                            // Dimmer.AURORA_RAMPS[10][(int) paint & 3]
                                            // checks another 6 bits, starting after discarding 18 bits from the bottom
                                            : ((paint >>> 18 & 0x3FL) < 8L)
                                            ? highlightColor
                                            : mainColor;
                        }
                    }
                }
            }
        }
        return Tools3D.largestPart(nextShip);
        //return nextShip;
        //return Tools3D.runCA(nextShip, 1);
    }

    /**
     * Uses some simplex noise from {@link FastNoise} to make paint patterns and shapes more "flowing" and less
     * haphazard in their placement. Still uses point hashes for a lot of its operations.
     * @return 3D byte array representing a spaceship
     */
    public byte[][][] shipLargeNoiseColorized()
    {
        xSize = shipLarge.length;
        ySize = shipLarge[0].length;
        zSize = shipLarge[0][0].length;
        byte[][][] nextShip = new byte[xSize][ySize][zSize];
        final int halfY = ySize >> 1, smallYSize = ySize - 1;
        int color;
        int seed = rng.nextInt(), current, paint;
        final byte mainColor = colorizer.darken(colorizer.getReducer().randomColorIndex(rng)),
                highlightColor = colorizer.brighten(colorizer.getReducer().randomColorIndex(rng)),
                cockpitColor = colorizer.darken(colorizer.reduce((0x40 + determineBounded(seed + 0x11111L, 0x70) << 24)
                        | (0xA0 + determineBounded(seed + 0x22222L, 0x60) << 16)
                        | (0xC0 + determineBounded(seed + 0x33333L, 0x40) << 8) | 0xFF));
        final FastNoise noiseMid = new FastNoise(~seed, 0x1p-5f);
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
                        current = (int) hashAll(xx + (xx | zz) >> 3, (yy + (yy | zz)) / 3, zz, color, seed)
                        + (int) (noiseMid.getSimplex(x * 0.5f, y * 0.75f, z * 0.666f) * 0x800000) + 0x800000;
                        paint = (int) hashAll((xx + (xx | z)) / 7, (yy + (yy | z)) / 5, z, color, seed);
//                        current = (int) (noiseOuter.getSimplex(x * 1.5f, y * 1.75f, z * 1.3666f) * 0x800000)
//                                + (int) (noiseMid.getSimplex(x * 1.5f, y * 1.75f, z * 1.3666f) * 0x800000) + 0x800000
//                                + (int) (noiseInner.getSimplex(x * 1.5f, y * 1.75f, z * 1.3666f) * 0x300000) - color;
//                        paint   = (int) (noiseOuter.getSimplex(x * 0.0625f, y * 0.125f, z * 0.1f) * 0x800000)
//                                + (int) (noiseMid.getSimplex(x * 0.0625f, y * 0.125f, z * 0.1f) * 0x500000)
//                                + (int) (noiseInner.getSimplex(x * 0.0625f, y * 0.125f, z * 0.1f) * 0x300000) + color;
                        if (color < 8) {
                            // checks top 3 bits
                            if((current >>> 21 & 7) != 0)
                                nextShip[x][smallYSize - y][z] = nextShip[x][y][z] =
                                        cockpitColor;
                        } else {
                            nextShip[x][smallYSize - y][z] = nextShip[x][y][z] =
                                    // checks top 9 bits, different branch
                                    ((current >>> 15 & 0x1FF) < color * 6)
                                            ? 0
                                            // checks 6 bits of paint, unusual start
                                            : ((paint >>> 19 & 0x3F) < 36)
                                            ? colorizer.grayscale()[(int)((noiseMid.getSimplex(x * 0.125f, y * 0.25f, z * 0.5f) * 0.4f + 0.599f) * (colorizer.grayscale().length - 1))]
                                            // checks another 6 bits of paint, starting after discarding 6 bits
                                            : (noiseMid.getSimplex(x * 0.04f, y * 0.07f, z * 0.125f) > 0.1f)
                                            ? highlightColor
                                            : mainColor;
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
    /**
     * Gets the minimum random int between 0 and {@code bound} generated out of {@code trials} generated numbers.
     * Useful for when numbers should have a strong bias toward zero, but all possible values are between 0, inclusive,
     * and bound, exclusive.
     * @param bound the outer exclusive bound; may be negative or positive
     * @param trials how many numbers to generate and get the minimum of
     * @return the minimum generated int between 0 and bound out of the specified amount of trials
     */
    public int minIntOf(final int bound, final int trials)
    {
        int value = rng.nextSignedInt(bound);
        for (int i = 1; i < trials; i++) {
            value = Math.min(value, rng.nextSignedInt(bound));
        }
        return value;
    }
    /**
     * Gets the maximum random int between 0 and {@code bound} generated out of {@code trials} generated numbers.
     * Useful for when numbers should have a strong bias away from zero, but all possible values are between 0,
     * inclusive, and bound, exclusive.
     * @param bound the outer exclusive bound; may be negative or positive
     * @param trials how many numbers to generate and get the maximum of
     * @return the maximum generated int between 0 and bound out of the specified amount of trials
     */
    public int maxIntOf(final int bound, final int trials)
    {
        int value = rng.nextSignedInt(bound);
        for (int i = 1; i < trials; i++) {
            value = Math.max(value, rng.nextSignedInt(bound));
        }
        return value;
    }

    public byte[][][] warriorRandom()
    {
        byte[][][][] used = new byte[maxIntOf(2, 4) + 1][][][];
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
        final byte mainColor = (byte)((RINSED_PALETTE)
                ? determineBounded(seed + 1L, 30) * 8 + determineBounded(seed + 22L, 4) + 18
                : determineBounded(seed + 1L, 18) * 6 +  + determineBounded(seed + 22L, 3) + 22),
                highlightColor = (byte)((RINSED_PALETTE)
                        ? ((determineBounded(seed + 333L, 30))) * 8 + determineBounded(seed + 4444L, 4) + 17
                        : ((determineBounded(seed + 333L, 18))) * 6 + determineBounded(seed + 4444L, 3) + 21);
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
        final byte mainColor = (byte)((RINSED_PALETTE)
                ? determineBounded(seed + 1L, 30) * 8 + determineBounded(seed + 22L, 4) + 18
                : determineBounded(seed + 1L, 18) * 6 +  + determineBounded(seed + 22L, 3) + 22),
                highlightColor = (byte)((RINSED_PALETTE)
                        ? ((determineBounded(seed + 333L, 30))) * 8 + determineBounded(seed + 4444L, 4) + 17
                        : ((determineBounded(seed + 333L, 18))) * 6 + determineBounded(seed + 4444L, 3) + 21);
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

    /**
     * Gets a random color palette index, adapted for whether this uses {@link Coloring#RINSED} or
     * {@link Coloring#ALT_PALETTE}. It will always be in the middle of the color range, but can lean towards darker
     * colors more often than lighter ones.
     * @return a byte representing a color palette index, randomly chosen
     */
    public byte randomMainColor() {
        return (byte)(RINSED_PALETTE
                ? rng.nextSignedInt(30) * 8 + rng.between(18, 22)
                : rng.nextSignedInt(18) * 6 + rng.between(21, 24));
    }

    /**
     * Gets 5 colors from lightest to darkest, with the same hue chosen randomly from the RINSED palette.
     * @return a 5-element byte array, with the first item having the lightest color and the last having the darkest
     */
    public byte[] randomColorRange()
    {
        byte idx = (byte) ((rng.nextSignedInt(30) << 3) + 17);
        return new byte[]{idx, (byte) (idx+1), (byte) (idx+2), (byte) (idx+3), (byte) (idx+4)};
    }
    /**
     * Gets 5 colors from lightest to darkest, with the same hue drawn from the given mainColor.
     * @param mainColor the color to mimic the hue of
     * @return a 5-element byte array, with the first item having the lightest color and the last having the darkest
     */
    public static byte[] colorRange(byte mainColor)
    {
        byte idx = (byte) (((mainColor >>> 3) << 3)+1);
        return new byte[]{idx, (byte) (idx+1), (byte) (idx+2), (byte) (idx+3), (byte) (idx+4)};
    }

    /**
     * Gets a random color palette index, always using {@link Coloring#RINSED}. It will always be in the middle of the
     * color range, but can lean towards darker colors more often than lighter ones.
     * @param seed a long seed that should be different every time this is called
     * @return a byte representing a color palette index, randomly chosen
     */
    public static byte randomMainColor(long seed) {
        return (byte)(LinnormRNG.determineBounded(seed, 30) * 8 + LinnormRNG.determineBounded(~seed, 4) + 18);
    }

    /**
     * Gets 5 colors from lightest to darkest, with the same hue chosen randomly from the RINSED palette.
     * @param seed a long seed that should be different every time this is called
     * @return a 5-element byte array, with the first item having the lightest color and the last having the darkest
     */
    public static byte[] randomColorRange(long seed)
    {
        byte idx = (byte) ((LinnormRNG.determineBounded(seed, 30) << 3) + 17);
        return new byte[]{idx, (byte) (idx+1), (byte) (idx+2), (byte) (idx+3), (byte) (idx+4)};
    }

}
