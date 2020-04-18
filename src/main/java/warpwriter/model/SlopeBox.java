package warpwriter.model;

import com.badlogic.gdx.graphics.Pixmap;
import warpwriter.Tools3D;
import warpwriter.view.render.VoxelPixmapRenderer;

import java.util.Arrays;

public class SlopeBox {
    public byte[][][][] data;

    public SlopeBox()
    {
        this(new byte[32][32][32]);
    }
    
    public SlopeBox(byte[][][] colors)
    {
        data = new byte[2][][][];
        data[0] = colors;
        data[1] = new byte[colors.length][colors[0].length][colors[0][0].length];
        Tools3D.fill(data[1], -1);
        putSlopes();
    }

    public int sizeX(){
        return data[0].length;
    }

    public int sizeY(){
        return data[0][0].length;
    }

    public int sizeZ(){
        return data[0][0][0].length;
    }

    public byte color(int x, int y, int z)
    {
        if((x|y|z) < 0 || x >= sizeX() || y >= sizeY() || z >= sizeZ())
            return 0;
        return data[0][x][y][z];
    }

    public byte slope(int x, int y, int z)
    {
        if((x|y|z) < 0 || x >= sizeX() || y >= sizeY() || z >= sizeZ() || data[0][x][y][z] == 0)
            return 0;
        return data[1][x][y][z];
    }

    public SlopeBox set(int x, int y, int z, int color, int slope)
    {
        if(!((x|y|z) < 0 || x >= sizeX() || y >= sizeY() || z >= sizeZ()))
        {
            data[0][x][y][z] = (byte)color;
            data[1][x][y][z] = (byte)slope;
        }
        return this;
    }

    public SlopeBox setColor(int x, int y, int z, int color)
    {
        if(!((x|y|z) < 0 || x >= sizeX() || y >= sizeY() || z >= sizeZ()))
        {
            data[0][x][y][z] = (byte)color;
            if(data[1][x][y][z] == 0) data[1][x][y][z] = -1;
        }
        return this;
    }

    public SlopeBox setSlope(int x, int y, int z, int slope)
    {
        if(!((x|y|z) < 0 || x >= sizeX() || y >= sizeY() || z >= sizeZ()) && data[0][x][y][z] != 0)
        {
            data[1][x][y][z] = (byte)slope;
        }
        return this;
    }
    
