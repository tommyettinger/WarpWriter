package warpwriter;

import squidpony.ArrayTools;

import java.util.Arrays;
/*
 TODO: Rotations 0 and 1 are transposed versions of each other, not rotations. Same for 2 and 3.
 This is noticeable only on models that aren't symmetrical.
  */
/**
 * Created by Tommy Ettinger on 11/4/2017.
 */
public class ModelRenderer {
    public ModelRenderer()
    {
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
                        if (current != 0) {
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
                        if(current != 0)
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
    public int[][] renderOrtho(byte[][][] voxels, int direction)
    {
        final int xs = voxels.length, ys = voxels[0].length, zs = voxels[0][0].length;
        VariableConverter con = directionsOrthoV[direction &= 3];
        int[][] working = makeRenderArray(xs, ys, zs, 3, 5, 1);
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
            for (int a = aa; a != aMax; a += aStep) {
                for (int b = 0; b < zs; b++) {
                    for (int c = cc; c != cMax; c += cStep) {
                        px = con.voxelToPixelX(c+1, a+1, b, xs, ys, zs);
                        py = con.voxelToPixelY(c+1, a+1, b, xs, ys, zs);
                        current = voxels[c][a][b] & 255;
                        if (current != 0) {
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
                                        working[px+sx][py+sy] = 8;
                                        depths[px+sx][py+sy] = 256 + b * 5 - c * 2;// + sy;
                                    }
                                    for (int sy = 3; sy < 5; sy++) {
                                        working[px+sx][py+sy] = 8;
                                        depths[px+sx][py+sy] = 255 + b * 5 - c * 2;// + sy;
                                    }
                                }
                                working[px][py] = 14;
                                working[px+1][py] = 14;
                                working[px][py+1] = 14;
                                working[px+1][py+1] = 14;
//                                working[px][py+2] = 14;
//                                working[px+1][py+2] = 14;
                            }
                            else
                            {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 0; sy < 3; sy++) {
                                        working[px+sx][py+sy] = current + 1;
                                        depths[px+sx][py+sy] = 256 + b * 5 - c * 2;// + sy;
                                    }
                                    for (int sy = 3; sy < 5; sy++) {
                                        working[px+sx][py+sy] = current;
                                        depths[px+sx][py+sy] = 255 + b * 5 - c * 2;// + sy;
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
                    for (int c = cc; c != cMax; c += cStep) {
                        px = con.voxelToPixelX(a+1, c+1, b, xs, ys, zs);
                        py = con.voxelToPixelY(a+1, c+1, b, xs, ys, zs);
                        current = voxels[a][c][b] & 255;
                        if(current != 0)
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
                                        working[px+sx][py+sy] = 8;
                                        depths[px+sx][py+sy] = 256 + b * 5 - a * 2;// + sy;
                                    }
                                    for (int sy = 3; sy < 5; sy++) {
                                        working[px+sx][py+sy] = 8;
                                        depths[px+sx][py+sy] = 255 + b * 5 - a * 2;// + sy;
                                    }
                                }
                                working[px][py] = 14;
                                working[px+1][py] = 14;
                                working[px][py+1] = 14;
                                working[px+1][py+1] = 14;
//                                working[px][py+2] = 14;
//                                working[px+1][py+2] = 14;
                            }
                            else
                            {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 0; sy < 3; sy++) {
                                        working[px+sx][py+sy] = current + 1;
                                        depths[px+sx][py+sy] = 256 + b * 5 - a * 2;// + sy;
                                    }
                                    for (int sy = 3; sy < 5; sy++) {
                                        working[px+sx][py+sy] = current;
                                        depths[px+sx][py+sy] = 255 + b * 5 - a * 2;// + sy;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        //working = easeSquares(working);
        int d, w;
        render = ArrayTools.copy(working);
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                if((w = clampDown(working[x][y])) > 3) {
                    d = depths[x][y];
                    if (working[x - 1][y] == 0 && working[x][y - 1] == 0)      { render[x][y] = 0; }
                    else if (working[x + 1][y] == 0 && working[x][y - 1] == 0) { render[x][y] = 0; }
                    else if (working[x - 1][y] == 0 && working[x][y + 1] == 0) { render[x][y] = 0; }
                    else if (working[x + 1][y] == 0 && working[x][y + 1] == 0) { render[x][y] = 0; }
                    else {
                        /* if (working[x - 1][y] == 0) { render[x - 1][y] = 2; } else */ if (working[x - 1][y] == 0 || depths[x - 1][y] < d - 2) { render[x][y] = w; }
                        /* if (working[x + 1][y] == 0) { render[x + 1][y] = 2; } else */ if (working[x + 1][y] == 0 || depths[x + 1][y] < d - 2) { render[x][y] = w; }
                        /* if (working[x][y - 1] == 0) { render[x][y - 1] = 2; } else */ if (working[x][y - 1] == 0 || depths[x][y - 1] < d - 5) { render[x][y] = w; }
                        /* if (working[x][y + 1] == 0) { render[x][y + 1] = 2; } else */ if (working[x][y + 1] == 0 || depths[x][y + 1] < d - 5) { render[x][y] = w; }
                    }
                }
            }
        }
        return render;
//        return easeSquares(render);
    }
    public static int clamp (int value, int min, int max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    public static int clampAlter(int color, int change)
    {
        return (color & -8) + clamp((color & 7) + change, 0, 7);
    }

    public static int clampDown(int color)
    {
        return (color & -8) + clamp((color & 7) - 1, 0, 7);
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
        int[][] working = makeRenderArray(xs, ys, zs, 4, 4, 1);
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
                    //px = px - 1 << 1;
                    //py = (py - 2 << 1) + 1;
                    current = voxels[c][a][b] & 255;
                    if (current != 0) {
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
                                        working[px + ix][py + iy] = 14;
                                        depths[px + ix][py + iy] = d + ((ix ^ ix >>> 1) & 1); // adds 1 only in center of a voxel
                                    }
                                    else
                                    {
                                        working[px + ix][py + iy] = 8;
                                        depths[px + ix][py + iy] = d + ((ix ^ ix >>> 1) & 1); // adds 1 only in center of a voxel
                                    }
                                }
                            }
                        } else {
                            for (int ix = 0; ix < 4; ix++) {
                                for (int iy = 0; iy < 2; iy++) {
                                    working[px + ix][py + iy] = current + 1;
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
                                    working[px + ix][py + iy] = current - 1;
                                    depths[px + ix][py + iy] = d + ((ix ^ ix >>> 1) & 1); // adds 1 only in center of a voxel
                                }
                            }
                        }
                    }
                }
            }
        }
        //working = easeSquares(working);
        //int[][] shaded = ArrayTools.fill(65535, 52, 64);

        render = ArrayTools.copy(working);
