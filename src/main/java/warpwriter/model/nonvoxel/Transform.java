package warpwriter.model.nonvoxel;

import com.badlogic.gdx.math.Vector3;
import warpwriter.model.AnimatedVoxelSeq;
import warpwriter.model.IVoxelSeq;
import warpwriter.model.VoxelSeq;

import static com.badlogic.gdx.math.MathUtils.lerp;
import static com.badlogic.gdx.math.MathUtils.round;
import static warpwriter.model.nonvoxel.HashMap3D.*;

/**
 * Stores the components of a transformation that can be applied to a voxel model (often a VoxelSeq).
 * <br>
 * Created by Tommy Ettinger on 6/4/2019.
 */
public class Transform {
//    /**
//     * Rotation around x-axis in brads, typically 0-255.
//     */
//    public int roll = 0;
//    /**
//     * Rotation around y-axis in brads, typically 0-255.
//     */
//    public int pitch = 0;
//    /**
//     * Rotation around z-axis in brads, typically 0-255.
//     */
//    public int yaw = 0;

    
    public TurnQuaternion rotation;
    public int moveX = 0;
    public int moveY = 0;
    public int moveZ = 0;
    /**
     * Multiplier for stretching the model on its x-axis.
     */
    public float stretchX = 1f;
    /**
     * Multiplier for stretching the model on its y-axis.
     */
    public float stretchY = 1f;
    /**
     * Multiplier for stretching the model on its z-axis.
     */
    public float stretchZ = 1f;
    
    private final Vector3 temp = new Vector3();
    
    public Transform()
    {
        rotation = new TurnQuaternion();
    }

    /**
     * 
     * @param roll in brads, 0-255
     * @param pitch in brads, 0-255
     * @param yaw in brads, 0-255
     * @param moveX translation amount in voxel units
     * @param moveY translation amount in voxel units
     * @param moveZ translation amount in voxel units
     */
    public Transform(int roll, int pitch, int yaw, int moveX, int moveY, int moveZ)
    {
//        this.roll = roll;
//        this.pitch = pitch;
//        this.yaw = yaw;
        this.rotation = new TurnQuaternion().setEulerAnglesBrad(roll, pitch, yaw);
        this.moveX = moveX;
        this.moveY = moveY;
        this.moveZ = moveZ;
    }

    /**
     *
     * @param roll in brads, 0-255
     * @param pitch in brads, 0-255
     * @param yaw in brads, 0-255
     * @param moveX translation amount in voxel units
     * @param moveY translation amount in voxel units
     * @param moveZ translation amount in voxel units
     * @param stretchX how much to stretch as a multiplier for the x-axis
     * @param stretchY how much to stretch as a multiplier for the y-axis
     * @param stretchZ how much to stretch as a multiplier for the z-axis
     */
    public Transform(int roll, int pitch, int yaw, int moveX, int moveY, int moveZ,
                     float stretchX, float stretchY, float stretchZ)
    {
        this(roll, pitch, yaw, moveX, moveY, moveZ);
        this.stretchX = stretchX;
        this.stretchY = stretchY;
        this.stretchZ = stretchZ;
    }

    /**
     *
     * @param rotation a TurnQuaternion specifying the axis and angle of rotation
     * @param moveX translation amount in voxel units
     * @param moveY translation amount in voxel units
     * @param moveZ translation amount in voxel units
     * @param stretchX how much to stretch as a multiplier for the x-axis
     * @param stretchY how much to stretch as a multiplier for the y-axis
     * @param stretchZ how much to stretch as a multiplier for the z-axis
     */
    public Transform(TurnQuaternion rotation, int moveX, int moveY, int moveZ,
                     float stretchX, float stretchY, float stretchZ)
    {
        this.rotation = rotation;
        this.moveX = moveX;
        this.moveY = moveY;
        this.moveZ = moveZ;
        this.stretchX = stretchX;
        this.stretchY = stretchY;
        this.stretchZ = stretchZ;
    }

    public Transform interpolate(Transform end, float alpha)
    {
        return new Transform(rotation.cpy().slerp(end.rotation, alpha),
                round(lerp(moveX, end.moveX, alpha)),
                round(lerp(moveY, end.moveY, alpha)),
                round(lerp(moveZ, end.moveZ, alpha)),
                lerp(stretchX, end.stretchX, alpha),
                lerp(stretchY, end.stretchY, alpha),
                lerp(stretchZ, end.stretchZ, alpha));
    }

