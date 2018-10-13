package warpwriter.model;

/**
 * This abstract class allows for IFetch implementations to defer to other IFetch instances for different coordinates instead of needing to return a byte.
 *
 * @author Ben McLean
 */
public abstract class Fetch implements IFetch, IFetch2D, IFetch1D {
    /**
     * This method is intended to be overridden with logic to determine which fill to return for the provided coordinate.
     * <p>
     * Returning null indicates to outside code that at(x, y, z) should be called instead.
     *
     * @return null
     */
    public Fetch fetch(int x, int y, int z) {
        return null;
    }

    public Fetch fetch(int x, int z) {
        return fetch(x, 0, z);
    }

    public Fetch fetch(int x) {
        return fetch(x, 0);
    }

    public int xChain, yChain, zChain;

    /**
     * Don't override this method or else you'll break the method chaining!
     *
     * Override bite(int x, int y, int z) instead!
     */
    public byte at(int x, int y, int z) {
        Fetch current = this;
        while (current.getPreviousFetch() != null) {
            current = current.getPreviousFetch();
        }
        Fetch next = current;
        do {
            current = next;
            current.xChain = x;
            current.yChain = y;
            current.zChain = z;
            next = current.fetch(x, y, z);
            x = current.xChain;
            y = current.yChain;
            z = current.zChain;
        } while (next != null);
        return current.bite(x, y, z);
    }

    /**
     * Override this method
     *
     * @return A final answer, with no method chaining
     */
    public byte bite(int x, int y, int z) {
        return 0;
    }

    public byte at(int x, int z) {
        return fetch(x, z).at(x, z);
    }

    public byte at(int x) {
        return fetch(x).at(x);
    }

    private Fetch nextFetch;

    public Fetch getNextFetch() {
        return nextFetch;
    }

    public Fetch setNextFetch(Fetch nextFetch) {
        this.nextFetch = nextFetch;
        return this;
    }

    private Fetch previousFetch;

    public Fetch getPreviousFetch() {
        return previousFetch;
    }

    public Fetch setPreviousFetch(Fetch previousFetch) {
        this.previousFetch = previousFetch;
        return this;
    }

    public Fetch add(Fetch fetch) {
        setPreviousFetch(fetch);
        fetch.setNextFetch(this);
        return this;
    }

    public FetchModel fetchModel(int xSize, int ySize, int zSize) {
        return new FetchModel(this, xSize, ySize, zSize);
    }

    public Fetch offset(int xSize, int ySize, int zSize) {
        return add(new Offset(xSize, ySize, zSize));
    }

    public Fetch loop(int xSize, int ySize, int zSize) {
        return add(new Loop(xSize, ySize, zSize));
    }

    public Fetch arrayModel(byte[][][] bytes) {
        return add(new ArrayModel(bytes));
    }

    public Fetch boxModel(int xSize, int ySize, int zSize, Fetch no) {
        return add(new BoxModel(xSize, ySize, zSize, no));
    }

    public Fetch boxModel(byte[][][] convenience, Fetch no) {
        return add(new BoxModel(convenience, no));
    }
}
