package warpwriter.model.nonvoxel;

/**
 * Created by Tommy Ettinger on 2/16/2019.
 */
public interface IntComparator {
    int compare(int left, int right);
    IntComparator[] side = {
            //0
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values x as 1024 times more important than z, and y is irrelevant
                    return (left << 10 & 0xFFC00) - (right << 10 & 0xFFC00) + (left >>> 20) - (right >>> 20);
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
                    // values reversed x as 1024 times more important than z, and y is irrelevant
                    return (right << 10 & 0xFFC00) - (left << 10 & 0xFFC00) + (left >>> 20) - (right >>> 20);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values reversed y as 1024 times more important than z, and x is irrelevant
                    return (right & 0xFFC00) - (left & 0xFFC00) + (left >>> 20) - (right >>> 20);
                }
            },
            
            //4
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values x as 1024 times more important than reversed y, and z is irrelevant
                    return (right & 0xFFC00) - (left & 0xFFC00) + ((left & 0x3FF) - (right & 0x3FF) << 20);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values y as 1024 times more important than x, and z is irrelevant
                    return (left & 0xFFC00) - (right & 0xFFC00) + ((left & 0x3FF) - (right & 0x3FF));
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values reversed x as 1024 times more important than y, and z is irrelevant
                    return (left & 0xFFC00) - (right & 0xFFC00) + ((right & 0x3FF) - (left & 0x3FF) << 20);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values reversed y as 1024 times more important than reversed x, and z is irrelevant
                    return (right & 0xFFC00) - (left & 0xFFC00) + ((right & 0x3FF) - (left & 0x3FF));
                }
            },
            
            //8
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values x as 1024 times more important than reversed z, and y is irrelevant
                    return (left << 10 & 0xFFC00) - (right << 10 & 0xFFC00) + (right >>> 20) - (left >>> 20);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values reversed y as 1024 times more important than reversed z, and x is irrelevant
                    return (right & 0xFFC00) - (left & 0xFFC00) + (right >>> 20) - (left >>> 20);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values x as 1024 times more important than reversed z, and y is irrelevant; reversed for x
                    return (right << 10 & 0xFFC00) - (left << 10 & 0xFFC00) + (right >>> 20) - (left >>> 20);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values y as 1024 times more important than reversed z, and x is irrelevant
                    return (left & 0xFFC00) - (right & 0xFFC00) + (right >>> 20) - (left >>> 20);
                }
            },
            
            //12
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values x as 1024 times more important than y, and z is irrelevant
                    return (left & 0xFFC00) - (right & 0xFFC00) + ((left & 0x3FF) - (right & 0x3FF) << 20);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values y as 1024 times more important than reversed x, and z is irrelevant
                    return (left & 0xFFC00) - (right & 0xFFC00) + ((right & 0x3FF) - (left & 0x3FF));
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values reversed x as 1024 times more important than reversed y, and z is irrelevant
                    return (right & 0xFFC00) - (left & 0xFFC00) + ((right & 0x3FF) - (left & 0x3FF) << 20);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values reversed y as 1024 times more important than x, and z is irrelevant
                    return (right & 0xFFC00) - (left & 0xFFC00) + ((left & 0x3FF) - (right & 0x3FF));
                }
            },
            
            //16
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values z as many times more important than x, and y is irrelevant
                    return ((left & 0x3FF00000) - (right & 0x3FF00000)) + ((left & 0x3FF) - (right & 0x3FF));
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values z as 1024 times more important than y, and x is irrelevant
                    return ((left & 0x3FF00000) - (right & 0x3FF00000)) + (left & 0xFFC00) - (right & 0xFFC00);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values z as many times more important than reversed x, and y is irrelevant
                    return ((left & 0x3FF00000) - (right & 0x3FF00000)) + ((right & 0x3FF) - (left & 0x3FF));
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values z as many times more important than reversed y, and x is irrelevant
                    return ((left & 0x3FF00000) - (right & 0x3FF00000)) + (right & 0xFFC00) - (left & 0xFFC00);
                }
            },
            
            //20
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values reversed z as many times more important than x, and y is irrelevant
                    return ((right & 0x3FF00000) - (left & 0x3FF00000)) + ((left & 0x3FF) - (right & 0x3FF));
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values reversed z as 1024 times more important than y, and x is irrelevant
                    return ((right & 0x3FF00000) - (left & 0x3FF00000)) + (left & 0xFFC00) - (right & 0xFFC00);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values reversed z as many times more important than reversed x, and y is irrelevant
                    return ((right & 0x3FF00000) - (left & 0x3FF00000)) + ((right & 0x3FF) - (left & 0x3FF));
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values reversed z as many times more important than reversed y, and x is irrelevant
                    return ((right & 0x3FF00000) - (left & 0x3FF00000)) + (right & 0xFFC00) - (left & 0xFFC00);
                }
            },
    };
    IntComparator[] side45 = {
            //0
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values x and y equally, either as 1024 times more important than z
                    return (left >>> 20) - (right >>> 20)
                            + (left << 10 & 0xFFC00) - (right << 10 & 0xFFC00)
                            + (left & 0xFFC00) - (right & 0xFFC00);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values reversed x and y equally, either as 1024 times more important than z
                    return (left >>> 20) - (right >>> 20)
                            + (left & 0xFFC00) - (right & 0xFFC00)
                            + (right << 10 & 0xFFC00) - (left << 10 & 0xFFC00);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values reversed x and reversed y equally, either as 1024 times more important than z
                    return (left >>> 20) - (right >>> 20)
                            + (right & 0xFFC00) - (left & 0xFFC00)
                            + (right << 10 & 0xFFC00) - (left << 10 & 0xFFC00);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values x and reversed y equally, either as 1024 times more important than z
                    return (left >>> 20) - (right >>> 20)
                            + (right & 0xFFC00) - (left & 0xFFC00)
                            + (left << 10 & 0xFFC00) - (right << 10 & 0xFFC00);
                }
            },
            
            //4
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values x and z equally, either as 1024 times more important than reversed y
                    return (left << 20 & 0x3FF00000) - (right << 20 & 0x3FF00000) + (right & 0xFFC00) - (left & 0xFFC00) +
                            (left & 0x3FF00000) - (right & 0x3FF00000);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values y and z equally, either as many times more important than x
                    return (left << 10 & 0x3FF00000) - (right << 10 & 0x3FF00000) + (left & 0x3FF) - (right & 0x3FF) +
                            (left & 0x3FF00000) - (right & 0x3FF00000);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values reversed x and z equally, either as 1024 times more important than y
                    return (right << 20 & 0x3FF00000) - (left << 20 & 0x3FF00000) + (left & 0xFFC00) - (right & 0xFFC00) +
                            (left & 0x3FF00000) - (right & 0x3FF00000);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values reversed y and z equally, either as many times more important than reversed x
                    return (right << 10 & 0x3FF00000) - (left << 10 & 0x3FF00000) + (right & 0x3FF) - (left & 0x3FF) +
                            (left & 0x3FF00000) - (right & 0x3FF00000);
                }
            },
            
            //8
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values x and y equally, either as 1024 times more important than reversed z
                    return (right >>> 20) - (left >>> 20) +
                            (left & 0xFFC00) - (right & 0xFFC00) +
                            (left << 10 & 0xFFC00) - (right << 10 & 0xFFC00);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values x and reversed y equally, either as 1024 times more important than reversed z
                    return (right >>> 20) - (left >>> 20)
                            + (right & 0xFFC00) - (left & 0xFFC00)
                            + (left << 10 & 0xFFC00) - (right << 10 & 0xFFC00);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values reversed x and reversed y equally, either as 1024 times more important than reversed z
                    return (right >>> 20) - (left >>> 20)
                            + (right & 0xFFC00) - (left & 0xFFC00)
                            + (right << 10 & 0xFFC00) - (left << 10 & 0xFFC00);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values reversed x and y equally, either as 1024 times more important than reversed z
                    return (right >>> 20) - (left >>> 20)
                            + (left & 0xFFC00) - (right & 0xFFC00)
                            + (right << 10 & 0xFFC00) - (left << 10 & 0xFFC00);
                }
            },


            //12
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values reversed y and reversed x equally, either as many times more important than z
                    return (left >>> 20 & 0x3FF) - (right >>> 20 & 0x3FF) +
                            (right << 10 & 0x3FF00000) - (left << 10 & 0x3FF00000) +
                            (right << 20 & 0x3FF00000) - (left << 20 & 0x3FF00000);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values reversed y and x equally, either as many times more important than z
                    return (left >>> 20 & 0x3FF) - (right >>> 20 & 0x3FF) +
                            (right << 10 & 0x3FF00000) - (left << 10 & 0x3FF00000) +
                            (left << 20 & 0x3FF00000) - (right << 20 & 0x3FF00000);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values y and x equally, either as many times more important than z
                    return (left >>> 20 & 0x3FF) - (right >>> 20 & 0x3FF) +
                            (left << 10 & 0x3FF00000) - (right << 10 & 0x3FF00000) +
                            (left << 20 & 0x3FF00000) - (right << 20 & 0x3FF00000);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values reversed x and y equally, either as many times more important than z
                    return (left >>> 20 & 0x3FF) - (right >>> 20 & 0x3FF) +
                            (left << 10 & 0x3FF00000) - (right << 10 & 0x3FF00000) +
                            (right << 20 & 0x3FF00000) - (left << 20 & 0x3FF00000);
                }
            },
            
            //16
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values reversed x and y equally, either as many times more important than z
                    return (left >>> 20 & 0x3FF) - (right >>> 20 & 0x3FF)
                            + (left << 10 & 0x3FF00000) - (right << 10 & 0x3FF00000)
                            + (right << 20 & 0x3FF00000) - (left << 20 & 0x3FF00000);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values reversed y and reversed x equally, either as many times more important than z
                    return (left >>> 20 & 0x3FF) - (right >>> 20 & 0x3FF)
                            + (right << 10 & 0x3FF00000) - (left << 10 & 0x3FF00000)
                            + (right << 20 & 0x3FF00000) - (left << 20 & 0x3FF00000);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values x and reversed y equally, either as many times more important than z
                    return (left >>> 20 & 0x3FF) - (right >>> 20 & 0x3FF)
                            + (right << 10 & 0x3FF00000) - (left << 10 & 0x3FF00000)
                            + (left << 20 & 0x3FF00000) - (right << 20 & 0x3FF00000);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values y and x equally, either as many times more important than z
                    return (left >>> 20 & 0x3FF) - (right >>> 20 & 0x3FF)
                            + (left & 0xFFC00) - (right & 0xFFC00)
                            + (left << 10 & 0xFFC00) - (right << 10 & 0xFFC00);
                }
            },
            
            //20
            // values reversed x and reversed y equally, either as many times more important than z
            // values reversed y and x equally, either as many times more important than z
            // values x and y equally, either as many times more important than z
            // values y and reversed x equally, either as many times more important than z

            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values reversed x and reversed y equally, either as many times more important than z
                    return (left >>> 20 & 0x3FF) - (right >>> 20 & 0x3FF)
                            + (right << 10 & 0x3FF00000) - (left << 10 & 0x3FF00000)
                            + (right << 20 & 0x3FF00000) - (left << 20 & 0x3FF00000);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values reversed y and x equally, either as many times more important than z
                    return (left >>> 20 & 0x3FF) - (right >>> 20 & 0x3FF)
                            + (right & 0xFFC00) - (left & 0xFFC00)
                            + (left << 10 & 0xFFC00) - (right << 10 & 0xFFC00);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values x and y equally, either as many times more important than z
                    return (left >>> 20 & 0x3FF) - (right >>> 20 & 0x3FF)
                            + (left << 10 & 0x3FF00000) - (right << 10 & 0x3FF00000)
                            + (left << 20 & 0x3FF00000) - (right << 20 & 0x3FF00000);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values y and reversed x equally, either as many times more important than z
                    return (left >>> 20 & 0x3FF) - (right >>> 20 & 0x3FF)
                            + (left & 0xFFC00) - (right & 0xFFC00)
                            + (right << 10 & 0xFFC00) - (left << 10 & 0xFFC00);
                }
            },
    };
    
    /*
    //side
                //0
                    // values x as 1024 times more important than z, and y is irrelevant
                    // values y as 1024 times more important than z, and x is irrelevant
                    // values reversed x as 1024 times more important than z, and y is irrelevant
                    // values reversed y as 1024 times more important than z, and x is irrelevant

                //20
                    // values reversed z as many times more important than x, and y is irrelevant
                    // values reversed z as many times more important than y, and x is irrelevant
                    // values reversed z as many times more important than reversed x, and y is irrelevant
                    // values reversed z as many times more important than reversed y, and x is irrelevant
                    
    //notes?
                //20 side-like
                    // values reversed x as many times more important than z, and y is irrelevant
                    // values reversed y as many times more important than z, and x is irrelevant
                    // values x as many times more important than z, and y is irrelevant
                    // values y as many times more important than z, and x is irrelevant

                //20 side45-like
                    // values reversed x and reversed y equally, either as many times more important than z
                    // values reversed y and x equally, either as many times more important than z
                    // values x and y equally, either as many times more important than z
                    // values y and reversed x equally, either as many times more important than z
                    
    
    //side45
                //0
                    // values x and y equally, either as 1024 times more important than z
                    // values reversed x and y equally, either as 1024 times more important than z
                    // values reversed x and reversed y equally, either as 1024 times more important than z
                    // values x and reversed y equally, either as 1024 times more important than z
                //16
                    // values z and y equally, either as many times more important than x
                    // values reversed x and z equally, either as 1024 times more important than y
                    // values z and reversed y equally, either as many times more important than reversed x
                    // values x and z equally, either as 1024 times more important than reversed y

                //20
                    // values reversed z and reversed y equally, either as many times more important than x
                    // values reversed z and x equally, either as 1024 times more important than y
                    // values reversed z and y equally, either as many times more important than reversed x
                    // values reversed x and reversed z equally, either as 1024 times more important than reversed y

     */
}
