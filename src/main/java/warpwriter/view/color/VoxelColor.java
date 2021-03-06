package warpwriter.view.color;

import squidpony.squidmath.FastNoise;
import warpwriter.Coloring;
import warpwriter.model.IVoxelSeq;
import warpwriter.model.color.Colorizer;

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

    public VoxelColor direction(LightDirection lightDirection) {
        this.lightDirection = lightDirection;
        return this;
    }

    protected int time = 0;

    /**
     * Gets the current time variable, which is typically measured in milliseconds but can be set however library users
     * want (faster or slower animation rates can be achieved with different rates of change than 1ms-to-1-time.
     * @return the current time variable, usually in milliseconds.
     */
    public int time() {
        return time;
    }
    
    /**
     * Sets the current time variable, which is typically measured in milliseconds but can be set however library users
     * want (faster or slower animation rates can be achieved with different rates of change than 1ms-to-1-time.
     * @param time the time to set for animated effects when the palette is capable of them; usually in milliseconds.
     * @return this for chaining
     */
    public VoxelColor time(int time) {
        this.time = time;
        return this;
    }

    protected Colorizer colorizer = Colorizer.SplayColorizer;
    
    protected int shadeBit = colorizer.getShadeBit();
    protected int waveBit = colorizer.getWaveBit();
    protected FastNoise noise = null;
    
    public Colorizer colorizer() {
        return colorizer;
    }

    public VoxelColor colorizer(Colorizer twilight) {
        this.colorizer = twilight;
        shadeBit = colorizer.getShadeBit();
        waveBit = colorizer.getWaveBit();
        if(waveBit != 0 && waveBit != shadeBit)
        {
            noise = new FastNoise(0x1337BEEF, 0.125f, FastNoise.SIMPLEX_FRACTAL, 2);
        }
        return this;
    }

    public static int mixLightly(int baseColor, int mixColor)
    {
        final int
                r = (baseColor >>> 26) * 3 + (mixColor >>> 26),
                g = (baseColor >>> 18 & 0x3F) * 3 + (mixColor >>> 18 & 0x3F),
                b = (baseColor >>> 10 & 0x3F) * 3 + (mixColor >>> 10 & 0x3F);
        return r << 24 | g << 16 | b << 8 | 0xFF;
    }
    public static int mixThird(int baseColor, int mixColor)
    {
        final int
                r = ((baseColor >>> 23 & 0x1FE) + (mixColor >>> 24)) / 3,
                g = ((baseColor >>> 15 & 0x1FE) + (mixColor >>> 16 & 0xFF)) / 3,
                b = ((baseColor >>> 7 & 0x1FE) + (mixColor >>> 8 & 0xFF)) / 3;
        return r << 24 | g << 16 | b << 8 | 0xFF;
    }

    public static int mixEvenly(int baseColor, int mixColor)
    {
        final int
                r = (baseColor >>> 25) + (mixColor >>> 25),
                g = (baseColor >>> 17 & 0x7F) + (mixColor >>> 17 & 0x7F),
                b = (baseColor >>> 9 & 0x7F) + (mixColor >>> 9 & 0x7F);
        return r << 24 | g << 16 | b << 8 | 0xFF;
    }

    public static int mixHeavily(int baseColor, int mixColor)
    {
        final int
                r = (mixColor >>> 26) * 3 + (baseColor >>> 26),
                g = (mixColor >>> 18 & 0x3F) * 3 + (baseColor >>> 18 & 0x3F),
                b = (mixColor >>> 10 & 0x3F) * 3 + (baseColor >>> 10 & 0x3F);
        return r << 24 | g << 16 | b << 8 | 0xFF;
    }

    public static int mix(int baseColor, int mixColor, float amount)
    {
        final float i = 1f - amount;
        final int
                r = (int) ((baseColor >>> 24) * i + (mixColor >>> 24) * amount),
                g = (int) ((baseColor >>> 16 & 0xFF) * i + (mixColor >>> 16 & 0xFF) * amount),
                b = (int) ((baseColor >>> 8 & 0xFF) * i + (mixColor >>> 8 & 0xFF) * amount);
        return r << 24 | g << 16 | b << 8 | (baseColor & 0xFF);
    }
    public static int darken(int baseColor, float amount)
    {
        amount = 1f - amount;
        final int
                r = (int) ((baseColor >>> 24) * amount),
                g = (int) ((baseColor >>> 16 & 0xFF) * amount),
                b = (int) ((baseColor >>> 8 & 0xFF) * amount);
        return r << 24 | g << 16 | b << 8 | (baseColor & 0xFF);
    }
    
    @Override
    public int verticalFace(byte voxel) {
        switch (lightDirection) {
            case ABOVE_RIGHT:
            case ABOVE_LEFT:
                return colorizer.bright(voxel);
            case LEFT_BELOW:
            case RIGHT_BELOW:
                return colorizer.dim(voxel);
            case BELOW_RIGHT:
            case BELOW_LEFT:
                return colorizer.dark(voxel);
            case RIGHT_ABOVE:
            case LEFT_ABOVE:
            default:
                return colorizer.medium(voxel);
        }
    }

    public int reverseVerticalFace(byte voxel) {
        switch (lightDirection) {
            case BELOW_RIGHT:
            case BELOW_LEFT:
                return colorizer.bright(voxel);
            case RIGHT_ABOVE:
            case LEFT_ABOVE:
                return colorizer.dim(voxel);
            case ABOVE_RIGHT:
            case ABOVE_LEFT:
                return colorizer.dark(voxel);
            case LEFT_BELOW:
            case RIGHT_BELOW:
            default:
                return colorizer.medium(voxel);
        }
    }

    @Override
    public int rightFace(byte voxel) {
        switch (lightDirection) {
            case RIGHT_ABOVE:
            case RIGHT_BELOW:
                return colorizer.bright(voxel);
            case ABOVE_LEFT:
            case BELOW_LEFT:
            case LEFT_ABOVE:
                return colorizer.dim(voxel);
            case ABOVE_RIGHT:
            case BELOW_RIGHT:
            case LEFT_BELOW:
            default:
                return colorizer.medium(voxel);
        }
    }

    @Override
    public int leftFace(byte voxel) {
        switch (lightDirection) {
            case LEFT_ABOVE:
            case LEFT_BELOW:
                return colorizer.bright(voxel);
            case ABOVE_LEFT:
            case BELOW_LEFT:
            case RIGHT_BELOW:
                return colorizer.medium(voxel);
            default:
            case ABOVE_RIGHT:
            case BELOW_RIGHT:
            case RIGHT_ABOVE:
                return colorizer.dim(voxel);
        }
    }
    
    protected int processNoise(int x, int y, int z)
    { 
        float a = noise.getConfiguredNoise(x, y, z, time);
        a = a * 0.5f + 0.5f;
        return (int) (a * a * (9.0f - 6.0f * a)) + 1;
    }
    

    @Override
    public int verticalFace(byte voxel, int x, int y, int z) {
        if((voxel & waveBit) != 0)
        {
            if((voxel & shadeBit) != 0)
            {
                final int brightness = (voxel + time & 3);
                return colorizer.dimmer(brightness + 1 - (brightness & (brightness << 1)), voxel);
            }
            else
            {
                return colorizer.dimmer(processNoise(x, y, z), voxel);
            }
        }
        return verticalFace(voxel);
    }
    public int reverseVerticalFace(byte voxel, int x, int y, int z) {
        if((voxel & waveBit) != 0)
        {
            if((voxel & shadeBit) != 0)
            {
                final int brightness = (voxel + time & 3);
                return colorizer.dimmer(brightness + 1 - (brightness & (brightness << 1)), voxel);
            }
            else
            {
                return colorizer.dimmer(processNoise(x, y, z), voxel);
            }
        }
        return reverseVerticalFace(voxel);
    }

    @Override
    public int rightFace(byte voxel, int x, int y, int z) {
        if((voxel & waveBit) != 0)
        {
            if((voxel & shadeBit) != 0)
            {
                final int brightness = (voxel + time & 3);
                return colorizer.dimmer(brightness + 1 - (brightness & (brightness << 1)), voxel);
            }
            else
            {
                return colorizer.dimmer(processNoise(x, y, z), voxel);
            }
        }
        return rightFace(voxel);
    }

    @Override
    public int leftFace(byte voxel, int x, int y, int z) {
        if((voxel & waveBit) != 0)
        {
            if((voxel & shadeBit) != 0)
            {
                final int brightness = (voxel + time & 3);
                return colorizer.dimmer(brightness + 1 - (brightness & (brightness << 1)), voxel);
            }
            else
            {
                return colorizer.dimmer(processNoise(x, y, z), voxel);
            }
        }
        return leftFace(voxel);
    }
    
    public int spotColor(byte voxel, int x, int y, int z, IVoxelSeq seq)
    {
        final byte 
                xh = seq.getRotated(x + 1, y, z),
                yh = seq.getRotated(x, y + 1, z),
                zh = seq.getRotated(x, y, z + 1),
                zl = seq.getRotated(x, y, z - 1);
        if(zh == 0)
        {
            if(xh == 0 && yh != 0)
                return mixLightly(leftFace(voxel, x, y, z), verticalFace(voxel, x, y, z));
            else if(yh == 0 && xh != 0)
                return mixLightly(rightFace(voxel, x, y, z), verticalFace(voxel, x, y, z));
            else
                return verticalFace(voxel, x, y, z);
        }
        else if(zl == 0)
        {
            if(xh == 0 && yh != 0)
                return mixLightly(leftFace(voxel, x, y, z), reverseVerticalFace(voxel, x, y, z));
            else if(yh == 0 && xh != 0)
                return mixLightly(rightFace(voxel, x, y, z), reverseVerticalFace(voxel, x, y, z));
            else
                return reverseVerticalFace(voxel, x, y, z);
        }
        else 
        {
            if(xh == 0 && yh != 0)
                return leftFace(voxel, x, y, z);
            else if(yh == 0 && xh != 0)
                return rightFace(voxel, x, y, z);
            else
                return mixEvenly(leftFace(voxel, x, y, z), rightFace(voxel, x, y, z));
            
        }
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
        public int verticalFace(byte voxel) {
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

        @Override
        public int verticalFace(byte voxel, int x, int y, int z) {
            return simple(voxel);
        }

        @Override
        public int leftFace(byte voxel, int x, int y, int z) {
            return simple(voxel);
        }

        @Override
        public int rightFace(byte voxel, int x, int y, int z) {
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
        public int verticalFace(byte voxel) {
            switch (lightDirection) {
                case ABOVE_RIGHT:
                case ABOVE_LEFT:
                    return darkSide ? colorizer.medium(voxel) : colorizer.bright(voxel);
                default:
                case RIGHT_ABOVE:
                case LEFT_ABOVE:
                case RIGHT_BELOW:
                case LEFT_BELOW:
                    return darkSide ? colorizer.dim(voxel) : colorizer.medium(voxel);
                case BELOW_RIGHT:
                case BELOW_LEFT:
                    return darkSide ? colorizer.dark(voxel) : colorizer.dim(voxel);
            }
        }

        @Override
        public int leftFace(byte voxel) {
            switch (lightDirection) {
                case LEFT_ABOVE:
                case LEFT_BELOW:
                    return darkSide ? colorizer.medium(voxel) : colorizer.bright(voxel);
                default:
                    return darkSide ? colorizer.dim(voxel) : colorizer.medium(voxel);
            }
        }

        @Override
        public int rightFace(byte voxel) {
            switch (lightDirection) {
                case RIGHT_ABOVE:
                case RIGHT_BELOW:
                    return darkSide ? colorizer.medium(voxel) : colorizer.bright(voxel);
                default:
                    return darkSide ? colorizer.dim(voxel) : colorizer.medium(voxel);
            }
        }
    }
}