//        for (int x = 1; x < 51; x++) {
//            for (int y = 1; y < 63; y++) {
//                if((w = working[x][y] - 1) > 3) {
//                    d = depths[x][y];
//                    if (working[x - 1][y] == 0 && working[x][y - 1] == 0)      { render[x][y] = 2; }
//                    else if (working[x + 1][y] == 0 && working[x][y - 1] == 0) { render[x][y] = 2; }
//                    else if (working[x - 1][y] == 0 && working[x][y + 1] == 0) { render[x][y] = 2; }
//                    else if (working[x + 1][y] == 0 && working[x][y + 1] == 0) { render[x][y] = 2; }
//                    else {
//                        if (working[x - 1][y] == 0) { render[x - 1][y] = 2; } else if ((working[x][y] & 7) > (w & 7) && depths[x - 1][y] < d - 5) { render[x][y] = w; }
//                        if (working[x + 1][y] == 0) { render[x + 1][y] = 2; } else if ((working[x][y] & 7) > (w & 7) && depths[x + 1][y] < d - 5) { render[x][y] = w; }
//                        if (working[x][y - 1] == 0) { render[x][y - 1] = 2; } else if ((working[x][y] & 7) > (w & 7) && depths[x][y - 1] < d - 5) { render[x][y] = w; }
//                        if (working[x][y + 1] == 0) { render[x][y + 1] = 2; } else if ((working[x][y] & 7) > (w & 7) && depths[x][y + 1] < d - 5) { render[x][y] = w; }
//                    }
//                }
//            }
//        }

        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                if((w = clampDown(working[x][y])) > 3) {
                    d = depths[x][y];
                    if (working[x - 1][y] == 0 && working[x][y - 1] == 0)      { render[x][y] = 0; }
                    else if (working[x + 1][y] == 0 && working[x][y - 1] == 0) { render[x][y] = 0; }
                    else if (working[x - 1][y] == 0 && working[x][y + 1] == 0) { render[x][y] = 0; }
                    else if (working[x + 1][y] == 0 && working[x][y + 1] == 0) { render[x][y] = 0; }
                    else {
                        /* if (working[x - 1][y] == 0) { render[x - 1][y] = 2; } else */ if (working[x - 1][y] == 0 || depths[x - 1][y] < d - 9) { render[x][y] = w; }
                        /* if (working[x + 1][y] == 0) { render[x + 1][y] = 2; } else */ if (working[x + 1][y] == 0 || depths[x + 1][y] < d - 9) { render[x][y] = w; }
                        /* if (working[x][y - 1] == 0) { render[x][y - 1] = 2; } else */ if (working[x][y - 1] == 0 || depths[x][y - 1] < d - 9) { render[x][y] = w; }
                        /* if (working[x][y + 1] == 0) { render[x][y + 1] = 2; } else */ if (working[x][y + 1] == 0 || depths[x][y + 1] < d - 9) { render[x][y] = w; }
                    }
                }
            }
        }
//        return render;
        return easeSquares(render, working);
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
                    if (current != 0) {
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
                                        working[px + ix][py + iy] = 14;
                                        depths[px + ix][py + iy] = d + (ix & ix >>> 1); // adds 1 only on the right edge of a voxel
                                    }
                                    else
                                    {
                                        working[px + ix][py + iy] = 8;
                                        depths[px + ix][py + iy] = d + (ix & ix >>> 1); // adds 1 only on the right edge of a voxel
                                    }
                                }
                            }
                        } else {
                            for (int ix = 0; ix < 4; ix++) {
                                for (int iy = 0; iy < 2; iy++) {
                                    working[px + ix][py + iy] = current + 1;
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
                                    working[px + ix][py + iy] = current - 1;
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
        //int[][] shaded = ArrayTools.fill(65535, 52, 64);
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
                             if (           (working[x][y] & 7) > (w & 7) && depths[x - 1][y] < d - 5) { render[rx][ry] = w; }
                        else if (           (working[x][y] & 7) > (w & 7) && depths[x + 2][y] < d - 6) { render[rx][ry] = w; }
                        else if (           (working[x][y] & 7) > (w & 7) && depths[x][y - 2] < d - 5) { render[rx][ry] = w; }
                        else if (y >= 62 || (working[x][y] & 7) > (w & 7) && depths[x][y + 1] < d - 5) { render[rx][ry] = w; }
                    }
                }
            }
        }

