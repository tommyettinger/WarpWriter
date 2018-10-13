package warpwriter.model;

public class FetchFactory extends Fetch {
    public FetchFactory() {
    }

    public static class FetchLink extends Fetch {
        @Override
        public IFetch fetch(int x, int y, int z) {
            return null;
        }
    }

    @Override
    public byte at(int x, int y, int z) {
        return 0;
    }
}
