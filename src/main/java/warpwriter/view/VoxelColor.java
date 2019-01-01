package warpwriter.view;

import warpwriter.Coloring;

/**
 * VoxelColor will color voxel cubes in a way that varies based on the direction of the light.
 *
 * @author Ben McLean
 */
public class VoxelColor implements IVoxelColor {
    public enum LightDirection {
        ABOVE_LEFT, ABOVE_RIGHT,
        LEFT_ABOVE, RIGHT_ABOVE,
        LEFT_BELOW, RIGHT_BELOW,
        BELOW_LEFT, BELOW_RIGHT;

        public boolean isAbove() {
            return this == ABOVE_RIGHT || this == ABOVE_LEFT;
        }

        public boolean isHorizontal() {
            return this == LEFT_ABOVE
                    || this == LEFT_BELOW
                    || this == RIGHT_ABOVE
                    || this == RIGHT_BELOW;
        }

        public boolean isBelow() {
            return this == BELOW_LEFT || this == BELOW_RIGHT;
        }

        public boolean isLeft() {
            return this == LEFT_ABOVE
                    || this == LEFT_BELOW
                    || this == ABOVE_LEFT
                    || this == BELOW_LEFT;
        }

        public boolean isRight() {
            return this == RIGHT_ABOVE
                    || this == RIGHT_BELOW
                    || this == ABOVE_RIGHT
                    || this == BELOW_RIGHT;
        }

        public LightDirection above() {
            switch (this) {
                case BELOW_LEFT:
                case LEFT_BELOW:
                case LEFT_ABOVE:
                    return ABOVE_LEFT;
                case BELOW_RIGHT:
                case RIGHT_BELOW:
                case RIGHT_ABOVE:
                    return ABOVE_RIGHT;
                default:
                    return this;
            }
        }

        public LightDirection horizontal() {
            switch (this) {
                case BELOW_LEFT:
                    return LEFT_BELOW;
                case ABOVE_LEFT:
                    return LEFT_ABOVE;
                case BELOW_RIGHT:
                    return RIGHT_BELOW;
                case ABOVE_RIGHT:
                    return RIGHT_ABOVE;
                default:
                    return this;
            }
        }

        public LightDirection below() {
            switch (this) {
                case ABOVE_LEFT:
                case LEFT_ABOVE:
                case LEFT_BELOW:
                    return BELOW_LEFT;
                case ABOVE_RIGHT:
                case RIGHT_ABOVE:
                case RIGHT_BELOW:
                    return BELOW_RIGHT;
                default:
                    return this;
            }
        }

        public LightDirection left() {
            switch (this) {
                case ABOVE_RIGHT:
                    return ABOVE_LEFT;
                case RIGHT_ABOVE:
                    return LEFT_ABOVE;
                case RIGHT_BELOW:
                    return LEFT_BELOW;
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
                case LEFT_ABOVE:
                    return RIGHT_ABOVE;
                case LEFT_BELOW:
                    return RIGHT_BELOW;
                case BELOW_LEFT:
                    return BELOW_RIGHT;
                default:
                    return this;
            }
        }

        public LightDirection clock() {
            switch (this) {
                case ABOVE_RIGHT:
                    return RIGHT_ABOVE;
                case RIGHT_ABOVE:
                    return RIGHT_BELOW;
                case RIGHT_BELOW:
                    return BELOW_RIGHT;
                case BELOW_RIGHT:
                    return BELOW_LEFT;
                case BELOW_LEFT:
                    return LEFT_BELOW;
                case LEFT_BELOW:
                    return LEFT_ABOVE;
                case LEFT_ABOVE:
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
                    return LEFT_ABOVE;
                case LEFT_ABOVE:
                    return LEFT_BELOW;
                case LEFT_BELOW:
                    return BELOW_LEFT;
                case BELOW_LEFT:
                    return BELOW_RIGHT;
                case BELOW_RIGHT:
                    return RIGHT_BELOW;
                case RIGHT_BELOW:
                    return RIGHT_ABOVE;
                case RIGHT_ABOVE:
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
                case RIGHT_ABOVE:
                    return RIGHT_BELOW;
                case RIGHT_BELOW:
                    return RIGHT_ABOVE;
                case LEFT_ABOVE:
                    return LEFT_BELOW;
                case LEFT_BELOW:
                    return LEFT_ABOVE;
                default:
                    return this;
            }
        }

        public LightDirection flipZ() {
            switch (this) {
                case ABOVE_RIGHT:
                    return ABOVE_LEFT;
                case BELOW_RIGHT:
                    return BELOW_LEFT;
                case ABOVE_LEFT:
                    return ABOVE_RIGHT;
                case BELOW_LEFT:
                    return BELOW_RIGHT;
                case RIGHT_BELOW:
                    return LEFT_BELOW;
                case RIGHT_ABOVE:
                    return LEFT_ABOVE;
                case LEFT_BELOW:
                    return RIGHT_BELOW;
                case LEFT_ABOVE:
                    return RIGHT_ABOVE;
                default:
                    return this;
            }
        }

