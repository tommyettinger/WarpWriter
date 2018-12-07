package warpwriter.view;

import warpwriter.model.IModel;
import warpwriter.model.TurnModel;

public class Turnable {
    protected SpriteBatchVoxelRenderer batchRenderer;
    protected TurnModel turnModel = new TurnModel();

    public Turnable set(IModel model) {
        turnModel.set(model);
        return this;
    }

    public Turnable set(SpriteBatchVoxelRenderer batchRenderer) {
        this.batchRenderer = batchRenderer;
        return this;
    }

    public SpriteBatchVoxelRenderer batchRenderer() {
        return batchRenderer;
    }

    public IModel getModel() {
        return turnModel.getModel();
    }

    public TurnModel turnModel() {
        return turnModel;
    }

    protected boolean z45 = false;

    public boolean getZ45() {
        return z45;
    }

    public Turnable setZ45(boolean z45) {
        this.z45 = z45;
        return this;
    }

    protected int angle = 2;

    public int angle() {
        return angle;
    }

    public Turnable setAngle(int angle) {
        this.angle = angle;
        return this;
    }

    protected float scaleX = 1f, scaleY = 1f;

    public Turnable setScale(float scale) {
        return setScale(scale, scale);
    }

    public Turnable setScale(float scaleX, float scaleY) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        return this;
    }

    protected int offsetX = 0, offsetY = 0;

    public Turnable setOffset(int offsetX, int offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        return this;
    }

    public int offsetX() {
        return offsetX;
    }

    public int offsetY() {
        return offsetY;
    }

    public Turnable clockX() {
        turnModel.turner().clockX();
        return this;
    }

    public Turnable counterX() {
        turnModel.turner().counterX();
        return this;
    }

    public Turnable clockY() {
        turnModel.turner().clockY();
        return this;
    }

    public Turnable counterY() {
        turnModel.turner().counterY();
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

    public Turnable reset() {
        turnModel.turner().reset();
        return this.setZ45(false).setAngle(2);
    }

    public Turnable render() {
        final int sizeX = (z45 ? SimpleDraw.isoWidth(turnModel) : turnModel.sizeY() - 1) * (int) scaleX,
                offCenter = sizeX / 2;
        switch (angle) {
            case 0: // Bottom
                SimpleDraw.simpleDrawBottom(turnModel, batchRenderer
                        .setFlipX(false).setFlipY(false)
                        .setScale(scaleX * 6f, scaleY * 6f)
                        .setOffset(offsetX - offCenter * 6, offsetY)
                );
                break;
            case 1: // Below
                if (z45) {
                    turnModel.turner().clockY().clockY().clockZ();
                    SimpleDraw.simpleDrawIso(turnModel, batchRenderer
                            .setFlipX(true).setFlipY(true)
                            .setScale(scaleX * 2f, scaleY)
                            .setOffset(offsetX + (sizeX - offCenter) * 2, offsetY + SimpleDraw.isoHeight(turnModel))
                    );
                    turnModel.turner().counterZ().counterY().counterY();
                } else {
                    turnModel.turner().clockY().clockY().clockZ().clockZ();
                    batchRenderer.color().set(batchRenderer.color().direction().flipZ());
                    SimpleDraw.simpleDrawAbove(turnModel, batchRenderer
                            .setFlipX(true).setFlipY(true)
                            .setScale(scaleX * 6f, scaleY * 2f)
                            .setOffset(offsetX + (sizeX - offCenter) * 6, offsetY + (turnModel.sizeX() + turnModel.sizeZ()) * 4)
                    );
                    batchRenderer.color().set(batchRenderer.color().direction().flipZ());
                    turnModel.turner().counterZ().counterZ().counterY().counterY();
                }
                break;
            case 2: // Side
                if (z45)
                    SimpleDraw.simpleDraw45(turnModel, batchRenderer
                            .setFlipX(false).setFlipY(false)
                            .setScale(scaleX * 4f, scaleY * 6f)
                            .setOffset(offsetX - offCenter * 2, offsetY)
                    );
                else
                    SimpleDraw.simpleDraw(turnModel, batchRenderer
                            .setFlipX(false).setFlipY(false)
                            .setScale(scaleX * 6f, scaleY * 6f)
                            .setOffset(offsetX - offCenter * 6, offsetY)
                    );
                break;
            case 3: // Above
                if (z45)
                    SimpleDraw.simpleDrawIso(turnModel, batchRenderer
                            .setFlipX(false).setFlipY(false)
                            .setScale(scaleX * 2f, scaleY)
                            .setOffset(offsetX - offCenter * 2, offsetY)
                    );
                else
                    SimpleDraw.simpleDrawAbove(turnModel, batchRenderer
                            .setFlipX(false).setFlipY(false)
                            .setScale(scaleX * 6f, scaleY * 2f)
                            .setOffset(offsetX - offCenter * 6, offsetY)
                    );
                break;
            case 4: // Top
                SimpleDraw.simpleDrawTop(turnModel, batchRenderer
                        .setFlipX(false).setFlipY(false)
                        .setScale(scaleX * 6f, scaleY * 6f)
                        .setOffset(offsetX - offCenter * 6, offsetY)
                );
                break;
            default:
                throw new IllegalStateException("In Turnable, angle set to unacceptable value: " + angle);
        }
        return this;
    }
}
