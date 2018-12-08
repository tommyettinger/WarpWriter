package warpwriter.view;

import com.badlogic.gdx.graphics.Color;
import warpwriter.model.IModel;

public class VoxelDraw {
    public static void simpleDraw(IModel model, IPixelRenderer renderer) {
        simpleDrawRight(model, renderer);
    }

    public static void simpleDrawRight(IModel model, IPixelRenderer renderer) {
        final int sizeX = model.sizeX(), sizeY = model.sizeY(), sizeZ = model.sizeZ();
        for (int z = 0; z < sizeZ; z++) {
            for (int y = 0; y < sizeY; y++) {
                for (int x = 0; x < sizeX; x++) {
                    byte result = model.at(x, y, z);
                    if (result != 0) {
                        renderer.drawPixelRightFace(y, z, result);
                        break;
                    }
                }
            }
        }
    }

    public static void simpleDrawLeft(IModel model, IPixelRenderer renderer) {
        final int sizeX = model.sizeX(), sizeY = model.sizeY(), sizeZ = model.sizeZ();
        for (int z = 0; z < sizeZ; z++) {
            for (int y = 0; y < sizeY; y++) {
                for (int x = sizeX - 1; x >= 0; x++) {
                    byte result = model.at(x, y, z);
                    if (result != 0) {
                        renderer.drawPixelLeftFace(y, z, result);
                        break;
                    }
                }
            }
        }
    }

    public static void simpleDrawTop(IModel model, IPixelRenderer renderer) {
        final int sizeX = model.sizeX(), sizeY = model.sizeY(), sizeZ = model.sizeZ();
        for (int y = 0; y < sizeY; y++) {
            for (int x = 0; x < sizeX; x++) {
                for (int z = sizeZ - 1; z >= 0; z--) {
                    byte result = model.at(x, y, z);
                    if (result != 0) {
                        renderer.drawPixelVerticalFace(y, x, result);
                        break;
                    }
                }
            }
        }
    }

    public static void simpleDrawBottom(IModel model, IPixelRenderer renderer) {
        final int sizeX = model.sizeX(), sizeY = model.sizeY(), sizeZ = model.sizeZ();
        for (int y = 0; y < sizeY; y++) {
            for (int x = 0; x < sizeX; x++) {
                for (int z = 0; z < sizeZ; z--) {
                    byte result = model.at(x, y, z);
                    if (result != 0) {
                        renderer.drawPixelVerticalFace(y, x, result);
                        break;
                    }
                }
            }
        }
    }

