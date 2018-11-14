package warpwriter.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;

public class SpriteBatchVoxelRenderer implements IPixelRenderer, ITriangleRenderer, Disposable {
    protected SpriteBatch batch;
    protected Texture one, triangle;
    protected Color color = new Color();
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
        triangle = new Texture(pixmap1);
        triangle.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
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

    @Override
    public IPixelRenderer drawPixel(int x, int y, int color) {
        return drawRect(x, y, 1, 1, color);
    }

    @Override
    public IPixelRenderer drawRect(int x, int y, int xSize, int ySize, int color) {
        float oldColor = batch.getPackedColor(); // requires less conversions than batch.getColor(), same result
        this.color.set(color);
        batch.setColor(this.color);
        batch.draw(one, (x * scaleX) + offsetX, (y * scaleY) + offsetY, xSize * scaleX, ySize * scaleY);
        batch.setColor(oldColor);
        return this;
    }

    /**
     * Does not dispose the sprite batch!
     */
    @Override
    public void dispose() {
        one.dispose();
    }

    @Override
    public ITriangleRenderer drawLeftTriangle(int x, int y, int color) {
        return this;
    }

    @Override
    public ITriangleRenderer drawRightTriangle(int x, int y, int color) {
        float oldColor = batch.getPackedColor(); // requires less conversions than batch.getColor(), same result
        this.color.set(color);
        batch.setColor(this.color);
        batch.draw(triangle, (x * scaleX) + offsetX, (y * scaleY) + offsetY, scaleX, scaleY);
        batch.setColor(oldColor);
        return this;
    }
}