    public SlopeBox putSlopes(){
        final int limitX = sizeX() - 1;
        final int limitY = sizeY() - 1;
        final int limitZ = sizeZ() - 1;
        byte[][][] nextColors = new byte[limitX+1][limitY+1][limitZ+1];
        byte[][][] nextData = new byte[limitX+1][limitY+1][limitZ+1];
        final int[] neighbors = new int[6];
        for (int x = 0; x <= limitX; x++) {
            for (int y = 0; y <= limitY; y++) {
                PER_CELL:
                for (int z = 0; z <= limitZ; z++) {
                    if(data[0][x][y][z] == 0)
                    {
                        int slope = 0;
                        if((neighbors[0] = x == 0 ? 0 : (data[0][x-1][y][z] & 255)) != 0) slope      |= 0x55;
                        if((neighbors[1] = y == 0 ? 0 : (data[0][x][y-1][z] & 255)) != 0) slope      |= 0x33;
                        if((neighbors[2] = z == 0 ? 0 : (data[0][x][y][z-1] & 255)) != 0) slope      |= 0x0F;
                        if((neighbors[3] = x == limitX ? 0 : (data[0][x+1][y][z] & 255)) != 0) slope |= 0xAA;
                        if((neighbors[4] = y == limitY ? 0 : (data[0][x][y+1][z] & 255)) != 0) slope |= 0xCC;
                        if((neighbors[5] = z == limitZ ? 0 : (data[0][x][y][z+1] & 255)) != 0) slope |= 0xF0;
                        if(Integer.bitCount(slope) < 5) // surrounded by empty or next to only one voxel
                        {
                            nextData[x][y][z] = 0;
                            continue;
                        }
                        int bestIndex = -1;
                        for (int i = 0; i < 6; i++) {
                            if(neighbors[i] == 0) continue;
                            if(bestIndex == -1) bestIndex = i;
                            for (int j = 0; j < i; j++) {
                                if(neighbors[i] == neighbors[j]){
                                    if(i == bestIndex || j == bestIndex) {
                                        nextColors[x][y][z] = (byte) neighbors[bestIndex];
                                        nextData[x][y][z] = (byte) slope;
                                        continue PER_CELL;
                                    }
                                    else {
                                        bestIndex = i;
                                    }
                                }
                            }
                        }
                        nextColors[x][y][z] = (byte) neighbors[bestIndex];
                        nextData[x][y][z] = (byte) slope;
                    }
                    else
                    {
                        nextColors[x][y][z] = data[0][x][y][z];
                        nextData[x][y][z] = -1;
                    }
                }
            }
        }
        
        for (int x = 0; x <= limitX; x++) {
            for (int y = 0; y <= limitY; y++) {
                PER_CELL:
                for (int z = 0; z <= limitZ; z++) {
                    if(nextColors[x][y][z] == 0)
                    {
                        int slope = 0;
                        if((neighbors[0] = x == 0 ? 0 : (nextColors[x-1][y][z] & 255)) != 0 && (nextData[x-1][y][z] & 0xAA) != 0xAA) slope      |= (data[1][x-1][y][z] & 0xAA) >>> 1;
                        if((neighbors[1] = y == 0 ? 0 : (nextColors[x][y-1][z] & 255)) != 0 && (nextData[x][y-1][z] & 0xCC) != 0xCC) slope      |= (data[1][x][y-1][z] & 0xCC) >>> 2;
                        if((neighbors[2] = z == 0 ? 0 : (nextColors[x][y][z-1] & 255)) != 0 && (nextData[x][y][z-1] & 0xF0) != 0xF0) slope      |= (data[1][x][y][z-1] & 0xF0) >>> 4;
                        if((neighbors[3] = x == limitX ? 0 : (nextColors[x+1][y][z] & 255)) != 0 && (nextData[x+1][y][z] & 0x55) != 0x55) slope |= (data[1][x+1][y][z] & 0x55) >>> 1;
                        if((neighbors[4] = y == limitY ? 0 : (nextColors[x][y+1][z] & 255)) != 0 && (nextData[x][y+1][z] & 0x33) != 0x33) slope |= (data[1][x][y+1][z] & 0x33) >>> 2;
                        if((neighbors[5] = z == limitZ ? 0 : (nextColors[x][y][z+1] & 255)) != 0 && (nextData[x][y][z+1] & 0x0F) != 0x0F) slope |= (data[1][x][y][z+1] & 0x0F) >>> 4;
                        if(Integer.bitCount(slope) < 4) // surrounded by empty or only one partial face
                        {
                            data[1][x][y][z] = 0;
                            continue;
                        }
                        int bestIndex = -1;
                        for (int i = 0; i < 6; i++) {
                            if(neighbors[i] == 0) continue;
                            if(bestIndex == -1) bestIndex = i;
                            for (int j = 0; j < i; j++) {
                                if(neighbors[i] == neighbors[j]){
                                    if(i == bestIndex || j == bestIndex) {
                                        data[0][x][y][z] = (byte) neighbors[bestIndex];
                                        data[1][x][y][z] = (byte) slope;
                                        continue PER_CELL;
                                    }
                                    else {
                                        bestIndex = i;
                                    }
                                }
                            }
                        }
                        data[0][x][y][z] = (byte) neighbors[bestIndex];
                        data[1][x][y][z] = (byte) slope;
                    }
                    else
                    {
                        data[0][x][y][z] = nextColors[x][y][z];
                        data[1][x][y][z] = nextData[x][y][z];
                    }
                }
            }
        }
        return this;
    }
    
    public static final double[][] SHAPES = new double[256][16];
    static {
        Arrays.fill(SHAPES, new double[]{
                7.0,7.0,7.0,7.0,
                7.0,7.0,7.0,7.0,
                7.0,7.0,7.0,7.0,
                7.0,7.0,7.0,7.0});
        SHAPES[0x77] = new double[]{
                1.5,1.5,1.5,1.5,
                1.5,1.5,1.5,1.5,
                1.5,1.5,1.5,1.5,
                1.5,1.5,1.5,1.5};
        SHAPES[0x3F] = new double[]{
                7.3,7.3,2.6,2.6,
                7.3,2.6,2.6,2.6,
                7.3,2.6,2.6,1.0,
                2.6,2.6,2.6,1.0};
        SHAPES[0x5F] = new double[]{
                1.4,1.4,7.3,7.3,
                1.4,1.4,1.4,7.3,
                2.0,1.4,1.4,7.3,
                2.0,1.4,1.4,1.4};
        SHAPES[0xAF] = new double[]{
                7.3,7.3,7.3,7.3,
                2.0,2.0,1.0,1.0,
                2.0,2.0,1.0,1.0,
                2.0,2.0,1.0,1.0};
        SHAPES[0xCF] = new double[]{
                7.3,7.3,7.3,7.3,
                2.0,2.0,1.0,1.0,
                2.0,2.0,1.0,1.0,
                2.0,2.0,1.0,1.0};
        SHAPES[0xFF] = new double[]{
                3.0,3.0,3.0,3.0,
                2.0,2.0,1.0,1.0,
                2.0,2.0,1.0,1.0,
                2.0,2.0,1.0,1.0};
    }
    
