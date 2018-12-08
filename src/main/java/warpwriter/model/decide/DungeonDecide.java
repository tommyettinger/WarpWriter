package warpwriter.model.decide;

import warpwriter.model.IDecide;

public class DungeonDecide implements IDecide {
    boolean[][] dungeon;
    int scale;

    public DungeonDecide (boolean[][] dungeon, int scale) {
        this.dungeon = dungeon;
        this.scale = scale;
    }

    @Override
    public boolean bool (int x, int y, int z) {
        y = y / scale;
        z = z / scale;
        return y >= 0 && y < dungeon.length && z >= 0 && z < dungeon[y].length && !dungeon[y][z];
    }
}
