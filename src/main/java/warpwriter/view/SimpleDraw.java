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
                    if (!leftDone) {
                        result = model.at(vx, vy - 1, py);
                        if (result != 0) {
                            renderer.drawPixel(px, py, color.rightFace(result));
                            leftDone = true;
                        }
                    }
                    if (!rightDone) {
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
        final int sizeX = model.sizeX(), sizeY = model.sizeY(), sizeZ = model.sizeZ(),
                sizeX2 = sizeX * 2, sizeY2 = sizeY * 2,
                pixelWidth = sizeX2 + sizeY2 - ((sizeX + sizeY) % 2 == 1 ? 4 : 0);
        int great, less;
        if (sizeX2 >= sizeY2) {
            great = sizeX2;
            less = sizeY2;
        } else {
            great = sizeY2;
            less = sizeX2;
        }
        // To move one x+ in voxels is x + 2, y + 2 in pixels.
        // To move one y+ in voxels is x + 2, y - 2 in pixels.
        // To move one z+ in voxels is y + 4 in pixels.
        for (int px = 0; px < pixelWidth; px += 4) {
            final boolean ascending = px < great && px > less && px > sizeY2;
            final boolean descending = px < great && px > less && px <= sizeY2;
            int bottomPY = Math.abs(sizeY2 - 2 - px),
                    topPY = bottomPY +
                            sizeZ * 4 // Height
                            - 2 + (
                            px < great ?
                                    px <= less ?
                                            px * 2
                                            : px <= sizeY2 ?
                                            sizeX * 3 // Descending
                                            : sizeX2 // Ascending
                                    : (pixelWidth - px) * 2 - (sizeZ % 2 == 1 ? 4 : 0));

            // Begin drawing bottom row triangles
            renderer.drawLeftTriangle(px, bottomPY - 4, Color.rgba8888(Color.PURPLE));
            renderer.drawRightTriangle(px + 2, bottomPY - 4, Color.rgba8888(Color.PURPLE));
            if (px < sizeY2 - 2) {
                renderer.drawLeftTriangle(px + 2, bottomPY - 6, Color.rgba8888(Color.PURPLE));
            } else if (px > sizeY2) {
                renderer.drawRightTriangle(px, bottomPY - 6, Color.rgba8888(Color.PURPLE));
            } else if (sizeY % 2 == 0) {
                renderer.drawRightTriangle(px, bottomPY - 6, Color.rgba8888(Color.WHITE)); // Very bottom
            }
            // Finish drawing bottom row triangles

            class Debug {
                Color a(Color color) {
                    return ascending ?
                            Color.ORANGE
                            : descending ?
                            Color.CYAN
                            : color;
                }
            }

            Debug debug = new Debug();

            // Begin drawing main bulk of model
            for (int py = bottomPY; py <= topPY; py += 4) {
                renderer.drawLeftTriangle(px, py, Color.rgba8888(debug.a(Color.GREEN)));
                renderer.drawRightTriangle(px, py - 2, Color.rgba8888(debug.a(Color.BLUE)));
                renderer.drawRightTriangle(px + 2, py, Color.rgba8888(debug.a(Color.YELLOW)));
                renderer.drawLeftTriangle(px + 2, py - 2, Color.rgba8888(debug.a(Color.RED)));
            }
            // Finish drawing main bulk of model

            // Begin drawing top triangles
            if (px < sizeX2) {
                renderer.drawLeftTriangle(px + 2, topPY, Color.rgba8888(Color.PURPLE));
            } else {
                renderer.drawRightTriangle(px, topPY, Color.rgba8888(Color.PURPLE));
            }
            // Finish drawing top triangles.
        }

        // Drawing right edge (only for when sizeX is odd numbered)
        if (sizeX % 2 == 1) {
            final int bottom = Math.abs(sizeY2 - 2 - pixelWidth);
            renderer.drawRightTriangle(pixelWidth + 2, bottom - 4, Color.rgba8888(Color.WHITE)); // lower right corner
            for (int py = bottom; py < bottom + sizeZ * 4; py += 4) {
                renderer.drawRightTriangle(pixelWidth + 2, py, Color.rgba8888(Color.YELLOW));
                renderer.drawLeftTriangle(pixelWidth + 2, py - 2, Color.rgba8888(Color.GOLDENROD));
            }
        }
        // Finish drawing right edge
    }
}
