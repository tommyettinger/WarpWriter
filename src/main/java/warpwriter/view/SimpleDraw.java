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
        byte result, right;
        boolean leftVisible, // true when voxel at x - 1 == 0
                rightVisible; // true when voxel at y + 1 == 0
        for (int px = 0; px < pixelWidth; px++) { // pixel x
            for (int py = 0; py < zSize; py++) { // pixel y
                for (int vx = px - ySize + 1, vy = ySize - 1; vx < xSize && vy >= 0; vx++, vy--) {  // vx is voxel x, vy is voxel y
                    leftVisible = (model.at(vx - 1, vy, py)) == 0;
                    rightVisible = (right = model.at(vx, vy + 1, py)) == 0;
                    if (!leftVisible && !rightVisible) 
                        result = right;// we are inside the model here because of how the diagonal passes through, but
                        // it wouldn't be correct to show the model's guts; instead show an edge color we already found
                    else
                        result = model.at(vx, vy, py);
                    if (result != 0) {
                        renderer.drawRect(px, (py), 1, 1, rightVisible ? color.rightFace(result) : color.leftFace(result));
                        break;
                    }
                }
            }
        }
    }
}
