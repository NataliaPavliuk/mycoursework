package pacman.entries.pacman;
import pacman.controllers.Controller;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import java.util.*;


public class RandomMovePacMan extends Controller<MOVE>
{
	private MOVE myMove=MOVE.STAY;


	public MOVE getMove(Game game, long timeDue)
	{


			MOVE[] moves = game.getPossibleMoves(game.getPacmanNodeIndex(),game.getPacmanLastMove());
			return moves[new Random().nextInt(moves.length)];



	}


}