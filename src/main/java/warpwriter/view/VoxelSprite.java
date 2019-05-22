package warpwriter.view;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import warpwriter.model.IModel;
import warpwriter.model.TurnModel;
import warpwriter.model.nonvoxel.ITurner;
import warpwriter.view.render.VoxelSpriteBatchRenderer;

/**
 * This class is can be understood as like the Sprite class in libGDX, except it is for rendering voxel models instead
 * of 2D textures.
 * <p>
 * An expected use case would be to make a VoxelSprite for every voxel model that needs to be rendered on screen, but
 * it's possible to also re-use the same instance by rapidly swapping out models and other settings.
 *
 * @author Ben McLean
 */
public class VoxelSprite implements Disposable, ITurner {
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

    public float scaleX() {
        return scaleX;
    }

    public float scaleY() {
        return scaleY;
    }

    public VoxelSprite addScale(float x, float y) {
        return setScale(scaleX() + x, scaleY() + y);
    }

    public VoxelSprite multiplyScale(float x, float y) {
        return setScale(scaleX() * x).setScale(scaleY() * y);
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

    /**
     * Rotates clockwise on the X axis 90 degrees.
     *
     * @return this
     */
    public VoxelSprite clockX() {
        turnModel.turner().clockX();
        return this;
    }

    /**
     * Rotates counterclockwise on the X axis 90 degrees.
     *
     * @return this
     */
    public VoxelSprite counterX() {
        turnModel.turner().counterX();
        return this;
    }

    /**
     * Rotates clockwise on the Y axis 90 degrees.
     *
     * @return this
     */
    public VoxelSprite clockY() {
        turnModel.turner().clockY();
        return this;
    }

    /**
     * Rotates counterclockwise on the Y axis 90 degrees.
     *
     * @return this
     */
    public VoxelSprite counterY() {
        turnModel.turner().counterY();
        return this;
    }

    /**
     * Rotates clockwise on the Z axis 45 degrees.
     *
     * @return this
     */
    public VoxelSprite clockZ() {
        if (z45) {
            z45 = false;
            turnModel.turner().clockZ();
        } else
            z45 = true;
        return this;
    }

    /**
     * Rotates counterclockwise on the Z axis 45 degrees.
     *
     * @return this
     */
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

    @Override
    public float angleX() {
        return 90f;
    }

    @Override
    public float angleY() {
        return 90f;
    }

    @Override
    public float angleZ() {
        return 45f;
    }

    public VoxelSprite render() {
        final int sizeX = (z45 ? VoxelDraw.isoWidth(turnModel) : turnModel.sizeY() - 1) * (int) scaleX,
                offCenter = sizeX / 2;
        switch (angle) {
            case 0: // Bottom
                VoxelDraw.drawBottom(turnModel, renderer
                        .setFlipX(false).setFlipY(false)
                        .setScale(scaleX, scaleY)
                        .setOffset(offsetX - offCenter * 6, offsetY)
                );
                break;
            case 1: // Below
                if (z45) {
                    renderer.color().set(renderer.color().direction().flipY());
                    turnModel.turner().clockY().clockY().clockZ();
                    VoxelDraw.drawIso(turnModel, renderer
                            .setFlipX(true).setFlipY(true)
                            .setScale(scaleX * 2f, scaleY)
                            .setOffset(
                                    offsetX + (offCenter - 1) * 2,
                                    offsetY + (int) (VoxelDraw.isoHeight(turnModel) * scaleY)
                            )
                    );
                    turnModel.turner().counterZ().counterY().counterY();
                    renderer.color().set(renderer.color().direction().flipY());
                } else {
                    renderer.color().set(renderer.color().direction().opposite());
                    turnModel.turner().clockY().clockY().clockZ().clockZ();
                    VoxelDraw.drawAbove(turnModel, renderer
                            .setFlipX(true).setFlipY(true)
                            .setScale(scaleX, scaleY)
                            .setOffset(
                                    offsetX + (offCenter + 1) * 6,
                                    offsetY + (int) ((turnModel.sizeX() + turnModel.sizeZ()) * 4 * scaleY)
                            )
                    );
                    turnModel.turner().counterZ().counterZ().counterY().counterY();
                    renderer.color().set(renderer.color().direction().opposite());
                }
                break;
            case 2: // Side
                if (z45)
                    VoxelDraw.draw45Peek(turnModel, renderer
                            .setFlipX(false).setFlipY(false)
                            .setScale(scaleX, scaleY)
                            .setOffset(offsetX - offCenter * 2, offsetY)
                    );
                else
                    VoxelDraw.drawRightPeek(turnModel, renderer
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
                            .setScale(scaleX, scaleY)
                            .setOffset(offsetX - offCenter * 6, offsetY)
                    );
                break;
            case 4: // Top
                VoxelDraw.drawTop(turnModel, renderer
                        .setFlipX(false).setFlipY(false)
                        .setScale(scaleX, scaleY)
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