    public Transform interpolateInto(Transform end, float alpha, Transform receiver)
    {
        receiver.rotation.set(rotation).slerp(end.rotation, alpha);
        receiver.moveX = round(lerp(moveX, end.moveX, alpha));
        receiver.moveY = round(lerp(moveY, end.moveY, alpha));
        receiver.moveZ = round(lerp(moveZ, end.moveZ, alpha));
        receiver.stretchX = lerp(stretchX, end.stretchX, alpha);
        receiver.stretchY = lerp(stretchY, end.stretchY, alpha);
        receiver.stretchZ = lerp(stretchZ, end.stretchZ, alpha);
        return receiver;
    }

    /**
     * Given a VoxelSeq to use as a basis and a 3D point to rotate around like a socket joint, this makes a new VoxelSeq
     * that may be rotated, translated, and/or stretched from its original location.
     * @param start a VoxelSeq that will not be modified
     * @param jointX the x-coordinate of the joint to rotate around, which may be in-between voxel coordinates
     * @param jointY the y-coordinate of the joint to rotate around, which may be in-between voxel coordinates
     * @param jointZ the z-coordinate of the joint to rotate around, which may be in-between voxel coordinates
     * @return a new VoxelSeq that will contain a transformed copy of start 
     */
    public IVoxelSeq transform(IVoxelSeq start, float jointX, float jointY, float jointZ)
    {
        return transformInto(start, new VoxelSeq((int)(start.size() * stretchX * stretchY * stretchZ + 8)),
                jointX, jointY, jointZ);
    }

    /**
     * Given a VoxelSeq {@code start} to use as a basis, a (usually empty) VoxelSeq {@code next} to fill with voxels,
     * and a 3D point to rotate around like a socket joint, this inserts voxels into {@code next} that may be rotated,
     * translated, and/or stretched from its original location.
     * @param start a VoxelSeq that will not be modified
     * @param start a VoxelSeq that will be modified, but won't be cleared (voxels will be added to its current content)
     * @param jointX the x-coordinate of the joint to rotate around, which may be in-between voxel coordinates
     * @param jointY the y-coordinate of the joint to rotate around, which may be in-between voxel coordinates
     * @param jointZ the z-coordinate of the joint to rotate around, which may be in-between voxel coordinates
     * @return {@code next}, with the added voxels, for chaining
     */
    public IVoxelSeq transformInto(IVoxelSeq start, IVoxelSeq next, float jointX, float jointY, float jointZ)
    {
        final int len = start.fullSize();
        int k, x, y, z;
        byte v;
        for (int i = 0; i < len; i++) {
            k = start.keyAtRotated(i);
            v = start.getAt(i);
            x = extractX(k);
            y = extractY(k);
            z = extractZ(k);
            temp.set(x - jointX, y - jointY, z - jointZ);
            rotation.transform(temp).add(jointX + moveX, jointY + moveY, jointZ + moveZ);
//            if(stretchX <= 1.01f && stretchY <= 1.01f && stretchZ <= 1.01f)
//                next.put(round(temp.x * stretchX), round(temp.y * stretchY), round(temp.z * stretchZ), v);
//            else 
            for (int sx = 0; sx <= stretchX; sx++) {
                for (int sy = 0; sy <= stretchY; sy++) {
                    for (int sz = 0; sz <= stretchZ; sz++) {
                        next.put(
                                round(temp.x * stretchX + sx),
                                round(temp.y * stretchY + sy),
                                round(temp.z * stretchZ + sz), v);
                    }
                }
            }
        }
        next.hollow();
        return next;
    }
    /**
     * Given an AnimatedVoxelSeq to use as a basis and a 3D point to rotate around like a socket joint, this makes a new
     * VoxelSeq that may be rotated, translated, and/or stretched from its original location. This treats the second
     * frame in initial, that is, {@code initial.seqs[1]}, as holding connections, and all later frames as holding
     * increasing priorities.
     * @param initial a VoxelSeq that will not be modified
     * @param jointX the x-coordinate of the joint to rotate around, which may be in-between voxel coordinates
     * @param jointY the y-coordinate of the joint to rotate around, which may be in-between voxel coordinates
     * @param jointZ the z-coordinate of the joint to rotate around, which may be in-between voxel coordinates
     * @return a new VoxelSeq that will contain a transformed copy of start 
     */
    public IVoxelSeq transform(AnimatedVoxelSeq initial, float jointX, float jointY, float jointZ)
    {
        VoxelSeq[] seqs = new VoxelSeq[initial.seqs.length];
        seqs[0] = new VoxelSeq((int)(initial.size() * stretchX * stretchY * stretchZ + 8));
        seqs[1] = new VoxelSeq();
        for (int i = 2; i < initial.seqs.length; i++) {
            seqs[i] = new VoxelSeq(initial.seqs[i].fullSize());
        }
        return transformInto(initial, new AnimatedVoxelSeq(seqs),
                jointX, jointY, jointZ);
    }

