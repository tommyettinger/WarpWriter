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

    protected int a, b, c;

    public int a() {
        return a;
    }

    public int b() {
        return b;
    }

    public int c() {
        return c;
    }

    public PlaneDecide set(int a, int b, int c) {
        this.a = a;
        this.b = b;
        this.c = c;
        return this;
    }

    public PlaneDecide(int a, int b, int c) {
        set(a, b, c);
    }

    @Override
    public boolean bool(int x, int y, int z) {
        int result = x * a + y * b + z * c;
        switch (condition) {
            default:
            case ON:
                return result == 0;
            case OFF:
                return result != 0;
            case ABOVE:
                return result > 0;
            case ON_ABOVE:
                return result >= 0;
            case BELOW:
                return result < 0;
            case ON_BELOW:
                return result <= 0;
            case TRUE:
                return true;
            case FALSE:
                return false;
        }
    }
}
