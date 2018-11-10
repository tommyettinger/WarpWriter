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

        public LightDirection clockX() {
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

        public LightDirection counterX() {
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

    protected boolean vertVisible = true;

    public boolean vertVisible() {
        return vertVisible;
    }

    /**
     * @param vertVisible Sets whether the vertical face (top or bottom) of the direction the light is pointing is visible from the angle being rendered from. This affects color choice. The left and right faces will use brighter colors if this is false.
     * @return this
     */
    public VoxelColor set(boolean vertVisible) {
        this.vertVisible = vertVisible;
        return this;
    }

    public interface ITwilight {
        int dark(byte voxel);

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
        public int dark(byte voxel) {
            return palette[(voxel & 255) + 1]; // TODO: Make array safe!
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
                : lightDirection.isAbove() ? twilight.bright(voxel) : twilight.dark(voxel);
    }

    @Override
    public int bottomFace(byte voxel) {
        return lightDirection.isHorizontal() ? twilight.twilight(voxel)
                : lightDirection.isBelow() ? twilight.bright(voxel) : twilight.dark(voxel);
    }

    @Override
    public int leftFace(byte voxel) {
        return lightDirection == LightDirection.LEFT ? twilight.bright(voxel)
                : vertVisible ?
                lightDirection.isLeft() ? twilight.twilight(voxel) : twilight.dark(voxel)
                : lightDirection.isLeft() ? twilight.bright(voxel) : twilight.twilight(voxel);
    }

    @Override
    public int rightFace(byte voxel) {
        return lightDirection == LightDirection.RIGHT ? twilight.bright(voxel)
                : vertVisible ?
                lightDirection.isRight() ? twilight.twilight(voxel) : twilight.dark(voxel)
                : lightDirection.isRight() ? twilight.bright(voxel) : twilight.twilight(voxel);
    }
}
