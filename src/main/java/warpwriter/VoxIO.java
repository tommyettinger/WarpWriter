package warpwriter;

import squidpony.annotation.GwtIncompatible;
import warpwriter.model.AnimatedVoxelSeq;
import warpwriter.model.IVoxelSeq;
import warpwriter.model.VoxelSeq;
import warpwriter.model.nonvoxel.LittleEndianDataInputStream;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import static warpwriter.model.nonvoxel.HashMap3D.*;


/**
 * Created by Tommy Ettinger on 12/12/2017.
 */
public class VoxIO {
    public static int[] lastPalette = {
            0x00000000, 0xffffffff, 0xffffccff, 0xffff99ff, 0xffff66ff, 0xffff33ff, 0xffff00ff, 0xffccffff,
            0xffccccff, 0xffcc99ff, 0xffcc66ff, 0xffcc33ff, 0xffcc00ff, 0xff99ffff, 0xff99ccff, 0xff9999ff,
            0xff9966ff, 0xff9933ff, 0xff9900ff, 0xff66ffff, 0xff66ccff, 0xff6699ff, 0xff6666ff, 0xff6633ff,
            0xff6600ff, 0xff33ffff, 0xff33ccff, 0xff3399ff, 0xff3366ff, 0xff3333ff, 0xff3300ff, 0xff00ffff,
            0xff00ccff, 0xff0099ff, 0xff0066ff, 0xff0033ff, 0xff0000ff, 0xccffffff, 0xccffccff, 0xccff99ff,
            0xccff66ff, 0xccff33ff, 0xccff00ff, 0xccccffff, 0xccccccff, 0xcccc99ff, 0xcccc66ff, 0xcccc33ff,
            0xcccc00ff, 0xcc99ffff, 0xcc99ccff, 0xcc9999ff, 0xcc9966ff, 0xcc9933ff, 0xcc9900ff, 0xcc66ffff,
            0xcc66ccff, 0xcc6699ff, 0xcc6666ff, 0xcc6633ff, 0xcc6600ff, 0xcc33ffff, 0xcc33ccff, 0xcc3399ff,
            0xcc3366ff, 0xcc3333ff, 0xcc3300ff, 0xcc00ffff, 0xcc00ccff, 0xcc0099ff, 0xcc0066ff, 0xcc0033ff,
            0xcc0000ff, 0x99ffffff, 0x99ffccff, 0x99ff99ff, 0x99ff66ff, 0x99ff33ff, 0x99ff00ff, 0x99ccffff,
            0x99ccccff, 0x99cc99ff, 0x99cc66ff, 0x99cc33ff, 0x99cc00ff, 0x9999ffff, 0x9999ccff, 0x999999ff,
            0x999966ff, 0x999933ff, 0x999900ff, 0x9966ffff, 0x9966ccff, 0x996699ff, 0x996666ff, 0x996633ff,
            0x996600ff, 0x9933ffff, 0x9933ccff, 0x993399ff, 0x993366ff, 0x993333ff, 0x993300ff, 0x9900ffff,
            0x9900ccff, 0x990099ff, 0x990066ff, 0x990033ff, 0x990000ff, 0x66ffffff, 0x66ffccff, 0x66ff99ff,
            0x66ff66ff, 0x66ff33ff, 0x66ff00ff, 0x66ccffff, 0x66ccccff, 0x66cc99ff, 0x66cc66ff, 0x66cc33ff,
            0x66cc00ff, 0x6699ffff, 0x6699ccff, 0x669999ff, 0x669966ff, 0x669933ff, 0x669900ff, 0x6666ffff,
            0x6666ccff, 0x666699ff, 0x666666ff, 0x666633ff, 0x666600ff, 0x6633ffff, 0x6633ccff, 0x663399ff,
            0x663366ff, 0x663333ff, 0x663300ff, 0x6600ffff, 0x6600ccff, 0x660099ff, 0x660066ff, 0x660033ff,
            0x660000ff, 0x33ffffff, 0x33ffccff, 0x33ff99ff, 0x33ff66ff, 0x33ff33ff, 0x33ff00ff, 0x33ccffff,
            0x33ccccff, 0x33cc99ff, 0x33cc66ff, 0x33cc33ff, 0x33cc00ff, 0x3399ffff, 0x3399ccff, 0x339999ff,
            0x339966ff, 0x339933ff, 0x339900ff, 0x3366ffff, 0x3366ccff, 0x336699ff, 0x336666ff, 0x336633ff,
            0x336600ff, 0x3333ffff, 0x3333ccff, 0x333399ff, 0x333366ff, 0x333333ff, 0x333300ff, 0x3300ffff,
            0x3300ccff, 0x330099ff, 0x330066ff, 0x330033ff, 0x330000ff, 0x00ffffff, 0x00ffccff, 0x00ff99ff,
            0x00ff66ff, 0x00ff33ff, 0x00ff00ff, 0x00ccffff, 0x00ccccff, 0x00cc99ff, 0x00cc66ff, 0x00cc33ff,
            0x00cc00ff, 0x0099ffff, 0x0099ccff, 0x009999ff, 0x009966ff, 0x009933ff, 0x009900ff, 0x0066ffff,
            0x0066ccff, 0x006699ff, 0x006666ff, 0x006633ff, 0x006600ff, 0x0033ffff, 0x0033ccff, 0x003399ff,
            0x003366ff, 0x003333ff, 0x003300ff, 0x0000ffff, 0x0000ccff, 0x000099ff, 0x000066ff, 0x000033ff,
            0xee0000ff, 0xdd0000ff, 0xbb0000ff, 0xaa0000ff, 0x880000ff, 0x770000ff, 0x550000ff, 0x440000ff,
            0x220000ff, 0x110000ff, 0x00ee00ff, 0x00dd00ff, 0x00bb00ff, 0x00aa00ff, 0x008800ff, 0x007700ff,
            0x005500ff, 0x004400ff, 0x002200ff, 0x001100ff, 0x0000eeff, 0x0000ddff, 0x0000bbff, 0x0000aaff,
            0x000088ff, 0x000077ff, 0x000055ff, 0x000044ff, 0x000022ff, 0x000011ff, 0xeeeeeeff, 0xddddddff,
            0xbbbbbbff, 0xaaaaaaff, 0x888888ff, 0x777777ff, 0x555555ff, 0x444444ff, 0x222222ff, 0x111111ff
    };
    public static byte[][][] readVox(LittleEndianDataInputStream stream) {
        // check out https://github.com/ephtracy/voxel-model/blob/master/MagicaVoxel-file-format-vox.txt for the file format used below
        byte[][][] voxelData = null;
        try {
            byte[] chunkId = new byte[4];
            if (4 != stream.read(chunkId))
                return null;
            //int version = 
            stream.readInt();
            int sizeX = 16, sizeY = 16, sizeZ = 16;
            // a MagicaVoxel .vox file starts with a 'magic' 4 character 'VOX ' identifier
            if (chunkId[0] == 'V' && chunkId[1] == 'O' && chunkId[2] == 'X' && chunkId[3] == ' ') {
                while (stream.available() > 0) {
                    // each chunk has an ID, size and child chunks
                    stream.read(chunkId);
                    int chunkSize = stream.readInt();
                    //int childChunks = 
                    stream.readInt();
                    String chunkName = new String(chunkId); // assumes default charset is compatible with ASCII

                    // there are only 3 chunks we only care about, and they are SIZE, XYZI, and RGBA
                    if (chunkName.equals("SIZE")) {
                        voxelData = new byte[sizeX = stream.readInt()][sizeY = stream.readInt()][sizeZ = stream.readInt()];
                        stream.skipBytes(chunkSize - 4 * 3);
                    } else if (chunkName.equals("XYZI") && voxelData != null) {
                        // XYZI contains n voxels
                        int numVoxels = stream.readInt();
                        // each voxel has x, y, z and color index values
                        for (int i = 0; i < numVoxels; i++) {
                            voxelData[stream.read()][stream.read()][stream.read()] = stream.readByte();
                        }
                    } else if(chunkName.equals("RGBA") && voxelData != null)
                    {
                        for (int i = 1; i < 256; i++) {
                            lastPalette[i] = Integer.reverseBytes(stream.readInt());
                        }
                        stream.readInt();
                    }
                    else stream.skipBytes(chunkSize);   // read any excess bytes
                }

            }
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return voxelData;
    }
    public static AnimatedVoxelSeq readVoxelSeq(LittleEndianDataInputStream stream) {
        // check out https://github.com/ephtracy/voxel-model/blob/master/MagicaVoxel-file-format-vox.txt for the file format used below
        AnimatedVoxelSeq voxelData = null;
        VoxelSeq[] seqs = null;
        int frames;
        int currentFrame = -1;
        try {
            byte[] chunkId = new byte[4];
            if (4 != stream.read(chunkId))
                return new AnimatedVoxelSeq();
            //int version = 
            stream.readInt();
            // a MagicaVoxel .vox file starts with a 'magic' 4 character 'VOX ' identifier
            if (chunkId[0] == 'V' && chunkId[1] == 'O' && chunkId[2] == 'X' && chunkId[3] == ' ') {
                while (stream.available() > 0) {
                    // each chunk has an ID, size and child chunks
                    stream.read(chunkId);
                    int chunkSize = stream.readInt();
                    //int childChunks = 
                    stream.readInt();
                    String chunkName = new String(chunkId); // assumes default charset is compatible with ASCII
                    
                    // if PACK is found, there could be multiple frames
                    if(chunkName.equals("PACK"))
                    {
                        frames = stream.readInt();
                        voxelData = new AnimatedVoxelSeq(seqs = new VoxelSeq[frames]);
                    }
                    // there are only 3 other chunks we only care about, and they are SIZE, XYZI, and RGBA
                    else if (chunkName.equals("SIZE")) {
                        if(seqs == null)
                        {
                            voxelData = new AnimatedVoxelSeq(seqs = new VoxelSeq[1]);
                        }
                        currentFrame++;
                        seqs[currentFrame] = new VoxelSeq();
                        seqs[currentFrame].sizeX(stream.readInt());
                        seqs[currentFrame].sizeY(stream.readInt());
                        seqs[currentFrame].sizeZ(stream.readInt());
                        stream.skipBytes(chunkSize - 4 * 3);
                    } else if (chunkName.equals("XYZI") && seqs != null) {
                        // XYZI contains n voxels
                        int numVoxels = stream.readInt();

                        // each voxel has x, y, z and color index values
                        for (int i = 0; i < numVoxels; i++) {
                            seqs[currentFrame].put(stream.read(), stream.read(), stream.read(), stream.readByte());
                        }
                        seqs[currentFrame].hollow();
                    } else if(chunkName.equals("RGBA") && voxelData != null)
                    {
                        for (int i = 1; i < 256; i++) {
                            lastPalette[i] = Integer.reverseBytes(stream.readInt());
                        }
                        stream.readInt();
                    }
                    else stream.skipBytes(chunkSize);   // read any excess bytes
                }

            }
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(voxelData == null)
        {
            voxelData = new AnimatedVoxelSeq();
        }
        return voxelData;
    }

    public static VoxelSeq readPriorities(LittleEndianDataInputStream stream) {
        // check out https://github.com/ephtracy/voxel-model/blob/master/MagicaVoxel-file-format-vox.txt for the file format used below
        VoxelSeq priorities = null;
        int sizeX = 16, sizeY = 16, sizeZ = 16;
        boolean hasReadSize = false, hasReadPositions = false;
        try {
            byte[] chunkId = new byte[4];
            if (4 != stream.read(chunkId))
                return null;
            //int version =
            stream.readInt();
            // a MagicaVoxel .vox file starts with a 'magic' 4 character 'VOX ' identifier
            if (chunkId[0] == 'V' && chunkId[1] == 'O' && chunkId[2] == 'X' && chunkId[3] == ' ') {
                while (stream.available() > 0) {
                    // each chunk has an ID, size and child chunks
                    stream.read(chunkId);
                    int chunkSize = stream.readInt();
                    //int childChunks = 
                    stream.readInt();
                    String chunkName = new String(chunkId); // assumes default charset is compatible with ASCII

                    // there are only 3 chunks we only care about, and they are SIZE, XYZI, and RGBA
                    if (chunkName.equals("SIZE")) {
                        sizeX = stream.readInt();
                        sizeY = stream.readInt();
                        sizeZ = stream.readInt();
                        stream.skipBytes(chunkSize - 4 * 3);
                        hasReadSize = true;
                    } else if (chunkName.equals("XYZI")) {
                        if(!hasReadPositions)
                        {
                            hasReadPositions = true;
                            stream.skipBytes(chunkSize);
                            continue;
                        }
                        // XYZI contains n voxels
                        int numVoxels = stream.readInt();
                        priorities = new VoxelSeq(numVoxels);
                        // each voxel has x, y, z and color index values
                        for (int i = 0; i < numVoxels; i++) {
                            priorities.put(stream.read(), stream.read(), stream.read(), stream.readByte());
                        }
                    }
                    else stream.skipBytes(chunkSize);   // read any excess bytes
                }

            }
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(priorities != null)
        {
            priorities.sizeX(sizeX);
            priorities.sizeY(sizeY);
            priorities.sizeZ(sizeZ);
        }
        return priorities;
    }
    private static void writeInt(DataOutputStream bin, int value) throws IOException
    {
        bin.writeInt(Integer.reverseBytes(value));
    }
    @GwtIncompatible
    public static void writeVOX(String filename, byte[][][] voxelData, int[] palette) {
        writeVOX(filename, voxelData, palette, null);
    }
    @GwtIncompatible
    public static void writeVOX(String filename, byte[][][] voxelData, int[] palette, VoxelSeq priorities) {
        // check out http://voxel.codeplex.com/wikipage?title=VOX%20Format&referringTitle=Home for the file format used below
        try {
            int xSize = voxelData.length, ySize = voxelData[0].length, zSize = voxelData[0][0].length;

            FileOutputStream fos = new FileOutputStream(filename);
            DataOutputStream bin = new DataOutputStream(fos);
            ByteArrayOutputStream voxelsRaw = new ByteArrayOutputStream(0);
            int cc;
            for (int x = 0; x < xSize; x++) {
                for (int y = 0; y < ySize; y++) {
                    for (int z = 0; z < zSize; z++) {
                        cc = voxelData[x][y][z];
                        if(cc == 0) continue;
                        voxelsRaw.write(x);
                        voxelsRaw.write(y);
                        voxelsRaw.write(z);
                        voxelsRaw.write(cc);
                    }
                }
            }

            // a MagicaVoxel .vox file starts with a 'magic' 4 character 'VOX ' identifier
            bin.writeBytes("VOX ");
            // current version
            writeInt(bin, 150);

            bin.writeBytes("MAIN");
            writeInt(bin, 0);
            writeInt(bin, 12 + 12 + 12 + 4 + voxelsRaw.size() + 12 + 1024);

            if(priorities != null) {
                bin.writeBytes("PACK");
                writeInt(bin, 4);
                writeInt(bin, 0);
                writeInt(bin, 2);
            }
            
            bin.writeBytes("SIZE");
            writeInt(bin, 12);
            writeInt(bin, 0);
            writeInt(bin, xSize);
            writeInt(bin, ySize);
            writeInt(bin, zSize);

            bin.writeBytes("XYZI");
            writeInt(bin, 4 + voxelsRaw.size());
            writeInt(bin, 0);
            writeInt(bin, voxelsRaw.size() >> 2);
            bin.write(voxelsRaw.toByteArray());

            if(priorities != null) {
                // priorities section 1
                bin.writeBytes("SIZE");
                writeInt(bin, 12);
                writeInt(bin, 0);
                writeInt(bin, xSize);
                writeInt(bin, ySize);
                writeInt(bin, zSize);

                // priorities section 2
                bin.writeBytes("XYZI");
                int fullSize = priorities.fullSize();
                writeInt(bin, fullSize * 4 + 4);
                writeInt(bin, 0);
                writeInt(bin, fullSize);
                for (int j = 0; j < fullSize; j++) {
                    int key = priorities.keyAt(j);
                    bin.writeByte(extractX(key));
                    bin.writeByte(extractY(key));
                    bin.writeByte(extractZ(key));
                    bin.writeByte(priorities.getAt(j));
                }
            }
            bin.writeBytes("RGBA");
            writeInt(bin, 1024);
            writeInt(bin, 0);
            int i = 1;
            for (; i < 256 && i < palette.length; i++) {
                bin.writeInt(palette[i]);
            }
            // if the palette is smaller than 256 colors, this fills the rest with lastPalette's colors
            for (; i < 256; i++) {
                bin.writeInt(lastPalette[i]);
            }
            writeInt(bin,  0);
            
            bin.flush();
            bin.close();
            fos.flush();
            fos.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GwtIncompatible
    public static void writeAnimatedVOX(String filename, byte[][][][] voxelData, int[] palette) {
        // check out http://voxel.codeplex.com/wikipage?title=VOX%20Format&referringTitle=Home for the file format used below
        try {
            int frames = voxelData.length, xSize = voxelData[0].length, ySize = voxelData[0][0].length, zSize = voxelData[0][0][0].length;

            FileOutputStream fos = new FileOutputStream(filename);
            DataOutputStream bin = new DataOutputStream(fos);
            byte[][] rawArrays = new byte[frames][];
            int cc, totalSize = 0;
            for (int f = 0; f < frames; f++) {
                ByteArrayOutputStream voxelsRaw = new ByteArrayOutputStream(0);
                for (int x = 0; x < xSize; x++) {
                    for (int y = 0; y < ySize; y++) {
                        for (int z = 0; z < zSize; z++) {
                            cc = voxelData[f][x][y][z];
                            if (cc == 0) continue;
                            voxelsRaw.write(x);
                            voxelsRaw.write(y);
                            voxelsRaw.write(z);
                            voxelsRaw.write(cc);
                        }
                    }
                }
                rawArrays[f] = voxelsRaw.toByteArray();
                totalSize += rawArrays[f].length;
            }

            // a MagicaVoxel .vox file starts with a 'magic' 4 character 'VOX ' identifier
            bin.writeBytes("VOX ");
            // current version
            writeInt(bin, 150);

            bin.writeBytes("MAIN");
            writeInt(bin,  0);
            writeInt(bin,  16 + 40 * frames + totalSize + 12 + 1024);

            bin.writeBytes("PACK");
            writeInt(bin, 4);
            writeInt(bin,  0);
            writeInt(bin,  frames);
            for (int f = 0; f < frames; f++) {
                bin.writeBytes("SIZE");
                writeInt(bin, 12);
                writeInt(bin, 0);
                writeInt(bin, xSize);
                writeInt(bin, ySize);
                writeInt(bin, zSize);

                bin.writeBytes("XYZI");
                writeInt(bin, 4 + rawArrays[f].length);
                writeInt(bin, 0);
                writeInt(bin, rawArrays[f].length >> 2);
                bin.write(rawArrays[f]);
                //voxelsRaw.writeTo(bin);
            }
            bin.writeBytes("RGBA");
            writeInt(bin, 1024);
            writeInt(bin,  0);
            int i = 1;
            for (; i < 256 && i < palette.length; i++) {
                bin.writeInt(palette[i]);
            }
            // if the palette is smaller than 256 colors, this fills the rest with lastPalette's colors
            for (; i < 256; i++) {
                bin.writeInt(lastPalette[i]);
            }
            writeInt(bin,  0);

            bin.flush();
            bin.close();
            fos.flush();
            fos.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This is meant to be used for AnimatedVoxelSeq collections that represent one primary voxel model in the first
     * frame, and some amount of priority sections in subsequent frames.
     * @param filename filename to write to
     * @param voxelData AnimatedVoxelSeq to write; expected to have 2 or more frames, but not required
     * @param palette the palette used by the model; the first item is always treated as 0 (fully transparent), and at
     *                most 255 subsequent entries will be used
     */
    @GwtIncompatible
    public static void writeAnimatedVOX(String filename, AnimatedVoxelSeq voxelData, int[] palette) {
        // check out http://voxel.codeplex.com/wikipage?title=VOX%20Format&referringTitle=Home for the file format used below
        try {
            int frames = voxelData.duration(), xSize = voxelData.sizeX(), ySize = voxelData.sizeY(), zSize = voxelData.sizeZ();

            FileOutputStream fos = new FileOutputStream(filename);
            DataOutputStream bin = new DataOutputStream(fos);
            byte[][] rawArrays = new byte[frames][];
            int totalSize = 0;
            for (int f = 0; f < frames; f++) {
                final IVoxelSeq vs = voxelData.seqs[f];
                final int len = vs.fullSize();
                ByteArrayOutputStream voxelsRaw = new ByteArrayOutputStream(len*4);
                int xyz;
                for (int i = 0; i < len; i++) {
                    xyz = vs.keyAt(i);
                    voxelsRaw.write(extractX(xyz));
                    voxelsRaw.write(extractY(xyz));
                    voxelsRaw.write(extractZ(xyz));
                    voxelsRaw.write(vs.getAt(i));
                }
                rawArrays[f] = voxelsRaw.toByteArray();
                totalSize += rawArrays[f].length;
            }

            // a MagicaVoxel .vox file starts with a 'magic' 4 character 'VOX ' identifier
            bin.writeBytes("VOX ");
            // current version
            writeInt(bin, 150);

            bin.writeBytes("MAIN");
            writeInt(bin,  0);
            writeInt(bin,  16 + 40 * frames + totalSize + 12 + 1024);

            bin.writeBytes("PACK");
            writeInt(bin, 4);
            writeInt(bin,  0);
            writeInt(bin,  frames);
            for (int f = 0; f < frames; f++) {
                bin.writeBytes("SIZE");
                writeInt(bin, 12);
                writeInt(bin, 0);
                writeInt(bin, xSize);
                writeInt(bin, ySize);
                writeInt(bin, zSize);

                bin.writeBytes("XYZI");
                writeInt(bin, 4 + rawArrays[f].length);
                writeInt(bin, 0);
                writeInt(bin, rawArrays[f].length >> 2);
                bin.write(rawArrays[f]);
                //voxelsRaw.writeTo(bin);
            }
            bin.writeBytes("RGBA");
            writeInt(bin, 1024);
            writeInt(bin,  0);
            int i = 1;
            for (; i < 256 && i < palette.length; i++) {
                bin.writeInt(palette[i]);
            }
            // if the palette is smaller than 256 colors, this fills the rest with lastPalette's colors
            for (; i < 256; i++) {
                bin.writeInt(lastPalette[i]);
            }
            writeInt(bin,  0);

            bin.flush();
            bin.close();
            fos.flush();
            fos.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}