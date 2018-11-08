package warpwriter.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;

public class SpriteBatchRenderer implements IRenderer, Disposable {
    protected SpriteBatch batch;
    protected Texture one;
    protected Color color = new Color();
    protected int offsetX=0, offsetY=0;
    protected float scaleX=1f, scaleY=1f;

    public SpriteBatchRenderer(SpriteBatch batch) {
        this.batch = batch;

        //creating "one" texture.
        Pixmap pixmap1 = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap1.drawPixel(0, 0, -1);
        one = new Texture(pixmap1);
        one.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        pixmap1.dispose();
    }

    public SpriteBatchRenderer set(SpriteBatch batch) {
        this.batch = batch;
        return this;
    }

    public SpriteBatchRenderer setOffset(int offsetX, int offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        return this;
    }

    @Override
    public SpriteBatchRenderer multiplyScale(float multiplier)
    {
        return setScale(scaleX * multiplier, scaleY * multiplier);
    }
    
    public SpriteBatchRenderer setScale(float scale) {
        return setScale(scale, scale);
    }

    public SpriteBatchRenderer setScale(float scaleX, float scaleY) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        return this;
    }

    @Override
    public IRenderer drawPixel(int x, int y, int color) {
        Color oldColor = batch.getColor();
        this.color.set(color);
        batch.setColor(this.color);
        batch.draw(one, (x * scaleX) + offsetX, (y * scaleY) + offsetY, scaleX, scaleY);
        batch.setColor(oldColor);
        return this;
    }

    @Override
    public int getPixel(int x, int y) {
        return 0;
    }

    /**
     * Does not dispose the sprite batch!
     */
    @Override
    public void dispose() {
        one.dispose();
    }
}
