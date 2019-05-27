package warpwriter.model.nonvoxel;

/**
 * Created by Tommy Ettinger on 3/21/2019.
 */
public interface ITurnable {
    ITurnable counterX();

    ITurnable counterY();

    ITurnable counterZ();

    ITurnable clockX();

    ITurnable clockY();

    ITurnable clockZ();

    ITurnable reset();

    float angleX();

    float angleY();

    float angleZ();
}
