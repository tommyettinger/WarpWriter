import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import warpwriter.model.nonvoxel.LittleEndianDataInputStream;
import warpwriter.VoxIO;

import java.io.InputStream;

/**
 * Created as a one-off thing to generate Common Lisp code that holds the same info as a one-color .vox file.
 * Unlikely to be useful for other purposes.
 * Created by Tommy Ettinger on 5/6/2018.
 */
public class VoxProcessor extends ApplicationAdapter {
    @Override
    public void create() {
        InputStream is = VoxProcessor.class.getResourceAsStream("/lambda.vox");
        byte[][][] thing = VoxIO.readVox(new LittleEndianDataInputStream(is));
        if(thing == null)
            return;
        int xs = thing.length, ys = thing[0].length, zs = thing[0][0].length;
        int outXS = 128, outYS = 8, outZS = 128;
        int padX = outXS - xs >> 1, padY = 0, padZ = outZS - zs >> 1;
        StringBuilder sb = new StringBuilder((outXS + 2) * (outYS + 2) * (outZS + 2) * 2);
        sb.append('(');
        for (int z = 0; z < padZ; z++) {
            sb.append("\n  (");
            for (int y = 0; y < outYS; y++) {
                sb.append("\n    (");
                for (int x = 0; x < outXS; x++) {
                    sb.append('0').append(' ');
                }
                sb.setCharAt(sb.length() - 1, ')');
            }
            sb.append(')');
        }
        for (int z = 0; z < zs; z++) {
            sb.append("\n  (");
            for (int y = 0; y < ys; y++) {
                sb.append("\n    (");
                for (int x = 0; x < padX; x++) {
                    sb.append('0').append(' ');
                }
                for (int x = 0; x < xs; x++) {
                    sb.append(thing[x][y][z] & 0xFF).append(' ');
                }
                for (int x = 0; x < padX; x++) {
                    sb.append('0').append(' ');
                }
                sb.setCharAt(sb.length() - 1, ')');
            }
            sb.append(')');
        }
        for (int z = 0; z < padZ; z++) {
            sb.append("\n  (");
            for (int y = 0; y < outYS; y++) {
                sb.append("\n    (");
                for (int x = 0; x < outXS; x++) {
                    sb.append('0').append(' ');
                }
                sb.setCharAt(sb.length() - 1, ')');
            }
            sb.append(')');
        }
        sb.append(')').append('\n');
        Gdx.files.local("lambda.lisp").writeString(sb.toString(), false);
        Gdx.app.exit();
    }
    public static void main(String[] args)
    {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setWindowedMode(500, 500);
        new Lwjgl3Application(new VoxProcessor(), config);

    }
}