//        for (int x = 1; x < 51; x++) {
//            for (int y = 1; y < 63; y++) {
//                if((w = working[x][y] - 1) > 3) {
//                    d = depths[x][y];
//                    if (working[x - 1][y] == 0 && working[x][y - 1] == 0)      { render[x][y] = 2; }
//                    else if (working[x + 1][y] == 0 && working[x][y - 1] == 0) { render[x][y] = 2; }
//                    else if (working[x - 1][y] == 0 && working[x][y + 1] == 0) { render[x][y] = 2; }
//                    else if (working[x + 1][y] == 0 && working[x][y + 1] == 0) { render[x][y] = 2; }
//                    else {
//                        if (working[x - 1][y] == 0) { render[x - 1][y] = 2; } else if ((working[x - 1][y]) > (w) && depths[x - 1][y] < d - 5) { render[x - 1][y] = w; }
//                        if (working[x + 1][y] == 0) { render[x + 1][y] = 2; } else if ((working[x + 1][y]) > (w) && depths[x + 1][y] < d - 5) { render[x + 1][y] = w; }
//                        if (working[x][y - 1] == 0) { render[x][y - 1] = 2; } else if ((working[x][y - 1]) > (w) && depths[x][y - 1] < d - 5) { render[x][y - 1] = w; }
//                        if (working[x][y + 1] == 0) { render[x][y + 1] = 2; } else if ((working[x][y + 1]) > (w) && depths[x][y + 1] < d - 5) { render[x][y + 1] = w; }
//                    }
//                }
//            }
//        }



//        for (int x = 1; x < 51; x++) {
//            for (int y = 1; y < 63; y++) {
//                if((w = working[x][y] - 1) > 3) {
//                    d = depths[x][y];
//                    if (working[x - 1][y] == 0 && working[x][y - 1] == 0)      { shaded[x][y] = render[x][y] = 2; }
//                    else if (working[x + 1][y] == 0 && working[x][y - 1] == 0) { shaded[x][y] = render[x][y] = 2; }
//                    else if (working[x - 1][y] == 0 && working[x][y + 1] == 0) { shaded[x][y] = render[x][y] = 2; }
//                    else if (working[x + 1][y] == 0 && working[x][y + 1] == 0) { shaded[x][y] = render[x][y] = 2; }
//                    else {
//                        if (working[x - 1][y] == 0) { shaded[x - 1][y] = render[x - 1][y] = 2; } else if ((shaded[x - 1][y] & 7) > (w & 7) && depths[x - 1][y] < d - 9 + (x & 1)) { shaded[x - 1][y] = render[x - 1][y] = w; }
//                        if (working[x + 1][y] == 0) { shaded[x + 1][y] = render[x + 1][y] = 2; } else if ((shaded[x + 1][y] & 7) > (w & 7) && depths[x + 1][y] < d - 9 + (x & 1)) { shaded[x + 1][y] = render[x + 1][y] = w; }
//                        if (working[x][y - 1] == 0) { shaded[x][y - 1] = render[x][y - 1] = 2; } else if ((shaded[x][y - 1] & 7) > (w & 7) && depths[x][y - 1] < d - 4)           { shaded[x][y - 1] = render[x][y - 1] = w; }
//                        if (working[x][y + 1] == 0) { shaded[x][y + 1] = render[x][y + 1] = 2; } else if ((shaded[x][y + 1] & 7) > (w & 7) && depths[x][y + 1] < d - 4)           { shaded[x][y + 1] = render[x][y + 1] = w; }
//                    }
//                }
//            }
//        }


