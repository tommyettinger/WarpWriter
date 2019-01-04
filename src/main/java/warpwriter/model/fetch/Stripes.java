package warpwriter.model.fetch;

import warpwriter.model.Fetch;

/**
 * @author Ben McLean
 */
public class Stripes extends Fetch {
    protected int[] widths;
    protected Fetch[] stripes;
    protected int repeat;

    /**
     * @param widths  The width of each stripe. All should be positive
     * @param stripes What to fetch each stripe with. Is expected to be the same size as widths. Specify null to indicate that a stripe should defer to the next Fetch
     */
    public Stripes(int[] widths, Fetch[] stripes) {
        this.widths = widths;
        this.stripes = stripes;
        repeat();
    }

    public Stripes(Fetch[] stripes, int[] widths) {
        this(widths, stripes);
    }

    private int repeat() {
        repeat = 0;
        for (int stripe : widths)
            repeat += stripe;
        return repeat;
    }

    public int getRepeat() {
        return repeat;
    }

    @Override
    public Fetch fetch() {
        final int z = chainZ(), xStep = Loop.loop(z, repeat);
        int step = 0;
        for (int i = 0; i < widths.length; i++)
            if (step <= xStep)
                step += widths[i];
            else
                return deferFetch(stripes[i]);
        return deferFetch(stripes[0]);
    }

    /**
     * @param y Two positive height values for rows
     * @param z Two positive width values for columns
     */
    public static Fetch checkers(Fetch white, Fetch black, int[] y, int[] z) {
        return checkers(new Fetch[]{white, black}, new Fetch[]{black, white}, y, z);
    }

    public static Fetch checkers(Fetch[] a, Fetch[] b, int[] y, int[] z) {
        return new Stripes(y, new Fetch[]{
                new Swapper(Swapper.Swap.zyx).stripes(z, a),
                new Swapper(Swapper.Swap.zyx).stripes(z, b)
        });
    }

    public static Fetch checkers(Fetch white, Fetch black, int size) {
        return checkers(white, black, size, size, size);
    }

    public static Fetch checkers(Fetch white, Fetch black, int y, int z) {
        return checkers(white, black, y, z);
    }

    public static Fetch checkers(Fetch white, Fetch black, int x, int y, int z) {
        return checkers(white, black, x, x, y, y, z, z);
    }

    public static Fetch checkers(Fetch white, Fetch black, int x1, int x2, int y1, int y2, int z1, int z2) {
        return checkers(white, black, new int[]{x1, x2}, new int[]{y1, y2}, new int[]{z1, z2});
    }

    /**
     * @param x Two positive depth values for layers
     * @param y Two positive height values for rows
     * @param z Two positive width values for columns
     */
    public static Fetch checkers(Fetch white, Fetch black, int[] x, int[] y, int[] z) {
        if (x == null)
            return checkers(white, black, y, z);
        if (y == null)
            return checkers(white, black, x, z);
        if (z == null)
            return checkers(white, black, x, y);
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
