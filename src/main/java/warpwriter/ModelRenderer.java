package warpwriter;

import squidpony.ArrayTools;

/**
 * Created by Tommy Ettinger on 11/4/2017.
 */
public class ModelRenderer {
    public int[] palette;
    public ModelRenderer()
    {
        palette = Coloring.CW_PALETTE;
    }
    public ModelRenderer(int[] palette)
    {
        this.palette = palette;
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
                                    working[px][py] = current + 2;
                                else if (b == 0 || voxels[c][a][b - 1] == 0)
                                    working[px][py] = current;
                                else
                                    working[px][py] = current + 1;
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
                                    working[px][py] = current + 2;
                                else if(b == 0 || voxels[a][c][b-1] == 0)
                                    working[px][py] = current;
                                else
                                    working[px][py] = current + 1;
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
        Converter con = directionsOrtho60x68[direction &= 3];
        int[][] working = new int[60][68], depths = new int[60][68], render;
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
                                        depths[px+sx][py+sy] = b * 2 - c;
                                    }
                                    for (int sy = 3; sy < 5; sy++) {
                                        working[px+sx][py+sy] = 8;
                                        depths[px+sx][py+sy] = b * 2 - c;
                                    }
                                }
                                working[px][py+3] = 14;
                            }
                            else
                            {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 0; sy < 3; sy++) {
                                        working[px+sx][py+sy] = current + 2;
                                        depths[px+sx][py+sy] = b * 2 - c;
                                    }
                                    for (int sy = 3; sy < 5; sy++) {
                                        working[px+sx][py+sy] = current;
                                        depths[px+sx][py+sy] = b * 2 - c;
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
                        px = con.voxelToPixelX(a, c, b);
                        py = con.voxelToPixelY(a, c, b);
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
                                        depths[px+sx][py+sy] = b * 2 - c;
                                    }
                                    for (int sy = 3; sy < 5; sy++) {
                                        working[px+sx][py+sy] = 8;
                                        depths[px+sx][py+sy] = b * 2 - c;
                                    }
                                }
                                working[px][py+3] = 14;
                            }
                            else
                            {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 0; sy < 3; sy++) {
                                        working[px+sx][py+sy] = current + 2;
                                        depths[px+sx][py+sy] = b * 2 - a;
                                    }
                                    for (int sy = 3; sy < 5; sy++) {
                                        working[px+sx][py+sy] = current;
                                        depths[px+sx][py+sy] = b * 2 - a;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        int d;
        render = ArrayTools.copy(working);
        for (int x = 1; x < 59; x++) {
            for (int y = 1; y < 67; y++) {
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
     * Renders the given 3D voxel byte array, which should be no larger than 12x12x8, to a 32x42 2D int array storing
     * color indices, using the given direction to rotate the model's facing (from 0 to 3).
     * @param voxels a 3D byte array with each byte storing color information for a voxel.
     * @param direction a 90-degree-increment counter-clockwise direction, from 0 to 3.
     * @return a 2D int array storing the pixel indices for the rendered model
     */
    public int[][] renderIso(byte[][][] voxels, int direction) {
        final int xs = voxels.length, ys = voxels[0].length, zs = voxels[0][0].length;
        Converter con = directionsIso32x36[direction &= 3];
        int[][] working = new int[32][36], depths = new int[32][36], render;
        int px, py;
        int current;
        int cStart = direction < 2 ? 0 : xs - 1, cEnd = direction < 2 ? xs : -1, cChange = direction < 2 ? 1 : -1;
        for (int b = 0; b < zs; b++) {
//            for (int a = ys - 1; a >= 0; a--) {
//                for (int c = xs - 1; c >= 0; c--) {
            for (int c = cStart; c != cEnd; c += cChange) {
                for (int a = ys - 1; a >= 0; a--) {

                    px = con.voxelToPixelX(c, a, b);
                    py = con.voxelToPixelY(c, a, b);
                    current = voxels[c][a][b] & 255;
                    if (current != 0) {
                        if (current <= 2) {
                            working[px][py] = current;
                        } else if (current == 3) {
                            for (int ix = 0; ix < 2; ix++) {
                                for (int iy = 0; iy < 2; iy++) {
                                    if (working[px + ix][py + iy] == 0)
                                        working[px + ix][py + iy] = 3;
                                }
                            }
                        } else if (current == 4) {
                            working[px][py] = 14;
                            depths[px][py] = b + c - a;
                            working[px+1][py] = 8;
                            depths[px+1][py] = b + c - a;
                            working[px][py+1] = 8;
                            depths[px][py+1] = b + c - a;
                            working[px+1][py+1] = 8;
                            depths[px+1][py+1] = b + c - a;
                        } else {
                            //for (int iy = 0; iy < 2; iy++) {
                            for (int ix = 0; ix < 2; ix++) {
                                working[px + ix][py] = current + 2;
                                depths[px + ix][py] = b + c - a;
                            }
                            //}
                            //for (int iy = 2; iy < 4; iy++) {
                            working[px][py + 1] = current + 1;
                            depths[px][py + 1] = b + c - a;
                            //for (int ix = 2; ix < 4; ix++) {
                            working[px + 1][py + 1] = current;
                            depths[px + 1][py + 1] = b + c - a;
                            //}
                            //}
                        }
                    }
                }
            }
        }

        int d;
        working = size2(working);
        depths = size2(depths);
        render = ArrayTools.copy(working);
        for (int x = 1; x < 59; x++) {
            for (int y = 1; y < 67; y++) {
                if((working[x][y]) > 2)
                {
                    d = depths[x][y];
                    if(working[x-1][y] == 0) render[x-1][y] = 2; else if(depths[x-1][y] < d - 8) render[x-1][y] = working[x-1][y] - 1;
                    if(working[x+1][y] == 0) render[x+1][y] = 2; else if(depths[x+1][y] < d - 8) render[x+1][y] = working[x+1][y] - 1;
                    if(working[x][y-1] == 0) render[x][y-1] = 2; else if(depths[x][y-1] < d - 8) render[x][y-1] = working[x][y-1] - 1;
                    if(working[x][y+1] == 0) render[x][y+1] = 2; else if(depths[x][y+1] < d - 8) render[x][y+1] = working[x][y+1] - 1;
                }
            }
        }
        return render;
    }

    public int[][] size2(int[][] original)
    {
        int xSize = original.length - 2, ySize = original[0].length - 2;
        int[][] out = new int[xSize * 2][ySize * 2];
        for (int x = 0, xx = 0; x < xSize; x++, xx += 2) {
            for (int y = 0, yy = 0; y < ySize; y++, yy += 2) {
                out[xx][yy] = out[xx+1][yy] = out[xx][yy+1] = out[xx+1][yy+1] = original[x+1][y+1];
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

    /**
     * For a max of a 14x14x8 voxel space, produces pixel coordinates for a 16x16 area
     */
    protected Converter[] directions16x16 = {
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
    protected Converter[] directionsOrtho60x68 = {
            // direction 0, no rotation
            new Converter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz) {
                    return vy * 3 + 9;
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz) {
                    return (vx * 3) + 20 - vz * 2;
                }
            },
            // direction 1
            new Converter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz) {
                    return 48 - vx * 3;
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz) {
                    return 59 - (vy * 3) - vz * 2;
                }
            },
            // direction 2
            new Converter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz) {
                    return 48 - vy * 3;
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz) {
                    return 59 - (vx * 3) - vz * 2;
                }
            },
            // direction 3
            new Converter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz) {
                    return vx * 3 + 9;
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz) {
                    return (vy * 3) + 20 - vz * 2;
                }
            }
    };
    protected Converter[] directionsIso32x36 = {
            // direction 0, no rotation
            new Converter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz) {
                    return (vy + vx) + 1;
                    //return vy + 2;
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz) {
                    return (vx - vy - vz) + 21;
                    //return (vx >> 1) + 8 - vz;
                }
            },
            // direction 1
            new Converter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz) {
                    return (26 - vy - vx) + 1;
                    //return 13 - vy;
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz) {
                    return (vx - vy - vz) + 21; //(13 - vy) - (13 - vx)
                    //return 13 - (vx >> 1) - vz;
                }
            },
            // direction 2
            new Converter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz) {
                    return (vy - vx + 13) + 1;
                    //return 13 - vx;
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz) {
                    return (13 - vx - vy - vz) + 21;
                    //return 13 - (vy >> 1) - vz;
                }
            },
            // direction 3
            new Converter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz) {
                    return (vx - vy + 13) + 1;
                    //return vx + 2;
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz) {
                    return ((13 - vy) - vx - vz) + 21;
                    //return (vy >> 1) + 8 - vz;
                }
            }
    };
}
