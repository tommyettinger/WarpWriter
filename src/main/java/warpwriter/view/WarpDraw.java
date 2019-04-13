package warpwriter.view;

import com.badlogic.gdx.graphics.Pixmap;
import warpwriter.Coloring;
import warpwriter.model.IModel;
import warpwriter.model.ITemporal;
import warpwriter.model.IVoxelSeq;
import warpwriter.model.nonvoxel.HashMap3D;
import warpwriter.model.nonvoxel.IntComparator;
import warpwriter.view.color.IVoxelColor;
import warpwriter.view.color.VoxelColor;
import warpwriter.view.render.IRectangleRenderer;
import warpwriter.view.render.VoxelPixmapRenderer;

/**
 * Created by Tommy Ettinger on 11/19/2018.
 */
public class WarpDraw {
    protected static IVoxelColor color = new VoxelColor();

//    public static void draw(VoxelModel model, IRectangleRenderer renderer) {
//        draw(model, renderer, color);
//    }
//
//    public static void draw(VoxelModel model, IRectangleRenderer renderer, IVoxelColor color) {
//        final int sizeX = model.sizeX(), sizeY = model.sizeY(), sizeZ = model.sizeZ(),
//                startX = model.startX(), startY = model.startY(), startZ = model.startZ(),
//                stepX = model.stepX(), stepY = model.stepY(), stepZ = model.stepZ(),
//                sy = model.sizes()[1], sz = model.sizes()[2],
//                rx = model.rotation()[0] ^ model.rotation()[0] >> 31,
//                ry = model.rotation()[1] ^ model.rotation()[1] >> 31,
//                rz = model.rotation()[2] ^ model.rotation()[2] >> 31;
//        int py, px, x, y, z;
//        for (py = 0, z = startZ; z < sizeZ && z >= 0; z += stepZ, py += 3) {
//            model.temp[rz] = z;
//            for (px = 0, y = startY; y < sizeY && y >= 0; y += stepY, px += 3) {
//                model.temp[ry] = y;
//                for (x = startX; x < sizeX && x >= 0; x += stepX) {
//                    model.temp[rx] = x;
//                    // uncomment to show background in red
//                    //renderer.drawPixel(px, py, Color.rgba8888(Color.RED));
//                    byte result = model.voxels[sz * (sy * model.temp[0] + model.temp[1]) + model.temp[2]];
//                    if (result != 0 && result != Coloring.CLEAR) {
//                        renderer.rect(px, py + 3, 3, 1, color.verticalFace(result));
//                        renderer.rect(px, py, 3, 3, color.rightFace(result));
//                        break;
//                    }
//                }
//            }
//        }
//    }
//
//    public static void drawOutline(VoxelModel model, IRectangleRenderer renderer) {
//        final int sizeX = model.sizeX(), sizeY = model.sizeY(), sizeZ = model.sizeZ(),
//                startX = model.startX(), startY = model.startY(), startZ = model.startZ(),
//                stepX = model.stepX(), stepY = model.stepY(), stepZ = model.stepZ(),
//                sy = model.sizes()[1], sz = model.sizes()[2],
//                rx = model.rotation()[0] ^ model.rotation()[0] >> 31,
//                ry = model.rotation()[1] ^ model.rotation()[1] >> 31,
//                rz = model.rotation()[2] ^ model.rotation()[2] >> 31,
//                outline = Coloring.RINSED[Coloring.OUTLINE];
//        int py, px, x, y, z;
//        for (py = -1, z = startZ; z < sizeZ && z >= 0; z += stepZ, py += 3) {
//            model.temp[rz] = z;
//            for (px = -1, y = startY; y < sizeY && y >= 0; y += stepY, px += 3) {
//                model.temp[ry] = y;
//                for (x = startX; x < sizeX && x >= 0; x += stepX) {
//                    model.temp[rx] = x;
//                    // uncomment to show background in red
//                    //renderer.drawPixel(px, py, Color.rgba8888(Color.RED));
//                    byte result = model.voxels[sz * (sy * model.temp[0] + model.temp[1]) + model.temp[2]];
//                    if (result != 0 && result != Coloring.CLEAR) {
//                        renderer.rect(px, py, 5, 6, outline);
//                        break;
//                    }
//                }
//            }
//        }
//    }

