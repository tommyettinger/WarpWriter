package warpwriter.view.render;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;
import squidpony.squidmath.NumberTools;
import warpwriter.view.color.VoxelColor;

/**
 * Renders voxel models to a libGDX SpriteBatch.
 *
 * @author Ben McLean
 */
public class VoxelImmediateRenderer implements IRectangleRenderer, ITriangleRenderer, Disposable {

    public VoxelImmediateRenderer(){
        this(640, 480);
    }
    public VoxelImmediateRenderer(int width, int height) {
        batch = new ImmediateModeRenderer20(200000, false, true, 0);
        proj = new Matrix4();
        proj.setToOrtho2D(0, 0, width, height);
    }

    /**
     * No-op.
     */
    @Override
    public void dispose() {
    }

    protected VoxelColor color = new VoxelColor();

    public VoxelColor color() {
        return color;
    }

    public VoxelImmediateRenderer set(VoxelColor color) {
        this.color = color;
        return this;
    }

    protected ImmediateModeRenderer20 batch;
    protected Matrix4 proj;

    public VoxelImmediateRenderer set(ImmediateModeRenderer20 batch) {
        this.batch = batch;
        return this;
    }

    public ImmediateModeRenderer20 batch() {
        return batch;
    }

    protected int offsetX = 0, offsetY = 0;

    public VoxelImmediateRenderer setOffset(int offsetX, int offsetY) {
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

    public VoxelImmediateRenderer flipX() {
        flipX = !flipX;
        return this;
    }

    public VoxelImmediateRenderer flipY() {
        flipY = !flipY;
        return this;
    }

    public boolean getFlipX() {
        return flipX;
    }

    public boolean getFlipY() {
        return flipY;
    }

    public VoxelImmediateRenderer setFlipX(boolean flipX) {
        this.flipX = flipX;
        return this;
    }

    public VoxelImmediateRenderer setFlipY(boolean flipY) {
        this.flipY = flipY;
        return this;
    }

    protected int scaleX = 1, scaleY = 1;

    public VoxelImmediateRenderer multiplyScale(float multiplier) {
        return setScale((int) (scaleX * multiplier), (int) (scaleY * multiplier));
    }

    public VoxelImmediateRenderer setScale(int scale) {
        return setScale(scale, scale);
    }

    public VoxelImmediateRenderer setScale(int scaleX, int scaleY) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        return this;
    }

    public VoxelImmediateRenderer drawPixel(int x, int y, int color) {
        return rect(x, y, 1, 1, color);
    }

    public VoxelImmediateRenderer rect(int x, int y, int sizeX, int sizeY, int color) {
        final float c = NumberTools.reversedIntBitsToFloat(transparency(color) & 0xFFFFFFFE);
        x = scaleX * (flipX ? -x : x) + offsetX;
        y = scaleY * (flipY ? -y : y) + offsetY;
        sizeX *= scaleX;
        sizeY *= scaleY;
        batch.color(c);
        batch.vertex(x, y, 0);
        batch.color(c);
        batch.vertex(x + sizeX, y, 0);
        batch.color(c);
        batch.vertex(x + sizeX, y + sizeY, 0);
        batch.color(c);
        batch.vertex(x, y, 0);
        batch.color(c);
        batch.vertex(x + sizeX, y + sizeY, 0);
        batch.color(c);
        batch.vertex(x, y + sizeY, 0);
        return this;
    }

    @Override
    public VoxelImmediateRenderer drawLeftTriangle(int x, int y, int color) {
        final float c = NumberTools.reversedIntBitsToFloat(transparency(color) & 0xFFFFFFFE);
        x = scaleX * (flipX ? -x : x) + offsetX;
        y = scaleY * (flipY ? -y : y) + offsetY;
        int sizeX = 2 * scaleX;
        batch.color(c);
        batch.vertex(x + sizeX, y, 0);
        batch.color(c);
        batch.vertex(x + sizeX, y + 3 * scaleY, 0);
        batch.color(c);
        batch.vertex(x, y + 1.5f * scaleY, 0);
        return this;
    }

