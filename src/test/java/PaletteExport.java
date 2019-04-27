import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import warpwriter.Coloring;

import java.awt.*;
import java.io.IOException;

public class PaletteExport extends ApplicationAdapter {
    public static void main(String[] arg) {
        final PaletteExport app = new PaletteExport();
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        new Lwjgl3Application(app, config);
    }

    @Override
    public void create() {
        FileDialog fileDialog = new FileDialog((Frame) null, "Where to save png file?", FileDialog.SAVE);
        fileDialog.setVisible(true);
        System.out.println(fileDialog.getFiles()[0].getAbsolutePath());
        //VoxIO.writeVOX(fileDialog.getFiles()[0].getAbsolutePath(), new byte[32][32][32], VGA25());
        PixmapIO.PNG png = new PixmapIO.PNG();
        try {
            png.write(new FileHandle(fileDialog.getFiles()[0]), palettePixmap(VGA25()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Gdx.app.exit();
        System.exit(0);
    }

    public static Pixmap palettePixmap(int[] palette) {
        Pixmap pixmap = new Pixmap(palette.length, 1, Pixmap.Format.RGBA8888);
        for (int x = 0; x < palette.length; x++)
            pixmap.drawPixel(x, 0, palette[x]);
        return pixmap;
    }

    public static int[] VGA25() {
        int[] palette = new int[25];
        palette[0] = Coloring.VGA256[89];
        for (int x = 1; x < 17; x++)
            palette[x] = Coloring.VGA256[x];
        palette[17] = Coloring.VGA256[32];
        palette[18] = Coloring.VGA256[33];
        palette[19] = Coloring.VGA256[34];
        palette[20] = Coloring.VGA256[35];
        palette[21] = Coloring.VGA256[40];
        palette[22] = Coloring.VGA256[41];
        palette[23] = Coloring.VGA256[42];
        palette[24] = Coloring.VGA256[43];
        return palette;
    }
}