//        for (int x = 1; x < 59; x++) {
//            for (int y = 1; y < 67; y++) {
//                if((w = working[x][y] - 1) > 3) {
//                    d = depths[x][y];
//                    if (working[x - 1][y] == 0 && working[x][y - 1] == 0) render[x][y] = 2;
//                    else if (working[x + 1][y] == 0 && working[x][y - 1] == 0) render[x][y] = 2;
//                    else if (working[x - 1][y] == 0 && working[x][y + 1] == 0) render[x][y] = 2;
//                    else if (working[x + 1][y] == 0 && working[x][y + 1] == 0) render[x][y] = 2;
//                    else {
//                        if (working[x - 1][y] == 0) render[x - 1][y] = 2;
//                        else if (depths[x - 1][y] < d - 8) render[x - 1][y] = w;
//                        if (working[x + 1][y] == 0) render[x + 1][y] = 2;
//                        else if (depths[x + 1][y] < d - 8) render[x + 1][y] = w;
//                        if (working[x][y - 1] == 0) render[x][y - 1] = 2;
//                        else if (depths[x][y - 1] < d - 8) render[x][y - 1] = w;
//                        if (working[x][y + 1] == 0) render[x][y + 1] = 2;
//                        else if (depths[x][y + 1] < d - 8) render[x][y + 1] = w;
//                    }
//                }
//            }
//        }

        return render;
    }
    public int[][] renderOrthoBelow(byte[][][] voxels, int direction)
    {
        final int xs = voxels.length, ys = voxels[0].length, zs = voxels[0][0].length;
        VariableConverter con = directionsOrthoV[direction &= 3];
        int[][] working = makeRenderArray(xs, ys, zs, 3, 5, 1);
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
                cc = xs - 1;
                cStep = -1;
                cMax = -1;

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
                cc = 0;
                cStep = 1;
                cMax = xs;

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
            for (int b = zs - 1; b >= 0; b--) {
                for (int a = aa; a != aMax; a += aStep) {
                    for (int c = cc; c != cMax; c += cStep) {
                        px = con.voxelToPixelX(c + 1, a + 1, b, xs, ys, zs);
                        py = con.voxelToPixelY(c + 1, a + 1, b, xs, ys, zs);
                        current = voxels[xs - 1 - c][a][b] & 255;
                        if (current != 0) {
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
                                        working[px+sx][py+sy] = 8;
                                        depths[px+sx][py+sy] = 254 + b * 5 - c * 2;// + sy;
                                    }
                                    for (int sy = 2; sy < 5; sy++) {
                                        working[px+sx][py+sy] = 8;
                                        depths[px+sx][py+sy] = 255 + b * 5 - c * 2;// + sy;
                                    }
                                }
                                working[px][py] = 14;
                                working[px+1][py] = 14;
                                working[px][py+1] = 14;
                                working[px+1][py+1] = 14;
                            }
                            else
                            {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 2; sy < 5; sy++) {
                                        working[px+sx][py+sy] = current - 1;
                                        depths[px+sx][py+sy] = 254 + b * 5 - c * 2;// + sy;
                                    }
                                    for (int sy = 0; sy < 2; sy++) {
                                        working[px+sx][py+sy] = current + 1;
                                        depths[px+sx][py+sy] = 255 + b * 5 - c * 2;// + sy;
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
                    for (int c = cc; c != cMax; c += cStep) {
                        px = con.voxelToPixelX(a + 1, c + 1, b, xs, ys, zs);
                        py = con.voxelToPixelY(a + 1, c + 1, b, xs, ys, zs);
                        current = voxels[a][ys - 1 - c][b] & 255;
                        if(current != 0)
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
                                        working[px+sx][py+sy] = 8;
                                        depths[px+sx][py+sy] = 254 + b * 5 - a * 2;// + sy;
                                    }
                                    for (int sy = 2; sy < 5; sy++) {
                                        working[px+sx][py+sy] = 8;
                                        depths[px+sx][py+sy] = 255 + b * 5 - a * 2;// + sy;
                                    }
                                }
                                working[px][py] = 14;
                                working[px+1][py] = 14;
                                working[px][py+1] = 14;
                                working[px+1][py+1] = 14;
                            }
                            else
                            {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 2; sy < 5; sy++) {
                                        working[px+sx][py+sy] = current - 1;
                                        depths[px+sx][py+sy] = 254 + b * 5 - a * 2;// + sy;
                                    }
                                    for (int sy = 0; sy < 2; sy++) {
                                        working[px+sx][py+sy] = current + 1;
                                        depths[px+sx][py+sy] = 255 + b * 5 - a * 2;// + sy;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        //working = easeSquares(working);
        int d, w;
        render = ArrayTools.copy(working);
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                if((w = clampDown(working[x][y])) > 3) {
                    d = depths[x][y];
                    if (working[x - 1][y] == 0 && working[x][y - 1] == 0)      { render[x][y] = 0; }
                    else if (working[x + 1][y] == 0 && working[x][y - 1] == 0) { render[x][y] = 0; }
                    else if (working[x - 1][y] == 0 && working[x][y + 1] == 0) { render[x][y] = 0; }
                    else if (working[x + 1][y] == 0 && working[x][y + 1] == 0) { render[x][y] = 0; }
                    else {
                        /* if (working[x - 1][y] == 0) { render[x - 1][y] = 2; } else */ if (working[x - 1][y] == 0 || depths[x - 1][y] < d - 2) { render[x][y] = w; }
                        /* if (working[x + 1][y] == 0) { render[x + 1][y] = 2; } else */ if (working[x + 1][y] == 0 || depths[x + 1][y] < d - 2) { render[x][y] = w; }
                        /* if (working[x][y - 1] == 0) { render[x][y - 1] = 2; } else */ if (working[x][y - 1] == 0 || depths[x][y - 1] < d - 5) { render[x][y] = w; }
                        /* if (working[x][y + 1] == 0) { render[x][y + 1] = 2; } else */ if (working[x][y + 1] == 0 || depths[x][y + 1] < d - 5) { render[x][y] = w; }
                    }
                }
            }
        }
        return render;
        //return easeSquares(render, working);
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
        int[][] working = makeRenderArray(xs, ys, zs, 4, 4, 1);
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
//                    px = px - 1 << 1;
//                    py = (py - 2 << 1) + 1;
                    current = voxels[c][a][b] & 255;
                    if (current != 0) {
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
                                        working[px + ix][py + iy] = 14;
                                        depths[px + ix][py + iy] = d + ((ix ^ ix >>> 1) & 1); // adds 1 only in center of a voxel
                                    }
                                    else
                                    {
                                        working[px + ix][py + iy] = 8;
                                        depths[px + ix][py + iy] = d + ((ix ^ ix >>> 1) & 1); // adds 1 only in center of a voxel
                                    }
                                }
                            }
                        } else {
                            for (int ix = 0; ix < 4; ix++) {
                                for (int iy = 2; iy < 4; iy++) {
                                    working[px + ix][py + iy] = current - 1;
                                    depths[px + ix][py + iy] = d + ((ix ^ ix >>> 1) & 1); // adds 1 only in center of a voxel
                                }
                            }
                            for (int ix = 0; ix < 2; ix++) {
                                for (int iy = 0; iy < 2; iy++) {
                                    working[px + ix][py + iy] = current + 1;
                                    depths[px + ix][py + iy] = d + ((ix ^ ix >>> 1) & 1); // adds 1 only in center of a voxel
                                }
                            }
                            for (int ix = 2; ix < 4; ix++) {
                                for (int iy = 0; iy < 2; iy++) {
                                    working[px + ix][py + iy] = current;
                                    depths[px + ix][py + iy] = d + ((ix ^ ix >>> 1) & 1); // adds 1 only in center of a voxel
                                }
                            }
                        }
                    }
                }
            }
        }
        //working = easeSquares(working);
        //int[][] shaded = ArrayTools.fill(65535, 52, 64);

        render = ArrayTools.copy(working);
