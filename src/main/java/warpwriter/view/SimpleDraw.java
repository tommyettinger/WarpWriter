package warpwriter.view;

import com.badlogic.gdx.graphics.Color;
import warpwriter.model.IModel;

public class SimpleDraw {
    protected static IVoxelColor color = new VoxelColor();

    public static void simpleDraw(IModel model, IPixelRenderer renderer) {
        simpleDraw(model, renderer, color);
    }

    public static void simpleDraw(IModel model, IPixelRenderer renderer, IVoxelColor color) {
        final int sizeX = model.sizeX(), sizeY = model.sizeY(), sizeZ = model.sizeZ();
        for (int z = 0; z < sizeZ; z++) {
            for (int y = 0; y < sizeY; y++) {
//                renderer.drawPixel(y, z, Color.rgba8888(Color.RED));
                for (int x = 0; x < sizeX; x++) {
                    byte result = model.at(x, y, z);
                    if (result != 0) {
                        renderer.drawPixel(y, z, color.rightFace(result));
                        break;
                    }
                }
            }
        }
    }

    public static void simpleDraw45(IModel model, IPixelRenderer renderer) {
        simpleDraw45(model, renderer, color);
    }

    public static void simpleDraw45(IModel model, IPixelRenderer renderer, IVoxelColor color) {
        final int sizeX = model.sizeX(),
                sizeY = model.sizeY(),
                sizeZ = model.sizeZ(),
                pixelWidth = sizeX + sizeY;
        byte result = 0;
        for (int py = 0; py < sizeZ; py++) { // pixel y
            for (int px = 0; px <= pixelWidth; px += 2) { // pixel x
//                renderer.drawRect(px, py, 2, 1, Color.rgba8888(Color.RED));
                boolean leftDone = false, rightDone = pixelWidth - px < 2;
                final int startX = px > sizeX - 1 ? 0 : sizeX - px - 1,
                        startY = px - sizeX + 1 < 0 ? 0 : px - sizeX + 1;
                for (int vx = startX, vy = startY;
                     vx <= sizeX && vy <= sizeY;
                     vx++, vy++) { // vx is voxel x, vy is voxel y
                    if (!leftDone && vy != 0) {
                        result = model.at(vx, vy - 1, py);
                        if (result != 0) {
                            renderer.drawPixel(px, py, color.rightFace(result));
                            leftDone = true;
                        }
                    }
                    if (!rightDone && vx > 0) {
                        result = model.at(vx - 1, vy, py);
                        if (result != 0) {
                            renderer.drawPixel(px + 1, py, color.leftFace(result));
                            rightDone = true;
                        }
                    }
                    if (leftDone && rightDone) break;
                    result = model.at(vx, vy, py);
                    if (result != 0) {
                        if (!leftDone) renderer.drawPixel(px, py, color.leftFace(result));
                        if (!rightDone) renderer.drawPixel(px + 1, py, color.rightFace(result));
                        break;
                    }
                }
            }
        }
    }

    public static void simpleDrawAbove(IModel model, IPixelRenderer renderer, IVoxelColor color) {
        final int sizeX = model.sizeX(),
                sizeY = model.sizeY(),
                sizeZ = model.sizeZ(),
                pixelHeight = (sizeX + sizeZ) * 2;
        for (int vy = 0; vy < sizeY; vy++) { // voxel y is pixel x
            // Begin bottom row
            byte result = model.at(sizeX - 1, 0, 0);
            if (result != 0) {
                renderer.drawPixel(vy, 0, color.rightFace(result));
            }
            // Finish bottom row
            // Begin main bulk of model
            for (int py = 1; py < pixelHeight; py += 2) { // pixel y
                boolean below = false, above = pixelHeight - py < 2;
                final int startX = (py / 2) > sizeZ - 1 ? (py / 2) - sizeZ + 1 : 0,
                        startZ = (py / 2) > sizeZ - 1 ? sizeZ - 1 : (py / 2);
                for (int vx = startX, vz = startZ;
                     vx <= sizeX && vz >= -1;
                     vx++, vz--) { // vx is voxel x, vz is voxel z
                    if (!above && vz + 1 < sizeZ) {
                        result = model.at(vx, vy, vz + 1);
                        if (result != 0) {
                            renderer.drawPixel(vy, py + 1, color.rightFace(result));
                            above = true;
                        }
                    }
                    if (!below && vx > 0) {
                        result = model.at(vx - 1, vy, vz);
                        if (result != 0) {
                            renderer.drawPixel(vy, py, color.topFace(result));
                            below = true;
                        }
                    }
                    if (above && below) break;
                    result = model.at(vx, vy, vz);
                    if (result != 0) {
                        if (!above) renderer.drawPixel(vy, py + 1, color.topFace(result));
                        if (!below) renderer.drawPixel(vy, py, color.rightFace(result));
                        break;
                    }
                }
            }
            // Finish main bulk of model
        }
    }

    public static void simpleDrawIso(IModel model, ITriangleRenderer renderer) {
        simpleDrawIso(model, renderer, color);
    }

