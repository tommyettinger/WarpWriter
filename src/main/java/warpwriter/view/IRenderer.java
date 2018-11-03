package warpwriter.view;

public interface IRenderer {
    IRenderer drawPixel(int x, int y, int color);
    int getPixel(int x, int y);
}