    @Override
    public VoxelImmediateRenderer drawRightTriangle(int x, int y, int color) {
        final float c = NumberTools.reversedIntBitsToFloat(transparency(color) & 0xFFFFFFFE);
        x = scaleX * (flipX ? -x : x) + offsetX;
        y = scaleY * (flipY ? -y : y) + offsetY;
        batch.color(c);
        batch.vertex(x, y, 0);
        batch.color(c);
        batch.vertex(x + 2 * scaleX, y + 1.5f * scaleY, 0);
        batch.color(c);
        batch.vertex(x, y + 3 * scaleY, 0);
        return this;
    }
    
    @Override
    public VoxelImmediateRenderer rectVertical(int x, int y, int sizeX, int sizeY, byte voxel) {
        return rect(x, y, sizeX, sizeY, color.verticalFace(voxel));
    }

    @Override
    public VoxelImmediateRenderer rectLeft(int x, int y, int sizeX, int sizeY, byte voxel) {
        return rect(x, y, sizeX, sizeY,
                flipX ?
                        color.rightFace(voxel)
                        : color.leftFace(voxel)
        );
    }

    @Override
    public VoxelImmediateRenderer rectRight(int x, int y, int sizeX, int sizeY, byte voxel) {
        return rect(x, y, sizeX, sizeY,
                flipX ?
                        color.leftFace(voxel)
                        : color.rightFace(voxel)
        );
    }

    @Override
    public VoxelImmediateRenderer rectVertical(int px, int py, int sizeX, int sizeY, byte voxel, int depth, int vx, int vy, int vz) {
        return rect(px, py, sizeX, sizeY, color.verticalFace(voxel, vx, vy, vz));
    }

    @Override
    public VoxelImmediateRenderer rectLeft(int px, int py, int sizeX, int sizeY, byte voxel, int depth, int vx, int vy, int vz) {
        return rect(px, py, sizeX, sizeY, color.leftFace(voxel, vx, vy, vz));
    }

    @Override
    public VoxelImmediateRenderer rectRight(int px, int py, int sizeX, int sizeY, byte voxel, int depth, int vx, int vy, int vz) {
        return rect(px, py, sizeX, sizeY, color.rightFace(voxel, vx, vy, vz));
    }

    @Override
    public VoxelImmediateRenderer drawLeftTriangleVerticalFace(int x, int y, byte voxel, int depth, int vx, int vy, int vz) {
        final int color = this.color.verticalFace(voxel, vx, vy, vz);
        return flipX ?
                drawRightTriangle(x, y, color)
                : drawLeftTriangle(x, y, color);
    }

    @Override
    public VoxelImmediateRenderer drawLeftTriangleLeftFace(int x, int y, byte voxel, int depth, int vx, int vy, int vz) {
        final int color = flipX ?
                this.color.rightFace(voxel, vx, vy, vz)
                : this.color.leftFace(voxel, vx, vy, vz);
        return flipX ?
                drawRightTriangle(x, y, color)
                : drawLeftTriangle(x, y, color);
    }

    @Override
    public VoxelImmediateRenderer drawLeftTriangleRightFace(int x, int y, byte voxel, int depth, int vx, int vy, int vz) {
        final int color = flipX ?
                this.color.leftFace(voxel, vx, vy, vz)
                : this.color.rightFace(voxel, vx, vy, vz);
        return flipX ?
                drawRightTriangle(x, y, color)
                : drawLeftTriangle(x, y, color);
    }

    @Override
    public VoxelImmediateRenderer drawRightTriangleVerticalFace(int x, int y, byte voxel, int depth, int vx, int vy, int vz) {
        final int color = this.color.verticalFace(voxel, vx, vy, vz);
        return flipX ?
                drawLeftTriangle(x, y, color)
                : drawRightTriangle(x, y, color);
    }

