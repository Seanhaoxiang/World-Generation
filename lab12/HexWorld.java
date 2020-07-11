package byow.lab12;
import org.junit.Test;
import static org.junit.Assert.*;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.Random;

/**
 * Draws a world consisting of hexagonal regions.
 */
public class HexWorld {
    private static int W;
    private static int H;
    private static int s;
    private static TETile[][] world;

    public static void fillWorld(TETile t) {
        for (int x = 0; x < W; x += 1) {
            for (int y = 0; y < H; y += 1) {
                world[x][y] = t;
            }
        }
    }
    public static void addHexagon(int x, int y, TETile t) {
        int row = s;
        for (int i = 0; i < s; i++) {
            for (int j = 0; j < row; j++) {
                world[x + j - i][y + i] = t;
            }
            row += 2;
        }
        for (int i = s; i < (2 * s); i++) {
            row -= 2;
            for (int j = 0; j < row; j++) {
                world[x + j - (2 * s - 1 - i)][y + i] = t;
            }
        }
    }
    public static void drawVert(int x, int y, int rep) {
        for (int i = 0; i < rep; i++) {
            addHexagon(x, y, Tileset.WALL);
            y += (s * 2);
        }
    }

    public static void drawMap() {
        int x = s - 1;
        drawVert(1 * x + 0 * s, 2 * s, 3);
        drawVert(2 * x + 1 * s, 1 * s, 4);
        drawVert(3 * x + 2 * s, 0 * s, 5);
        drawVert(4 * x + 3 * s, 1 * s, 4);
        drawVert(5 * x + 4 * s, 2 * s, 3);

    }

    public static void main(String[] args) {
        s = 4;
        W = (s * 5) + ((s - 1) * 6);
        H = (s * 2) * 5;
        TERenderer ter = new TERenderer();
        ter.initialize(W, H);
        world = new TETile[W][H];
        fillWorld(Tileset.NOTHING);
        drawMap();
        ter.renderFrame(world);
    }

}
