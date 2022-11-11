package pacman.controllers;

import pacman.controllers.Controller;
import pacman.game.Game;

import static pacman.game.Constants.*;

public class NearestPillPacMan extends Controller<MOVE>
{	

	public MOVE getMove(Game game,long timeDue)
	{		
		int currentNodeIndex=game.getPacmanNodeIndex();

		int[] activePills=game.getActivePillsIndex();

		int[] activePowerPills=game.getActivePowerPillsIndex();

		int[] targetNodeIndices=new int[activePills.length+activePowerPills.length];

		System.arraycopy(activePills, 0, targetNodeIndices, 0, activePills.length);

		System.arraycopy(activePowerPills, 0, targetNodeIndices, activePills.length, activePowerPills.length);

		return game.getNextMoveTowardsTarget(game.getPacmanNodeIndex(),game.getClosestNodeIndexFromNodeIndex(currentNodeIndex,targetNodeIndices,DM.PATH),DM.PATH);
	}
}