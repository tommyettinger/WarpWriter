package warpwriter.view;

import com.badlogic.gdx.graphics.Color;
import warpwriter.model.IModel;

public class SimpleDraw {
    protected static IVoxelColor color = new VoxelColor();

    public static void simpleDraw(IModel model, IPixelRenderer renderer) {
        simpleDraw(model, renderer, color);
    }

    public static void simpleDraw(IModel model, IPixelRenderer renderer, IVoxelColor color) {
        int xSize = model.sizeX(), ySize = model.sizeY(), zSize = model.sizeZ();
        for (int z = 0; z < zSize; z++) {
            for (int y = 0; y < ySize; y++) {
                for (int x = 0; x < xSize; x++) {
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
        int xSize = model.sizeX(), ySize = model.sizeY(), zSize = model.sizeZ();
        int pixelWidth = xSize + ySize;
        byte result = 0;
        for (int px = 0; px < pixelWidth; px += 2) { // pixel x
            for (int py = 0; py < zSize; py++) { // pixel y
                boolean leftDone = false, rightDone = false;
                for (int vx = px - ySize, vy = ySize; vx <= xSize && vy >= -1; vx++, vy--) { // vx is voxel x, vy is voxel y
                    if (!leftDone) {
                        result = model.at(vx - 1, vy, py);
                        if (result != 0) {
                            renderer.drawPixel(px, py, color.rightFace(result));
                            leftDone = true;
                        }
                    }
                    if (!rightDone) {
                        result = model.at(vx, vy + 1, py);
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
        renderer.drawLeftTriangle(0, 0, Color.rgba8888(Color.RED));
        renderer.drawRightTriangle(2, 0, Color.rgba8888(Color.BLUE));
    }
}
