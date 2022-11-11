package pacman.game;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.EnumMap;

import static pacman.game.Constants.*;

public final class GameView extends JComponent {
    private static final String imageFileName = "";
    private final Game game;
    private final Images images;
    private MOVE lastPacManMove;
    private int time;
    private GameFrame frame;
    private Graphics graphics;
    private BufferedImage offscreen;

    public GameView(Game game) {
        this.game = game;

        images = new Images();
        lastPacManMove = game.getPacmanLastMove();
        time = game.getTotalTime();
    }


    public void paintComponent(Graphics g) {
        time = game.getTotalTime();

        if (offscreen == null) {
            offscreen = (BufferedImage) createImage(this.getPreferredSize().width, this.getPreferredSize().height);
            graphics = offscreen.getGraphics();
        }

        drawLabyrinth();
        drawPills();
        drawPowerPills();
        drawPacMan();
        drawGhosts();
        drawLives();
        drawInfoOfGame();

        if (game.gameOver())
            drawGameOver();

        g.drawImage(offscreen, 0, 0, this);
    }

    private void drawLabyrinth() {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, GV_WIDTH * MAG, GV_HEIGHT * MAG + 20);

        graphics.drawImage(images.getMaze(game.getLabyrinthIndex()), 2, 6, null);
    }

    private void drawPills() {
        int[] pillIndex = game.getPillIndex();

        graphics.setColor(Color.BLUE);

        for (int i = 0; i < pillIndex.length; i++)
            if (game.isPillAvailable(i))
                graphics.fillOval(game.getNodeXCood(pillIndex[i]) * MAG + 4, game.getNodeYCood(pillIndex[i]) * MAG + 8, 4, 4);
    }

    private void drawPowerPills() {
        int[] powerPillIndex = game.getPowerPillIndex();

        graphics.setColor(Color.blue);

        for (int i = 0; i < powerPillIndex.length; i++)
            if (game.isPowerPillAvailable(i))
                graphics.fillOval(game.getNodeXCood(powerPillIndex[i]) * MAG + 1, game.getNodeYCood(powerPillIndex[i]) * MAG + 5, 10, 10);
    }

    private void drawPacMan() {
        int pacManNode = game.getPacmanNodeIndex();

        MOVE tempLastPacManMove = game.getPacmanLastMove();

        if (tempLastPacManMove != MOVE.STAY)
            lastPacManMove = tempLastPacManMove;

        graphics.drawImage(images.getPacMan(lastPacManMove, time), game.getNodeXCood(pacManNode) * MAG - 1, game.getNodeYCood(pacManNode) * MAG + 3, null);
    }

    private void drawGhosts() {
        for (GHOST ghostType : GHOST.values()) {
            int currentNodeIndex = game.getGhostNodeIndex(ghostType);
            int nodeXCood = game.getNodeXCood(currentNodeIndex);
            int nodeYCood = game.getNodeYCood(currentNodeIndex);

            if (game.getGhostEdibleTime(ghostType) > 0) {
                if (game.getGhostEdibleTime(ghostType) < EDIBLE_ALERT && ((time % 6) / 3) == 0)
                    graphics.drawImage(images.getEdibleGhost(time), nodeXCood * MAG - 1, nodeYCood * MAG + 3, null);
                else
                    graphics.drawImage(images.getEdibleGhost(time), nodeXCood * MAG - 1, nodeYCood * MAG + 3, null);
            } else {
                int numberOfType = ghostType.ordinal();

                if (game.getGhostLairTime(ghostType) > 0)
                    graphics.drawImage(images.getGhost(ghostType, game.getGhostLastMove(ghostType), time), nodeXCood * MAG - 1 + (numberOfType * 5), nodeYCood * MAG + 3, null);
                else
                    graphics.drawImage(images.getGhost(ghostType, game.getGhostLastMove(ghostType), time), nodeXCood * MAG - 1, nodeYCood * MAG + 3, null);
            }
        }
    }

    private void drawLives() {
        for (int i = 0; i < game.getPacmanNumberOfLivesRemaining() - 1; i++)
            graphics.drawImage(images.getPacManForExtraLives(), 610 - (30 * i * 3) / 2, 751, null);
    }

    private void drawInfoOfGame() {
        graphics.setColor(Color.WHITE);
        graphics.drawString("S: ", 4, 771);
        graphics.drawString("" + game.getScore(), 16, 771);
        graphics.drawString("L: ", 78, 771);
        graphics.drawString("" + (game.getLevel() + 1), 90, 771);
        graphics.drawString("T: ", 116, 771);
        graphics.drawString("" + game.getLevelTime(), 129, 771);
    }

    private void drawGameOver() {
        graphics.setColor(Color.BLACK);
        graphics.drawString("Game Over", 300, 350);
    }


    public Dimension getPreferredSize() {
        return new Dimension(GV_WIDTH * MAG, GV_HEIGHT * MAG + 20);
    }

    public GameView showGame() {
        this.frame = new GameFrame(this);

        try {
            Thread.sleep(2000);
        } catch (Exception ignored) {
        }

        return this;
    }

    public GameFrame getFrame() {
        return frame;
    }

    public class GameFrame extends JFrame {
        public GameFrame(JComponent comp) {
            getContentPane().add(BorderLayout.CENTER, comp);
            pack();
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            this.setLocation((int) (screen.getWidth() * 3 / 8), (int) (screen.getHeight() * 1 / 8));
            this.setVisible(true);
            this.setResizable(false);
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            repaint();
        }
    }

    public class Images {
        private EnumMap<MOVE, BufferedImage[]> pacMan;
        private EnumMap<GHOST, EnumMap<MOVE, BufferedImage[]>> ghosts;
        private BufferedImage[] edibleGhosts, labyrinth;

        public Images() {
            pacMan = new EnumMap<MOVE, BufferedImage[]>(MOVE.class);

            pacMan.put(MOVE.UP, new BufferedImage[]{loadImage("mspacman-up-normal.png"),
                    loadImage("mspacman-up-open.png"),
                    loadImage("mspacman-up-closed.png")});

            pacMan.put(MOVE.RIGHT, new BufferedImage[]{loadImage("mspacman-right-normal.png"),
                    loadImage("mspacman-right-open.png"),
                    loadImage("mspacman-right-closed.png")});

            pacMan.put(MOVE.DOWN, new BufferedImage[]{loadImage("mspacman-down-normal.png"),
                    loadImage("mspacman-down-open.png"),
                    loadImage("mspacman-down-closed.png")});

            pacMan.put(MOVE.LEFT, new BufferedImage[]{loadImage("mspacman-left-normal.png"),
                    loadImage("mspacman-left-open.png"),
                    loadImage("mspacman-left-closed.png")});

            ghosts = new EnumMap<GHOST, EnumMap<MOVE, BufferedImage[]>>(GHOST.class);

            ghosts.put(GHOST.PIRAT, new EnumMap<MOVE, BufferedImage[]>(MOVE.class));
            ghosts.get(GHOST.PIRAT).put(MOVE.UP, new BufferedImage[]{loadImage("police-right.png"), loadImage("police-right.png")});
            ghosts.get(GHOST.PIRAT).put(MOVE.RIGHT, new BufferedImage[]{loadImage("police-right.png"), loadImage("police-right.png")});
            ghosts.get(GHOST.PIRAT).put(MOVE.DOWN, new BufferedImage[]{loadImage("police-right.png"), loadImage("police-right.png")});
            ghosts.get(GHOST.PIRAT).put(MOVE.LEFT, new BufferedImage[]{loadImage("police-right.png"), loadImage("police-right.png")});

            ghosts.put(GHOST.POLICEMAN, new EnumMap<MOVE, BufferedImage[]>(MOVE.class));
            ghosts.get(GHOST.POLICEMAN).put(MOVE.UP, new BufferedImage[]{loadImage("ninja.png"), loadImage("ninja.png")});
            ghosts.get(GHOST.POLICEMAN).put(MOVE.RIGHT, new BufferedImage[]{loadImage("ninja.png"), loadImage("ninja.png")});
            ghosts.get(GHOST.POLICEMAN).put(MOVE.DOWN, new BufferedImage[]{loadImage("ninja.png"), loadImage("ninja.png")});
            ghosts.get(GHOST.POLICEMAN).put(MOVE.LEFT, new BufferedImage[]{loadImage("ninja.png"), loadImage("ninja.png")});

            ghosts.put(GHOST.NINJA, new EnumMap<MOVE, BufferedImage[]>(MOVE.class));
            ghosts.get(GHOST.NINJA).put(MOVE.UP, new BufferedImage[]{loadImage("army.png"), loadImage("army.png")});
            ghosts.get(GHOST.NINJA).put(MOVE.RIGHT, new BufferedImage[]{loadImage("army.png"), loadImage("army.png")});
            ghosts.get(GHOST.NINJA).put(MOVE.DOWN, new BufferedImage[]{loadImage("army.png"), loadImage("army.png")});
            ghosts.get(GHOST.NINJA).put(MOVE.LEFT, new BufferedImage[]{loadImage("army.png"), loadImage("army.png")});

            ghosts.put(GHOST.ARMY, new EnumMap<MOVE, BufferedImage[]>(MOVE.class));
            ghosts.get(GHOST.ARMY).put(MOVE.UP, new BufferedImage[]{loadImage("pirat.png"), loadImage("pirat.png")});
            ghosts.get(GHOST.ARMY).put(MOVE.RIGHT, new BufferedImage[]{loadImage("pirat.png"), loadImage("pirat.png")});
            ghosts.get(GHOST.ARMY).put(MOVE.DOWN, new BufferedImage[]{loadImage("pirat.png"), loadImage("pirat.png")});
            ghosts.get(GHOST.ARMY).put(MOVE.LEFT, new BufferedImage[]{loadImage("pirat.png"), loadImage("pirat.png")});

            edibleGhosts = new BufferedImage[2];
            edibleGhosts[0] = loadImage("edible.png");
            edibleGhosts[1] = loadImage("edible.png");

            labyrinth = new BufferedImage[4];
            for (int i = 0; i < labyrinth.length; i++)
                labyrinth[i] = loadImage(labyrinthNames[i]);
        }

        public BufferedImage getPacMan(MOVE move, int time) {
            return pacMan.get(move)[(time % 6) / 2];
        }

        public BufferedImage getPacManForExtraLives() {
            return pacMan.get(MOVE.RIGHT)[0];
        }

        public BufferedImage getGhost(GHOST ghost, MOVE move, int time) {
            if (move == MOVE.STAY)
                return ghosts.get(ghost).get(MOVE.UP)[(time % 6) / 3];
            else
                return ghosts.get(ghost).get(move)[(time % 6) / 3];
        }

        public BufferedImage getEdibleGhost(int time) {
            return edibleGhosts[(time % 6) / 3];
        }

        public BufferedImage getMaze(int mazeIndex) {
            return labyrinth[mazeIndex];
        }

        private BufferedImage loadImage(String fileName) {
            BufferedImage image = null;

            try {
                image = ImageIO.read(new File(pathImages + System.getProperty("file.separator") + fileName));
            } catch (IOException e) {
                e.printStackTrace();
            }

            return image;
        }
    }
}