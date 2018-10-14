package warpwriter.model;

/**
 * @author Ben McLean
 */
public class Stripes extends Fetch {
    protected int[] widths;
    protected Fetch[] stripes;
    protected int repeat;

    /**
     * @param widths  The width of each stripe. All should be positive
     * @param stripes What to fill each stripe with. Is expected to be the same size as widths. Specify null to indicate that a stripe should defer to the next Fetch
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
    public Fetch fetch(int x, int y, int z) {
        int xStep = Loop.loop(x, repeat);
        int step = 0;
        for (int i = 0; i < widths.length; i++)
            if (step <= xStep)
                step += widths[i];
            else
                return deferFetch(stripes[i]);
        return deferFetch(stripes[0]);
    }

    public Fetch deferFetch(Fetch fetch) {
        return fetch == null ? getNextFetch() : fetch;
    }

    public static Fetch checkers2D(Fetch white, Fetch black, int[] x, int[] z) {
        return new Stripes(x, new Fetch[]{
                new Swapper(Swapper.Swap.zyx).stripes(z, new Fetch[]{white, black}),
                new Swapper(Swapper.Swap.zyx).stripes(z, new Fetch[]{black, white})
        });
    }
}
