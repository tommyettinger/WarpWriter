package warpwriter.model;

/**
 * @author Ben McLean
 */
public class PlaneDecide implements IDecide {
    public enum Condition {
        ON, OFF, ABOVE, BELOW, TRUE, FALSE
    }

    protected Condition condition=Condition.ON;

    public Condition condition() {
        return condition;
    }

    public PlaneDecide set(Condition condition) {
        this.condition = condition;
        return this;
    }

    protected double a, b, c;

    public double a() {
        return a;
    }

    public double b() {
        return b;
    }

    public double c() {
        return c;
    }

    public PlaneDecide set(double a, double b, double c) {
        this.a = a;
        this.b = b;
        this.c = c;
        return this;
    }

    public PlaneDecide(double a, double b, double c) {
        this(a, b, c, Condition.ON);
    }

    public PlaneDecide(double a, double b, double c, Condition condition) {
        set(a, b, c).set(condition);
    }

    @Override
    public boolean bool(int x, int y, int z) {
        double result = x * a + y * b + z * c;
        switch (condition) {
            default:
            case ON:
                return Math.abs(result) < 1;
            case OFF:
                return !(Math.abs(result) < 1);
            case ABOVE:
                return result > 0;
            case BELOW:
                return result < 0;
            case TRUE:
                return true;
            case FALSE:
                return false;
        }
    }
}
