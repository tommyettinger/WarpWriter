package warpwriter.view;

import com.badlogic.gdx.graphics.Pixmap;
import squidpony.ArrayTools;

/**
 * Created by Tommy Ettinger on 12/16/2018.
 */
public class VoxelPixmapRenderer implements IRectangleRenderer {
    public Pixmap pixmap;
    public int[][] depths, working, render, outlines;
    public VoxelColor color;
    public boolean flipX, flipY, easing = true, outline = true;

    public VoxelPixmapRenderer() {
        this(new Pixmap(64, 64, Pixmap.Format.RGBA8888));
    }

    public VoxelPixmapRenderer(Pixmap pixmap) {
        this(pixmap, new VoxelColor());
    }

    public VoxelPixmapRenderer(Pixmap pixmap, VoxelColor color) {
        this.pixmap = pixmap;
        working = new int[pixmap.getWidth()][pixmap.getHeight()];
        render = new int[pixmap.getWidth()][pixmap.getHeight()];
        depths = new int[pixmap.getWidth()][pixmap.getHeight()];
        outlines = new int[pixmap.getWidth()][pixmap.getHeight()];
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

    public IRectangleRenderer rect(int x, int y, int sizeX, int sizeY, int color, int outlineColor, int depth) {
        //pixmap.setColor(color);
        //pixmap.fillRectangle(x, y, sizeX, sizeY);
        for (int i = 0; i < sizeX && x < working.length; i++, x++) {
            for (int j = 0, yy = y; j < sizeY && yy < working[0].length; j++, yy++) {
                working[x][yy] = color;
                depths[x][yy] = depth;
                outlines[x][yy] = outlineColor;
            }
        }
        return this;
    }

    @Override
    public IRectangleRenderer rectVertical(int x, int y, int sizeX, int sizeY, byte voxel) {
        return rect(x, y, sizeX, sizeY, color.verticalFace(voxel));
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
        return rect(x, y, sizeX, sizeY, color.verticalFace(voxel), color.twilight().dark(voxel), depth);
    }

    public IRectangleRenderer rectLeft(int x, int y, int sizeX, int sizeY, byte voxel, int depth) {
        return rect(x, y, sizeX, sizeY, color.leftFace(voxel), color.twilight().dark(voxel), depth);
    }

    public IRectangleRenderer rectRight(int x, int y, int sizeX, int sizeY, byte voxel, int depth) {
        return rect(x, y, sizeX, sizeY, color.rightFace(voxel), color.twilight().dark(voxel), depth);
    }

    public int getPixel(int x, int y) {
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

    public int depth(int x, int y) {
        return (x >= 0 && x < depths.length && y >= 0 && y < depths.length) ? depths[x][y] : 0;
    }

    public VoxelPixmapRenderer clear() {
        pixmap.setColor(0);
        pixmap.fill();
        ArrayTools.fill(working, 0);
        ArrayTools.fill(depths, 0);
        return this;
    }

    private int lightness(int color) {
        return (color >>> 24) + (color >>> 16 & 0xFF) + (color >>> 8 & 0xFF);
    }

    public Pixmap blit(int threshold, int pixelWidth, int pixelHeight) {
        pixmap.setColor(0);
        pixmap.fill();
        int xSize = Math.min(pixelWidth, working.length) - 1,
                ySize = Math.min(pixelHeight, working[0].length) - 1,
                depth;
        for (int x = 0; x <= xSize; x++) {
            System.arraycopy(working[x], 0, render[x], 0, ySize);
        }
        if(outline) {
            int o;
            for (int x = 1; x < xSize; x++) {
                for (int y = 1; y < ySize; y++) {
                    if ((o = outlines[x][y]) != 0) {
                        depth = depths[x][y];
                        if (outlines[x - 1][y] == 0 && outlines[x][y - 1] == 0) {
                            render[x - 1][y] = o;
                            render[x][y - 1] = o;
                            render[x][y] = o;
                        } else if (outlines[x + 1][y] == 0 && outlines[x][y - 1] == 0) {
                            render[x + 1][y] = o;
                            render[x][y - 1] = o;
                            render[x][y] = o;
                        } else if (outlines[x - 1][y] == 0 && outlines[x][y + 1] == 0) {
                            render[x - 1][y] = o;
                            render[x][y + 1] = o;
                            render[x][y] = o;
                        } else if (outlines[x + 1][y] == 0 && outlines[x][y + 1] == 0) {
                            render[x + 1][y] = o;
                            render[x][y + 1] = o;
                            render[x][y] = o;
                        } else {
                            if (outlines[x - 1][y] == 0 || depths[x - 1][y] < depth - threshold) {
                                render[x - 1][y] = o;
                            }
                            if (outlines[x + 1][y] == 0 || depths[x + 1][y] < depth - threshold) {
                                render[x + 1][y] = o;
                            }
                            if (outlines[x][y - 1] == 0 || depths[x][y - 1] < depth - threshold) {
                                render[x][y - 1] = o;
                            }
                            if (outlines[x][y + 1] == 0 || depths[x][y + 1] < depth - threshold) {
                                render[x][y + 1] = o;
                            }
                        }
                    }
                }
            }
        }
        
        final int pmh = pixmap.getHeight() - 1;
        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                pixmap.drawPixel(x, pmh - y, render[x][y]);
            }
        }

        if (easing) {
            int o, a, b, c, d, e, f, g, h;
            int lo;
            for (int x = 1; x < xSize; x++) {
                for (int y = 1; y < ySize; y++) {
                    o = render[x][y];
                    a = render[x - 1][y - 1];
                    b = render[x + 1][y - 1];
                    c = render[x - 1][y + 1];
                    d = render[x + 1][y + 1];
                    e = render[x - 1][y];
                    f = render[x + 1][y];
                    g = render[x][y - 1];
                    h = render[x][y + 1];
                    if (o != 0) {
                        if (a != 0 && b != 0 && c != 0 && d != 0 && e != 0 && f != 0 && g != 0 && h != 0 && (e != f || g != h)) {
                            lo = lightness(o);
                            if (a == d && lightness(a) > lo)
                                pixmap.drawPixel(x, pmh - y, a);
                            else if (b == c && lightness(b) > lo)
                                pixmap.drawPixel(x, pmh - y, b);
                            //else pixmap.drawPixel(x, y, color.twilight().twilight(o));
                        }
                    }
                    //else pixmap.drawPixel(x, y, color.twilight().twilight(o));
                }
            }
        }
        ArrayTools.fill(render, 0);
        ArrayTools.fill(working, 0);
        ArrayTools.fill(depths, 0);
        ArrayTools.fill(outlines, 0);
        return pixmap;
    }

}