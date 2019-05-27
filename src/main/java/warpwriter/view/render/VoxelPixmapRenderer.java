package warpwriter.view.render;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.IntIntMap;
import squidpony.ArrayTools;
import warpwriter.view.color.VoxelColor;

/**
 * Created by Tommy Ettinger on 12/16/2018.
 */
public class VoxelPixmapRenderer implements IRectangleRenderer, ITriangleRenderer {
    protected Pixmap pixmap;
    public int[][] depths, working, render, outlines;
    protected VoxelColor color = new VoxelColor();
    public boolean flipX, flipY, easing = true, outline = true;
    public int scaleX = 1, scaleY = 1;

    public VoxelPixmapRenderer multiplyScale(int multiplier) {
        return setScale(scaleX * multiplier, scaleY * multiplier);
    }

    public VoxelPixmapRenderer setScale(int scale) {
        return setScale(scale, scale);
    }

    public VoxelPixmapRenderer setScale(int scaleX, int scaleY) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        return this;
    }

    public Pixmap pixmap() {
        return pixmap;
    }

    public VoxelPixmapRenderer set(Pixmap pixmap) {
        this.pixmap = pixmap;
        working = new int[pixmap.getWidth()][pixmap.getHeight()];
        render = new int[pixmap.getWidth()][pixmap.getHeight()];
        depths = new int[pixmap.getWidth()][pixmap.getHeight()];
        outlines = new int[pixmap.getWidth()][pixmap.getHeight()];
        return this;
    }

    public VoxelColor voxelColor() {
        return color;
    }

    public VoxelPixmapRenderer set(VoxelColor color) {
        this.color = color;
        return this;
    }

    protected int offsetX = 0, offsetY = 0;

    public VoxelPixmapRenderer setOffset(int offsetX, int offsetY) {
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

    @Override
    public VoxelPixmapRenderer rect(int x, int y, int sizeX, int sizeY, int color) {
        pixmap.setColor(color);
        pixmap.fillRectangle(
                scaleX * (flipX ? -x : x) + offsetX,
                scaleY * (flipY ? -y : y) + offsetY,
                sizeX * scaleX,
                sizeY * scaleY
        );
        return this;
    }

    public VoxelPixmapRenderer rect(int x, int y, int sizeX, int sizeY, int color, int outlineColor, int depth) {
        //pixmap.setColor(color);
        //pixmap.fillRectangle(x, y, sizeX, sizeY);

        x = scaleX * (flipX ? -x : x) + offsetX;
        y = scaleY * (flipY ? -y : y) + offsetY;
        sizeX *= scaleX;
        sizeY *= scaleY;

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
    public VoxelPixmapRenderer rectVertical(int x, int y, int sizeX, int sizeY, byte voxel) {
        return rect(x, y, sizeX, sizeY, color.verticalFace(voxel));
    }

    @Override
    public VoxelPixmapRenderer rectLeft(int x, int y, int sizeX, int sizeY, byte voxel) {
        return rect(x, y, sizeX, sizeY,
                flipX ?
                        color.rightFace(voxel)
                        : color.leftFace(voxel)
        );
    }

    @Override
    public VoxelPixmapRenderer rectRight(int x, int y, int sizeX, int sizeY, byte voxel) {
        return rect(x, y, sizeX, sizeY,
                flipX ?
                        color.leftFace(voxel)
                        : color.rightFace(voxel)
        );
    }

    public VoxelPixmapRenderer rectVertical(int px, int py, int sizeX, int sizeY, byte voxel, int depth, int vx, int vy, int vz) {
        return rect(px, py, sizeX, sizeY, color.verticalFace(voxel, vx, vy, vz), color.twilight().dark(voxel), depth);
    }

    public VoxelPixmapRenderer rectLeft(int px, int py, int sizeX, int sizeY, byte voxel, int depth, int vx, int vy, int vz) {
        return rect(px, py, sizeX, sizeY, color.leftFace(voxel, vx, vy, vz), color.twilight().dark(voxel), depth);
    }

    public VoxelPixmapRenderer rectRight(int px, int py, int sizeX, int sizeY, byte voxel, int depth, int vx, int vy, int vz) {
        return rect(px, py, sizeX, sizeY, color.rightFace(voxel, vx, vy, vz), color.twilight().dark(voxel), depth);
    }

    public int getPixel(int x, int y) {
        return pixmap.getPixel(x * scaleX + offsetX, y * scaleY + offsetY);
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

    private static final IntIntMap counts = new IntIntMap(9);

    public Pixmap blit(int threshold, int pixelWidth, int pixelHeight) {
        pixmap.setColor(0);
        pixmap.fill();
        int xSize = Math.min(pixelWidth, working.length) - 1,
                ySize = Math.min(pixelHeight, working[0].length) - 1,
                depth;
        for (int x = 0; x <= xSize; x++) {
            System.arraycopy(working[x], 0, render[x], 0, ySize);
        }
        if (outline) {
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
                if (render[x][y] != 0) {
                    pixmap.setColor(render[x][y]);
                    pixmap.fillRectangle(x * scaleX + offsetX, pmh - (y * scaleY) + offsetY, scaleX, scaleY);
                }
            }
        }

        if (easing) {
            int o, a, b, c, d, e, f, g, h;
            int tgt, lo;
            for (int x = 1; x < xSize; x++) {
                for (int y = 1; y < ySize; y++) {
                    o = render[x][y];
                    if (o != 0) {
                        counts.clear();
                        //counts.put(o, 2);
                        counts.getAndIncrement(a = render[x - 1][y - 1], 0, 1);
                        counts.getAndIncrement(b = render[x + 1][y - 1], 0, 1);
                        counts.getAndIncrement(c = render[x - 1][y + 1], 0, 1);
                        counts.getAndIncrement(d = render[x + 1][y + 1], 0, 1);
                        counts.getAndIncrement(e = render[x - 1][y], 0, 1);
                        counts.getAndIncrement(f = render[x + 1][y], 0, 1);
                        counts.getAndIncrement(g = render[x][y - 1], 0, 1);
                        counts.getAndIncrement(h = render[x][y + 1], 0, 1);
                        if (a != 0 && b != 0 && c != 0 && d != 0 && e != 0 && f != 0 && g != 0 && h != 0) {
                            tgt = 0;
                            lo = lightness(o);
                            if (counts.get(a, 0) >= 4 && lightness(a) < lo)
                                tgt = a;
                            else if (counts.get(b, 0) >= 4 && lightness(b) < lo)
                                tgt = b;
                            else if (counts.get(c, 0) >= 4 && lightness(c) < lo)
                                tgt = c;
                            else if (counts.get(d, 0) >= 4 && lightness(d) < lo)
                                tgt = d;
                            if (tgt != 0) {
                                pixmap.setColor(tgt);
                                pixmap.fillRectangle(x * scaleX + offsetX, pmh - (y * scaleY) + offsetY, scaleX, scaleY);

//                                lt = lightness(tgt);
//                                if (a == d && lt < lo)
//                                    pixmap.drawPixel(x, pmh - y, a);
//                                else if (b == c && lb < lo)
//                                    pixmap.drawPixel(x, pmh - y, b);
                            }
                            //else pixmap.drawPixel(x, y, color.dimmer().medium(o));
                        }
                    }
                    //else pixmap.drawPixel(x, y, color.dimmer().medium(o));
                }
            }
        }
        ArrayTools.fill(render, 0);
        ArrayTools.fill(working, 0);
        ArrayTools.fill(depths, 0);
        ArrayTools.fill(outlines, 0);
        return pixmap;
    }

    @Override
    public VoxelPixmapRenderer drawLeftTriangle(int x, int y, int color) {
        pixmap.setColor(color);
        pixmap.fillRectangle((x + 1) * scaleX + offsetX, y * scaleY + offsetY, scaleX, scaleY * 3);
        pixmap.fillRectangle(x * scaleX + offsetX, (y + 1) * scaleY + offsetY * scaleY, scaleX, scaleY);
//        pixmap.fillTriangle(
//                x + 1 + offsetX, y + offsetY,
//                x + 1 + offsetX, y + 2 + offsetY,
//                x + offsetX, y + 1 + offsetY
//        );
        return this;
    }

    @Override
    public VoxelPixmapRenderer drawRightTriangle(int x, int y, int color) {
        pixmap.setColor(color);
        pixmap.fillRectangle(x * scaleX + offsetX, y * scaleY + offsetY, scaleX, scaleY * 3);
        pixmap.fillRectangle((x + 1) * scaleX + offsetX, (y + 1) * scaleY + offsetY, scaleX, scaleY);
//        pixmap.fillTriangle(
//                x + offsetX, y + offsetY,
//                x + offsetX, y + 2 + offsetY,
//                x + 1 + offsetX, y + 1 + offsetY
//                );
        return this;
    }


    @Override
    public VoxelPixmapRenderer drawLeftTriangleVerticalFace(int x, int y, byte voxel, int vx, int vy, int vz) {
        final int color = this.color.verticalFace(voxel, vx, vy, vz);
        return flipX ?
                drawRightTriangle(x, y, color)
                : drawLeftTriangle(x, y, color);
    }

    @Override
    public VoxelPixmapRenderer drawLeftTriangleLeftFace(int x, int y, byte voxel, int vx, int vy, int vz) {
        final int color = flipX ?
                this.color.rightFace(voxel, vx, vy, vz)
                : this.color.leftFace(voxel, vx, vy, vz);
        return flipX ?
                drawRightTriangle(x, y, color)
                : drawLeftTriangle(x, y, color);
    }

    @Override
    public VoxelPixmapRenderer drawLeftTriangleRightFace(int x, int y, byte voxel, int vx, int vy, int vz) {
        final int color = flipX ?
                this.color.leftFace(voxel, vx, vy, vz)
                : this.color.rightFace(voxel, vx, vy, vz);
        return flipX ?
                drawRightTriangle(x, y, color)
                : drawLeftTriangle(x, y, color);
    }

    @Override
    public VoxelPixmapRenderer drawRightTriangleVerticalFace(int x, int y, byte voxel, int vx, int vy, int vz) {
        final int color = this.color.verticalFace(voxel, vx, vy, vz);
        return flipX ?
                drawLeftTriangle(x, y, color)
                : drawRightTriangle(x, y, color);
    }

    @Override
    public VoxelPixmapRenderer drawRightTriangleLeftFace(int x, int y, byte voxel, int vx, int vy, int vz) {
        final int color = flipX ?
                this.color.rightFace(voxel, vx, vy, vz)
                : this.color.leftFace(voxel, vx, vy, vz);
        return flipX ?
                drawLeftTriangle(x, y, color)
                : drawRightTriangle(x, y, color);
    }

    @Override
    public VoxelPixmapRenderer drawRightTriangleRightFace(int x, int y, byte voxel, int vx, int vy, int vz) {
        final int color = flipX ?
                this.color.leftFace(voxel, vx, vy, vz)
                : this.color.rightFace(voxel, vx, vy, vz);
        return flipX ?
                drawLeftTriangle(x, y, color)
                : drawRightTriangle(x, y, color);
    }
    
    public VoxelPixmapRenderer drawLeftTriangle(int x, int y, int shown, byte voxel, int depth) {
        final int outline = color.twilight().dark(voxel);
        rect((x + 1) * scaleX + offsetX, y * scaleY + offsetY, scaleX, scaleY * 3, shown, outline, depth);
        return rect(x * scaleX + offsetX, (y + 1) * scaleY + offsetY * scaleY, scaleX, scaleY, shown, outline, depth);
    }

    public VoxelPixmapRenderer drawRightTriangle(int x, int y, int shown, byte voxel, int depth) {
        final int outline = color.twilight().dark(voxel);
        rect(x * scaleX + offsetX, y * scaleY + offsetY, scaleX, scaleY * 3, shown, outline, depth);
        return rect((x + 1) * scaleX + offsetX, (y + 1) * scaleY + offsetY, scaleX, scaleY, shown, outline, depth);
    }


    @Override
    public VoxelPixmapRenderer drawLeftTriangleVerticalFace(int x, int y, byte voxel, int depth, int vx, int vy, int vz) {
        final int color = this.color.verticalFace(voxel, vx, vy, vz);
        return flipX ?
                drawRightTriangle(x, y, color, voxel, depth)
                : drawLeftTriangle(x, y, color, voxel, depth);
    }

    @Override
    public VoxelPixmapRenderer drawLeftTriangleLeftFace(int x, int y, byte voxel, int depth, int vx, int vy, int vz) {
        final int color = flipX ?
                this.color.rightFace(voxel, vx, vy, vz)
                : this.color.leftFace(voxel, vx, vy, vz);
        return flipX ?
                drawRightTriangle(x, y, color, voxel, depth)
                : drawLeftTriangle(x, y, color, voxel, depth);
    }

    @Override
    public VoxelPixmapRenderer drawLeftTriangleRightFace(int x, int y, byte voxel, int depth, int vx, int vy, int vz) {
        final int color = flipX ?
                this.color.leftFace(voxel, vx, vy, vz)
                : this.color.rightFace(voxel, vx, vy, vz);
        return flipX ?
                drawRightTriangle(x, y, color, voxel, depth)
                : drawLeftTriangle(x, y, color, voxel, depth);
    }

    @Override
    public VoxelPixmapRenderer drawRightTriangleVerticalFace(int x, int y, byte voxel, int depth, int vx, int vy, int vz) {
        final int color = this.color.verticalFace(voxel, vx, vy, vz);
        return flipX ?
                drawLeftTriangle(x, y, color, voxel, depth)
                : drawRightTriangle(x, y, color, voxel, depth);
    }

    @Override
    public VoxelPixmapRenderer drawRightTriangleLeftFace(int x, int y, byte voxel, int depth, int vx, int vy, int vz) {
        final int color = flipX ?
                this.color.rightFace(voxel, vx, vy, vz)
                : this.color.leftFace(voxel, vx, vy, vz);
        return flipX ?
                drawLeftTriangle(x, y, color, voxel, depth)
                : drawRightTriangle(x, y, color, voxel, depth);
    }

    @Override
    public VoxelPixmapRenderer drawRightTriangleRightFace(int x, int y, byte voxel, int depth, int vx, int vy, int vz) {
        final int color = flipX ?
                this.color.leftFace(voxel, vx, vy, vz)
                : this.color.rightFace(voxel, vx, vy, vz);
        return flipX ?
                drawLeftTriangle(x, y, color, voxel, depth)
                : drawRightTriangle(x, y, color, voxel, depth);
    }
}