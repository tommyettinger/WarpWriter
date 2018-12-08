package warpwriter;

import squidpony.squidmath.FastNoise;
import squidpony.squidmath.GWTRNG;

/**
 * Created by Tommy Ettinger on 10/28/2018.
 */
public class WorldMaker {
    public GWTRNG rng;
    protected static final float terrainFreq = 1.45f, terrainRidgedFreq = 3.1f, heatFreq = 2.1f, moistureFreq = 2.125f, otherFreq = 3.375f;
    protected double minHeat0 = Double.POSITIVE_INFINITY, maxHeat0 = Double.NEGATIVE_INFINITY,
            minHeat1 = Double.POSITIVE_INFINITY, maxHeat1 = Double.NEGATIVE_INFINITY,
            minWet0 = Double.POSITIVE_INFINITY, maxWet0 = Double.NEGATIVE_INFINITY;

    public final FastNoise terrain, heat, moisture, heatRidged, moistureRidged, terrainLayered;
    public double landModifier = -1.0, coolingModifier = -1.0;
    
    public WorldMaker()
    {
        this(0x1337BEEFD00DL, 0.7);
    }
    public WorldMaker(long seed, double detailMultiplier)
    {
        rng = new GWTRNG(seed);
        terrain = new FastNoise(rng.nextInt(), terrainFreq * 0.6f, FastNoise.SIMPLEX_FRACTAL, (int) (0.5 + detailMultiplier * 10));
        terrain.setFractalType(FastNoise.RIDGED_MULTI);
        terrainLayered = new FastNoise(rng.nextInt(), terrainRidgedFreq * 2.75f, FastNoise.SIMPLEX_FRACTAL, (int) (1 + detailMultiplier * 6), 0.5f, 2f);
        heat = new FastNoise(rng.nextInt(), heatFreq, FastNoise.SIMPLEX_FRACTAL, (int) (0.5 + detailMultiplier * 3), 0.75f, 1f / 0.75f);
        moisture = new FastNoise(rng.nextInt(), moistureFreq, FastNoise.SIMPLEX_FRACTAL, (int) (0.5 + detailMultiplier * 4), 0.55f, 1f / 0.55f);
        heatRidged = new FastNoise(rng.nextInt(), otherFreq, FastNoise.SIMPLEX_FRACTAL, (int) (0.5 + detailMultiplier * 6));
        heatRidged.setFractalType(FastNoise.RIDGED_MULTI);
        moistureRidged = new FastNoise(rng.nextInt(), otherFreq, FastNoise.SIMPLEX_FRACTAL, (int) (0.5 + detailMultiplier * 6));
        moistureRidged.setFractalType(FastNoise.RIDGED_MULTI);

    }
    public static final double
            deepWaterLower = -1.0, deepWaterUpper = -0.7,        // 0
            mediumWaterLower = -0.7, mediumWaterUpper = -0.3,    // 1
            shallowWaterLower = -0.3, shallowWaterUpper = -0.1,  // 2
            coastalWaterLower = -0.1, coastalWaterUpper = 0.02,  // 3
            sandLower = 0.02, sandUpper = 0.12,                  // 4
            grassLower = 0.14, grassUpper = 0.35,                // 5
            forestLower = 0.35, forestUpper = 0.6,               // 6
            rockLower = 0.6, rockUpper = 0.8,                    // 7
            snowLower = 0.8, snowUpper = 1.0;                    // 8
    public static final double
            // heat        
            coldestValueLower = 0.0,   coldestValueUpper = 0.15, // 0
            colderValueLower = 0.15,   colderValueUpper = 0.31,  // 1
            coldValueLower = 0.31,     coldValueUpper = 0.5,     // 2
            warmValueLower = 0.5,      warmValueUpper = 0.69,    // 3
            warmerValueLower = 0.69,   warmerValueUpper = 0.85,  // 4
            warmestValueLower = 0.85,  warmestValueUpper = 1.0,  // 5
            // moisture
            driestValueLower = 0.0,    driestValueUpper  = 0.27, // 0
            drierValueLower = 0.27,    drierValueUpper   = 0.4,  // 1
            dryValueLower = 0.4,       dryValueUpper     = 0.6,  // 2
            wetValueLower = 0.6,       wetValueUpper     = 0.8,  // 3
            wetterValueLower = 0.8,    wetterValueUpper  = 0.9,  // 4
            wettestValueLower = 0.9,   wettestValueUpper = 1.0;  // 5