    public static void simpleDraw(IModel model, IRectangleRenderer renderer) {
        simpleDraw(model, renderer, color, true);
    }

    public static void simpleDraw(IModel model, IRectangleRenderer renderer, IVoxelColor color) {
        simpleDraw(model, renderer, color, true);
    }

    public static void simpleDraw(IModel model, IRectangleRenderer renderer, IVoxelColor color, boolean outline) {
        final int sizeX = model.sizeX(), sizeY = model.sizeY(), sizeZ = model.sizeZ();
        if (outline) {
            final int outlineColor = Coloring.RINSED[Coloring.OUTLINE];
            for (int py = 0, z = 0; z < sizeZ; z++, py += 3) {
                for (int px = 0, y = 0; y < sizeY; y++, px += 3) {
                    for (int x = 0; x < sizeX; x++) {
                        byte result = model.at(x, y, z);
                        if (result != 0 && result != Coloring.CLEAR) {
                            renderer.rect(px, py, 5, 6, outlineColor);
                            break;
                        }
                    }
                }
            }
        }
        for (int py = 1, z = 0; z < sizeZ; z++, py += 3) {
            for (int px = 1, y = 0; y < sizeY; y++, px += 3) {
//                renderer.drawPixel(y, z, Color.rgba8888(Color.RED));
                for (int x = 0; x < sizeX; x++) {
                    byte result = model.at(x, y, z);
                    if (result != 0 && result != Coloring.CLEAR) {
                        renderer.rect(px, py + 3, 3, 1, color.verticalFace(result));
                        renderer.rect(px, py, 3, 3, color.rightFace(result));
                        break;
                    }
                }
            }
        }
    }

    public static void simpleDraw45(IModel model, IRectangleRenderer renderer) {
        simpleDraw45(model, renderer, color, true);
    }

    public static void simpleDraw45(IModel model, IRectangleRenderer renderer, IVoxelColor color) {
        simpleDraw45(model, renderer, color, true);
    }