        public LightDirection opposite() {
            switch (this) {
                case ABOVE_RIGHT:
                    return BELOW_LEFT;
                case RIGHT_ABOVE:
                    return LEFT_BELOW;
                case RIGHT_BELOW:
                    return LEFT_ABOVE;
                case BELOW_RIGHT:
                    return ABOVE_LEFT;
                case ABOVE_LEFT:
                    return BELOW_RIGHT;
                case LEFT_BELOW:
                    return RIGHT_ABOVE;
                case LEFT_ABOVE:
                    return RIGHT_BELOW;
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

    protected ITwilight twilight = Twilight.RinsedTwilight;

    public ITwilight twilight() {
        return twilight;
    }

    public VoxelColor set(ITwilight twilight) {
        this.twilight = twilight;
        return this;
    }

    @Override
    public int topFace(byte voxel) {
        switch (lightDirection) {
            case ABOVE_RIGHT:
            case ABOVE_LEFT:
                return twilight.bright(voxel);
            case RIGHT_ABOVE:
            case LEFT_ABOVE:
            default:
                return twilight.twilight(voxel);
            case LEFT_BELOW:
            case RIGHT_BELOW:
                return twilight.dim(voxel);
            case BELOW_RIGHT:
            case BELOW_LEFT:
                return twilight.dark(voxel);
        }
    }

    @Override
    public int rightFace(byte voxel) {
        switch (lightDirection) {
            case RIGHT_ABOVE:
            case RIGHT_BELOW:
                return twilight.bright(voxel);
            case ABOVE_RIGHT:
            case BELOW_RIGHT:
            case LEFT_BELOW:
            default:
                return twilight.twilight(voxel);
            case ABOVE_LEFT:
            case BELOW_LEFT:
            case LEFT_ABOVE:
                return twilight.dim(voxel);
        }
    }

    @Override
    public int leftFace(byte voxel) {
        switch (lightDirection) {
            case LEFT_ABOVE:
            case LEFT_BELOW:
                return twilight.bright(voxel);
            case ABOVE_LEFT:
            case BELOW_LEFT:
            case RIGHT_BELOW:
                return twilight.twilight(voxel);
            default:
            case ABOVE_RIGHT:
            case BELOW_RIGHT:
            case RIGHT_ABOVE:
                return twilight.dim(voxel);
        }
    }

    @Override
    public int bottomFace(byte voxel) {
        switch (lightDirection) {
            case BELOW_RIGHT:
            case BELOW_LEFT:
                return twilight.bright(voxel);
            case RIGHT_BELOW:
            case LEFT_ABOVE: // seems wrong
                return twilight.twilight(voxel);
            case LEFT_BELOW:
            case RIGHT_ABOVE: // seems wrong
                return twilight.dim(voxel);
            case ABOVE_RIGHT:
            case ABOVE_LEFT:
                return twilight.dark(voxel);
        }
        return 0;
    }

    /**
     * SimpleVoxelColor shows one color per voxel, with no shading.
     *
     * @author Ben McLean
     */
    public static class SimpleVoxelColor implements IVoxelColor {
        public static int simple(byte voxel) {
            return Coloring.RINSED[voxel & 255];
        }

        @Override
        public int topFace(byte voxel) {
            return simple(voxel);
        }

        @Override
        public int bottomFace(byte voxel) {
            return simple(voxel);
        }

        @Override
        public int leftFace(byte voxel) {
            return simple(voxel);
        }

        @Override
        public int rightFace(byte voxel) {
            return simple(voxel);
        }
    }

    /***
     * VoxelColor3D is intended to make light behave in a way that is more consistent with physics, where normal VoxelColor tries to always show maximum contrast.
     *
     * @author Ben McLean
     */
    public static class VoxelColor3D extends VoxelColor {
        /**
         * Never underestimate the power of the Dark Side.
         */
        protected boolean darkSide = false;

        public VoxelColor set(boolean darkSide) {
            this.darkSide = darkSide;
            return this;
        }

        public boolean darkSide() {
            return darkSide;
        }

        @Override
        public int topFace(byte voxel) {
            switch (lightDirection) {
                case ABOVE_RIGHT:
                case ABOVE_LEFT:
                    return darkSide ? twilight.twilight(voxel) : twilight.bright(voxel);
                default:
                case RIGHT_ABOVE:
                case LEFT_ABOVE:
                case RIGHT_BELOW:
                case LEFT_BELOW:
                    return darkSide ? twilight.dim(voxel) : twilight.twilight(voxel);
                case BELOW_RIGHT:
                case BELOW_LEFT:
                    return darkSide ? twilight.dark(voxel) : twilight.dim(voxel);
            }
        }

        @Override
        public int bottomFace(byte voxel) {
            switch (lightDirection) {
                case BELOW_LEFT:
                case BELOW_RIGHT:
                    return darkSide ? twilight.twilight(voxel) : twilight.bright(voxel);
                default:
                case LEFT_BELOW:
                case RIGHT_BELOW:
                case LEFT_ABOVE:
                case RIGHT_ABOVE:
                    return darkSide ? twilight.dim(voxel) : twilight.twilight(voxel);
                case ABOVE_LEFT:
                case ABOVE_RIGHT:
                    return darkSide ? twilight.dark(voxel) : twilight.dim(voxel);
            }
        }

        @Override
        public int leftFace(byte voxel) {
            switch (lightDirection) {
                case LEFT_ABOVE:
                case LEFT_BELOW:
                    return darkSide ? twilight.twilight(voxel) : twilight.bright(voxel);
                default:
                    return darkSide ? twilight.dim(voxel) : twilight.twilight(voxel);
            }
        }

        @Override
        public int rightFace(byte voxel) {
            switch (lightDirection) {
                case RIGHT_ABOVE:
                case RIGHT_BELOW:
                    return darkSide ? twilight.twilight(voxel) : twilight.bright(voxel);
                default:
                    return darkSide ? twilight.dim(voxel) : twilight.twilight(voxel);
            }
        }
    }
}
