//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package pacman.entries.pacman.TreeSearch;

import java.util.ArrayList;
import pacman.game.Game;
import pacman.game.Constants.MOVE;

class NODE {
    private Game gameState;
    private MOVE movement;
    public ArrayList<NODE> children = new ArrayList();

    public NODE(Game g, MOVE m) {
        this.gameState = g;
        this.movement = m;
    }

    public Game getGame() {
        return this.gameState;
    }

    public MOVE getMovement() {
        return this.movement;
    }
}