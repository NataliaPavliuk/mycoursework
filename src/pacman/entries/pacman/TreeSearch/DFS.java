package pacman.entries.pacman.TreeSearch;
import pacman.controllers.Controller;
import pacman.controllers.StarterGhosts;
import pacman.game.Constants;
import pacman.game.Game;
import pacman.game.Constants.MOVE;
import pacman.Main;

public class DFS extends Controller<Constants.MOVE> {
    @Override

    public Constants.MOVE getMove(Game game, long timeDue) {
        int limit = 0;
        int best = 0;
        MOVE bestMove = MOVE.STAY;
        int depth = 0;
        for (MOVE m : game.getPossibleMoves(game.getPacmanNodeIndex(), game.getPacmanLastMove())) {
            Game g = game.copy();
            g.advanceGame(m, Main.getMove(g, timeDue));
            int value = dfsRecursive(g, timeDue, depth, limit);
            if (value > best) {
                best = value;
                bestMove = m;
            }
        }
        limit++;
        return bestMove;

    }

    public int dfsRecursive(Game gameState, long timeDue, int counter, int limit) {
        limit++;
        int best = 0;
        int value= gameState.getScore();
        if (counter >= 99) { best = value; return best;}
        if (gameState.getPacmanNumberOfLivesRemaining() == 0) return -100;
        if (gameState.getNumberOfActivePills()==0) {
            value = gameState.getScore();
            return value;
        }
        MOVE[] moves = gameState.getPossibleMoves(gameState.getPacmanNodeIndex(), gameState.getPacmanLastMove());
        for ( MOVE move: moves) {
            Game copyGame = gameState.copy();
            copyGame.advanceGame(move, new StarterGhosts().getMove(copyGame, timeDue));

            value =  dfsRecursive(copyGame, timeDue, counter+1, limit);
            if (value > best) best = value;
        }
        return best;
    }


}