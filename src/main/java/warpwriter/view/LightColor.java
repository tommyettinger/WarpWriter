package warpwriter.view;

import warpwriter.Coloring;

public class LightColor implements IColor {
    protected int[] palette = Coloring.RINSED;
    // vox - 1 is bright
    // vox is normal
    // vox + 1 is dark

    public int[] palette() {
        return palette;
    }

    public LightColor set(int[] palette) {
        this.palette = palette;
        return this;
    }

    public enum Direction {
        ABOVE_RIGHT, ABOVE_LEFT, BELOW_RIGHT, BELOW_LEFT;

        public boolean above() {
            return this == ABOVE_RIGHT || this == ABOVE_LEFT;
        }

        public boolean below() {
            return !above();
        }

        public boolean left() {
            return this == ABOVE_LEFT || this == BELOW_LEFT;
        }

        public boolean right() {
            return !left();
        }

        public Direction zTurn() {
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

        public Direction yTurn() {
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

        public Direction xClock() {
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

        public Direction xCounter() {
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

        public Direction uTurn() {
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

    protected Direction direction = Direction.ABOVE_RIGHT;

    public Direction direction() {
        return direction;
    }

    public LightColor set(Direction direction) {
        this.direction = direction;
        return this;
    }

    public int bright(byte voxel) {
        return palette[(voxel & 255) - 1];
    }

    public int normal(byte voxel) {
        return palette[(voxel & 255)];
    }

    public int dark(byte voxel) {
        return palette[(voxel & 255) + 1];
    }

    @Override
    public int topFace(byte voxel) {
        return direction.above() ? bright(voxel) : dark(voxel);
    }

    @Override
    public int bottomFace(byte voxel) {
        return direction.below() ? bright(voxel) : dark(voxel);
    }

    @Override
    public int leftFace(byte voxel) {
        return direction.left() ? normal(voxel) : dark(voxel);
    }

    @Override
    public int rightFace(byte voxel) {
        return direction.right() ? normal(voxel) : dark(voxel);
    }
}