    public static void simpleDraw45(IModel model, IPixelRenderer renderer) {
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
                            renderer.drawPixelRightFace(px, py, result);
                            leftDone = true;
                        }
                    }
                    if (!rightDone && vx > 0) {
                        result = model.at(vx - 1, vy, py);
                        if (result != 0) {
                            renderer.drawPixelLeftFace(px + 1, py, result);
                            rightDone = true;
                        }
                    }
                    if (leftDone && rightDone) break;
                    result = model.at(vx, vy, py);
                    if (result != 0) {
                        if (!leftDone) renderer.drawPixelLeftFace(px, py, result);
                        if (!rightDone) renderer.drawPixelRightFace(px + 1, py, result);
                        break;
                    }
                }
            }
        }
    }

    public static void simpleDrawAbove(IModel model, IPixelRenderer renderer) {
        final int sizeX = model.sizeX(),
                sizeY = model.sizeY(),
                sizeZ = model.sizeZ(),
                pixelHeight = (sizeX + sizeZ) * 2;
        for (int vy = 0; vy < sizeY; vy++) { // voxel y is pixel x
            // Begin bottom row
            byte result = model.at(0, vy, 0);
            if (result != 0) {
                renderer.drawPixelRightFace(vy, 0, result);
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
                            renderer.drawPixelRightFace(vy, py + 1, result);
                            above = true;
                        }
                    }
                    if (!below && vx > 0) {
                        result = model.at(vx - 1, vy, vz);
                        if (result != 0) {
                            renderer.drawPixelVerticalFace(vy, py, result);
                            below = true;
                        }
                    }
                    if (above && below) break;
                    result = model.at(vx, vy, vz);
                    if (result != 0) {
                        if (!above) renderer.drawPixelVerticalFace(vy, py + 1, result);
                        if (!below) renderer.drawPixelRightFace(vy, py, result);
                        break;
                    }
                }
            }
            // Finish main bulk of model
        }
    }

    public static int isoHeight(IModel model) {
        return (model.sizeX() - 1) * 2 +
                (model.sizeZ() - 1) * 4 +
                (model.sizeY() - 1) * 2;
    }

    public static int isoWidth(IModel model) {
        final int sizeVX = model.sizeX(), sizeVY = model.sizeY();
        return sizeVX * 2 + sizeVY * 2 - ((sizeVX + sizeVY) % 2 == 1 ? 4 : 0);
    }

    public static void simpleDrawIso(IModel model, ITriangleRenderer renderer) {
        byte result;
        final int sizeVX = model.sizeX(), sizeVY = model.sizeY(), sizeVZ = model.sizeZ(),
                sizeVX2 = sizeVX * 2, sizeVY2 = sizeVY * 2,
                pixelWidth = isoWidth(model);
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
            if (px < sizeVX2 - 2) { // Left side of model
                result = model.at(px / 2, 0, 0);
                if (result != 0) {
                    renderer.drawLeftTriangleLeftFace(px + 2, bottomPY - 6, result);
                    renderer.drawLeftTriangleLeftFace(px, bottomPY - 4, result);
                    renderer.drawRightTriangleLeftFace(px + 2, bottomPY - 4, result);
                } else if (px / 2 < sizeVX2 - 1) {
                    result = model.at(px / 2 + 1, 0, 0);
                    if (result != 0) {
                        renderer.drawLeftTriangleRightFace(px, bottomPY - 4, result);
                    }
                }
            } else if (px > sizeVX2 - 2) { // Right side of model
                result = model.at(0, px / 2 - sizeVX + 1, 0);
                if (result != 0) {
                    renderer.drawRightTriangleRightFace(px, bottomPY - 6, result);
                    renderer.drawLeftTriangleRightFace(px, bottomPY - 4, result);
                    renderer.drawRightTriangleRightFace(px + 2, bottomPY - 4, result);
                } else if (px / 2 - sizeVX + 1 < sizeVY - 1) {
                    result = model.at(0, px / 2 - sizeVX + 2, 0);
                    if (result != 0) {
                        renderer.drawRightTriangleLeftFace(px + 2, bottomPY - 4, result);
                    }
                }
            } else { // Very bottom
                result = model.at(0, 0, 0);
                if (result != 0) {
                    renderer.drawLeftTriangleLeftFace(px, bottomPY - 4, result);
                    renderer.drawRightTriangleRightFace(px + 2, bottomPY - 4, result);
                    if (sizeVX % 2 == 0)
                        renderer.drawRightTriangleRightFace(px, bottomPY - 6, result);
                } else {
                    result = model.at(px / 2 + 1, 0, 0);
                    if (result != 0) {
                        renderer.drawLeftTriangleRightFace(px, bottomPY - 4, result);
                    }
                    result = model.at(0, px / 2 - sizeVX + 2, 0);
                    if (result != 0) {
                        renderer.drawRightTriangleLeftFace(px + 2, bottomPY - 4, result);
                    }
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
                    // x+, y, z- = Below left
                    // x, y+ z- = Below right

                    // OK here goes:
                    // x, y-, z+ = Above front left
                    if ((!left || !topLeft) && vx == 0 && vy > 0 && vz < sizeVZ - 1) {
                        result = model.at(vx, vy - 1, vz + 1);
                        if (result != 0) {
                            if (!topLeft) {
                                renderer.drawLeftTriangleRightFace(px, py, result);
                                topLeft = true;
                            }
                            if (!left) {
                                renderer.drawRightTriangleRightFace(px, py - 2, result);
                                left = true;
                            }
                        }
                    }

                    // x-, y, z+ = Above front right
                    if ((!topRight || !right) && vx > 0 && vy == 0 && vz < sizeVZ - 1) {
                        result = model.at(vx - 1, vy, vz + 1);
                        if (result != 0) {
                            if (!topRight) {
                                renderer.drawRightTriangleLeftFace(px + 2, py, result);
                                topRight = true;
                            }
                            if (!right) {
                                renderer.drawLeftTriangleLeftFace(px + 2, py - 2, result);
                                right = true;
                            }
                        }
                    }

                    // x, y, z+ = Above
                    if ((!topLeft || !topRight) && vz < sizeVZ - 1) {
                        result = model.at(vx, vy, vz + 1);
                        if (result != 0) {
                            if (!topLeft) {
                                renderer.drawLeftTriangleLeftFace(px, py, result);
                                topLeft = true;
                            }
                            if (!topRight) {
                                renderer.drawRightTriangleRightFace(px + 2, py, result);
                                topRight = true;
                            }
                        }
                    }

                    // x, y-, z = Front left
                    if (!left && vy > 0) {
                        result = model.at(vx, vy - 1, vz);
                        if (result != 0) {
                            renderer.drawRightTriangleVerticalFace(px, py - 2, result);
                            left = true;
                        }
                    }

                    // x-, y, z = Front right
                    if (!right && vx > 0) {
                        result = model.at(vx - 1, vy, vz);
                        if (result != 0) {
                            renderer.drawLeftTriangleVerticalFace(px + 2, py - 2, result);
                            right = true;
                        }
                    }

                    // x, y, z  = Center
                    if (left && topLeft && topRight && right) break;
                    result = model.at(vx, vy, vz);
                    if (result != 0) {
                        if (!topLeft)
                            renderer.drawLeftTriangleVerticalFace(px, py, result);
                        if (!left)
                            renderer.drawRightTriangleLeftFace(px, py - 2, result);
                        if (!topRight)
                            renderer.drawRightTriangleVerticalFace(px + 2, py, result);
                        if (!right)
                            renderer.drawLeftTriangleRightFace(px + 2, py - 2, result);
                        break;
                    }

                    // x+, y, z = Back left
                    if ((!left || !topLeft) && vx < sizeVX - 1) {
                        result = model.at(vx + 1, vy, vz);
                        if (result != 0) {
                            if (!topLeft) {
                                renderer.drawLeftTriangleRightFace(px, py, result);
                                topLeft = true;
                            }
                            if (!left) {
                                renderer.drawRightTriangleRightFace(px, py - 2, result);
                                left = true;
                            }
                        }
                    }

                    // x, y+, z = Back right
                    if ((!right || !topRight) && vy < sizeVY - 1) {
                        result = model.at(vx, vy + 1, vz);
                        if (result != 0) {
                            if (!topRight) {
                                renderer.drawRightTriangleLeftFace(px + 2, py, result);
                                topRight = true;
                            }
                            if (!right) {
                                renderer.drawLeftTriangleLeftFace(px + 2, py - 2, result);
                                right = true;
                            }
                        }
                    }

                    // x+, y+ z = Back center
                    if ((!topLeft || !topRight) && vx < sizeVX - 1 && vy < sizeVY - 1) {
                        result = model.at(vx + 1, vy + 1, vz);
                        if (result != 0) {
                            if (!topRight) {
                                renderer.drawRightTriangleRightFace(px + 2, py, result);
                                topRight = true;
                            }
                            if (!topLeft) {
                                renderer.drawLeftTriangleLeftFace(px, py, result);
                                topLeft = true;
                            }
                        }
                    }

                    // x+, y, z- = Below left
                    if (!left && vx < sizeVX - 1 && vz > 0) {
                        result = model.at(vx + 1, vy, vz - 1);
                        if (result != 0) {
                            renderer.drawRightTriangleVerticalFace(px, py - 2, result);
                            left = true;
                        }
                    }

                    // x, y+ z- = Below right
                    if (!right && vy < sizeVY - 1 && vz > 0) {
                        result = model.at(vx, vy + 1, vz - 1);
                        if (result != 0) {
                            renderer.drawLeftTriangleVerticalFace(px + 2, py - 2, result);
                            right = true;
                        }
                    }

                    // Debugging
//                    if (startVX == 10 && startVY == 0 && startVZ == 3) {
//                        Gdx.app.log("debug", "Coord: " + vx + ", " + vy + ", " + vz);
////                    if (!topLeft)
//                        renderer.drawLeftTriangle(px, py, flash());
////                    if (!left)
//                        renderer.drawRightTriangle(px, py - 2, flash());
////                    if (!topRight)
//                        renderer.drawRightTriangle(px + 2, py, flash());
////                    if (!right)
//                        renderer.drawLeftTriangle(px + 2, py - 2, flash());
//                    }
                    // Finish debugging
                }
            }
            // Finish drawing main bulk of model

            // Begin drawing top triangles
            if (px + 2 < sizeVY2) { // Top left triangles
                result = model.at(0, px / 2, sizeVZ - 1);
                if (result != 0) {
                    renderer.drawLeftTriangleVerticalFace(px + 2, topPY, result);
                }
            } else if (px + 2 > sizeVY2) { // Top right triangles
                result = model.at((px - sizeVY2) / 2, sizeVY - 1, sizeVZ - 1);
                if (result != 0) {
                    renderer.drawRightTriangleVerticalFace(px, topPY, result);
                }
            }
            // Finish drawing top triangles.

            // Drawing right edge (only for when sizeVX + sizeVY is odd numbered)
            if ((sizeVX + sizeVY) % 2 == 1) {
                final int vx = 0, vy = sizeVY - 1,
                        bottom = Math.abs(sizeVX2 - 2 - pixelWidth);
                result = model.at(vx, vy, 0);
                if (result != 0) {
                    renderer.drawRightTriangleRightFace(pixelWidth + 2, bottom - 4, result); // lower right corner
                }
                for (int py = bottom; py < bottom + sizeVZ * 4; py += 4) {
                    final int vz = (py - bottom) / 4;
                    boolean aboveEmpty = true;
                    if (vz != sizeVZ - 1) {
                        result = model.at(vx, vy, vz + 1);
                        renderer.drawRightTriangleRightFace(pixelWidth + 2, py, result);
                        aboveEmpty = false;
                    }
                    result = model.at(vx, vy, vz);
                    if (result != 0) {
                        if (aboveEmpty) {
                            renderer.drawRightTriangleVerticalFace(pixelWidth + 2, py, result);
                        }
                        renderer.drawLeftTriangleRightFace(pixelWidth + 2, py - 2, result);
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

    static int flash() {
        return flash(Color.rgba8888(Color.CLEAR));
    }
}
