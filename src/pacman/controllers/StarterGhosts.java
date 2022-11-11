package pacman.controllers;

import java.util.EnumMap;
import java.util.Random;
import pacman.controllers.Controller;
import pacman.game.Game;

import static pacman.game.Constants.*;

public final class StarterGhosts extends Controller<EnumMap<GHOST,MOVE>>
{	
	private final static float CONSISTENCY=0.9f;
	private final static int PILL_PROXIMITY=15;
	
	Random rnd=new Random();
	EnumMap<GHOST,MOVE> myMoves=new EnumMap<GHOST,MOVE>(GHOST.class);
	
	public EnumMap<GHOST,MOVE> getMove(Game game,long timeDue)
	{
		for(GHOST ghost : GHOST.values())
		{			
			if(game.doesGhostRequireAction(ghost))
			{
				if(game.getGhostEdibleTime(ghost)>0 || closeToPower(game))
					myMoves.put(ghost,game.getApproximateNextMoveAwayFromTarget(game.getGhostNodeIndex(ghost),
							game.getPacmanNodeIndex(),game.getGhostLastMove(ghost),DM.PATH));
				else 
				{
					if(rnd.nextFloat()<CONSISTENCY)
						myMoves.put(ghost,game.getApproximateNextMoveTowardsTarget(game.getGhostNodeIndex(ghost),
								game.getPacmanNodeIndex(),game.getGhostLastMove(ghost),DM.PATH));
					else
					{					
						MOVE[] possibleMoves=game.getPossibleMoves(game.getGhostNodeIndex(ghost),game.getGhostLastMove(ghost));
						myMoves.put(ghost,possibleMoves[rnd.nextInt(possibleMoves.length)]);
					}
				}
			}
		}

		return myMoves;
	}

	private boolean closeToPower(Game game)
    {
    	int[] powerPills=game.getPowerPillIndex();
    	
    	for(int i=0;i<powerPills.length;i++)
    		if(game.isPowerPillAvailable(i) && game.getShortestPathDistance(powerPills[i],game.getPacmanNodeIndex())<PILL_PROXIMITY)
    			return true;

        return false;
    }
}