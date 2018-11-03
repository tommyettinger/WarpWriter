package warpwriter.view;

import warpwriter.model.IModel;

/**
 * An alternative to ModelRenderer which uses SpriteBatch and ditches arrays.
 *
 * @author Ben McLean
 */
public class AltRenderer {

    public final boolean RINSED_PALETTE;
    public final int EYE_DARK;
    public final int EYE_LIGHT;
    private transient int[] tempPalette;
    public boolean hardOutline = false;

    public AltRenderer() {
        this(true);
    }

    public AltRenderer(boolean rinsedPalette) {
        tempPalette = new int[256];
        RINSED_PALETTE = rinsedPalette;
        EYE_DARK = RINSED_PALETTE ? 22 : 30;
        EYE_LIGHT = 17;
    }

    public AltRenderer renderOrthoSide(IModel voxels, IRenderer renderer) {
        final int xs = voxels.xSize(), ys = voxels.ySize(), zs = voxels.zSize();
        AltRenderer.VariableConverter con = directionsOrthoSideV[2];
        //int[][] working = makeRenderArray(xs, ys, zs, 4, 5, 1);
        //int width = working.length, height = working[0].length;
        //int[][] depths = new int[width][height], render;
        int aa, cc, aStep, cStep, aMax, cMax, px, py;
        boolean flip = true;
        int current;

        aa = ys - 1;
        aStep = -1;
        aMax = -1;
        cc = xs - 1;
        cStep = -1;
        cMax = -1;

        if (flip) {
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
                                        //working[px + sx][py + sy] = current;
                                        renderer.drawPixel(px + sx, py + sy, current);
                                    }
                                }
                            } else if (current == 3) {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 0; sy < 4; sy++) {
                                        if (renderer.getPixel(px + sx, py + sy) == 0)
                                            renderer.drawPixel(px + sx, py + sy, current);
                                    }
                                }
                            } else if (current == 4) {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 1; sy < 4; sy++) {
                                        renderer.drawPixel(px + sx, py + sy, EYE_DARK);
                                        //depths[px + sx][py + sy] = 256 + c * 2;
                                    }
                                    renderer.drawPixel(px + sx, py, EYE_DARK);
                                    //depths[px + sx][py] = 256 + c * 2;
                                }
                                renderer.drawPixel(px, py, EYE_LIGHT);
                                renderer.drawPixel(px + 1, py, EYE_LIGHT);
                                renderer.drawPixel(px, py + 1, EYE_LIGHT);
                                renderer.drawPixel(px + 1, py + 1, EYE_LIGHT);
                            } else {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 1; sy < 4; sy++) {
                                        renderer.drawPixel(px + sx, py + sy, current);
                                        //depths[px + sx][py + sy] = 256 + c * 2;
                                    }
                                    renderer.drawPixel(px + sx, py, current - 1);
                                    //depths[px + sx][py] = 256 + c * 2;
                                }
                            }
                        }
                    }
                }
            }
        } else {
            for (int a = aa; a != aMax; a += aStep) {
                for (int b = 0; b < zs; b++) {
                    for (int c = cc; c != cMax; c += cStep) {
                        px = con.voxelToPixelX(a + 1, c + 1, b, xs, ys, zs);
                        py = con.voxelToPixelY(a + 1, c + 1, b, xs, ys, zs);
                        current = voxels.at(a, c, b) & 255;
                        if (current != 0 && !((current >= 8 && current < 16) || (!RINSED_PALETTE && current >= 136 && current < 144))) {
                            if (current <= 2) {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 0; sy < 4; sy++) {
                                        renderer.drawPixel(px + sx, py + sy, current);
                                    }
                                }
                            } else if (current == 3) {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 0; sy < 4; sy++) {
                                        if (renderer.getPixel(px + sx, py + sy) == 0)
                                            renderer.drawPixel(px + sx, py + sy, current);
                                    }
                                }
                            } else if (current == 4) {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 1; sy < 4; sy++) {
                                        renderer.drawPixel(px + sx, py + sy, EYE_DARK);
                                        //depths[px + sx][py + sy] = 256 + c * 2;
                                    }
                                    renderer.drawPixel(px + sx, py, EYE_DARK);
                                    //depths[px + sx][py] = 256 + c * 2;

                                }
                                renderer.drawPixel(px, py, EYE_LIGHT);
                                renderer.drawPixel(px + 1, py, EYE_LIGHT);
                                renderer.drawPixel(px, py + 1, EYE_LIGHT);
                                renderer.drawPixel(px + 1, py + 1, EYE_LIGHT);
                            } else {
                                for (int sx = 0; sx < 3; sx++) {
                                    for (int sy = 1; sy < 4; sy++) {
                                        renderer.drawPixel(px + sx, py + sy, current);
                                        //depths[px + sx][py + sy] = 256 + c * 2;
                                    }
                                    renderer.drawPixel(px + sx, py, current - 1);
                                    //depths[px + sx][py] = 256 + c * 2;
                                }
                            }
                        }
                    }
                }
            }
        }
        return this;
    }

    protected interface VariableConverter {
        /**
         * Converts from voxel space to pixel space and gets the 2D x position. Here, the x-axis runs back-to-front,
         * the y-axis runs left-to-right, and the z-axis runs bottom-to-top, before rotations are applied.
         *
         * @param vx    before rotating, the position on the 3D back-to-front x axis
         * @param vy    before rotating, the position on the 3D left-to-right y axis
         * @param vz    before rotating, the position on the 3D bottom-to-top z axis
         * @param xSize size of the bounding box's x dimension
         * @param ySize size of the bounding box's y dimension
         * @param zSize size of the bounding box's z dimension
         * @return the 2D x position of the voxel, with 0 at the left
         */
        int voxelToPixelX(int vx, int vy, int vz, int xSize, int ySize, int zSize);

        /**
         * Converts from voxel space to pixel space and gets the 2D y position. Here, the x-axis runs back-to-front,
         * the y-axis runs left-to-right, and the z-axis runs bottom-to-top, before rotations are applied.
         *
         * @param vx    before rotating, the position on the 3D back-to-front x axis
         * @param vy    before rotating, the position on the 3D left-to-right y axis
         * @param vz    before rotating, the position on the 3D bottom-to-top z axis
         * @param xSize size of the bounding box's x dimension
         * @param ySize size of the bounding box's y dimension
         * @param zSize size of the bounding box's z dimension
         * @return the 2D y position of the voxel, with 0 at the top
         */
        int voxelToPixelY(int vx, int vy, int vz, int xSize, int ySize, int zSize);
    }

    protected static final AltRenderer.VariableConverter[] directionsOrthoV = {
            // direction 0, no rotation
            new AltRenderer.VariableConverter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return vy * 3 + (ys - 1 >> 1);
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return (vx * 3) + (zs - vz) * 2 + (zs >> 1);
                }
            },
            // direction 1
            new AltRenderer.VariableConverter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return 3 + (xs - vx) * 3 + (xs - 1 >> 1);
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return (vy * 3) + (zs - vz) * 2 + (zs >> 1);
                }
            },
            // direction 2
            new AltRenderer.VariableConverter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return 3 + (ys - vy) * 3 + (ys - 1 >> 1);
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return 3 + (xs - vx) * 3 + (zs - vz) * 2 + (zs >> 1);
                }
            },
            // direction 3
            new AltRenderer.VariableConverter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return vx * 3 + (xs - 1 >> 1);
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return 3 + (ys - vy) * 3 + (zs - vz) * 2 + (zs >> 1);
                }
            }
    };
    protected static final AltRenderer.VariableConverter[] directionsIsoV = {

            /*
                    px = px - 1 << 1;
                    py = (py - 2 << 1) + 1;

             */

            // direction 0, no rotation
            new AltRenderer.VariableConverter() {
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
            new AltRenderer.VariableConverter() {
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
            new AltRenderer.VariableConverter() {
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
            new AltRenderer.VariableConverter() {
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
    protected static final AltRenderer.VariableConverter[] directionsOrthoSideV = {
            // direction 0, no rotation
            new AltRenderer.VariableConverter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return vy * 3 + (ys - 1 >> 1);
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return 4 + zs * 4 - vz * 3;
                }
            },
            // direction 1
            new AltRenderer.VariableConverter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return 3 + (xs - vx) * 3 + (xs - 1 >> 1);
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return 4 + zs * 4 - vz * 3;
                }
            },
            // direction 2
            new AltRenderer.VariableConverter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return 3 + (ys - vy) * 3 + (ys - 1 >> 1);
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return 4 + zs * 4 - vz * 3;
                }
            },
            // direction 3
            new AltRenderer.VariableConverter() {
                @Override
                public int voxelToPixelX(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return vx * 3 + (xs - 1 >> 1);
                }

                @Override
                public int voxelToPixelY(int vx, int vy, int vz, int xs, int ys, int zs) {
                    return 4 + zs * 4 - vz * 3;
                }
            }
    };

    protected static final AltRenderer.VariableConverter[] directionsIsoSideV = {
            // direction 0, no rotation
            new AltRenderer.VariableConverter() {
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
            new AltRenderer.VariableConverter() {
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
            new AltRenderer.VariableConverter() {
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
            new AltRenderer.VariableConverter() {
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

    protected static final AltRenderer.VariableConverter[][] allDirectionsV = {
            directionsIsoV, directionsOrthoV, directionsIsoSideV, directionsOrthoSideV
    };

}
