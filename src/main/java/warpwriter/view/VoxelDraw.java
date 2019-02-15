package warpwriter.view;

import warpwriter.model.IModel;
import warpwriter.view.render.BlinkRenderer;
import warpwriter.view.render.IRectangleRenderer;
import warpwriter.view.render.ITriangleRenderer;

/**
 * VoxelDraw contains the logic to render voxel models as 2D sprites from various perspectives with a high level of abstraction.
 *
 * @author Ben McLean
 */
public class VoxelDraw {
    protected static final BlinkRenderer brenderer = new BlinkRenderer();

    public static BlinkRenderer brenderer() {
        return brenderer;
    }

    public static void draw(IModel model, IRectangleRenderer renderer) {
        drawRight(model, renderer);
    }

    public static void drawRight(IModel model, IRectangleRenderer renderer) {
        drawRight(model, renderer, 6, 6);
    }

    public static void drawRight(IModel model, IRectangleRenderer renderer, int scaleX, int scaleY) {
        final int sizeX = model.sizeX(), sizeY = model.sizeY(), sizeZ = model.sizeZ();
        for (int z = 0; z < sizeZ; z++) {
            for (int y = 0; y < sizeY; y++) {
                for (int x = 0; x < sizeX; x++) {
                    byte v = model.at(x, y, z);
                    if (v != 0) {
                        renderer.rectRight(y * scaleX, z * scaleY, scaleX, scaleY, v);
                        break;
                    }
                }
            }
        }
    }

    public static void drawRightPeek(IModel model, IRectangleRenderer renderer) {
        drawRightPeek(model, renderer, 6, 6);
    }

    public static void drawRightPeek(IModel model, IRectangleRenderer renderer, int scaleX, int scaleY) {
        final int sizeX = model.sizeX(), sizeY = model.sizeY(), sizeZ = model.sizeZ();
        for (int z = 0; z < sizeZ; z++) {
            for (int y = 0; y < sizeY; y++) {
                for (int x = 0; x < sizeX; x++) {
                    byte v = model.at(x, y, z);
                    if (v != 0) {
                        renderer.rectRight(y * scaleX, z * scaleY, scaleX, scaleY, v);
                        if (z >= sizeZ - 1 || model.at(x, y, z + 1) == 0)
                            renderer.rectVertical(y * scaleX, (z + 1) * scaleY - 1, scaleX, 1, v);
                        break;
                    }
                }
            }
        }
    }

    public static void drawLeft(IModel model, IRectangleRenderer renderer) {
        drawLeft(model, renderer, 6, 6);
    }

    public static void drawLeft(IModel model, IRectangleRenderer renderer, int scaleX, int scaleY) {
        final int sizeX = model.sizeX(), sizeY = model.sizeY(), sizeZ = model.sizeZ();
        for (int z = 0; z < sizeZ; z++) {
            for (int y = 0; y < sizeY; y++) {
                for (int x = sizeX - 1; x >= 0; x++) {
                    byte v = model.at(x, y, z);
                    if (v != 0) {
                        renderer.rectLeft(y * scaleX, z * scaleY, scaleX, scaleY, v);
                        break;
                    }
                }
            }
        }
    }

    public static void drawTop(IModel model, IRectangleRenderer renderer) {
        drawTop(model, renderer, 6, 6);
    }

    public static void drawTop(IModel model, IRectangleRenderer renderer, int scaleX, int scaleY) {
        final int sizeX = model.sizeX(), sizeY = model.sizeY(), sizeZ = model.sizeZ();
        for (int y = 0; y < sizeY; y++) {
            for (int x = 0; x < sizeX; x++) {
                for (int z = sizeZ - 1; z >= 0; z--) {
                    byte v = model.at(x, y, z);
                    if (v != 0) {
                        renderer.rectVertical(y * scaleX, z * scaleY, scaleX, scaleY, v);
                        break;
                    }
                }
            }
        }
    }

    public static void drawBottom(IModel model, IRectangleRenderer renderer) {
        drawBottom(model, renderer, 6, 6);
    }

