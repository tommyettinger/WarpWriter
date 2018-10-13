package warpwriter.model;

/**
 * Always returns a solid color. Prevents creating more than 256 ColorFetch objects total.
 *
 * @author Ben McLean
 */
public class ColorFetch extends Fetch {
    private static final ColorFetch[] colors = new ColorFetch[256];
    public static final ColorFetch transparent = color((byte) 0);

    /**
     * Use this method instead of the constructor.
     */
    public static ColorFetch color(byte color) {
        if (colors[color & 255] == null) {
            colors[color & 255] = new ColorFetch(color);
        }
        return colors[color & 255];
    }

    private byte color;

    private ColorFetch(byte color) {
        this.color = color;
    }

    @Override
    public byte bite(int x, int y, int z) {
        return color;
    }
}
