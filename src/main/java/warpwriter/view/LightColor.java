package warpwriter.view;

import warpwriter.Coloring;

public class LightColor implements IColor {
    public enum LightDirection {
        ABOVE_RIGHT, ABOVE_LEFT, BELOW_RIGHT, BELOW_LEFT;

        public boolean isAbove() {
            return this == ABOVE_RIGHT || this == ABOVE_LEFT;
        }

        public boolean isBelow() {
            return !isAbove();
        }

        public boolean isLeft() {
            return this == ABOVE_LEFT || this == BELOW_LEFT;
        }

        public boolean isRight() {
            return !isLeft();
        }

        public LightDirection above() {
            return this == BELOW_RIGHT ? ABOVE_RIGHT : this == BELOW_LEFT ? ABOVE_LEFT : this;
        }

        public LightDirection below() {
            return this == ABOVE_RIGHT ? BELOW_RIGHT : this == ABOVE_LEFT ? BELOW_LEFT : this;
        }

        public LightDirection left() {
            return this == ABOVE_RIGHT ? ABOVE_LEFT : this == BELOW_RIGHT ? BELOW_LEFT : this;
        }

        public LightDirection right() {
            return this == ABOVE_LEFT ? ABOVE_RIGHT : this == BELOW_LEFT ? BELOW_RIGHT : this;
        }

        public LightDirection clockX() {
            switch (this) {
                default:
                case ABOVE_RIGHT:
                    return BELOW_RIGHT;
                case BELOW_RIGHT:
                    return BELOW_LEFT;
                case BELOW_LEFT:
                    return ABOVE_LEFT;
                case ABOVE_LEFT:
                    return ABOVE_RIGHT;
            }
        }

        public LightDirection counterX() {
            switch (this) {
                default:
                case ABOVE_RIGHT:
                    return ABOVE_LEFT;
                case ABOVE_LEFT:
                    return BELOW_LEFT;
                case BELOW_LEFT:
                    return BELOW_RIGHT;
                case BELOW_RIGHT:
                    return ABOVE_LEFT;
            }
        }

        public LightDirection turnY() {
            switch (this) {
                default:
                case ABOVE_RIGHT:
                    return BELOW_RIGHT;
                case ABOVE_LEFT:
                    return BELOW_LEFT;
                case BELOW_RIGHT:
                    return ABOVE_RIGHT;
                case BELOW_LEFT:
                    return ABOVE_LEFT;
            }
        }

        public LightDirection turnZ() {
            switch (this) {
                default:
                case ABOVE_RIGHT:
                    return ABOVE_LEFT;
                case ABOVE_LEFT:
                    return ABOVE_RIGHT;
                case BELOW_RIGHT:
                    return BELOW_LEFT;
                case BELOW_LEFT:
                    return BELOW_RIGHT;
            }
        }

        public LightDirection opposite() {
            switch (this) {
                default:
                case ABOVE_RIGHT:
                    return BELOW_LEFT;
                case ABOVE_LEFT:
                    return BELOW_RIGHT;
                case BELOW_LEFT:
                    return ABOVE_RIGHT;
                case BELOW_RIGHT:
                    return ABOVE_LEFT;
            }
        }
    }

    protected LightDirection lightDirection = LightDirection.ABOVE_RIGHT;

    public LightDirection direction() {
        return lightDirection;
    }

    public LightColor set(LightDirection lightDirection) {
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
    public LightColor set(boolean vertVisible) {
        this.vertVisible = vertVisible;
        return this;
    }

    protected int[] palette = Coloring.RINSED;

    public int[] palette() {
        return palette;
    }

    public LightColor set(int[] palette) {
        this.palette = palette;
        return this;
    }

    public int bright(byte voxel) {
        return palette[(voxel & 255) - 1]; // TODO: Make array safe!
    }

    public int normal(byte voxel) {
        return palette[(voxel & 255)];
    }

    public int dark(byte voxel) {
        return palette[(voxel & 255) + 1]; // TODO: Make array safe!
    }

    @Override
    public int topFace(byte voxel) {
        return lightDirection.isAbove() ? bright(voxel) : dark(voxel);
    }

    @Override
    public int bottomFace(byte voxel) {
        return lightDirection.isBelow() ? bright(voxel) : dark(voxel);
    }

    @Override
    public int leftFace(byte voxel) {
        return vertVisible ?
                lightDirection.isLeft() ? normal(voxel) : dark(voxel)
                : lightDirection.isLeft() ? bright(voxel) : normal(voxel);
    }

    @Override
    public int rightFace(byte voxel) {
        return vertVisible ?
                lightDirection.isRight() ? normal(voxel) : dark(voxel)
                : lightDirection.isRight() ? bright(voxel) : normal(voxel);
    }
}
