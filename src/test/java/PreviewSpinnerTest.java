import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.utils.Array;
import warpwriter.VoxIO;
import warpwriter.model.IModel;
import warpwriter.model.TurnModel;
import warpwriter.model.VoxelModel;
import warpwriter.model.nonvoxel.LittleEndianDataInputStream;
import warpwriter.view.VoxelDraw;
import warpwriter.view.color.Dimmer;
import warpwriter.view.render.VoxelPixmapRenderer;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class PreviewSpinnerTest extends ApplicationAdapter {

    public static void main(String[] arg) {
        final PreviewSpinnerTest app = new PreviewSpinnerTest();
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        new Lwjgl3Application(app, config);
    }

    @Override
    public void create() {
        FileDialog fileDialog = new FileDialog((Frame) null, "Load vox file", FileDialog.LOAD);
        fileDialog.setVisible(true);

        byte[][][] bytes;
        IModel model = null;

        try {
            model = new VoxelModel(VoxIO.readVox(new LittleEndianDataInputStream(new FileInputStream(fileDialog.getFiles()[0].getAbsolutePath()))));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        renderer.color().set(Dimmer.arbitraryDimmer(VoxIO.lastPalette));

        Pixmap[] pixmaps = spin(model);

//        fileDialog = new FileDialog((Frame) null, "Where to save GIF file?", FileDialog.SAVE);
//        fileDialog.setVisible(true);
        System.out.println(fileDialog.getFiles()[0].getAbsolutePath());

        writeGIF(pixmaps, new FileHandle(fileDialog.getDirectory() + "temp"), new FileHandle(fileDialog.getDirectory()));

        Gdx.app.exit();
        System.exit(0);
    }

    public float saveprogress;
    public File lastRecording;
    public boolean saving;
    private int recordfps = 2;

    public void writeGIF(Pixmap[] pixmaps, final FileHandle directory, final FileHandle writedirectory) {
        if (saving)
            return;
        saving = true;

        final Array<String> strings = new Array<>();

        PixmapIO.PNG png = new PixmapIO.PNG();
        png.setFlipY(true);
        int i = 0;
        for (Pixmap pixmap : pixmaps) {
//                PixmapIO.writePNG(Gdx.files.absolute(directory.file().getAbsolutePath() + "/frame" + i + ".png"), pixmap);
            try {
                png.write(new FileHandle(directory.file().getAbsolutePath() + "/frame" + i + ".png"), pixmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
            strings.add("frame" + i + ".png");
            saveprogress += (0.5f / pixmaps.length);
            i++;
        }

        lastRecording = compileGIF(strings, directory, writedirectory);
//            directory.deleteDirectory();
        for (Pixmap pixmap : pixmaps) {
            pixmap.dispose();
        }
        saving = false;
    }

    private Array<byte[]> frames = new Array<>();

    private File compileGIF(Array<String> strings, FileHandle inputdirectory, FileHandle directory) {
        if (strings.size == 0) {
            throw new RuntimeException("No strings!");
        }

        try {
            String time = "" + (int) (System.currentTimeMillis() / 1000);
            String dirstring = inputdirectory.file().getAbsolutePath();
            new File(directory.file().getAbsolutePath()).mkdir();
            BufferedImage firstImage = ImageIO.read(new File(dirstring + "/" + strings.get(0)));
            File file = new File(directory.file().getAbsolutePath() + "/recording" + time + ".gif");
            ImageOutputStream output = new FileImageOutputStream(file);
            GifSequenceWriter writer = new GifSequenceWriter(output, BufferedImage.TYPE_4BYTE_ABGR, (int) (1f / recordfps * 1000f), true);

            writer.writeToSequence(firstImage);

            for (int i = 1; i < strings.size; i++) {
                BufferedImage after = ImageIO.read(new File(dirstring + "/" + strings.get(i)));
                saveprogress += (0.5f / frames.size);
                writer.writeToSequence(after);
            }
            writer.close();
            output.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Pixmap[] spin(IModel model) {
        return spin(new IModel[]{model});
    }

    public Pixmap[] spin(IModel models[]) {
        final int width = width(models) * 2, height = height(models);
        final TurnModel turnModel = new TurnModel();
        Pixmap[] result = new Pixmap[models.length * 8];
        for (int z = 0; z < 4; z++) {
            for (int model = 0; model < models.length; model++) {
                turnModel.set(models[model]);
                result[model * 8 + z * 2] = draw(turnModel, true, width, height);
                result[model * 8 + z * 2 + 1] = draw(turnModel, false, width, height); // z45 should be true, but that doesn't show up
            }
            turnModel.turner().counterZ();
        }
//        turnModel.set(models[0]);
//        Pixmap[] result = { draw(turnModel, false, width, height) };
        return result;
    }

    protected VoxelPixmapRenderer renderer = new VoxelPixmapRenderer();

    public Pixmap draw(TurnModel turnModel) {
        return draw(turnModel, true);
    }

    public Pixmap draw(TurnModel turnModel, final boolean z45) {
        return draw(turnModel, z45, VoxelDraw.isoWidth(turnModel), VoxelDraw.isoHeight(turnModel));
    }

    public Pixmap draw(TurnModel turnModel, final int width, final int height) {
        return draw(turnModel, true, width, height);
    }

    public Pixmap draw(TurnModel turnModel, final boolean z45, final int width, final int height) {
        return draw(turnModel, z45, width, height, 3);
    }

    public Pixmap draw(TurnModel turnModel, final boolean z45, final int width, final int height, final int angle) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        renderer.set(pixmap);

        final int sizeX = (z45 ? VoxelDraw.isoWidth(turnModel) : turnModel.sizeY() - 1),
                offCenter = sizeX / 2;
        final int offsetX = width / 2, offsetY = 0;

        switch (angle) {
            case 0: // Bottom
                VoxelDraw.drawBottom(turnModel, renderer
                        //.setFlipX(false).setFlipY(false)
                        .setOffset(offsetX - offCenter * 6, offsetY)
                );
                break;
            case 1: // Below
                if (z45) {
                    renderer.color().set(renderer.color().direction().flipY());
                    turnModel.turner().clockY().clockY().clockZ();
                    VoxelDraw.drawIso(turnModel, renderer
                            //.setFlipX(true).setFlipY(true)
                            .setScale(2, 1)
                            .setOffset(
                                    offsetX + (offCenter - 1) * 2,
                                    offsetY + (int) (VoxelDraw.isoHeight(turnModel))
                            )
                    );
                    turnModel.turner().counterZ().counterY().counterY();
                    renderer.color().set(renderer.color().direction().flipY());
                } else {
                    renderer.color().set(renderer.color().direction().opposite());
                    turnModel.turner().clockY().clockY().clockZ().clockZ();
                    VoxelDraw.drawAbove(turnModel, renderer
                            //.setFlipX(true).setFlipY(true)
                            .setScale(1, 1)
                            .setOffset(
                                    offsetX + (offCenter + 1) * 6,
                                    offsetY + (int) ((turnModel.sizeX() + turnModel.sizeZ()) * 4)
                            )
                    );
                    turnModel.turner().counterZ().counterZ().counterY().counterY();
                    renderer.color().set(renderer.color().direction().opposite());
                }
                break;
            case 2: // Side
                if (z45)
                    VoxelDraw.draw45Peek(turnModel, renderer
                            //.setFlipX(false).setFlipY(false)
                            .setOffset(offsetX - offCenter * 2, offsetY)
                    );
                else
                    VoxelDraw.drawRightPeek(turnModel, renderer
                            //.setFlipX(false).setFlipY(false)
                            .setOffset(offsetX - offCenter * 6, offsetY)
                    );
                break;
            case 3: // Above
                if (z45)
                    VoxelDraw.drawIso(turnModel, renderer
                            //.setFlipX(false).setFlipY(false)
                            .setScale(2, 1)
                            .setOffset(offsetX - offCenter * 2, offsetY + 4)
                    );
                else
                    VoxelDraw.drawAbove(turnModel, renderer
                            //.setFlipX(false).setFlipY(false)
                            .setScale(1, 1)
                            .setOffset(offsetX - offCenter * 6, offsetY)
                    );
                break;
            case 4: // Top
                VoxelDraw.drawTop(turnModel, renderer
                        //.setFlipX(false).setFlipY(false)
                        .setOffset(offsetX - offCenter * 6, offsetY)
                );
                break;
            default:
                throw new IllegalStateException("Angle set to unacceptable value: " + angle);
        }
        return pixmap;
    }

    public static int width(IModel[] models) {
        int width = VoxelDraw.isoWidth(models[0]);
        for (IModel model : models) {
            int newWidth = VoxelDraw.isoWidth(model);
            if (newWidth > width) width = newWidth;
        }
        return width;
    }

    public static int height(IModel[] models) {
        int height = VoxelDraw.isoHeight(models[0]);
        for (IModel model : models) {
            int newHeight = VoxelDraw.isoHeight(model);
            if (newHeight > height) height = newHeight;
        }
        return height;
    }
}
