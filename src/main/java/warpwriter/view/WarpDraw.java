package warpwriter.view;

import warpwriter.Coloring;
import warpwriter.warp.VoxelModel;

/**
 * Created by Tommy Ettinger on 11/19/2018.
 */
public class WarpDraw {
    protected static IVoxelColor color = new VoxelColor();

    public static void draw(VoxelModel model, IPixelRenderer renderer) {
        draw(model, renderer, color);
    }

    public static void draw(VoxelModel model, IPixelRenderer renderer, IVoxelColor color) {
        final int sizeX = model.sizeX(), sizeY = model.sizeY(), sizeZ = model.sizeZ(),
                startX = model.startX(), startY = model.startY(), startZ = model.startZ(),
                stepX = model.stepX(), stepY = model.stepY(), stepZ = model.stepZ(),
                sy = model.sizes()[1], sz = model.sizes()[2],
                rx = model.rotation()[0] ^ model.rotation()[0] >> 31,
                ry = model.rotation()[1] ^ model.rotation()[1] >> 31,
                rz = model.rotation()[2] ^ model.rotation()[2] >> 31;
        int py, px, x, y, z;
        for (py = 0, z = startZ; z < sizeZ && z >= 0; z += stepZ, py+=3) {
            model.temp[rz] = z;
            for (px = 0, y = startY; y < sizeY && y >= 0; y += stepY, px+=3) {
                model.temp[ry] = y;
                for (x = startX; x < sizeX && x >= 0; x += stepX) {
                    model.temp[rx] = x;
                    // uncomment to show background in red
                    //renderer.drawPixel(px, py, Color.rgba8888(Color.RED));
                    byte result = model.voxels[sz * (sy * model.temp[0] + model.temp[1]) + model.temp[2]];
                    if (result != 0 && result != Coloring.CLEAR) {
                        renderer.drawRect(px, py + 3, 3, 1, color.topFace(result));
                        renderer.drawRect(px, py, 3, 3, color.rightFace(result));
                        break;
                    }
                }
            }
        }
    }

    public static void drawOutline(VoxelModel model, IPixelRenderer renderer) {
        final int sizeX = model.sizeX(), sizeY = model.sizeY(), sizeZ = model.sizeZ(),
                startX = model.startX(), startY = model.startY(), startZ = model.startZ(),
                stepX = model.stepX(), stepY = model.stepY(), stepZ = model.stepZ(),
                sy = model.sizes()[1], sz = model.sizes()[2],
                rx = model.rotation()[0] ^ model.rotation()[0] >> 31,
                ry = model.rotation()[1] ^ model.rotation()[1] >> 31,
                rz = model.rotation()[2] ^ model.rotation()[2] >> 31, 
                outline = Coloring.RINSED[Coloring.OUTLINE];
        int py, px, x, y, z;
        for (py = -1, z = startZ; z < sizeZ && z >= 0; z += stepZ, py+=3) {
            model.temp[rz] = z;
            for (px = -1, y = startY; y < sizeY && y >= 0; y += stepY, px+=3) {
                model.temp[ry] = y;
                for (x = startX; x < sizeX && x >= 0; x += stepX) {
                    model.temp[rx] = x;
                    // uncomment to show background in red
                    //renderer.drawPixel(px, py, Color.rgba8888(Color.RED));
                    byte result = model.voxels[sz * (sy * model.temp[0] + model.temp[1]) + model.temp[2]];
                    if (result != 0 && result != Coloring.CLEAR) {
                        renderer.drawRect(px, py, 5, 6, outline);
                        break;
                    }
                }
            }
        }
    }

    public static void draw45(VoxelModel model, IPixelRenderer renderer) {
        draw45(model, renderer, color);
    }

