package warpwriter.view.render;

import com.badlogic.gdx.graphics.Color;

/**
 * BlinkRenderer will flash a specified color to help identify which part of the code is rendering which pixels onscreen. Uses encapsulation to be compatible with IRectangleRenderer and ITriangleRenderer.
 *
 * @author Ben McLean
 */
public class BlinkRenderer implements IRectangleRenderer, ITriangleRenderer {
    protected boolean flash = false;
    protected long time = System.currentTimeMillis();
    protected final long duration = 500; // Milliseconds flashes last

    /**
     * @param voxel Color to show when not flashing
     * @return The flash color if flash is active, else the voxel parameter.
     */
    public byte f(byte voxel) {
        final long now = System.currentTimeMillis();
        if (now - time >= duration) {
            flash = !flash;
            time = now;
        }
        return flash ? this.voxel : voxel;
    }

    public byte f() {
        return f((byte) 0);
    }

    /**
     * @param color Color to show when not flashing
     * @return The flash color if flash is active, else the color parameter.
     */
    public int f(int color) {
        final long now = System.currentTimeMillis();
        if (now - time >= duration) {
            flash = !flash;
            time = now;
        }
        return flash ? this.color : color;
    }

    public int fint() {
        return f((int) 0);
    }

    protected byte voxel = 15;

    /**
     * @param voxel Sets which voxel color to flash
     * @return this, for method chaining
     */
    public BlinkRenderer set(byte voxel) {
        this.voxel = voxel;
        return this;
    }

    /**
     * @return The color of the flashes
     */
    public byte getVoxel() {
        return voxel;
    }

    protected int color = Color.rgba8888(Color.WHITE);

    public BlinkRenderer set(int color) {
        this.color = color;
        return this;
    }

    public int getColor() {
        return color;
    }

    protected IRectangleRenderer rect;

    public BlinkRenderer set(IRectangleRenderer rect) {
        this.rect = rect;
        return this;
    }

    public IRectangleRenderer getRect() {
        return rect;
    }

    protected ITriangleRenderer tri;

    public BlinkRenderer set(ITriangleRenderer tri) {
        this.tri = tri;
        return this;
    }

    public ITriangleRenderer getTri() {
        return tri;
    }

    @Override
    public IRectangleRenderer rect(int x, int y, int sizeX, int sizeY, int color) {
        return rect.rect(x, y, sizeX, sizeY, f(color));
    }

    @Override
    public IRectangleRenderer rectVertical(int x, int y, int sizeX, int sizeY, byte voxel) {
        return rect.rectVertical(x, y, sizeX, sizeY, f(voxel));
    }

    @Override
    public IRectangleRenderer rectLeft(int x, int y, int sizeX, int sizeY, byte voxel) {
        return rect.rectLeft(x, y, sizeX, sizeY, f(voxel));
    }

    @Override
    public IRectangleRenderer rectRight(int x, int y, int sizeX, int sizeY, byte voxel) {
        return rect.rectRight(x, y, sizeX, sizeY, f(voxel));
    }

    @Override
    public IRectangleRenderer rectVertical(int px, int py, int sizeX, int sizeY, byte voxel, int depth, int vx, int vy, int vz) {
        return rect.rectVertical(px, py, sizeX, sizeY, voxel, depth, vx, vy, vz);
    }

    @Override
    public IRectangleRenderer rectLeft(int px, int py, int sizeX, int sizeY, byte voxel, int depth, int vx, int vy, int vz) {
        return rect.rectLeft(px, py, sizeX, sizeY, voxel, depth, vx, vy, vz);
    }

    @Override
    public IRectangleRenderer rectRight(int px, int py, int sizeX, int sizeY, byte voxel, int depth, int vx, int vy, int vz) {
        return rect.rectRight(px, py, sizeX, sizeY, voxel, depth, vx, vy, vz);
    }

    @Override
    public ITriangleRenderer drawLeftTriangle(int x, int y, int color) {
        return tri.drawLeftTriangle(x, y, f(color));
    }

    @Override
    public ITriangleRenderer drawRightTriangle(int x, int y, int color) {
        return tri.drawRightTriangle(x, y, f(color));
    }

    @Override
    public ITriangleRenderer drawLeftTriangleVerticalFace(int x, int y, byte voxel) {
        return tri.drawLeftTriangleVerticalFace(x, y, f(voxel));
    }

    @Override
    public ITriangleRenderer drawLeftTriangleLeftFace(int x, int y, byte voxel) {
        return tri.drawLeftTriangleLeftFace(x, y, f(voxel));
    }

    @Override
    public ITriangleRenderer drawLeftTriangleRightFace(int x, int y, byte voxel) {
        return tri.drawLeftTriangleRightFace(x, y, f(voxel));
    }

    @Override
    public ITriangleRenderer drawRightTriangleVerticalFace(int x, int y, byte voxel) {
        return tri.drawRightTriangleVerticalFace(x, y, f(voxel));
    }

    @Override
    public ITriangleRenderer drawRightTriangleLeftFace(int x, int y, byte voxel) {
        return tri.drawRightTriangleLeftFace(x, y, f(voxel));
    }

    @Override
    public ITriangleRenderer drawRightTriangleRightFace(int x, int y, byte voxel) {
        return tri.drawRightTriangleRightFace(x, y, f(voxel));
    }
}