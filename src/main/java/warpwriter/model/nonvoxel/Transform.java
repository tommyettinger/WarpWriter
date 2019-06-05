package warpwriter.model.nonvoxel;

/**
 * Stores the components of a transformation that can be applied to a voxel model (often a VoxelSeq).
 * <br>
 * Created by Tommy Ettinger on 6/4/2019.
 */
public class Transform {
    /**
     * Rotation around x-axis in brads, typically 0-255.
     */
    public int roll = 0;
    /**
     * Rotation around y-axis in brads, typically 0-255.
     */
    public int pitch = 0;
    /**
     * Rotation around z-axis in brads, typically 0-255.
     */
    public int yaw = 0;

    public int moveX = 0;
    public int moveY = 0;
    public int moveZ = 0;
    /**
     * Multiplier for stretching the model on its x-axis.
     */
    public float stretchX = 1;
    /**
     * Multiplier for stretching the model on its y-axis.
     */
    public float stretchY = 1;
    /**
     * Multiplier for stretching the model on its z-axis.
     */
    public float stretchZ = 1;
    
    public Transform()
    {
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
        this.roll = roll;
        this.pitch = pitch;
        this.yaw = yaw;
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
}
