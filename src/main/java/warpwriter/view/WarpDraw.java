package warpwriter.view;

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
                    if (result != 0) {
                        renderer.drawRect(px, py + 3, 3, 1, color.topFace(result));
                        renderer.drawRect(px, py, 3, 3, color.rightFace(result));
                        break;
                    }
                }
            }
        }
    }

}