    public static void simpleDrawIso(IModel model, ITriangleRenderer renderer, IVoxelColor color) {
        byte result;
        final int sizeVX = model.sizeX(), sizeVY = model.sizeY(), sizeVZ = model.sizeZ(),
                sizeVX2 = sizeVX * 2, sizeVY2 = sizeVY * 2,
                pixelWidth = sizeVX2 + sizeVY2 - ((sizeVX + sizeVY) % 2 == 1 ? 4 : 0);
        // To move one x+ in voxels is x + 2, y - 2 in pixels.
        // To move one x- in voxels is x - 2, y + 2 in pixels.
        // To move one y+ in voxels is x + 2, y + 2 in pixels.
        // To move one y- in voxels is x - 2, y - 2 in pixels.
        // To move one z+ in voxels is y + 4 in pixels.
        // To move one z- in voxels is y - 4 in pixels.
        for (int px = 0; px < pixelWidth; px += 4) {
            final boolean rightSide = px + 2 > sizeVY2, leftSide = !rightSide;
            final int bottomPY = Math.abs(sizeVX2 - 2 - px),
                    topPY = sizeVX2 - 2 + // black space at the bottom from the first column
                            (sizeVZ - 1) * 4 + // height of model
                            sizeVY2 - Math.abs(sizeVY2 - 2 - px);

            // Begin drawing bottom row triangles
            renderer.drawLeftTriangle(px, bottomPY - 4, Color.rgba8888(Color.OLIVE));
            renderer.drawRightTriangle(px + 2, bottomPY - 4, Color.rgba8888(Color.OLIVE));
            if (px < sizeVX2 - 2) {
                renderer.drawLeftTriangle(px + 2, bottomPY - 6, Color.rgba8888(Color.PURPLE));
            } else if (px > sizeVX2) {
                renderer.drawRightTriangle(px, bottomPY - 6, Color.rgba8888(Color.PURPLE));
            } else if (sizeVX % 2 == 0) {
                result = model.at(0, 0, 0);
                if (result != 0) {
                    renderer.drawRightTriangle(px, bottomPY - 6, color.rightFace(result)); // Very bottom
                }
            }
            // Finish drawing bottom row triangles

            // Begin drawing main bulk of model
            for (int py = bottomPY - 4; py <= topPY; py += 4) {
                final boolean topSide = py > bottomPY + (sizeVZ - 1) * 4, bottomSide = !topSide;
                final int additive = (py - bottomPY) / 4 - sizeVZ + 1,
                        startVX = (px < sizeVX2 ? sizeVX - 1 - px / 2 : 0) + (topSide ? additive : 0),
                        startVY = (px < sizeVX2 ? 0 : px / 2 - sizeVX + 1) + (topSide ? additive : 0),
                        startVZ = bottomSide ? (py - bottomPY) / 4 : sizeVZ - 1;

                boolean left = false,
                        topLeft = false,
                        topRight = false,
                        right = false;
                for (int vx = startVX, vy = startVY, vz = startVZ;
                     vx < sizeVX && vy < sizeVY && vz >= 0;
                     vx++, vy++, vz--) {

                    // Order to check
                    // x, y-, z+ = Above front left
                    // x-, y, z+ = Above front right
                    // x, y, z+ = Above
                    // x, y-, z = Front left
                    // x-, y, z = Front right
                    // x, y, z  = Center
                    // x+, y, z = Back left
                    // x, y+, z = Back right

                    // OK here goes:
                    // x, y-, z+ = Above front left
                    if ((!left || !topLeft) && vy > 0 && vz < sizeVZ - 1) {
                        result = model.at(vx, vy - 1, vz + 1);
                        if (result != 0) {
                            if (!topLeft) {
                                renderer.drawLeftTriangle(px, py, color.rightFace(result));
                                topLeft = true;
                            }
                            if (!left) {
                                renderer.drawRightTriangle(px, py - 2, color.rightFace(result));
                                left = true;
                            }
                        }
                    }

                    // x-, y, z+ = Above front right
                    if ((!topRight || !right) && vx > 0 && vz < sizeVZ - 1) {
                        result = model.at(vx - 1, vy, vz + 1);
                        if (result != 0) {
                            if (!topRight) {
                                renderer.drawRightTriangle(px + 2, py, color.leftFace(result));
                                topRight = true;
                            }
                            if (!right) {
                                renderer.drawLeftTriangle(px + 2, py - 2, color.leftFace(result));
                                right = true;
                            }
                        }
                    }

                    // x, y, z+ = Above
                    if ((!topLeft || !topRight) && vz < sizeVZ - 1) {
                        result = model.at(vx, vy, vz + 1);
                        if (result != 0) {
                            if (!topLeft) {
                                renderer.drawLeftTriangle(px, py, color.leftFace(result));
                                topLeft = true;
                            }
                            if (!topRight) {
                                renderer.drawRightTriangle(px + 2, py, color.rightFace(result));
                                topRight = true;
                            }
                        }
                    }

                    // x, y-, z = Front left
                    if (!left && vy > 0) {
                        result = model.at(vx, vy - 1, vz);
                        if (result != 0) {
                            renderer.drawRightTriangle(px, py - 2, color.topFace(result));
                            left = true;
                        }
                    }

                    // x-, y, z = Front right
                    if (!right && vx > 0) {
                        result = model.at(vx - 1, vy, vz);
                        if (result != 0) {
                            renderer.drawLeftTriangle(px + 2, py - 2, color.topFace(result));
                            right = true;
                        }
                    }

                    // x, y, z  = Center
                    if (left && topLeft && topRight && right) break;
                    result = model.at(vx, vy, vz);
                    if (result != 0) {
                        if (!topLeft)
                            renderer.drawLeftTriangle(px, py, color.topFace(result));
                        if (!left)
                            renderer.drawRightTriangle(px, py - 2, color.leftFace(result));
                        if (!topRight)
                            renderer.drawRightTriangle(px + 2, py, color.topFace(result));
                        if (!right)
                            renderer.drawLeftTriangle(px + 2, py - 2, color.rightFace(result));
                        break;
                    }

                    // x+, y, z = Back left
                    if ((!left || !topLeft) && vx < sizeVX - 1) {
                        result = model.at(vx + 1, vy, vz);
                        if (result != 0) {
                            if (!topLeft) {
                                renderer.drawLeftTriangle(px, py, color.rightFace(result));
                                topLeft = true;
                            }
                            if (!left) {
                                renderer.drawRightTriangle(px, py - 2, color.rightFace(result));
                                left = true;
                            }
                        }
                    }

                    // x, y+, z = Back right
                    if ((!right || !topRight) && vy < sizeVY - 1) {
                        result = model.at(vx, vy + 1, vz);
                        if (result != 0) {
                            if (!topRight) {
                                renderer.drawRightTriangle(px + 2, py, color.leftFace(result));
                                topRight = true;
                            }
                            if (!right) {
                                renderer.drawLeftTriangle(px + 2, py - 2, color.leftFace(result));
                                right = true;
                            }
                        }
                    }

                    // Debugging to show something instead of transparent
//                    if (!topLeft)
//                        renderer.drawLeftTriangle(px, py, Color.rgba8888(Color.GREEN));
//                    if (!left)
//                        renderer.drawRightTriangle(px, py - 2, Color.rgba8888(Color.BLUE));
//                    if (!topRight)
//                        renderer.drawRightTriangle(px + 2, py, Color.rgba8888(Color.YELLOW));
//                    if (!right)
//                        renderer.drawLeftTriangle(px + 2, py - 2, Color.rgba8888(Color.RED));
                    // Finish debugging
                }
            }
            // Finish drawing main bulk of model

            // Begin drawing top triangles
            if (px + 2 < sizeVY2) { // Top left triangles
                result = model.at(0, px / 2, sizeVZ - 1);
                if (result != 0) {
                    renderer.drawLeftTriangle(px + 2, topPY, color.topFace(result));
                }
            } else if (px + 2 > sizeVY2) { // Top right triangles
                result = model.at((px - sizeVY2) / 2, sizeVY - 1, sizeVZ - 1);
                if (result != 0) {
                    renderer.drawRightTriangle(px, topPY, color.topFace(result));
                }
            }
            // Finish drawing top triangles.

            // Drawing right edge (only for when sizeVX + sizeVY is odd numbered)
            if ((sizeVX + sizeVY) % 2 == 1) {
                final int vx = 0, vy = sizeVY - 1,
                        bottom = Math.abs(sizeVX2 - 2 - pixelWidth);
                result = model.at(vx, vy, 0);
                if (result != 0) {
                    renderer.drawRightTriangle(pixelWidth + 2, bottom - 4, color.rightFace(result)); // lower right corner
                }
                for (int py = bottom; py < bottom + sizeVZ * 4; py += 4) {
                    final int vz = (py - bottom) / 4;
                    boolean aboveEmpty = true;
                    if (vz != sizeVZ - 1) {
                        result = model.at(vx, vy, vz + 1);
                        renderer.drawRightTriangle(pixelWidth + 2, py, color.rightFace(result));
                        aboveEmpty = false;
                    }
                    result = model.at(vx, vy, vz);
                    if (result != 0) {
                        if (aboveEmpty) {
                            renderer.drawRightTriangle(pixelWidth + 2, py, color.topFace(result));
                        }
                        renderer.drawLeftTriangle(pixelWidth + 2, py - 2, color.rightFace(result));
                    }
                }
            }
            // Finish drawing right edge
        }
    }

    static boolean flash = false;
    static long time = System.currentTimeMillis(),
            duration = 500; // Milliseconds flashes last

    /**
     * This enables colors to flash onscreen for debugging purposes.
     *
     * @param color Color to show when not flashing
     * @return White if flash is active, else color.
     */
    static int flash(int color) {
        final long now = System.currentTimeMillis();
        if (now - time >= duration) {
            flash = !flash;
            time = now;
        }
        return flash ? Color.rgba8888(Color.WHITE) : color;
    }
}
