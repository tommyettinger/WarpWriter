package warpwriter.model;

import squidpony.squidmath.CellularAutomaton;
import squidpony.squidmath.GreasedRegion;

import static warpwriter.model.nonvoxel.HashMap3D.*;

/**
 * Simple code to find the area that could be shaded below a voxel object.
 * <br>
 * Created by Tommy Ettinger on 5/22/2019.
 */
public class ShadowFinder {
    /**
     * Gets the shaded area under a VoxelSeq as a GreasedRegion, optionally "blobbing" the shadow. You can shrink the
     * shadow with {@link GreasedRegion#retract(int)} if you want, which may be useful for seeming to fade the shadow.
     * @param seq a VoxelSeq to find the shadow for; its sizeX and sizeY fields will be used for what this returns 
     * @param blobShadow if true, the shadow will be rounded and expanded in some places using a cellular automaton
     * @return a GreasedRegion with one cell per voxel (not pixel); "on" marks shadow and "off" marks normal light
     */
    public static GreasedRegion shadowBelow(IVoxelSeq seq, boolean blobShadow) {
        GreasedRegion floor = new GreasedRegion(seq.sizeX(), seq.sizeY());
        final int sz = seq.size();
        int k;
        for (int i = 0; i < sz; i++) {
            k = seq.keyAtRotatedHollow(i);
            floor.insert(extractX(k), extractY(k));
        }
        if (blobShadow) {
            CellularAutomaton ca = new CellularAutomaton(floor);
            ca.runBasicSmoothing();
            ca.runBasicSmoothing();
            return ca.current;
        }
        else {
            return floor;
        }
    }
}
