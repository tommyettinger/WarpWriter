package warpwriter.model.decide;

import warpwriter.model.IDecide;

/**
 * @author Ben McLean
 */
public class PlaneDecide implements IDecide {
    public enum Condition {
        ON, OFF, ABOVE, ON_ABOVE, BELOW, ON_BELOW, TRUE, FALSE
    }

    protected Condition condition = Condition.ON;

    public Condition condition() {
        return condition;
    }

    public PlaneDecide set(Condition condition) {
        this.condition = condition;
        return this;
    }

    protected double slopeX, slopeY, slopeZ;
    protected int offset;

    public double slopeX() {
        return slopeX;
    }

    public double slopeY() {
        return slopeY;
    }

    public double slopeZ() {
        return slopeZ;
    }

    /**
     * Offset from the plane.
     *
     * @return the current offset
     */
    public int offset() {
        return offset;
    }

    public PlaneDecide set(double slopeX, double slopeY, double slopeZ, int offset) {
        this.slopeX = slopeX;
        this.slopeY = slopeY;
        this.slopeZ = slopeZ;
        this.offset = offset;
        return this;
    }

    public PlaneDecide(double slopeX, double slopeY, double slopeZ, int offset) {
        set(slopeX, slopeY, slopeZ, offset);
    }

    @Override
    public boolean bool(int x, int y, int z) {
        double result = x * slopeX + y * slopeY + z * slopeZ - offset;
        switch (condition) {
            case OFF:
                return Math.abs(result) > 1;
            case ABOVE:
                return result > 1;
            case ON_ABOVE:
                return result > 0;
            case BELOW:
                return result < -1;
            case ON_BELOW:
                return result < 0;
            case TRUE:
                return true;
            case FALSE:
                return false;
            default:
            case ON:
                return Math.abs(result) < 1;
        }
    }
}
