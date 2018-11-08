package warpwriter.view;

import warpwriter.Coloring;
import warpwriter.model.IModel;

public class SimpleDraw {

    public static void simpleDraw(IModel model, IRenderer renderer) {
        simpleDraw(model, renderer, Coloring.RINSED);
    }

    public static void simpleDraw(IModel model, IRenderer renderer, int[] palette) {
        for (int z = 0; z < model.zSize(); z++)
            for (int y = 0; y < model.ySize(); y++)
                for (int x = 0; x < model.xSize(); x++) {
                    byte result = model.at(x, y, z);
                    if (result != 0) {
                        renderer.drawPixel(y, z, palette[result & 255]);
                        break;
                    }
                }
    }

    public static void simpleDraw45(IModel model, IRenderer renderer) {
        simpleDraw45(model, renderer, Coloring.RINSED);
    }

    public static void simpleDraw45(IModel model, IRenderer renderer, int[] palette) {
        int shorter = model.xSize() < model.ySize() ? model.xSize() : model.ySize();
        for (int z = 0; z < model.zSize(); z++)
            for (int y = 0; y < model.ySize(); y++)
                for (int x = 0; x < shorter; x++) {
                    byte result = model.at(x, y + x, z);
                    if (result != 0) {
                        renderer.drawPixel(y, z, palette[result & 255]);
                        break;
                    }
                }
    }
}
