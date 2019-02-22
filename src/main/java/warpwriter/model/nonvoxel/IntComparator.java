package warpwriter.model.nonvoxel;

/**
 * Created by Tommy Ettinger on 2/16/2019.
 */
public interface IntComparator {
    int compare(int left, int right);
    IntComparator side = new IntComparator() {
        @Override
        public int compare(int left, int right) {
            // values x as 4096 times more important than z, and y is irrelevant
            return ((left << 12 | left >>> 20) & 0x3FFFFF) - ((right << 12 | right >>> 20) & 0x3FFFFF);
        }
    };
    IntComparator side45 = new IntComparator() {
        @Override
        public int compare(int left, int right) {
            // values x and y equally, either as 4096 times more important than z
            return ((left << 12 | left >>> 20) & 0x3FFFFF) - ((right << 12 | right >>> 20) & 0x3FFFFF)
                    + (left << 2 & 0x3FF000) - (right << 2 & 0x3FF000);
        }
    };
}
