package warpwriter;

import com.badlogic.gdx.graphics.Pixmap;
import squidpony.ArrayTools;
import warpwriter.model.CompassDirection;
import warpwriter.model.IModel;

import java.util.Arrays;
/**
 * Created by Tommy Ettinger on 11/4/2017.
 */
public class ModelRenderer {
    public final boolean RINSED_PALETTE;
    public final int EYE_DARK;
    public final int EYE_LIGHT;
    private transient int[] tempPalette;
    public boolean hardOutline = false;
    public boolean easing;
    public ModelRenderer()
    {
        this(true, true);
    }
    
    public ModelRenderer(boolean easing, boolean rinsedPalette)
    {
        this.easing = easing;
        tempPalette = new int[256];
        RINSED_PALETTE = rinsedPalette;
        EYE_DARK = RINSED_PALETTE ? 22 : 30;
        EYE_LIGHT = 17;
    }
    public static int clamp (int value, int min, int max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    public int clampDown(int color) {
        if (RINSED_PALETTE) {
            if(color < 16)
                return color;
            color -= 16;
            int m = color & 7;
            return (color - m) + clamp(m+1, 1, 7) + 16;// + (m > 5 ? 16 + m : 21);
        } else {
            final int off = color & 128;
            color -= off;
            if (color < 16) return color + off;
            if (color < 32) return clamp(color + 1, 17, 31) + off;
            color -= 32;
            int m = (color % 6);
            return (color - m) + clamp(m + 1, 1, 5) + 32 + off;
        }
    }

    /**
     * Gets the internal int code used for a given CompassDirection enum, which must not be null. Extremely unlikely to be
     * necessary in external code, but maybe if you need to use one of the VariableConverter arrays, it could be handy.
     * @param d a CompassDirection to look up; must not be null
     * @return an internal-use int code for the given CompassDirection, from 0 to 3 inclusive
     */
    protected static int directionCode(CompassDirection d)
    {
        switch(d)
        {
            case SOUTH:
            case SOUTH_EAST:
                return 0; // 0
            case EAST:
            case NORTH_EAST:
                return 3; // 3
            case NORTH:
            case NORTH_WEST:
                return 2; // 2
            default:
                return 1; // 1
        }
    }

    public int[][] renderOrtho(IModel voxels, CompassDirection dir)
    {
        final int xs = voxels.xSize(), ys = voxels.ySize(), zs = voxels.zSize(), direction = directionCode(dir);
        VariableConverter con = directionsOrthoV[direction];
        int[][] working = makeRenderArray(xs, ys, zs, 4, 5, 1);
        int width = working.length, height = working[0].length;
        int[][] depths = new int[width][height], render;
        int aa, cc, aStep, cStep, aMax, cMax, px, py,
                depthStart = xs - 1,
                depthStep = -1;

        boolean flip = true;
        int current;
        switch (direction)
        {
            case 0:
                aa = 0;
                aStep = 1;
                aMax = ys;
                cc = 0;
                cStep = 1;
                cMax = xs;
                break;
            case 1:
                aa = xs - 1;
                aStep = -1;
                aMax = -1;
                cc = 0;
                cStep = 1;
                cMax = ys;
                flip = false;
                break;
            case 2:
                aa = ys - 1;
                aStep = -1;
                aMax = -1;
                cc = xs - 1;
                cStep = -1;
                cMax = -1;
                break;
            default:
                aa = 0;
                aStep = 1;
                aMax = xs;
                cc = ys - 1;
                cStep = -1;
                cMax = -1;
                flip = false;
                break;
        }
        if(flip) {
            for (int a = aa; a != aMax; a += aStep) {
                for (int b = 0; b < zs; b++) {
                    for (int c = cc, d = depthStart; c != cMax; c += cStep, d += depthStep) {
                        px = con.voxelToPixelX(c+1, a+1, b, xs, ys, zs);
                        py = con.voxelToPixelY(c+1, a+1, b, xs, ys, zs);
                        current = voxels.at(c, a, b) & 255;
                        if (current != 0 && !((current >= 8 && current < 16) || (!RINSED_PALETTE && current >= 136 && current < 144))) {
                            if (current <= 2) {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 0; sy < 5; sy++) {
                                        working[px+sx][py+sy] = current;
                                    }
                                }
                            } else if(current == 3) {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 0; sy < 3; sy++) {
                                        if (working[px + sx][py + sy] == 0) {
                                            working[px + sx][py + sy] = 3;
                                            depths[px + sx][py + sy] = 256 + b * 8 - d * 4 + sy;
                                        }
                                    }
                                    for (int sy = 3; sy < 5; sy++) {
                                        if (working[px + sx][py + sy] == 0) {
                                            working[px + sx][py + sy] = 3;
                                            depths[px + sx][py + sy] = 255 + b * 8 - d * 4 + sy;
                                        }
                                    }
                                }
                            } else if(current == 4) {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 0; sy < 3; sy++) {
                                        working[px+sx][py+sy] = EYE_DARK;
                                        depths[px+sx][py+sy] = 256 + b * 8 - d * 4 + sy; // used c
                                    }
                                    for (int sy = 3; sy < 5; sy++) {
                                        working[px+sx][py+sy] = EYE_DARK;
                                        depths[px+sx][py+sy] = 255 + b * 8 - d * 4 + sy; // used c
                                    }
                                }
                                working[px][py] = EYE_LIGHT;
                                working[px+1][py] = EYE_LIGHT;
                                working[px][py+1] = EYE_LIGHT;
                                working[px+1][py+1] = EYE_LIGHT;
//                                working[px][py+2] = 14;
//                                working[px+1][py+2] = 14;
                            }
                            else
                            {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 0; sy < 3; sy++) {
                                        working[px+sx][py+sy] = current - 1;
                                        depths[px+sx][py+sy] = 256 + b * 8 - d * 4 + sy;
                                    }
                                    for (int sy = 3; sy < 5; sy++) {
                                        working[px+sx][py+sy] = current;
                                        depths[px+sx][py+sy] = 255 + b * 8 - d * 4 + sy;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        else  {
            for (int a = aa; a != aMax; a += aStep) {
                for (int b = 0; b < zs; b++) {
                    for (int c = cc, d = depthStart; c != cMax; c += cStep, d += depthStep) {
                        px = con.voxelToPixelX(a+1, c+1, b, xs, ys, zs);
                        py = con.voxelToPixelY(a+1, c+1, b, xs, ys, zs);
                        current = voxels.at(a, c, b) & 255;
                        if(current != 0 && !((current >= 8 && current < 16) || (!RINSED_PALETTE && current >= 136 && current < 144)))
                        {
                            if (current <= 2) {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 0; sy < 5; sy++) {
                                        working[px+sx][py+sy] = current;
                                    }
                                }
                            } else if(current == 3) {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 0; sy < 3; sy++) {
                                        if (working[px + sx][py + sy] == 0) {
                                            working[px + sx][py + sy] = 3;
                                            depths[px + sx][py + sy] = 256 + b * 8 - d * 4 + sy;
                                        }
                                    }
                                    for (int sy = 3; sy < 5; sy++) {
                                        if (working[px + sx][py + sy] == 0) {
                                            working[px + sx][py + sy] = 3;
                                            depths[px + sx][py + sy] = 255 + b * 8 - d * 4 + sy;
                                        }
                                    }
                                }
                            } else if(current == 4) {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 0; sy < 3; sy++) {
                                        working[px+sx][py+sy] = EYE_DARK;
                                        depths[px+sx][py+sy] = 256 + b * 8 - d * 4 + sy; // used a
                                    }
                                    for (int sy = 3; sy < 5; sy++) {
                                        working[px+sx][py+sy] = EYE_DARK;
                                        depths[px+sx][py+sy] = 255 + b * 8 - d * 4 + sy; // used a
                                    }
                                }
                                working[px][py] = EYE_LIGHT;
                                working[px+1][py] = EYE_LIGHT;
                                working[px][py+1] = EYE_LIGHT;
                                working[px+1][py+1] = EYE_LIGHT;
                            }
                            else
                            {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 0; sy < 3; sy++) {
                                        working[px+sx][py+sy] = current - 1;
                                        depths[px+sx][py+sy] = 256 + b * 8 - d * 4 + sy;
                                    }
                                    for (int sy = 3; sy < 5; sy++) {
                                        working[px+sx][py+sy] = current;
                                        depths[px+sx][py+sy] = 255 + b * 8 - d * 4 + sy;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        int d, w;
        render = ArrayTools.copy(working);
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                if((w = clampDown(working[x][y])) > 2) {
                    if(w == 3) w = 2;
                    int o = hardOutline ? 2 : w;
                    d = depths[x][y];
                    if      (working[x - 1][y] == 0 && working[x][y - 1] == 0) { render[x - 1][y] = o; render[x][y - 1] = o; render[x][y] = w; }
                    else if (working[x + 1][y] == 0 && working[x][y - 1] == 0) { render[x + 1][y] = o; render[x][y - 1] = o; render[x][y] = w; }
                    else if (working[x - 1][y] == 0 && working[x][y + 1] == 0) { render[x - 1][y] = o; render[x][y + 1] = o; render[x][y] = w; }
                    else if (working[x + 1][y] == 0 && working[x][y + 1] == 0) { render[x + 1][y] = o; render[x][y + 1] = o; render[x][y] = w; }
                    else {
                        if (hardOutline && working[x - 1][y] == 0) { render[x - 1][y] = 2; } else if (working[x - 1][y] == 0 || depths[x - 1][y] < d - 9 ) { render[x - 1][y] = w; }
                        if (hardOutline && working[x + 1][y] == 0) { render[x + 1][y] = 2; } else if (working[x + 1][y] == 0 || depths[x + 1][y] < d - 9 ) { render[x + 1][y] = w; }
                        if (hardOutline && working[x][y - 1] == 0) { render[x][y - 1] = 2; } else if (working[x][y - 1] == 0 || depths[x][y - 1] < d - 9 ) { render[x][y - 1] = w; }
                        if (hardOutline && working[x][y + 1] == 0) { render[x][y + 1] = 2; } else if (working[x][y + 1] == 0 || depths[x][y + 1] < d - 9 ) { render[x][y + 1] = w; }
                    }
                }
            }
        }
        return easeSquares(render, working);
//        return render;
//        return easeSquares(render);
    }
    /**
     * Renders the given 3D voxel byte array, which should be no larger than 12x12x8, to a 52x64 2D int array storing
     * color indices, using the given CompassDirection enum to rotate the model's facing.
     * @param voxels a 3D byte array with each byte storing color information for a voxel.
     * @param dir a CompassDirection enum storing 90-degree-increment direction info
     * @return a 2D int array storing the pixel indices for the rendered model
     */
    public int[][] renderIso(IModel voxels, CompassDirection dir) {
        final int xs = voxels.xSize(), ys = voxels.ySize(), zs = voxels.zSize(), direction = directionCode(dir);
        VariableConverter con = directionsIsoV[direction];
        int[][] working = makeRenderArray(xs, ys, zs, 4, 5, 1);
        int width = working.length, height = working[0].length;
        int[][] depths = new int[width][height], render;
        int px, py;
        int current;
        int d, w;
        // for 0, v> , DOWN_RIGHT, x low to high, y high to low
        // for 1, <v , DOWN_LEFT,  x low to high, y low to high
        // for 2, <^ , UP_RIGHT,   x high to low, y low to high
        // for 3, ^> , UP_LEFT,    x high to low, x high to low
        final int gray = direction ^ direction >>> 1;
        final int cStart = (gray & 2) == 0 ? 0 : xs - 1, cEnd = (gray & 2) == 0 ? xs : -1, cChange = (gray & 2) == 0 ? 1 : -1;
        final int aStart = (gray & 1) == 0 ? ys - 1 : 0, aEnd = (gray & 1) == 0 ? -1 : ys, aChange = (gray & 1) == 0 ? -1 : 1;
        for (int b = 0; b < zs; b++) {
            for (int c = cStart; c != cEnd; c += cChange) {
                for (int a = aStart; a != aEnd; a += aChange) {
                    px = con.voxelToPixelX(c + 1, a + 1, b, xs, ys, zs);
                    py = con.voxelToPixelY(c + 1, a + 1, b, xs, ys, zs);
                    if(px < 0 || py < 0) continue;
                    current = voxels.at(c, a, b) & 255;
                    if (current != 0 && !((current >= 8 && current < 16) || (!RINSED_PALETTE && current >= 136 && current < 144))) {
                        d = 3 * (b + (c * cChange - a)) + 256;
                        if (current <= 2) {
                            working[px][py] = current;
                        } else if (current == 3) {
                            for (int ix = 0; ix < 4; ix++) {
                                for (int iy = 0; iy < 4; iy++) {
                                    if (working[px + ix][py + iy] == 0)
                                    {
                                        working[px + ix][py + iy] = 3;
                                        depths[px + ix][py + iy] = d + ((ix ^ ix >>> 1) & 1); // adds 1 only in center of a voxel
                                    }
                                }
                            }
                        } else if (current == 4) {
                            for (int ix = 0; ix < 4; ix++) {
                                for (int iy = 0; iy < 4; iy++) {
                                    if (ix < 2 && iy < 2)
                                    {
                                        working[px + ix][py + iy] = EYE_LIGHT;
                                        depths[px + ix][py + iy] = d + ((ix ^ ix >>> 1) & 1); // adds 1 only in center of a voxel
                                    }
                                    else
                                    {
                                        working[px + ix][py + iy] = EYE_DARK;
                                        depths[px + ix][py + iy] = d + ((ix ^ ix >>> 1) & 1); // adds 1 only in center of a voxel
                                    }
                                }
                            }
                        } else {
                            for (int ix = 0; ix < 4; ix++) {
                                for (int iy = 0; iy < 2; iy++) {
                                    working[px + ix][py + iy] = current - 1;
                                    depths[px + ix][py + iy] = d + ((ix ^ ix >>> 1) & 1); // adds 1 only in center of a voxel
                                }
                            }
                            for (int ix = 0; ix < 2; ix++) {
                                for (int iy = 2; iy < 4; iy++) {
                                    working[px + ix][py + iy] = current;
                                    depths[px + ix][py + iy] = d + ((ix ^ ix >>> 1) & 1); // adds 1 only in center of a voxel
                                }
                            }
                            for (int ix = 2; ix < 4; ix++) {
                                for (int iy = 2; iy < 4; iy++) {
                                    working[px + ix][py + iy] = current + 1;
                                    depths[px + ix][py + iy] = d + ((ix ^ ix >>> 1) & 1); // adds 1 only in center of a voxel
                                }
                            }
                        }
                    }
                }
            }
        }
        render = ArrayTools.copy(working);
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                if((w = clampDown(working[x][y])) > 2) {
                    if(w == 3) w = 2;
                    int o = hardOutline ? 2 : w;
                    d = depths[x][y];
                    if      (working[x - 1][y] == 0 && working[x][y - 1] == 0) { render[x - 1][y] = o; render[x][y - 1] = o; render[x][y] = w; }
                    else if (working[x + 1][y] == 0 && working[x][y - 1] == 0) { render[x + 1][y] = o; render[x][y - 1] = o; render[x][y] = w; }
                    else if (working[x - 1][y] == 0 && working[x][y + 1] == 0) { render[x - 1][y] = o; render[x][y + 1] = o; render[x][y] = w; }
                    else if (working[x + 1][y] == 0 && working[x][y + 1] == 0) { render[x + 1][y] = o; render[x][y + 1] = o; render[x][y] = w; }
                    else {
                        if (hardOutline && working[x - 1][y] == 0) { render[x - 1][y] = 2; } else if (working[x - 1][y] == 0 || depths[x - 1][y] < d - 9) { render[x - 1][y] = w; }
                        if (hardOutline && working[x + 1][y] == 0) { render[x + 1][y] = 2; } else if (working[x + 1][y] == 0 || depths[x + 1][y] < d - 9) { render[x + 1][y] = w; }
                        if (hardOutline && working[x][y - 1] == 0) { render[x][y - 1] = 2; } else if (working[x][y - 1] == 0 || depths[x][y - 1] < d - 9) { render[x][y - 1] = w; }
                        if (hardOutline && working[x][y + 1] == 0) { render[x][y + 1] = 2; } else if (working[x][y + 1] == 0 || depths[x][y + 1] < d - 9) { render[x][y + 1] = w; }
                    }
                }
            }
        }
        return easeSquares(render, working);
    }

    public int[][] renderOrthoBelow(IModel voxels, CompassDirection dir)
    {
        final int xs = voxels.xSize(), ys = voxels.ySize(), zs = voxels.zSize(), direction = directionCode(dir);
        VariableConverter con = directionsOrthoV[direction];
        int[][] working = makeRenderArray(xs, ys, zs, 4, 5, 1);
        int width = working.length, height = working[0].length;
        int[][] depths = new int[width][height], render;
        int aa, cc, aStep, cStep, aMax, cMax, px, py,
                depthStart = xs - 1,
                depthStep = -1;
        boolean flip = true;
        int current;
        switch (direction)
        {
            case 0:
                aa = 0;
                aStep = 1;
                aMax = ys;
                cc = 0;
                cStep = 1;
                cMax = xs;
                break;
            case 1:
                aa = xs - 1;
                aStep = -1;
                aMax = -1;
                cc = 0;
                cStep = 1;
                cMax = ys;
                flip = false;
                break;
            case 2:
                aa = ys - 1;
                aStep = -1;
                aMax = -1;
                cc = xs - 1;
                cStep = -1;
                cMax = -1;
                break;
            default:
                aa = 0;
                aStep = 1;
                aMax = xs;
                cc = ys - 1;
                cStep = -1;
                cMax = -1;
                flip = false;
                break;
        }
        if(flip) {
            for (int b = zs - 1; b >= 0; b--) {
                for (int a = aa; a != aMax; a += aStep) {
                    for (int c = cc, d = depthStart; c != cMax; c += cStep, d += depthStep) {
                        px = con.voxelToPixelX(c + 1, a + 1, b, xs, ys, zs);
                        py = con.voxelToPixelY(c + 1, a + 1, b, xs, ys, zs);
                        current = voxels.at(xs - 1 - c, a, b) & 255;
                        if (current != 0 && !((current >= 8 && current < 16) || (!RINSED_PALETTE && current >= 136 && current < 144))) {
                            if (current <= 2) {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 0; sy < 5; sy++) {
                                        working[px+sx][py+sy] = current;
                                    }
                                }
                            } else if(current == 3) {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 0; sy < 2; sy++) {
                                        if (working[px + sx][py + sy] == 0) {
                                            working[px + sx][py + sy] = 3;
                                            depths[px + sx][py + sy] = 256 + b * 8 - d * 4 + sy;
                                        }
                                    }
                                    for (int sy = 2; sy < 5; sy++) {
                                        if (working[px + sx][py + sy] == 0) {
                                            working[px + sx][py + sy] = 3;
                                            depths[px + sx][py + sy] = 255 + b * 8 - d * 4 + sy;
                                        }
                                    }
                                }
                            } else if(current == 4) {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 0; sy < 2; sy++) {
                                        working[px+sx][py+sy] = EYE_DARK;
                                        depths[px+sx][py+sy] = 256 + b * 8 - d * 4 + sy;
                                    }
                                    for (int sy = 2; sy < 5; sy++) {
                                        working[px+sx][py+sy] = EYE_DARK;
                                        depths[px+sx][py+sy] = 255 + b * 8 - d * 4 + sy;
                                    }
                                }
                                working[px][py] = EYE_LIGHT;
                                working[px+1][py] = EYE_LIGHT;
                                working[px][py+1] = EYE_LIGHT;
                                working[px+1][py+1] = EYE_LIGHT;
                            }
                            else
                            {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 3; sy < 5; sy++) {
                                        working[px+sx][py+sy] = current + 1;
                                        depths[px+sx][py+sy] = 256 + b * 8 - d * 4 + sy;
                                    }
                                    for (int sy = 0; sy < 3; sy++) {
                                        working[px+sx][py+sy] = current;
                                        depths[px+sx][py+sy] = 255 + b * 8 - d * 4 + sy;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        else  {
            for (int b = zs - 1; b >= 0; b--) {
                for (int a = aa; a != aMax; a += aStep) {
                    for (int c = cc, d = depthStart; c != cMax; c += cStep, d += depthStep) {
                        px = con.voxelToPixelX(a + 1, c + 1, b, xs, ys, zs);
                        py = con.voxelToPixelY(a + 1, c + 1, b, xs, ys, zs);
                        current = voxels.at(a, ys - 1 - c, b) & 255;
                        if(current != 0 && !((current >= 8 && current < 16) || (!RINSED_PALETTE && current >= 136 && current < 144)))
                        {
                            if (current <= 2) {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 0; sy < 5; sy++) {
                                        working[px+sx][py+sy] = current;
                                    }
                                }
                            } else if(current == 3) {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 0; sy < 2; sy++) {
                                        if (working[px + sx][py + sy] == 0) {
                                            working[px + sx][py + sy] = 3;
                                            depths[px + sx][py + sy] = 256 + b * 8 - d * 4 + sy;
                                        }
                                    }
                                    for (int sy = 2; sy < 5; sy++) {
                                        if (working[px + sx][py + sy] == 0) {
                                            working[px + sx][py + sy] = 3;
                                            depths[px + sx][py + sy] = 255 + b * 8 - d * 4 + sy;
                                        }
                                    }
                                }
                            } else if(current == 4) {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 0; sy < 2; sy++) {
                                        working[px+sx][py+sy] = EYE_DARK;
                                        depths[px+sx][py+sy] = 256 + b * 8 - d * 4 + sy;
                                    }
                                    for (int sy = 2; sy < 5; sy++) {
                                        working[px+sx][py+sy] = EYE_DARK;
                                        depths[px+sx][py+sy] = 255 + b * 8 - d * 4 + sy;
                                    }
                                }
                                working[px][py] = EYE_LIGHT;
                                working[px+1][py] = EYE_LIGHT;
                                working[px][py+1] = EYE_LIGHT;
                                working[px+1][py+1] = EYE_LIGHT;
                            }
                            else
                            {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 3; sy < 5; sy++) {
                                        working[px+sx][py+sy] = current + 1;
                                        depths[px+sx][py+sy] = 256 + b * 8 - d * 4 + sy;
                                    }
                                    for (int sy = 0; sy < 3; sy++) {
                                        working[px+sx][py+sy] = current;
                                        depths[px+sx][py+sy] = 255 + b * 8 - d * 4 + sy;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        int d, w, o = hardOutline ? 2 : 0;
        render = ArrayTools.copy(working);
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                if((w = clampDown(working[x][y])) > 3) {
                    d = depths[x][y];
                    if      (working[x - 1][y] == 0 && working[x][y - 1] == 0) { render[x - 1][y] = o; render[x][y - 1] = o; render[x][y] = w; }
                    else if (working[x + 1][y] == 0 && working[x][y - 1] == 0) { render[x + 1][y] = o; render[x][y - 1] = o; render[x][y] = w; }
                    else if (working[x - 1][y] == 0 && working[x][y + 1] == 0) { render[x - 1][y] = o; render[x][y + 1] = o; render[x][y] = w; }
                    else if (working[x + 1][y] == 0 && working[x][y + 1] == 0) { render[x + 1][y] = o; render[x][y + 1] = o; render[x][y] = w; }
                    else {
                        if (hardOutline && working[x - 1][y] == 0) { render[x - 1][y] = 2; } else if (working[x - 1][y] == 0 || depths[x - 1][y] < d - 9 ) { render[x - 1][y] = w; }
                        if (hardOutline && working[x + 1][y] == 0) { render[x + 1][y] = 2; } else if (working[x + 1][y] == 0 || depths[x + 1][y] < d - 9 ) { render[x + 1][y] = w; }
                        if (hardOutline && working[x][y - 1] == 0) { render[x][y - 1] = 2; } else if (working[x][y - 1] == 0 || depths[x][y - 1] < d - 9 ) { render[x][y - 1] = w; }
                        if (hardOutline && working[x][y + 1] == 0) { render[x][y + 1] = 2; } else if (working[x][y + 1] == 0 || depths[x][y + 1] < d - 9 ) { render[x][y + 1] = w; }
                    }
                }
            }
        }
        return easeSquares(render, working);
    }

    /**
     * Renders the given 3D voxel byte array, which should be no larger than 12x12x8, to a 52x64 2D int array storing
     * color indices, using the given direction to rotate the model's facing (from 0 to 3).
     * @param voxels a 3D byte array with each byte storing color information for a voxel.
     * @param dir a CompassDirection enum storing 90-degree-increment direction info
     * @return a 2D int array storing the pixel indices for the rendered model
     */
    public int[][] renderIsoBelow(IModel voxels, CompassDirection dir) {
        final int xs = voxels.xSize(), ys = voxels.ySize(), zs = voxels.zSize(), direction = directionCode(dir);
        VariableConverter con = directionsIsoV[direction];
        int[][] working = makeRenderArray(xs, ys, zs, 4, 5, 1);
        int width = working.length, height = working[0].length;
        int[][] depths = new int[width][height], render;
        int px, py;
        int current;
        int d, w;
        final int gray = direction ^ direction >>> 1;
        final int cStart = (gray & 2) == 0 ? 0 : xs - 1, cEnd = (gray & 2) == 0 ? xs : -1, cChange = (gray & 2) == 0 ? 1 : -1;
        final int aStart = (gray & 1) == 0 ? ys - 1 : 0, aEnd = (gray & 1) == 0 ? -1 : ys, aChange = (gray & 1) == 0 ? -1 : 1;
        for (int b = zs - 1; b >= 0; b--) {
            for (int c = cStart; c != cEnd; c += cChange) {
                for (int a = aStart; a != aEnd; a += aChange) {
                    px = con.voxelToPixelX(c + 1, a + 1, b, xs, ys, zs);
                    py = con.voxelToPixelY(xs - c, ys - a, b, xs, ys, zs);
                    if(px < 0 || py < 0) continue;
                    current = voxels.at(c, a, b) & 255;
                    if (current != 0 && !((current >= 8 && current < 16) || (!RINSED_PALETTE && current >= 136 && current < 144))) {
                        d = 3 * (b + (c * cChange - a)) + 256;
                        if (current <= 2) {
                            working[px][py] = current;
                        } else if (current == 3) {
                            for (int ix = 0; ix < 4; ix++) {
                                for (int iy = 0; iy < 4; iy++) {
                                    if (working[px + ix][py + iy] == 0)
                                    {
                                        working[px + ix][py + iy] = 3;
                                        depths[px + ix][py + iy] = d + ((ix ^ ix >>> 1) & 1); // adds 1 only in center of a voxel
                                    }
                                }
                            }
                        } else if (current == 4) {
                            for (int ix = 0; ix < 4; ix++) {
                                for (int iy = 0; iy < 4; iy++) {
                                    if (ix < 2 && iy < 2)
                                    {
                                        working[px + ix][py + iy] = EYE_LIGHT;
                                        depths[px + ix][py + iy] = d + ((ix ^ ix >>> 1) & 1); // adds 1 only in center of a voxel
                                    }
                                    else
                                    {
                                        working[px + ix][py + iy] = EYE_DARK;
                                        depths[px + ix][py + iy] = d + ((ix ^ ix >>> 1) & 1); // adds 1 only in center of a voxel
                                    }
                                }
                            }
                        } else {
                            for (int ix = 0; ix < 4; ix++) {
                                for (int iy = 2; iy < 4; iy++) {
                                    working[px + ix][py + iy] = current;
                                    depths[px + ix][py + iy] = d + ((ix ^ ix >>> 1) & 1); // adds 1 only in center of a voxel
                                }
                            }
                            for (int ix = 0; ix < 2; ix++) {
                                for (int iy = 0; iy < 2; iy++) {
                                    working[px + ix][py + iy] = current - 1;
                                    depths[px + ix][py + iy] = d + ((ix ^ ix >>> 1) & 1); // adds 1 only in center of a voxel
                                }
                            }
                            for (int ix = 2; ix < 4; ix++) {
                                for (int iy = 0; iy < 2; iy++) {
                                    working[px + ix][py + iy] = current + 1;
                                    depths[px + ix][py + iy] = d + ((ix ^ ix >>> 1) & 1); // adds 1 only in center of a voxel
                                }
                            }
                        }
                    }
                }
            }
        }

        render = ArrayTools.copy(working);

        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                if((w = clampDown(working[x][y])) > 2) {
                    if(w == 3) w = 2;
                    int o = hardOutline ? 2 : w;
                    d = depths[x][y];
                    if      (working[x - 1][y] == 0 && working[x][y - 1] == 0) { render[x - 1][y] = o; render[x][y - 1] = o; render[x][y] = w; }
                    else if (working[x + 1][y] == 0 && working[x][y - 1] == 0) { render[x + 1][y] = o; render[x][y - 1] = o; render[x][y] = w; }
                    else if (working[x - 1][y] == 0 && working[x][y + 1] == 0) { render[x - 1][y] = o; render[x][y + 1] = o; render[x][y] = w; }
                    else if (working[x + 1][y] == 0 && working[x][y + 1] == 0) { render[x + 1][y] = o; render[x][y + 1] = o; render[x][y] = w; }
                    else {
                        if (hardOutline && working[x - 1][y] == 0) { render[x - 1][y] = 2; } else if (working[x - 1][y] == 0 || depths[x - 1][y] < d - 9) { render[x - 1][y] = w; }
                        if (hardOutline && working[x + 1][y] == 0) { render[x + 1][y] = 2; } else if (working[x + 1][y] == 0 || depths[x + 1][y] < d - 9) { render[x + 1][y] = w; }
                        if (hardOutline && working[x][y - 1] == 0) { render[x][y - 1] = 2; } else if (working[x][y - 1] == 0 || depths[x][y - 1] < d - 9) { render[x][y - 1] = w; }
                        if (hardOutline && working[x][y + 1] == 0) { render[x][y + 1] = 2; } else if (working[x][y + 1] == 0 || depths[x][y + 1] < d - 9) { render[x][y + 1] = w; }
                    }
                }
            }
        }

        return easeSquares(render, working);
    }

    public int[][] renderOrthoSide(IModel voxels, CompassDirection dir)
    {
        final int xs = voxels.xSize(), ys = voxels.ySize(), zs = voxels.zSize(), direction = directionCode(dir);
        VariableConverter con = directionsOrthoSideV[direction];
        int[][] working = makeRenderArray(xs, ys, zs, 4, 5, 1);
        int width = working.length, height = working[0].length;
        int[][] depths = new int[width][height], render;
        int aa, cc, aStep, cStep, aMax, cMax, px, py;
        boolean flip = true;
        int current;
        switch (direction)
        {
            case 0:
                aa = 0;
                aStep = 1;
                aMax = ys;
                cc = 0;
                cStep = 1;
                cMax = xs;
                break;
            case 1:
                aa = xs - 1;
                aStep = -1;
                aMax = -1;
                cc = 0;
                cStep = 1;
                cMax = ys;
                flip = false;
                break;
            case 2:
                aa = ys - 1;
                aStep = -1;
                aMax = -1;
                cc = xs - 1;
                cStep = -1;
                cMax = -1;
                break;
            default:
                aa = 0;
                aStep = 1;
                aMax = xs;
                cc = ys - 1;
                cStep = -1;
                cMax = -1;
                flip = false;
                break;
        }
        if(flip) {
            for (int c = cc; c != cMax; c += cStep) {
                for (int a = aa; a != aMax; a += aStep) {
                    for (int b = 0; b < zs; b++) {
                        px = con.voxelToPixelX(c + 1, a + 1, b, xs, ys, zs);
                        py = con.voxelToPixelY(c + 1, a + 1, b, xs, ys, zs);
                        current = voxels.at(c, a, b) & 255;
                        if (current != 0 && !((current >= 8 && current < 16) || (!RINSED_PALETTE && current >= 136 && current < 144))) {
                            if (current <= 2) {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 0; sy < 4; sy++) {
                                        working[px+sx][py+sy] = current;
                                    }
                                }
                            } else if(current == 3) {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 0; sy < 4; sy++) {
                                        if (working[px + sx][py + sy] == 0) {
                                            working[px + sx][py + sy] = 3;
                                            depths[px + sx][py + sy] = 256 + c * 2;
                                        }
                                    }
                                }
                            } else if(current == 4) {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 1; sy < 4; sy++) {
                                        working[px+sx][py+sy] = EYE_DARK;
                                        depths[px+sx][py+sy] = 256 + c * 2;
                                    }
                                    working[px+sx][py] = EYE_DARK;
                                    depths[px+sx][py] = 256 + c * 2;

                                }
                                working[px][py] = EYE_LIGHT;
                                working[px+1][py] = EYE_LIGHT;
                                working[px][py+1] = EYE_LIGHT;
                                working[px+1][py+1] = EYE_LIGHT;
                            }
                            else
                            {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 1; sy < 4; sy++) {
                                        working[px+sx][py+sy] = current;
                                        depths[px+sx][py+sy] = 256 + c * 2;
                                    }
                                    working[px+sx][py] = current - 1;
                                    depths[px+sx][py] = 256 + c * 2;
                                }
                            }
                        }
                    }
                }
            }
        }
        else  {
            for (int a = aa; a != aMax; a += aStep) {
                for (int b = 0; b < zs; b++) {
                    for (int c = cc; c != cMax; c += cStep) {
                        px = con.voxelToPixelX(a + 1, c + 1, b, xs, ys, zs);
                        py = con.voxelToPixelY(a + 1, c + 1, b, xs, ys, zs);
                        current = voxels.at(a, c, b) & 255;
                        if(current != 0 && !((current >= 8 && current < 16) || (!RINSED_PALETTE && current >= 136 && current < 144)))
                        {
                            if (current <= 2) {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 0; sy < 4; sy++) {
                                        working[px+sx][py+sy] = current;
                                    }
                                }
                            } else if(current == 3) {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 0; sy < 4; sy++) {
                                        if (working[px + sx][py + sy] == 0) {
                                            working[px + sx][py + sy] = 3;
                                            depths[px + sx][py + sy] = 256 + c * 2;
                                        }
                                    }
                                }
                            } else if(current == 4) {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 1; sy < 4; sy++) {
                                        working[px+sx][py+sy] = EYE_DARK;
                                        depths[px+sx][py+sy] = 256 + c * 2;
                                    }
                                    working[px+sx][py] = EYE_DARK;
                                    depths[px+sx][py] = 256 + c * 2;

                                }
                                working[px][py] = EYE_LIGHT;
                                working[px+1][py] = EYE_LIGHT;
                                working[px][py+1] = EYE_LIGHT;
                                working[px+1][py+1] = EYE_LIGHT;
                            }
                            else
                            {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 1; sy < 4; sy++) {
                                        working[px+sx][py+sy] = current;
                                        depths[px+sx][py+sy] = 256 + c * 2;
                                    }
                                    working[px+sx][py] = current - 1;
                                    depths[px+sx][py] = 256 + c * 2;
                                }
                            }
                        }
                    }
                }
            }
        }
        int d, w;
        render = ArrayTools.copy(working);
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                if((w = clampDown(working[x][y])) > 2) {
                    if(w == 3) w = 2;
                    int o = hardOutline ? 2 : w;
                    d = depths[x][y];
                    if      (working[x - 1][y] == 0 && working[x][y - 1] == 0) { render[x - 1][y] = o; render[x][y - 1] = o; render[x][y] = w; }
                    else if (working[x + 1][y] == 0 && working[x][y - 1] == 0) { render[x + 1][y] = o; render[x][y - 1] = o; render[x][y] = w; }
                    else if (working[x - 1][y] == 0 && working[x][y + 1] == 0) { render[x - 1][y] = o; render[x][y + 1] = o; render[x][y] = w; }
                    else if (working[x + 1][y] == 0 && working[x][y + 1] == 0) { render[x + 1][y] = o; render[x][y + 1] = o; render[x][y] = w; }
                    else {
                        if (hardOutline && working[x - 1][y] == 0) { render[x - 1][y] = 2; } else if (working[x - 1][y] == 0 || depths[x - 1][y] < d - 2) { render[x - 1][y] = w; }
                        if (hardOutline && working[x + 1][y] == 0) { render[x + 1][y] = 2; } else if (working[x + 1][y] == 0 || depths[x + 1][y] < d - 2) { render[x + 1][y] = w; }
                        if (hardOutline && working[x][y - 1] == 0) { render[x][y - 1] = 2; } else if (working[x][y - 1] == 0 || depths[x][y - 1] < d - 2) { render[x][y - 1] = w; }
                        if (hardOutline && working[x][y + 1] == 0) { render[x][y + 1] = 2; } else if (working[x][y + 1] == 0 || depths[x][y + 1] < d - 2) { render[x][y + 1] = w; }
                    }
                }
            }
        }
        return easeSquares(render, working);
    }
    public int[][] renderIsoSide(IModel voxels, CompassDirection dir) {
        final int xs = voxels.xSize(), ys = voxels.ySize(), zs = voxels.zSize(), direction = directionCode(dir);
        VariableConverter con = directionsIsoSideV[direction];
        int[][] working = makeRenderArray(xs, ys, zs, 4, 5, 1);
        int width = working.length, height = working[0].length;
        int[][] depths = new int[width][height], render;
        int px, py;
        int current;
        int d, w;
        final int gray = direction ^ direction >>> 1;
        final int cStart = (gray & 2) == 0 ? 0 : xs - 1, cEnd = (gray & 2) == 0 ? xs : -1, cChange = (gray & 2) == 0 ? 1 : -1;
        final int aStart = (gray & 1) == 0 ? ys - 1 : 0, aEnd = (gray & 1) == 0 ? -1 : ys, aChange = (gray & 1) == 0 ? -1 : 1;
        //final int aStart = ys - 1, aEnd = -1, aChange = -1;
        for (int b = 0; b < zs; b++) {
            for (int c = cStart; c != cEnd; c += cChange) {
                for (int a = aStart; a != aEnd; a += aChange) {
                    px = con.voxelToPixelX(c + 1, a + 1, b, xs, ys, zs);
                    py = con.voxelToPixelY(c + 1, a + 1, b, xs, ys, zs);
                    if(px < 0 || py < 0) continue;
                    current = voxels.at(c, a, b) & 255;
                    if (current != 0 && !((current >= 8 && current < 16) || (!RINSED_PALETTE && current >= 136 && current < 144))) {
                        d = 3 * (c * cChange + a * aChange) + 256;
                        if (current <= 2) {
                            working[px][py] = current;
                        } else if (current == 3) {
                            for (int ix = 0; ix < 4; ix++) {
                                for (int iy = 0; iy < 4; iy++) {
                                    if (working[px + ix][py + iy] == 0)
                                    {
                                        working[px + ix][py + iy] = 3;
                                        depths[px + ix][py + iy] = d + (ix & ix >>> 1); // adds 1 only on the right edge of a voxel
                                    }
                                }
                            }
                        } else if (current == 4) {
                            for (int ix = 0; ix < 4; ix++) {
                                for (int iy = 0; iy < 4; iy++) {
                                    if (ix < 2 && iy < 2)
                                    {
                                        working[px + ix][py + iy] = EYE_LIGHT;
                                        depths[px + ix][py + iy] = d + (ix & ix >>> 1); // adds 1 only on the right edge of a voxel
                                    }
                                    else
                                    {
                                        working[px + ix][py + iy] = EYE_DARK;
                                        depths[px + ix][py + iy] = d + (ix & ix >>> 1); // adds 1 only on the right edge of a voxel
                                    }
                                }
                            }
                        } else {
                            for (int ix = 0; ix < 4; ix++) {
                                working[px + ix][py] = current - 1;
                                depths[px + ix][py] = d + (ix & ix >>> 1); // adds 1 only on the right edge of a voxel
                            }
                            for (int ix = 0; ix < 2; ix++) {
                                for (int iy = 1; iy < 4; iy++) {
                                    working[px + ix][py + iy] = current;
                                    depths[px + ix][py + iy] = d + ix; // adds 1 only in center of voxel
                                }
                            }
                            for (int ix = 2; ix < 4; ix++) {
                                for (int iy = 1; iy < 4; iy++) {
                                    working[px + ix][py + iy] = current + 1;
                                    depths[px + ix][py + iy] = d - ix + 3; // adds 1 only in center of voxel
                                }
                            }
                        }
                    }
                }
            }
        }
        render = ArrayTools.copy(working);
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                if((w = clampDown(working[x][y])) > 2) {
                    if(w == 3) w = 2;
                    int o = hardOutline ? 2 : w;
                    d = depths[x][y];
                    if      (working[x - 1][y] == 0 && working[x][y - 1] == 0) { render[x - 1][y] = o; render[x][y - 1] = o; render[x][y] = w; }
                    else if (working[x + 1][y] == 0 && working[x][y - 1] == 0) { render[x + 1][y] = o; render[x][y - 1] = o; render[x][y] = w; }
                    else if (working[x - 1][y] == 0 && working[x][y + 1] == 0) { render[x - 1][y] = o; render[x][y + 1] = o; render[x][y] = w; }
                    else if (working[x + 1][y] == 0 && working[x][y + 1] == 0) { render[x + 1][y] = o; render[x][y + 1] = o; render[x][y] = w; }
                    else {
                        if (hardOutline && working[x - 1][y] == 0) { render[x - 1][y] = 2; } else if (working[x - 1][y] == 0 || depths[x - 1][y] < d - 5) { render[x - 1][y] = w; }
                        if (hardOutline && working[x + 1][y] == 0) { render[x + 1][y] = 2; } else if (working[x + 1][y] == 0 || depths[x + 1][y] < d - 5) { render[x + 1][y] = w; }
                        if (hardOutline && working[x][y - 1] == 0) { render[x][y - 1] = 2; } else if (working[x][y - 1] == 0 || depths[x][y - 1] < d - 5) { render[x][y - 1] = w; }
                        if (hardOutline && working[x][y + 1] == 0) { render[x][y + 1] = 2; } else if (working[x][y + 1] == 0 || depths[x][y + 1] < d - 5) { render[x][y + 1] = w; }
                    }
                }
            }
        }
        return easeSquares(render, working);
    }


    public int[][] renderOrtho(byte[][][] voxels, int direction)
    {
        final int xs = voxels.length, ys = voxels[0].length, zs = voxels[0][0].length;
        VariableConverter con = directionsOrthoV[direction &= 3];
        int[][] working = makeRenderArray(xs, ys, zs, 4, 5, 1);
        int width = working.length, height = working[0].length;
        int[][] depths = new int[width][height], render;
        int aa, cc, aStep, cStep, aMax, cMax, px, py, 
                depthStart = xs - 1,
                depthStep = -1;

        boolean flip = true;
        int current;
        switch (direction)
        {
            case 0:
                aa = 0;
                aStep = 1;
                aMax = ys;
                cc = 0;
                cStep = 1;
                cMax = xs;
                break;
            case 1:
                aa = xs - 1;
                aStep = -1;
                aMax = -1;
                cc = 0;
                cStep = 1;
                cMax = ys;
                flip = false;
                break;
            case 2:
                aa = ys - 1;
                aStep = -1;
                aMax = -1;
                cc = xs - 1;
                cStep = -1;
                cMax = -1;
                break;
            default:
                aa = 0;
                aStep = 1;
                aMax = xs;
                cc = ys - 1;
                cStep = -1;
                cMax = -1;
                flip = false;
                break;
        }
        if(flip) {
            for (int a = aa; a != aMax; a += aStep) {
                for (int b = 0; b < zs; b++) {
                    for (int c = cc, d = depthStart; c != cMax; c += cStep, d += depthStep) {
                        px = con.voxelToPixelX(c+1, a+1, b, xs, ys, zs);
                        py = con.voxelToPixelY(c+1, a+1, b, xs, ys, zs);
                        current = voxels[c][a][b] & 255;
                        if (current != 0 && !((current >= 8 && current < 16) || (!RINSED_PALETTE && current >= 136 && current < 144))) {
                            if (current <= 2) {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 0; sy < 5; sy++) {
                                        working[px+sx][py+sy] = current;
                                    }
                                }
                            } else if(current == 3) {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 0; sy < 5; sy++) {
                                        if(working[px+sx][py+sy] == 0) working[px+sx][py+sy] = current;
                                    }
                                }
                            } else if(current == 4) {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 0; sy < 3; sy++) {
                                        working[px+sx][py+sy] = EYE_DARK;
                                        depths[px+sx][py+sy] = 256 + b * 7 - d * 4 + sy; // used c
                                    }
                                    for (int sy = 3; sy < 5; sy++) {
                                        working[px+sx][py+sy] = EYE_DARK;
                                        depths[px+sx][py+sy] = 256 + b * 7 - d * 4 + sy; // used c
                                    }
                                }
                                working[px][py] = EYE_LIGHT;
                                working[px+1][py] = EYE_LIGHT;
                                working[px][py+1] = EYE_LIGHT;
                                working[px+1][py+1] = EYE_LIGHT;
//                                working[px][py+2] = 14;
//                                working[px+1][py+2] = 14;
                            }
                            else
                            {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 0; sy < 3; sy++) {
                                        working[px+sx][py+sy] = current - 1;
                                        depths[px+sx][py+sy] = 256 + b * 7 - d * 4 + sy;
                                    }
                                    for (int sy = 3; sy < 5; sy++) {
                                        working[px+sx][py+sy] = current;
                                        depths[px+sx][py+sy] = 256 + b * 7 - d * 4 + sy;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        else  {
            for (int a = aa; a != aMax; a += aStep) {
                for (int b = 0; b < zs; b++) {
                    for (int c = cc, d = depthStart; c != cMax; c += cStep, d += depthStep) {
                        px = con.voxelToPixelX(a+1, c+1, b, xs, ys, zs);
                        py = con.voxelToPixelY(a+1, c+1, b, xs, ys, zs);
                        current = voxels[a][c][b] & 255;
                        if(current != 0 && !((current >= 8 && current < 16) || (!RINSED_PALETTE && current >= 136 && current < 144)))
                        {
                            if (current <= 2) {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 0; sy < 5; sy++) {
                                        working[px+sx][py+sy] = current;
                                    }
                                }
                            } else if(current == 3) {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 0; sy < 5; sy++) {
                                        if(working[px+sx][py+sy] == 0) working[px+sx][py+sy] = current;
                                    }
                                }
                            } else if(current == 4) {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 0; sy < 3; sy++) {
                                        working[px+sx][py+sy] = EYE_DARK;
                                        depths[px+sx][py+sy] = 256 + b * 7 - d * 4 + sy; // used a
                                    }
                                    for (int sy = 3; sy < 5; sy++) {
                                        working[px+sx][py+sy] = EYE_DARK;
                                        depths[px+sx][py+sy] = 256 + b * 7 - d * 4 + sy; // used a
                                    }
                                }
                                working[px][py] = EYE_LIGHT;
                                working[px+1][py] = EYE_LIGHT;
                                working[px][py+1] = EYE_LIGHT;
                                working[px+1][py+1] = EYE_LIGHT;
                            }
                            else
                            {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 0; sy < 3; sy++) {
                                        working[px+sx][py+sy] = current - 1;
                                        depths[px+sx][py+sy] = 256 + b * 7 - d * 4 + sy;
                                    }
                                    for (int sy = 3; sy < 5; sy++) {
                                        working[px+sx][py+sy] = current;
                                        depths[px+sx][py+sy] = 256 + b * 7 - d * 4 + sy;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        int d, w;
        render = ArrayTools.copy(working);
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                if((w = clampDown(working[x][y])) > 3) {
                    int o = hardOutline ? 2 : w;
                    d = depths[x][y];
                    if      (working[x - 1][y] == 0 && working[x][y - 1] == 0) { render[x - 1][y] = o; render[x][y - 1] = o; render[x][y] = w; }
                    else if (working[x + 1][y] == 0 && working[x][y - 1] == 0) { render[x + 1][y] = o; render[x][y - 1] = o; render[x][y] = w; }
                    else if (working[x - 1][y] == 0 && working[x][y + 1] == 0) { render[x - 1][y] = o; render[x][y + 1] = o; render[x][y] = w; }
                    else if (working[x + 1][y] == 0 && working[x][y + 1] == 0) { render[x + 1][y] = o; render[x][y + 1] = o; render[x][y] = w; }
                    else {
                        if (hardOutline && working[x - 1][y] == 0) { render[x - 1][y] = 2; } else if (working[x - 1][y] == 0 || depths[x - 1][y] < d - 9 ) { render[x - 1][y] = w; }
                        if (hardOutline && working[x + 1][y] == 0) { render[x + 1][y] = 2; } else if (working[x + 1][y] == 0 || depths[x + 1][y] < d - 9 ) { render[x + 1][y] = w; }
                        if (hardOutline && working[x][y - 1] == 0) { render[x][y - 1] = 2; } else if (working[x][y - 1] == 0 || depths[x][y - 1] < d - 10) { render[x][y - 1] = w; }
                        if (hardOutline && working[x][y + 1] == 0) { render[x][y + 1] = 2; } else if (working[x][y + 1] == 0 || depths[x][y + 1] < d - 10) { render[x][y + 1] = w; }
                    }
                }
            }
        }
        return easeSquares(render, working);
