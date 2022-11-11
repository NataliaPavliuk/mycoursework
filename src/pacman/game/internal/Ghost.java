package pacman.game.internal;

import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;

public final class Ghost
{
	public int currentNodeIndex, edibleTime, denTime;
	public GHOST type;
	public MOVE lastMove;

	public Ghost(GHOST type, int currentNodeIndex, int edibleTime, int denTime, MOVE lastMove)
	{
		this.type=type;
		this.currentNodeIndex = currentNodeIndex;
		this.edibleTime = edibleTime;
		this.denTime = denTime;
		this.lastMove = lastMove;
	}

	public Ghost copy()
	{
		return new Ghost(type, currentNodeIndex, edibleTime, denTime, lastMove);
	}
}