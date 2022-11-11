//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package pacman.entries.pacman.TreeSearch;

import java.util.ArrayList;
import pacman.game.Game;
import pacman.game.Constants.MOVE;

class PathClass {
    Game game;
    int score;
    ArrayList<MOVE> myMove;
    MOVE originalMoves;

    PathClass(MOVE m, int s, Game g) {
        this.myMove = new ArrayList();
        this.myMove.add(m);
        this.originalMoves = m;
        this.score = s;
        this.game = g;
    }

    PathClass(ArrayList<MOVE> routes, MOVE om, MOVE m, int s, Game g) {
        this.myMove = new ArrayList(routes);
        this.originalMoves = om;
        this.myMove.add(m);
        this.score = s;
        this.game = g;
    }
}
