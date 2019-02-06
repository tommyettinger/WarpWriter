package warpwriter.model;

/**
 * An interface to merge the APIs for things that change over time in discrete frames, and have a limited duration.
 * <p>
 * Created by Tommy Ettinger on 1/26/2019.
 */
public interface ITemporal {
    int duration();
    ITemporal setDuration(int duration);
    int frame();
    ITemporal setFrame(int frame);
}
