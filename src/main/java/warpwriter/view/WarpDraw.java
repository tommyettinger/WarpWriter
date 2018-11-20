package warpwriter.view;

import com.badlogic.gdx.graphics.Color;
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
                stepX = model.stepX(), stepY = model.stepY(), stepZ = model.stepZ();
        for (int z = startZ; z < sizeZ && z >= 0; z += stepZ) {
            for (int y = startY; y < sizeY && y >= 0; y += stepY) {
                for (int x = startX; x < sizeX && x >= 0; x += stepX) {
                    renderer.drawPixel(y, z, Color.rgba8888(Color.RED));
                    byte result = model.voxels[sizeZ * (sizeY * x + y) + z];
                    if (result != 0) {
                        renderer.drawPixel(y, z, color.rightFace(result));
                        break;
                    }
                }
            }
        }
    }

}
