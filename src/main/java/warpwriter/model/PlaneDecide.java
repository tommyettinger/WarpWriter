package warpwriter.model;

/**
 * @author Ben McLean
 */
public class PlaneDecide implements IDecide {
    public enum Condition {
        ON, OFF, ABOVE, ON_ABOVE, BELOW, ON_BELOW, TRUE, FALSE
    }

    protected Condition condition=Condition.ON;

    public Condition condition() {
        return condition;
    }

    public PlaneDecide set(Condition condition) {
        this.condition = condition;
        return this;
    }

    protected int a, b, c, o;

    public int a() {
        return a;
    }

    public int b() {
        return b;
    }

    public int c() {
        return c;
    }

    /**
     * Offset from the plane.
     * @return the current offset
     */
    public int o() {
        return o;
    }

    public PlaneDecide set(int a, int b, int c, int o) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.o = o;
        return this;
    }

    public PlaneDecide(int a, int b, int c, int o) {
        set(a, b, c, o);
    }

    @Override
    public boolean bool(int x, int y, int z) {
        int result = x * a + y * b + z * c;
        switch (condition) {
            case OFF:
                return result != o;
            case ABOVE:
                return result > o;
            case ON_ABOVE:
                return result >= o;
            case BELOW:
                return result < o;
            case ON_BELOW:
                return result <= o;
            case TRUE:
                return true;
            case FALSE:
                return false;
            default:
            case ON:
                return result == o;
        }
    }
}