    public static void simpleDraw45(IModel model, IRectangleRenderer renderer, IVoxelColor color, boolean outline) {
        final int sizeX = model.sizeX(),
                sizeY = model.sizeY(),
                sizeZ = model.sizeZ(),
                rayWidth = sizeX + sizeY;
        byte result;
        if (outline) {
            final int outlineColor = Coloring.RINSED[Coloring.OUTLINE];
            for (int py = 0, ry = 0; ry < sizeZ; ry++, py += 3) {
                // ry is the number of rays cast on the screen-y axis, py is the pixel-y position
                for (int px = 0, rx = 0; rx <= rayWidth; rx += 2, px += 4) {
                    // rx is the number of rays cast on the screen-x axis, px is the pixel-x position

                    boolean leftDone = false, rightDone = rayWidth - rx < 2;
                    final int startX = rx > sizeX - 1 ? 0 : sizeX - rx - 1,
                            startY = rx - sizeX + 1 < 0 ? 0 : rx - sizeX + 1;
                    for (int vx = startX, vy = startY;
                         vx <= sizeX && vy <= sizeY;
                         vx++, vy++) { // vx is voxel x, vy is voxel y
                        if (!leftDone && vy != 0) {
                            result = model.at(vx, vy - 1, ry);
                            if (result != 0 && result != Coloring.CLEAR) {
                                renderer.rect(px, py, 4, 6, outlineColor);
                                leftDone = true;
                            }
                        }
                        if (!rightDone && vx > 0) {
                            result = model.at(vx - 1, vy, ry);
                            if (result != 0 && result != Coloring.CLEAR) {
                                renderer.rect(px + 2, py, 4, 6, outlineColor);
                                rightDone = true;
                            }
                        }
                        if (leftDone && rightDone) break;
                        result = model.at(vx, vy, ry);
                        if (result != 0 && result != Coloring.CLEAR) {
                            renderer.rect(px, py, 6, 6, outlineColor);
                            break;
                        }
                    }
                }
            }
        }
        for (int py = 1, ry = 0; ry < sizeZ; ry++, py += 3) {
            // ry is the number of rays cast on the screen-y axis, py is the pixel-y position
            for (int px = 1, rx = 0; rx <= rayWidth; rx += 2, px += 4) {
                // rx is the number of rays cast on the screen-x axis, px is the pixel-x position
//                renderer.rect(px, py, 2, 1, Color.rgba8888(Color.RED));
                boolean leftDone = false, rightDone = rayWidth - rx < 2;
                final int startX = rx > sizeX - 1 ? 0 : sizeX - rx - 1,
                        startY = rx - sizeX + 1 < 0 ? 0 : rx - sizeX + 1;
                for (int vx = startX, vy = startY;
                     vx <= sizeX && vy <= sizeY;
                     vx++, vy++) { // vx is voxel x, vy is voxel y
                    if (!leftDone && vy != 0) {
                        result = model.at(vx, vy - 1, ry);
                        if (result != 0 && result != Coloring.CLEAR) {
                            renderer.rect(px, py + 3, 2, 1, color.verticalFace(result));
                            renderer.rect(px, py, 2, 3, color.rightFace(result));
                            leftDone = true;
                        }
                    }
                    if (!rightDone && vx > 0) {
                        result = model.at(vx - 1, vy, ry);
                        if (result != 0 && result != Coloring.CLEAR) {
                            renderer.rect(px + 2, py + 3, 2, 1, color.verticalFace(result));
                            renderer.rect(px + 2, py, 2, 3, color.leftFace(result));
                            rightDone = true;
                        }
                    }
                    if (leftDone && rightDone) break;
                    result = model.at(vx, vy, ry);
                    if (result != 0 && result != Coloring.CLEAR) {
                        if (!leftDone && !rightDone) {
                            renderer.rect(px, py + 3, 4, 1, color.verticalFace(result));
                            renderer.rect(px, py, 2, 3, color.leftFace(result));
                            renderer.rect(px + 2, py, 2, 3, color.rightFace(result));
                        } else {
                            if (!leftDone) {
                                renderer.rect(px, py + 3, 2, 1, color.verticalFace(result));
                                renderer.rect(px, py, 2, 3, color.leftFace(result));
                            }
                            if (!rightDone) {
                                renderer.rect(px + 2, py + 3, 2, 1, color.verticalFace(result));
                                renderer.rect(px + 2, py, 2, 3, color.rightFace(result));
                            }
                        }
                        break;
                    }
                }
            }
        }
    }


//    public static void draw45(VoxelModel model, IRectangleRenderer renderer) {
//        draw45(model, renderer, color);
//    }
//
//    public static void draw45(VoxelModel model, IRectangleRenderer renderer, IVoxelColor color) {
//        final int sizeX = model.sizeX(), sizeY = model.sizeY(), sizeZ = model.sizeZ(),
//                stepX = model.stepX(), stepY = model.stepY(), stepZ = model.stepZ(),
//                sy = model.sizes()[1], sz = model.sizes()[2],
//                rx = model.rotation()[0] ^ model.rotation()[0] >> 31,
//                ry = model.rotation()[1] ^ model.rotation()[1] >> 31,
//                rz = model.rotation()[2] ^ model.rotation()[2] >> 31,
//                pixelWidth = sizeX + sizeY,
//                startZ = model.startZ();
//        byte result;
//        for (int py = 0, vz = startZ; vz < sizeZ && vz >= 0; vz += stepZ, py += 3) {
//            model.temp[rz] = vz;
//            for (int px = 0; px <= pixelWidth; px += 2) { // pixel x
////                renderer.rect(px, py, 2, 1, Color.rgba8888(Color.RED));
//                boolean leftDone = false, rightDone = pixelWidth - px < 2;
//                final int startX = px > sizeX - 1 ? 0 : sizeX - px - 1,
//                        startY = px - sizeX + 1 < 0 ? 0 : px - sizeX + 1;
//                for (int vx = startX, vy = startY;
//                     vx <= sizeX && vy <= sizeY && vx >= 0 && vy >= 0;
//                     vx += stepX, vy += stepY) { // vx is voxel x, vy is voxel y
//                    if (!leftDone && vy > 0 && vx < sizeX) {
//                        model.temp[ry] = vy - 1;
//                        model.temp[rx] = vx;
//                        result = model.voxels[sz * (sy * model.temp[0] + model.temp[1]) + model.temp[2]];
//                        if (result != 0 && result != Coloring.CLEAR) {
//                            renderer.rect(px << 1, py + 3, 2, 1, color.verticalFace(result));
//                            renderer.rect(px << 1, py, 2, 3, color.rightFace(result));
//                            leftDone = true;
//                        }
//                    }
//                    if (!rightDone && vx > 0 && vy < sizeY) {
//                        model.temp[ry] = vy;
//                        model.temp[rx] = vx - 1;
//                        result = model.voxels[sz * (sy * model.temp[0] + model.temp[1]) + model.temp[2]];
//                        if (result != 0 && result != Coloring.CLEAR) {
//                            renderer.rect((px + 1) << 1, py + 3, 2, 1, color.verticalFace(result));
//                            renderer.rect((px + 1) << 1, py, 2, 3, color.leftFace(result));
//                            rightDone = true;
//                        }
//                    }
//                    if ((leftDone && rightDone) || vx >= sizeX || vy >= sizeY) break;
//                    model.temp[ry] = vy;
//                    model.temp[rx] = vx;
//                    result = model.voxels[sz * (sy * model.temp[0] + model.temp[1]) + model.temp[2]];
//                    if (result != 0 && result != Coloring.CLEAR) {
//                        if (!leftDone && !rightDone) {
//                            renderer.rect(px << 1, py + 3, 4, 1, color.verticalFace(result));
//                            renderer.rect(px << 1, py, 2, 3, color.leftFace(result));
//                            renderer.rect((px + 1) << 1, py, 2, 3, color.rightFace(result));
//                        } else {
//                            if (!leftDone) {
//                                renderer.rect(px << 1, py + 3, 2, 1, color.verticalFace(result));
//                                renderer.rect(px << 1, py, 2, 3, color.leftFace(result));
//                            }
//                            if (!rightDone) {
//                                renderer.rect((px + 1) << 1, py + 3, 2, 1, color.verticalFace(result));
//                                renderer.rect((px + 1) << 1, py, 2, 3, color.rightFace(result));
//                            }
//                        }
//                        break;
//                    }
//                }
//            }
//        }
//    }
//
//    public static void draw45Outline(VoxelModel model, IRectangleRenderer renderer) {
//        final int sizeX = model.sizeX(), sizeY = model.sizeY(), sizeZ = model.sizeZ(),
//                stepX = model.stepX(), stepY = model.stepY(), stepZ = model.stepZ(),
//                sy = model.sizes()[1], sz = model.sizes()[2],
//                rx = model.rotation()[0] ^ model.rotation()[0] >> 31,
//                ry = model.rotation()[1] ^ model.rotation()[1] >> 31,
//                rz = model.rotation()[2] ^ model.rotation()[2] >> 31,
//                pixelWidth = sizeX + sizeY,
//                outline = Coloring.RINSED[Coloring.OUTLINE],
//                startZ = model.startZ();
//        byte result;
//        for (int py = 0, vz = startZ; vz < sizeZ && vz >= 0; vz += stepZ, py += 3) {
//            model.temp[rz] = vz;
//            for (int px = 0; px <= pixelWidth; px++) { // pixel x
//                final int startX = px > sizeX - 1 ? 0 : sizeX - px - 1,
//                        startY = px - sizeX + 1 < 0 ? 0 : px - sizeX + 1;
//                for (int vx = startX, vy = startY;
//                     vx < sizeX && vy < sizeY && vx >= 0 && vy >= 0;
//                     vx += stepX, vy += stepY) { // vx is voxel x, vy is voxel y
//                    model.temp[ry] = vy;
//                    model.temp[rx] = vx;
//                    result = model.voxels[sz * (sy * model.temp[0] + model.temp[1]) + model.temp[2]];
//                    if (result != 0 && result != Coloring.CLEAR) {
//                        renderer.rect((px << 1) - 1, py - 1, 6, 6, outline);
//                        break;
//                    }
//                }
//            }
//        }
//    }

