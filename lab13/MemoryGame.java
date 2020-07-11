package byow.lab13;

import edu.princeton.cs.introcs.StdDraw;

import java.awt.Color;
import java.awt.Font;
import java.util.Random;

public class MemoryGame {
    private int width;
    private int height;
    private int round;
    private Random rand;
    private boolean gameOver;
    private boolean playerTurn;
    private static final char[] CHARACTERS = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final String[] ENCOURAGEMENT = {"You can do this!", "I believe in you!",
                                                   "You got this!", "You're a star!", "Go Bears!",
                                                   "Too easy for you!", "Wow, so impressive!"};

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please enter a seed");
            return;
        }

        int seed = Integer.parseInt(args[0]);
        MemoryGame game = new MemoryGame(40, 40);
        game.rand.setSeed(seed);
        game.startGame();
    }

    public MemoryGame(int width, int height) {
        /* Sets up StdDraw so that it has a width by height grid of 16 by 16 squares as its canvas
         * Also sets up the scale so the top left is (0,0) and the bottom right is (width, height)
         */
        this.width = width;
        this.height = height;
        this.rand = new Random();
        StdDraw.setCanvasSize(this.width * 16, this.height * 16);
        Font font = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.setXscale(0, this.width);
        StdDraw.setYscale(0, this.height);
        StdDraw.clear(Color.BLACK);
        StdDraw.enableDoubleBuffering();

        //TODO: Initialize random number generator
    }

    public String generateRandomString(int n) {
        //TODO: Generate random string of letters of length n
        String tot = "";
        for (int i  = 0; i < n; i++) {
            int t = rand.nextInt(26);
            char to = CHARACTERS[t];
            tot += to;
        }
        return tot;
    }

    public void drawFrame(String s) {
        //TODO: Take the string and display it in the center of the screen
        //TODO: If game is not over, display relevant game information at the top of the screen
        StdDraw.clear(Color.black);
        StdDraw.setPenColor(Color.WHITE);
        Font f = new Font("Arial", Font.BOLD, 30);
        StdDraw.setFont(f);
        StdDraw.text(20, 20, s);
        StdDraw.show();
    }

    public void flashSequence(String letters) {
        //TODO: Display each character in letters, making sure to blank the screen between letters
        for (int i = 0; i < letters.length(); i++) {
            String c = letters.substring(i, i + 1);
            this.drawFrame(c);
            StdDraw.pause(1000);
            StdDraw.clear(Color.black);
            StdDraw.show();
            StdDraw.pause(500);
        }
    }

    public String solicitNCharsInput(int n) {
        //TODO: Read n letters of player input
        String sum = "";
        for (int i = 0; i < n; i++) {
            if (StdDraw.hasNextKeyTyped()) {
                sum += StdDraw.nextKeyTyped();
                drawFrame(sum);
            }
            else {
                i--;
            }
        }
        return sum;
    }

    public void startGame() {
        //TODO: Set any relevant variables before the game starts

        round = 1;
        gameOver = false;
        playerTurn = false;
        while (!gameOver) {
            drawFrame("Round: " + round);
            StdDraw.pause(1000);
            StdDraw.clear(Color.black);
            StdDraw.pause(500);
            String s = generateRandomString(round);
            flashSequence(s);
            playerTurn = true;
            String a = solicitNCharsInput(round);
            if (s.equals(a)) {
                int r = rand.nextInt(7);
                drawFrame(ENCOURAGEMENT[r]);
                round++;
            } else {
                gameOver = true;
            }
        }
        drawFrame("Game Over! You made it to round: " + round);
        //TODO: Establish Engine loop
    }

}
