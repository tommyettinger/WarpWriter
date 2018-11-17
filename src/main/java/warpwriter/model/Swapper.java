package warpwriter.model;

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
    public Fetch fetch() {
        int x = chainX(), y = chainY(), z = chainZ();
        switch (swap) {
            case xyz:
                setChains(x, y, z);
                break;
            case xzy:
                setChains(x, z, y);
                break;
            case yxz:
                setChains(y, x, z);
                break;
            case yzx:
                setChains(y, z, x);
                break;
            case zxy:
                setChains(z, x, y);
                break;
            case zyx:
                setChains(z, y, x);
                break;
            case Oyz:
                setChains(0, y, z);
                break;
            case Ozy:
                setChains(0, z, y);
                break;
            case Oxz:
                setChains(0, x, z);
                break;
            case Ozx:
                setChains(0, z, x);
                break;
            case Oxy:
                setChains(0, x, y);
                break;
            case Oyx:
                setChains(0, y, x);
                break;
            case x0z:
                setChains(x, 0, z);
                break;
            case x0y:
                setChains(x, 0, y);
                break;
            case y0z:
                setChains(y, 0, z);
                break;
            case y0x:
                setChains(y, 0, x);
                break;
            case z0y:
                setChains(z, 0, y);
                break;
            case z0x:
                setChains(z, 0, x);
                break;
            case xy0:
                setChains(x, y, 0);
                break;
            case xz0:
                setChains(x, z, 0);
                break;
            case yx0:
                setChains(y, x, 0);
                break;
            case yz0:
                setChains(y, z, 0);
                break;
            case zx0:
                setChains(z, x, 0);
                break;
            case zy0:
                setChains(z, y, 0);
                break;
            case x00:
                setChains(x, 0, 0);
                break;
            case y00:
                setChains(y, 0, 0);
                break;
            case z00:
                setChains(z, 0, 0);
                break;
            case Ox0:
                setChains(0, x, 0);
                break;
            case Oy0:
                setChains(0, y, 0);
                break;
            case Oz0:
                setChains(0, z, 0);
                break;
            case O0x:
                setChains(0, 0, x);
                break;
            case O0y:
                setChains(0, 0, y);
                break;
            case O0z:
                setChains(0, 0, z);
                break;
            case O00:
                setChains(0, 0, 0);
                break;
            case clockX:
                setChains(x, z * -1, y);
                break;
            case counterX:
                setChains(x, z, y * -1);
                break;
            case clockY:
                setChains(z * -1, y, x);
                break;
            case counterY:
                setChains(z, y, x * -1);
                break;
            case clockZ:
                setChains(y * -1, x, z);
                break;
            case counterZ:
            default:
                setChains(y, x * -1, z);
                break;
        }
        return getNextFetch();
    }
}