    @Override
    public VoxelImmediateRenderer drawRightTriangleLeftFace(int x, int y, byte voxel, int depth, int vx, int vy, int vz) {
        final int color = flipX ?
                this.color.rightFace(voxel, vx, vy, vz)
                : this.color.leftFace(voxel, vx, vy, vz);
        return flipX ?
                drawLeftTriangle(x, y, color)
                : drawRightTriangle(x, y, color);
    }

    @Override
    public VoxelImmediateRenderer drawRightTriangleRightFace(int x, int y, byte voxel, int depth, int vx, int vy, int vz) {
        final int color = flipX ?
                this.color.leftFace(voxel, vx, vy, vz)
                : this.color.rightFace(voxel, vx, vy, vz);
        return flipX ?
                drawLeftTriangle(x, y, color)
                : drawRightTriangle(x, y, color);
    }

    @Override
    public VoxelImmediateRenderer drawLeftTriangleVerticalFace(int x, int y, byte voxel, int vx, int vy, int vz) {
        final int color = this.color.verticalFace(voxel, vx, vy, vz);
        return flipX ?
                drawRightTriangle(x, y, color)
                : drawLeftTriangle(x, y, color);
    }

    @Override
    public VoxelImmediateRenderer drawLeftTriangleLeftFace(int x, int y, byte voxel, int vx, int vy, int vz) {
        final int color = flipX ?
                this.color.rightFace(voxel, vx, vy, vz)
                : this.color.leftFace(voxel, vx, vy, vz);
        return flipX ?
                drawRightTriangle(x, y, color)
                : drawLeftTriangle(x, y, color);
    }

    @Override
    public VoxelImmediateRenderer drawLeftTriangleRightFace(int x, int y, byte voxel, int vx, int vy, int vz) {
        final int color = flipX ?
                this.color.leftFace(voxel, vx, vy, vz)
                : this.color.rightFace(voxel, vx, vy, vz);
        return flipX ?
                drawRightTriangle(x, y, color)
                : drawLeftTriangle(x, y, color);
    }

    @Override
    public VoxelImmediateRenderer drawRightTriangleVerticalFace(int x, int y, byte voxel, int vx, int vy, int vz) {
        final int color = this.color.verticalFace(voxel, vx, vy, vz);
        return flipX ?
                drawLeftTriangle(x, y, color)
                : drawRightTriangle(x, y, color);
    }

    @Override
    public VoxelImmediateRenderer drawRightTriangleLeftFace(int x, int y, byte voxel, int vx, int vy, int vz) {
        final int color = flipX ?
                this.color.rightFace(voxel, vx, vy, vz)
                : this.color.leftFace(voxel, vx, vy, vz);
        return flipX ?
                drawLeftTriangle(x, y, color)
                : drawRightTriangle(x, y, color);
    }

    @Override
    public VoxelImmediateRenderer drawRightTriangleRightFace(int x, int y, byte voxel, int vx, int vy, int vz) {
        final int color = flipX ?
                this.color.leftFace(voxel, vx, vy, vz)
                : this.color.rightFace(voxel, vx, vy, vz);
        return flipX ?
                drawLeftTriangle(x, y, color)
                : drawRightTriangle(x, y, color);
    }

    protected byte transparency = Byte.MAX_VALUE;

    @Override
    public final byte transparency() {
        return transparency;
    }

    @Override
    public VoxelImmediateRenderer setTransparency(byte transparency) {
        this.transparency = transparency;
        return this;
    }

    public final int transparency(int color) {
        return transparency(color, transparency);
    }

    public static int transparency(int color, byte transparency) {
        return (color & 0xFFFFFF00) | ((color & 0xFF) * (transparency + 1) >>> 7);
    }

    public void begin()
    {
        batch.begin(proj, GL20.GL_TRIANGLES);
    }
    public void end()
    {
        batch.end();
    }
}
