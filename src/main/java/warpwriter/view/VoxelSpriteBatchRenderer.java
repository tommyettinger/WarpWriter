package warpwriter.view;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.NumberUtils;

/**
 * Renders voxel models to a libGDX SpriteBatch.
 *
 * @author Ben McLean
 */
public class VoxelSpriteBatchRenderer implements IRectangleRenderer, ITriangleRenderer, Disposable {
    public VoxelSpriteBatchRenderer(SpriteBatch batch) {
        this();
        set(batch);
    }

    private Texture one;
    private Sprite triangle;

    public VoxelSpriteBatchRenderer() {
        //creating "one" texture.
        Pixmap pixmap1 = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap1.drawPixel(0, 0, -1);
        one = new Texture(pixmap1);
        one.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        pixmap1.dispose();

        //creating "triangle" texture.
        pixmap1 = new Pixmap(2, 3, Pixmap.Format.RGBA8888);
        pixmap1.drawPixel(0, 0, -1);
        pixmap1.drawPixel(0, 1, -1);
        pixmap1.drawPixel(1, 1, -1);
        pixmap1.drawPixel(0, 2, -1);
        Texture triangle = new Texture(pixmap1);
        triangle.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        this.triangle = new Sprite(triangle);
        pixmap1.dispose();
    }

    /**
     * Does not dispose the sprite batch!
     */
    @Override
    public void dispose() {
        one.dispose();
        triangle.getTexture().dispose();
    }

    protected VoxelColor color = new VoxelColor();

    public VoxelColor color() {
        return color;
    }

    public VoxelSpriteBatchRenderer set(VoxelColor color) {
        this.color = color;
        return this;
    }

    protected SpriteBatch batch;

    public VoxelSpriteBatchRenderer set(SpriteBatch batch) {
        this.batch = batch;
        return this;
    }

    public SpriteBatch batch() {
        return batch;
    }

    protected int offsetX = 0, offsetY = 0;

    public VoxelSpriteBatchRenderer setOffset(int offsetX, int offsetY) {
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

    protected boolean flipX = false, flipY = false;

    public VoxelSpriteBatchRenderer flipX() {
        flipX = !flipX;
        return this;
    }

    public VoxelSpriteBatchRenderer flipY() {
        flipY = !flipY;
        return this;
    }

    public boolean getFlipX() {
        return flipX;
    }

    public boolean getFlipY() {
        return flipY;
    }

    public VoxelSpriteBatchRenderer setFlipX(boolean flipX) {
        this.flipX = flipX;
        return this;
    }

    public VoxelSpriteBatchRenderer setFlipY(boolean flipY) {
        this.flipY = flipY;
        return this;
    }

    protected float scaleX = 1f, scaleY = 1f;

    public VoxelSpriteBatchRenderer multiplyScale(float multiplier) {
        return setScale(scaleX * multiplier, scaleY * multiplier);
    }

    public VoxelSpriteBatchRenderer setScale(float scale) {
        return setScale(scale, scale);
    }

    public VoxelSpriteBatchRenderer setScale(float scaleX, float scaleY) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        return this;
    }

    public IRectangleRenderer drawPixel(int x, int y, int color) {
        return rect(x, y, 1, 1, color);
    }

    public IRectangleRenderer rect(int x, int y, int sizeX, int sizeY, int color) {
        //final float oldColor = batch.getPackedColor(); // requires less conversions than batch.getColor(), same result
        batch.setColor(NumberUtils.intToFloatColor(Integer.reverseBytes(color)));
        // converts from RGBA to ABGR
        // converts ABGR int to float color, which SpriteBatch can use without needing an object
        batch.draw(one,
                x * scaleX * (flipX ? -1 : 1) + offsetX,
                y * scaleY * (flipY ? -1 : 1) + offsetY,
                sizeX * scaleX,
                sizeY * scaleY
        );
        //batch.setColor(oldColor);
        return this;
    }

