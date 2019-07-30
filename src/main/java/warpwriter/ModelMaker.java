package warpwriter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import squidpony.squidmath.*;
import warpwriter.model.color.Colorizer;
import warpwriter.model.nonvoxel.LittleEndianDataInputStream;

import java.io.InputStream;

import static squidpony.squidmath.GWTRNG.determineBounded;
import static squidpony.squidmath.GWTRNG.determineInt;
import static squidpony.squidmath.MathExtras.clamp;
import static squidpony.squidmath.Noise.IntPointHash.hash32;
import static squidpony.squidmath.Noise.IntPointHash.hashAll;
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
        this((long)((Math.random() - 0.5) * 4.503599627370496E15) ^ (long)((Math.random() - 0.5) * 2.0 * -9.223372036854776E18), Colorizer.RinsedColorizer);
    }
    public ModelMaker(long seed)
    {
        this(seed, Colorizer.RinsedColorizer);
    }
    public ModelMaker(long seed, Colorizer colorizer)
    {
        rng = new GWTRNG(seed);
        InputStream is = Gdx.files.internal("ship.vox").read();
        ship = VoxIO.readVox(new LittleEndianDataInputStream(is));
        if(ship == null) ship = new byte[12][12][8];
        is = Gdx.files.internal("ship_40_40_30.vox").read();
        shipLarge = VoxIO.readVox(new LittleEndianDataInputStream(is));
        if(shipLarge == null) shipLarge = new byte[40][40][30];
        xSize = ship.length;
        ySize = ship[0].length;
        zSize = ship[0][0].length;
        
        this.colorizer = colorizer;
        
        is = Gdx.files.internal((RINSED_PALETTE ? "Rinsed_" : "")  + "Warrior_Male_Attach.vox").read();
        warriorMale = VoxIO.readVox(new LittleEndianDataInputStream(is));
        if(warriorMale == null) warriorMale = new byte[12][12][8];
        is = Gdx.files.internal((RINSED_PALETTE ? "Rinsed_" : "")  + "Sword_1H_Attach.vox").read();
        sword0 = VoxIO.readVox(new LittleEndianDataInputStream(is));
        if(sword0 == null) sword0 = new byte[12][12][8];
        is = Gdx.files.internal((RINSED_PALETTE ? "Rinsed_" : "")  + "Spear_1H_Attach.vox").read();
        spear0 = VoxIO.readVox(new LittleEndianDataInputStream(is));
        if(spear0 == null) spear0 = new byte[12][12][8];
        is = Gdx.files.internal((RINSED_PALETTE ? "Rinsed_" : "")  + "Board_Shield_1H_Attach.vox").read();
        shield0 = VoxIO.readVox(new LittleEndianDataInputStream(is));
        if(shield0 == null) shield0 = new byte[12][12][8];
        is = Gdx.files.internal((RINSED_PALETTE ? "Rinsed_" : "")  + "Round_Shield_1H_Attach.vox").read();
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




    /**
     * Gets a bounded int point hash of a 2D point (x and y are both ints) and a state/seed as an int. This point
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
     * Should be very similar to {@link squidpony.squidmath.Noise.IntPointHash#hashAll(int, int, int)}, but gets a hash
     * between 0 and a bound, instead of any 32-bit int.
     * @param x x position; any int
     * @param y y position; any int
     * @param s the state; any int
     * @param bound outer exclusive bound; may be negative
     * @return an int between 0 (inclusive) and bound (exclusive) dependent on the position and state
     */
    public static int hashBounded(int x, int y, int s, int bound) {
        s ^= x * 0x1827F5 ^ y * 0x123C21;
        return (int) (bound * (((s = (s ^ (s << 19 | s >>> 13) ^ (s << 6 | s >>> 26) ^ 0xD1B54A35) * 0x125493) ^ s >>> 15) & 0xFFFFFFFFL) >> 32);
    }


    /**
     * Gets a bounded int point hash of a 3D point (x, y, and z are all ints) and a state/seed as an int. This point
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
     * Should be very similar to {@link squidpony.squidmath.Noise.IntPointHash#hashAll(int, int, int, int)}, but gets a
     * hash between 0 and a bound, instead of any 32-bit int.
     * @param x x position; any int
     * @param y y position; any int
     * @param z z position; any int
     * @param s the state; any int
     * @param bound outer exclusive bound; may be negative
     * @return an int between 0 (inclusive) and bound (exclusive) dependent on the position and state
     */
    public static int hashBounded(int x, int y, int z, int s, int bound)
    {
        s ^= x * 0x1A36A9 ^ y * 0x157931 ^ z * 0x119725;
        return (int) (bound * (((s = (s ^ (s << 19 | s >>> 13) ^ (s << 6 | s >>> 26) ^ 0xD1B54A35) * 0x125493) ^ s >>> 15) & 0xFFFFFFFFL) >> 32);
    }

    /**
     * Gets a bounded int point hash of a 4D point (x, y, z, and w are all ints) and a state/seed as an int. This point
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
     * Should be very similar to {@link squidpony.squidmath.Noise.IntPointHash#hashAll(int, int, int, int, int)}, but
     * gets a hash between 0 and a bound, instead of any 32-bit int.
     * @param x x position; any int
     * @param y y position; any int
     * @param z z position; any int
     * @param w w position, often time; any int
     * @param s the state; any int
     * @param bound outer exclusive bound; may be negative
     * @return an int between 0 (inclusive) and bound (exclusive) dependent on the position and state
     */
    public static int hashBounded(int x, int y, int z, int w, int s, int bound)
    {
        s ^= x * 0x1B69E1 ^ y * 0x177C0B ^ z * 0x141E5D ^ w * 0x113C31;
        return (int) (bound * (((s = (s ^ (s << 19 | s >>> 13) ^ (s << 6 | s >>> 26) ^ 0xD1B54A35) * 0x125493) ^ s >>> 15) & 0xFFFFFFFFL) >> 32);
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
        int state = rng.getStateA();
        int current = (RINSED_PALETTE) ? determineBounded(state+1, 30):  determineBounded(state + 1, 18);
        final byte mainColor = (byte)((RINSED_PALETTE) 
                ? (current * 8) + determineBounded(state + 22, 4) + 18 
                : (current * 6) + determineBounded(state + 22, 3) + 22),
                highlightColor = (byte)((RINSED_PALETTE)
                        ? ((current + 4 + determineBounded(state + 333, 10)) % 30) * 8 + determineBounded(state + 4444, 4) + 18
                        : ((current + 4 + determineBounded(state + 333, 10)) % 18) * 6 + determineBounded(state + 4444, 3) + 21);
        do {
            final int seed = rng.nextInt();
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
                                            ((Math.abs(x - 7) + 3) * (Math.abs(z - 4) + 2) * (7 - y)) * 21) < 555) ?
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
        int seed = rng.nextInt(), current;
        final byte mainColor = (byte)((RINSED_PALETTE)
                ? determineBounded(seed + 1, 30) * 8 + determineBounded(seed + 22, 4) + 18
                : determineBounded(seed + 1, 18) * 6 +  + determineBounded(seed + 22, 3) + 22),
                highlightColor = (byte)((RINSED_PALETTE)
                        ? ((determineBounded(seed + 333, 30))) * 8 + determineBounded(seed + 4444, 4) + 17
                        : ((determineBounded(seed + 333, 18))) * 6 + determineBounded(seed + 4444, 3) + 21),
                cockpitColor = (byte)((RINSED_PALETTE) 
                        ? RINSED_COCKPIT_COLORS[determineBounded(seed + 55555, 6)] * 8 + 19 
                        : 84 + (determineBounded(seed + 55555, 6) * 6));
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
                        if (color > 0 && color < 8 && z >= 2) {
                            nextShip[x][smallYSize - y][z] = nextShip[x][y][z] = (byte) (cockpitColor - (z - 2 >> 1));//9;
                        } else {
                            nextShip[x][smallYSize - y][z] = nextShip[x][y][z] =
                                    // checks bottom 6 bits
                                    ((current & 0x3F) < 45)
                                            ? 0
                                            // checks another 6 bits, starting after discarding 6 bits from the bottom
                                            : ((current >>> 6 & 0x3F) < 40) ? (byte)(
                                            (RINSED_PALETTE) ? 18 + (current & 3) : 18 + (current & 7))
                                            // checks another 6 bits, starting after discarding 12 bits from the bottom
                                            : ((current >>> 12 & 0x3F) < 8) ? highlightColor : mainColor;
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
        int seed = rng.nextInt(), current, paint;
        final byte mainColor = (byte)((RINSED_PALETTE)
                ? determineBounded(seed + 1, 30) * 8 + determineBounded(seed + 22, 4) + 18
                : determineBounded(seed + 1, 18) * 6 +  + determineBounded(seed + 22, 3) + 22),
                highlightColor = (byte)((RINSED_PALETTE)
                        ? ((determineBounded(seed + 333, 30))) * 8 + determineBounded(seed + 4444, 4) + 17
                        : ((determineBounded(seed + 333, 18))) * 6 + determineBounded(seed + 4444, 3) + 21),
                cockpitColor = (byte)((RINSED_PALETTE)
                        ? RINSED_COCKPIT_COLORS[determineBounded(seed + 55555, 6)] * 8 + 22
                        : 84 + (determineBounded(seed + 55555, 6) * 6));
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
                                    // checks 9 bits
                                    ((current & 0x1FFL) < color * 6)
                                            ? 0
                                            // checks another 6 bits, starting after discarding 9 bits from the bottom
                                            : ((paint >>> 9 & 0x3F) < 40) ? (byte)(
                                            (RINSED_PALETTE) ? 18 + (paint & 3) : 18 + (paint & 7))
                                            // checks another 6 bits, starting after discarding 15 bits from the bottom
                                            : ((paint >>> 15 & 0x3F) < 8) ? highlightColor : mainColor;
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
        int seed = rng.nextInt(), current, paint;
        final byte mainColor = colorizer.darken(colorizer.getReducer().paletteMapping[(int) seed & 0x7FFF]), // bottom 15 bits
                highlightColor = colorizer.brighten(colorizer.getReducer().paletteMapping[(int) seed >>> 17]), // top 15 of 32 bits
                cockpitColor = colorizer.darken(colorizer.reduce((0x20 + determineBounded(seed + 0x11111, 0x60) << 24)
                        | (0xA0 + determineBounded(seed + 0x22222, 0x60) << 16)
                        | (0xC8 + determineBounded(seed + 0x33333, 0x38) << 8) | 0xFF));
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
                                    // checks 9 bits
                                    ((current & 0x1FF) < color * 6)
                                            ? 0
                                            // checks another 6 bits, starting after discarding 9 bits from the bottom
                                            : ((paint >>> 9 & 0x3F) < 40)
                                            ? colorizer.grayscale()[determineBounded(paint, colorizer.grayscale().length - 2) + 1]
                                            // Dimmer.AURORA_RAMPS[10][(int) paint & 3]
                                            // checks another 6 bits, starting after discarding 15 bits from the bottom
                                            : ((paint >>> 15 & 0x3F) < 8)
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
     * @return a 12x12x8 3D byte array representing a spaceship
     */
    public byte[][][] shipNoiseColorized()
    {
        return shipNoiseColorized(ship);
    }
    /**
     * Uses some simplex noise from {@link FastNoise} to make paint patterns and shapes more "flowing" and less
     * haphazard in their placement. Still uses point hashes for a lot of its operations.
     * @return a larger (40x40x30) 3D byte array representing a spaceship
     */
    public byte[][][] shipLargeNoiseColorized()
    {
        return Tools3D.largestPart(shipNoiseColorized(shipLarge));
    }
    /**
     * Uses some simplex noise from {@link FastNoise} to make paint patterns and shapes more "flowing" and less
     * haphazard in their placement. Still uses point hashes for a lot of its operations.
     * @param ship one of the two ships loaded from resources here, probably, {@link #ship} and {@link #shipLarge}
     * @return 3D byte array representing a spaceship
     */
    private byte[][][] shipNoiseColorized(byte[][][] ship)
    {
        xSize = ship.length;
        ySize = ship[0].length;
        zSize = ship[0][0].length;
        byte[][][] nextShip = new byte[xSize][ySize][zSize];
        final int halfY = ySize >> 1, smallYSize = ySize - 1;
        int color;
        int seed = rng.nextInt(), current = seed, paint = seed;
        int thrust = Coloring.RINSED[randomMainColor(seed ^ 0x44444) & 0xFF];
        byte[] grays = colorizer.grayscale();
        byte mainColor = colorizer.getReducer().paletteMapping[seed & 0x7FFF], // bottom 15 bits
                highlightColor = colorizer.brighten(colorizer.getReducer().paletteMapping[seed >>> 17]), // top 15 bits
                cockpitColor = colorizer.darken(colorizer.reduce((0x20 + determineBounded(seed ^ 0x11111, 0x60) << 24)
                        | (0xA0 + determineBounded(seed ^ 0x22222, 0x60) << 16)
                        | (0xC8 + determineBounded(seed ^ 0x33333, 0x38) << 8) | 0xFF)),
                thrustColor = colorizer.reduce(thrust),
                lightColor = (byte) (colorizer.brighten(colorizer.getReducer().paletteMapping[(seed ^ seed >>> 4 ^ seed >>> 13) & 0x7FFF]) | colorizer.getShadeBit() | colorizer.getWaveBit());
        thrustColor = (byte) (colorizer.brighten(thrustColor) | colorizer.getWaveBit() | colorizer.getShadeBit());
        for (int i = 0; i < grays.length; i++) {
            if(highlightColor == grays[i])
            {
                highlightColor = colorizer.getReducer().paletteMapping[determineInt(~seed) & 0x7FFF];
                break;
            }
        }
        final FastNoise noise = new FastNoise(seed ^ seed >>> 21 ^ seed << 6, 0x1.4p0f / xSize);
        int xx, yy, zz;
        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < halfY; y++) {
                for (int z = 0; z < zSize; z++) {
                    color = (ship[x][y][z] & 255);
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
                        current = hashAll(xx + (xx | zz) >> 3, (yy + (yy | zz)) / 3, zz, color, seed)
                                + (int) (noise.getSimplex(x * 0.5f, y * 0.75f, z * 0.666f) * 0x800000) + 0x800000;
                        paint = hashAll((xx + (xx | z)) / 7, (yy + (yy | z)) / 5, z, color, seed + 0x12345);
                        if (color < 8) {
                            // checks sorta-top 3 bits
                            if((current >>> 21 & 7) != 0)
                                nextShip[x][smallYSize - y][z] = nextShip[x][y][z] = cockpitColor;
                        } else {
                            nextShip[x][smallYSize - y][z] = nextShip[x][y][z] =
                                    // checks sorta-top 9 bits, different branch
                                    ((current >>> 15 & 0x1FF) < color * 6)
                                            ? 0
                                            // checks 6 bits of paint
                                            : ((paint & 0x3F) < 36) // is a random number from 0-63 less than 36? 
                                            ? grays[(int)((noise.getSimplex(x * 0.125f, y * 0.2f, z * 0.24f) * 0.4f + 0.599f) * (grays.length - 1))]
                                            : (noise.getSimplex(x * 0.04f, y * 0.07f, z * 0.09f) > 0.15f)
                                            ? highlightColor
                                            : mainColor;
                        }
                    }
                }
            }
        }
        paint ^= paint << 7 ^ paint >>> 23;
        current ^= current << 5 ^ current >>> 19;
        for (int y = 0; y < halfY; y++) {
            for (int z = 0; z < zSize; z++) {
                if(Noise.IntPointHash.hash64(z, y, paint) < 3)
                {
                    for (int x = xSize - 2; x >= 0; x--) {
                        if(nextShip[x][y][z] != 0 && nextShip[x][y][z] != cockpitColor)
                        {
                            nextShip[x+1][smallYSize - y][z] = nextShip[x+1][y][z] = lightColor;
                            break;
                        }
                    }
                }
                if(hash32(z * 3 >>> 2, y * 5 + (z >>> 1) >>> 3, current) < 15)
                {
                    for (int x = 1; x < xSize; x++) {
                        if(nextShip[x][y][z] != 0 && nextShip[x][y][z] != cockpitColor)
                        {
                            nextShip[x-1][smallYSize - y][z] = nextShip[x-1][y][z] = thrustColor;
                            break;
                        }
                    }
                }
            }
        }
        return nextShip;
    }

    /**
     * Uses some simplex noise from {@link FastNoise} to make paint patterns and shapes more "flowing" and less
     * haphazard in their placement. Still uses point hashes for a lot of its operations.
     * @return a 12x12x8 3D byte array representing a spaceship
     */
    public byte[][][] shipSmoothColorized()
    {
        return shipSmoothColorized(ship);
    }
    /**
     * Uses some simplex noise from {@link FastNoise} to make paint patterns and shapes more "flowing" and less
     * haphazard in their placement. Still uses point hashes for a lot of its operations.
     * @return a larger (40x40x30) 3D byte array representing a spaceship
     */
    public byte[][][] shipLargeSmoothColorized()
    {
        final byte[][][] next = new byte[60][60][60];
        Tools3D.translateCopyInto(Tools3D.largestPart(shipSmoothColorized(shipLarge)), next, 10, 10, 15);
        return next;
    }
    /**
     * Uses some simplex noise from {@link FastNoise} to make paint patterns and shapes more "flowing" and less
     * haphazard in their placement. Still uses point hashes for a lot of its operations.
     * @param ship one of the two ships loaded from resources here, probably, {@link #ship} and {@link #shipLarge}
     * @return 3D byte array representing a spaceship
     */
    private byte[][][] shipSmoothColorized(byte[][][] ship)
    {
        xSize = ship.length;
        ySize = ship[0].length;
        zSize = ship[0][0].length;
        byte[][][] nextShip = new byte[xSize][ySize][zSize];
        final int halfY = ySize >> 1, smallYSize = ySize - 1;
        int color;
        int seed = rng.nextInt(), current = seed;
        int thrust = Coloring.RINSED[randomMainColor(seed ^ 0x44444) & 0xFF];
        byte[] grays = colorizer.grayscale();
        byte mainColor = colorizer.getReducer().paletteMapping[seed & 0x7FFF], // bottom 15 bits
                //highlightColor = colorizer.brighten(colorizer.getReducer().paletteMapping[seed >>> 17]), // top 15 bits
                cockpitColor = colorizer.reduce((0x10 + determineBounded(seed ^ 0x11111, 0x30) << 24)
                        | (0x90 + determineBounded(seed ^ 0x22222, 0x40) << 16)
                        | (0xB0 + determineBounded(seed ^ 0x33333, 0x40) << 8) | 0xFF),
                thrustColor = colorizer.reduce(thrust);
//        byte lightColor = (byte) (colorizer.brighten(colorizer.getReducer().paletteMapping[(seed ^ seed >>> 4 ^ seed >>> 13) & 0x7FFF]) | colorizer.getShadeBit() | colorizer.getWaveBit());
        thrustColor = (byte) (colorizer.brighten(thrustColor) | colorizer.getWaveBit() | colorizer.getShadeBit());
//        for (int i = 0; i < grays.length; i++) {
//            if(highlightColor == grays[i])
//            {
//                highlightColor = colorizer.getReducer().paletteMapping[determineInt(~seed) & 0x7FFF];
//                break;
//            }
//        }
        final FastNoise noise = new FastNoise(seed ^ seed >>> 21 ^ seed << 6, 0x1.4p0f / xSize);
        int xx, yy;
        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < halfY; y++) {
                for (int z = 0; z < zSize; z++) {
                    color = (ship[x][y][z] & 255);
                    if (color != 0) {
                        // this 4-input-plus-state hash is really a slight modification on LightRNG.determine(), but
                        // it mixes the x, y, and z inputs more thoroughly than other techniques do, and we then use
                        // different sections of the random bits for different purposes. This helps reduce the possible
                        // issues from using rng.next(5) and rng.next(6) all over if the bits those use have a pattern.
                        // In the original model, all voxels of the same color will be hashed with similar behavior but
                        // any with different colors will get unrelated values.
                        xx = x + 1;
                        yy = y + 1;
                        current = //hashAll(xx + (xx | zz) >> 3, (yy + (yy | zz)) / 3, zz, color, seed)
                                + (int) (noise.getSimplex(x * 0.5f, y * 0.75f, z * 0.666f) * 0x800000f) + 0x800000;
                        if (color < 8) {
                            // checks sorta-top 4 bits
                            if((current >>> 21 & 15) != 0)
                                nextShip[x][smallYSize - y][z] = nextShip[x][y][z] = cockpitColor;
                        } else {
                            nextShip[x][smallYSize - y][z] = nextShip[x][y][z] =
                                    // checks sorta-top 9 bits, different branch
                                    ((current >>> 15 & 0x3FF) < color * 13)
                                            ? 0
                                            // checks 6 bits of paint
                                            : (merlin3D(x, y, z, seed) == 0) 
                                            ? grays[(int)((noise.getSimplex(x * 0.125f, y * 0.2f, z * 0.24f) * 0.4f + 0.599f) * (grays.length - 1))]
                                            : colorizer.colorize(mainColor, (int)(noise.getSimplex(x * 0.0625f, y * 0.1f, z * 0.14f) * 2.25f));
//                                            (noise.getSimplex(x * 0.04f, y * 0.07f, z * 0.09f) > 0.15f)
//                                            ? highlightColor
//                                            : mainColor;
                        }
                    }
                }
            }
        }
