package warpwriter;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;


/**
 * Created by Tommy Ettinger on 12/12/2017.
 */
public class VoxIO {
    public static byte[][][] readVox(LittleEndianDataInputStream stream) {
        // check out https://github.com/ephtracy/voxel-model/blob/master/MagicaVoxel-file-format-vox.txt for the file format used below
        byte[][][] voxelData = null;
        try {
            byte[] chunkId = new byte[4];
            if (4 != stream.read(chunkId))
                return null;
            int version = stream.readInt();

            // a MagicaVoxel .vox file starts with a 'magic' 4 character 'VOX ' identifier
            if (chunkId[0] == 'V' && chunkId[1] == 'O' && chunkId[2] == 'X' && chunkId[3] == ' ') {
                while (stream.available() > 0) {
                    // each chunk has an ID, size and child chunks
                    stream.read(chunkId);
                    int chunkSize = stream.readInt();
                    int childChunks = stream.readInt();
                    String chunkName = new String(chunkId, StandardCharsets.US_ASCII);

                    // there are only 2 chunks we only care about, and they are SIZE and XYZI
                    if (chunkName.equals("SIZE")) {
                        voxelData = new byte[stream.readInt()][stream.readInt()][stream.readInt()];
                        stream.skipBytes(chunkSize - 4 * 3);
                    } else if (chunkName.equals("XYZI") && voxelData != null) {
                        // XYZI contains n voxels
                        int numVoxels = stream.readInt();
                        // each voxel has x, y, z and color index values
                        for (int i = 0; i < numVoxels; i++) {
                            voxelData[stream.read()][stream.read()][stream.read()] = stream.readByte();
                        }
                    } else stream.skipBytes(chunkSize);   // read any excess bytes
                }

            }
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return voxelData;
    }
    private static void writeInt(DataOutputStream bin, int value) throws IOException
    {
        bin.writeInt(Integer.reverseBytes(value));
    }
    public static void writeVOX(String filename, byte[][][] voxelData, int[] palette) {
        // check out http://voxel.codeplex.com/wikipage?title=VOX%20Format&referringTitle=Home for the file format used below
        try {
            int xSize = voxelData.length, ySize = voxelData[0].length, zSize = voxelData[0][0].length;

            FileOutputStream fos = new FileOutputStream(filename);
            DataOutputStream bin = new DataOutputStream(fos);
            ByteArrayOutputStream voxelsRaw = new ByteArrayOutputStream(0);
            int voxelsCount = 0, cc;
            for (int x = 0; x < xSize; x++) {
                for (int y = 0; y < ySize; y++) {
                    for (int z = 0; z < zSize; z++) {
                        cc = voxelData[x][y][z];
                        if(cc == 0) continue;
                        voxelsRaw.write(x);
                        voxelsRaw.write(y);
                        voxelsRaw.write(z);
                        voxelsRaw.write(cc);
                        voxelsCount++;
                    }
                }
            }

            // a MagicaVoxel .vox file starts with a 'magic' 4 character 'VOX ' identifier
            bin.writeBytes("VOX ");
            // current version
            writeInt(bin, 150);

            bin.writeBytes("MAIN");
            writeInt(bin,  0);
            writeInt(bin,  12 + 12 + 12 + 4 + voxelsRaw.size() + 12 + 1024);

            //bin.writeBytes("PACK");
            //writeInt(bin,  0);
            //writeInt(bin,  12 + 12 + 12 + 4 + voxelsRaw.size() + 12 + 1024);

            bin.writeBytes("SIZE");
            writeInt(bin, 12);
            writeInt(bin, 0);
            writeInt(bin, xSize);
            writeInt(bin, ySize);
            writeInt(bin, zSize);

            bin.writeBytes("XYZI");
            writeInt(bin,  4 + voxelsRaw.size());
            writeInt(bin,  0);
            writeInt(bin,  voxelsRaw.size() >> 2);
            bin.write(voxelsRaw.toByteArray());
            //voxelsRaw.writeTo(bin);

            bin.writeBytes("RGBA");
            writeInt(bin, 1024);
            writeInt(bin,  0);
            for (int i = 1; i < 4; i++) {
                bin.writeInt(palette[i]);
            }
            bin.writeInt(palette[14]); // special case for eye shine
            for (int i = 5; i < 256; i++) {
                bin.writeInt(palette[i]);
            }
            writeInt(bin,  0);

            bin.flush();
            bin.close();
            fos.flush();
            fos.close();
        }catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }
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
            for (int i = 1; i < 4; i++) {
                bin.writeInt(palette[i]);
            }
            bin.writeInt(palette[14]); // special case for eye shine
            for (int i = 5; i < 256; i++) {
                bin.writeInt(palette[i]);
            }
            writeInt(bin,  0);

            bin.flush();
            bin.close();
            fos.flush();
            fos.close();
        }catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }
}