//        for (int x = 1; x < 51; x++) {
//            for (int y = 1; y < 63; y++) {
//                if((w = working[x][y] - 1) > 3) {
//                    d = depths[x][y];
//                    if (working[x - 1][y] == 0 && working[x][y - 1] == 0)      { render[x][y] = 2; }
//                    else if (working[x + 1][y] == 0 && working[x][y - 1] == 0) { render[x][y] = 2; }
//                    else if (working[x - 1][y] == 0 && working[x][y + 1] == 0) { render[x][y] = 2; }
//                    else if (working[x + 1][y] == 0 && working[x][y + 1] == 0) { render[x][y] = 2; }
//                    else {
//                        if (working[x - 1][y] == 0) { render[x - 1][y] = 2; } else if ((working[x][y] & 7) > (w & 7) && depths[x - 1][y] < d - 5) { render[x][y] = w; }
//                        if (working[x + 1][y] == 0) { render[x + 1][y] = 2; } else if ((working[x][y] & 7) > (w & 7) && depths[x + 1][y] < d - 5) { render[x][y] = w; }
//                        if (working[x][y - 1] == 0) { render[x][y - 1] = 2; } else if ((working[x][y] & 7) > (w & 7) && depths[x][y - 1] < d - 5) { render[x][y] = w; }
//                        if (working[x][y + 1] == 0) { render[x][y + 1] = 2; } else if ((working[x][y] & 7) > (w & 7) && depths[x][y + 1] < d - 5) { render[x][y] = w; }
//                    }
//                }
//            }
//        }

        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                if((w = clampDown(working[x][y])) > 3) {
                    d = depths[x][y];
                    if (working[x - 1][y] == 0 && working[x][y - 1] == 0)      { render[x][y] = 0; }
                    else if (working[x + 1][y] == 0 && working[x][y - 1] == 0) { render[x][y] = 0; }
                    else if (working[x - 1][y] == 0 && working[x][y + 1] == 0) { render[x][y] = 0; }
                    else if (working[x + 1][y] == 0 && working[x][y + 1] == 0) { render[x][y] = 0; }
                    else {
                        /* if (working[x - 1][y] == 0) { render[x - 1][y] = 2; } else */ if (working[x - 1][y] == 0 || depths[x - 1][y] < d - 9) { render[x][y] = w; }
                        /* if (working[x + 1][y] == 0) { render[x + 1][y] = 2; } else */ if (working[x + 1][y] == 0 || depths[x + 1][y] < d - 9) { render[x][y] = w; }
                        /* if (working[x][y - 1] == 0) { render[x][y - 1] = 2; } else */ if (working[x][y - 1] == 0 || depths[x][y - 1] < d - 9) { render[x][y] = w; }
                        /* if (working[x][y + 1] == 0) { render[x][y + 1] = 2; } else */ if (working[x][y + 1] == 0 || depths[x][y + 1] < d - 9) { render[x][y] = w; }
                    }
                }
            }
        }
        return easeSquares(render, working);
        //return easeSquares(render);
    }

    public int[][] renderOrthoSide(byte[][][] voxels, int direction)
    {
        final int xs = voxels.length, ys = voxels[0].length, zs = voxels[0][0].length;
        VariableConverter con = directionsOrthoSideV[direction &= 3];
        int[][] working = makeRenderArray(xs, ys, zs, 3, 4, 1);
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
            for (int a = aa; a != aMax; a += aStep) {
                for (int b = 0; b < zs; b++) {
                    for (int c = cc; c != cMax; c += cStep) {
                        px = con.voxelToPixelX(c + 1, a + 1, b, xs, ys, zs);
                        py = con.voxelToPixelY(c + 1, a + 1, b, xs, ys, zs);
                        current = voxels[c][a][b] & 255;
                        if (current != 0) {
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
                                        working[px+sx][py+sy] = 8;
                                        depths[px+sx][py+sy] = 256 - c * 2;
                                    }                                     
                                    working[px+sx][py] = 8;
                                    depths[px+sx][py] = 256 - c * 2;
                                    
                                }
                                working[px][py] = 14;
                                working[px+1][py] = 14;
                                working[px][py+1] = 14;
                                working[px+1][py+1] = 14;
                            }
                            else
                            {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 1; sy < 4; sy++) {
                                        working[px+sx][py+sy] = current;
                                        depths[px+sx][py+sy] = 256 - c * 2;
                                    }
                                    working[px+sx][py] = current + 1;
                                    depths[px+sx][py] = 256 - c * 2;
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
                        if(current != 0)
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
                                        working[px+sx][py+sy] = 8;
                                        depths[px+sx][py+sy] = 256 - a * 2;
                                    }
                                    working[px+sx][py] = 8;
                                    depths[px+sx][py] = 256 - a * 2;

                                }
                                working[px][py] = 14;
                                working[px+1][py] = 14;
                                working[px][py+1] = 14;
                                working[px+1][py+1] = 14;
                            }
                            else
                            {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 1; sy < 4; sy++) {
                                        working[px+sx][py+sy] = current;
                                        depths[px+sx][py+sy] = 256 - a * 2;
                                    }
                                    working[px+sx][py] = current + 1;
                                    depths[px+sx][py] = 256 - a * 2;
                                }
                            }
                        }
                    }
                }
            }
        }
        //working = easeSquares(working);
        int d, w;
        render = ArrayTools.copy(working);
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                if((w = clampDown(working[x][y])) > 3) {
                    d = depths[x][y];
                    if (working[x - 1][y] == 0 && working[x][y - 1] == 0)      { render[x][y] = 0; }
                    else if (working[x + 1][y] == 0 && working[x][y - 1] == 0) { render[x][y] = 0; }
                    else if (working[x - 1][y] == 0 && working[x][y + 1] == 0) { render[x][y] = 0; }
                    else if (working[x + 1][y] == 0 && working[x][y + 1] == 0) { render[x][y] = 0; }
                    else {
                        /* if (working[x - 1][y] == 0) { render[x - 1][y] = 2; } else */ if (working[x - 1][y] == 0 || depths[x - 1][y] < d - 2) { render[x][y] = w; }
                        /* if (working[x + 1][y] == 0) { render[x + 1][y] = 2; } else */ if (working[x + 1][y] == 0 || depths[x + 1][y] < d - 2) { render[x][y] = w; }
                        /* if (working[x][y - 1] == 0) { render[x][y - 1] = 2; } else */ if (working[x][y - 1] == 0 || depths[x][y - 1] < d - 2) { render[x][y] = w; }
                        /* if (working[x][y + 1] == 0) { render[x][y + 1] = 2; } else */ if (working[x][y + 1] == 0 || depths[x][y + 1] < d - 2) { render[x][y] = w; }
                    }
                }
            }
        }
        return render;
