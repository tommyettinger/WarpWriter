package warpwriter.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.math.Matrix4;
import warpwriter.model.IVoxelSeq;
import warpwriter.model.nonvoxel.HashMap3D;
import warpwriter.model.nonvoxel.IntComparator;
import warpwriter.view.color.VoxelColor;
import warpwriter.view.render.IRectangleRenderer;
import warpwriter.view.render.ITriangleRenderer;

/**
 * VoxelDraw contains the logic to render voxel models as 2D sprites from various perspectives with a high level of abstraction.
 *
 * @author Ben McLean
 */
public class SlopeDraw {

    public SlopeDraw(){
        this(640, 480);
    }
    public SlopeDraw(int width, int height) {
        batch = new ImmediateModeRenderer20(width * height * 10, false, true, 0);
        proj = new Matrix4();
        proj.setToOrtho2D(0, 0, width, height, -4096f, 4096f);
    }
    protected VoxelColor color = new VoxelColor();

    public VoxelColor color() {
        return color;
    }

    public SlopeDraw set(VoxelColor color) {
        this.color = color;
        return this;
    }

    protected ImmediateModeRenderer20 batch;
    protected Matrix4 proj;

    public SlopeDraw set(ImmediateModeRenderer20 batch) {
        this.batch = batch;
        return this;
    }

    public ImmediateModeRenderer20 batch() {
        return batch;
    }

    protected int offsetX = 0, offsetY = 0;

    public SlopeDraw setOffset(int offsetX, int offsetY) {
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

    public SlopeDraw flipX() {
        flipX = !flipX;
        return this;
    }

    public SlopeDraw flipY() {
        flipY = !flipY;
        return this;
    }

    public boolean getFlipX() {
        return flipX;
    }

    public boolean getFlipY() {
        return flipY;
    }

    public SlopeDraw setFlipX(boolean flipX) {
        this.flipX = flipX;
        return this;
    }

    public SlopeDraw setFlipY(boolean flipY) {
        this.flipY = flipY;
        return this;
    }

    protected int scaleX = 1, scaleY = 1;

    public SlopeDraw multiplyScale(float multiplier) {
        return setScale((int) (scaleX * multiplier), (int) (scaleY * multiplier));
    }

    public SlopeDraw setScale(int scale) {
        return setScale(scale, scale);
    }

    public SlopeDraw setScale(int scaleX, int scaleY) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        return this;
    }

    public void begin()
    {
        Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);

//        Gdx.gl.glEnable(GL20.GL_CULL_FACE);
//        Gdx.gl.glCullFace(GL20.GL_BACK);
//
        Gdx.gl.glDepthFunc(GL20.GL_LESS);

        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthMask(true);

