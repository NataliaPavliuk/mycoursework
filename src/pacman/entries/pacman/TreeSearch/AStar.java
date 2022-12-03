package pacman.entries.pacman.TreeSearch;
import pacman.controllers.Controller;
import pacman.controllers.StarterGhosts;
import pacman.game.Constants;
import pacman.game.Game;
import pacman.game.Constants.MOVE;
import java.util.*;


public class AStar extends Controller<Constants.MOVE> {
    private Controller<EnumMap<Constants.GHOST,MOVE>> ghostController = new StarterGhosts();
    @Override
    public Constants.MOVE getMove(Game game, long timeDue) {
        int current = game.getPacmanNodeIndex();


        for(Constants.GHOST ghost : Constants.GHOST.values())
            if(game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost)==0)
                if(game.getShortestPathDistance(current,game.getGhostNodeIndex(ghost))<5)
                    return game.getNextMoveAwayFromTarget(game.getPacmanNodeIndex(),game.getGhostNodeIndex(ghost), Constants.DM.PATH);

        ArrayList<PathClass> pathList = new ArrayList<>();
        int totalPills = game.getNumberOfPills() + game.getNumberOfPowerPills();
        int countOfPillsCollected = game.getNumberOfActivePills() + game.getNumberOfActivePowerPills();
        int pillMultiplier = 2;
        int scoreMultiplier = 1;

        MOVE theBestMove = MOVE.STAY;
        MOVE [] moves =  game.getPossibleMoves(current, game.getPacmanLastMove());
        for(MOVE move: moves) {
            Game copy = game.copy();
            copy.advanceGame(move, ghostController.getMove(copy, timeDue));
            PathClass path = new PathClass(move, copy.getScore()*scoreMultiplier + (totalPills - countOfPillsCollected)*pillMultiplier, copy);
            pathList.add(path);
        }

        while(!pathList.isEmpty()) {
            if(System.currentTimeMillis() >= timeDue)
                return theBestMove;

            int best = 0;
            for(int i = 1; i < pathList.size(); i++) {
                if (pathList.get(i).score > pathList.get(best).score){
                    best = i;
                }
            }

            PathClass currentPath = pathList.remove(best);
            theBestMove = currentPath.originalMoves;
            int next = currentPath.game.getPacmanNodeIndex();
            MOVE [] nextMoves =  currentPath.game.getPossibleMoves(next, currentPath.game.getPacmanLastMove());
            for(MOVE move: nextMoves) {
                Game copy = game.copy();
                copy.advanceGame(move, ghostController.getMove(copy, timeDue));
                PathClass path;
                int eaten = copy.getNumberOfActivePills() + copy.getNumberOfActivePowerPills();
                path = new PathClass(currentPath.myMove, currentPath.originalMoves, move, copy.getScore()*scoreMultiplier + (totalPills - eaten)*pillMultiplier, copy);
                pathList.add(path);
            }

        }
        return theBestMove;
    }
}