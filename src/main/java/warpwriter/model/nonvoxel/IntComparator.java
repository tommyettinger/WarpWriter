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
                    // values x as 1024 times more important than z, and y is irrelevant; reversed for x
                    return (right << 10 & 0xFFC00) - (left << 10 & 0xFFC00) + (left >>> 20) - (right >>> 20);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values y as 1024 times more important than z, and x is irrelevant; reversed for y
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
                    // values y as 1024 times more important than reversed z, and x is irrelevant
                    return (left & 0xFFC00) - (right & 0xFFC00) + (right >>> 20) - (left >>> 20);
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
                    // values y as 1024 times more important than reversed z, and x is irrelevant; reversed for y
                    return (right & 0xFFC00) - (left & 0xFFC00) + (right >>> 20) - (left >>> 20);
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
                    // values reversed x and reversed z equally, either as 1024 times more important than y
                    return (right << 20 & 0x3FF00000) - (left << 20 & 0x3FF00000) +
                            (left & 0xFFC00) - (right & 0xFFC00) +
                            (right & 0x3FF00000) - (left & 0x3FF00000);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values reversed y and reversed z equally, either as many times more important than reversed x
                    return (right << 10 & 0x3FF00000) - (left << 10 & 0x3FF00000) +
                            (right & 0x3FF) - (left & 0x3FF) +
                            (right & 0x3FF00000) - (left & 0x3FF00000);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values x and reversed z equally, either as 1024 times more important than reversed y
                    return (left << 20 & 0x3FF00000) - (right << 20 & 0x3FF00000) + 
                            (right & 0xFFC00) - (left & 0xFFC00) +
                            (right & 0x3FF00000) - (left & 0x3FF00000);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values y and reversed z equally, either as many times more important than x
                    return (left << 10 & 0x3FF00000) - (right << 10 & 0x3FF00000) +
                            (left & 0x3FF) - (right & 0x3FF) +
                            (right & 0x3FF00000) - (left & 0x3FF00000);
                }
            },



            //16
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values z and y equally, either as many times more important than reversed x
                    return (left & 0x3FF00000) - (right & 0x3FF00000)
                            + (left << 10 & 0x3FF00000) - (right << 10 & 0x3FF00000)
                            + (right & 0x3FF) - (left & 0x3FF);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values reversed x and z equally, either as 1024 times more important than reversed y
                    return (left & 0x3FF00000) - (right & 0x3FF00000)
                            + (right & 0xFFC00) - (left & 0xFFC00)
                            + (right << 20 & 0x3FF00000) - (left << 20 & 0x3FF00000);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values z and reversed y equally, either as many times more important than x
                    return (left & 0x3FF00000) - (right & 0x3FF00000)
                            + (right << 10 & 0x3FF00000) - (left << 10 & 0x3FF00000)
                            + (left & 0x3FF) - (right & 0x3FF);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values x and z equally, either as 1024 times more important than y
                    return (left & 0x3FF00000) - (right & 0x3FF00000)
                            + (left & 0xFFC00) - (right & 0xFFC00)
                            + (left << 20 & 0x3FF00000) - (right << 20 & 0x3FF00000);
                }
            },
            
            //20
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values reversed z and reversed y equally, either as many times more important than x
                    return (right & 0x3FF00000) - (left & 0x3FF00000)
                            + (right << 10 & 0x3FF00000) - (left << 10 & 0x3FF00000)
                            + (left & 0x3FF) - (right & 0x3FF);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values x and reversed z equally, either as 1024 times more important than y
                    return (right & 0x3FF00000) - (left & 0x3FF00000)
                            + (left & 0xFFC00) - (right & 0xFFC00)
                            + (left << 20 & 0x3FF00000) - (right << 20 & 0x3FF00000);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values reversed z and y equally, either as many times more important than reversed x
                    return (right & 0x3FF00000) - (left & 0x3FF00000)
                            + (left << 10 & 0x3FF00000) - (right << 10 & 0x3FF00000)
                            + (right & 0x3FF) - (left & 0x3FF);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values reversed x and reversed z equally, either as 1024 times more important than reversed y
                    return (right & 0x3FF00000) - (left & 0x3FF00000)
                            + (right & 0xFFC00) - (left & 0xFFC00)
                            + (right << 20 & 0x3FF00000) - (left << 20 & 0x3FF00000);
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
                    return (left & 0xFFC00) - (right & 0xFFC00) + (left >>> 10 & 0xFFC00) - (right >>> 10 & 0xFFC00);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values x equally to z, and y is irrelevant; reversed for x
                    return (right & 0x3FF) - (left & 0x3FF) + (left >>> 20 & 0x3FF) - (right >>> 20 & 0x3FF);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values y equally to z, and x is irrelevant; reversed for y
                    return (right & 0xFFC00) - (left & 0xFFC00) + (left >>> 10 & 0xFFC00) - (right >>> 10 & 0xFFC00);
                }
            },
            
            
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values x equally to reversed y, and z is irrelevant
                    return (right & 0xFFC00) - (left & 0xFFC00) + ((left & 0x3FF) - (right & 0x3FF) << 10);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values y equally to x, and z is irrelevant
                    return (left & 0xFFC00) - (right & 0xFFC00) + ((left & 0x3FF) - (right & 0x3FF) << 10);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values reversed x equally to y, and z is irrelevant
                    return (left & 0xFFC00) - (right & 0xFFC00) + ((right & 0x3FF) - (left & 0x3FF) << 10);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values reversed y equally to reversed x, and z is irrelevant
                    return (right & 0xFFC00) - (left & 0xFFC00) + ((right & 0x3FF) - (left & 0x3FF) << 10);
                }
            },
            
            
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values x equally to reversed z, and y is irrelevant
                    return (left & 0x3FF) - (right & 0x3FF) + (right >>> 20) - (left >>> 20);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values y equally to reversed z, and x is irrelevant
                    return (left & 0xFFC00) - (right & 0xFFC00) + (right >>> 10 & 0xFFC00) - (left >>> 10 & 0xFFC00);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values reversed x equally to reversed z, and y is irrelevant
                    return (right << 20 & 0x3FF00000) - (left << 20 & 0x3FF00000) + (right & 0x3FF00000) - (left & 0x3FF00000);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values reversed y equally to reversed z, and x is irrelevant
                    return (right & 0xFFC00) - (left & 0xFFC00) + (right >>> 10 & 0xFFC00) - (left >>> 10 & 0xFFC00);
                }
            },
            
            
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values x equally to y, and z is irrelevant
                    return (left & 0xFFC00) - (right & 0xFFC00) + ((left & 0x3FF) - (right & 0x3FF) << 10);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values y equally to reversed x, and z is irrelevant
                    return (left & 0xFFC00) - (right & 0xFFC00) + ((right & 0x3FF) - (left & 0x3FF) << 10);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values reversed x equally to reversed y, and z is irrelevant
                    return (right & 0xFFC00) - (left & 0xFFC00) + ((right & 0x3FF) - (left & 0x3FF) << 10);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values reversed y equally to x, and z is irrelevant
                    return (right & 0xFFC00) - (left & 0xFFC00) + ((left & 0x3FF) - (right & 0x3FF) << 10);
                }
            },
            
            
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values z equally to reversed x, and y is irrelevant
                    return ((left & 0x3FF00000) - (right & 0x3FF00000)) + ((right & 0x3FF) - (left & 0x3FF) << 20);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values z equally to y, and x is irrelevant
                    return ((left & 0x3FF00000) - (right & 0x3FF00000)) + ((left & 0xFFC00) - (right & 0xFFC00) << 10);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values z equally to x, and y is irrelevant
                    return ((left & 0x3FF00000) - (right & 0x3FF00000)) + ((left & 0x3FF) - (right & 0x3FF) << 20);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values z equally to reversed y, and x is irrelevant
                    return ((left & 0x3FF00000) - (right & 0x3FF00000)) + ((right & 0xFFC00) - (left & 0xFFC00) << 10);
                }
            },
            
            
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values reversed z equally to x, and y is irrelevant
                    return ((right & 0x3FF00000) - (left & 0x3FF00000)) + ((left & 0x3FF) - (right & 0x3FF) << 20);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values reversed z equally to y, and x is irrelevant
                    return ((right & 0x3FF00000) - (left & 0x3FF00000)) + ((left & 0xFFC00) - (right & 0xFFC00) << 10);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values reversed z equally to reversed x, and y is irrelevant
                    return ((right & 0x3FF00000) - (left & 0x3FF00000)) + ((right & 0x3FF) - (left & 0x3FF) << 20);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values reversed z equally to reversed y, and x is irrelevant
                    return ((right & 0x3FF00000) - (left & 0x3FF00000)) + ((right & 0xFFC00) - (left & 0xFFC00) << 10);
                }
            },

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
                    // values x and y equally, either as 1024 times less important than z; reversed x
                    return (right << 10 & 0xFFC00) - (left << 10 & 0xFFC00)
                            + (left & 0x3FFFFC00) - (right & 0x3FFFFC00);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values x and y equally, either as 1024 times less important than z
                    return (left & 0x3FF00000) - (right & 0x3FF00000)
                            + (right & 0xFFC00) - (left & 0xFFC00)
                            + (right << 10 & 0xFFC00) - (left << 10 & 0xFFC00);
                }
            },
            new IntComparator() {
                @Override
                public int compare(int left, int right) {
                    // values x and y equally, either as 1024 times less important than z
                    return (left & 0x3FF00000) - (right & 0x3FF00000)
                            + (right & 0xFFC00) - (left & 0xFFC00)
                            + (left << 10 & 0xFFC00) - (right << 10 & 0xFFC00);
                }
            },
    };

}
