package warpwriter.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.math.Matrix4;
import squidpony.squidmath.IntVLA;
import warpwriter.model.IVoxelSeq;
import warpwriter.model.nonvoxel.HashMap3D;
import warpwriter.model.nonvoxel.IntComparator;
import warpwriter.model.nonvoxel.IntSort;
import warpwriter.view.color.VoxelColor;
import warpwriter.view.render.IRectangleRenderer;

/**
 * SlopeDraw is a mad-science experiment in rendering voxels as the vertices for triangles that don't necessarily align
 * to right-angle faces (they can be slopes!).
 *
 * @author Tommy Ettinger
 */
public class SlopeDraw {
    public final int[] isoAdjacent = new int[]{
                                     HashMap3D.fuse(1, 0, 0), 
            HashMap3D.fuse(0, 1, 0), HashMap3D.fuse(1, 1, 0), HashMap3D.fuse(2, 1, 0),
                                     HashMap3D.fuse(1, 2, 0), 

            HashMap3D.fuse(0, 0, 1), HashMap3D.fuse(1, 0, 1), HashMap3D.fuse(2, 0, 1),
            HashMap3D.fuse(0, 1, 1),                          HashMap3D.fuse(2, 1, 1),
            HashMap3D.fuse(0, 2, 1), HashMap3D.fuse(1, 2, 1), HashMap3D.fuse(2, 2, 1),

                                     HashMap3D.fuse(1, 0, 2), 
            HashMap3D.fuse(0, 1, 2), HashMap3D.fuse(1, 1, 2), HashMap3D.fuse(2, 1, 2),
                                     HashMap3D.fuse(1, 2, 2), 
    };

    public final IntVLA isoOrder = IntVLA.with(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17);
    
    public final int[][] isoPoints = new int[18][3];
    
    public final IntVLA[] isoRefs = new IntVLA[18];
    
    public SlopeDraw(){
        this(640, 480);
    }
    public SlopeDraw(int width, int height) {
        batch = new ImmediateModeRenderer20(width * height * 9, false, true, 0);
        proj = new Matrix4();
        proj.setToOrtho2D(0, 0, width, height, -4096f, 4096f);
        for (int i = 0; i < 18; i++) {
            isoRefs[i] = new IntVLA(16);
        }
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

    public void drawIso(IVoxelSeq seq) {
        // To move one x+ in voxels is x + 2, y - 2 in pixels.
        // To move one x- in voxels is x - 2, y + 2 in pixels.
        // To move one y+ in voxels is x + 2, y + 2 in pixels.
        // To move one y- in voxels is x - 2, y - 2 in pixels.
        // To move one z+ in voxels is y + 4 in pixels.
        // To move one z- in voxels is y - 4 in pixels.
        final int len = seq.size(), sizeX = seq.sizeX(), sizeY = seq.sizeY(), sizeZ = seq.sizeZ();
        final VoxelColor vc = color;
//                pixelWidth = (sizeY + sizeX + 2) * scaleX + 1, pixelHeight = (sizeX + sizeY + sizeZ + 3) * scaleY + 1;
        final IntComparator comp = IntComparator.side45[seq.rotation()];
        seq.sort(comp);
        IntSort.sort(isoAdjacent, isoOrder, comp);
        for (int i = 0; i < 18; i++) {
            final int xyz = isoAdjacent[isoOrder.get(i)];
            isoRefs[i].clear();
            final int x = isoPoints[i][0] = HashMap3D.extractX(xyz) - 1;
            final int y = isoPoints[i][1] = HashMap3D.extractY(xyz) - 1;
            final int z = isoPoints[i][2] = HashMap3D.extractZ(xyz) - 1;
            for (int j = 0; j < i; j++) {
                if(x != -isoPoints[j][0] && y != -isoPoints[j][1] && z != -isoPoints[j][2]) 
                    isoRefs[j].add(i);
            }
        }
        byte color1, color2;
        int x1, y1, z1, x2, y2, z2, xPos1, yPos1;
        for (int i = 0; i < len; i++) {
            final byte color0 = seq.getAtHollow(i);
            if (color0 != 0) {
				final int xyz = seq.keyAtRotatedHollow(i),
						x = HashMap3D.extractX(xyz),
						y = HashMap3D.extractY(xyz),
						z = HashMap3D.extractZ(xyz),
						xPos = (sizeY - y + x) * 2 + 1,
						yPos = (z + sizeX + sizeY - x - y) * 2 + 1;
//                        dep = 3 * (x + y + z) + 256;
                
                for (int j = 0; j < 17; j++) {
                    if((color1 = seq.getRotated(x1 = x + isoPoints[j][0], y1 = y + isoPoints[j][1], z1 = z + isoPoints[j][2])) != 0) {
                        xPos1 = (sizeY - y1 + x1) * 2 + 1;
                        yPos1 = (z1 + sizeX + sizeY - x1 - y1) * 2 + 1;
                        final int innerLength = isoRefs[j].size;
                        for (int k = 0; k < innerLength; k++) {
                            final int r = isoRefs[j].get(k);
                            if((color2 = seq.getRotated(x2 = x + isoPoints[r][0], y2 = y + isoPoints[r][1], z2 = z + isoPoints[r][2])) != 0) {
                                batch.color(vc.leftFace(color0, xPos, yPos, x + y + z));
                                batch.vertex(x, y, z);
                                batch.color(vc.leftFace(color1, xPos1, yPos1, x1 + y1 + z1));
                                batch.vertex(x1, y1, z1);
                                batch.color(vc.leftFace(color2, (sizeY - y2 + x2) * 2 + 1, (z2 + sizeX + sizeY - x2 - y2) * 2 + 1, x2 + y2 + z2));
                                batch.vertex(x2, y2, z2);
                            }
                        }
                    }
                }
			}
        }
    }
}