    @Override
    public ITriangleRenderer drawLeftTriangle(int x, int y, int color) {
        return drawTriangle(x, y, color, true);
    }

    @Override
    public ITriangleRenderer drawRightTriangle(int x, int y, int color) {
        return drawTriangle(x, y, color, false);
    }

    public VoxelSpriteBatchRenderer drawTriangle(int x, int y, int color, boolean left) {
        float oldColor = batch.getPackedColor(); // requires less conversions than batch.getColor(), same result
        batch.setColor(NumberUtils.intToFloatColor(Integer.reverseBytes(color)));
        // converts from RGBA to ABGR
        // converts ABGR int to float color, which SpriteBatch can use without needing an object
        triangle.setFlip(left, false);
        batch.draw(triangle,
                x * scaleX * (flipX ? -1 : 1) + offsetX,
                y * scaleY * (flipY ? -1 : 1) + offsetY,
                scaleX * 2,
                scaleY * 3
        );
        batch.setColor(oldColor);
        return this;
    }

    @Override
    public IRectangleRenderer rectVertical(int x, int y, int sizeX, int sizeY, byte voxel) {
        return rect(x, y, sizeX, sizeY,
                flipY ?
                        color.bottomFace(voxel)
                        : color.topFace(voxel)
        );
    }

    @Override
    public IRectangleRenderer rectLeft(int x, int y, int sizeX, int sizeY, byte voxel) {
        return rect(x, y, sizeX, sizeY,
                flipX ?
                        color.rightFace(voxel)
                        : color.leftFace(voxel)
        );
    }

    @Override
    public IRectangleRenderer rectRight(int x, int y, int sizeX, int sizeY, byte voxel) {
        return rect(x, y, sizeX, sizeY,
                flipX ?
                        color.leftFace(voxel)
                        : color.rightFace(voxel)
        );
    }

    @Override
    public ITriangleRenderer drawLeftTriangleVerticalFace(int x, int y, byte voxel) {
        final int color = flipY ?
                this.color.bottomFace(voxel)
                : this.color.topFace(voxel);
        return flipX ?
                drawRightTriangle(x, y, color)
                : drawLeftTriangle(x, y, color);
    }

    @Override
    public ITriangleRenderer drawLeftTriangleLeftFace(int x, int y, byte voxel) {
        final int color = flipX ?
                this.color.rightFace(voxel)
                : this.color.leftFace(voxel);
        return flipX ?
                drawRightTriangle(x, y, color)
                : drawLeftTriangle(x, y, color);
    }

    @Override
    public ITriangleRenderer drawLeftTriangleRightFace(int x, int y, byte voxel) {
        final int color = flipX ?
                this.color.leftFace(voxel)
                : this.color.rightFace(voxel);
        return flipX ?
                drawRightTriangle(x, y, color)
                : drawLeftTriangle(x, y, color);
    }

    @Override
    public ITriangleRenderer drawRightTriangleVerticalFace(int x, int y, byte voxel) {
        final int color = flipY ?
                this.color.bottomFace(voxel)
                : this.color.topFace(voxel);
        return flipX ?
                drawLeftTriangle(x, y, color)
                : drawRightTriangle(x, y, color);
    }

    @Override
    public ITriangleRenderer drawRightTriangleLeftFace(int x, int y, byte voxel) {
        final int color = flipX ?
                this.color.rightFace(voxel)
                : this.color.leftFace(voxel);
        return flipX ?
                drawLeftTriangle(x, y, color)
                : drawRightTriangle(x, y, color);
    }

    @Override
    public ITriangleRenderer drawRightTriangleRightFace(int x, int y, byte voxel) {
        final int color = flipX ?
                this.color.leftFace(voxel)
                : this.color.rightFace(voxel);
        return flipX ?
                drawLeftTriangle(x, y, color)
                : drawRightTriangle(x, y, color);
    }
}
