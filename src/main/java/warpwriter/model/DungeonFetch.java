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
        return dungeon.length < y && dungeon[y].length < z && dungeon[y][z] ? walls : getNextFetch();
    }
}