    public int codeHeight(final double high)
    {
        if(high < deepWaterUpper)
            return 0;
        if(high < mediumWaterUpper)
            return 1;
        if(high < shallowWaterUpper)
            return 2;
        if(high < coastalWaterUpper)
            return 3;
        if(high < sandUpper)
            return 4;
        if(high < grassUpper)
            return 5;
        if(high < forestUpper)
            return 6;
        if(high < rockUpper)
            return 7;
        return 8;
    }
    public static final byte[] biomeTable = {
            //COLDEST   //COLDER    //COLD      //HOT       //HOTTER    //HOTTEST
            (byte) 193, (byte) 194, (byte) 107, (byte)  97, (byte)  97, (byte)  97,             //DRYEST
            (byte) 193, (byte) 147, (byte) 107, (byte) 107, (byte)  97, (byte)  97,             //DRYER
            (byte) 193, (byte) 147, (byte) 155, (byte) 155, (byte) 122, (byte) 121,             //DRY
            (byte) 193, (byte) 147, (byte) 131, (byte) 131, (byte) 123, (byte) 122,             //WET
            (byte) 193, (byte) 147, (byte) 157, (byte) 133, (byte) 139, (byte) 123,             //WETTER
            (byte) 193, (byte) 157, (byte) 157, (byte) 133, (byte) 139, (byte) 132,             //WETTEST
            (byte)  51, (byte)  51, (byte)  98, (byte)  98, (byte)  98, (byte)  98,             //COASTS
            (byte) 193, (byte) 201, (byte) 201, (byte) 201, (byte) 201, (byte) 201,             //RIVERS
            (byte) 193, (byte) 201, (byte) 201, (byte) 201, (byte) 201, (byte) 201,             //LAKES
            (byte) 194, (byte) 203, (byte) 203, (byte) 203, (byte) 203, (byte) 203,             //OCEAN
            0,                                                                                  //SPACE
    };

