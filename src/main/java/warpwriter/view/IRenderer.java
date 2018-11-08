package warpwriter.view;

public interface IRenderer {
    IRenderer drawPixel(int x, int y, int color);
    int getPixel(int x, int y);

    /**
     * Very optional; adjusts this IRenderer, if applicable, by multiplying x and y scaling by the given value. If an
     * implementation does not support adjustable scaling, it should return {@code this} without changes.
     * @param multiplier the multiplier to apply to x and y scaling
     * @return this, usually, for chaining
     */
    IRenderer multiplyScale(float multiplier);

}