    public static int xLimit(IModel model)
    {
        final int size = Math.max(model.sizeX(), Math.max(model.sizeY(), model.sizeZ())) - 1;
        return size * 4 + 7;
    }

    public static int yLimit(IModel model)
    {
        final int size = Math.max(model.sizeX(), Math.max(model.sizeY(), model.sizeZ())) - 1;
        return Math.max(size * 5 + (size >> 1) + 7, size * 6 + 7);
    }
    public static Pixmap draw(IModel model, VoxelPixmapRenderer renderer)
    {
        final int time = (model instanceof ITemporal) ? ((ITemporal) model).frame() : 0;
        final int sizeX = model.sizeX() - 1, sizeY = model.sizeY() - 1, sizeZ = model.sizeZ() - 1,
                offsetPX = (sizeY >> 1) + 1, pixelWidth = sizeY * 3 + (sizeY >> 1) + 6, pixelHeight = sizeZ * 3 + 7;
        for (int z = 0; z <= sizeZ; z++) {
            for (int y = 0; y <= sizeY; y++) {
                for (int x = 0; x <= sizeX; x++) {
                    byte v = model.at(x, y, z);
                    if (v != 0) {
                        final int xPos = (sizeY - y) * 3 + offsetPX;
                        renderer.rectRight(xPos, z * 3 + 1, 3, 3, v, 256 + x * 2, x, y, z);
                        if (z >= sizeZ - 1 || model.at(x, y, z + 1) == 0)
                            renderer.rectVertical(xPos, z * 3 + 4, 3, 1, v, 256 + x * 2, x, y, z);
                    }
                }
            }
        }
        return renderer.blit(2, pixelWidth, pixelHeight);
    }