        batch.begin(proj, GL20.GL_TRIANGLES);
    }
    public void end()
    {
        batch.end();
    }


    public static int isoHeight(IVoxelSeq model) {
        return (model.sizeX() - 1) * 2 +
                (model.sizeZ() - 1) * 4 +
                (model.sizeY() - 1) * 2;
    }

    public static int isoWidth(IVoxelSeq model) {
        final int sizeVX = model.sizeX(), sizeVY = model.sizeY();
        return (sizeVX + sizeVY) * 2 -
                ((sizeVX + sizeVY & 1) << 2); // if sizeVX + sizeVY is odd, this is 4, otherwise it is 0
    }

    public static void draw(IVoxelSeq seq, IRectangleRenderer renderer)
    {
        draw(seq, renderer, 3, 3);
    }
    public static void draw(IVoxelSeq seq, IRectangleRenderer renderer, int scaleX, int scaleY) // scaleX 3, scaleY 3
    {
        final int len = seq.size(), sizeX = seq.sizeX(), sizeY = seq.sizeY(), sizeZ = seq.sizeZ(),
                offsetPX = (sizeY - 1 >> 1) + 1;
//                pixelWidth = sizeY * scaleX + (sizeY - 1 >> 1) + 3, pixelHeight = sizeZ * scaleY + 4;
        seq.sort(IntComparator.side[seq.rotation()]);
        int xyz, x, y, z;
        byte v;
        for (int i = 0; i < len; i++) {
            v = seq.getAtHollow(i);
            if (v != 0) {
                xyz = seq.keyAtRotatedHollow(i);
                x = HashMap3D.extractX(xyz);
                y = HashMap3D.extractY(xyz);
                z = HashMap3D.extractZ(xyz);
                final int xPos = (sizeY - y) * 3 + offsetPX;
                renderer.rectRight(xPos, z * scaleY, scaleX, scaleY, v, x*3, x, y, z);//, x * 2, x, y, z
                if (z >= sizeZ - 1 || seq.getRotated(x, y, z + 1) == 0)
                    renderer.rectVertical(xPos, (z+1) * scaleY, scaleX, 1, v, x*3, x, y, z);//, x * 2, x, y, z
            }
        }
    }
    public static void draw45(IVoxelSeq seq, IRectangleRenderer renderer)
    {
        draw45(seq, renderer, 2, 3);
    }
    public static void draw45(IVoxelSeq seq, IRectangleRenderer renderer, int scaleX, int scaleY) // scaleX 2, scaleY 3
    {
        final int len = seq.size(), sizeX = seq.sizeX(), sizeY = seq.sizeY(), sizeZ = seq.sizeZ();
//                pixelWidth = (sizeX + sizeY) * scaleX + 3, pixelHeight = sizeZ * scaleY + 4;
        int dep;
        seq.sort(IntComparator.side45[seq.rotation()]);
        int xyz, x, y, z;
        byte v;
        for (int i = 0; i < len; i++) {
            v = seq.getAtHollow(i);
            if (v != 0) {
                xyz = seq.keyAtRotatedHollow(i);
                x = HashMap3D.extractX(xyz);
                y = HashMap3D.extractY(xyz);
                z = HashMap3D.extractZ(xyz);
                final int xPos = (sizeY + x - y) * scaleX + 1;
                dep = 50 + (x + y) * 2;
                renderer.rectLeft(xPos, z * scaleY, scaleX, scaleY, v, dep, x, y, z);
                renderer.rectRight(xPos + scaleX, z * scaleY, scaleX, scaleY, v, dep, x, y, z);
                if (z >= sizeZ - 1 || seq.getRotated(x, y, z + 1) == 0)
                    renderer.rectVertical(xPos, (z+1) * scaleY, scaleX * 2, 1, v, dep, x, y, z);
            }
        }
    }
    public static void drawAbove(IVoxelSeq seq, IRectangleRenderer renderer)
    {
        drawAbove(seq, renderer, 1, 1);
    }
    public static void drawAbove(IVoxelSeq seq, IRectangleRenderer renderer, int scaleX, int scaleY) // scaleX 1, scaleY 1
    {
        final int len = seq.size(), sizeX = seq.sizeX(), sizeY = seq.sizeY(), sizeZ = seq.sizeZ(),
                offsetPX = (sizeY * scaleX >> 1) + 1, offsetPY = (sizeX * scaleY >> 1) + 1;
//                pixelWidth = (sizeY * 3) + (sizeY >> 1) + 6, pixelHeight = sizeZ * 2 + sizeX * 3 + (sizeX >> 1) + 8;
        seq.sort(IntComparator.side[seq.rotation()]);
        int dep;
        int xyz, x, y, z;
        byte v;
        for (int i = 0; i < len; i++) {
            v = seq.getAtHollow(i);
            if (v != 0) {
                xyz = seq.keyAtRotatedHollow(i);
                x = HashMap3D.extractX(xyz);
                y = HashMap3D.extractY(xyz);
                z = HashMap3D.extractZ(xyz);
                dep = 1500 + z * 8 + x * 5;
                final int xPos = (sizeY - y) * 3 * scaleX + offsetPX, yPos = z * 2 * scaleY + (sizeX - x) * 3 * scaleY + offsetPY;
                renderer.rectRight(xPos, yPos, 3 * scaleX, 2 * scaleY, v, dep, x, y, z);
                //if (z >= sizeZ - 1 || seq.getRotated(x, y, z + 1) == 0)
                renderer.rectVertical(xPos, yPos + 2 * scaleY, 3 * scaleX, 3 * scaleY, v, dep, x, y, z);
            }
        }
    }
    public static void drawAbove45(IVoxelSeq seq, IRectangleRenderer renderer)
    {
        drawAbove45(seq, renderer, 2, 2);
    }

    public static void drawAbove45(IVoxelSeq seq, IRectangleRenderer renderer, int scaleX, int scaleY) // scaleX 2, scaleY 2
    {
        final int len = seq.size(), sizeX = seq.sizeX(), sizeY = seq.sizeY(), sizeZ = seq.sizeZ();
//                pixelWidth = (sizeY + sizeX + 2) * scaleX + 1, pixelHeight = (sizeX + sizeY + sizeZ + 3) * scaleY + 1;
        seq.sort(IntComparator.side45[seq.rotation()]);
        int xyz, x, y, z;
        int dep;
        byte v;
        for (int i = 0; i < len; i++) {
            v = seq.getAtHollow(i);
            if (v != 0) {
                xyz = seq.keyAtRotatedHollow(i);
                x = HashMap3D.extractX(xyz);
                y = HashMap3D.extractY(xyz);
                z = HashMap3D.extractZ(xyz);
                dep = (x + y + z) * 3;
                final int xPos = (sizeY - y + x) * scaleX + 1, yPos = (z - x - y + sizeX + sizeY) * scaleY + 1;
                renderer.rectLeft(xPos, yPos, scaleX, scaleY, v, dep, x, y, z);
                renderer.rectRight(xPos + scaleX, yPos, scaleX, scaleY, v, dep, x, y, z);
                //if (z >= sizeZ - 1 || seq.getRotated(x, y, z + 1) == 0)
                renderer.rectVertical(xPos, yPos + scaleY, 2 * scaleX, scaleY, v, dep, x, y, z);
            }
        }
    }

    public static void drawIso(IVoxelSeq seq, ITriangleRenderer renderer) {
        // To move one x+ in voxels is x + 2, y - 2 in pixels.
        // To move one x- in voxels is x - 2, y + 2 in pixels.
        // To move one y+ in voxels is x + 2, y + 2 in pixels.
        // To move one y- in voxels is x - 2, y - 2 in pixels.
        // To move one z+ in voxels is y + 4 in pixels.
        // To move one z- in voxels is y - 4 in pixels.
        final int len = seq.size(), sizeX = seq.sizeX(), sizeY = seq.sizeY(), sizeZ = seq.sizeZ();
//                pixelWidth = (sizeY + sizeX + 2) * scaleX + 1, pixelHeight = (sizeX + sizeY + sizeZ + 3) * scaleY + 1;
        seq.sort(IntComparator.side45[seq.rotation()]);
        for (int i = 0; i < len; i++) {
            final byte v = seq.getAtHollow(i);
            if (v != 0) {
				final int xyz = seq.keyAtRotatedHollow(i),
						x = HashMap3D.extractX(xyz),
						y = HashMap3D.extractY(xyz),
						z = HashMap3D.extractZ(xyz),
						xPos = (sizeY - y + x) * 2 + 1,
						yPos = (z + sizeX + sizeY - x - y) * 2 + 1;
//                        dep = 3 * (x + y + z) + 256;					
                renderer.drawTriangleOutline(xPos, yPos, v, x, y, z);
				renderer.drawLeftTriangleRightFace(xPos + 2, yPos + 2, v, x, y, z);
				renderer.drawRightTriangleRightFace(xPos + 2, yPos, v, x, y, z);
				renderer.drawLeftTriangleLeftFace(xPos, yPos, v, x, y, z);
				renderer.drawRightTriangleLeftFace(xPos, yPos + 2, v, x, y, z);
				if (z >= sizeZ - 1 || seq.getRotated(x, y, z + 1) == 0) {
					renderer.drawLeftTriangleVerticalFace(xPos, yPos + 4, v, x, y, z);
					renderer.drawRightTriangleVerticalFace(xPos + 2, yPos + 4, v, x, y, z);
				}
			}
        }
    }
}
