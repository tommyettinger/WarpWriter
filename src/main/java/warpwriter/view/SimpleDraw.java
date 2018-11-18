package warpwriter.view;

import com.badlogic.gdx.graphics.Color;
import warpwriter.model.IModel;

public class SimpleDraw {
    protected static IVoxelColor color = new VoxelColor();

    public static void simpleDraw(IModel model, IPixelRenderer renderer) {
        simpleDraw(model, renderer, color);
    }

    public static void simpleDraw(IModel model, IPixelRenderer renderer, IVoxelColor color) {
        int sizeX = model.sizeX(), sizeY = model.sizeY(), sizeZ = model.sizeZ();
        for (int z = 0; z < sizeZ; z++) {
            for (int y = 0; y < sizeY; y++) {
                for (int x = sizeX - 1; x >= 0; x--) {
//                    renderer.drawPixel(y, z, Color.rgba8888(Color.RED));
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
        int sizeX = model.sizeX(), sizeY = model.sizeY(), sizeZ = model.sizeZ();
        int pixelWidth = sizeX + sizeY;
        byte result = 0;
        for (int px = 0; px < pixelWidth; px += 2) { // pixel x
            for (int py = 0; py < sizeZ; py++) { // pixel y
//                renderer.drawRect(px, py, 2, 1, Color.rgba8888(Color.RED));
                boolean leftDone = false, rightDone = pixelWidth - px < 2;
                for (int vx = sizeX, vy = px - sizeY; vx >= -1 && vy <= sizeY; vx--, vy++) { // vx is voxel x, vy is voxel y
                    if (!leftDone) {
                        result = model.at(vx, vy - 1, py);
                        if (result != 0) {
                            renderer.drawPixel(px, py, color.rightFace(result));
                            leftDone = true;
                        }
                    }
                    if (!rightDone) {
                        result = model.at(vx + 1, vy, py);
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
        int sizeX = model.sizeX(), sizeY = model.sizeY(), sizeZ = model.sizeZ(),
                sizeX2 = sizeX * 2, sizeY2 = sizeY * 2, pixelWidth = sizeX2 + sizeY2;
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
        for (int px = 0; px < pixelWidth + 1; px += 4) {
            int bottomPY = Math.abs(sizeY2 - px);
            for (int py = bottomPY;
                 py <= bottomPY + sizeZ * 4 + (
                         px < great ?
                                 px < less ?
                                         px * 2
                                         : great + 8
                                 : (pixelWidth - px) * 2
                 );
                 py += 4) {
                renderer.drawLeftTriangle(px, py, Color.rgba8888(Color.GREEN));
                renderer.drawRightTriangle(px, py - 2, Color.rgba8888(Color.BLUE));
                renderer.drawRightTriangle(px + 2, py, Color.rgba8888(Color.YELLOW));
                renderer.drawLeftTriangle(px + 2, py - 2, Color.rgba8888(Color.RED));
            }
        }

    }
}