//        return easeSquares(render);
    }
    public int[][] renderIsoSide(byte[][][] voxels, int direction) {
        final int xs = voxels.length, ys = voxels[0].length, zs = voxels[0][0].length;
        VariableConverter con = directionsIsoSideV[direction &= 3];
        int[][] working = makeRenderArray(xs, ys, zs, 4, 4, 1);
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
//                    px = px - 1 << 1;
//                    py = (py - 2 << 1) + 1;
                    current = voxels[c][a][b] & 255;
                    if (current != 0) {
                        d = 3 * (c * cChange - a) + 256;
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
                                        working[px + ix][py + iy] = 14;
                                        depths[px + ix][py + iy] = d + (ix & ix >>> 1); // adds 1 only on the right edge of a voxel
                                    }
                                    else
                                    {
                                        working[px + ix][py + iy] = 8;
                                        depths[px + ix][py + iy] = d + (ix & ix >>> 1); // adds 1 only on the right edge of a voxel
                                    }
                                }
                            }
                        } else {
                            for (int ix = 0; ix < 4; ix++) {
                                //for (int iy = 0; iy < 2; iy++) {
                                    working[px + ix][py] = current + 1;
                                    depths[px + ix][py] = d + (ix & ix >>> 1); // adds 1 only on the right edge of a voxel
                                //}
                            }
                            for (int ix = 0; ix < 2; ix++) {
                                for (int iy = 1; iy < 4; iy++) {
                                    working[px + ix][py + iy] = current;
                                    depths[px + ix][py + iy] = d + ix; // adds 1 only in center of voxel
                                }
                            }
                            for (int ix = 2; ix < 4; ix++) {
                                for (int iy = 1; iy < 4; iy++) {
                                    working[px + ix][py + iy] = current - 1;
                                    depths[px + ix][py + iy] = d - ix + 3; // adds 1 only in center of voxel
                                }
                            }
                        }
                    }
                }
            }
        }
        //working = easeSquares(working);
        //int[][] shaded = ArrayTools.fill(65535, 52, 64);

        render = ArrayTools.copy(working);