    public static Pixmap drawIsoTri(SlopeBox seq, VoxelPixmapRenderer renderer) {
        // To move one x+ in voxels is x + 2, y - 2 in pixels.
        // To move one x- in voxels is x - 2, y + 2 in pixels.
        // To move one y+ in voxels is x + 2, y + 2 in pixels.
        // To move one y- in voxels is x - 2, y - 2 in pixels.
        // To move one z+ in voxels is y + 4 in pixels.
        // To move one z- in voxels is y - 4 in pixels.
        if(seq instanceof ITemporal) {
            renderer.color().time(((ITemporal) seq).frame());
        }
        final int sizeX = seq.sizeX(), sizeY = seq.sizeY(), sizeZ = seq.sizeZ(),
                pixelWidth = (Math.max(seq.sizeX(), Math.max(seq.sizeY(), seq.sizeZ()))) * 4 + 2,
                pixelHeight = (Math.max(seq.sizeX(), seq.sizeY()) * 2 + seq.sizeZ() + 1) * 2 + 1;
        for (int z = 0; z < sizeZ; z++) {
            for (int x = 0; x < sizeX; x++) {
                for (int y = 0; y < sizeY; y++) {
                    final byte v = seq.color(x, y, z);
                    if(v == 0) continue;
                    final int xPos = (sizeY - y + x) * 2 - 1,
                            yPos = (z + sizeX + sizeY - x - y) * 2 - 3,
                            dep = 3 * (x + y + z) + 256;
                    renderer.drawLeftTriangleLeftFace(xPos, yPos, v, dep, x, y, z);
                    renderer.drawRightTriangleLeftFace(xPos, yPos + 2, v, dep, x, y, z);
                    renderer.drawLeftTriangleRightFace(xPos + 2, yPos + 2, v, dep, x, y, z);
                    renderer.drawRightTriangleRightFace(xPos + 2, yPos, v, dep, x, y, z);
                    //if (z >= sizeZ - 1 || seq.getRotated(x, y, z + 1) == 0)
                    renderer.drawLeftTriangleVerticalFace(xPos, yPos + 4, v, dep, x, y, z);
                    renderer.drawRightTriangleVerticalFace(xPos + 2, yPos + 4, v, dep, x, y, z);
                }
            }
        }
        return renderer.blit(12, pixelWidth, pixelHeight);
    }

    public static Pixmap drawIso(SlopeBox seq, VoxelPixmapRenderer renderer) {
        // To move one x+ in voxels is x + 2, y - 2 in pixels.
        // To move one x- in voxels is x - 2, y + 2 in pixels.
        // To move one y+ in voxels is x + 2, y + 2 in pixels.
        // To move one y- in voxels is x - 2, y - 2 in pixels.
        // To move one z+ in voxels is y + 4 in pixels.
        // To move one z- in voxels is y - 4 in pixels.
        if(seq instanceof ITemporal) {
            renderer.color().time(((ITemporal) seq).frame());
        }
        final int sizeX = seq.sizeX(), sizeY = seq.sizeY(), sizeZ = seq.sizeZ(),
                pixelWidth = (Math.max(seq.sizeX(), Math.max(seq.sizeY(), seq.sizeZ()))) * 4 + 2,
                pixelHeight = (Math.max(seq.sizeX(), seq.sizeY()) + seq.sizeZ() * 3) + 2;
        for (int z = 0; z < sizeZ; z++) {
            for (int x = 0; x < sizeX; x++) {
                for (int y = 0; y < sizeY; y++) {
                    final byte v = seq.color(x, y, z);
                    if(v == 0) continue;
                    final int xPos = (sizeY - y + x) * 2 - 1,
                            yPos = (z * 3 + sizeX + sizeY - x - y) - 1,
                            dep = (x + y + z * 2) * 2 + 256;
                    renderer.select(xPos, yPos, v, SHAPES[seq.slope(x, y, z) & 255], dep);
                }
            }
        }
        return renderer.blit(12, pixelWidth, pixelHeight);
    }

}
