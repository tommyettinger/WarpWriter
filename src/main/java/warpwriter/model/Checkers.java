package warpwriter.model;

/**
 * @author Ben McLean
 */
public class Checkers {
    /**
     * @param x Two positive width values for columns
     * @param z Two positive height values for rows
     */
    public static Fetch checkers(Fetch white, Fetch black, int[] x, int[] z) {
        return checkers(new Fetch[]{white, black}, new Fetch[]{black, white}, x, z);
    }
    
    public static Fetch checkers(Fetch[] a, Fetch[] b, int[] x, int[] z) {
        return new Stripes(x, new Fetch[]{
                new Swapper(Swapper.Swap.zyx).stripes(z, a),
                new Swapper(Swapper.Swap.zyx).stripes(z, b)
        });
    }

    public static Fetch checkers(Fetch white, Fetch black, int size) {
        return checkers(white, black, size, size, size);
    }

    public static Fetch checkers(Fetch white, Fetch black, int x, int z) {
        return checkers(white, black, x, z);
    }

    public static Fetch checkers(Fetch white, Fetch black, int x, int y, int z) {
        return checkers(white, black, x, x, y, y, z, z);
    }

    public static Fetch checkers(Fetch white, Fetch black, int x1, int x2, int y1, int y2, int z1, int z2) {
        return checkers(white, black, new int[]{x1, x2}, new int[]{y1, y2}, new int[]{z1, z2});
    }
    
    /**
     * @param x Two positive width values for columns
     * @param y Two positive height values for rows
     * @param z Two positive depth values for layers
     */
    public static Fetch checkers(Fetch white, Fetch black, int[] x, int[] y, int[] z) {
        Fetch[] a = {white, black};
        Fetch[] b = {black, white};
        return new Stripes(
                x,
                new Fetch[]{
                        new Swapper(Swapper.Swap.yzx).add(checkers(a, b, y, z)),
                        new Swapper(Swapper.Swap.yzx).add(checkers(b, a, y, z))
                }
        );
    }
}
