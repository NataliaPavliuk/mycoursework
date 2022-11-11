package pacman.game.internal;

import pacman.game.Constants.MOVE;

public final class PacMan
{
	public int currentNodeIndex, numberOfLivesRemaining;
	public MOVE lastMove;
	public boolean hasReceivedExtraLife;
	
	public PacMan(int currentNodeIndex, MOVE lastMove, int numberOfLivesRemaining, boolean hasReceivedExtraLife)
	{
		this.currentNodeIndex = currentNodeIndex;
		this.lastMove = lastMove;
		this.numberOfLivesRemaining = numberOfLivesRemaining;
		this.hasReceivedExtraLife = hasReceivedExtraLife;
	}
	
	public PacMan copy()
	{
		return new PacMan(currentNodeIndex, lastMove, numberOfLivesRemaining, hasReceivedExtraLife);
	}
}