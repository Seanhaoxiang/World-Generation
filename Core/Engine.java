package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.Serializable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Engine implements Serializable {
    /* Feel free to change the WIDTH and HEIGHT. */
    private long seed;
    private static final int WIDTH = 70;
    private static final int HEIGHT = 35;
    private List<Room> rooms = new ArrayList<>();
    private Random ran;
    private TETile[][] world;
    private List<Space> tunnels;
    private List<Space> bigWalls;
    private List<Space> floors;
    private List<Space> pacs;
    private int score = 0;
    private Space character;
    private Space gate;
    private boolean win;
    private boolean inSpace = false;
    private boolean inDoor = false;
    private String name = "";
    private String movements = "";
    private String savem;

    private TERenderer ter;

    public class Save implements Serializable {
        private String save;
        public Save(String s) {
            save = s;
        }
    }

    private class Room implements Serializable {
        private List<Space> walls;
        private List<Space> all;
        private int size;
        private Room() {
            walls = new ArrayList<>();
            all = new ArrayList<>();
            size = 0;
        }

        private void addSpace(Space p) {
            if (p.x > (int) (WIDTH * .7)) {
                floors.add(p);
            }
            all.add(p);
        }

        private void addWall(Space p) {
            if (p.x > (int) (WIDTH * .7)) {
                floors.add(p);
            }
            walls.add(p);
            size++;
        }

        private void delete() {
            for (Space p : walls) {
                p.delete();
            }
            size = 0;
        }
    }

    private class Space implements Serializable {
        private int x;
        private int y;
        private String dir;
        private int counteract;
        private Space(int xcor, int ycor, String d) {
            x = xcor;
            y = ycor;
            dir = d;
            counteract = 0;
        }

        private void delete() {
            world[x][y] = Tileset.NOTHING;
        }

        private void wallIt() {
            for (int i = x - 1; i < x + 2; i++) {
                for (int j = y - 1; j < y + 2; j++) {
                    if (world[i][j].equals(Tileset.NOTHING)) {
                        world[i][j] = Tileset.WALL;
                        if ((i == x || j == y) && x < (WIDTH * .3)) {
                            bigWalls.add(new Space(i, j, ""));
                        }
                    }
                }
            }
        }

        private void move(int lr, int ud) {
            world[x][y] = Tileset.FLOOR;
            x = x + lr;
            y = y + ud;
            if (world[x][y] == Tileset.UNLOCKED_DOOR) {
                win = true;
                inDoor = true;
            } else if (world[x][y] == Tileset.TREE) {
                score += 1;
            } else if (world[x][y] == Tileset.FLOWER && score >= 6) {
                arriveInMagicRoom();
            } else if (world[x][y] == Tileset.SAND) {
                win = true;
            }
            world[x][y] = Tileset.AVATAR;
        }

        private double dist(int x1, int y1, int x2, int y2) {
            return Math.sqrt(Math.pow(y2 - y1, 2) + Math.pow(x2 - x1, 2));
        }

        private Space searchAlg(Space loc, List<Space> c) {
            Space b = c.get(0);
            int xg = character.x;
            int yg = character.y;
            for (int i = 0; i < c.size(); i++) {
                double be = dist(xg, yg, b.x + loc.x, b.y + loc.y);
                if (be > dist(xg, yg, loc.x + c.get(i).x, loc.y + c.get(i).y)) {
                    b = c.get(i);
                }
            }
            return b;
        }

        private void rdMove() {
            List<Space> cec = new ArrayList<>();
            TETile left = world[x - 1][y];
            TETile right = world[x + 1][y];
            TETile down = world[x][y - 1];
            TETile up = world[x][y + 1];
            if (left != Tileset.WALL && left != Tileset.UNLOCKED_DOOR && left != Tileset.FLOWER) {
                cec.add(new Space(-1, 0, ""));
            }
            if (right != Tileset.WALL && right != Tileset.UNLOCKED_DOOR && right != Tileset.FLOWER) {
                cec.add(new Space(1, 0, ""));
            }
            if (down != Tileset.WALL && down != Tileset.UNLOCKED_DOOR && right != Tileset.FLOWER) {
                cec.add(new Space(0, -1, ""));
            }
            if (up != Tileset.WALL && up != Tileset.UNLOCKED_DOOR && right != Tileset.FLOWER) {
                cec.add(new Space(0, 1, ""));
            }
            if (cec.size() >= 1) {
                Space best = searchAlg(new Space(x, y, ""), cec);
                if (counteract % 3 == 0) {
                    if (counteract % 16 == 0) {
                        world[x][y] = Tileset.TREE;
                    } else {
                        world[x][y] = Tileset.FLOOR;
                    }
                    x = x + best.x;
                    y = y + best.y;
                    if (world[x][y] == Tileset.AVATAR) {
                        win = true;
                    }
                    world[x][y] = Tileset.SAND;
                }
                counteract += 1;
            }
        }

    }

    public void animation(String line) {
        buildEmptyMap();
        int half = HEIGHT / 2;
        for (int y = 0; y < half - 4; y++) {
            world[0][y] = Tileset.SAND;
        }
        for (int y = half + 4; y < HEIGHT; y++) {
            world[0][y] = Tileset.SAND;
        }
        ter.renderFrame(world);
        StdDraw.pause(50);
        for (int x = 0; x < WIDTH - 1; x++) {
            for (int y = 0; y < half - 4; y++) {
                world[x][y] = Tileset.NOTHING;
            }
            for (int y = half + 4; y < HEIGHT; y++) {
                world[x][y] = Tileset.NOTHING;
            }
            for (int y = 0; y < half - 4; y++) {
                world[x + 1][y] = Tileset.SAND;
            }
            for (int y = half + 4; y < HEIGHT; y++) {
                world[x + 1][y] = Tileset.SAND;
            }
            StdDraw.setPenColor(Color.WHITE);
            StdDraw.rectangle(20, HEIGHT / 2, WIDTH - 20, 3);
            StdDraw.text(WIDTH / 2, HEIGHT / 2, line);
            StdDraw.show();
            StdDraw.pause(50);
            ter.renderFrame(world);

        }
    }


    public void arriveInMagicRoom() {
        animation("Being teleported to the land of flowers...");
        buildEmptyMap();
        for (int x = 31; x < 39; x += 1) {
            for (int y = 12; y < 18; y += 1) {
                world[x][y] = Tileset.SAND;
            }
        }
        for (int x = 30; x < 40; x += 1) {
            world[x][18] = Tileset.SAND;
        }
        for (int x = 29; x < 41; x += 1) {
            world[x][19] = Tileset.SAND;
        }
        for (int y = 6; y < 12; y += 1) {
            world[34][y] = Tileset.SAND;
            world[35][y] = Tileset.SAND;
        }
        for (int x = 33; x < 37; x += 1) {
            world[x][5] = Tileset.SAND;
            world[x][4] = Tileset.SAND;
        }
        for (int x = 32; x < 38; x += 1) {
            world[x][3] = Tileset.SAND;
            world[x][2] = Tileset.SAND;
        }
        ter.renderFrame(world);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.rectangle(20, HEIGHT - 10, WIDTH - 20, 3);
        StdDraw.text(WIDTH / 2, HEIGHT - 9, "Sometimes, the progress is more important than the goal!");
        StdDraw.text(WIDTH / 2, HEIGHT - 11, "YOU WON!!!");
        StdDraw.show();
        inSpace = true;
    }

    public void replay() {
        for (int i = 0; i < movements.length(); i++) {
            if (move(movements.charAt(i))) {
                for (Space s: pacs) {
                    s.rdMove();
                }
                StdDraw.pause(100);
                ter.renderFrame(world);
            }

        }
    }

    public void erplay() {
        for (int i = 0; i < movements.length(); i++) {
            if (move(movements.charAt(i))) {
                for (Space s: pacs) {
                    s.rdMove();
                }
            }

        }
        ter.renderFrame(world);
    }

    public void saveFile(String save) {
        File f = new File("./saveddata.txt");
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            FileOutputStream fs = new FileOutputStream(f);
            ObjectOutputStream os = new ObjectOutputStream(fs);
            os.writeObject(new Save(save));
        }  catch (FileNotFoundException e) {
            System.out.println("file not found");
            System.exit(0);
        } catch (IOException e) {
            System.out.println(e);
            System.exit(0);
        }
    }

    public Save loadFile() {
        File f = new File("./saveddata.txt");
        if (f.exists()) {
            try {
                FileInputStream fs = new FileInputStream(f);
                ObjectInputStream os = new ObjectInputStream(fs);
                return (Save) os.readObject();
            } catch (FileNotFoundException e) {
                System.out.println("file not found");
                System.exit(0);
            } catch (IOException e) {
                System.out.println(e);
                System.exit(0);
            } catch (ClassNotFoundException e) {
                System.out.println("class not found");
                System.exit(0);
            }
        }
        return null;
    }

    public boolean move(char c) {
        if (c == 'a' && world[character.x - 1][character.y] != Tileset.WALL) {
            character.move(-1, 0);
            savem += c;
            return true;
        } else if (c == 'w' && world[character.x][character.y + 1] != Tileset.WALL) {
            character.move(0, 1);
            savem += c;
            return true;
        } else if (c == 'd' && world[character.x + 1][character.y] != Tileset.WALL) {
            character.move(1, 0);
            savem += c;
            return true;
        } else if (c == 's' && world[character.x][character.y - 1] != Tileset.WALL) {
            character.move(0, -1);
            savem += c;
            return true;
        }
        return false;
    }

    public void actions() {
        win = false;
        while (!win) {
            if (StdDraw.hasNextKeyTyped()) {
                char ce = Character.toLowerCase(StdDraw.nextKeyTyped());
                move(ce);
                if (inSpace) {
                    return;
                }
                if (ce == ':') {
                    while (!win) {
                        if (StdDraw.hasNextKeyTyped()) {
                            char c = Character.toLowerCase(StdDraw.nextKeyTyped());
                            if (c == 'q') {
                                saveFile(savem);
                                return;
                            }
                            break;
                        }
                    }
                }
                for (Space s: pacs) {
                    s.rdMove();
                }
                ter.renderFrame(world);
            }
            label();
            StdDraw.show();
        }
    }

    public void label() {
        if ((int) StdDraw.mouseX() < WIDTH && (int) StdDraw.mouseY() < HEIGHT) {
            int xses = (int) StdDraw.mouseX();
            int yses = (int) StdDraw.mouseY();
            StdDraw.setPenColor(Color.BLACK);
            StdDraw.filledRectangle(0, HEIGHT - 1, WIDTH, 2);
            StdDraw.setPenColor(Color.WHITE);
            StdDraw.rectangle(0, HEIGHT, WIDTH, 3);
            if (world[xses][yses] == Tileset.WALL) {
                StdDraw.text(5, HEIGHT - 1, "wall");
            } else if (world[xses][yses] == Tileset.FLOOR) {
                StdDraw.text(5, HEIGHT - 1, "floor");
            } else if (world[xses][yses] == Tileset.AVATAR) {
                StdDraw.text(5, HEIGHT - 1, "avatar");
            } else if (world[xses][yses] == Tileset.SAND) {
                StdDraw.text(5, HEIGHT - 1, "monster!");
            } else if (world[xses][yses] == Tileset.UNLOCKED_DOOR) {
                StdDraw.text(5, HEIGHT - 1, "~magic gate~");
            } else if (world[xses][yses] == Tileset.TREE) {
                StdDraw.text(5, HEIGHT - 1, "monster loot");
            } else if (score >= 6 && world[xses][yses] == Tileset.FLOWER) {
                StdDraw.text(5, HEIGHT - 1, "portal");
            } else if (world[xses][yses] == Tileset.FLOWER) {
                StdDraw.text(5, HEIGHT - 1, "flower (for now)");
            } else {
                StdDraw.text(5, HEIGHT - 1, "nothing");
            }
            StdDraw.text(25, HEIGHT - 1, "avatar name: " + name);
            StdDraw.text(45, HEIGHT - 1, "loot count: " + score);
            StdDraw.text(50, HEIGHT - 2, "(Hint: Stepping on flowers kills them. Unless you have 6 monster loots)");
        }
    }

    public void drawWalls() {
        bigWalls = new ArrayList<>();
        for (Room r: rooms) {
            if (r.size != 0) {
                for (Space p : r.walls) {
                    p.wallIt();
                }
            }
        }
        for (Space p: tunnels) {
            p.wallIt();
        }
    }

    public boolean vertT(int x, int y) {
        int xmin = Math.max(x - 2, 0);
        int xmax = Math.min(x + 3, WIDTH - 1);
        for (int i = xmin; i < xmax; i++) {
            if (world[i][y].equals(Tileset.FLOOR)) {
                return true;
            }
        }
        return false;
    }

    public boolean horrT(int x, int y) {
        int ymin = Math.max(y - 2, 0);
        int ymax = Math.min(y + 3, HEIGHT - 1);
        for (int i = ymin; i < ymax; i++) {
            if (world[x][i].equals(Tileset.FLOOR)) {
                return true;
            }
        }
        return false;
    }

    public boolean hits(Space s) {
        if (s.dir.equals("up")) {
            for (int i = s.y + 1; i < HEIGHT - 1; i++) {
                if (world[s.x][i].equals(Tileset.FLOOR)) {
                    return true;
                } else if (vertT(s.x, i)) {
                    break;
                }
            }
        } else if (s.dir.equals("down")) {
            for (int i = s.y - 1; i > 0; i--) {
                if (world[s.x][i].equals(Tileset.FLOOR)) {
                    return true;
                } else if (vertT(s.x, i)) {
                    break;
                }
            }
        } else if (s.dir.equals("left")) {
            for (int i = s.x - 1; i > 0; i--) {
                if (world[i][s.y].equals(Tileset.FLOOR)) {
                    return true;
                } else if (horrT(i, s.y)) {
                    break;
                }
            }
        } else if (s.dir.equals("right")) {
            for (int i = s.x + 1; i < WIDTH - 1; i++) {
                if (world[i][s.y].equals(Tileset.FLOOR)) {
                    return true;
                } else if (horrT(i, s.y)) {
                    break;
                }
            }
        } else if (s.dir.equals("done")) {
            return true;
        }
        return false;
    }

    public void build(Space s) {
        int x = s.x;
        int y = s.y;
        if (s.dir.equals("up")) {
            while (!world[x][y + 1].equals(Tileset.FLOOR)) {
                world[x][y + 1] = Tileset.FLOOR;
                tunnels.add(new Space(x, y + 1, ""));
                y++;
            }
        } else if (s.dir.equals("down")) {
            while (!world[x][y - 1].equals(Tileset.FLOOR)) {
                world[x][y - 1] = Tileset.FLOOR;
                tunnels.add(new Space(x, y - 1, ""));
                y--;
            }
        } else if (s.dir.equals("left")) {
            while (!world[x - 1][y].equals(Tileset.FLOOR)) {
                world[x - 1][y] = Tileset.FLOOR;
                tunnels.add(new Space(x - 1, y, ""));
                x--;
            }
        } else if (s.dir.equals("right")) {
            while (!world[x + 1][y].equals(Tileset.FLOOR)) {
                world[x + 1][y] = Tileset.FLOOR;
                tunnels.add(new Space(x + 1, y, ""));
                x++;
            }
        }
    }

    public void drawTunnels() {
        tunnels = new ArrayList<>();
        for (Room r: rooms) {
            int touch = r.size / 10 + 1;
            List<String> dirs = new ArrayList<>();
            for (Space s: r.walls) {
                if (touch > 0 && hits(s)) {
                    touch--;
                    build(s);
                    dirs.add(s.dir);
                }
            }
            if (touch == r.size / 10 + 1) {
                r.delete();
            }
        }
    }

    public boolean availability(int xcor, int ycor, int xlen, int ylen) {
        int xmax = Math.min(xcor + xlen + 2, WIDTH);
        int ymax = Math.min(ycor + ylen + 2, HEIGHT);
        int xmin = Math.max(xcor - 2, 0);
        int ymin = Math.max(ycor - 2, 0);
        for (int i = xmin; i < xmax; i++) {
            for (int j = ymin; j < ymax; j++) {
                if (!world[i][j].equals(Tileset.NOTHING)) {
                    return false;
                }
            }
        }
        return true;
    }

    public void drawRoom(int xcor, int ycor, int xlen, int ylen) {
        int xmax = Math.min(xcor + xlen, WIDTH - 1);
        int ymax = Math.min(ycor + ylen, HEIGHT - 5);
        Room r = new Room();
        pacs = new ArrayList<>();
        for (int i = xcor; i < xmax; i++) {
            for (int j = ycor; j < ymax; j++) {
                if (i == xcor) {
                    r.addWall(new Space(i, j, "left"));
                }
                if (i == xmax - 1) {
                    r.addWall(new Space(i, j, "right"));
                }
                if (j == ycor) {
                    r.addWall(new Space(i, j, "down"));
                }
                if (j == ymax - 1) {
                    r.addWall(new Space(i, j, "up"));
                }
                world[i][j] = Tileset.FLOOR;
                r.addSpace(new Space(i, j, ""));
            }
        }
        rooms.add(r);
    }

    public void drawRooms(int count) {
        floors = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int xstart = RandomUtils.uniform(ran, 1, WIDTH - 3);
            int ystart = RandomUtils.uniform(ran, 1, HEIGHT - 3);
            int xlen = RandomUtils.uniform(ran, 1, 8);
            int ylen = RandomUtils.uniform(ran, 1, 8);
            if (availability(xstart, ystart, xlen, ylen)) {
                drawRoom(xstart, ystart, xlen, ylen);
            } else {
                i--;
            }
        }
    }

    public void buildEmptyMap() {
        world = new TETile[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                world[x][y] = Tileset.NOTHING;
            }
        }
    }

    // fills new world with random rooms and hallways
    public TETile[][] populateMap() {
        drawRooms(28);
        drawTunnels();
        drawWalls();
        return world;
    }

    public void implementSeed(long inputSeed) {
        ran = new Random(inputSeed);
    }

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void drawFrame(String txt) {
        StdDraw.clear(Color.black);
        StdDraw.text(30, 20, "Enter Seed: " + txt);
        StdDraw.show();
    }

    public String worldSeed() {
        String see = "";
        int count = 19;
        StdDraw.clear(Color.black);
        StdDraw.text(30, 20, "Enter Seed: ");
        StdDraw.show();
        while (true) {
            if (count == 0) {
                break;
            }
            if (StdDraw.hasNextKeyTyped()) {
                char ce = Character.toLowerCase(StdDraw.nextKeyTyped());
                if (ce == 's') {
                    return see;
                }
                see += ce;
                drawFrame(see);
                count--;
            }
        }
        return see;
    }

    public String setName() {
        name = "";
        boolean complete = false;
        StdDraw.clear(Color.black);
        StdDraw.text(30, 20, "Enter Name: ");
        StdDraw.show();
        while (!complete) {
            if (StdDraw.hasNextKeyTyped()) {
                char ce = Character.toLowerCase(StdDraw.nextKeyTyped());
                if (ce == 's') {
                    return name;
                }
                name += ce;
                StdDraw.clear(Color.black);
                StdDraw.text(30, 20, "Enter Name: " + name);
                StdDraw.show();
            }
        }
        return name;
    }

    public void setCharacter() {
        int hold = RandomUtils.uniform(ran, 0, floors.size() - 1);
        character = floors.get(hold);
        world[character.x][character.y] = Tileset.AVATAR;
    }

    public void setGate() {
        int hold = RandomUtils.uniform(ran, 0, bigWalls.size() - 1);
        gate = bigWalls.get(hold);
        world[gate.x][gate.y] = Tileset.UNLOCKED_DOOR;
    }

    public void setPacs(int x) {
        List<Room> hold = new ArrayList<>();
        for (int i = 0; i < x; i++) {
            int r = RandomUtils.uniform(ran, 0, rooms.size() - 1);
            hold.add(rooms.get(r));
        }
        List<Space> he = new ArrayList<>();
        for (Room r: hold) {
            if (r.size > 1) {
                Space q = r.walls.get(0);
                if (world[q.x][q.y] == Tileset.FLOOR) {
                    he.add(q);
                    world[q.x][q.y] = Tileset.SAND;
                }
            }
        }
        pacs = he;
    }

    public void setMagicRooms(int x) {
        List<Room> hold = new ArrayList<>();
        for (int i = 0; i < x; i++) {
            int r = RandomUtils.uniform(ran, 0, rooms.size() - 1);
            hold.add(rooms.get(r));
        }
        List<Space> he = new ArrayList<>();
        for (Room r: hold) {
            if (r.all.size() > 8) {
                Space q = r.all.get(r.all.size() / 2);
                if (world[q.x][q.y] == Tileset.FLOOR) {
                    he.add(q);
                    world[q.x][q.y] = Tileset.FLOWER;
                }
            }
        }
    }

    public void characterize() {
        setGate();
        setCharacter();
        setPacs(10);
        setMagicRooms(10);
        ter.renderFrame(world);
    }

    public void displayMainMenu() {
        StdDraw.text(30, 30, "CS61B: The Game");
        Font options = new Font("Arial", Font.BOLD, 20);
        StdDraw.setFont(options);
        StdDraw.text(30, 20, "New Game (N)");
        StdDraw.text(30, 18, "Load Game (L)");
        StdDraw.text(30, 16, "Watch Replay (R)");
        StdDraw.text(30, 14, "Change Avatar Name (C)");
        StdDraw.text(30, 12, "Quit (Q)");
        if (name.equals("")) {
            StdDraw.text(35, 5, "Your avatar has no name. Name your avatar!");
        } else {
            StdDraw.text(30, 5, "Your avatar's name: " + name);
        }
    }

    public void nowWhat() {
        animation("Walking through the door...");
        buildEmptyMap();
        ter.renderFrame(world);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.rectangle(20, HEIGHT - 10, WIDTH - 20, 3);
        StdDraw.text(WIDTH / 2, HEIGHT - 10, "So... You got to the end... Congratulations!");
        StdDraw.show();
    }
    public void interactWithKeyboard() {
        boolean useLoading = false;
        boolean replay = false;
        StdDraw.setXscale(0, WIDTH);
        StdDraw.setYscale(0, HEIGHT);
        StdDraw.enableDoubleBuffering();
        StdDraw.clear(Color.black);
        StdDraw.setPenColor(Color.white);
        Font title = new Font("Arial", Font.BOLD, 30);
        StdDraw.setFont(title);
        displayMainMenu();
        String see = "";
        StdDraw.show();
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = StdDraw.nextKeyTyped();
                if (c == 'n' || c == 'N') {
                    see = worldSeed();
                    break;
                } else if (c == 'c' || c == 'C') {
                    name = setName();
                    StdDraw.clear(Color.black);
                    displayMainMenu();
                    StdDraw.show();
                } else if (c == 'l' || c == 'L') {
                    see = loadFile().save;
                    useLoading = true;
                    break;
                } else if (c == 'r' || c == 'R') {
                    see = loadFile().save;
                    useLoading = true;
                    replay = true;
                    break;
                }
            }
        }
        StdDraw.clear(Color.black);
        ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);
        animation("Move the Avatar to the open door, and don't get caught by the monsters!");
        buildEmptyMap();
        if (!useLoading) {
            seed = Long.parseLong(see);
            implementSeed(seed);
            world = populateMap();
        } else {
            int counter = 0;
            if (see.charAt(0) == 'n') {
                counter += 1;
                String thisSeed = "";
                while (counter < see.length()) {
                    if (Character.isDigit(see.charAt(counter))) {
                        thisSeed += see.charAt(counter);
                    } else if (see.charAt(counter) == 's') {
                        seed = Long.parseLong(thisSeed);
                        implementSeed(seed);
                        world = populateMap();
                        movements = see.substring(counter + 1);
                        break;
                    }
                    counter++;
                }
            }
        }
        savem = 'n' + Long.toString(seed) + 's';
        ter.renderFrame(world);
        characterize();
        if (replay) {
            replay();
        } else {
            erplay();
        }
        StdDraw.setPenColor(Color.WHITE);
        actions();
        if (inSpace) {
            return;
        }
        if (inDoor) {
            nowWhat();
            StdDraw.pause(5000);
            System.exit(0);
            return;
        }
        System.out.println("ending");
        System.exit(0);
        return;
    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     *
     * In other words, both of these calls:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */

    public TETile[][] interactWithInputString(String input) {
        String cleanedInput = input.toLowerCase();
        int counter = 0;
        String thisSeed = "";
        if (cleanedInput.charAt(0) == 'n') {
            counter += 1;
            while (counter < cleanedInput.length()) {
                if (Character.isDigit(cleanedInput.charAt(counter))) {
                    thisSeed += cleanedInput.charAt(counter);
                } else if (cleanedInput.charAt(counter) == 's') {
                    // otherwise, use new seed to make new world
                    seed = Long.parseLong(thisSeed);
                    implementSeed(seed);
                    buildEmptyMap();
                    world = populateMap();
                    counter++;
                    savem = 'n' + Long.toString(seed) + 's';
                    break;
                }
                counter++;
            }
        } else if (cleanedInput.charAt(0) == 'l') {
            // just build old world
            String se = loadFile().save;
            if (cleanedInput.length() > 1) {
                se = se + cleanedInput.substring(1);
            }
            return interactWithInputString(se);
        }
        setGate();
        setCharacter();
        if (counter < cleanedInput.length()) {
            movements = cleanedInput.substring(counter);
            for (int i = 0; i < movements.length(); i++) {
                boolean helper = i + 1 < movements.length();
                if (move(movements.charAt(i))) {
                    i = i;
//                    for (Space s: pacs) {
//                        s.rdMove();
//                    }
                } else if (helper && movements.substring(i, i + 2).equals(":q")) {
                    saveFile(savem);
                    break;
                }
            }
        }
        return world;
    }

    public void drawMap(TETile[][] meworld) {
        ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);
        ter.renderFrame(meworld);
    }
}