    /**
     * Given an AnimatedVoxelSeq {@code initial} to use as a basis, a (usually empty) VoxelSeq {@code next} to fill with 
     * voxels, and a 3D point to rotate around like a socket joint, this inserts voxels into {@code next} that may be
     * rotated, translated, and/or stretched from its original location. This treats the second frame in initial, that
     * is, {@code initial.seqs[1]}, as holding connections, and all later frames as holding increasing priorities.
     * @param initial an AnimatedVoxelSeq that will be modified, but won't be cleared (voxels will be added to its current content)
     * @param jointX the x-coordinate of the joint to rotate around, which may be in-between voxel coordinates
     * @param jointY the y-coordinate of the joint to rotate around, which may be in-between voxel coordinates
     * @param jointZ the z-coordinate of the joint to rotate around, which may be in-between voxel coordinates
     * @return {@code next}, with the added voxels, for chaining
     */
    public IVoxelSeq transformInto(AnimatedVoxelSeq initial, AnimatedVoxelSeq next, float jointX, float jointY, float jointZ)
    {
        IVoxelSeq curr = initial.seqs[0];
        next.setFrame(0);
        int len = curr.fullSize();
        int k, x, y, z;
        byte v;
        for (int i = 0; i < len; i++) {
            k = curr.keyAtRotated(i);
            v = curr.getAt(i);
            x = extractX(k);
            y = extractY(k);
            z = extractZ(k);
            temp.set(x - jointX, y - jointY, z - jointZ);
            rotation.transform(temp).add(jointX + moveX, jointY + moveY, jointZ + moveZ);
//            if(stretchX <= 1.01f && stretchY <= 1.01f && stretchZ <= 1.01f)
//                next.put(round(temp.x * stretchX), round(temp.y * stretchY), round(temp.z * stretchZ), v);
//            else 
            for (int sx = 0; sx <= stretchX; sx++) {
                for (int sy = 0; sy <= stretchY; sy++) {
                    for (int sz = 0; sz <= stretchZ; sz++) {
                        next.put(
                                round(temp.x * stretchX + sx),
                                round(temp.y * stretchY + sy),
                                round(temp.z * stretchZ + sz), v);
                    }
                }
            }
        }
        next.hollow();
        for (int f = 1; f < initial.seqs.length; f++) {
            next.setFrame(f);
            len = curr.fullSize();
            for (int i = 0; i < len; i++) {
                k = curr.keyAtRotated(i);
                v = curr.getAt(i);
                x = extractX(k);
                y = extractY(k);
                z = extractZ(k);
                temp.set(x - jointX, y - jointY, z - jointZ);
                rotation.transform(temp).add(jointX + moveX, jointY + moveY, jointZ + moveZ);
                if (stretchX <= 1.01f && stretchY <= 1.01f && stretchZ <= 1.01f)
                    next.put(round(temp.x * stretchX), round(temp.y * stretchY), round(temp.z * stretchZ), v);
                else {
                    for (int sx = 0; sx < stretchX; sx++) {
                        for (int sy = 0; sy < stretchY; sy++) {
                            for (int sz = 0; sz < stretchZ; sz++) {
                                next.put(
                                        round(temp.x * stretchX + sx),
                                        round(temp.y * stretchY + sy),
                                        round(temp.z * stretchZ + sz), v);
                            }
                        }
                    }
                }
            }
            next.hollow();
        }
        return next;
    }
}
