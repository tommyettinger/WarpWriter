package warpwriter.view.render;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Disposable;
import squidpony.squidmath.NumberTools;
import warpwriter.view.color.VoxelColor;

/**
 * Renders voxel models to a libGDX Batch.
 *
 * @author Ben McLean
 */
public class VoxelSpriteBatchRenderer implements IRectangleRenderer, ITriangleRenderer, Disposable {
    public VoxelSpriteBatchRenderer(Batch batch) {
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

    protected Batch batch;

    public VoxelSpriteBatchRenderer set(Batch batch) {
        this.batch = batch;
        return this;
    }

    public Batch batch() {
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

    public VoxelSpriteBatchRenderer drawPixel(int x, int y, int color) {
        return rect(x, y, 1, 1, color);
    }

    public VoxelSpriteBatchRenderer rect(int x, int y, int sizeX, int sizeY, int color) {
        //final float oldColor = batch.getPackedColor(); // requires less conversions than batch.getColor(), same result
        batch.setPackedColor(NumberTools.reversedIntBitsToFloat(transparency(color) & 0xFFFFFFFE));
        // color is an RGBA int, batch takes an ABGR int with only 7 bits of alpha allowed.
        // this masks out one bit of alpha (libGDX thing to avoid bad floats like NaN), and converts to float color
        // I think SquidLib's reversedIntBitsToFloat() is slightly more efficient than libGDX's NumberUtils, not sure.
        // it has optimizations on GWT, mainly.
        // float colors can be used by Batch without needing an object.
        batch.draw(one,
                scaleX * (flipX ? -x : x) + offsetX,
                scaleY * (flipY ? -y : y) + offsetY,
                sizeX * scaleX,
                sizeY * scaleY
        );
        //batch.setColor(oldColor);
        return this;
    }

    @Override
    public VoxelSpriteBatchRenderer drawLeftTriangle(int x, int y, int color) {
        return drawTriangle(x, y, color, true);
    }

    @Override
    public VoxelSpriteBatchRenderer drawRightTriangle(int x, int y, int color) {
        return drawTriangle(x, y, color, false);
    }

    public VoxelSpriteBatchRenderer drawTriangle(int x, int y, int color, boolean left) {
        float oldColor = batch.getPackedColor(); // requires less conversions than batch.getColor(), same result
        batch.setPackedColor(NumberTools.reversedIntBitsToFloat(transparency(color) & 0xFFFFFFFE));
        // converts RGBA int to ABGR int, masks out one bit of alpha (libGDX thing), and converts to float color
        // I think SquidLib's intBitsToFloat() is slightly more efficient than libGDX's NumberUtils, not sure.
        // float colors can be used by Batch without needing an object.
        triangle.setFlip(left, false);
        batch.draw(triangle,
                x * scaleX * (flipX ? -1 : 1) + offsetX,
                y * scaleY * (flipY ? -1 : 1) + offsetY,
                scaleX * 2,
                scaleY * 3
        );
        batch.setPackedColor(oldColor);
        return this;
    }

    @Override
    public VoxelSpriteBatchRenderer rectVertical(int x, int y, int sizeX, int sizeY, byte voxel) {
        return rect(x, y, sizeX, sizeY, color.verticalFace(voxel));
    }

    @Override
    public VoxelSpriteBatchRenderer rectLeft(int x, int y, int sizeX, int sizeY, byte voxel) {
        return rect(x, y, sizeX, sizeY,
                flipX ?
                        color.rightFace(voxel)
                        : color.leftFace(voxel)
        );
    }

    @Override
    public VoxelSpriteBatchRenderer rectRight(int x, int y, int sizeX, int sizeY, byte voxel) {
        return rect(x, y, sizeX, sizeY,
                flipX ?
                        color.leftFace(voxel)
                        : color.rightFace(voxel)
        );
    }

    @Override
    public VoxelSpriteBatchRenderer rectVertical(int px, int py, int sizeX, int sizeY, byte voxel, int depth, int vx, int vy, int vz) {
        return rect(px, py, sizeX, sizeY, color.verticalFace(voxel, vx, vy, vz));
    }

    @Override
    public VoxelSpriteBatchRenderer rectLeft(int px, int py, int sizeX, int sizeY, byte voxel, int depth, int vx, int vy, int vz) {
        return rect(px, py, sizeX, sizeY, color.leftFace(voxel, vx, vy, vz));
    }

    @Override
    public VoxelSpriteBatchRenderer rectRight(int px, int py, int sizeX, int sizeY, byte voxel, int depth, int vx, int vy, int vz) {
        return rect(px, py, sizeX, sizeY, color.rightFace(voxel, vx, vy, vz));
    }

    @Override
    public VoxelSpriteBatchRenderer drawLeftTriangleVerticalFace(int x, int y, byte voxel, int depth, int vx, int vy, int vz) {
        final int color = this.color.verticalFace(voxel, vx, vy, vz);
        return flipX ?
                drawRightTriangle(x, y, color)
                : drawLeftTriangle(x, y, color);
    }

    @Override
    public VoxelSpriteBatchRenderer drawLeftTriangleLeftFace(int x, int y, byte voxel, int depth, int vx, int vy, int vz) {
        final int color = flipX ?
                this.color.rightFace(voxel, vx, vy, vz)
                : this.color.leftFace(voxel, vx, vy, vz);
        return flipX ?
                drawRightTriangle(x, y, color)
                : drawLeftTriangle(x, y, color);
    }

    @Override
    public VoxelSpriteBatchRenderer drawLeftTriangleRightFace(int x, int y, byte voxel, int depth, int vx, int vy, int vz) {
        final int color = flipX ?
                this.color.leftFace(voxel, vx, vy, vz)
                : this.color.rightFace(voxel, vx, vy, vz);
        return flipX ?
                drawRightTriangle(x, y, color)
                : drawLeftTriangle(x, y, color);
    }

    @Override
    public VoxelSpriteBatchRenderer drawRightTriangleVerticalFace(int x, int y, byte voxel, int depth, int vx, int vy, int vz) {
        final int color = this.color.verticalFace(voxel, vx, vy, vz);
        return flipX ?
                drawLeftTriangle(x, y, color)
                : drawRightTriangle(x, y, color);
    }

    @Override
    public VoxelSpriteBatchRenderer drawRightTriangleLeftFace(int x, int y, byte voxel, int depth, int vx, int vy, int vz) {
        final int color = flipX ?
                this.color.rightFace(voxel, vx, vy, vz)
                : this.color.leftFace(voxel, vx, vy, vz);
        return flipX ?
                drawLeftTriangle(x, y, color)
                : drawRightTriangle(x, y, color);
    }

    @Override
    public VoxelSpriteBatchRenderer drawRightTriangleRightFace(int x, int y, byte voxel, int depth, int vx, int vy, int vz) {
        final int color = flipX ?
                this.color.leftFace(voxel, vx, vy, vz)
                : this.color.rightFace(voxel, vx, vy, vz);
        return flipX ?
                drawLeftTriangle(x, y, color)
                : drawRightTriangle(x, y, color);
    }

    @Override
    public VoxelSpriteBatchRenderer drawLeftTriangleVerticalFace(int x, int y, byte voxel, int vx, int vy, int vz) {
        final int color = this.color.verticalFace(voxel, vx, vy, vz);
        return flipX ?
                drawRightTriangle(x, y, color)
                : drawLeftTriangle(x, y, color);
    }

    @Override
    public VoxelSpriteBatchRenderer drawLeftTriangleLeftFace(int x, int y, byte voxel, int vx, int vy, int vz) {
        final int color = flipX ?
                this.color.rightFace(voxel, vx, vy, vz)
                : this.color.leftFace(voxel, vx, vy, vz);
        return flipX ?
                drawRightTriangle(x, y, color)
                : drawLeftTriangle(x, y, color);
    }

    @Override
    public VoxelSpriteBatchRenderer drawLeftTriangleRightFace(int x, int y, byte voxel, int vx, int vy, int vz) {
        final int color = flipX ?
                this.color.leftFace(voxel, vx, vy, vz)
                : this.color.rightFace(voxel, vx, vy, vz);
        return flipX ?
                drawRightTriangle(x, y, color)
                : drawLeftTriangle(x, y, color);
    }

    @Override
    public VoxelSpriteBatchRenderer drawRightTriangleVerticalFace(int x, int y, byte voxel, int vx, int vy, int vz) {
        final int color = this.color.verticalFace(voxel, vx, vy, vz);
        return flipX ?
                drawLeftTriangle(x, y, color)
                : drawRightTriangle(x, y, color);
    }

    @Override
    public VoxelSpriteBatchRenderer drawRightTriangleLeftFace(int x, int y, byte voxel, int vx, int vy, int vz) {
        final int color = flipX ?
                this.color.rightFace(voxel, vx, vy, vz)
                : this.color.leftFace(voxel, vx, vy, vz);
        return flipX ?
                drawLeftTriangle(x, y, color)
                : drawRightTriangle(x, y, color);
    }

    @Override
    public VoxelSpriteBatchRenderer drawRightTriangleRightFace(int x, int y, byte voxel, int vx, int vy, int vz) {
        final int color = flipX ?
                this.color.leftFace(voxel, vx, vy, vz)
                : this.color.rightFace(voxel, vx, vy, vz);
        return flipX ?
                drawLeftTriangle(x, y, color)
                : drawRightTriangle(x, y, color);
    }

    @Override
    public VoxelSpriteBatchRenderer drawTriangleOutline(int x, int y, byte voxel, int vx, int vy, int vz) {
        return this;
    }

    protected byte transparency = Byte.MAX_VALUE;

    @Override
    public final byte transparency() {
        return transparency;
    }

    @Override
    public VoxelSpriteBatchRenderer setTransparency(byte transparency) {
        this.transparency = transparency;
        return this;
    }

    public final int transparency(int color) {
        return transparency(color, transparency);
    }

    public static int transparency(int color, byte transparency) {
        return (color & 0xFFFFFF00) | ((color & 0xFF) * (transparency + 1) >>> 7);
    }
}
