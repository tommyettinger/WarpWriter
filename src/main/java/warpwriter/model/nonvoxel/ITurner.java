package warpwriter.model.nonvoxel;

/**
 * Created by Tommy Ettinger on 3/21/2019.
 */
public interface ITurner {
    ITurner counterX();

    ITurner counterY();

    ITurner counterZ();

    ITurner clockX();

    ITurner clockY();

    ITurner clockZ();

    ITurner reset();
}
