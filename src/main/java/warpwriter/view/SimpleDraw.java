package warpwriter.view;

import warpwriter.model.IModel;

public class SimpleDraw {
    protected static SimpleColor simpleColor = new SimpleColor();

    public static void simpleDraw(IModel model, IRenderer renderer) {
        simpleDraw(model, renderer, simpleColor);
    }

    public static void simpleDraw(IModel model, IRenderer renderer, IColor color) {
        for (int z = 0; z < model.zSize(); z++) {
            for (int y = 0; y < model.ySize(); y++) {
                for (int x = 0; x < model.xSize(); x++) {
                    byte result = model.at(x, y, z);
                    if (result != 0) {
                        renderer.drawPixel(y, z, color.vertColor(result));
                        break;
                    }
                }
            }
        }
    }

    public static void simpleDraw45(IModel model, IRenderer renderer) {
        simpleDraw45(model, renderer, simpleColor);
    }

    public static void simpleDraw45(IModel model, IRenderer renderer, IColor color) {
        final int xSize = model.xSize(), ySize = model.ySize(), zSize = model.zSize();
        final int pixelWidth = xSize + ySize;
        for (int px = 0; px < pixelWidth; px++) { // pixel x
            for (int py = 0; py < zSize; py++) {  // pixel y
                for (int a = px, b = 0; a >= 0 && b < xSize; a--, b++) {
                    if (model.at(b - 1, a, py) != 0 && model.at(b, a + 1, py) != 0)
                        break; // we are inside the model here, don't try to render any further
                    byte result = model.at(b, a, py);
                    if (result != 0) {
                        renderer.drawRect(px, (py << 1), 2, 2, color.leftColor(result)); // "<<1" means "*2"
                        break;
                    }
                }
                for (int a = ySize - 1, b = px - ySize + 1; a >= 0 && b < xSize; a--, b++) {
                    if (model.at(b - 1, a, py) != 0 && model.at(b, a + 1, py) != 0)
                        break; // we are inside the model here, don't try to render any further
                    byte result = model.at(b, a, py);
                    if (result != 0) {
                        renderer.drawRect(px, (py << 1), 2, 2, color.leftColor(result)); // "<<1" means "*2"
                        break;
                    }
                }
            }
        }
    }
}