//        return render;
//        return easeSquares(render);
    }
    /**
     * Renders the given 3D voxel byte array, which should be no larger than 12x12x8, to a 52x64 2D int array storing
     * color indices, using the given direction to rotate the model's facing (from 0 to 3).
     * @param voxels a 3D byte array with each byte storing color information for a voxel.
     * @param direction a 90-degree-increment counter-clockwise direction, from 0 to 3.
     * @return a 2D int array storing the pixel indices for the rendered model
     */
    public int[][] renderIso(byte[][][] voxels, int direction) {
        final int xs = voxels.length, ys = voxels[0].length, zs = voxels[0][0].length;
        VariableConverter con = directionsIsoV[direction &= 3];
        int[][] working = makeRenderArray(xs, ys, zs, 4, 5, 1);
        int width = working.length, height = working[0].length;
        int[][] depths = new int[width][height], render;
        int px, py;
        int current;
        int d, w;
        // for 0, v> , x low to high, y high to low
        // for 1, <v , x low to high, y low to high
        // for 2, <^ , x high to low, y low to high
        // for 3, ^> , x high to low, x high to low
        final int gray = direction ^ direction >>> 1;
        final int cStart = (gray & 2) == 0 ? 0 : xs - 1, cEnd = (gray & 2) == 0 ? xs : -1, cChange = (gray & 2) == 0 ? 1 : -1;
        final int aStart = (gray & 1) == 0 ? ys - 1 : 0, aEnd = (gray & 1) == 0 ? -1 : ys, aChange = (gray & 1) == 0 ? -1 : 1;
        for (int b = 0; b < zs; b++) {
            for (int c = cStart; c != cEnd; c += cChange) {
                for (int a = aStart; a != aEnd; a += aChange) {
                    px = con.voxelToPixelX(c + 1, a + 1, b, xs, ys, zs);
                    py = con.voxelToPixelY(c + 1, a + 1, b, xs, ys, zs);
                    if(px < 0 || py < 0) continue;
                    current = voxels[c][a][b] & 255;
                    if (current != 0 && !((current >= 8 && current < 16) || (!RINSED_PALETTE && current >= 136 && current < 144))) {
                        d = 3 * (b + (c * cChange - a)) + 256;
                        if (current <= 2) {
                            working[px][py] = current;
                        } else if (current == 3) {
                            for (int ix = 0; ix < 4; ix++) {
                                for (int iy = 0; iy < 4; iy++) {
                                    if (working[px + ix][py + iy] == 0)
                                    {
                                        working[px + ix][py + iy] = 3;
                                        depths[px + ix][py + iy] = d + ((ix ^ ix >>> 1) & 1); // adds 1 only in center of a voxel
                                    }
                                }
                            }
                        } else if (current == 4) {
                            for (int ix = 0; ix < 4; ix++) {
                                for (int iy = 0; iy < 4; iy++) {
                                    if (ix < 2 && iy < 2)
                                    {
                                        working[px + ix][py + iy] = EYE_LIGHT;
                                        depths[px + ix][py + iy] = d + ((ix ^ ix >>> 1) & 1); // adds 1 only in center of a voxel
                                    }
                                    else
                                    {
                                        working[px + ix][py + iy] = EYE_DARK;
                                        depths[px + ix][py + iy] = d + ((ix ^ ix >>> 1) & 1); // adds 1 only in center of a voxel
                                    }
                                }
                            }
                        } else {
                            for (int ix = 0; ix < 4; ix++) {
                                for (int iy = 0; iy < 2; iy++) {
                                    working[px + ix][py + iy] = current - 1;
                                    depths[px + ix][py + iy] = d + ((ix ^ ix >>> 1) & 1); // adds 1 only in center of a voxel
                                }
                            }
                            for (int ix = 0; ix < 2; ix++) {
                                for (int iy = 2; iy < 4; iy++) {
                                    working[px + ix][py + iy] = current;
                                    depths[px + ix][py + iy] = d + ((ix ^ ix >>> 1) & 1); // adds 1 only in center of a voxel
                                }
                            }
                            for (int ix = 2; ix < 4; ix++) {
                                for (int iy = 2; iy < 4; iy++) {
                                    working[px + ix][py + iy] = current + 1;
                                    depths[px + ix][py + iy] = d + ((ix ^ ix >>> 1) & 1); // adds 1 only in center of a voxel
                                }
                            }
                        }
                    }
                }
            }
        }
        render = ArrayTools.copy(working);
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                if((w = clampDown(working[x][y])) > 3) {
                    int o = hardOutline ? 2 : w;
                    d = depths[x][y];
                    if      (working[x - 1][y] == 0 && working[x][y - 1] == 0) { render[x - 1][y] = o; render[x][y - 1] = o; render[x][y] = w; }
                    else if (working[x + 1][y] == 0 && working[x][y - 1] == 0) { render[x + 1][y] = o; render[x][y - 1] = o; render[x][y] = w; }
                    else if (working[x - 1][y] == 0 && working[x][y + 1] == 0) { render[x - 1][y] = o; render[x][y + 1] = o; render[x][y] = w; }
                    else if (working[x + 1][y] == 0 && working[x][y + 1] == 0) { render[x + 1][y] = o; render[x][y + 1] = o; render[x][y] = w; }
                    else {
                        if (hardOutline && working[x - 1][y] == 0) { render[x - 1][y] = 2; } else if (working[x - 1][y] == 0 || depths[x - 1][y] < d - 9) { render[x - 1][y] = w; }
                        if (hardOutline && working[x + 1][y] == 0) { render[x + 1][y] = 2; } else if (working[x + 1][y] == 0 || depths[x + 1][y] < d - 9) { render[x + 1][y] = w; }
                        if (hardOutline && working[x][y - 1] == 0) { render[x][y - 1] = 2; } else if (working[x][y - 1] == 0 || depths[x][y - 1] < d - 9) { render[x][y - 1] = w; }
                        if (hardOutline && working[x][y + 1] == 0) { render[x][y + 1] = 2; } else if (working[x][y + 1] == 0 || depths[x][y + 1] < d - 9) { render[x][y + 1] = w; }
                    }
                }
            }
        }
        return easeSquares(render, working);
    }

    public int[][] renderOrthoBelow(byte[][][] voxels, int direction)
    {
        final int xs = voxels.length, ys = voxels[0].length, zs = voxels[0][0].length;
        VariableConverter con = directionsOrthoV[direction &= 3];
        int[][] working = makeRenderArray(xs, ys, zs, 4, 5, 1);
        int width = working.length, height = working[0].length;
        int[][] depths = new int[width][height], render;
        int aa, cc, aStep, cStep, aMax, cMax, px, py,
                depthStart = xs - 1,
                depthStep = -1;
        boolean flip = true;
        int current;
        switch (direction)
        {
            case 0:
                aa = 0;
                aStep = 1;
                aMax = ys;
                cc = 0;
                cStep = 1;
                cMax = xs;
                break;
            case 1:
                aa = xs - 1;
                aStep = -1;
                aMax = -1;
                cc = 0;
                cStep = 1;
                cMax = ys;
                flip = false;
                break;
            case 2:
                aa = ys - 1;
                aStep = -1;
                aMax = -1;
                cc = xs - 1;
                cStep = -1;
                cMax = -1;
                break;
            default:
                aa = 0;
                aStep = 1;
                aMax = xs;
                cc = ys - 1;
                cStep = -1;
                cMax = -1;
                flip = false;
                break;
        }
        if(flip) {
            for (int b = zs - 1; b >= 0; b--) {
                for (int a = aa; a != aMax; a += aStep) {
                    for (int c = cc, d = depthStart; c != cMax; c += cStep, d += depthStep) {
                        px = con.voxelToPixelX(c + 1, a + 1, b, xs, ys, zs);
                        py = con.voxelToPixelY(c + 1, a + 1, b, xs, ys, zs);
                        current = voxels[xs - 1 - c][a][b] & 255;
                        if (current != 0 && !((current >= 8 && current < 16) || (!RINSED_PALETTE && current >= 136 && current < 144))) {
                            if (current <= 2) {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 0; sy < 5; sy++) {
                                        working[px+sx][py+sy] = current;
                                    }
                                }
                            } else if(current == 3) {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 0; sy < 5; sy++) {
                                        if(working[px+sx][py+sy] == 0) working[px+sx][py+sy] = current;
                                    }
                                }
                            } else if(current == 4) {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 0; sy < 2; sy++) {
                                        working[px+sx][py+sy] = EYE_DARK;
                                        depths[px+sx][py+sy] = 256 + b * 7 - d * 4 + sy;
                                    }
                                    for (int sy = 2; sy < 5; sy++) {
                                        working[px+sx][py+sy] = EYE_DARK;
                                        depths[px+sx][py+sy] = 256 + b * 7 - d * 4 + sy;
                                    }
                                }
                                working[px][py] = EYE_LIGHT;
                                working[px+1][py] = EYE_LIGHT;
                                working[px][py+1] = EYE_LIGHT;
                                working[px+1][py+1] = EYE_LIGHT;
                            }
                            else
                            {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 3; sy < 5; sy++) {
                                        working[px+sx][py+sy] = current + 1;
                                        depths[px+sx][py+sy] = 256 + b * 7 - d * 4 + sy;
                                    }
                                    for (int sy = 0; sy < 3; sy++) {
                                        working[px+sx][py+sy] = current;
                                        depths[px+sx][py+sy] = 256 + b * 7 - d * 4 + sy;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        else  {
            for (int b = zs - 1; b >= 0; b--) {
                for (int a = aa; a != aMax; a += aStep) {
                    for (int c = cc, d = depthStart; c != cMax; c += cStep, d += depthStep) {
                        px = con.voxelToPixelX(a + 1, c + 1, b, xs, ys, zs);
                        py = con.voxelToPixelY(a + 1, c + 1, b, xs, ys, zs);
                        current = voxels[a][ys - 1 - c][b] & 255;
                        if(current != 0 && !((current >= 8 && current < 16) || (!RINSED_PALETTE && current >= 136 && current < 144)))
                        {
                            if (current <= 2) {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 0; sy < 5; sy++) {
                                        working[px+sx][py+sy] = current;
                                    }
                                }
                            } else if(current == 3) {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 0; sy < 5; sy++) {
                                        if(working[px+sx][py+sy] == 0) working[px+sx][py+sy] = current;
                                    }
                                }
                            } else if(current == 4) {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 0; sy < 2; sy++) {
                                        working[px+sx][py+sy] = EYE_DARK;
                                        depths[px+sx][py+sy] = 256 + b * 7 - d * 4 + sy;
                                    }
                                    for (int sy = 2; sy < 5; sy++) {
                                        working[px+sx][py+sy] = EYE_DARK;
                                        depths[px+sx][py+sy] = 256 + b * 7 - d * 4 + sy;
                                    }
                                }
                                working[px][py] = EYE_LIGHT;
                                working[px+1][py] = EYE_LIGHT;
                                working[px][py+1] = EYE_LIGHT;
                                working[px+1][py+1] = EYE_LIGHT;
                            }
                            else
                            {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 3; sy < 5; sy++) {
                                        working[px+sx][py+sy] = current + 1;
                                        depths[px+sx][py+sy] = 256 + b * 7 - d * 4 + sy;
                                    }
                                    for (int sy = 0; sy < 3; sy++) {
                                        working[px+sx][py+sy] = current;
                                        depths[px+sx][py+sy] = 256 + b * 7 - d * 4 + sy;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        int d, w, o = hardOutline ? 2 : 0;
        render = ArrayTools.copy(working);
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                if((w = clampDown(working[x][y])) > 3) {
                    d = depths[x][y];
                    if      (working[x - 1][y] == 0 && working[x][y - 1] == 0) { render[x - 1][y] = o; render[x][y - 1] = o; render[x][y] = w; }
                    else if (working[x + 1][y] == 0 && working[x][y - 1] == 0) { render[x + 1][y] = o; render[x][y - 1] = o; render[x][y] = w; }
                    else if (working[x - 1][y] == 0 && working[x][y + 1] == 0) { render[x - 1][y] = o; render[x][y + 1] = o; render[x][y] = w; }
                    else if (working[x + 1][y] == 0 && working[x][y + 1] == 0) { render[x + 1][y] = o; render[x][y + 1] = o; render[x][y] = w; }
                    else {
                        if (hardOutline && working[x - 1][y] == 0) { render[x - 1][y] = 2; } else if (working[x - 1][y] == 0 || depths[x - 1][y] < d - 9 ) { render[x - 1][y] = w; }
                        if (hardOutline && working[x + 1][y] == 0) { render[x + 1][y] = 2; } else if (working[x + 1][y] == 0 || depths[x + 1][y] < d - 9 ) { render[x + 1][y] = w; }
                        if (hardOutline && working[x][y - 1] == 0) { render[x][y - 1] = 2; } else if (working[x][y - 1] == 0 || depths[x][y - 1] < d - 10) { render[x][y - 1] = w; }
                        if (hardOutline && working[x][y + 1] == 0) { render[x][y + 1] = 2; } else if (working[x][y + 1] == 0 || depths[x][y + 1] < d - 10) { render[x][y + 1] = w; }
                    }
                }
            }
        }
        return easeSquares(render, working);
    }

    /**
     * Renders the given 3D voxel byte array, which should be no larger than 12x12x8, to a 52x64 2D int array storing
     * color indices, using the given direction to rotate the model's facing (from 0 to 3).
     * @param voxels a 3D byte array with each byte storing color information for a voxel.
     * @param direction a 90-degree-increment counter-clockwise direction, from 0 to 3.
     * @return a 2D int array storing the pixel indices for the rendered model
     */
    public int[][] renderIsoBelow(byte[][][] voxels, int direction) {
        final int xs = voxels.length, ys = voxels[0].length, zs = voxels[0][0].length;
        VariableConverter con = directionsIsoV[direction &= 3];
        int[][] working = makeRenderArray(xs, ys, zs, 4, 5, 1);
        int width = working.length, height = working[0].length;
        int[][] depths = new int[width][height], render;
        int px, py;
        int current;
        int d, w;
        final int gray = direction ^ direction >>> 1;
        final int cStart = (gray & 2) == 0 ? 0 : xs - 1, cEnd = (gray & 2) == 0 ? xs : -1, cChange = (gray & 2) == 0 ? 1 : -1;
        final int aStart = (gray & 1) == 0 ? ys - 1 : 0, aEnd = (gray & 1) == 0 ? -1 : ys, aChange = (gray & 1) == 0 ? -1 : 1;
        for (int b = zs - 1; b >= 0; b--) {
            for (int c = cStart; c != cEnd; c += cChange) {
                for (int a = aStart; a != aEnd; a += aChange) {
                    px = con.voxelToPixelX(c + 1, a + 1, b, xs, ys, zs);
                    py = con.voxelToPixelY(xs - c, ys - a, b, xs, ys, zs);
                    if(px < 0 || py < 0) continue;
                    current = voxels[c][a][b] & 255;
                    if (current != 0 && !((current >= 8 && current < 16) || (!RINSED_PALETTE && current >= 136 && current < 144))) {
                        d = 3 * (b + (c * cChange - a)) + 256;
                        if (current <= 2) {
                            working[px][py] = current;
                        } else if (current == 3) {
                            for (int ix = 0; ix < 4; ix++) {
                                for (int iy = 0; iy < 4; iy++) {
                                    if (working[px + ix][py + iy] == 0)
                                    {
                                        working[px + ix][py + iy] = 3;
                                        depths[px + ix][py + iy] = d + ((ix ^ ix >>> 1) & 1); // adds 1 only in center of a voxel
                                    }
                                }
                            }
                        } else if (current == 4) {
                            for (int ix = 0; ix < 4; ix++) {
                                for (int iy = 0; iy < 4; iy++) {
                                    if (ix < 2 && iy < 2)
                                    {
                                        working[px + ix][py + iy] = EYE_LIGHT;
                                        depths[px + ix][py + iy] = d + ((ix ^ ix >>> 1) & 1); // adds 1 only in center of a voxel
                                    }
                                    else
                                    {
                                        working[px + ix][py + iy] = EYE_DARK;
                                        depths[px + ix][py + iy] = d + ((ix ^ ix >>> 1) & 1); // adds 1 only in center of a voxel
                                    }
                                }
                            }
                        } else {
                            for (int ix = 0; ix < 4; ix++) {
                                for (int iy = 2; iy < 4; iy++) {
                                    working[px + ix][py + iy] = current;
                                    depths[px + ix][py + iy] = d + ((ix ^ ix >>> 1) & 1); // adds 1 only in center of a voxel
                                }
                            }
                            for (int ix = 0; ix < 2; ix++) {
                                for (int iy = 0; iy < 2; iy++) {
                                    working[px + ix][py + iy] = current - 1;
                                    depths[px + ix][py + iy] = d + ((ix ^ ix >>> 1) & 1); // adds 1 only in center of a voxel
                                }
                            }
                            for (int ix = 2; ix < 4; ix++) {
                                for (int iy = 0; iy < 2; iy++) {
                                    working[px + ix][py + iy] = current + 1;
                                    depths[px + ix][py + iy] = d + ((ix ^ ix >>> 1) & 1); // adds 1 only in center of a voxel
                                }
                            }
                        }
                    }
                }
            }
        }

        render = ArrayTools.copy(working);

        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                if((w = clampDown(working[x][y])) > 3) {
                    int o = hardOutline ? 2 : w;
                    d = depths[x][y];
                    if      (working[x - 1][y] == 0 && working[x][y - 1] == 0) { render[x - 1][y] = o; render[x][y - 1] = o; render[x][y] = w; }
                    else if (working[x + 1][y] == 0 && working[x][y - 1] == 0) { render[x + 1][y] = o; render[x][y - 1] = o; render[x][y] = w; }
                    else if (working[x - 1][y] == 0 && working[x][y + 1] == 0) { render[x - 1][y] = o; render[x][y + 1] = o; render[x][y] = w; }
                    else if (working[x + 1][y] == 0 && working[x][y + 1] == 0) { render[x + 1][y] = o; render[x][y + 1] = o; render[x][y] = w; }
                    else {
                        if (hardOutline && working[x - 1][y] == 0) { render[x - 1][y] = 2; } else if (working[x - 1][y] == 0 || depths[x - 1][y] < d - 9) { render[x - 1][y] = w; }
                        if (hardOutline && working[x + 1][y] == 0) { render[x + 1][y] = 2; } else if (working[x + 1][y] == 0 || depths[x + 1][y] < d - 9) { render[x + 1][y] = w; }
                        if (hardOutline && working[x][y - 1] == 0) { render[x][y - 1] = 2; } else if (working[x][y - 1] == 0 || depths[x][y - 1] < d - 9) { render[x][y - 1] = w; }
                        if (hardOutline && working[x][y + 1] == 0) { render[x][y + 1] = 2; } else if (working[x][y + 1] == 0 || depths[x][y + 1] < d - 9) { render[x][y + 1] = w; }
                    }
                }
            }
        }

        return easeSquares(render, working);
    }

    public int[][] renderOrthoSide(byte[][][] voxels, int direction)
    {
        final int xs = voxels.length, ys = voxels[0].length, zs = voxels[0][0].length;
        VariableConverter con = directionsOrthoSideV[direction &= 3];
        int[][] working = makeRenderArray(xs, ys, zs, 4, 5, 1);
        int width = working.length, height = working[0].length;
        int[][] depths = new int[width][height], render;
        int aa, cc, aStep, cStep, aMax, cMax, px, py;
        boolean flip = true;
        int current;
        switch (direction)
        {
            case 0:
                aa = 0;
                aStep = 1;
                aMax = ys;
                cc = 0;
                cStep = 1;
                cMax = xs;
                break;
            case 1:
                aa = xs - 1;
                aStep = -1;
                aMax = -1;
                cc = 0;
                cStep = 1;
                cMax = ys;
                flip = false;
                break;
            case 2:
                aa = ys - 1;
                aStep = -1;
                aMax = -1;
                cc = xs - 1;
                cStep = -1;
                cMax = -1;
                break;
            default:
                aa = 0;
                aStep = 1;
                aMax = xs;
                cc = ys - 1;
                cStep = -1;
                cMax = -1;
                flip = false;
                break;
        }
        if(flip) {
            for (int c = cc; c != cMax; c += cStep) { 
                for (int a = aa; a != aMax; a += aStep) { 
                    for (int b = 0; b < zs; b++) {
                        px = con.voxelToPixelX(c + 1, a + 1, b, xs, ys, zs);
                        py = con.voxelToPixelY(c + 1, a + 1, b, xs, ys, zs);
                        current = voxels[c][a][b] & 255;
                        if (current != 0 && !((current >= 8 && current < 16) || (!RINSED_PALETTE && current >= 136 && current < 144))) {
                            if (current <= 2) {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 0; sy < 4; sy++) {
                                        working[px+sx][py+sy] = current;
                                    }
                                }
                            } else if(current == 3) {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 0; sy < 4; sy++) {
                                        if(working[px+sx][py+sy] == 0) working[px+sx][py+sy] = current;
                                    }
                                }
                            } else if(current == 4) {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 1; sy < 4; sy++) {
                                        working[px+sx][py+sy] = EYE_DARK;
                                        depths[px+sx][py+sy] = 256 + c * 2;
                                    }                                     
                                    working[px+sx][py] = EYE_DARK;
                                    depths[px+sx][py] = 256 + c * 2;
                                    
                                }
                                working[px][py] = EYE_LIGHT;
                                working[px+1][py] = EYE_LIGHT;
                                working[px][py+1] = EYE_LIGHT;
                                working[px+1][py+1] = EYE_LIGHT;
                            }
                            else
                            {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 1; sy < 4; sy++) {
                                        working[px+sx][py+sy] = current;
                                        depths[px+sx][py+sy] = 256 + c * 2;
                                    }
                                    working[px+sx][py] = current - 1;
                                    depths[px+sx][py] = 256 + c * 2;
                                }
                            }
                        }
                    }
                }
            }
        }
        else  {
            for (int a = aa; a != aMax; a += aStep) {
                for (int b = 0; b < zs; b++) {
                    for (int c = cc; c != cMax; c += cStep) {
                        px = con.voxelToPixelX(a + 1, c + 1, b, xs, ys, zs);
                        py = con.voxelToPixelY(a + 1, c + 1, b, xs, ys, zs);
                        current = voxels[a][c][b] & 255;
                        if(current != 0 && !((current >= 8 && current < 16) || (!RINSED_PALETTE && current >= 136 && current < 144)))
                        {
                            if (current <= 2) {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 0; sy < 4; sy++) {
                                        working[px+sx][py+sy] = current;
                                    }
                                }
                            } else if(current == 3) {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 0; sy < 4; sy++) {
                                        if(working[px+sx][py+sy] == 0) working[px+sx][py+sy] = current;
                                    }
                                }
                            } else if(current == 4) {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 1; sy < 4; sy++) {
                                        working[px+sx][py+sy] = EYE_DARK;
                                        depths[px+sx][py+sy] = 256 + c * 2;
                                    }
                                    working[px+sx][py] = EYE_DARK;
                                    depths[px+sx][py] = 256 + c * 2;

                                }
                                working[px][py] = EYE_LIGHT;
                                working[px+1][py] = EYE_LIGHT;
                                working[px][py+1] = EYE_LIGHT;
                                working[px+1][py+1] = EYE_LIGHT;
                            }
                            else
                            {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 1; sy < 4; sy++) {
                                        working[px+sx][py+sy] = current;
                                        depths[px+sx][py+sy] = 256 + c * 2;
                                    }
                                    working[px+sx][py] = current - 1;
                                    depths[px+sx][py] = 256 + c * 2;
                                }
                            }
                        }
                    }
                }
            }
        }
        int d, w;
        render = ArrayTools.copy(working);
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                if((w = clampDown(working[x][y])) > 3) {
                    int o = hardOutline ? 2 : w;
                    d = depths[x][y];
                    if      (working[x - 1][y] == 0 && working[x][y - 1] == 0) { render[x - 1][y] = o; render[x][y - 1] = o; render[x][y] = w; }
                    else if (working[x + 1][y] == 0 && working[x][y - 1] == 0) { render[x + 1][y] = o; render[x][y - 1] = o; render[x][y] = w; }
                    else if (working[x - 1][y] == 0 && working[x][y + 1] == 0) { render[x - 1][y] = o; render[x][y + 1] = o; render[x][y] = w; }
                    else if (working[x + 1][y] == 0 && working[x][y + 1] == 0) { render[x + 1][y] = o; render[x][y + 1] = o; render[x][y] = w; }
                    else {
                        if (hardOutline && working[x - 1][y] == 0) { render[x - 1][y] = 2; } else if (working[x - 1][y] == 0 || depths[x - 1][y] < d - 2) { render[x - 1][y] = w; }
                        if (hardOutline && working[x + 1][y] == 0) { render[x + 1][y] = 2; } else if (working[x + 1][y] == 0 || depths[x + 1][y] < d - 2) { render[x + 1][y] = w; }
                        if (hardOutline && working[x][y - 1] == 0) { render[x][y - 1] = 2; } else if (working[x][y - 1] == 0 || depths[x][y - 1] < d - 2) { render[x][y - 1] = w; }
                        if (hardOutline && working[x][y + 1] == 0) { render[x][y + 1] = 2; } else if (working[x][y + 1] == 0 || depths[x][y + 1] < d - 2) { render[x][y + 1] = w; }
                    }
                }
            }
        }
        return easeSquares(render, working);
    }
    public int[][] renderIsoSide(byte[][][] voxels, int direction) {
        final int xs = voxels.length, ys = voxels[0].length, zs = voxels[0][0].length;
        VariableConverter con = directionsIsoSideV[direction &= 3];
        int[][] working = makeRenderArray(xs, ys, zs, 4, 5, 1);
        int width = working.length, height = working[0].length;
        int[][] depths = new int[width][height], render;
        int px, py;
        int current;
        int d, w;
        final int gray = direction ^ direction >>> 1;
        final int cStart = (gray & 2) == 0 ? 0 : xs - 1, cEnd = (gray & 2) == 0 ? xs : -1, cChange = (gray & 2) == 0 ? 1 : -1;
        final int aStart = (gray & 1) == 0 ? ys - 1 : 0, aEnd = (gray & 1) == 0 ? -1 : ys, aChange = (gray & 1) == 0 ? -1 : 1;
        //final int aStart = ys - 1, aEnd = -1, aChange = -1;
        for (int b = 0; b < zs; b++) {
            for (int c = cStart; c != cEnd; c += cChange) {
                for (int a = aStart; a != aEnd; a += aChange) {
                    px = con.voxelToPixelX(c + 1, a + 1, b, xs, ys, zs);
                    py = con.voxelToPixelY(c + 1, a + 1, b, xs, ys, zs);
                    if(px < 0 || py < 0) continue;
                    current = voxels[c][a][b] & 255;
                    if (current != 0 && !((current >= 8 && current < 16) || (!RINSED_PALETTE && current >= 136 && current < 144))) {
                        d = 3 * (c * cChange + a * aChange) + 256;
                        if (current <= 2) {
                            working[px][py] = current;
                        } else if (current == 3) {
                            for (int ix = 0; ix < 4; ix++) {
                                for (int iy = 0; iy < 4; iy++) {
                                    if (working[px + ix][py + iy] == 0)
                                    {
                                        working[px + ix][py + iy] = 3;
                                        depths[px + ix][py + iy] = d + (ix & ix >>> 1); // adds 1 only on the right edge of a voxel
                                    }
                                }
                            }
                        } else if (current == 4) {
                            for (int ix = 0; ix < 4; ix++) {
                                for (int iy = 0; iy < 4; iy++) {
                                    if (ix < 2 && iy < 2)
                                    {
                                        working[px + ix][py + iy] = EYE_LIGHT;
                                        depths[px + ix][py + iy] = d + (ix & ix >>> 1); // adds 1 only on the right edge of a voxel
                                    }
                                    else
                                    {
                                        working[px + ix][py + iy] = EYE_DARK;
                                        depths[px + ix][py + iy] = d + (ix & ix >>> 1); // adds 1 only on the right edge of a voxel
                                    }
                                }
                            }
                        } else {
                            for (int ix = 0; ix < 4; ix++) {
                                    working[px + ix][py] = current - 1;
                                    depths[px + ix][py] = d + (ix & ix >>> 1); // adds 1 only on the right edge of a voxel
                            }
                            for (int ix = 0; ix < 2; ix++) {
                                for (int iy = 1; iy < 4; iy++) {
                                    working[px + ix][py + iy] = current;
                                    depths[px + ix][py + iy] = d + ix; // adds 1 only in center of voxel
                                }
                            }
                            for (int ix = 2; ix < 4; ix++) {
                                for (int iy = 1; iy < 4; iy++) {
                                    working[px + ix][py + iy] = current + 1;
                                    depths[px + ix][py + iy] = d - ix + 3; // adds 1 only in center of voxel
                                }
                            }
                        }
                    }
                }
            }
        }
        render = ArrayTools.copy(working);
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                if((w = clampDown(working[x][y])) > 3) {
                    int o = hardOutline ? 2 : w;
                    d = depths[x][y];
                    if      (working[x - 1][y] == 0 && working[x][y - 1] == 0) { render[x - 1][y] = o; render[x][y - 1] = o; render[x][y] = w; }
                    else if (working[x + 1][y] == 0 && working[x][y - 1] == 0) { render[x + 1][y] = o; render[x][y - 1] = o; render[x][y] = w; }
                    else if (working[x - 1][y] == 0 && working[x][y + 1] == 0) { render[x - 1][y] = o; render[x][y + 1] = o; render[x][y] = w; }
                    else if (working[x + 1][y] == 0 && working[x][y + 1] == 0) { render[x + 1][y] = o; render[x][y + 1] = o; render[x][y] = w; }
                    else {
                        if (hardOutline && working[x - 1][y] == 0) { render[x - 1][y] = 2; } else if (working[x - 1][y] == 0 || depths[x - 1][y] < d - 5) { render[x - 1][y] = w; }
                        if (hardOutline && working[x + 1][y] == 0) { render[x + 1][y] = 2; } else if (working[x + 1][y] == 0 || depths[x + 1][y] < d - 5) { render[x + 1][y] = w; }
                        if (hardOutline && working[x][y - 1] == 0) { render[x][y - 1] = 2; } else if (working[x][y - 1] == 0 || depths[x][y - 1] < d - 5) { render[x][y - 1] = w; }
                        if (hardOutline && working[x][y + 1] == 0) { render[x][y + 1] = 2; } else if (working[x][y + 1] == 0 || depths[x][y + 1] < d - 5) { render[x][y + 1] = w; }
                    }
                }
            }
        }
        return easeSquares(render, working);
    }
    
    public int[][] easeSquares(int[][] original, int[][] out){
        if(!easing)
            return original;
        int xSize = original.length - 1, ySize = original[0].length - 1;

        Arrays.fill(tempPalette, 0);
        for (int x = 0; x < xSize + 1; x++) {
            for (int y = 0; y < ySize + 1; y++) { 
                tempPalette[out[x][y] & 255]++;
            }
        }
        int o, a, b, c, d;
        for (int x = 1; x < xSize; x++) {
            for (int y = 1; y < ySize; y++) {
                o = original[x][y];
                a = original[x - 1][y - 1];
                b = original[x + 1][y - 1];
                c = original[x - 1][y + 1];
                d = original[x + 1][y + 1];
                if (o > 2 && a > 2 && b > 2 && c > 2 && d > 2) {
                    if (a == d && tempPalette[a] < tempPalette[o]) out[x][y] = a;
                    else if (b == c && tempPalette[b] < tempPalette[o]) out[x][y] = b;
                    else out[x][y] = o;
                }
                else out[x][y] = o;
            }
        }
        return out;
    }

    /**
     * Renders the given 3D voxel byte array, which should be no larger than 12x12x8, to a 16x16 2D int array storing
     * color indices, using the given direction to rotate the model's facing (from 0 to 3).
     * @param voxels a 3D byte array with each byte storing color information for a voxel.
     * @param direction a 90-degree-increment counter-clockwise direction, from 0 to 3.
     * @return a 2D int array storing the pixel indices for the rendered model
     */
    public int[][] render16x16(byte[][][] voxels, int direction)
    {
        final int xs = voxels.length, ys = voxels[0].length, zs = voxels[0][0].length;
        Converter con = directions16x16[direction &= 3];
        int[][] working = new int[16][16], depths = new int[16][16], render;
        int aa, cc, aStep, cStep, aMax, cMax, px, py;
        boolean flip = true;
        int current;
        switch (direction)
        {
            case 0:
                aa = 0;
                aStep = 1;
                aMax = ys;
                cc = 0;
                cStep = 1;
                cMax = xs;
                break;
            case 1:
                aa = xs - 1;
                aStep = -1;
                aMax = -1;
                cc = ys - 1;
                cStep = -1;
                cMax = -1;
                flip = false;
                break;
            case 2:
                aa = ys - 1;
                aStep = -1;
                aMax = -1;
                cc = xs - 1;
                cStep = -1;
                cMax = -1;
                break;
            default:
                aa = 0;
                aStep = 1;
                aMax = xs;
                cc = 0;
                cStep = 1;
                cMax = ys;
                flip = false;
                break;
        }
        if(flip) {
            for (int a = aa; a != aMax; a += aStep) {
                for (int b = 0; b < zs; b++) {
                    for (int c = cc; c != cMax; c += cStep) {
                        px = con.voxelToPixelX(c, a, b);
                        py = con.voxelToPixelY(c, a, b);
                        current = voxels[c][a][b] & 255;
                        if (current != 0 && !((current >= 8 && current < 16) || (!RINSED_PALETTE && current >= 136 && current < 144))) {
                            if (current <= 2) {
                                working[px][py] = current;
                            } else if(current == 3) {
                                if(working[px][py] == 0)
                                    working[px][py] = 3;
                            }
                            else
                            {
                                if (b == zs - 1 || voxels[c][a][b + 1] == 0)
                                    working[px][py] = current + 1;
                                else if (b == 0 || voxels[c][a][b - 1] == 0)
                                    working[px][py] = current - 1;
                                else
                                    working[px][py] = current;
                                depths[px][py] = b * 2 - c;
                            }
                        }
                    }
                }
            }
        }
        else  {
            for (int a = aa; a != aMax; a += aStep) {
                for (int b = 0; b < zs; b++) {
                    for (int c = cc; c != cMax; c += cStep) {
                        px = con.voxelToPixelX(a, c, b);
                        py = con.voxelToPixelY(a, c, b);
                        current = voxels[a][c][b] & 255;
                        if(current != 0 && !((current >= 8 && current < 16) || (!RINSED_PALETTE && current >= 136 && current < 144)))
                        {
                            if(current <= 2) {
                                working[px][py] = current;
                            } else if(current == 3) {
                                if(working[px][py] == 0)
                                    working[px][py] = 3;
                            }
                            else
                            {
                                if(b == zs - 1 || voxels[a][c][b+1] == 0)
                                    working[px][py] = current + 1;
                                else if(b == 0 || voxels[a][c][b-1] == 0)
                                    working[px][py] = current - 1;
                                else
                                    working[px][py] = current;
                                depths[px][py] = b * 2 - a;
                            }
                        }
                    }
                }
            }
        }
        int d;
        render = ArrayTools.copy(working);
        for (int x = 1; x < 15; x++) {
            for (int y = 1; y < 15; y++) {
                if((working[x][y]) > 2)
                {
                    d = depths[x][y];
                    if(working[x-1][y] == 0) render[x-1][y] = 2; else if(depths[x-1][y] < d - 2) render[x-1][y] = working[x-1][y] - 1;
                    if(working[x+1][y] == 0) render[x+1][y] = 2; else if(depths[x+1][y] < d - 2) render[x+1][y] = working[x+1][y] - 1;
                    if(working[x][y-1] == 0) render[x][y-1] = 2; else if(depths[x][y-1] < d - 2) render[x][y-1] = working[x][y-1] - 1;
                    if(working[x][y+1] == 0) render[x][y+1] = 2; else if(depths[x][y+1] < d - 2) render[x][y+1] = working[x][y+1] - 1;
                }
            }
        }
        return render;
    }
    /**
     * Renders the given 3D voxel byte array, which should be no larger than 12x12x8, to a 24x32 2D int array storing
     * color indices, using the given direction to rotate the model's facing (from 0 to 3).
     * @param voxels a 3D byte array with each byte storing color information for a voxel.
     * @param direction a 90-degree-increment counter-clockwise direction, from 0 to 3.
     * @return a 2D int array storing the pixel indices for the rendered model
     */
    public int[][] renderIso24x32(byte[][][] voxels, int direction) {
        final int xs = voxels.length, ys = voxels[0].length, zs = voxels[0][0].length;
        Converter con = directionsIso28x35[direction &= 3];
        int[][] working = new int[52][64], depths = new int[52][64], render;
        int px, py;
        int current;
        int d, w;
        final int gray = direction ^ direction >>> 1;
        final int cStart = (gray & 2) == 0 ? 0 : xs - 1, cEnd = (gray & 2) == 0 ? xs : -1, cChange = (gray & 2) == 0 ? 1 : -1;
        final int aStart = (gray & 1) == 0 ? ys - 1 : 0, aEnd = (gray & 1) == 0 ? -1 : ys, aChange = (gray & 1) == 0 ? -1 : 1;
        for (int b = 0; b < zs; b++) {
            for (int c = cStart; c != cEnd; c += cChange) {
                for (int a = aStart; a != aEnd; a += aChange) {

                    px = con.voxelToPixelX(c+1, a+1, b);
                    py = con.voxelToPixelY(c+1, a+1, b);
                    if(px < 1 || py < 2) continue;
                    px = px - 1 << 1;
                    py = (py - 2 << 1) + 1;
                    current = voxels[c][a][b] & 255;
                    if (current != 0 && !((current >= 8 && current < 16) || (!RINSED_PALETTE && current >= 136 && current < 144))) {
                        d = 3 * (b + (c * cChange - a)) + 256;
                        if (current <= 2) {
                            working[px][py] = current;
                        } else if (current == 3) {
                            for (int ix = 0; ix < 4; ix++) {
                                for (int iy = 0; iy < 4; iy++) {
                                    if (working[px + ix][py + iy] == 0)
                                    {
                                        working[px + ix][py + iy] = 3;
                                        depths[px + ix][py + iy] = d + (ix & ix >>> 1); // adds 1 only on the right edge of a voxel
                                    }
                                }
                            }
                        } else if (current == 4) {
                            for (int ix = 0; ix < 4; ix++) {
                                for (int iy = 0; iy < 4; iy++) {
                                    if (ix < 2 && iy < 2)
                                    {
                                        working[px + ix][py + iy] = EYE_LIGHT;
                                        depths[px + ix][py + iy] = d + (ix & ix >>> 1); // adds 1 only on the right edge of a voxel
                                    }
                                    else
                                    {
                                        working[px + ix][py + iy] = EYE_DARK;
                                        depths[px + ix][py + iy] = d + (ix & ix >>> 1); // adds 1 only on the right edge of a voxel
                                    }
                                }
                            }
                        } else {
                            for (int ix = 0; ix < 4; ix++) {
                                for (int iy = 0; iy < 2; iy++) {
                                    working[px + ix][py + iy] = current - 1;
                                    depths[px + ix][py + iy] = d + (ix & ix >>> 1); // adds 1 only on the right edge of a voxel
                                }
                            }
                            for (int ix = 0; ix < 2; ix++) {
                                for (int iy = 2; iy < 4; iy++) {
                                    working[px + ix][py + iy] = current;
                                    depths[px + ix][py + iy] = d + ix; // adds 1 only in center of voxel
                                }
                            }
                            for (int ix = 2; ix < 4; ix++) {
                                for (int iy = 2; iy < 4; iy++) {
                                    working[px + ix][py + iy] = current + 1;
                                    depths[px + ix][py + iy] = d - ix + 3; // adds 1 only in center of voxel
                                }
                            }
                        }
                    }
                }
            }
        }
        //working = easeSquares(working);
        //working = easeSquares(size2(working));
        //working = size2(working);
        //depths = size2Mix(depths);
        //depths = size2(depths);
        render = new int[24][32];//ArrayTools.copy(working);
        //int[][] shaded = ArrayTools.fetch(65535, 52, 64);
        for (int x = 2, rx = 0; x < 50 && rx < 24; x += 2, rx++) {
            for (int y = 2, ry = 1; y < 64 && ry < 32; y += 2, ry++) {
                render[rx][ry] = working[x][y];
            }
        }

        for (int x = 2, rx = 0; x < 50 && rx < 24; x += 2, rx++) {
            for (int y = 2, ry=1; y < 64 && ry < 32; y += 2, ry++) {
                if((w = clampDown(working[x][y])) > 3) {
                    d = depths[x][y];
                    //if (working[x - 1][y] == 0 && working[x][y - 1] == 0)      { render[rx][ry] = 2; }
                    //else if (working[x + 1][y] == 0 && working[x][y - 1] == 0) { render[rx][ry] = 2; }
                    //else if (working[x - 1][y] == 0 && working[x][y + 1] == 0) { render[rx][ry] = 2; }
                    //else if (working[x + 1][y] == 0 && working[x][y + 1] == 0) { render[rx][ry] = 2; }
                    //else
                    {
                        if (           /* (working[x][y] & 7) > (w & 7) && */ depths[x - 1][y] < d - 5) { render[rx][ry] = w; }
                        else if (           /* (working[x][y] & 7) > (w & 7) && */ depths[x + 2][y] < d - 6) { render[rx][ry] = w; }
                        else if (           /* (working[x][y] & 7) > (w & 7) && */ depths[x][y - 2] < d - 5) { render[rx][ry] = w; }
                        else if (y >= 62 || /* (working[x][y] & 7) > (w & 7) && */ depths[x][y + 1] < d - 5) { render[rx][ry] = w; }
                    }
                }
            }
        }
        return render;
    }

    protected interface Converter
    {
        /**
         * Converts from voxel space to pixel space and gets the 2D x position. Here, the x-axis runs back-to-front,
         * the y-axis runs left-to-right, and the z-axis runs bottom-to-top, before rotations are applied.
         * @param vx before rotating, the position on the 3D back-to-front x axis
         * @param vy before rotating, the position on the 3D left-to-right y axis
         * @param vz before rotating, the position on the 3D bottom-to-top z axis
         * @return the 2D x position of the voxel, with 0 at the left
         */
        int voxelToPixelX(int vx, int vy, int vz);
        /**
         * Converts from voxel space to pixel space and gets the 2D y position. Here, the x-axis runs back-to-front,
         * the y-axis runs left-to-right, and the z-axis runs bottom-to-top, before rotations are applied.
         * @param vx before rotating, the position on the 3D back-to-front x axis
         * @param vy before rotating, the position on the 3D left-to-right y axis
         * @param vz before rotating, the position on the 3D bottom-to-top z axis
         * @return the 2D y position of the voxel, with 0 at the top
         */
        int voxelToPixelY(int vx, int vy, int vz);
    }

    protected interface VariableConverter
    {
        /**
         * Converts from voxel space to pixel space and gets the 2D x position. Here, the x-axis runs back-to-front,
         * the y-axis runs left-to-right, and the z-axis runs bottom-to-top, before rotations are applied.
         * @param vx before rotating, the position on the 3D back-to-front x axis
         * @param vy before rotating, the position on the 3D left-to-right y axis
         * @param vz before rotating, the position on the 3D bottom-to-top z axis
         * @param xSize size of the bounding box's x dimension
         * @param ySize size of the bounding box's y dimension
         * @param zSize size of the bounding box's z dimension
         * @return the 2D x position of the voxel, with 0 at the left
         */
        int voxelToPixelX(int vx, int vy, int vz, int xSize, int ySize, int zSize);
        /**
         * Converts from voxel space to pixel space and gets the 2D y position. Here, the x-axis runs back-to-front,
         * the y-axis runs left-to-right, and the z-axis runs bottom-to-top, before rotations are applied.
         * @param vx before rotating, the position on the 3D back-to-front x axis
         * @param vy before rotating, the position on the 3D left-to-right y axis
         * @param vz before rotating, the position on the 3D bottom-to-top z axis
         * @param xSize size of the bounding box's x dimension
         * @param ySize size of the bounding box's y dimension
         * @param zSize size of the bounding box's z dimension
         * @return the 2D y position of the voxel, with 0 at the top
         */
        int voxelToPixelY(int vx, int vy, int vz, int xSize, int ySize, int zSize);
    }

    /**
     * For a max of a 14x14x8 voxel space, produces pixel coordinates for a 16x16 area
     */
    protected static final  Converter[] directions16x16 = {
            // direction 0, no rotation
            new Converter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz) {
                    return vy + 1;
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz) {
                    return (vx >> 1) + 8 - vz;
                }
            },
            // direction 1
            new Converter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz) {
                    return 14 - vx;
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz) {
                    return 14 - (vy >> 1) - vz;
                }
            },
            // direction 2
            new Converter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz) {
                    return 14 - vy;
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz) {
                    return 14 - (vx >> 1) - vz;
                }
            },
            // direction 3
            new Converter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz) {
                    return vx + 1;
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz) {
                    return (vy >> 1) + 8 - vz;
                }
            }
    };

    /**
     * For a max of a 14x14x8 voxel space, produces pixel coordinates for a 16x16 area
     */
    protected static final  Converter[] directionsOrtho52x64 = {
            // direction 0, no rotation
            new Converter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz) {
                    return vy * 3 + 4;
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz) {
                    return 16 + (vx * 3) - vz * 2;
                }
            },
            // direction 1
            new Converter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz) {
                    return 43 - vx * 3;
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz) {
                    return 16 + (vy * 3) - vz * 2;
                }
            },
            // direction 2
            new Converter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz) {
                    return 43 - vy * 3;
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz) {
                    return 55 - (vx * 3) - vz * 2;
                }
            },
            // direction 3
            new Converter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz) {
                    return vx * 3 + 4;
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz) {
                    return 55 - (vy * 3) - vz * 2;
                }
            }
    };
    protected static final Converter[] directionsIso28x35 = {
            // direction 0, no rotation
            new Converter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz) {
                    return (vy + vx);
                    //return vy + 2;
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz) {
                    return (vx - vy - vz) + 20;
                    //return (vx >> 1) + 8 - vz;
                }
            },
            // direction 1
            new Converter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz) {
                    return (vy - vx + 13);
                    //return 13 - vx;
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz) {
                    return (vy + vx - vz) + 7;
                    //return 13 - (vy >> 1) - vz;
                }
            },
            // direction 2
            new Converter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz) {
                    return (26 - vy - vx);
                    //return 13 - vy;
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz) {
                    return (vy - vx - vz) + 20; //(13 - vy) - (13 - vx)
                    //return 13 - (vx >> 1) - vz;
                }
            },
            // direction 3
            new Converter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz) {
                    return (vx - vy + 13);
                    //return vx + 2;
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz) {
                    return ((13 - vx) - vy - vz) + 20;
                    //return (vy >> 1) + 8 - vz;
                }
            }
    };
    protected static final  Converter[] directionsOrthoSide52x64 = {
            // direction 0, no rotation
            new Converter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz) {
                    return vy * 3 + 4;
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz) {
                    return 36 - vz * 3;
                }
            },
            // direction 1
            new Converter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz) {
                    return 43 - vx * 3;
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz) {
                    return 36 - vz * 3;
                }
            },
            // direction 2
            new Converter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz) {
                    return 43 - vy * 3;
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz) {
                    return 36 - vz * 3;
                }
            },
            // direction 3
            new Converter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz) {
                    return vx * 3 + 4;
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz) {
                    return 36 - vz * 3;
                }
            }
    };

    protected static final  Converter[] directionsIsoSide52x64 = {
            // direction 0, no rotation
            new Converter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz) {
                    return (vy + vx) - 1 << 1;
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz) {
                    return 36 - vz * 3;
                }
            },
            // direction 1
            new Converter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz) {
                    return (vy - vx + 13) - 1 << 1;
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz) {
                    return 36 - vz * 3;
                }
            },
            // direction 2
            new Converter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz) {
                    return (26 - vy - vx) - 1 << 1;
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz) {
                    return 36 - vz * 3;
                }
            },
            // direction 3
            new Converter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz) {
                    return (vx - vy + 13) - 1 << 1;
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz) {
                    return 36 - vz * 3;
                }
            }
    };
    protected  static final VariableConverter[] directionsOrthoV = {
            // direction 0, no rotation
            new VariableConverter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return vy * 3 + (ys-1>>1);
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return (vx * 3) + (zs - vz) * 2 + (zs>>1);
                }
            },
            // direction 1
            new VariableConverter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return 3 + (xs - vx) * 3 + (xs-1>>1);
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return (vy * 3) + (zs - vz) * 2 + (zs>>1);
                }
            },
            // direction 2
            new VariableConverter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return 3 + (ys - vy) * 3 + (ys-1>>1);
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return 3 + (xs - vx) * 3 + (zs - vz) * 2 + (zs>>1);
                }
            },
            // direction 3
            new VariableConverter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return vx * 3 + (xs-1>>1);
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return 3 + (ys - vy) * 3 + (zs - vz) * 2 + (zs>>1);
                }
            }
    };
    protected static final  VariableConverter[] directionsIsoV = {
            
            /*
                    px = px - 1 << 1;
                    py = (py - 2 << 1) + 1;

             */
            
            // direction 0, no rotation
            new VariableConverter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return (vy + vx) - 1 << 1;
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return ((vx - vy - vz + ys + zs) << 1) - 2;
                    //return (vx >> 1) + 8 - vz;
                }
            },
            // direction 1
            new VariableConverter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return (vy - vx + xs) << 1;
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return ((vy + vx - vz + zs) << 1) - 4;
                }
            },
            // direction 2
            new VariableConverter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return ((xs + ys - vy - vx) << 1) + 2;
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return ((vy - vx - vz + xs + zs) << 1) - 2;
                }
            },
            // direction 3
            new VariableConverter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return (vx - vy + ys) << 1;
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return ((xs + ys + zs - vy - vx - vz) << 1);
                }
            }
    };
    protected static final VariableConverter[] directionsOrthoSideV = {
            // direction 0, no rotation
            new VariableConverter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return vy * 3 + (ys-1>>1);
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return 4 + zs * 4 - vz * 3;
                }
            },
            // direction 1
            new VariableConverter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return 3 + (xs - vx) * 3 + (xs-1>>1);
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return 4 + zs * 4 - vz * 3;
                }
            },
            // direction 2
            new VariableConverter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return 3 + (ys - vy) * 3 + (ys-1>>1);
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return 4 + zs * 4 - vz * 3;
                }
            },
            // direction 3
            new VariableConverter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return vx * 3 + (xs-1>>1);
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return 4 + zs * 4 - vz * 3;
                }
            }
    };

    protected static final VariableConverter[] directionsIsoSideV = {
            // direction 0, no rotation
            new VariableConverter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return (vy + vx) - 1 << 1;
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return 4 + zs * 4 - vz * 3;
                }
            },
            // direction 1
            new VariableConverter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return (vy - vx + xs) << 1;
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return 4 + zs * 4 - vz * 3;
                }
            },
            // direction 2
            new VariableConverter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return ((xs + ys - vy - vx) + 1 << 1);
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return 4 + zs * 4 - vz * 3;
                }
            },
            // direction 3
            new VariableConverter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return ((ys - vy + vx) << 1);
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return 4 + zs * 4 - vz * 3;
                }
            }
    };
    
    protected static final VariableConverter[][] allDirectionsV = {
            directionsIsoV, directionsOrthoV, directionsIsoSideV, directionsOrthoSideV
    };
    
    public int[][] makeRenderArray(int xs, int ys, int zs, int voxelWidth, int voxelHeight, int outline)
    {
        int w = voxelWidth, h = voxelHeight;
        for(VariableConverter[] cons : allDirectionsV) {
            for (VariableConverter vc : cons) {
                w = Math.max(w, vc.voxelToPixelX(1, 1, 0, xs, ys, zs) + voxelWidth);
                h = Math.max(h, vc.voxelToPixelY(1, 1, 0, xs, ys, zs) + voxelHeight);
                w = Math.max(w, vc.voxelToPixelX(xs, 1, 0, xs, ys, zs) + voxelWidth);
                h = Math.max(h, vc.voxelToPixelY(xs, 1, 0, xs, ys, zs) + voxelHeight);
                w = Math.max(w, vc.voxelToPixelX(1, ys, 0, xs, ys, zs) + voxelWidth);
                h = Math.max(h, vc.voxelToPixelY(1, ys, 0, xs, ys, zs) + voxelHeight);
                w = Math.max(w, vc.voxelToPixelX(xs, ys, 0, xs, ys, zs) + voxelWidth);
                h = Math.max(h, vc.voxelToPixelY(xs, ys, 0, xs, ys, zs) + voxelHeight);
                w = Math.max(w, vc.voxelToPixelX(1, 1, zs - 1, xs, ys, zs) + voxelWidth);
                h = Math.max(h, vc.voxelToPixelY(1, 1, zs - 1, xs, ys, zs) + voxelHeight);
                w = Math.max(w, vc.voxelToPixelX(xs, 1, zs - 1, xs, ys, zs) + voxelWidth);
                h = Math.max(h, vc.voxelToPixelY(xs, 1, zs - 1, xs, ys, zs) + voxelHeight);
                w = Math.max(w, vc.voxelToPixelX(1, ys, zs - 1, xs, ys, zs) + voxelWidth);
                h = Math.max(h, vc.voxelToPixelY(1, ys, zs - 1, xs, ys, zs) + voxelHeight);
                w = Math.max(w, vc.voxelToPixelX(xs, ys, zs - 1, xs, ys, zs) + voxelWidth);
                h = Math.max(h, vc.voxelToPixelY(xs, ys, zs - 1, xs, ys, zs) + voxelHeight);
            }
        }
        return new int[w + (outline << 1)][h + (outline << 1)];
    }

    public Pixmap renderToPixmap(IModel model, int angle, CompassDirection dir) {
        return renderToPixmap(model, angle, dir, Coloring.RINSED);
    }

    public Pixmap renderToPixmap(IModel model, int angle, CompassDirection dir, int[] palette) {
        int[][] indices = renderToArray(model, angle, dir);
        Pixmap pix = new Pixmap(indices.length, indices[0].length, Pixmap.Format.RGBA8888);
        for (int x = 0; x < indices.length; x++)
            for (int y = 0; y < indices[0].length; y++)
                pix.drawPixel(x, y, palette[indices[x][y]]);
        return pix;
    }

    public int[][] renderToArray(IModel model, int angle, CompassDirection dir) {
        switch (angle) {
            case 1:
                return dir.isCardinal() ?
                        renderOrthoBelow(model, dir)
                        : renderIsoBelow(model, dir);
            case 3:
                return dir.isCardinal() ?
                        renderOrtho(model, dir)
                        : renderIso(model, dir);
            default:
                return dir.isCardinal() ?
                        renderOrthoSide(model, dir)
                        : renderIsoSide(model, dir);
        }
    }
}
