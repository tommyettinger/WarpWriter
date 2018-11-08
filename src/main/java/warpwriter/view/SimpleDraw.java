package warpwriter.view;

import warpwriter.Coloring;
import warpwriter.model.IModel;

public class SimpleDraw {

    public static void simpleDraw(IModel model, IRenderer renderer) {
        simpleDraw(model, renderer, Coloring.RINSED);
    }

    public static void simpleDraw(IModel model, IRenderer renderer, int[] palette) {
        for (int y = 0; y <= model.ySize(); y++)
            for (int z = 0; z <= model.zSize(); z++)
                for (int x = 0; x <= model.xSize(); x++) {
                    byte result = model.at(x, y, z);
                    if (result != 0) {
                        renderer.drawPixel(y, z, palette[result & 255]);
                        break;
                    }
                }
    }
}