    public static void drawBottom(IModel model, IRectangleRenderer renderer, int scaleX, int scaleY) {
        final int sizeX = model.sizeX(), sizeY = model.sizeY(), sizeZ = model.sizeZ();
        for (int y = 0; y < sizeY; y++) {
            for (int x = 0; x < sizeX; x++) {
                for (int z = 0; z < sizeZ; z--) {
                    byte v = model.at(x, y, z);
                    if (v != 0) {
                        renderer.rectVertical(y * scaleX, z * scaleY, scaleX, scaleY, v);
                        break;
                    }
                }
            }
        }
    }

    public static void draw45(IModel model, IRectangleRenderer renderer) {
        draw45(model, renderer, 6, 6);
    }

    public static void draw45(IModel model, IRectangleRenderer renderer, int scaleX, int scaleY) {
        byte v;
        final int sizeX = model.sizeX(),
                sizeY = model.sizeY(),
                sizeZ = model.sizeZ(),
                pixelWidth = sizeX + sizeY;
        for (int py = 0; py < sizeZ; py++) { // pixel y
            for (int px = 0; px <= pixelWidth; px += 2) { // pixel x
                boolean leftDone = false, rightDone = pixelWidth - px < 2;
                final int startX = px > sizeX - 1 ? 0 : sizeX - px - 1,
                        startY = px - sizeX + 1 < 0 ? 0 : px - sizeX + 1;
                for (int vx = startX, vy = startY;
                     vx <= sizeX && vy <= sizeY;
                     vx++, vy++) { // vx is voxel x, vy is voxel y
                    if (!leftDone && vy != 0) {
                        v = model.at(vx, vy - 1, py);
                        if (v != 0) {
                            renderer.rectRight(px * scaleX, py * scaleY, scaleX, scaleY, v);
                            leftDone = true;
                        }
                    }
                    if (!rightDone && vx > 0) {
                        v = model.at(vx - 1, vy, py);
                        if (v != 0) {
                            renderer.rectLeft((px + 1) * scaleX, py * scaleY, scaleX, scaleY, v);
                            rightDone = true;
                        }
                    }
                    if (leftDone && rightDone) break;
                    v = model.at(vx, vy, py);
                    if (v != 0) {
                        if (!leftDone)
                            renderer.rectLeft(px * scaleX, py * scaleY, scaleX, scaleY, v);
                        if (!rightDone)
                            renderer.rectRight((px + 1) * scaleX, py * scaleY, scaleX, scaleY, v);
                        break;
                    }
                }
            }
        }
    }

    public static void draw45Peek(IModel model, IRectangleRenderer renderer) {
        draw45Peek(model, renderer, 4, 6);
    }

    public static void draw45Peek(IModel model, IRectangleRenderer renderer, int scaleX, int scaleY) {
        byte v;
        final int sizeX = model.sizeX(),
                sizeY = model.sizeY(),
                sizeZ = model.sizeZ(),
                pixelWidth = sizeX + sizeY;
        for (int py = 0; py < sizeZ; py++) { // pixel y
            for (int px = 0; px <= pixelWidth; px += 2) { // pixel x
                boolean leftDone = false, rightDone = pixelWidth - px < 2;
                final int startX = px >= sizeX ? 0 : sizeX - px - 1,
                        startY = px < sizeX ? 0 : px - sizeX + 1;
                for (int vx = startX, vy = startY;
                     vx <= sizeX && vy <= sizeY;
                     vx++, vy++) { // vx is voxel x, vy is voxel y
                    if (!leftDone && vy > 0 && vx < sizeX) {
                        v = model.at(vx, vy - 1, py);
                        if (v != 0) {
                            renderer.rectRight(px * scaleX, py * scaleY, scaleX, scaleY, v);
                            if (py >= sizeZ - 1 || model.at(vx, vy - 1, py + 1) == 0)
                                renderer.rectVertical(px * scaleX, (py + 1) * scaleY - 1, scaleX, 1, v);
                            leftDone = true;
                        }
                    }
                    if (!rightDone && vx > 0 && vy < sizeY) {
                        v = model.at(vx - 1, vy, py);
                        if (v != 0) {
                            renderer.rectLeft((px + 1) * scaleX, py * scaleY, scaleX, scaleY, v);
                            if (py >= sizeZ - 1 || model.at(vx - 1, vy, py + 1) == 0)
                                renderer.rectVertical((px + 1) * scaleX, (py + 1) * scaleY - 1, scaleX, 1, v);
                            rightDone = true;
                        }
                    }
                    if ((leftDone && rightDone) || vx >= sizeX || vy >= sizeY) break;
                    v = model.at(vx, vy, py);
                    if (v != 0) {
                        boolean peek = py >= sizeZ - 1 || model.at(vx, vy, py + 1) == 0;
                        if (!leftDone) {
                            renderer.rectLeft(px * scaleX, py * scaleY, scaleX, scaleY, v);
                            if (peek)
                                renderer.rectVertical(px * scaleX, (py + 1) * scaleY - 1, scaleX, 1, v);
                        }
                        if (!rightDone) {
                            renderer.rectRight((px + 1) * scaleX, py * scaleY, scaleX, scaleY, v);
                            if (peek)
                                renderer.rectVertical((px + 1) * scaleX, (py + 1) * scaleY - 1, scaleX, 1, v);
                        }
                        break;
                    }
                }
            }
        }
    }

