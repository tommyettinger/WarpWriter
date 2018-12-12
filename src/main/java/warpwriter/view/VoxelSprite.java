package warpwriter.view;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import warpwriter.model.IModel;
import warpwriter.model.TurnModel;

public class VoxelSprite implements Disposable {
    protected VoxelSpriteBatchRenderer renderer;
    protected TurnModel turnModel = new TurnModel();

    public VoxelSprite() {
    }

    public VoxelSprite(SpriteBatch batch) {
        this();
        set(new VoxelSpriteBatchRenderer(batch));
    }

    public VoxelSprite set(IModel model) {
        turnModel.set(model);
        return this;
    }

    public VoxelSprite set(VoxelSpriteBatchRenderer renderer) {
        this.renderer = renderer;
        return this;
    }

    public VoxelSpriteBatchRenderer renderer() {
        return renderer;
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

    public VoxelSprite setZ45(boolean z45) {
        this.z45 = z45;
        return this;
    }

    protected int angle = 2;

    public int angle() {
        return angle;
    }

    public VoxelSprite setAngle(int angle) {
        this.angle = angle;
        return this;
    }

    protected float scaleX = 1f, scaleY = 1f;

    public VoxelSprite setScale(float scale) {
        return setScale(scale, scale);
    }

    public VoxelSprite setScale(float scaleX, float scaleY) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        return this;
    }

    protected int offsetX = 0, offsetY = 0;

    public VoxelSprite setOffset(int offsetX, int offsetY) {
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

    public VoxelSprite clockX() {
        turnModel.turner().clockX();
        return this;
    }

    public VoxelSprite counterX() {
        turnModel.turner().counterX();
        return this;
    }

    public VoxelSprite clockY() {
        turnModel.turner().clockY();
        return this;
    }

    public VoxelSprite counterY() {
        turnModel.turner().counterY();
        return this;
    }

    public VoxelSprite clockZ() {
        if (z45) {
            z45 = false;
            turnModel.turner().clockZ();
        } else
            z45 = true;
        return this;
    }

    public VoxelSprite counterZ() {
        if (z45)
            z45 = false;
        else {
            z45 = true;
            turnModel.turner().counterZ();
        }
        return this;
    }

    public VoxelSprite reset() {
        turnModel.turner().reset();
        return this.setZ45(false).setAngle(2);
    }

    public VoxelSprite render() {
        final int sizeX = (z45 ? VoxelDraw.isoWidth(turnModel) : turnModel.sizeY() - 1) * (int) scaleX,
                offCenter = sizeX / 2;
        switch (angle) {
            case 0: // Bottom
                VoxelDraw.drawBottom(turnModel, renderer
                        .setFlipX(false).setFlipY(false)
                        .setScale(scaleX * 6f, scaleY * 6f)
                        .setOffset(offsetX - offCenter * 6, offsetY)
                );
                break;
            case 1: // Below
                if (z45) {
                    turnModel.turner().clockY().clockY().clockZ();
                    VoxelDraw.drawIso(turnModel, renderer
                            .setFlipX(true).setFlipY(true)
                            .setScale(scaleX * 2f, scaleY)
                            .setOffset(offsetX + (sizeX - offCenter) * 2, offsetY + VoxelDraw.isoHeight(turnModel))
                    );
                    turnModel.turner().counterZ().counterY().counterY();
                } else {
                    turnModel.turner().clockY().clockY().clockZ().clockZ();
                    renderer.color().set(renderer.color().direction().flipZ());
                    VoxelDraw.drawAbove(turnModel, renderer
                            .setFlipX(true).setFlipY(true)
                            .setScale(scaleX * 6f, scaleY * 2f)
                            .setOffset(offsetX + (sizeX - offCenter) * 6, offsetY + (turnModel.sizeX() + turnModel.sizeZ()) * 4)
                    );
                    renderer.color().set(renderer.color().direction().flipZ());
                    turnModel.turner().counterZ().counterZ().counterY().counterY();
                }
                break;
            case 2: // Side
                if (z45)
                    VoxelDraw.draw45(turnModel, renderer
                            .setFlipX(false).setFlipY(false)
                            .setScale(scaleX * 4f, scaleY * 6f)
                            .setOffset(offsetX - offCenter * 2, offsetY)
                    );
                else
                    VoxelDraw.drawRight6PeekTop(turnModel, renderer
                            .setFlipX(false).setFlipY(false)
                            .setScale(scaleX, scaleY)
                            .setOffset(offsetX - offCenter * 6, offsetY)
                    );
                break;
            case 3: // Above
                if (z45)
                    VoxelDraw.drawIso(turnModel, renderer
                            .setFlipX(false).setFlipY(false)
                            .setScale(scaleX * 2f, scaleY)
                            .setOffset(offsetX - offCenter * 2, offsetY)
                    );
                else
                    VoxelDraw.drawAbove(turnModel, renderer
                            .setFlipX(false).setFlipY(false)
                            .setScale(scaleX * 6f, scaleY * 2f)
                            .setOffset(offsetX - offCenter * 6, offsetY)
                    );
                break;
            case 4: // Top
                VoxelDraw.drawTop(turnModel, renderer
                        .setFlipX(false).setFlipY(false)
                        .setScale(scaleX * 6f, scaleY * 6f)
                        .setOffset(offsetX - offCenter * 6, offsetY)
                );
                break;
            default:
                throw new IllegalStateException("In VoxelSprite, angle set to unacceptable value: " + angle);
        }
        return this;
    }

    @Override
    public void dispose() {
        if (renderer != null)
            renderer.dispose();
    }
}
