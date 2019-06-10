package warpwriter.view.render;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;
import squidpony.squidmath.NumberTools;
import warpwriter.model.IVoxelSeq;
import warpwriter.model.nonvoxel.HashMap3D;
import warpwriter.model.nonvoxel.IntComparator;
import warpwriter.view.color.VoxelColor;

/**
 * Renders voxel models to a libGDX ImmediateModeRenderer20.
 *
 * @author Ben McLean
 */
public class VoxelSpotRenderer implements Disposable {

    public VoxelSpotRenderer(){
        this(640, 480);
    }
    public VoxelSpotRenderer(int width, int height) {
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

    public VoxelSpotRenderer set(VoxelColor color) {
        this.color = color;
        return this;
    }

    protected ImmediateModeRenderer20 batch;
    protected Matrix4 proj;

    public VoxelSpotRenderer set(ImmediateModeRenderer20 batch) {
        this.batch = batch;
        return this;
    }

    public ImmediateModeRenderer20 batch() {
        return batch;
    }

    protected int offsetX = 0, offsetY = 0;

    public VoxelSpotRenderer setOffset(int offsetX, int offsetY) {
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

    public VoxelSpotRenderer flipX() {
        flipX = !flipX;
        return this;
    }

    public VoxelSpotRenderer flipY() {
        flipY = !flipY;
        return this;
    }

    public boolean getFlipX() {
        return flipX;
    }

    public boolean getFlipY() {
        return flipY;
    }

    public VoxelSpotRenderer setFlipX(boolean flipX) {
        this.flipX = flipX;
        return this;
    }

    public VoxelSpotRenderer setFlipY(boolean flipY) {
        this.flipY = flipY;
        return this;
    }

    protected float scaleX = 0.5f, scaleY = 0.5f;

    public VoxelSpotRenderer multiplyScale(float multiplier) {
        return setScale((scaleX * multiplier), (scaleY * multiplier));
    }

    public VoxelSpotRenderer setScale(float scale) {
        return setScale(scale, scale);
    }

    public VoxelSpotRenderer setScale(float scaleX, float scaleY) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        return this;
    }

    public VoxelSpotRenderer spot(float x, float y, int color) {
        final float c = NumberTools.reversedIntBitsToFloat(transparency(color) & 0xFFFFFFFE);
        x = scaleX * (flipX ? -x : x) + offsetX;
        y = scaleY * (flipY ? -y : y) + offsetY;
        batch.color(c);
        batch.vertex(x, y, 0);
        return this;
    }

    public void drawAbove45(IVoxelSeq seq)
    {
        final int len = seq.size(), sizeX = seq.sizeX(), sizeY = seq.sizeY(), sizeZ = seq.sizeZ();
//                pixelWidth = (sizeY + sizeX + 2) * scaleX + 1, pixelHeight = (sizeX + sizeY + sizeZ + 3) * scaleY + 1;
        seq.sort(IntComparator.side45[seq.rotation()]);
        int xyz, x, y, z;
//        int dep;
        byte v;
        for (int i = 0; i < len; i++) {
            v = seq.getAtHollow(i);
            if (v != 0) {
                xyz = seq.keyAtRotated(i);
                x = HashMap3D.extractX(xyz);
                y = HashMap3D.extractY(xyz);
                z = HashMap3D.extractZ(xyz);
//                dep = 3 * (x + y + z) + 256;
                final float xPos = (sizeY - y + x) * scaleX + 1, yPos = (z - x - y + sizeX + sizeY) * scaleY + 1;
                spot(xPos, yPos, color.spotColor(v, x, y, z, seq));
            }
        }
    }

    protected byte transparency = Byte.MAX_VALUE;
    
    public final byte transparency() {
        return transparency;
    }

    public VoxelSpotRenderer setTransparency(byte transparency) {
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
        batch.begin(proj, GL20.GL_POINTS);
    }
    public void end()
    {
        batch.end();
    }
}
