import warpwriter.Coloring;
import warpwriter.model.color.Colorizer;

public class EmptyVoxMaker {
    public static void main(String[] arg) {
        Colorizer colorizer = Colorizer.arbitraryBonusColorizer(Coloring.VGA256);
        System.out.println("Hello world!");

        // from VoxIO
        // public static void writeVOX(String filename, byte[][][] voxelData, int[] palette)

        int[] palette = new int[26];
        for (int x=0; x<17; x++)
            palette[x] = Coloring.VGA256[x];
        palette[17] = Coloring.VGA256[32];
        palette[18] = Coloring.VGA256[33];
        palette[19] = Coloring.VGA256[34];
        palette[20] = Coloring.VGA256[35];
        palette[21] = Coloring.VGA256[40];
        palette[22] = Coloring.VGA256[41];
        palette[23] = Coloring.VGA256[42];
        palette[24] = Coloring.VGA256[43];
        palette[25] = Coloring.VGA256[89];
    }
}