//        paint ^= paint << 7 ^ paint >>> 23;
        current ^= current << 5 ^ current >>> 19;
        for (int y = 0; y < halfY; y++) {
            for (int x = xSize - 1; x > 0; x--) {
                int antennaHash = Noise.IntPointHash.hash256(x, y, ~current);
                if (antennaHash < 9) {
                    for (int z = zSize - 2; z >= 0; z--) {
                        if (nextShip[x][y][z] != 0 && nextShip[x][y][z] != cockpitColor) {
                            byte antennaColor = grays[grays.length - 2 - (antennaHash % (grays.length-1))];
                            nextShip[x][smallYSize - y][z] = nextShip[x][y][z] =
                                    nextShip[x][smallYSize - y][z + 1] = nextShip[x][y][z + 1] = antennaColor;
                            if (antennaHash < 5 && z < zSize - 2) {
                                nextShip[x][smallYSize - y][z + 2] = nextShip[x][y][z + 2] = antennaColor;
                            }
                            break;
                        }
                    }
                }
            }             
            for (int z = 1; z < zSize; z++) {
                for (int x = 0; x < xSize; x++) {
                    if (hash32(z * 3 >>> 2, y * 5 + (z >>> 1) >>> 3, current) < 15) {
                        if (nextShip[x][y][z] != 0 && nextShip[x][y][z] != cockpitColor) {
                            nextShip[x - 1][smallYSize - y][z] = nextShip[x - 1][y][z] = thrustColor;
                            break;
                        }
                    }
                }
            }
        }
        return nextShip;
    }
    
    protected static int determine32(int state)
    {
        int z = (state *= 0xB79F5);
        z = (z ^ z >>> 15) * (z | 0xFFF0003D);
        z ^= z >>> 8;
        z = (z ^ z >>> 7) * (state >>> 12 | 1);
        return z ^ z >>> 14;

    }
    
    private static final int resolution = 2;
    private static int lorp(int start, int end, int a) {
        return ((1 << resolution) - a) * start + a * end >>> resolution;
    }

    public static int merlin3D(int x, int y, int z, int state) {
        int xb = (x >> resolution) + state, yb = (y >> resolution) - state, zb = (z >> resolution) + (0x9E3779B9 ^ state),
                xr = x & ~(-1 << resolution), yr = y & ~(-1 << resolution), zr = z & ~(-1 << resolution),
                x0 = determine32(xb), x1 = determine32(xb + 1),
                y0 = determine32(yb), y1 = determine32(yb + 1),
                z0 = determine32(zb), z1 = determine32(zb + 1),
                x0y0z0 = (x0 * y0 * z0 ^ x0 - y0 + (z0 - x0 << 16 | y0 - z0 >>> 16)) >>> resolution, x1y0z0 = (x1 * y0 * z0 ^ x1 - y0 + (z0 - x1 << 16 | y0 - z0 >>> 16)) >>> resolution,
                x0y1z0 = (x0 * y1 * z0 ^ x0 - y1 + (z0 - x0 << 16 | y1 - z0 >>> 16)) >>> resolution, x1y1z0 = (x1 * y1 * z0 ^ x1 - y1 + (z0 - x1 << 16 | y1 - z0 >>> 16)) >>> resolution,
                x0y0z1 = (x0 * y0 * z1 ^ x0 - y0 + (z1 - x0 << 16 | y0 - z1 >>> 16)) >>> resolution, x1y0z1 = (x1 * y0 * z1 ^ x1 - y0 + (z1 - x1 << 16 | y0 - z1 >>> 16)) >>> resolution,
                x0y1z1 = (x0 * y1 * z1 ^ x0 - y1 + (z1 - x0 << 16 | y1 - z1 >>> 16)) >>> resolution, x1y1z1 = (x1 * y1 * z1 ^ x1 - y1 + (z1 - x1 << 16 | y1 - z1 >>> 16)) >>> resolution;

        return lorp(lorp(lorp(x0y0z0, x1y0z0, xr), lorp(x0y1z0, x1y1z0, xr), yr),
                lorp(lorp(x0y0z1, x1y0z1, xr), lorp(x0y1z1, x1y1z1, xr), yr), zr)
                >>> -resolution - 1;
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
                    hashes[x][smallYSize - y][z] |= hashes[x][y][z] |= Noise.HastyPointHash.hashAll(x, y, z, seed) | 1L;
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
            int rx = (int)((DiverRNG.determineFloat(seed + 100L + i) + DiverRNG.determineFloat(seed - 100L + i)) * xSize * 0.5f),
                    rz = (int)(DiverRNG.determineFloat(seed + 1000L + i) * DiverRNG.determineFloat(seed - 1000L + i) * zSize);
            hashes[rx][halfY-1][rz] |= 0xFFFFFFFFFFFFL | hashes[rx][halfY-1][rz] << 16;
            hashes[rx][halfY-2][rz] |= 0xFFFFFFFFFFFFL | hashes[rx][halfY-2][rz] << 16;
            hashes[rx][halfY-3][rz] |= 0xFFFFFFFFFFFFL | hashes[rx][halfY-3][rz] << 16;
            hashes[rx][halfY-4][rz] |= 0xFFFFFFFFFFFFL | hashes[rx][halfY-4][rz] << 16;
        }
        for (int i = 0; i < 20; i++) {
            int rx = (int)((DiverRNG.determineFloat(seed + 100L + i) + DiverRNG.determineFloat(seed - 100L + i)) * xSize * 0.5f),
                    rz = (int)(DiverRNG.determineFloat(seed + 1000L + i) * DiverRNG.determineFloat(seed - 1000L + i) * zSize);
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
                ? DiverRNG.determineBounded(seed + 1L, 30) * 8 + DiverRNG.determineBounded(seed + 22L, 4) + 18
                : DiverRNG.determineBounded(seed + 1L, 18) * 6 + DiverRNG.determineBounded(seed + 22L, 3) + 22),
                highlightColor = (byte)((RINSED_PALETTE)
                        ? ((DiverRNG.determineBounded(seed + 333L, 30))) * 8 + DiverRNG.determineBounded(seed + 4444L, 4) + 17
                        : ((DiverRNG.determineBounded(seed + 333L, 18))) * 6 + DiverRNG.determineBounded(seed + 4444L, 3) + 21);
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
                    hashes[x][smallYSize - y][z] |= hashes[x][y][z] |= Noise.HastyPointHash.hashAll(x, y, z, seed) | 1L;
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
            int rx = (int)((DiverRNG.determineFloat(seed + 100L + i) + DiverRNG.determineFloat(seed - 100L + i)) * xSize * 0.5f),
                    rz = (int)(DiverRNG.determineFloat(seed + 1000L + i) * DiverRNG.determineFloat(seed - 1000L + i) * zSize);
            hashes[rx][halfY-1][rz] |= 0xFFFFFFFFFFFFL | hashes[rx][halfY-1][rz] << 16;
            hashes[rx][halfY-2][rz] |= 0xFFFFFFFFFFFFL | hashes[rx][halfY-2][rz] << 16;
            hashes[rx][halfY-3][rz] |= 0xFFFFFFFFFFFFL | hashes[rx][halfY-3][rz] << 16;
            hashes[rx][halfY-4][rz] |= 0xFFFFFFFFFFFFL | hashes[rx][halfY-4][rz] << 16;
        }
        for (int i = 0; i < 20; i++) {
            int rx = (int)((DiverRNG.determineFloat(seed + 100L + i) + DiverRNG.determineFloat(seed - 100L + i)) * xSize * 0.5f),
                    rz = (int)(DiverRNG.determineFloat(seed + 1000L + i) * DiverRNG.determineFloat(seed - 1000L + i) * zSize);
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
                ? DiverRNG.determineBounded(seed + 1L, 30) * 8 + DiverRNG.determineBounded(seed + 22L, 4) + 18
                : DiverRNG.determineBounded(seed + 1L, 18) * 6 + DiverRNG.determineBounded(seed + 22L, 3) + 22),
                highlightColor = (byte)((RINSED_PALETTE)
                        ? ((DiverRNG.determineBounded(seed + 333L, 30))) * 8 + DiverRNG.determineBounded(seed + 4444L, 4) + 17
                        : ((DiverRNG.determineBounded(seed + 333L, 18))) * 6 + DiverRNG.determineBounded(seed + 4444L, 3) + 21);
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
        return (byte)((DiverRNG.determineBounded(seed, 240) & 0xFB) + 18);
    }

    /**
     * Gets 5 colors from lightest to darkest, with the same hue chosen randomly from the RINSED palette.
     * @param seed a long seed that should be different every time this is called
     * @return a 5-element byte array, with the first item having the lightest color and the last having the darkest
     */
    public static byte[] randomColorRange(long seed)
    {
        byte idx = (byte) ((DiverRNG.determineBounded(seed, 30) << 3) + 17);
        return new byte[]{idx, (byte) (idx+1), (byte) (idx+2), (byte) (idx+3), (byte) (idx+4)};
    }
    /**
     * Gets a random color palette index, always using {@link Coloring#RINSED}. It will always be in the middle of the
     * color range, leaning toward lighter colors (but never the lightest color in a group of similar Rinsed colors).
     * @param seed a long seed that should be different every time this is called
     * @return a byte representing a color palette index, randomly chosen
     */
    public static byte randomMainColor(int seed) {
        return (byte)((determineBounded(seed, 29) << 3) + 25);
    }

    /**
     * Gets 5 colors from lightest to darkest, with the same hue chosen randomly from the RINSED palette.
     * @param seed a long seed that should be different every time this is called
     * @return a 5-element byte array, with the first item having the lightest color and the last having the darkest
     */
    public static byte[] randomColorRange(int seed)
    {
        byte idx = (byte) ((determineBounded(seed, 30) << 3) + 17);
        return new byte[]{idx, (byte) (idx+1), (byte) (idx+2), (byte) (idx+3), (byte) (idx+4)};
    }

    /**
     * Gets a range of fire colors given four RGBA8888 ints as targets for this to try to produce. Goes from
     * {@code early} (which could be bold orange, for the start of a fire) to {@code mid} (which could be light orange,
     * for the middle of a fire), to {@code bright} (which could be light yellow, for sparks) to {@code smoke} (which
     * could be gray or brown) to clear (which this always appends as the fifth item, so the fire fades out).
     * @param early the color for the start of a fire, such as for embers
     * @param mid the color for the middle of a fire, often lighter than {@code early}
     * @param bright the color for sparks, often very light
     * @param smoke the color for smoke near the end of a fire
     * @return a 5-item array of byte color indices for fire colors
     */
    public byte[] fireRange(int early, int mid, int bright, int smoke)
    {
        return new byte[] {
                (byte)(colorizer.reduce(early) | colorizer.getShadeBit()),
                (byte)(colorizer.reduce(mid) | colorizer.getShadeBit()),
                (byte)(colorizer.reduce(bright) | colorizer.getShadeBit()),
                (byte)(colorizer.reduce(smoke) | colorizer.getShadeBit()),
                0
        };
    }
    
    /**
     * Gets a range of fire colors, going from bold orange to light orange to light yellow to brown for smoke to clear.
     * @return a 5-item array of byte color indices for fire colors
     */
    public byte[] fireRange() {
        return fireRange(0xFB6B1DFF, 0xFF9E17FF, 0xFBFF86FF, 0x5C3A41FF);
    }
    public byte[] randomFireRange()
    {
        int idx = rng.nextSignedInt(30) * 8 + 16;
        return fireRange(Coloring.RINSED[idx + 4], Coloring.RINSED[idx + 2], Coloring.RINSED[idx], Coloring.RINSED[idx + 6]);
    }

    /**
     * Takes an array of arrays of RGBA8888 ints representing colors, such as
     * {@link warpwriter.view.color.Dimmer#AURORA_RAMP_VALUES}, and gets a random ramp from it to attempt to construct
     * a fire range.
     * @param rampValues a 2D array of RGBA8888 ints, where each interior array must have at least 4 elements and first
     *                   interior array is ignored (expected to be transparent)
     * @return a 5-item array of byte color indices for fire colors
     */
    public byte[] randomFireRange(int[][] rampValues)
    {
        int idx = rng.nextSignedInt(rampValues.length - 1) + 1;
        if(rampValues[idx][3] == 0)
            idx >>>= 1;
        return fireRange(rampValues[idx][2], rampValues[idx][1], rampValues[idx][0], rampValues[idx][3]);
    }
    public byte[][][][] animateExplosion(int frames, int xSize, int ySize, int zSize)
    {
        return animateExplosion(frames, xSize, ySize, zSize, fireRange());
    }
    public byte[][][][] animateExplosion(int frames, int xSize, int ySize, int zSize, byte[] fireColors)
    {
        final int sa = rng.nextInt(), sb = rng.nextInt();
        FastNoise noise = new FastNoise(sa ^ sb, 0x1.2p-3f, FastNoise.SIMPLEX_FRACTAL, 2);
        noise.setFractalType(FastNoise.FBM);
        final byte[][][][] boom = new byte[frames][xSize][ySize][zSize];
        int centerX = xSize >> 1, centerY = ySize >> 1;
        int expandLength = Math.round(frames * 0.2f);
        int riseLength = Math.round(frames * 0.3f);
        int smokeLength = frames - riseLength - expandLength;
        float maxRadius = Math.min(centerX, centerY);
        float startRadius = maxRadius * 0.375f;
        float currentRadius = startRadius;
        float rad2 = currentRadius * currentRadius * 0.875f;
        float w = 0f;
        for (int i = 0; i < expandLength && i < frames; i++, w += 0.125f) {
            currentRadius = Interpolation.pow2InInverse.apply(startRadius, maxRadius, (float) i / expandLength);
            rad2 = currentRadius * currentRadius * 0.825f;
            for (float x = -currentRadius; x <= currentRadius; x++) {
                for (float y = -currentRadius; y <= currentRadius; y++) {
                    if(x * x + y * y > rad2 + 7)
                        continue;
                    for (float z = 0; z < currentRadius; z++) {
                        float ns = noise.getSimplexFractal(x, y, z, w);
                        if(x * x + y * y + z * z <= rad2 + rng.next(4) - 16 * (ns + 1.5f) && ns * 16 + 12 < i) {
                                boom[i][Math.round(centerX + x)][Math.round(centerY + y)][Math.round(z)] = fireColors[minIntOf(7, 1 + expandLength - i) >> 1];
                        }
                    }
                }
            }
        }
        float startLift = zSize * 0.125f;
        float currentLift = startLift;
        startRadius = currentRadius;
        maxRadius = Math.min(centerX, centerY) * 1.1f - 1;
        for (int j = 0, i = expandLength; j < riseLength && i < frames; j++, i++, w += 0.125f) {
            currentRadius = MathUtils.lerp(startRadius, maxRadius, (float) j / riseLength);
            rad2 = currentRadius * currentRadius * 0.875f;
            currentLift = MathUtils.lerp(startLift, zSize * 0.4f, (float) j / riseLength);
            for (float x = -currentRadius; x <= currentRadius; x++) {
                for (float y = -currentRadius; y <= currentRadius; y++) {
                    for (float z = currentRadius * -0.875f; z < currentRadius && z + 0.5f + currentLift < zSize; z++) {
                        float ns = noise.getSimplexFractal(x, y, z, w);
                        if(z + currentLift >= 0 && x * x + y * y + z * z <= rad2 + rng.next(4) - 16 * (ns + 1.5f) && ns * 16 + 11 < i)
                        {
                            boom[i][Math.round(centerX + x)][Math.round(centerY + y)][Math.round(z + currentLift)] = fireColors[Math.round(NumberTools.formCurvedFloat(rng.nextInt()) * 1.6f + 1.5f + 0.1f * j)];
                        }
                    }
                }
            }
        }
        startLift = currentLift;
        //startRadius = currentRadius;
        for (int j = 0, i = expandLength + riseLength; i < frames; j++, i++, w += 0.125f) {
            currentLift = MathUtils.lerp(startLift, zSize * 0.7f, (float) j / smokeLength);
            currentRadius = maxRadius;//MathUtils.lerp(startRadius, maxRadius, (j + 1f) / smokeLength);
            rad2 = currentRadius * currentRadius * 0.875f;
            for (float x = -currentRadius; x <= currentRadius; x++) {
                for (float y = -currentRadius; y <= currentRadius; y++) {
                    for (float z = -currentRadius + 0.15f * j * (float) Math.sqrt(x * x + y * y); z + 0.5f + currentLift < zSize; z++) {
                        float ns = noise.getSimplexFractal(x, y, z, w);
                        if(z + currentLift >= 0 && x * x + y * y + z * z * 0.9f <= rad2 + rng.next(4) - 16 * (ns + 1.5f) && ns * 512 + 230 < 11 * smokeLength - i * 7)
                        {
                            boom[i][MathUtils.clamp(Math.round(centerX + x), 0, xSize-1)][MathUtils.clamp(Math.round(centerY + y), 0, ySize-1)][MathUtils.clamp(Math.round(z + currentLift), 0, zSize-1)] = fireColors[
                                    Math.min(4, Math.round(NumberTools.formCurvedFloat(rng.nextInt()) * 1.4f + 3.45f + 0.2f * (j - smokeLength)))
                                    //maxIntOf(4, 5 + j)
                                    ];
                        }
                    }
                }
            }
        }
        rng.setState(sa, sb);
        return boom;
    }

}
