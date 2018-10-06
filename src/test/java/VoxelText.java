import com.badlogic.gdx.graphics.Pixmap;

public class VoxelText {
    public interface FillRule {
        boolean fill(int color);
    }
    public static FillRule FillRuleColor (final int testColor)
    {
        return new FillRule() {
            @Override
            public boolean fill(int color) {
                return color == testColor;
            }
        };
    }
    public static FillRule FillRuleTransparent ()
    {
        return FillRuleTransparent(0.5f);
    }
    public static FillRule FillRuleTransparent (final float threshold) {
        return new FillRule() {
            @Override
            public boolean fill(int color) {
                return (color & 0xFF) / 255f > threshold;
            }
        };
    }
    public static FillRule FillRuleTransparent (final int threshold) {
        return new FillRule() {
            @Override
            public boolean fill(int color) {
                return (color & 0xFF) > threshold;
            }
        };
    }

    public static byte[][] PixmapToBytes(Pixmap pixmap) {
        return PixmapToBytes(pixmap, (byte)255);
    }

    public static byte[][] PixmapToBytes(Pixmap pixmap, byte fill) {
        return PixmapToBytes(pixmap, fill, FillRuleTransparent());
    }

    public static byte[][] PixmapToBytes(Pixmap pixmap, byte fill, FillRule fillRule)
    {
        byte[][] result = new byte[pixmap.getWidth()][pixmap.getHeight()];
        for (int x = 0; x < pixmap.getWidth(); x++)
            for (int y = 0; y < pixmap.getHeight(); y++) {
                if (fillRule.fill(pixmap.getPixel(x, y)))
                {
                    result[x][y] = fill;
                }
            }
        return result;
    }
}
