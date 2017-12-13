package warpwriter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


/**
 * Created by Tommy Ettinger on 12/12/2017.
 */
public class VoxReader {
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
}