    public static void drawAbove(IModel model, IRectangleRenderer renderer) {
        drawAbove(model, renderer, 6, 2);
    }

    public static void drawAbove(IModel model, IRectangleRenderer renderer, int scaleX, int scaleY) {
        brenderer.set(renderer);
        final int sizeX = model.sizeX(),
                sizeY = model.sizeY(),
                sizeZ = model.sizeZ(),
                pixelHeight = (sizeX + sizeZ) * 2;
        for (int vy = 0; vy < sizeY; vy++) { // voxel y is pixel x
            // Begin bottom row
            byte v = model.at(0, vy, 0);
            if (v != 0) renderer.rectRight(vy * scaleX, 0, scaleX, scaleY, v);
            // Finish bottom row
            // Begin main bulk of model
            for (int py = 1; py < pixelHeight; py += 2) { // pixel y
                boolean below = false, above = pixelHeight - py < 2;
                final int startX = (py / 2) > sizeZ - 1 ? (py / 2) - sizeZ + 1 : 0,
                        startZ = (py / 2) > sizeZ - 1 ? sizeZ - 1 : (py / 2);
                for (int vx = startX, vz = startZ;
                     vx <= sizeX && vz >= -1;
                     vx++, vz--) { // vx is voxel x, vz is voxel z
                    if (!above && vz + 1 < sizeZ && vx < sizeX) {
                        v = model.at(vx, vy, vz + 1);
                        if (v != 0) {
                            renderer.rectRight(vy * scaleX, (py + 1) * scaleY, scaleX, scaleY, v);
                            above = true;
                        }
                    }
                    if (!below && vx > 0 && vz >= 0) {
                        v = model.at(vx - 1, vy, vz);
                        if (v != 0) {
                            renderer.rectVertical(vy * scaleX, py * scaleY, scaleX, scaleY, v);
                            below = true;
                        }
                    }
                    if ((above && below) || vx >= sizeX || vz < 0) break;
                    v = model.at(vx, vy, vz);
                    if (v != 0) {
                        if (!above) renderer.rectVertical(vy * scaleX, (py + 1) * scaleY, scaleX, scaleY, v);
                        if (!below) renderer.rectRight(vy * scaleX, py * scaleY, scaleX, scaleY, v);
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
        return (sizeVX + sizeVY) * 2 -
                ((sizeVX + sizeVY & 1) << 2); // if sizeVX + sizeVY is odd, this is 4, otherwise it is 0 
    }

    public static void drawIso(IModel model, ITriangleRenderer renderer) {
        byte v;
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
                boolean rightEmpty = true;
                v = model.at(sizeVX - px / 2 - 2, 0, 0); // Front right
                if (v != 0) {
                    renderer.drawRightTriangleLeftFace(px + 2, bottomPY - 4, v);
                    renderer.drawLeftTriangleLeftFace(px + 2, bottomPY - 6, v);
                    rightEmpty = false;
                }
                v = model.at(sizeVX - px / 2 - 1, 0, 0); // Center
                if (v != 0) {
                    renderer.drawLeftTriangleLeftFace(px, bottomPY - 4, v);
                    if (rightEmpty)
                        renderer.drawRightTriangleRightFace(px + 2, bottomPY - 4, v);
                }
            } else if (px > sizeVX2 - 2) { // Right side of model
                boolean leftEmpty = true;
                v = model.at(0, px / 2 - sizeVX, 0); // Front left
                if (v != 0) {
                    renderer.drawRightTriangleRightFace(px, bottomPY - 6, v);
                    renderer.drawLeftTriangleRightFace(px, bottomPY - 4, v);
                    leftEmpty = false;
                }
                v = model.at(0, px / 2 - sizeVX + 1, 0); // Center
                if (v != 0) {
                    renderer.drawRightTriangleRightFace(px + 2, bottomPY - 4, v);
                    if (leftEmpty)
                        renderer.drawLeftTriangleLeftFace(px, bottomPY - 4, v);
                }
            } else { // Very bottom
                v = model.at(0, 0, 0);
                if (v != 0) {
                    renderer.drawLeftTriangleLeftFace(px, bottomPY - 4, v);
                    renderer.drawRightTriangleRightFace(px + 2, bottomPY - 4, v);
                    if (sizeVX % 2 == 0)
                        renderer.drawRightTriangleRightFace(px, bottomPY - 6, v);
                } else {
                    v = model.at(px / 2 + 1, 0, 0);
                    if (v != 0)
                        renderer.drawLeftTriangleRightFace(px, bottomPY - 4, v);
                    v = model.at(0, px / 2 - sizeVX + 2, 0);
                    if (v != 0)
                        renderer.drawRightTriangleLeftFace(px + 2, bottomPY - 4, v);
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
                    // x+, y, z- = Below back left
                    // x, y+ z- = Below back right

                    // OK here goes:
                    // x, y-, z+ = Above front left
                    if ((!left || !topLeft) && vx == 0 && vy > 0 && vz < sizeVZ - 1) {
                        v = model.at(vx, vy - 1, vz + 1);
                        if (v != 0) {
                            if (!topLeft) {
                                renderer.drawLeftTriangleRightFace(px, py, v);
                                topLeft = true;
                            }
                            if (!left) {
                                renderer.drawRightTriangleRightFace(px, py - 2, v);
                                left = true;
                            }
                        }
                    }

                    // x-, y, z+ = Above front right
                    if ((!topRight || !right) && vx > 0 && vy == 0 && vz < sizeVZ - 1) {
                        v = model.at(vx - 1, vy, vz + 1);
                        if (v != 0) {
                            if (!topRight) {
                                renderer.drawRightTriangleLeftFace(px + 2, py, v);
                                topRight = true;
                            }
                            if (!right) {
                                renderer.drawLeftTriangleLeftFace(px + 2, py - 2, v);
                                right = true;
                            }
                        }
                    }

                    // x, y, z+ = Above
                    if ((!topLeft || !topRight) && vz < sizeVZ - 1) {
                        v = model.at(vx, vy, vz + 1);
                        if (v != 0) {
                            if (!topLeft) {
                                renderer.drawLeftTriangleLeftFace(px, py, v);
                                topLeft = true;
                            }
                            if (!topRight) {
                                renderer.drawRightTriangleRightFace(px + 2, py, v);
                                topRight = true;
                            }
                        }
                    }

                    // x, y-, z = Front left
                    if (!left && vy > 0) {
                        v = model.at(vx, vy - 1, vz);
                        if (v != 0) {
                            renderer.drawRightTriangleVerticalFace(px, py - 2, v);
                            left = true;
                        }
                    }

                    // x-, y, z = Front right
                    if (!right && vx > 0) {
                        v = model.at(vx - 1, vy, vz);
                        if (v != 0) {
                            renderer.drawLeftTriangleVerticalFace(px + 2, py - 2, v);
                            right = true;
                        }
                    }

                    // x, y, z  = Center
                    if (left && topLeft && topRight && right) break;
                    v = model.at(vx, vy, vz);
                    if (v != 0) {
                        if (!topLeft)
                            renderer.drawLeftTriangleVerticalFace(px, py, v);
                        if (!left)
                            renderer.drawRightTriangleLeftFace(px, py - 2, v);
                        if (!topRight)
                            renderer.drawRightTriangleVerticalFace(px + 2, py, v);
                        if (!right)
                            renderer.drawLeftTriangleRightFace(px + 2, py - 2, v);
                        break;
                    }

                    // x+, y, z = Back left
                    if ((!left || !topLeft) && vx < sizeVX - 1) {
                        v = model.at(vx + 1, vy, vz);
                        if (v != 0) {
                            if (!topLeft) {
                                renderer.drawLeftTriangleRightFace(px, py, v);
                                topLeft = true;
                            }
                            if (!left) {
                                renderer.drawRightTriangleRightFace(px, py - 2, v);
                                left = true;
                            }
                        }
                    }

                    // x, y+, z = Back right
                    if ((!right || !topRight) && vy < sizeVY - 1) {
                        v = model.at(vx, vy + 1, vz);
                        if (v != 0) {
                            if (!topRight) {
                                renderer.drawRightTriangleLeftFace(px + 2, py, v);
                                topRight = true;
                            }
                            if (!right) {
                                renderer.drawLeftTriangleLeftFace(px + 2, py - 2, v);
                                right = true;
                            }
                        }
                    }

                    // x+, y+ z = Back center
                    if ((!topLeft || !topRight) && vx < sizeVX - 1 && vy < sizeVY - 1) {
                        v = model.at(vx + 1, vy + 1, vz);
                        if (v != 0) {
                            if (!topRight) {
                                renderer.drawRightTriangleRightFace(px + 2, py, v);
                                topRight = true;
                            }
                            if (!topLeft) {
                                renderer.drawLeftTriangleLeftFace(px, py, v);
                                topLeft = true;
                            }
                        }
                    }

                    // x+, y, z- = Below back left
                    if (!left && vx < sizeVX - 1 && vz > 0) {
                        v = model.at(vx + 1, vy, vz - 1);
                        if (v != 0) {
                            renderer.drawRightTriangleVerticalFace(px, py - 2, v);
                            left = true;
                        }
                    }

                    // x, y+ z- = Below back right
                    if (!right && vy < sizeVY - 1 && vz > 0) {
                        v = model.at(vx, vy + 1, vz - 1);
                        if (v != 0) {
                            renderer.drawLeftTriangleVerticalFace(px + 2, py - 2, v);
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
                v = model.at(sizeVX - 1, px / 2 + 1, sizeVZ - 1);
                if (v != 0)
                    renderer.drawLeftTriangleVerticalFace(px + 2, topPY, v);
            } else if (px + 2 > sizeVY2) { // Top right triangles
                v = model.at(sizeVY - 1 + sizeVX - px / 2, sizeVY - 1, sizeVZ - 1);
                if (v != 0)
                    renderer.drawRightTriangleVerticalFace(px, topPY, v);
            }
            // Finish drawing top triangles.

            // Drawing right edge (only for when sizeVX + sizeVY is odd numbered)
            if ((sizeVX + sizeVY) % 2 == 1) {
                final int vx = 0, vy = sizeVY - 1,
                        bottom = Math.abs(sizeVX2 - 2 - pixelWidth);
                v = model.at(vx, vy, 0);
                if (v != 0)
                    renderer.drawRightTriangleRightFace(pixelWidth + 2, bottom - 4, v); // lower right corner
                for (int py = bottom; py < bottom + sizeVZ * 4; py += 4) {
                    final int vz = (py - bottom) / 4;
                    boolean aboveEmpty = true;
                    if (vz != sizeVZ - 1) {
                        v = model.at(vx, vy, vz + 1);
                        if (v != 0) {
                            renderer.drawRightTriangleRightFace(pixelWidth + 2, py, v);
                            aboveEmpty = false;
                        }
                    }
                    v = model.at(vx, vy, vz);
                    if (v != 0) {
                        renderer.drawLeftTriangleRightFace(pixelWidth + 2, py - 2, v);
                        if (aboveEmpty)
                            renderer.drawRightTriangleVerticalFace(pixelWidth + 2, py, v);
                    }
                }
            }
            // Finish drawing right edge
        }
    }
}
