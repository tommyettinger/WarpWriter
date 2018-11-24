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
                for (int vx = 0, vy = px - sizeX + 1; vx <= sizeX && vy <= sizeY; vx++, vy++) { // vx is voxel x, vy is voxel y
                    if (!leftDone && vy != sizeY - 1) {
                        result = model.at(vx, vy - 1, py);
                        if (result != 0) {
                            renderer.drawPixel(px, py, color.rightFace(result));
                            leftDone = true;
                        }
                    }
                    if (!rightDone && vy != sizeX - 1) {
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

    public static void simpleDrawIso(IModel model, ITriangleRenderer renderer) {
        simpleDrawIso(model, renderer, color);
    }

    public static void simpleDrawIso(IModel model, ITriangleRenderer renderer, IVoxelColor color) {
        byte result;
        final int sizeVX = model.sizeX(), sizeVY = model.sizeY(), sizeVZ = model.sizeZ(),
                sizeVX2 = sizeVX * 2, sizeVY2 = sizeVY * 2,
                pixelWidth = sizeVX2 + sizeVY2 - ((sizeVX + sizeVY) % 2 == 1 ? 4 : 0);
        // To move one x+ in voxels is x + 2, y + 2 in pixels.
        // To move one y+ in voxels is x + 2, y - 2 in pixels.
        // To move one z+ in voxels is y + 4 in pixels.
        for (int px = 0; px < pixelWidth; px += 4) {
            final boolean rightSide = px + 2 > sizeVY2, leftSide = !rightSide;
            final int bottomPY = Math.abs(sizeVY2 - 2 - px),
                    topPY = Math.abs(sizeVY2 - 2) + // black space at the bottom from the first column
                            (sizeVZ - 1) * 4 + // height of model
                            sizeVX2 - Math.abs(sizeVX2 - 2 - px);

            // Begin drawing bottom row triangles
            renderer.drawLeftTriangle(px, bottomPY - 4, Color.rgba8888(Color.OLIVE));
            renderer.drawRightTriangle(px + 2, bottomPY - 4, Color.rgba8888(Color.OLIVE));
            if (px < sizeVY2 - 2) {
                renderer.drawLeftTriangle(px + 2, bottomPY - 6, Color.rgba8888(Color.PURPLE));
            } else if (px > sizeVY2) {
                renderer.drawRightTriangle(px, bottomPY - 6, Color.rgba8888(Color.PURPLE));
            } else if (sizeVY % 2 == 0) {
                result = model.at(0, 0, 0);
                if (result != 0) {
                    renderer.drawRightTriangle(px, bottomPY - 6, color.rightFace(result)); // Very bottom
                }
            }
            // Finish drawing bottom row triangles

            // Begin drawing main bulk of model
            for (int py = bottomPY - 4; py <= topPY; py += 4) {
                final boolean topSide = py > bottomPY + (sizeVZ - 1) * 4, bottomSide = !topSide;
                final int startVX = rightSide ? px / 2 - sizeVY + 1 : 0, // TODO: Fix this to work up top
                        startVY = leftSide ? px / 2 : sizeVY - 1, // TODO: Fix this to work up top
                        startVZ = bottomSide ? (py - bottomPY) / 4 : sizeVZ - 1;

                if (bottomSide) {
                    boolean left = false,
                            topLeft = false,
                            topRight = false,
                            right = false;
                    for (int vx = startVX, vy = startVY, vz = startVZ;
                         vx < sizeVX && vy < sizeVY && vz >= 0;
                         vx++, vy++, vz--) {

                        if (vz != sizeVZ - 1 && vx != sizeVX - 1) {
                            result = model.at(vx - 1, vy, vz + 1);
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

                        if (vz != sizeVZ - 1 && vy != sizeVY - 1) {
                            result = model.at(vx, vy - 1, vz + 1);
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

                        if (vz != sizeVZ - 1) {
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

                        if (!left && vx != 0) {
                            result = model.at(vx - 1, vy, vz);
                            if (result != 0) {
                                renderer.drawRightTriangle(px, py - 2, color.topFace(result));
                                left = true;
                            }
                        }

                        if (!right && vy != 0) {
                            result = model.at(vx, vy - 1, vz);
                            if (result != 0) {
                                renderer.drawLeftTriangle(px + 2, py - 2, color.topFace(result));
                                right = true;
                            }
                        }

                        if (left && topLeft && topRight && right) break;

                        result = model.at(vx, vy, vz);
                        if (result != 0) {
                            if (!topLeft) {
                                renderer.drawLeftTriangle(px, py, color.topFace(result));
                            }
                            if (!topRight) {
                                renderer.drawRightTriangle(px + 2, py, color.topFace(result));
                            }
                            if (!left) {
                                renderer.drawRightTriangle(px, py - 2, color.leftFace(result));
                            }
                            if (!right) {
                                renderer.drawLeftTriangle(px + 2, py - 2, color.rightFace(result));
                            }
                            break;
                        }

                        if (vy < sizeVY - 1) {
                            result = model.at(vx, vy + 1, vz);
                            if (result != 0) {
                                if (!topLeft) {
                                    // TODO: Check this is working
                                    renderer.drawLeftTriangle(px, py, Color.rgba8888(Color.GREEN));
                                    topLeft = true;
                                }
                                if (!left) {
                                    // TODO: Check this is working
                                    renderer.drawRightTriangle(px, py - 2, Color.rgba8888(Color.BLUE));
                                    left = true;
                                }
                            }
                        }

                        if (vx < sizeVX - 1) {
                            result = model.at(vx + 1, vy, vz);
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
                    }
                } else {
                    // Top left:
                    renderer.drawLeftTriangle(px, py, Color.rgba8888(Color.GREEN));
                    // Left:
                    renderer.drawRightTriangle(px, py - 2, Color.rgba8888(Color.BLUE));
                    // Top right:
                    renderer.drawRightTriangle(px + 2, py, Color.rgba8888(Color.YELLOW));
                    // Right:
                    renderer.drawLeftTriangle(px + 2, py - 2, Color.rgba8888(Color.RED));
                }
            }
            // Finish drawing main bulk of model

            // Begin drawing top triangles
            if (px + 2 > sizeVX2) {
                result = model.at(sizeVX - 1, (px / 2) - sizeVX, sizeVZ - 1);
                if (result != 0) {
                    renderer.drawRightTriangle(px, topPY, color.topFace(result));
                }
            } else if (sizeVX % 2 == 0 || sizeVX2 != px + 2) { // Prevent extra triangle from appearing on top
                result = model.at(px / 2, 0, sizeVZ - 1);
                if (result != 0) {
                    renderer.drawLeftTriangle(px + 2, topPY, color.topFace(result));
                }
            }
            // Finish drawing top triangles.
        }

        // Drawing right edge (only for when sizeX is odd numbered)
        if (sizeVX % 2 == 1) {
            final int vx = sizeVX - 1, vy = 0,
                    bottom = Math.abs(sizeVY2 - 2 - pixelWidth);
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
