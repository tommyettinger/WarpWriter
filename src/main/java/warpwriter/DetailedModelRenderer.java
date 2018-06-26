package warpwriter;

/**
 * Created by Tommy Ettinger on 6/25/2018.
 */
public class DetailedModelRenderer extends ModelRenderer {
    public int[] palette;
    public double[][] distances = {
            {
                0,0,0,0,0
                //    0.0, 0.11180339887498948, 0.125, 0.11180339887498948, 0.0,
                //0.0, 0.22360679774997896, 0.25, 0.223606797749978960, 0.0
            },
            {
                    0, 0.17677669529663687, 0.25, 0.17677669529663687, 0,
                    //0.11180339887498948, 0.17677669529663687, 0.25, 0.17677669529663687, 0.11180339887498948,
                //0.22360679774997896, 0.35355339059327373, 0.5, 0.35355339059327373, 0.22360679774997896
            },
            {
                    0, 0.25, 0.0, 0.25, 0,
                    //0.125, 0.25, 0.0, 0.25, 0.125,
                //0.25, 0.5, 0.0, 0.5, 0.25
            },
            {
                    0, 0.17677669529663687, 0.25, 0.17677669529663687, 0,
                    //0.11180339887498948, 0.17677669529663687, 0.25, 0.17677669529663687, 0.11180339887498948,
                //0.22360679774997896, 0.35355339059327373, 0.5, 0.35355339059327373, 0.22360679774997896
            },
            {
                    0,0,0,0,0
                    //    0.0, 0.11180339887498948, 0.125, 0.11180339887498948, 0.0,
                    //0.0, 0.22360679774997896, 0.25, 0.223606797749978960, 0.0
            }
    };
    public DetailedModelRenderer()
    {
        super();
        palette = Coloring.ALT_PALETTE;
    }
    public static int difference(final int color1, final int color2)
    {
        int rmean = ((color1 >>> 24) + (color2 >>> 24)) >> 1;
        int r = (color1 >>> 24) - (color2 >>> 24);
        int g = (color1 >>> 16 & 0xFF) - (color2 >>> 16 & 0xFF);
        int b = (color1 >>> 8 & 0xFF) - (color2 >>> 8 & 0xFF);
        return (((512+rmean)*r*r)>>8) + 4*g*g + (((767-rmean)*b*b)>>8);
    }

    public static int lerpIntColors(final int s, final int e, final double change) {
        final int
                rs = (s >>> 24 & 0xFF), gs = (s >>> 16) & 0xFF, bs = (s >>> 8 & 0xFF), as = (s & 0xFF),
                re = (e >>> 24 & 0xFF), ge = (e >>> 16) & 0xFF, be = (e >>> 8 & 0xFF);
        return ((int) (rs + change * (re - rs)) & 0xFF) << 24
                | ((int) (gs + change * (ge - gs)) & 0xFF) << 16
                | (((int) (bs + change * (be - bs)) & 0xFF) << 8)
                | as;
    }

    public static void lerpIntColors(final int[][] holder, final int x, final int y, final int e, final double change) {
        final int s = holder[x][y];
        //if((s & 0x80) == 0) return;
        final int
                rs = (s >>> 24 & 0xFF), gs = (s >>> 16) & 0xFF, bs = (s >>> 8 & 0xFF), as = (s & 0xFF),
                re = (e >>> 24 & 0xFF), ge = (e >>> 16) & 0xFF, be = (e >>> 8 & 0xFF);
        holder[x][y] =
                  ((int) (rs + change * (re - rs)) & 0xFF) << 24
                | ((int) (gs + change * (ge - gs)) & 0xFF) << 16
                | (((int) (bs + change * (be - bs)) & 0xFF) << 8)
                | (as);
    }
    
    @Override
    public int[][] easeSquares(int[][] original, int[][] out) {
        int xSize = original.length, ySize = original[0].length;
        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                original[x][y] = out[x][y] = palette[original[x][y]];
            }
        }
        for (int x = 2; x < xSize-2; x++) {
            for (int y = 2; y < ySize-2; y++) {
                for (int xx = x - 1, xi = 1; xi < 4; xx++, xi++) {
                    for (int yy = y - 1, yi = 1; yi < 4; yy++, yi++) {
                        double d = distances[xi][yi];
                        if(d > 0.0)
                        {
                            lerpIntColors(out, x,  y, original[xx][yy], d);
                        }
                    }
                }
            }
        }
        return out;
    }
}
