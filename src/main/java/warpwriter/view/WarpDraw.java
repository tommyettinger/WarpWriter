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
        int py, px;
        for (py = 0, model.temp[rz] = startZ; model.temp[rz] < sizeZ && model.temp[rz] >= 0; model.temp[rz] += stepZ, py++) {
            for (px = 0, model.temp[ry] = startY; model.temp[ry] < sizeY && model.temp[ry] >= 0; model.temp[ry] += stepY, px++) {
                for (model.temp[rx] = startX; model.temp[rx] < sizeX && model.temp[rx] >= 0; model.temp[rx] += stepX) {
                    // uncomment to show background in red
                    //renderer.drawPixel(px, py, Color.rgba8888(Color.RED));
                    byte result = model.voxels()[sz * (sy * model.temp[0] + model.temp[1]) + model.temp[2]];
                    if (result != 0) {
                        renderer.drawPixel(px, py, color.rightFace(result));
                        break;
                    }
                }
            }
        }
    }

}
