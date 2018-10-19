package warpwriter.model;

public class DungeonFetch extends Fetch {
    boolean[][] dungeon;
    Fetch walls;
    int scale;

    public DungeonFetch (boolean[][] dungeon, int scale, Fetch walls) {
        this.dungeon = dungeon;
        this.scale = scale;
        this.walls = walls;
    }

    public Fetch fetch (int x, int y, int z) {
        y = y / scale;
        z = z / scale;
        return y >= 0 && y < dungeon.length && z >= 0 && z < dungeon[y].length && !dungeon[y][z] ? walls : getNextFetch();
    }
}
