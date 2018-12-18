package warpwriter.view;

import com.badlogic.gdx.graphics.Pixmap;
import squidpony.ArrayTools;

/**
 * Created by Tommy Ettinger on 12/16/2018.
 */
public class VoxelPixmapRenderer implements IRectangleRenderer {
    public Pixmap pixmap;
    public int[][] depths;
    public byte[][] working;
    public VoxelColor color;
    public boolean flipX, flipY, easing = true;

    public VoxelPixmapRenderer()
    {
        this(new Pixmap(64, 64, Pixmap.Format.RGBA8888));
    }
    public VoxelPixmapRenderer(Pixmap pixmap) {
        this.pixmap = pixmap;
        working = new byte[pixmap.getWidth()][pixmap.getHeight()];
        depths = new int[pixmap.getWidth()][pixmap.getHeight()];
        color = new VoxelColor();
    }
    
    public VoxelPixmapRenderer(Pixmap pixmap, VoxelColor color) {
        this.pixmap = pixmap;
        working = new byte[pixmap.getWidth()][pixmap.getHeight()];
        depths = new int[pixmap.getWidth()][pixmap.getHeight()];
        this.color = color;
    }

    @Override
    public IRectangleRenderer rect(int x, int y, int sizeX, int sizeY, int color) {
        pixmap.setColor(color);
        pixmap.fillRectangle(x, y, sizeX, sizeY);
//        for (int i = 0; i < sizeX; i++, x++) {
//            for (int j = 0, yy = y; j < sizeY; j++, yy++) {
//                working[x][yy] = color;
//            }
//        }
        return this;
    }

    public IRectangleRenderer rect(int x, int y, int sizeX, int sizeY, byte color, int depth) {
        //pixmap.setColor(color);
        //pixmap.fillRectangle(x, y, sizeX, sizeY);
        for (int i = 0; i < sizeX && x < working.length; i++, x++) {
            for (int j = 0, yy = working[0].length - y - 1; j < sizeY && yy >= 0; j++, yy--) {
                working[x][yy] = color;
                depths[x][yy] = depth;
            }
        }
        return this;
    }

    @Override
    public IRectangleRenderer rectVertical(int x, int y, int sizeX, int sizeY, byte voxel) {
        return rect(x, y, sizeX, sizeY,
                flipY ?
                        color.bottomFace(voxel)
                        : color.topFace(voxel)
        );
    }

    @Override
    public IRectangleRenderer rectLeft(int x, int y, int sizeX, int sizeY, byte voxel) {
        return rect(x, y, sizeX, sizeY,
                flipX ?
                        color.rightFace(voxel)
                        : color.leftFace(voxel)
        );
    }

    @Override
    public IRectangleRenderer rectRight(int x, int y, int sizeX, int sizeY, byte voxel) {
        return rect(x, y, sizeX, sizeY,
                flipX ?
                        color.leftFace(voxel)
                        : color.rightFace(voxel)
        );
    }

    public IRectangleRenderer rectVertical(int x, int y, int sizeX, int sizeY, byte voxel, int depth) {
        return rect(x, y, sizeX, sizeY, Twilight.RAMPS[voxel & 255][0], depth);
    }

    public IRectangleRenderer rectLeft(int x, int y, int sizeX, int sizeY, byte voxel, int depth) {
        return rect(x, y, sizeX, sizeY, Twilight.RAMPS[voxel & 255][1], depth);
    }

    public IRectangleRenderer rectRight(int x, int y, int sizeX, int sizeY, byte voxel, int depth) {
        return rect(x, y, sizeX, sizeY, Twilight.RAMPS[voxel & 255][2], depth);
    }

    public int getPixel(int x, int y)
    {
        return pixmap.getPixel(x, y);
    }
    
    public VoxelPixmapRenderer flipX() {
        flipX = !flipX;
        return this;
    }

    public VoxelPixmapRenderer flipY() {
        flipY = !flipY;
        return this;
    }

    public boolean getFlipX() {
        return flipX;
    }

    public boolean getFlipY() {
        return flipY;
    }

    public VoxelPixmapRenderer setFlipX(boolean flipX) {
        this.flipX = flipX;
        return this;
    }

