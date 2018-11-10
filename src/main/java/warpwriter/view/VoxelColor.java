package warpwriter.view;

import warpwriter.Coloring;

public class VoxelColor implements IVoxelColor {
    public enum LightDirection {
        ABOVE_LEFT, ABOVE_RIGHT,
        LEFT, RIGHT,
        BELOW_LEFT, BELOW_RIGHT;

        public boolean isAbove() {
            return this == ABOVE_RIGHT || this == ABOVE_LEFT;
        }

        public boolean isHorizontal() {
            return this == LEFT || this == RIGHT;
        }

        public boolean isBelow() {
            return this == BELOW_LEFT || this == BELOW_RIGHT;
        }

        public boolean isLeft() {
            return this == LEFT || this == ABOVE_LEFT || this == BELOW_LEFT;
        }

        public boolean isRight() {
            return this == RIGHT || this == ABOVE_RIGHT || this == BELOW_RIGHT;
        }

        public LightDirection above() {
            switch (this) {
                case BELOW_LEFT:
                case LEFT:
                    return ABOVE_LEFT;
                case BELOW_RIGHT:
                case RIGHT:
                    return ABOVE_RIGHT;
                default:
                    return this;
            }
        }

        public LightDirection horizontal() {
            switch (this) {
                case BELOW_LEFT:
                case ABOVE_LEFT:
                    return LEFT;
                case BELOW_RIGHT:
                case ABOVE_RIGHT:
                    return RIGHT;
                default:
                    return this;
            }
        }

        public LightDirection below() {
            switch (this) {
                case ABOVE_LEFT:
                case LEFT:
                    return BELOW_LEFT;
                case ABOVE_RIGHT:
                case RIGHT:
                    return BELOW_RIGHT;
                default:
                    return this;
            }
        }

        public LightDirection left() {
            switch (this) {
                case ABOVE_RIGHT:
                    return ABOVE_LEFT;
                case RIGHT:
                    return LEFT;
                case BELOW_RIGHT:
                    return BELOW_LEFT;
                default:
                    return this;
            }
        }

        public LightDirection right() {
            switch (this) {
                case ABOVE_LEFT:
                    return ABOVE_RIGHT;
                case LEFT:
                    return RIGHT;
                case BELOW_LEFT:
                    return BELOW_RIGHT;
                default:
                    return this;
            }
        }

        public LightDirection clock() {
            switch (this) {
                case ABOVE_RIGHT:
                    return RIGHT;
                case RIGHT:
                    return BELOW_RIGHT;
                case BELOW_RIGHT:
                    return BELOW_LEFT;
                case BELOW_LEFT:
                    return LEFT;
                case LEFT:
                    return ABOVE_LEFT;
                case ABOVE_LEFT:
                    return ABOVE_RIGHT;
                default:
                    return this;
            }
        }

        public LightDirection counter() {
            switch (this) {
                case ABOVE_RIGHT:
                    return ABOVE_LEFT;
                case ABOVE_LEFT:
                    return LEFT;
                case LEFT:
                    return BELOW_LEFT;
                case BELOW_LEFT:
                    return BELOW_RIGHT;
                case BELOW_RIGHT:
                    return RIGHT;
                case RIGHT:
                    return ABOVE_RIGHT;
                default:
                    return this;
            }
        }

        public LightDirection flipY() {
            switch (this) {
                case ABOVE_RIGHT:
                    return BELOW_RIGHT;
                case ABOVE_LEFT:
                    return BELOW_LEFT;
                case BELOW_RIGHT:
                    return ABOVE_RIGHT;
                case BELOW_LEFT:
                    return ABOVE_LEFT;
                default:
                    return this;
            }
        }

        public LightDirection flipZ() {
            switch (this) {
                case ABOVE_RIGHT:
                    return ABOVE_LEFT;
                case RIGHT:
                    return LEFT;
                case BELOW_RIGHT:
                    return BELOW_LEFT;
                case ABOVE_LEFT:
                    return ABOVE_RIGHT;
                case LEFT:
                    return RIGHT;
                case BELOW_LEFT:
                    return BELOW_RIGHT;
                default:
                    return this;
            }
        }

        public LightDirection opposite() {
            switch (this) {
                case ABOVE_RIGHT:
                    return BELOW_LEFT;
                case RIGHT:
                    return LEFT;
                case BELOW_RIGHT:
                    return ABOVE_LEFT;
                case ABOVE_LEFT:
                    return BELOW_RIGHT;
                case LEFT:
                    return RIGHT;
                case BELOW_LEFT:
                    return ABOVE_RIGHT;
                default:
                    return this;
            }
        }
    }

    protected LightDirection lightDirection = LightDirection.ABOVE_RIGHT;

    public LightDirection direction() {
        return lightDirection;
    }

    public VoxelColor set(LightDirection lightDirection) {
        this.lightDirection = lightDirection;
        return this;
    }

    protected boolean lightFromFront = true;

    public boolean lightFromFront() {
        return lightFromFront;
    }

    public VoxelColor set(boolean lightFromFront) {
        this.lightFromFront = lightFromFront;
        return this;
    }

    public interface ITwilight {
        int dark(byte voxel);

        int dim(byte voxel);

        int twilight(byte voxel);

        int bright(byte voxel);
    }

    public static final ITwilight RinsedTwilight = new ITwilight() {
        protected int[] palette = Coloring.RINSED;

        @Override
        public int bright(byte voxel) {
            return palette[(voxel & 255) - 1]; // TODO: Make array safe!
        }

        @Override
        public int twilight(byte voxel) {
            return palette[(voxel & 255)];
        }

        @Override
        public int dim(byte voxel) {
            return palette[(voxel & 255) + 1]; // TODO: Make array safe!
        }

        @Override
        public int dark(byte voxel) {
            return palette[(voxel & 255) + 2]; // TODO: Make array safe!
        }
    };

    protected ITwilight twilight = RinsedTwilight;

    public ITwilight twilight() {
        return twilight;
    }

    public VoxelColor set(ITwilight twilight) {
        this.twilight = twilight;
        return this;
    }

    @Override
    public int topFace(byte voxel) {
        return lightDirection.isHorizontal() ? twilight.twilight(voxel)
                : lightDirection.isAbove() ? twilight.bright(voxel) : twilight.dim(voxel);
    }

    @Override
    public int bottomFace(byte voxel) {
        return lightDirection.isHorizontal() ? twilight.twilight(voxel)
                : lightDirection.isBelow() ? twilight.bright(voxel) : twilight.dim(voxel);
    }

    @Override
    public int leftFace(byte voxel) {
        return lightDirection == LightDirection.LEFT ? twilight.bright(voxel)
                : lightFromFront ?
                lightDirection.isLeft() ? twilight.twilight(voxel) : twilight.dim(voxel)
                : lightDirection.isLeft() ? twilight.dim(voxel) : twilight.dark(voxel);
    }

    @Override
    public int rightFace(byte voxel) {
        return lightDirection == LightDirection.RIGHT ? twilight.bright(voxel)
                : lightFromFront ?
                lightDirection.isRight() ? twilight.twilight(voxel) : twilight.dim(voxel)
                : lightDirection.isRight() ? twilight.dim(voxel) : twilight.dark(voxel);
    }
}