//        for (int x = 1; x < 51; x++) {
//            for (int y = 1; y < 63; y++) {
//                if((w = working[x][y] - 1) > 3) {
//                    d = depths[x][y];
//                    if (working[x - 1][y] == 0 && working[x][y - 1] == 0)      { render[x][y] = 2; }
//                    else if (working[x + 1][y] == 0 && working[x][y - 1] == 0) { render[x][y] = 2; }
//                    else if (working[x - 1][y] == 0 && working[x][y + 1] == 0) { render[x][y] = 2; }
//                    else if (working[x + 1][y] == 0 && working[x][y + 1] == 0) { render[x][y] = 2; }
//                    else {
//                        if (working[x - 1][y] == 0) { render[x - 1][y] = 2; } else if ((working[x][y] & 7) > (w & 7) && depths[x - 1][y] < d - 5) { render[x][y] = w; }
//                        if (working[x + 1][y] == 0) { render[x + 1][y] = 2; } else if ((working[x][y] & 7) > (w & 7) && depths[x + 1][y] < d - 5) { render[x][y] = w; }
//                        if (working[x][y - 1] == 0) { render[x][y - 1] = 2; } else if ((working[x][y] & 7) > (w & 7) && depths[x][y - 1] < d - 5) { render[x][y] = w; }
//                        if (working[x][y + 1] == 0) { render[x][y + 1] = 2; } else if ((working[x][y] & 7) > (w & 7) && depths[x][y + 1] < d - 5) { render[x][y] = w; }
//                    }
//                }
//            }
//        }

        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                if((w = clampDown(working[x][y])) > 3) {
                    d = depths[x][y];
                    if (working[x - 1][y] == 0 && working[x][y - 1] == 0)      { render[x][y] = 0; }
                    else if (working[x + 1][y] == 0 && working[x][y - 1] == 0) { render[x][y] = 0; }
                    else if (working[x - 1][y] == 0 && working[x][y + 1] == 0) { render[x][y] = 0; }
                    else if (working[x + 1][y] == 0 && working[x][y + 1] == 0) { render[x][y] = 0; }
                    else {
                        /* if (working[x - 1][y] == 0) { render[x - 1][y] = 2; } else */ if (working[x - 1][y] == 0 || depths[x - 1][y] < d - 5) { render[x][y] = w; }
                        /* if (working[x + 1][y] == 0) { render[x + 1][y] = 2; } else */ if (working[x + 1][y] == 0 || depths[x + 1][y] < d - 5) { render[x][y] = w; }
                        /* if (working[x][y - 1] == 0) { render[x][y - 1] = 2; } else */ if (working[x][y - 1] == 0 || depths[x][y - 1] < d - 5) { render[x][y] = w; }
                        /* if (working[x][y + 1] == 0) { render[x][y + 1] = 2; } else */ if (working[x][y + 1] == 0 || depths[x][y + 1] < d - 5) { render[x][y] = w; }
                    }
                }
            }
        }
        return easeSquares(render, working);
        //return easeSquares(render);
    }


    public int[][] size2(int[][] original)
    {
        int xSize = original.length - 2, ySize = original[0].length - 2;
        int[][] out = new int[xSize * 2][ySize * 2];
        for (int x = 1, xx = 0; x < xSize; x++, xx += 2) {
            for (int y = 2, yy = 1; y <= ySize; y++, yy += 2) {
                out[xx][yy] = out[xx+1][yy] = out[xx][yy+1] = out[xx+1][yy+1] = original[x][y];
            }
        }
        return out;
    }

    public int[][] size2Mix(int[][] original)
    {
        int xSize = original.length - 2, ySize = original[0].length - 2, ne, nw, se, sw;
        int[][] out = new int[xSize * 2][ySize * 2];
        //int toggle = 8;
        for (int x = 1, xx = 0; x <= xSize; x++, xx += 2) {
            for (int y = 1, yy = 0; y <= ySize; y++, yy += 2) {
                nw = original[x][y];
                if(nw <= 0)
                {
                    out[xx][yy] = 0;
                    out[xx+1][yy] = 0;
                    out[xx][yy+1] = 0;
                    out[xx+1][yy+1] = 0;
                }
                else {
                    out[xx][yy] = nw;
                    ne = original[x + 1][y];
                    sw = original[x][y + 1];
                    se = original[x + 1][y + 1];
                    out[xx + 1][yy] = (ne > 0) ? nw + ne >> 1 : nw;
                    out[xx][yy + 1] = (sw > 0) ? nw + sw >> 1 : nw;
                    out[xx + 1][yy + 1] = (se > 0) ? nw + se >> 1 : nw - 4;
                }
            }
        }
        return out;
    }

    public int[][] easeSquares(int[][] original, int[][] out){
        int xSize = original.length - 1, ySize = original[0].length - 1;
//        for (int i = 0; i < original.length; i++) {
//            System.arraycopy(original[i], 0, out[i], 0, original[0].length);
//        }
        int o, a, b, c, d;
        for (int x = 1; x < xSize; x++) {
            for (int y = 1; y < ySize; y++) {
                o = original[x][y];
                a = original[x - 1][y - 1];
                b = original[x + 1][y - 1];
                c = original[x - 1][y + 1];
                d = original[x + 1][y + 1];
                if (o > 2 && a > 2 && b > 2 && c > 2 && d > 2) {
                    if (a == d && a > o) out[x][y] = a;
                    else if (b == c && b > o) out[x][y] = b;
                    else out[x][y] = o;
                }
                else out[x][y] = o;
            }
        }
        return out;
    }
    
    // not the same algo as 2-arg easeSquares; this can completely remove small details...
    public int[][] easeSquares(int[][] original){
        int xSize = original.length - 1, ySize = original[0].length - 1, idx, bestIdx, bestCount;
        int[][] out = ArrayTools.copy(original);
        int[] colors = new int[9], counts = new int[9];
        for (int x = 1; x < xSize; x++) {
            CELL:
            for (int y = 1; y < ySize; y++) {
                Arrays.fill(colors, 0);
                Arrays.fill(counts, -1);
                for (int xx = x-1, i = 0; i < 3; i++, xx++) {
                    for (int yy = y - 1, j = 0; j < 3; j++, yy++) {
                        if((colors[idx = i * 3 + j] = original[xx][yy]) <= 2) continue CELL;
                        for (int c = 0; c <= idx; c++) {
                            if(colors[idx] == colors[c])
                            {
                                counts[c]++;
                                break;
                            }
                        }
                    }
                }
                bestIdx = 0;
                bestCount = -1;
                for (int i = 0; i < 9; i++) {
                    if(counts[i] > bestCount)
                    {
                        bestIdx = i;
                        bestCount = counts[i];
                    }
                }
                if((colors[bestIdx] & 7) > (out[x][y] & 7))
                    out[x][y] = colors[bestIdx];
//                c = original[x][y];
//                e = original[x + 1][y];
//                n = original[x][y + 1];
//                w = original[x - 1][y];
//                s = original[x][y - 1];
//                ne = original[x + 1][y + 1];
//                se = original[x + 1][y - 1];
//                nw = original[x - 1][y + 1];
//                sw = original[x - 1][y - 1];
//                if (c > 2 && e > 2 && n > 2 && s > 2 && w > 2) {
//                         if (c == e && e == n /* && (d & -8) == (c & -8) */) out[x + 1][y + 1] = n;
//                    else if (c == e && e == ne /* && (c & -8) == (d & -8) */) out[x][y + 1] = ne;
//                    else if (c == n && n == ne /* && (b & -8) == (d & -8) */) out[x + 1][y] = ne;
//                    else if (e == n && n == ne /* && (a & -8) == (d & -8) */) out[x][y] = ne;
//                }
            }
        }
        return out;
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
                    return vy * 3 + 4;
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return (vx * 3) + (zs - vz) * 2;
                }
            },
            // direction 1
            new VariableConverter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return 7 + (xs - vx) * 3;
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return (vy * 3) + (zs - vz) * 2;
                }
            },
            // direction 2
            new VariableConverter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return 7 + (ys - vy) * 3;
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return 3 + (xs - vx) * 3 + (zs - vz) * 2;
                }
            },
            // direction 3
            new VariableConverter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return vx * 3 + 4;
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return 3 + (ys - vy) * 3 + (zs - vz) * 2;
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
                    return vy * 3 + 4;
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
                    return 7 + (xs - vx) * 3;
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
                    return 7 + (ys - vy) * 3;
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
                    return vx * 3 + 4;
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
                w = Math.max(w, vc.voxelToPixelX(0, 0, 0, xs, ys, zs) + voxelWidth);
                h = Math.max(h, vc.voxelToPixelY(0, 0, 0, xs, ys, zs) + voxelHeight);
                w = Math.max(w, vc.voxelToPixelX(xs, 0, 0, xs, ys, zs) + voxelWidth);
                h = Math.max(h, vc.voxelToPixelY(xs, 0, 0, xs, ys, zs) + voxelHeight);
                w = Math.max(w, vc.voxelToPixelX(0, ys, 0, xs, ys, zs) + voxelWidth);
                h = Math.max(h, vc.voxelToPixelY(0, ys, 0, xs, ys, zs) + voxelHeight);
                w = Math.max(w, vc.voxelToPixelX(xs, ys, 0, xs, ys, zs) + voxelWidth);
                h = Math.max(h, vc.voxelToPixelY(xs, ys, 0, xs, ys, zs) + voxelHeight);
                w = Math.max(w, vc.voxelToPixelX(0, 0, zs - 1, xs, ys, zs) + voxelWidth);
                h = Math.max(h, vc.voxelToPixelY(0, 0, zs - 1, xs, ys, zs) + voxelHeight);
                w = Math.max(w, vc.voxelToPixelX(xs, 0, zs - 1, xs, ys, zs) + voxelWidth);
                h = Math.max(h, vc.voxelToPixelY(xs, 0, zs - 1, xs, ys, zs) + voxelHeight);
                w = Math.max(w, vc.voxelToPixelX(0, ys, zs - 1, xs, ys, zs) + voxelWidth);
                h = Math.max(h, vc.voxelToPixelY(0, ys, zs - 1, xs, ys, zs) + voxelHeight);
                w = Math.max(w, vc.voxelToPixelX(xs, ys, zs - 1, xs, ys, zs) + voxelWidth);
                h = Math.max(h, vc.voxelToPixelY(xs, ys, zs - 1, xs, ys, zs) + voxelHeight);
            }
        }
        return new int[w + (outline << 1)][h + (outline << 1)];
    }
}
