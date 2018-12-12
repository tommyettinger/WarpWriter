package warpwriter.view;

import warpwriter.Coloring;

/**
 * @author Ben McLean
 */
public class VoxelColor implements IVoxelColor {
    public enum LightDirection {
        ABOVE_LEFT, ABOVE_RIGHT,
        LEFT_ABOVE, LEFT_BELOW, RIGHT_ABOVE, RIGHT_BELOW,
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

    public interface ITwilight {
        int dark(byte voxel);

        int dim(byte voxel);

        int twilight(byte voxel);

        int bright(byte voxel);

        int light(int brightness, byte voxel);
    }

    public static abstract class Twilight implements ITwilight {
        @Override
        public int light(int brightness, byte voxel) {
            if (voxel == 0) return 0;// 0 is equivalent to Color.rgba8888(Color.CLEAR)
            switch (brightness) {
                case 0:
                    return dark(voxel);
                case 1:
                    return dim(voxel);
                case 2:
                    return twilight(voxel);
                case 3:
                    return bright(voxel);
            }
            return brightness > 3
                    ? 0xffffffff //rgba8888 value of Color.WHITE
                    : 0xff;      //rgba8888 value of Color.BLACK
        }
    }

    public static final ITwilight RinsedTwilight = new Twilight() {
        protected int[] palette = Coloring.RINSED;

        @Override
        public int bright(byte voxel) {
            return palette[(voxel & 248) + Math.max((voxel & 7) - 1, 0)];
        }

        @Override
        public int twilight(byte voxel) {
            return palette[(voxel & 255)];
        }

        @Override
        public int dim(byte voxel) {
            return palette[(voxel & 248) + Math.min((voxel & 7) + 1, 7)];
        }

        @Override
        public int dark(byte voxel) {
            return palette[(voxel & 248) + Math.min((voxel & 7) + 2, 7)];
        }
    };
    
