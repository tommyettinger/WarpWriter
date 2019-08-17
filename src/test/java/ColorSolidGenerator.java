import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import warpwriter.Coloring;
import warpwriter.PaletteReducer;
import warpwriter.VoxIO;
import warpwriter.model.color.Colorizer;

public class ColorSolidGenerator extends ApplicationAdapter {
    public static void main(String[] arg) {
        final ColorSolidGenerator app = new ColorSolidGenerator();
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        new Lwjgl3Application(app, config);
    }

    private PaletteReducer reducer;
    private byte[][][] sparse = new byte[96][96][96], dense = new byte[32][32][32];
    @Override
    public void create() {
        Gdx.files.local("ColorSolids/").mkdirs();
        reducer = new PaletteReducer();
        for (int x = 0; x < 32; x++) {
            for (int y = 0; y < 32; y++) {
                for (int z = 0; z < 32; z++) {
                    dense[x][y][z] = sparse[x*3+1][y*3+1][z*3+1] = reducer.paletteMapping[x << 10 | y << 5 | z];
                }
            }
        }
        VoxIO.writeVOX("ColorSolids/AuroraColorSolid.vox", dense, reducer.paletteArray);
        VoxIO.writeVOX("ColorSolids/AuroraColorSpace.vox", sparse, reducer.paletteArray);
        generate("Flesurrect", Coloring.FLESURRECT);
        generate("FlesurrectBonus", Colorizer.FlesurrectBonusPalette);
        generate("Rinsed", Coloring.RINSED);
        generate("VGA256", Coloring.VGA256);
        generate("Twirl64", Coloring.TWIRL64);
        generate("Twirl256", Coloring.TWIRL256);

        Gdx.app.exit();
    }
    
    public void generate(String name, int[] pal)
    {
        reducer.exact(pal);
        for (int x = 0; x < 32; x++) {
            for (int y = 0; y < 32; y++) {
                for (int z = 0; z < 32; z++) {
                    dense[x][y][z] = sparse[x*3+1][y*3+1][z*3+1] = reducer.paletteMapping[x << 10 | y << 5 | z];
                }
            }
        }
        VoxIO.writeVOX("ColorSolids/" + name + "ColorSolid.vox", dense, reducer.paletteArray);
        VoxIO.writeVOX("ColorSolids/" + name + "ColorSpace.vox", sparse, reducer.paletteArray);

    }
}
