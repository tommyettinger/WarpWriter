package warpwriter.model.nonvoxel;

/**
 * Created by Tommy Ettinger on 2/16/2019.
 */
public interface IntComparator {
    int compare(int left, int right);
    IntComparator[] side = {
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values x as 4096 times more important than z, and y is irrelevant
                    return ((left << 12 | left >>> 20) & 0x3FFFFF) - ((right << 12 | right >>> 20) & 0x3FFFFF);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values y as 1024 times more important than z, and x is irrelevant
                    return (left & 0xFFC00) - (right & 0xFFC00) + (left >>> 20) - (right >>> 20);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values x as 4096 times more important than z, and y is irrelevant; reversed for x
                    return (right << 12 & 0x3FF000) - (left << 12 & 0x3FF000) + (left >>> 20) - (right >>> 20);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values y as 1024 times more important than z, and x is irrelevant; reversed for y
                    return (right & 0xFFC00) - (left & 0xFFC00) + (left >>> 20) - (right >>> 20);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values x as 4096 times more important than reversed y, and z is irrelevant
                    return (right & 0xFFC00) - (left & 0xFFC00) + ((left & 0x3FF) - (right & 0x3FF) << 20);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values y as 4096 times more important than x, and z is irrelevant
                    return (left & 0xFFC00) - (right & 0xFFC00) + ((left & 0x3FF) - (right & 0x3FF));
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values reversed x as 4096 times more important than y, and z is irrelevant
                    return (left & 0xFFC00) - (right & 0xFFC00) + ((right & 0x3FF) - (left & 0x3FF) << 20);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values reversed y as 4096 times more important than reversed x, and z is irrelevant
                    return (right & 0xFFC00) - (left & 0xFFC00) + ((right & 0x3FF) - (left & 0x3FF));
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values x as 4096 times more important than reversed z, and y is irrelevant
                    return (left << 12 & 0x3FF000) - (right << 12 & 0x3FF000) + (right >>> 20) - (left >>> 20);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values y as 1024 times more important than reversed z, and x is irrelevant
                    return (left & 0xFFC00) - (right & 0xFFC00) + (right >>> 20) - (left >>> 20);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values x as 4096 times more important than reversed z, and y is irrelevant; reversed for x
                    return (right << 12 & 0x3FF000) - (left << 12 & 0x3FF000) + (right >>> 20) - (left >>> 20);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values y as 1024 times more important than reversed z, and x is irrelevant; reversed for y
                    return (right & 0xFFC00) - (left & 0xFFC00) + (right >>> 20) - (left >>> 20);
                }
            },

    };
    IntComparator[] side45 = {
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values x and y equally, either as 4096 times more important than z
                    return ((left << 12 | left >>> 20) & 0x3FFFFF) - ((right << 12 | right >>> 20) & 0x3FFFFF)
                            + (left << 2 & 0x3FF000) - (right << 2 & 0x3FF000);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values x and y equally, either as 4096 times more important than z
                    return (left >>> 20 & 0x3FF) - (right >>> 20 & 0x3FF)
                            + (left & 0xFFC00) - (right & 0xFFC00)
                            + (right << 10 & 0xFFC00) - (left << 10 & 0xFFC00);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values x and y equally, either as 4096 times more important than z
                    return (left >>> 20 & 0x3FF) - (right >>> 20 & 0x3FF)
                            + (right & 0xFFC00) - (left & 0xFFC00)
                            + (right << 10 & 0xFFC00) - (left << 10 & 0xFFC00);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values x and y equally, either as 4096 times more important than z
                    return (left >>> 20 & 0x3FF) - (right >>> 20 & 0x3FF)
                            + (right & 0xFFC00) - (left & 0xFFC00)
                            + (left << 10 & 0xFFC00) - (right << 10 & 0xFFC00);
                }
            },
    };
    IntComparator[] above = {
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values x equally to z, and y is irrelevant
                    return (left & 0x3FF) - (right & 0x3FF) + (left >>> 10 & 0xFFC00) - (right >>> 10 & 0xFFC00);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values y equally to z, and x is irrelevant
                    return (left >>> 10 & 0x3FF) - (right >>> 10 & 0x3FF) + (left >>> 10 & 0xFFC00) - (right >>> 10 & 0xFFC00);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values x equally to z, and y is irrelevant; reversed for x
                    return (right & 0x3FF) - (left & 0x3FF) + (left >>> 10 & 0xFFC00) - (right >>> 10 & 0xFFC00);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values y equally to z, and x is irrelevant; reversed for y
                    return (right >>> 10 & 0x3FF) - (left >>> 10 & 0x3FF) + (left >>> 10 & 0xFFC00) - (right >>> 10 & 0xFFC00);
                }
            }
    };
    IntComparator[] above45 = {
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values x and y equally, either as 1024 times less important than z
                    return (left << 10 & 0xFFC00) - (right  << 10 & 0xFFC00)
                            + (left & 0x3FFFFC00) - (right & 0x3FFFFC00);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values x and y equally, either as 4096 times more important than z; reversed x
                    return (right << 10 & 0xFFC00) - (left << 10 & 0xFFC00)
                            + (left & 0x3FFFFC00) - (right & 0x3FFFFC00);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values x and y equally, either as 4096 times more important than z
                    return (left & 0x3FF00000) - (right & 0x3FF00000)
                            + (right & 0xFFC00) - (left & 0xFFC00)
                            + (right << 10 & 0xFFC00) - (left << 10 & 0xFFC00);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values x and y equally, either as 4096 times more important than z
                    return (left & 0x3FF00000) - (right & 0x3FF00000)
                            + (right & 0xFFC00) - (left & 0xFFC00)
                            + (left << 10 & 0xFFC00) - (right << 10 & 0xFFC00);
                }
            },
    };

}
