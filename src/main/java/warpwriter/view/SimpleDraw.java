package warpwriter.view;

import warpwriter.model.IModel;

public class SimpleDraw {
    protected static IVoxelColor color = new VoxelColor();

    public static void simpleDraw(IModel model, IRenderer renderer) {
        simpleDraw(model, renderer, color);
    }

    public static void simpleDraw(IModel model, IRenderer renderer, IVoxelColor color) {
        for (int z = 0; z < model.zSize(); z++) {
            for (int y = 0; y < model.ySize(); y++) {
                for (int x = 0; x < model.xSize(); x++) {
                    byte result = model.at(x, y, z);
                    if (result != 0) {
                        renderer.drawPixel(y, z, color.rightFace(result));
                        break;
                    }
                }
            }
        }
    }

    public static void simpleDraw45(IModel model, IRenderer renderer) {
        simpleDraw45(model, renderer, color);
    }

    public static void simpleDraw45(IModel model, IRenderer renderer, IVoxelColor color) {
        final int xSize = model.xSize(), ySize = model.ySize(), zSize = model.zSize();
        final int pixelWidth = xSize + ySize;
        for (int py = 0; py < zSize; py += 2) { // pixel y
            for (int px = 0; px < pixelWidth; px++) { // pixel x
                boolean leftDone = false, rightDone = false;
                for (int vx = px - ySize + 1, vy = 0; vx < xSize && vy < ySize; vx++, vy++) { // vx is voxel x, vy is voxel y
                    byte result = model.at(vx, vy, py);
                    if (result != 0) {
                        renderer.drawPixel(px, py, color.leftFace(result));
                        renderer.drawPixel(px, py + 1, color.rightFace(result));
                        break;
                    }
                    if (!leftDone) {
                        result = model.at(vx + 1, vy, py);
                        if (result != 0) {
                            renderer.drawPixel(px, py, color.leftFace(result));
                            leftDone = true;
                        }
                    }
                    if (!rightDone) {
                        result = model.at(vx, vy + 1, py);
                        if (result != 0) {
                            renderer.drawPixel(px, py + 1, color.rightFace(result));
                            rightDone = true;
                        }
                    }
                    if (leftDone && rightDone) break;
                }
            }
        }
    }
}
