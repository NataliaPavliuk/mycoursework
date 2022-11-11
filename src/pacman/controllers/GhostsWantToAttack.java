package pacman.controllers;

import java.util.EnumMap;
import java.util.Random;
import pacman.controllers.Controller;
import pacman.game.Game;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;

import static pacman.game.Constants.*;

public final class GhostsWantToAttack extends Controller<EnumMap<GHOST,MOVE>>
{
	private final Random rnd=new Random();
	private final EnumMap<GHOST,MOVE> myMoves=new EnumMap<GHOST,MOVE>(GHOST.class);

	public EnumMap<GHOST,MOVE> getMove(Game game,long timeDue)
	{		
		myMoves.clear();
		
		for(GHOST ghost : GHOST.values())
			if(game.doesGhostRequireAction(ghost))
			{
				rnd.nextFloat();
				myMoves.put(ghost,game.getApproximateNextMoveTowardsTarget(game.getGhostNodeIndex(ghost),
						game.getPacmanNodeIndex(),game.getGhostLastMove(ghost),DM.PATH));
			}

		return myMoves;
	}
}