    public byte[][][] makeWorld(int diameter, double landMod, double coolMod)
    {
        terrain.setSeed(rng.nextInt());
        terrainLayered.setSeed(rng.nextInt());
        heat.setSeed(rng.nextInt());
        moisture.setSeed(rng.nextInt());
        heatRidged.setSeed(rng.nextInt());
        moistureRidged.setSeed(rng.nextInt());
        int t;

        landModifier = (landMod <= 0) ? rng.nextDouble(0.2) + 0.91 : landMod;
        coolingModifier = (coolMod <= 0) ? rng.nextDouble(0.45) * (rng.nextDouble() - 0.5) + 1.1 : coolMod;
        
        double minHeight = Double.POSITIVE_INFINITY, maxHeight = Double.NEGATIVE_INFINITY,
                minHeat = Double.POSITIVE_INFINITY, maxHeat = Double.NEGATIVE_INFINITY,
                minWet = Double.POSITIVE_INFINITY, maxWet = Double.NEGATIVE_INFINITY;
        
        float radius = diameter * 0.5f, r2 = radius * radius, iRadius = 1f / radius,
                dist2, offX, offY, offZ;
        float sx, sy, sz;
        double hi, he, mo;
        byte[][][] world = new byte[diameter][diameter][diameter];
        double[][][] heightData = new double[diameter][diameter][diameter],
                heatData = new double[diameter][diameter][diameter],
                moistureData = new double[diameter][diameter][diameter];
        for (int x = 0; x < diameter; x++) {
            offX = x - radius;
            sx = (offX * iRadius);
            offX *= offX;
            for (int y = 0; y < diameter; y++) {
                offY = y - radius;
                sy = (offY * iRadius);
                offY *= offY;
                for (int z = 0; z < diameter; z++) {
                    offZ = z - radius;
                    sz = (offZ * iRadius);
                    offZ *= offZ;
                    dist2 = offX + offY + offZ;
                    // check for surface positions
                    if(dist2 <= r2)
                    {
                        heightData[x][y][z] = (hi = terrainLayered.getSimplexFractal(sx +
                                        terrain.getSimplexFractal(sx, sy, sz) * 0.5f,
                                sy, sz) + (float) landModifier - 1f);
                        heatData[x][y][z] = (he = heat.getSimplexFractal(sx, sy
                                        + heatRidged.getSimplexFractal(sx, sy, sz)
                                , sz));
                        moistureData[x][y][z] = (mo = moisture.getSimplexFractal(sx, sy, sz
                                + moistureRidged.getSimplexFractal(sx, sy, sz)));
                        
                        minHeight = Math.min(minHeight, hi);
                        maxHeight = Math.max(maxHeight, hi);

                        minHeat0 = Math.min(minHeat0, he);
                        maxHeat0 = Math.max(maxHeat0, he);

                        minWet0 = Math.min(minWet0, mo);
                        maxWet0 = Math.max(maxWet0, mo);
                    }
                    else{
                        heightData[x][y][z] = Double.POSITIVE_INFINITY;
                    }
                }
            }
        }
        double  heatDiff = 0.8 / (maxHeat0 - minHeat0),
                wetDiff = 1.0 / (maxWet0 - minWet0),
                hMod;
        double mn = Float.POSITIVE_INFINITY, mx = Float.NEGATIVE_INFINITY, temp;

        for (int z = 0; z < diameter; z++) {
            temp = Math.abs(z - radius) * iRadius;
            temp *= (2.4 - temp);
            temp = 2.2 - temp;
            for (int x = 0; x < diameter; x++) {
                for (int y = 0; y < diameter; y++) {
                    hi = heightData[x][y][z];
                    if (hi > 10000) {
                        continue;
                    } else
                        t = codeHeight(hi);
                    hMod = 1.0;
                    switch (t) {
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                            hi = 0.4;
                            hMod = 0.2;
                            break;
                        case 6:
                            hi = -0.1 * (hi - forestLower - 0.08);
                            break;
                        case 7:
                            hi *= -0.25;
                            break;
                        case 8:
                            hi *= -0.4;
                            break;
                        default:
                            hi *= 0.05;
                    }
                    heatData[x][y][z] = (hi = (((heatData[x][y][z] - minHeat0) * heatDiff * hMod) + hi + 0.6) * temp);
                    mn = Math.min(mn, (float) hi); //minHeat0
                    mx = Math.max(mx, (float) hi); //maxHeat0
                }
            }
        }         
        minHeat1 = mn;
        maxHeat1 = mx;
        heatDiff = coolingModifier / (maxHeat1 - minHeat1);

        for (int x = 0; x < diameter; x++) {
            for (int y = 0; y < diameter; y++) {
                for (int z = 0; z < diameter; z++) {
                    if (heightData[x][y][z] <= 10000) {
                        heatData[x][y][z] = (he = ((heatData[x][y][z] - minHeat1) * heatDiff));
                        moistureData[x][y][z] = (mo = (moistureData[x][y][z] - minWet0) * wetDiff);
                        minHeat = Math.min(minHeat, (float) he);
                        maxHeat = Math.max(maxHeat, (float) he);
                        minWet = Math.min(minWet, (float) mo);
                        maxWet = Math.max(maxWet, (float) mo);
                    }
                }
            }
        }
        for (int x = 0; x < diameter; x++) {
            for (int y = 0; y < diameter; y++) {
                for (int z = 0; z < diameter; z++) {
                    if ((hi = heightData[x][y][z]) <= 10000) {
                        he = heatData[x][y][z];
                        mo = moistureData[x][y][z];
                        int hc, mc;
                        if(hi < coastalWaterUpper) {
                            mc = 9;
                        }
                        else if (mo > wetterValueUpper) {
                            mc = 5;
                        } else if (mo > wetValueUpper) {
                            mc = 4;
                        } else if (mo > dryValueUpper) {
                            mc = 3;
                        } else if (mo > drierValueUpper) {
                            mc = 2;
                        } else if (mo > driestValueUpper) {
                            mc = 1;
                        } else {
                            mc = 0;
                        }

                        if (he > warmerValueUpper) {
                            hc = 5;
                        } else if (he > warmValueUpper) {
                            hc = 4;
                        } else if (he > coldValueUpper) {
                            hc = 3;
                        } else if (he > colderValueUpper) {
                            hc = 2;
                        } else if (he > coldestValueUpper) {
                            hc = 1;
                        } else {
                            hc = 0;
                        }

                        if(hi < coastalWaterUpper) 
                            // deeper water gets a darker color, some shallow water gets a lighter color
                            world[x][y][z] = (byte)(biomeTable[hc + 54] - 0.01 - hi * 3); // 54 == 9 * 6, 9 is used for Ocean groups
                        else {
                            // produces minor color variations in splotches, seemingly at random;
                            // x,y,z don't correspond to the same arguments in getSimplexFractal() intentionally
                            float adj = Math.abs(heat.getSimplexFractal(y * 0.1f, z * 0.1f, x * 0.1f) * 3f);
                            world[x][y][z] = (byte)(biomeTable[(hi >= sandLower && hi < sandUpper) ? hc + 36 : hc + mc * 6] + adj);
                        }
                    }
                }
            }
        }

        return world;
    }
}
