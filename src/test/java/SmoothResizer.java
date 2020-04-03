import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import warpwriter.VoxIO;
import warpwriter.model.VoxelSeq;
import warpwriter.model.nonvoxel.LittleEndianDataInputStream;

/**
 * For testing how MagicaVoxel's marching cubes render looks with an expanded-smoothed model.
 */
public class SmoothResizer extends ApplicationAdapter {
    @Override
    public void create() {
        FileHandle input = Gdx.files.local("voxInput"), output = Gdx.files.local("voxOutput");
        input.mkdirs();
        output.mkdirs();
        FileHandle[] files = input.list(".vox");
        for(FileHandle file : files) {
            byte[][][] thing = VoxIO.readVox(new LittleEndianDataInputStream(file.read()));
            if (thing == null)
                return;
            VoxelSeq seq = new VoxelSeq();
            seq.putArray(thing);
            seq.doubleSizeSmooth();
            VoxIO.writeVOX("voxOutput/"+file.name(), seq, VoxIO.lastPalette);
        }
        Gdx.app.exit();
    }
    public static void main(String[] args)
    {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setWindowedMode(500, 500);
        config.setResizable(false);
        new Lwjgl3Application(new SmoothResizer(), config);
    }
}