    public VoxelPixmapRenderer setFlipY(boolean flipY) {
        this.flipY = flipY;
        return this;
    }

    public VoxelColor color() {
        return color;
    }

    public int depth(int x, int y)
    {
        return (x >= 0 && x < depths.length && y >= 0 && y < depths.length) ? depths[x][y] : 0;
    }
    
    public VoxelPixmapRenderer clear()
    {
        pixmap.setColor(0);
        pixmap.fill();
        ArrayTools.fill(working, (byte) 0);
        ArrayTools.fill(depths, 0);
        return this;
    }

    private int lightness(int color)
    {
        return (color >>> 24) + (color >>> 16 & 0xFF) + (color >>> 8 & 0xFF);
    }
    public Pixmap blit(int threshold){
        pixmap.setColor(0);
        pixmap.fill();
        int xSize = working.length - 1, ySize = working[0].length - 1, dep;
        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                pixmap.drawPixel(x, y, Twilight.RAMP_VALUES[working[x][y] & 255][1]);
            }
        }
        byte w;
        for (int x = 1; x < xSize; x++) {
            for (int y = 1; y < ySize; y++) {
                if((w = Twilight.RAMPS[working[x][y] & 255][3]) != 0) {
                    int o = Twilight.RAMP_VALUES[w & 255][1];
                    dep = depths[x][y];
                    if      (working[x - 1][y] == 0 && working[x][y - 1] == 0) { pixmap.drawPixel(x - 1, y, o); pixmap.drawPixel(x, y - 1, o); pixmap.drawPixel(x, y, o); }
                    else if (working[x + 1][y] == 0 && working[x][y - 1] == 0) { pixmap.drawPixel(x + 1, y, o); pixmap.drawPixel(x, y - 1, o); pixmap.drawPixel(x, y, o); }
                    else if (working[x - 1][y] == 0 && working[x][y + 1] == 0) { pixmap.drawPixel(x - 1, y, o); pixmap.drawPixel(x, y + 1, o); pixmap.drawPixel(x, y, o); }
                    else if (working[x + 1][y] == 0 && working[x][y + 1] == 0) { pixmap.drawPixel(x + 1, y, o); pixmap.drawPixel(x, y + 1, o); pixmap.drawPixel(x, y, o); }
                    else {
                        if (working[x - 1][y] == 0 || depths[x - 1][y] < dep - threshold ) { pixmap.drawPixel(x - 1, y, o); }
                        if (working[x + 1][y] == 0 || depths[x + 1][y] < dep - threshold ) { pixmap.drawPixel(x + 1, y, o); }
                        if (working[x][y - 1] == 0 || depths[x][y - 1] < dep - threshold ) { pixmap.drawPixel(x, y - 1, o); }
                        if (working[x][y + 1] == 0 || depths[x][y + 1] < dep - threshold ) { pixmap.drawPixel(x, y + 1, o); }
                    }
                }
            }
        }

        if(!easing)
        {
            ArrayTools.fill(working, (byte) 0);
            ArrayTools.fill(depths, 0);
            return pixmap;
        }

        byte o, a, b, c, d;
        for (int x = 1; x < xSize; x++) {
            for (int y = 1; y < ySize; y++) {
                o = working[x][y];
                a = working[x - 1][y - 1];
                b = working[x + 1][y - 1];
                c = working[x - 1][y + 1];
                d = working[x + 1][y + 1];
                if (o != 0 && a != 0 && b != 0 && c != 0 && d != 0) {
                    if (a == d && lightness(a) > lightness(o))
                        pixmap.drawPixel(x, y, Twilight.RAMP_VALUES[a & 255][1]);
                    else if (b == c && lightness(b) > lightness(o))
                        pixmap.drawPixel(x, y, Twilight.RAMP_VALUES[b & 255][1]);
                    //else pixmap.drawPixel(x, y, color.twilight().twilight(o));
                }
                //else pixmap.drawPixel(x, y, color.twilight().twilight(o));
            }
        }
        ArrayTools.fill(working, (byte) 0);
        ArrayTools.fill(depths, 0);
        return pixmap;
    }

}