    public static Pixmap draw45(IModel model, VoxelPixmapRenderer renderer)
    {
        final int time = (model instanceof ITemporal) ? ((ITemporal) model).frame() : 0;
        final int sizeX = model.sizeX() - 1, sizeY = model.sizeY() - 1, sizeZ = model.sizeZ() - 1,
                pixelWidth = (sizeX + sizeY) * 2 + 7, pixelHeight = sizeZ * 3 + 7;
        int dep;
        for (int z = 0; z <= sizeZ; z++) {
            for (int x = 0; x <= sizeX; x++) {
                for (int y = 0; y <= sizeY; y++) {
                    byte v = model.at(x, y, z);
                    if (v != 0) {
                        dep = 3 * (x - y) + 256;
                        final int xPos = (sizeY + x - y) * 2 + 1;
                        renderer.rectLeft(xPos, z * 3 + 1, 2, 3, v, dep, x, y, z);
                        renderer.rectRight(xPos + 2, z * 3 + 1, 2, 3, v, dep, x, y, z);
                        if (z >= sizeZ - 1 || model.at(x, y, z + 1) == 0)
                            renderer.rectVertical(xPos, z * 3 + 4, 4, 1, v, dep, x, y, z);
                    }
                }
            }
        }
        return renderer.blit(5, pixelWidth, pixelHeight);
    }
    public static Pixmap drawAbove(IModel model, VoxelPixmapRenderer renderer)
    {
        final int time = (model instanceof ITemporal) ? ((ITemporal) model).frame() : 0;
        final int sizeX = model.sizeX() - 1, sizeY = model.sizeY() - 1, sizeZ = model.sizeZ() - 1,
                offsetPX = (sizeY >> 1) + 1, offsetPY = (sizeX >> 1) + 1,
                pixelWidth = (sizeY * 3) + (sizeY >> 1) + 6, pixelHeight = sizeZ * 2 + sizeX * 3 + (sizeX >> 1) + 8;
        for (int z = 0; z <= sizeZ; z++) {
            for (int y = 0; y <= sizeY; y++) {
                for (int x = 0, d = 0; x <= sizeX; x++, d += 5) {
                    byte v = model.at(x, y, z);
                    if (v != 0) {
                        final int xPos = (sizeY - y) * 3 + offsetPX, yPos = z * 2 + (sizeX - x) * 3 + offsetPY;
                        renderer.rectRight(xPos, yPos, 3, 2, v, 256 + z * 8 - d, x, y, z);
                        if (z >= sizeZ - 1 || model.at(x, y, z + 1) == 0)
                        {
                            renderer.rectVertical(xPos, yPos + 3, 3, 3, v, 260 + z * 8 - d, x, y, z);
                        }
                    }
                }
            }
        }
        return renderer.blit(13, pixelWidth, pixelHeight);
    }
    public static Pixmap drawIso(IModel model, VoxelPixmapRenderer renderer)
    {
        final int time = (model instanceof ITemporal) ? ((ITemporal) model).frame() : 0;
        final int sizeX = model.sizeX() - 1, sizeY = model.sizeY() - 1, sizeZ = model.sizeZ() - 1,
                pixelWidth = (sizeY + sizeX) * 2 + 7, pixelHeight = (sizeX + sizeY + sizeZ) * 2 + 7;
        int dep;
        for (int z = 0; z <= sizeZ; z++) {
            for (int x = 0; x <= sizeX; x++) {
                for (int y = 0; y <= sizeY; y++) {
                    byte v = model.at(x, y, z);
                    if (v != 0) {
                        dep = 3 * (x + y + z) + 256;
                        final int xPos = (sizeY - y + x) * 2 + 1, yPos = (z - x - y + sizeX + sizeY) * 2 + 1;
                        renderer.rectLeft(xPos, yPos, 2, 2, v, dep, x, y, z);
                        renderer.rectRight(xPos + 2, yPos, 2, 2, v, dep, x, y, z);
//                        renderer.depths[xPos+1][yPos]++;
//                        renderer.depths[xPos+2][yPos]++;
//                        renderer.depths[xPos+1][yPos+1]++;
//                        renderer.depths[xPos+2][yPos+1]++;
                        if (z >= sizeZ - 1 || model.at(x, y, z + 1) == 0)
                        {
                            renderer.rectVertical(xPos, yPos + 2, 4, 2, v, dep, x, y, z);
//                            renderer.depths[xPos+1][yPos+2]++;
//                            renderer.depths[xPos+2][yPos+2]++;
//                            renderer.depths[xPos+1][yPos+3]++;
//                            renderer.depths[xPos+2][yPos+3]++;
                        }
                    }
                }
            }
        }
        return renderer.blit(12, pixelWidth, pixelHeight);
    }
    public static Pixmap draw(IVoxelSeq seq, VoxelPixmapRenderer renderer)
    {
        final int time = (seq instanceof ITemporal) ? ((ITemporal) seq).frame() : 0;
        final int len = seq.size(), sizeX = seq.sizeX(), sizeY = seq.sizeY(), sizeZ = seq.sizeZ(),
                offsetPX = (sizeY - 1 >> 1) + 1, pixelWidth = sizeY * 3 + (sizeY - 1 >> 1) + 3, pixelHeight = sizeZ * 3 + 4;
        seq.sort(IntComparator.side[seq.rotation()]);
        int xyz, x, y, z;
        byte v;
        for (int i = 0; i < len; i++) {
            v = seq.getAtHollow(i);
            if (v != 0) {
                xyz = seq.keyAtRotated(i);
                x = HashMap3D.extractX(xyz);
                y = HashMap3D.extractY(xyz);
                z = HashMap3D.extractZ(xyz);
                final int xPos = (sizeY - y) * 3 + offsetPX;
                renderer.rectRight(xPos, z * 3 + 1, 3, 3, v, 256 + x, x, y, z);
                if (z >= sizeZ - 1 || seq.getRotated(x, y, z + 1) == 0)
                    renderer.rectVertical(xPos, z * 3 + 4, 3, 1, v, 256 + x, x, y, z);
            }
        }
        return renderer.blit(2, pixelWidth, pixelHeight);
    }
    public static Pixmap draw45(IVoxelSeq seq, VoxelPixmapRenderer renderer) {
        final int time = (seq instanceof ITemporal) ? ((ITemporal) seq).frame() : 0;
        final int len = seq.size(), sizeX = seq.sizeX(), sizeY = seq.sizeY(), sizeZ = seq.sizeZ(), 
                pixelWidth = (sizeX + sizeY) * 2 + 3, pixelHeight = sizeZ * 3 + 4;
        int dep;
        seq.sort(IntComparator.side45[seq.rotation()]);
        int xyz, x, y, z;
        byte v;
        for (int i = 0; i < len; i++) {
            v = seq.getAtHollow(i);
            if (v != 0) {
                xyz = seq.keyAtRotated(i);
                x = HashMap3D.extractX(xyz);
                y = HashMap3D.extractY(xyz);
                z = HashMap3D.extractZ(xyz);
                dep = 3 * (x - y) + 256;
                final int xPos = (sizeY + x - y) * 2 + 1;
                renderer.rectLeft(xPos, z * 3 + 1, 2, 3, v, dep, x, y, z);
                renderer.rectRight(xPos + 2, z * 3 + 1, 2, 3, v, dep, x, y, z);
                if (z >= sizeZ - 1 || seq.getRotated(x, y, z + 1) == 0)
                    renderer.rectVertical(xPos, z * 3 + 4, 4, 1, v, dep, x, y, z);
            }
        }
        return renderer.blit(5, pixelWidth, pixelHeight);
    }
    public static Pixmap drawAbove(IVoxelSeq seq, VoxelPixmapRenderer renderer)
    {
        final int time = (seq instanceof ITemporal) ? ((ITemporal) seq).frame() : 0;
        final int len = seq.size(), sizeX = seq.sizeX(), sizeY = seq.sizeY(), sizeZ = seq.sizeZ(),
                offsetPX = (sizeY >> 1) + 1, offsetPY = (sizeX >> 1) + 1,
                pixelWidth = (sizeY * 3) + (sizeY >> 1) + 6, pixelHeight = sizeZ * 2 + sizeX * 3 + (sizeX >> 1) + 8;
        seq.sort(IntComparator.side[seq.rotation()]);
        int xyz, x, y, z;
        byte v;
        for (int i = 0; i < len; i++) {
            v = seq.getAtHollow(i);
            if (v != 0) {
                xyz = seq.keyAtRotated(i);
                x = HashMap3D.extractX(xyz);
                y = HashMap3D.extractY(xyz);
                z = HashMap3D.extractZ(xyz);
                final int xPos = (sizeY - y) * 3 + offsetPX, yPos = z * 2 + (sizeX - x) * 3 + offsetPY;
                renderer.rectRight(xPos, yPos, 3, 2, v, 256 + z * 8 - x * 5, x, y, z);
                //if (z >= sizeZ - 1 || seq.getRotated(x, y, z + 1) == 0)
                    renderer.rectVertical(xPos, yPos + 3, 3, 3, v, 260 + z * 8 - x * 5, x, y, z);
            }
        }
        return renderer.blit(13, pixelWidth, pixelHeight);
    }
    public static Pixmap drawIso(IVoxelSeq seq, VoxelPixmapRenderer renderer)
    {
        final int time = (seq instanceof ITemporal) ? ((ITemporal) seq).frame() : 0;
        final int len = seq.size(), sizeX = seq.sizeX(), sizeY = seq.sizeY(), sizeZ = seq.sizeZ(),
                pixelWidth = (sizeY + sizeX) * 2 + 7, pixelHeight = (sizeX + sizeY + sizeZ) * 2 + 7;
        int dep;
        seq.sort(IntComparator.side45[seq.rotation()]);
        int xyz, x, y, z;
        byte v;
        for (int i = 0; i < len; i++) {
            v = seq.getAtHollow(i);
            if (v != 0) {
                xyz = seq.keyAtRotated(i);
                x = HashMap3D.extractX(xyz);
                y = HashMap3D.extractY(xyz);
                z = HashMap3D.extractZ(xyz);
                dep = 3 * (x + y + z) + 256;
                final int xPos = (sizeY - y + x) * 2 + 1, yPos = (z - x - y + sizeX + sizeY) * 2 + 1;
                renderer.rectLeft(xPos, yPos, 2, 2, v, dep, x, y, z);
                renderer.rectRight(xPos + 2, yPos, 2, 2, v, dep, x, y, z);
                //if (z >= sizeZ - 1 || seq.getRotated(x, y, z + 1) == 0)
                    renderer.rectVertical(xPos, yPos + 2, 4, 2, v, dep, x, y, z);
            }
        }
        return renderer.blit(12, pixelWidth, pixelHeight);
    }

}
