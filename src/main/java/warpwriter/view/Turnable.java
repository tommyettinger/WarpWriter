package warpwriter.view;

import warpwriter.model.TurnModel;

public class Turnable {
    TurnModel turnModel;

    public Turnable set(TurnModel turnModel) {
        this.turnModel = turnModel;
        return this;
    }

    public TurnModel turnModel() {
        return turnModel;
    }

    boolean y45 = false, z45 = false;

    public Turnable clockX() {
        turnModel.turner().clockX();
        return this;
    }

    public Turnable counterX() {
        turnModel.turner().counterX();
        return this;
    }

    public Turnable clockY() {
        if (y45)
            y45 = false;
        else {
            y45 = true;
            turnModel.turner().clockY();
        }
        return this;
    }

    public Turnable counterY() {
        if (y45) {
            y45 = false;
            turnModel.turner().counterY();
        } else
            y45 = true;
        return this;
    }

    public Turnable clockZ() {
        if (z45) {
            z45 = false;
            turnModel.turner().clockZ();
        } else
            z45 = true;
        return this;
    }

    public Turnable counterZ() {
        if (z45)
            z45 = false;
        else {
            z45 = true;
            turnModel.turner().counterZ();
        }
        return this;
    }
}