    public static final ITwilight AuroraTwilight = new Twilight() {
        protected final int[][] RAMPS = new int[][]{
                { 0x00000000, 0x00000000, 0x00000000, 0x00000000 },
                { 0x131313FF, 0x010101FF, 0x010101FF, 0x010101FF },
                { 0x191E0FFF, 0x131313FF, 0x010101FF, 0x010101FF },
                { 0x1E2D23FF, 0x252525FF, 0x191E0FFF, 0x131313FF },
                { 0x494949FF, 0x373737FF, 0x1E2D23FF, 0x252525FF },
                { 0x5B5B5BFF, 0x494949FF, 0x373737FF, 0x1E2D23FF },
                { 0x6E6E6EFF, 0x5B5B5BFF, 0x494949FF, 0x373737FF },
                { 0x808080FF, 0x6E6E6EFF, 0x5B5B5BFF, 0x494949FF },
                { 0x929292FF, 0x808080FF, 0x6E6E6EFF, 0x5B5B5BFF },
                { 0xA4A4A4FF, 0x929292FF, 0x808080FF, 0x6E6E6EFF },
                { 0xB6B6B6FF, 0xA4A4A4FF, 0x929292FF, 0x808080FF },
                { 0xC9C9C9FF, 0xB6B6B6FF, 0xA4A4A4FF, 0x929292FF },
                { 0xDBDBDBFF, 0xC9C9C9FF, 0xB6B6B6FF, 0xA4A4A4FF },
                { 0xEDEDEDFF, 0xDBDBDBFF, 0xC9C9C9FF, 0xB6B6B6FF },
                { 0xFFFFFFFF, 0xEDEDEDFF, 0xDBDBDBFF, 0xC9C9C9FF },
                { 0xFFFFFFFF, 0xFFFFFFFF, 0xEDEDEDFF, 0xDBDBDBFF },
                { 0x129880FF, 0x007F7FFF, 0x055A5CFF, 0x235037FF },
                { 0x55E6FFFF, 0x3FBFBFFF, 0x64ABABFF, 0x6E8287FF },
                { 0x55E6FFFF, 0x00FFFFFF, 0x08DED5FF, 0x3FBFBFFF },
                { 0xFFFFFFFF, 0xBFFFFFFF, 0xC7F1F1FF, 0xABE3E3FF },
                { 0x90B0FFFF, 0x8181FFFF, 0x7676CAFF, 0x736EAAFF },
                { 0x101CDAFF, 0x0000FFFF, 0x0010BDFF, 0x00007FFF },
                { 0x4A5AFFFF, 0x3F3FBFFF, 0x0F377DFF, 0x162C52FF },
                { 0x231094FF, 0x00007FFF, 0x0F0F50FF, 0x010101FF },
                { 0x0C2148FF, 0x0F0F50FF, 0x0F192DFF, 0x010101FF },
                { 0xA01982FF, 0x7F007FFF, 0x641464FF, 0x410062FF },
                { 0xBD62FFFF, 0xBF3FBFFF, 0xAB57ABFF, 0x8F578FFF },
                { 0xDA20E0FF, 0xF500F5FF, 0xBD10C5FF, 0xA01982FF },
                { 0xEBACE1FF, 0xFD81FFFF, 0xE673FFFF, 0xC78FB9FF },
                { 0xFFDCF5FF, 0xFFC0CBFF, 0xE3C7ABFF, 0xE1B9D2FF },
                { 0xFAA0B9FF, 0xFF8181FF, 0xD08A74FF, 0xC07872FF },
                { 0xFF3C0AFF, 0xFF0000FF, 0xDA2010FF, 0xA5140AFF },
                { 0xD5524AFF, 0xBF3F3FFF, 0x98344DFF, 0x73413CFF },
                { 0xA5140AFF, 0x7F0000FF, 0x621800FF, 0x551414FF },
                { 0x5F3214FF, 0x551414FF, 0x401811FF, 0x280A1EFF },
                { 0xA04B05FF, 0x7F3F00FF, 0x5F3214FF, 0x3B2D1FFF },
                { 0xC49E73FF, 0xBF7F3FFF, 0x8C805AFF, 0x8F7357FF },
                { 0xFFA53CFF, 0xFF7F00FF, 0xDA6E0AFF, 0xBF7F3FFF },
                { 0xF5E1D2FF, 0xFFBF81FF, 0xF5B99BFF, 0xEBAA8CFF },
                { 0xFFFFFFFF, 0xFFFFBFFF, 0xEDEDC7FF, 0xDADAABFF },
                { 0xFFFFBFFF, 0xFFFF00FF, 0xFFEA4AFF, 0xE6D55AFF },
                { 0xE6D55AFF, 0xBFBF3FFF, 0x8EBE55FF, 0xA2A255FF },
                { 0xAC9400FF, 0x7F7F00FF, 0x626200FF, 0x53500AFF },
                { 0x149605FF, 0x007F00FF, 0x0C5C0CFF, 0x204608FF },
                { 0x4BF05AFF, 0x3FBF3FFF, 0x578F57FF, 0x587D3EFF },
                { 0x4BF05AFF, 0x00FF00FF, 0x14E60AFF, 0x0AD70AFF },
                { 0xFFFFBFFF, 0xAFFFAFFF, 0xC7E3ABFF, 0xABE3C5FF },
                { 0xC9C9C9FF, 0xBCAFC0FF, 0xB6B6B6FF, 0xA4A4A4FF },
                { 0xC7C78FFF, 0xCBAA89FF, 0xC49E73FF, 0xA6A090FF },
                { 0xB6B6B6FF, 0xA6A090FF, 0x929292FF, 0x808080FF },
                { 0x87B48EFF, 0x7E9494FF, 0x6E8287FF, 0x6E6E6EFF },
                { 0x7E9494FF, 0x6E8287FF, 0x6E6E6EFF, 0x5B5B5BFF },
                { 0x8C805AFF, 0x7E6E60FF, 0x506450FF, 0x5B5B5BFF },
                { 0xAB7373FF, 0xA0695FFF, 0x7E6E60FF, 0x8E5555FF },
                { 0xD08A74FF, 0xC07872FF, 0xAB7373FF, 0xA0695FFF },
                { 0xE19B7DFF, 0xD08A74FF, 0xC07872FF, 0xAB7373FF },
                { 0xEBAA8CFF, 0xE19B7DFF, 0xC49E73FF, 0xD08A74FF },
                { 0xF5B99BFF, 0xEBAA8CFF, 0xCBAA89FF, 0xC49E73FF },
                { 0xF6C8AFFF, 0xF5B99BFF, 0xEBAA8CFF, 0xCBAA89FF },
                { 0xF5E1D2FF, 0xF6C8AFFF, 0xE3C7ABFF, 0xE3ABABFF },
                { 0xFFFFBFFF, 0xF5E1D2FF, 0xDBDBDBFF, 0xDADAABFF },
                { 0x73413CFF, 0x573B3BFF, 0x463246FF, 0x3B2D1FFF },
                { 0x73573BFF, 0x73413CFF, 0x573B3BFF, 0x3B2D1FFF },
                { 0xA0695FFF, 0x8E5555FF, 0x73573BFF, 0x73413CFF },
                { 0xC78F8FFF, 0xAB7373FF, 0xA0695FFF, 0x8F7357FF },
                { 0xE3ABABFF, 0xC78F8FFF, 0x929292FF, 0xAB7373FF },
                { 0xE3C7ABFF, 0xE3ABABFF, 0xCBAA89FF, 0xA6A090FF },
                { 0xF5E1D2FF, 0xF8D2DAFF, 0xE3C7E3FF, 0xC9C9C9FF },
                { 0xDADAABFF, 0xE3C7ABFF, 0xC7C78FFF, 0xE3ABABFF },
                { 0xCBAA89FF, 0xC49E73FF, 0xA2A255FF, 0x8F8F57FF },
                { 0x8C805AFF, 0x8F7357FF, 0x7E6E60FF, 0x73733BFF },
                { 0x73733BFF, 0x73573BFF, 0x73413CFF, 0x465032FF },
                { 0x414123FF, 0x3B2D1FFF, 0x252525FF, 0x191E0FFF },
                { 0x465032FF, 0x414123FF, 0x3B2D1FFF, 0x283405FF },
                { 0x8C805AFF, 0x73733BFF, 0x73573BFF, 0x465032FF },
                { 0xA2A255FF, 0x8F8F57FF, 0x8C805AFF, 0x8F7357FF },
                { 0xB5B572FF, 0xA2A255FF, 0x8F8F57FF, 0x8C805AFF },
                { 0xC7C78FFF, 0xB5B572FF, 0xA6A090FF, 0xA2A255FF },
                { 0xDADAABFF, 0xC7C78FFF, 0xABC78FFF, 0xB5B572FF },
                { 0xEDEDC7FF, 0xDADAABFF, 0xC7C78FFF, 0xABC78FFF },
                { 0xFFFFBFFF, 0xEDEDC7FF, 0xDBDBDBFF, 0xDADAABFF },
                { 0xEDEDC7FF, 0xC7E3ABFF, 0xA2D8A2FF, 0xC7C78FFF },
                { 0xC7E3ABFF, 0xABC78FFF, 0x8FC78FFF, 0x87B48EFF },
                { 0x9BF046FF, 0x8EBE55FF, 0x73AB73FF, 0x8F8F57FF },
                { 0x73AB73FF, 0x738F57FF, 0x578F57FF, 0x587D3EFF },
                { 0x578F57FF, 0x587D3EFF, 0x506450FF, 0x3B573BFF },
                { 0x506450FF, 0x465032FF, 0x414123FF, 0x373737FF },
                { 0x252525FF, 0x191E0FFF, 0x131313FF, 0x010101FF },
                { 0x3B573BFF, 0x235037FF, 0x123832FF, 0x1E2D23FF },
                { 0x506450FF, 0x3B573BFF, 0x235037FF, 0x414123FF },
                { 0x507D5FFF, 0x506450FF, 0x3B573BFF, 0x494949FF },
                { 0x507D5FFF, 0x3B7349FF, 0x3B573BFF, 0x235037FF },
                { 0x73AB73FF, 0x578F57FF, 0x587D3EFF, 0x507D5FFF },
                { 0x87B48EFF, 0x73AB73FF, 0x738F57FF, 0x578F57FF },
                { 0x8FC78FFF, 0x64C082FF, 0x73AB73FF, 0x578F57FF },
                { 0xA2D8A2FF, 0x8FC78FFF, 0x87B48EFF, 0x73AB73FF },
                { 0xABE3C5FF, 0xA2D8A2FF, 0x8FC78FFF, 0x87B48EFF },
                { 0xFFFFFFFF, 0xE1F8FAFF, 0xC7F1F1FF, 0xDBDBDBFF },
                { 0xE1F8FAFF, 0xB4EECAFF, 0xABE3C5FF, 0xA2D8A2FF },
                { 0xB4EECAFF, 0xABE3C5FF, 0xA2D8A2FF, 0xABC78FFF },
                { 0x8FC78FFF, 0x87B48EFF, 0x73AB73FF, 0x7E9494FF },
                { 0x578F57FF, 0x507D5FFF, 0x3B7349FF, 0x506450FF },
                { 0x1C8C4EFF, 0x0F6946FF, 0x235037FF, 0x123832FF },
                { 0x373737FF, 0x1E2D23FF, 0x191E0FFF, 0x131313FF },
                { 0x3B573BFF, 0x234146FF, 0x123832FF, 0x1E2D23FF },
                { 0x6E8287FF, 0x3B7373FF, 0x3B5773FF, 0x3B573BFF },
                { 0x8FC7C7FF, 0x64ABABFF, 0x7E9494FF, 0x6E8287FF },
                { 0xABE3E3FF, 0x8FC7C7FF, 0x8FABC7FF, 0x87B48EFF },
                { 0xC7F1F1FF, 0xABE3E3FF, 0xABC7E3FF, 0x8FC7C7FF },
                { 0xE1F8FAFF, 0xC7F1F1FF, 0xB4EECAFF, 0xABE3E3FF },
                { 0xD0DAF8FF, 0xBED2F0FF, 0xABC7E3FF, 0xA8B9DCFF },
                { 0xBED2F0FF, 0xABC7E3FF, 0xA8B9DCFF, 0x8FABC7FF },
                { 0xABC7E3FF, 0xA8B9DCFF, 0x8FABC7FF, 0xA4A4A4FF },
                { 0xA8B9DCFF, 0x8FABC7FF, 0x7E9494FF, 0x6E8287FF },
                { 0x699DC3FF, 0x578FC7FF, 0x4B7DC8FF, 0x57738FFF },
                { 0x6E8287FF, 0x57738FFF, 0x3B7373FF, 0x506450FF },
                { 0x3B7373FF, 0x3B5773FF, 0x234146FF, 0x123832FF },
                { 0x1F1F3BFF, 0x0F192DFF, 0x131313FF, 0x010101FF },
                { 0x123832FF, 0x1F1F3BFF, 0x0F192DFF, 0x131313FF },
                { 0x494949FF, 0x3B3B57FF, 0x234146FF, 0x373737FF },
                { 0x57578FFF, 0x494973FF, 0x3B3B57FF, 0x234146FF },
                { 0x57738FFF, 0x57578FFF, 0x494973FF, 0x3B3B57FF },
                { 0x8F8FC7FF, 0x736EAAFF, 0x57738FFF, 0x73578FFF },
                { 0x8F8FC7FF, 0x7676CAFF, 0x736EAAFF, 0x57738FFF },
                { 0x8FABC7FF, 0x8F8FC7FF, 0x7E9494FF, 0x6E8287FF },
                { 0xA8B9DCFF, 0xABABE3FF, 0x8FABC7FF, 0xAB8FC7FF },
                { 0xE3E3FFFF, 0xD0DAF8FF, 0xBED2F0FF, 0xC9C9C9FF },
                { 0xEDEDEDFF, 0xE3E3FFFF, 0xD0DAF8FF, 0xDBDBDBFF },
                { 0xABABE3FF, 0xAB8FC7FF, 0x8F8FC7FF, 0x929292FF },
                { 0xBD62FFFF, 0x8F57C7FF, 0x73578FFF, 0x724072FF },
                { 0x736EAAFF, 0x73578FFF, 0x57578FFF, 0x5B5B5BFF },
                { 0x73578FFF, 0x573B73FF, 0x3B3B57FF, 0x463246FF },
                { 0x4B2837FF, 0x3C233CFF, 0x252525FF, 0x321623FF },
                { 0x494949FF, 0x463246FF, 0x373737FF, 0x3C233CFF },
                { 0x73578FFF, 0x724072FF, 0x573B73FF, 0x573B3BFF },
                { 0xAB73ABFF, 0x8F578FFF, 0x73578FFF, 0x724072FF },
                { 0xAB73ABFF, 0xAB57ABFF, 0x8F578FFF, 0x73578FFF },
                { 0xAB8FC7FF, 0xAB73ABFF, 0x808080FF, 0x8F578FFF },
                { 0xF8C6FCFF, 0xEBACE1FF, 0xD7A0BEFF, 0xBCAFC0FF },
                { 0xFFFFFFFF, 0xFFDCF5FF, 0xF8D2DAFF, 0xDBDBDBFF },
                { 0xF8D2DAFF, 0xE3C7E3FF, 0xE1B9D2FF, 0xC9C9C9FF },
                { 0xE3C7E3FF, 0xE1B9D2FF, 0xBCAFC0FF, 0xA4A4A4FF },
                { 0xE1B9D2FF, 0xD7A0BEFF, 0xC78FB9FF, 0xA4A4A4FF },
                { 0xD7A0BEFF, 0xC78FB9FF, 0xC87DA0FF, 0x929292FF },
                { 0xC78FB9FF, 0xC87DA0FF, 0xAB73ABFF, 0xAB7373FF },
                { 0xC87DA0FF, 0xC35A91FF, 0x8F578FFF, 0x8E5555FF },
                { 0x573B3BFF, 0x4B2837FF, 0x3C233CFF, 0x321623FF },
                { 0x3B2D1FFF, 0x321623FF, 0x280A1EFF, 0x131313FF },
                { 0x321623FF, 0x280A1EFF, 0x010101FF, 0x010101FF },
                { 0x3B2D1FFF, 0x401811FF, 0x280A1EFF, 0x131313FF },
                { 0x5F3214FF, 0x621800FF, 0x551414FF, 0x401811FF },
                { 0xDA2010FF, 0xA5140AFF, 0x7F0000FF, 0x621800FF },
                { 0xFF3C0AFF, 0xDA2010FF, 0xA5140AFF, 0x911437FF },
                { 0xFF6262FF, 0xD5524AFF, 0xBF3F3FFF, 0x8E5555FF },
                { 0xF55A32FF, 0xFF3C0AFF, 0xBF3F3FFF, 0xA04B05FF },
                { 0xFF6262FF, 0xF55A32FF, 0xD5524AFF, 0xBF3F3FFF },
                { 0xFF8181FF, 0xFF6262FF, 0xD5524AFF, 0xA0695FFF },
                { 0xFFEA4AFF, 0xF6BD31FF, 0xFFA53CFF, 0xBFBF3FFF },
                { 0xF6BD31FF, 0xFFA53CFF, 0xE19B7DFF, 0xC49E73FF },
                { 0xF6BD31FF, 0xD79B0FFF, 0xAC9400FF, 0xBF7F3FFF },
                { 0xFF7F00FF, 0xDA6E0AFF, 0xB45A00FF, 0x73733BFF },
                { 0xDA6E0AFF, 0xB45A00FF, 0xA04B05FF, 0x7F3F00FF },
                { 0xB45A00FF, 0xA04B05FF, 0x7F3F00FF, 0x5F3214FF },
                { 0x73413CFF, 0x5F3214FF, 0x3B2D1FFF, 0x401811FF },
                { 0x626200FF, 0x53500AFF, 0x414123FF, 0x283405FF },
                { 0x7F7F00FF, 0x626200FF, 0x53500AFF, 0x414123FF },
                { 0x8F8F57FF, 0x8C805AFF, 0x8F7357FF, 0x7E6E60FF },
                { 0xB1B10AFF, 0xAC9400FF, 0x7F7F00FF, 0x73733BFF },
                { 0xBFBF3FFF, 0xB1B10AFF, 0xAC9400FF, 0x8F8F57FF },
                { 0xFFEA4AFF, 0xE6D55AFF, 0xC7C78FFF, 0xBFBF3FFF },
                { 0xFFFF00FF, 0xFFD510FF, 0xF6BD31FF, 0xBFBF3FFF },
                { 0xFFFFBFFF, 0xFFEA4AFF, 0xE6D55AFF, 0xC7C78FFF },
                { 0xFFEA4AFF, 0xC8FF41FF, 0xE6D55AFF, 0x9BF046FF },
                { 0xC8FF41FF, 0x9BF046FF, 0x8EBE55FF, 0x87B48EFF },
                { 0x9BF046FF, 0x96DC19FF, 0x8EBE55FF, 0xA2A255FF },
                { 0x96DC19FF, 0x73C805FF, 0x6AA805FF, 0x738F57FF },
                { 0x73C805FF, 0x6AA805FF, 0x7F7F00FF, 0x587D3EFF },
                { 0x587D3EFF, 0x3C6E14FF, 0x204608FF, 0x283405FF },
                { 0x204608FF, 0x283405FF, 0x191E0FFF, 0x131313FF },
                { 0x465032FF, 0x204608FF, 0x283405FF, 0x191E0FFF },
                { 0x3C6E14FF, 0x0C5C0CFF, 0x204608FF, 0x1E2D23FF },
                { 0x3FBF3FFF, 0x149605FF, 0x007F00FF, 0x0C5C0CFF },
                { 0x14E60AFF, 0x0AD70AFF, 0x00C514FF, 0x149605FF },
                { 0x00FF00FF, 0x14E60AFF, 0x0AD70AFF, 0x00C514FF },
                { 0xAFFFAFFF, 0x7DFF73FF, 0x8FC78FFF, 0x87B48EFF },
                { 0x7DFF73FF, 0x4BF05AFF, 0x3FBF3FFF, 0x64C082FF },
                { 0x0AD70AFF, 0x00C514FF, 0x149605FF, 0x1C8C4EFF },
                { 0x00DE6AFF, 0x05B450FF, 0x1C8C4EFF, 0x0F6946FF },
                { 0x05B450FF, 0x1C8C4EFF, 0x0F6946FF, 0x235037FF },
                { 0x234146FF, 0x123832FF, 0x1E2D23FF, 0x0F192DFF },
                { 0x06C491FF, 0x129880FF, 0x1C8C4EFF, 0x3B7373FF },
                { 0x2DEBA8FF, 0x06C491FF, 0x129880FF, 0x1C8C4EFF },
                { 0x2DEBA8FF, 0x00DE6AFF, 0x05B450FF, 0x1C8C4EFF },
                { 0x3CFEA5FF, 0x2DEBA8FF, 0x3FBFBFFF, 0x64ABABFF },
                { 0x6AFFCDFF, 0x3CFEA5FF, 0x2DEBA8FF, 0x64C082FF },
                { 0xBFFFFFFF, 0x6AFFCDFF, 0x8FC7C7FF, 0x87B48EFF },
                { 0xBFFFFFFF, 0x91EBFFFF, 0x7DD7F0FF, 0xABC7E3FF },
                { 0x91EBFFFF, 0x55E6FFFF, 0x7DD7F0FF, 0x5AC5FFFF },
                { 0x91EBFFFF, 0x7DD7F0FF, 0x8FC7C7FF, 0x8FABC7FF },
                { 0x00FFFFFF, 0x08DED5FF, 0x3FBFBFFF, 0x129880FF },
                { 0x4AA4FFFF, 0x109CDEFF, 0x2378DCFF, 0x129880FF },
                { 0x007F7FFF, 0x055A5CFF, 0x123832FF, 0x0F192DFF },
                { 0x234146FF, 0x162C52FF, 0x0C2148FF, 0x0F192DFF },
                { 0x326496FF, 0x0F377DFF, 0x162C52FF, 0x0C2148FF },
                { 0x186ABDFF, 0x004A9CFF, 0x0F377DFF, 0x162C52FF },
                { 0x57738FFF, 0x326496FF, 0x3B5773FF, 0x234146FF },
                { 0x007FFFFF, 0x0052F6FF, 0x004A9CFF, 0x0F377DFF },
                { 0x2378DCFF, 0x186ABDFF, 0x326496FF, 0x055A5CFF },
                { 0x4AA4FFFF, 0x2378DCFF, 0x186ABDFF, 0x326496FF },
                { 0x8FABC7FF, 0x699DC3FF, 0x578FC7FF, 0x6E8287FF },
                { 0x5AC5FFFF, 0x4AA4FFFF, 0x578FC7FF, 0x4B7DC8FF },
                { 0xBED2F0FF, 0x90B0FFFF, 0x8FABC7FF, 0x8F8FC7FF },
                { 0x55E6FFFF, 0x5AC5FFFF, 0x699DC3FF, 0x7E9494FF },
                { 0xD7C3FAFF, 0xBEB9FAFF, 0xA8B9DCFF, 0xABABE3FF },
                { 0x00FFFFFF, 0x00BFFFFF, 0x109CDEFF, 0x4B7DC8FF },
                { 0x109CDEFF, 0x007FFFFF, 0x186ABDFF, 0x326496FF },
                { 0x578FC7FF, 0x4B7DC8FF, 0x57738FFF, 0x326496FF },
                { 0x8181FFFF, 0x786EF0FF, 0x7676CAFF, 0x736EAAFF },
                { 0x786EF0FF, 0x4A5AFFFF, 0x3F3FBFFF, 0x494973FF },
                { 0x786EF0FF, 0x6241F6FF, 0x3F3FBFFF, 0x573B73FF },
                { 0x4A5AFFFF, 0x3C3CF5FF, 0x3F3FBFFF, 0x0F377DFF },
                { 0x3C3CF5FF, 0x101CDAFF, 0x0010BDFF, 0x00007FFF },
                { 0x101CDAFF, 0x0010BDFF, 0x00007FFF, 0x0F0F50FF },
                { 0x3F3FBFFF, 0x231094FF, 0x0F0F50FF, 0x010101FF },
                { 0x162C52FF, 0x0C2148FF, 0x0F192DFF, 0x010101FF },
                { 0x8732D2FF, 0x5010B0FF, 0x231094FF, 0x410062FF },
                { 0x8732D2FF, 0x6010D0FF, 0x5010B0FF, 0x231094FF },
                { 0x9C41FFFF, 0x8732D2FF, 0x573B73FF, 0x463246FF },
                { 0xBD62FFFF, 0x9C41FFFF, 0x8732D2FF, 0x57578FFF },
                { 0x8732D2FF, 0x7F00FFFF, 0x6010D0FF, 0x5010B0FF },
                { 0xE673FFFF, 0xBD62FFFF, 0x8F57C7FF, 0x736EAAFF },
                { 0xD7A5FFFF, 0xB991FFFF, 0xAB8FC7FF, 0x8F8FC7FF },
                { 0xD7C3FAFF, 0xD7A5FFFF, 0xABABE3FF, 0xAB8FC7FF },
                { 0xD0DAF8FF, 0xD7C3FAFF, 0xBEB9FAFF, 0xBCAFC0FF },
                { 0xFFDCF5FF, 0xF8C6FCFF, 0xE3C7E3FF, 0xE1B9D2FF },
                { 0xFD81FFFF, 0xE673FFFF, 0xAB8FC7FF, 0xAB73ABFF },
                { 0xFD81FFFF, 0xFF52FFFF, 0xBD62FFFF, 0xBF3FBFFF },
                { 0xFF52FFFF, 0xDA20E0FF, 0xBD10C5FF, 0xBF3FBFFF },
                { 0xBD62FFFF, 0xBD29FFFF, 0x8732D2FF, 0x724072FF },
                { 0xDA20E0FF, 0xBD10C5FF, 0xA01982FF, 0x641464FF },
                { 0x8732D2FF, 0x8C14BEFF, 0x5A187BFF, 0x3C233CFF },
                { 0x573B73FF, 0x5A187BFF, 0x320A46FF, 0x280A1EFF },
                { 0x573B73FF, 0x641464FF, 0x551937FF, 0x320A46FF },
                { 0x5A187BFF, 0x410062FF, 0x320A46FF, 0x280A1EFF },
                { 0x3C233CFF, 0x320A46FF, 0x280A1EFF, 0x010101FF },
                { 0x573B3BFF, 0x551937FF, 0x321623FF, 0x280A1EFF },
                { 0xBF3FBFFF, 0xA01982FF, 0x641464FF, 0x551937FF },
                { 0xE61E78FF, 0xC80078FF, 0x911437FF, 0x641464FF },
                { 0xFF6AC5FF, 0xFF50BFFF, 0xC35A91FF, 0x8F578FFF },
                { 0xFD81FFFF, 0xFF6AC5FF, 0xC87DA0FF, 0xAB73ABFF },
                { 0xFFC0CBFF, 0xFAA0B9FF, 0xE3ABABFF, 0xD7A0BEFF },
                { 0xFF50BFFF, 0xFC3A8CFF, 0xC35A91FF, 0xBF3F3FFF },
                { 0xFC3A8CFF, 0xE61E78FF, 0x98344DFF, 0x573B3BFF },
                { 0xBF3F3FFF, 0xBD1039FF, 0x911437FF, 0x551414FF },
                { 0x8E5555FF, 0x98344DFF, 0x73413CFF, 0x573B3BFF },
                { 0x98344DFF, 0x911437FF, 0x551414FF, 0x401811FF },
        };


        @Override
        public int dark(byte voxel) {
            return RAMPS[voxel & 255][3];
        }

        @Override
        public int dim(byte voxel) {
            return RAMPS[voxel & 255][2];
        }

        @Override
        public int twilight(byte voxel) {
            return RAMPS[voxel & 255][1];
        }

        @Override
        public int bright(byte voxel) {
            return RAMPS[voxel & 255][0];
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
        switch (lightDirection) {
            case ABOVE_RIGHT:
            case ABOVE_LEFT:
                return twilight.bright(voxel);
            case RIGHT_BELOW:
            case LEFT_BELOW:
                return twilight.dim(voxel);
            case BELOW_RIGHT:
            case BELOW_LEFT:
                return twilight.dark(voxel);
            default:
                return twilight.twilight(voxel);
        }
    }

    @Override
    public int bottomFace(byte voxel) {
        switch (lightDirection) {
            case BELOW_LEFT:
            case BELOW_RIGHT:
                return twilight.bright(voxel);
            case LEFT_ABOVE:
            case RIGHT_ABOVE:
                return twilight.dim(voxel);
            case ABOVE_LEFT:
            case ABOVE_RIGHT:
                return twilight.dark(voxel);
            default:
                return twilight.twilight(voxel);
        }
    }

    @Override
    public int leftFace(byte voxel) {
        switch (lightDirection) {
            case LEFT_ABOVE:
            case LEFT_BELOW:
                return twilight.bright(voxel);
            case ABOVE_RIGHT:
            case BELOW_RIGHT:
                return twilight.dim(voxel);
            case RIGHT_ABOVE:
            case RIGHT_BELOW:
                return twilight.dark(voxel);
            default:
                return twilight.twilight(voxel);
        }
    }

    @Override
    public int rightFace(byte voxel) {
        switch (lightDirection) {
            case RIGHT_ABOVE:
            case RIGHT_BELOW:
                return twilight.bright(voxel);
            case ABOVE_LEFT:
            case BELOW_LEFT:
                return twilight.dim(voxel);
            case LEFT_ABOVE:
            case LEFT_BELOW:
                return twilight.dark(voxel);
            default:
                return twilight.twilight(voxel);
        }
    }

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
}
