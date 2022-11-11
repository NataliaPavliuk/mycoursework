package pacman.controllers;

import java.util.ArrayList;
import pacman.controllers.Controller;
import pacman.game.Game;

import static pacman.game.Constants.*;

public class StarterPacMan extends Controller<MOVE>
{	
	private static final int MIN_DISTANCE=20;	//if a ghost is this close, run away
	
	public MOVE getMove(Game game,long timeDue)
	{			
		int current=game.getPacmanNodeIndex();

		for(GHOST ghost : GHOST.values())
			if(game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost)==0)
				if(game.getShortestPathDistance(current,game.getGhostNodeIndex(ghost))<MIN_DISTANCE)
					return game.getNextMoveAwayFromTarget(game.getPacmanNodeIndex(),game.getGhostNodeIndex(ghost),DM.PATH);

		int minDistance=Integer.MAX_VALUE;
		GHOST minGhost=null;		
		
		for(GHOST ghost : GHOST.values())
			if(game.getGhostEdibleTime(ghost)>0)
			{
				int distance=game.getShortestPathDistance(current,game.getGhostNodeIndex(ghost));
				
				if(distance<minDistance)
				{
					minDistance=distance;
					minGhost=ghost;
				}
			}
		
		if(minGhost!=null)
			return game.getNextMoveTowardsTarget(game.getPacmanNodeIndex(),game.getGhostNodeIndex(minGhost),DM.PATH);

		int[] pills=game.getPillIndex();
		int[] powerPills=game.getPowerPillIndex();
		
		ArrayList<Integer> targets=new ArrayList<Integer>();
		
		for(int i=0;i<pills.length;i++)
			if(game.isPillAvailable(i))
				targets.add(pills[i]);
		
		for(int i=0;i<powerPills.length;i++)
			if(game.isPowerPillAvailable(i))
				targets.add(powerPills[i]);				
		
		int[] targetsArray=new int[targets.size()];
		
		for(int i=0;i<targetsArray.length;i++)
			targetsArray[i]=targets.get(i);

		return game.getNextMoveTowardsTarget(current,game.getClosestNodeIndexFromNodeIndex(current,targetsArray,DM.PATH),DM.PATH);
	}
}