    public static void draw45(VoxelModel model, IPixelRenderer renderer, IVoxelColor color) {
        final int sizeX = model.sizeX(), sizeY = model.sizeY(), sizeZ = model.sizeZ(),
                stepX = model.stepX(), stepY = model.stepY(), stepZ = model.stepZ(),
                sy = model.sizes()[1], sz = model.sizes()[2],
                rx = model.rotation()[0] ^ model.rotation()[0] >> 31,
                ry = model.rotation()[1] ^ model.rotation()[1] >> 31,
                rz = model.rotation()[2] ^ model.rotation()[2] >> 31,
                pixelWidth = sizeX + sizeY,
                startZ = model.startZ();
        byte result;
        for (int py = 0, vz = startZ; vz < sizeZ && vz >= 0; vz += stepZ, py+=3) {
            model.temp[rz] = vz;
            for (int px = 0; px <= pixelWidth; px += 2) { // pixel x
//                renderer.drawRect(px, py, 2, 1, Color.rgba8888(Color.RED));
                boolean leftDone = false, rightDone = pixelWidth - px < 2;
                final int startX = px > sizeX - 1 ? 0 : sizeX - px - 1,
                        startY = px - sizeX + 1 < 0 ? 0 : px - sizeX + 1;
                for (int vx = startX, vy = startY;
                     vx <= sizeX && vy <= sizeY && vx >= 0 && vy >= 0;
                     vx += stepX, vy += stepY) { // vx is voxel x, vy is voxel y
                    if (!leftDone && vy > 0 && vx < sizeX) {
                        model.temp[ry] = vy - 1;
                        model.temp[rx] = vx;
                        result = model.voxels[sz * (sy * model.temp[0] + model.temp[1]) + model.temp[2]];
                        if (result != 0 && result != Coloring.CLEAR) {
                            renderer.drawRect(px << 1, py + 3, 2, 1, color.topFace(result));
                            renderer.drawRect(px << 1, py, 2, 3, color.rightFace(result));
                            leftDone = true;
                        }
                    }
                    if (!rightDone && vx > 0 && vy < sizeY) {
                        model.temp[ry] = vy;
                        model.temp[rx] = vx - 1;
                        result = model.voxels[sz * (sy * model.temp[0] + model.temp[1]) + model.temp[2]];
                        if (result != 0 && result != Coloring.CLEAR) {
                            renderer.drawRect((px + 1) << 1, py + 3, 2, 1, color.topFace(result));
                            renderer.drawRect((px + 1) << 1, py, 2, 3, color.leftFace(result));
                            rightDone = true;
                        }
                    }
                    if ((leftDone && rightDone) || vx >= sizeX || vy >= sizeY) break;
                    model.temp[ry] = vy;
                    model.temp[rx] = vx;
                    result = model.voxels[sz * (sy * model.temp[0] + model.temp[1]) + model.temp[2]];
                    if (result != 0 && result != Coloring.CLEAR) {
                        if(!leftDone && !rightDone)
                        {
                            renderer.drawRect(px << 1, py + 3, 4, 1, color.topFace(result));
                            renderer.drawRect(px << 1, py, 2, 3, color.leftFace(result));
                            renderer.drawRect((px + 1) << 1, py, 2, 3, color.rightFace(result));
                        }
                        else {
                            if (!leftDone)
                            {
                                renderer.drawRect(px << 1, py + 3, 2, 1, color.topFace(result));
                                renderer.drawRect(px << 1, py, 2, 3, color.leftFace(result));
                            }
                            if (!rightDone)
                            {
                                renderer.drawRect((px + 1) << 1, py + 3, 2, 1, color.topFace(result));
                                renderer.drawRect((px + 1) << 1, py, 2, 3, color.rightFace(result));
                            }
                        }
                        break;
                    }
                }
            }
        }
    }
    public static void draw45Outline(VoxelModel model, IPixelRenderer renderer) {
        final int sizeX = model.sizeX(), sizeY = model.sizeY(), sizeZ = model.sizeZ(),
                stepX = model.stepX(), stepY = model.stepY(), stepZ = model.stepZ(),
                sy = model.sizes()[1], sz = model.sizes()[2],
                rx = model.rotation()[0] ^ model.rotation()[0] >> 31,
                ry = model.rotation()[1] ^ model.rotation()[1] >> 31,
                rz = model.rotation()[2] ^ model.rotation()[2] >> 31,
                pixelWidth = sizeX + sizeY, 
                outline = Coloring.RINSED[Coloring.OUTLINE],
                startZ = model.startZ();
        byte result;
        for (int py = 0, vz = startZ; vz < sizeZ && vz >= 0; vz += stepZ, py+=3) {
            model.temp[rz] = vz;
            for (int px = 0; px <= pixelWidth; px++) { // pixel x
                final int startX = px > sizeX - 1 ? 0 : sizeX - px - 1,
                        startY = px - sizeX + 1 < 0 ? 0 : px - sizeX + 1;
                for (int vx = startX, vy = startY;
                     vx < sizeX && vy < sizeY && vx >= 0 && vy >= 0;
                     vx += stepX, vy += stepY) { // vx is voxel x, vy is voxel y
                    model.temp[ry] = vy;
                    model.temp[rx] = vx;
                    result = model.voxels[sz * (sy * model.temp[0] + model.temp[1]) + model.temp[2]];
                    if (result != 0 && result != Coloring.CLEAR) {                         
                        renderer.drawRect((px << 1) - 1, py - 1, 6, 6, outline);
                        break;
                    }
                }
            }
        }
    }


}
