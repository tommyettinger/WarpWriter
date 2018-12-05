package warpwriter.view;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.NumberUtils;

public class SpriteBatchVoxelRenderer implements IPixelRenderer, ITriangleRenderer, Disposable {
    protected static IVoxelColor color = new VoxelColor();
    protected SpriteBatch batch;
    protected Texture one;
    protected Sprite triangle;
    protected int offsetX = 0, offsetY = 0;
    protected float scaleX = 1f, scaleY = 1f;

    public SpriteBatchVoxelRenderer(SpriteBatch batch) {
        this.batch = batch;

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

    public SpriteBatchVoxelRenderer set(SpriteBatch batch) {
        this.batch = batch;
        return this;
    }

    public SpriteBatchVoxelRenderer setOffset(int offsetX, int offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        return this;
    }

    public SpriteBatchVoxelRenderer multiplyScale(float multiplier) {
        return setScale(scaleX * multiplier, scaleY * multiplier);
    }

    public SpriteBatchVoxelRenderer setScale(float scale) {
        return setScale(scale, scale);
    }

    public SpriteBatchVoxelRenderer setScale(float scaleX, float scaleY) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        return this;
    }

    public IPixelRenderer drawPixel(int x, int y, int color) {
        return drawRect(x, y, 1, 1, color);
    }

    public IPixelRenderer drawRect(int x, int y, int xSize, int ySize, int color) {
        //final float oldColor = batch.getPackedColor(); // requires less conversions than batch.getColor(), same result
        batch.setColor(NumberUtils.intToFloatColor(Integer.reverseBytes(color)));
        // converts from RGBA to ABGR
        // converts ABGR int to float color, which SpriteBatch can use without needing an object
        batch.draw(one, (x * scaleX) + offsetX, (y * scaleY) + offsetY, xSize * scaleX, ySize * scaleY);
        //batch.setColor(oldColor);
        return this;
    }

    /**
     * Does not dispose the sprite batch!
     */
    @Override
    public void dispose() {
        one.dispose();
        triangle.getTexture().dispose();
    }

    public ITriangleRenderer drawLeftTriangle(int x, int y, int color) {
        return drawTriangle(x, y, color, true);
    }

    public ITriangleRenderer drawRightTriangle(int x, int y, int color) {
        return drawTriangle(x, y, color, false);
    }

    public SpriteBatchVoxelRenderer drawTriangle(int x, int y, int color, boolean left) {
        float oldColor = batch.getPackedColor(); // requires less conversions than batch.getColor(), same result
        batch.setColor(NumberUtils.intToFloatColor(Integer.reverseBytes(color)));
        // converts from RGBA to ABGR
        // converts ABGR int to float color, which SpriteBatch can use without needing an object
        triangle.setFlip(left, false);
        batch.draw(triangle, (x * scaleX) + offsetX, (y * scaleY) + offsetY, scaleX * 2, scaleY * 3);
        batch.setColor(oldColor);
        return this;
    }

    @Override
    public IPixelRenderer drawPixelVerticalFace(int x, int y, byte voxel) {
        return drawPixel(x, y, SpriteBatchVoxelRenderer.color.topFace(voxel));
    }

    @Override
    public IPixelRenderer drawPixelLeftFace(int x, int y, byte voxel) {
        return drawPixel(x, y, SpriteBatchVoxelRenderer.color.leftFace(voxel));
    }

    @Override
    public IPixelRenderer drawPixelRightFace(int x, int y, byte voxel) {
        return drawPixel(x, y, SpriteBatchVoxelRenderer.color.rightFace(voxel));
    }

    @Override
    public ITriangleRenderer drawLeftTriangleVerticalFace(int x, int y, byte voxel) {
        return drawLeftTriangle(x, y, SpriteBatchVoxelRenderer.color.topFace(voxel));
    }

    @Override
    public ITriangleRenderer drawLeftTriangleLeftFace(int x, int y, byte voxel) {
        return drawLeftTriangle(x, y, SpriteBatchVoxelRenderer.color.leftFace(voxel));
    }

    @Override
    public ITriangleRenderer drawLeftTriangleRightFace(int x, int y, byte voxel) {
        return drawLeftTriangle(x, y, SpriteBatchVoxelRenderer.color.rightFace(voxel));
    }

    @Override
    public ITriangleRenderer drawRightTriangleVerticalFace(int x, int y, byte voxel) {
        return drawRightTriangle(x, y, SpriteBatchVoxelRenderer.color.topFace(voxel));
    }

    @Override
    public ITriangleRenderer drawRightTriangleLeftFace(int x, int y, byte voxel) {
        return drawRightTriangle(x, y, SpriteBatchVoxelRenderer.color.leftFace(voxel));
    }

    @Override
    public ITriangleRenderer drawRightTriangleRightFace(int x, int y, byte voxel) {
        return drawRightTriangle(x, y, SpriteBatchVoxelRenderer.color.rightFace(voxel));
    }
}
