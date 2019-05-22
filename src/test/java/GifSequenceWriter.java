import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.utils.Array;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * Copied straight from https://github.com/Anuken/GDXGifRecorder/blob/master/src/io/anuke/gif/GifSequenceWriter.java
 */
public class GifSequenceWriter {
    protected ImageWriter gifWriter;
    protected ImageWriteParam imageWriteParam;
    protected IIOMetadata imageMetaData;

    /**
     * Creates a new GifSequenceWriter
     *
     * @param imageType           one of the imageTypes specified in BufferedImage
     * @param timeBetweenFramesMS the time between frames in miliseconds
     * @param loopContinuously    whether the gif should loop repeatedly
     * @throws IIOException if no gif ImageWriters are found
     * @author Elliot Kroo (elliot@kroo.net)
     */
    public GifSequenceWriter(int imageType, int timeBetweenFramesMS, boolean loopContinuously) {
        // my method to create a writer
        try {
            gifWriter = getWriter();

        imageWriteParam = gifWriter.getDefaultWriteParam();
        ImageTypeSpecifier imageTypeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(imageType);
        imageMetaData = gifWriter.getDefaultImageMetadata(imageTypeSpecifier, imageWriteParam);

        String metaFormatName = imageMetaData.getNativeMetadataFormatName();

        IIOMetadataNode root = (IIOMetadataNode) imageMetaData.getAsTree(metaFormatName);

        IIOMetadataNode graphicsControlExtensionNode = getNode(root, "GraphicControlExtension");

        graphicsControlExtensionNode.setAttribute("disposalMethod", "restoreToBackgroundColor");
        graphicsControlExtensionNode.setAttribute("userInputFlag", "FALSE");
        graphicsControlExtensionNode.setAttribute("transparentColorFlag", "TRUE");
        graphicsControlExtensionNode.setAttribute("delayTime", Integer.toString(timeBetweenFramesMS / 10));
        graphicsControlExtensionNode.setAttribute("transparentColorIndex", "0");

        IIOMetadataNode commentsNode = getNode(root, "CommentExtensions");
        commentsNode.setAttribute("CommentExtension", "Created by MAH");

        IIOMetadataNode appEntensionsNode = getNode(root, "ApplicationExtensions");

        IIOMetadataNode child = new IIOMetadataNode("ApplicationExtension");

        child.setAttribute("applicationID", "NETSCAPE");
        child.setAttribute("authenticationCode", "2.0");

        int loop = loopContinuously ? 0 : 1;

        child.setUserObject(new byte[]{0x1, (byte) (loop & 0xFF), (byte) ((loop >> 8) & 0xFF)});
        appEntensionsNode.appendChild(child);

        imageMetaData.setFromTree(metaFormatName, root);

        gifWriter.prepareWriteSequence(null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeToSequence(RenderedImage img) throws IOException {
        gifWriter.writeToSequence(new IIOImage(img, null, imageMetaData), imageWriteParam);
    }

    /**
     * Close this GifSequenceWriter object. This does not close the underlying
     * stream, just finishes off the GIF.
     */
    public void close() throws IOException {
        gifWriter.endWriteSequence();
    }

    /**
     * Returns the first available GIF ImageWriter using
     * ImageIO.getImageWritersBySuffix("gif").
     *
     * @return a GIF ImageWriter object
     * @throws IIOException if no GIF image writers are returned
     */
    private static ImageWriter getWriter() throws IIOException {
        Iterator<ImageWriter> iter = ImageIO.getImageWritersBySuffix("gif");
        if (!iter.hasNext()) {
            throw new IIOException("No GIF Image Writers Exist");
        } else {
            return iter.next();
        }
    }

    /**
     * Returns an existing child node, or creates and returns a new child node (if
     * the requested node does not exist).
     *
     * @param rootNode the <tt>IIOMetadataNode</tt> to search for the child node.
     * @param nodeName the name of the child node.
     * @return the child node, if found or a new node created with the given name.
     */
    private static IIOMetadataNode getNode(IIOMetadataNode rootNode, String nodeName) {
        int nNodes = rootNode.getLength();
        for (int i = 0; i < nNodes; i++) {
            if (rootNode.item(i).getNodeName().compareToIgnoreCase(nodeName) == 0) {
                return ((IIOMetadataNode) rootNode.item(i));
            }
        }
        IIOMetadataNode node = new IIOMetadataNode(nodeName);
        rootNode.appendChild(node);
        return (node);
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
            FileImageOutputStream output = new FileImageOutputStream(file);
            gifWriter.setOutput(output);

            writeToSequence(firstImage);

            for (int i = 1; i < strings.size; i++) {
                BufferedImage after = ImageIO.read(new File(dirstring + "/" + strings.get(i)));
                saveprogress += (0.5f / frames.size);
                writeToSequence(after);
            }
            close();
            output.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
