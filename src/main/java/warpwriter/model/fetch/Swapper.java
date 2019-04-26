package warpwriter.model.fetch;

import warpwriter.model.Fetch;

/**
 * @author Ben McLean
 */
public class Swapper extends Fetch {
    public enum Swap {
        xyz, xzy, yxz, yzx, zxy, zyx,
        Oyz, Ozy, Oxz, Ozx, Oxy, Oyx, // Letter O represents zero
        x0z, x0y, y0z, y0x, z0y, z0x,
        xy0, xz0, yx0, yz0, zx0, zy0,
        x00, y00, z00,
        Ox0, Oy0, Oz0,
        O0x, O0y, O0z,
        O00,
        clockX, counterX,
        clockY, counterY,
        clockZ, counterZ
    }

    public Swap swap;

    public Swapper(Swap swap) {
        this.swap = swap;
    }

    @Override
    public byte at(int x, int y, int z) {
        switch (swap) {
            case xyz:
            default:
                return getNextFetch().at(x, y, z);
            case xzy:
                return getNextFetch().at(x, z, y);
            case yxz:
                return getNextFetch().at(y, x, z);
            case yzx:
                return getNextFetch().at(y, z, x);
            case zxy:
                return getNextFetch().at(z, x, y);
            case zyx:
                return getNextFetch().at(z, y, x);
            case Oyz:
                return getNextFetch().at(0, y, z);
            case Ozy:
                return getNextFetch().at(0, z, y);
            case Oxz:
                return getNextFetch().at(0, x, z);
            case Ozx:
                return getNextFetch().at(0, z, x);
            case Oxy:
                return getNextFetch().at(0, x, y);
            case Oyx:
                return getNextFetch().at(0, y, x);
            case x0z:
                return getNextFetch().at(x, 0, z);
            case x0y:
                return getNextFetch().at(x, 0, y);
            case y0z:
                return getNextFetch().at(y, 0, z);
            case y0x:
                return getNextFetch().at(y, 0, x);
            case z0y:
                return getNextFetch().at(z, 0, y);
            case z0x:
                return getNextFetch().at(z, 0, x);
            case xy0:
                return getNextFetch().at(x, y, 0);
            case xz0:
                return getNextFetch().at(x, z, 0);
            case yx0:
                return getNextFetch().at(y, x, 0);
            case yz0:
                return getNextFetch().at(y, z, 0);
            case zx0:
                return getNextFetch().at(z, x, 0);
            case zy0:
                return getNextFetch().at(z, y, 0);
            case x00:
                return getNextFetch().at(x, 0, 0);
            case y00:
                return getNextFetch().at(y, 0, 0);
            case z00:
                return getNextFetch().at(z, 0, 0);
            case Ox0:
                return getNextFetch().at(0, x, 0);
            case Oy0:
                return getNextFetch().at(0, y, 0);
            case Oz0:
                return getNextFetch().at(0, z, 0);
            case O0x:
                return getNextFetch().at(0, 0, x);
            case O0y:
                return getNextFetch().at(0, 0, y);
            case O0z:
                return getNextFetch().at(0, 0, z);
            case O00:
                return getNextFetch().at(0, 0, 0);
            case clockX:
                return getNextFetch().at(x, z * -1, y);
            case counterX:
                return getNextFetch().at(x, z, y * -1);
            case clockY:
                return getNextFetch().at(z * -1, y, x);
            case counterY:
                return getNextFetch().at(z, y, x * -1);
            case clockZ:
                return getNextFetch().at(y * -1, x, z);
            case counterZ:
                return getNextFetch().at(y, x * -1, z);
        }
    }
}
