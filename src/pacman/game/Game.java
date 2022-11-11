package pacman.game;

import java.util.BitSet;
import java.util.EnumMap;
import java.util.Random;
import java.util.Map.Entry;
import pacman.game.Constants.DM;
import pacman.game.Constants.MOVE;
import pacman.game.internal.Ghost;
import pacman.game.internal.Labyrinth;
import pacman.game.internal.Node;
import pacman.game.internal.PacMan;
import pacman.game.internal.PathsCache;

import static pacman.game.Constants.*;

public final class Game
{

	private BitSet pills, powerPills;
	private int labyrinthIndex, levelIndex, levelTime, totalTime, score, ghostEatMultiplier, timeOfLastGlobalReversal;
	private boolean gameOver, pacmanWasEaten, pillWasEaten, powerPillWasEaten;
	private EnumMap<GHOST,Boolean> ghostsEaten;
	private PacMan pacMan;
	private EnumMap<GHOST, Ghost> ghosts;

	private static Labyrinth[] labyrinth=new Labyrinth[LABYRINTHS_NUMBER];;
	
	private Labyrinth currentLabyrinth;
	
	static 
	{
		for(int i = 0; i< labyrinth.length; i++)
			labyrinth[i]=new Labyrinth(i);
	}
	
	public static PathsCache[] pathsCaches =new PathsCache[LABYRINTHS_NUMBER];
	
	static 
	{
		for(int i = 0; i< labyrinth.length; i++)
		{
			pathsCaches[i]=new PathsCache(i);
		}
	}
	
	private Random random;
	private long seed;	

	public Game(long seed)
	{		
		this.seed=seed;
		random =new Random(seed);
		
		initialize(0);
	}

	public Game(long seed,int initialLabyrinth)
	{						
		this.seed=seed;
		random =new Random(seed);
		
		initialize(initialLabyrinth);
	}

	private Game(){}

	private void initialize(int initialLabyrinth)
	{
		labyrinthIndex =initialLabyrinth;
		score= levelTime = levelIndex =totalTime=0;
		ghostEatMultiplier=1;
		gameOver=false;
		timeOfLastGlobalReversal=-1;		
		pacmanWasEaten=false;
		pillWasEaten=false;
		powerPillWasEaten=false;
		
		ghostsEaten=new EnumMap<GHOST,Boolean>(GHOST.class);
		
		for(GHOST ghost : GHOST.values())
			ghostsEaten.put(ghost,false);
		
		setPills(currentLabyrinth = labyrinth[labyrinthIndex]);
		initializeGhosts();
		
		pacMan =new PacMan(currentLabyrinth.initialPacManNodeIndex,MOVE.LEFT, NUMBER_OF_LIVES,false);
	}

	private void newLevelReset()
	{
		labyrinthIndex =++labyrinthIndex % LABYRINTHS_NUMBER;
		levelIndex++;
		currentLabyrinth = labyrinth[labyrinthIndex];
		
		levelTime =0;
		ghostEatMultiplier=1;
			
		setPills(currentLabyrinth);
		levelReset();
	}

	private void levelReset()
	{
		ghostEatMultiplier=1;
		
		initializeGhosts();
		
		pacMan.currentNodeIndex= currentLabyrinth.initialPacManNodeIndex;
		pacMan.lastMove =MOVE.LEFT;
	}

	private void setPills(Labyrinth labyrinth1)
	{
		pills=new BitSet(currentLabyrinth.pillIndices.length);
		pills.set(0, currentLabyrinth.pillIndices.length);
		powerPills=new BitSet(currentLabyrinth.powerPillIndices.length);
		powerPills.set(0, currentLabyrinth.powerPillIndices.length);
	}

	private void initializeGhosts()
	{
		ghosts=new EnumMap<GHOST, Ghost>(GHOST.class);
					
		for(GHOST ghostType : GHOST.values())
			ghosts.put(ghostType,new Ghost(ghostType, currentLabyrinth.lairNodeIndex,0,
					(int)(ghostType.initialLairTime*(Math.pow(LAIR_DECREASE, levelIndex % LEVEL_RESET_DECREASE))),MOVE.STAY));
	}

	public Game copy()
	{
		Game copy=new Game();
			
		copy.seed=seed;
		copy.random =new Random(seed);
		copy.currentLabyrinth = currentLabyrinth;
		copy.pills=(BitSet)pills.clone();
		copy.powerPills=(BitSet)powerPills.clone();		
		copy.labyrinthIndex = labyrinthIndex;
		copy.levelIndex = levelIndex;
		copy.levelTime = levelTime;
		copy.totalTime=totalTime;
		copy.score=score;
		copy.ghostEatMultiplier=ghostEatMultiplier;
		copy.gameOver=gameOver;
		copy.timeOfLastGlobalReversal=timeOfLastGlobalReversal;		
		copy.pacmanWasEaten=pacmanWasEaten;
		copy.pillWasEaten=pillWasEaten;
		copy.powerPillWasEaten=powerPillWasEaten;		
		copy.pacMan = pacMan.copy();
		
		copy.ghostsEaten=new EnumMap<GHOST,Boolean>(GHOST.class);
		copy.ghosts=new EnumMap<GHOST,Ghost>(GHOST.class);
		
		for(GHOST ghostType : GHOST.values())
		{
			copy.ghosts.put(ghostType,ghosts.get(ghostType).copy());
			copy.ghostsEaten.put(ghostType,ghostsEaten.get(ghostType));
		}
			
		return copy;	
	}

	public void advanceGame(MOVE pacManMove,EnumMap<GHOST,MOVE> ghostMoves)
	{		
		updatePacMan(pacManMove);
		updateGhosts(ghostMoves);	
		updateGame();
	}

	public void updatePacMan(MOVE pacManMove)
	{
		updateThisPacMan(pacManMove);
		eatPill();
		eatPowerPill();
	}

	public void updateGhosts(EnumMap<GHOST,MOVE> ghostMoves)
	{
		ghostMoves= completeGhostMoves(ghostMoves);
		
		if(!reverseGhosts(ghostMoves,false))
			updateThisGhosts(ghostMoves);
	}

	public void updateGame()
	{
		repast();
		updateLairTimes();
		updatePacManExtraLife();

		totalTime++;
		levelTime++;
		
		checkLevelState();
	}
	

	private void updateLairTimes()
	{
		for(Ghost ghost : ghosts.values())
			if(ghost.denTime >0)
				if(--ghost.denTime ==0)
					ghost.currentNodeIndex= currentLabyrinth.initialGhostNodeIndex;
	}

	private void updatePacManExtraLife()
	{
		if(!pacMan.hasReceivedExtraLife && score>=EXTRA_LIFE_SCORE)	//award 1 extra life at 10000 points
		{
			pacMan.hasReceivedExtraLife=true;
			pacMan.numberOfLivesRemaining++;
		}
	}

	private void updateThisPacMan(MOVE move)
	{
		pacMan.lastMove = correctPacManDir(move);
		pacMan.currentNodeIndex= pacMan.lastMove == MOVE.STAY ? pacMan.currentNodeIndex :
			currentLabyrinth.graph[pacMan.currentNodeIndex].neighbourhood.get(pacMan.lastMove);
	}

	private MOVE correctPacManDir(MOVE direction)
	{
		Node node= currentLabyrinth.graph[pacMan.currentNodeIndex];

		if(node.neighbourhood.containsKey(direction))
			return direction;
		else
		{
			if(node.neighbourhood.containsKey(pacMan.lastMove))
				return pacMan.lastMove;
			else
				return MOVE.STAY;
		}
	}

	private void updateThisGhosts(EnumMap<GHOST,MOVE> moves)
	{
		for(Entry<GHOST,MOVE> entry : moves.entrySet())
		{
			Ghost ghost=ghosts.get(entry.getKey());

			if(ghost.denTime ==0)
			{
				if(ghost.edibleTime==0 || ghost.edibleTime% GHOST_SPEED_DECREASE !=0)
				{
					ghost.lastMove = checkGhostDir(ghost,entry.getValue());
					moves.put(entry.getKey(), ghost.lastMove);
					ghost.currentNodeIndex= currentLabyrinth.graph[ghost.currentNodeIndex].neighbourhood.get(ghost.lastMove);
				}
			}
		}
	}
	
	private EnumMap<GHOST,MOVE> completeGhostMoves(EnumMap<GHOST,MOVE> moves)
	{
		if(moves==null)
		{
			moves=new EnumMap<GHOST,MOVE>(GHOST.class);
			
			for(GHOST ghostType : GHOST.values())
				moves.put(ghostType,ghosts.get(ghostType).lastMove);
		}
		
		if(moves.size()< NUMBER_OF_GHOSTS)
			for(GHOST ghostType : GHOST.values())
				if(!moves.containsKey(ghostType))
					moves.put(ghostType,MOVE.STAY);
		
		return moves;
	}

	private MOVE checkGhostDir(Ghost ghost, MOVE direction)
	{
		Node node= currentLabyrinth.graph[ghost.currentNodeIndex];

		if(node.neighbourhood.containsKey(direction) && direction!=ghost.lastMove.opposite())
			return direction;
		else
		{
			if(node.neighbourhood.containsKey(ghost.lastMove))
				return ghost.lastMove;
			else
			{
				MOVE[] moves=node.allPossibleMoves.get(ghost.lastMove);
				return moves[random.nextInt(moves.length)];
			}
		}
	}

	private void eatPill()
	{
		pillWasEaten=false;
		
		int pillIndex= currentLabyrinth.graph[pacMan.currentNodeIndex].pillIndex;

		if(pillIndex>=0 && pills.get(pillIndex))
		{
			score+=PILL;
			pills.clear(pillIndex);
			pillWasEaten=true;
		}
	}

	private void eatPowerPill()
	{
		powerPillWasEaten=false;	
		
		int powerPillIndex= currentLabyrinth.graph[pacMan.currentNodeIndex].powerPillIndex;
		
		if(powerPillIndex>=0 && powerPills.get(powerPillIndex))
		{
			score+=POWER_PILL;
			ghostEatMultiplier=1;
			powerPills.clear(powerPillIndex);
			
			int newEdibleTime=(int)(EDIBLE_TIME*(Math.pow(EDIBLE_TIME_DECREASE, levelIndex % LEVEL_RESET_DECREASE)));
			
			for(Ghost ghost : ghosts.values())
				if(ghost.denTime ==0)
					ghost.edibleTime=newEdibleTime;
				else
					ghost.edibleTime=0;
			
			powerPillWasEaten=true;
		}
	}
	
	private boolean reverseGhosts(EnumMap<GHOST,MOVE> moves, boolean force)
	{
		boolean reversed=false;		
		boolean globalReverse=false;
			
		if(Math.random()<GHOST_REVERSAL)
			globalReverse=true;
		
		for(Entry<GHOST,MOVE> entry : moves.entrySet())
		{
			Ghost ghost=ghosts.get(entry.getKey());
		
			if(levelTime >1 && ghost.denTime ==0 && ghost.lastMove !=MOVE.STAY)
			{
				if(force || (powerPillWasEaten || globalReverse))
				{
					ghost.lastMove =ghost.lastMove.opposite();
					ghost.currentNodeIndex= currentLabyrinth.graph[ghost.currentNodeIndex].neighbourhood.get(ghost.lastMove);
					reversed=true;
					timeOfLastGlobalReversal = totalTime;
				}
			}
		}
		
		return reversed;
	}

	private void repast()
	{		
		pacmanWasEaten=false;
		
		for(GHOST ghost : GHOST.values())
			ghostsEaten.put(ghost,false);
		
		for(Ghost ghost : ghosts.values())
		{
			int distance=getShortestPathDistance(pacMan.currentNodeIndex, ghost.currentNodeIndex);
			
			if(distance<=EAT_DISTANCE && distance!=-1)
			{
				if(ghost.edibleTime>0)
				{
					score+=GHOST_EAT_SCORE*ghostEatMultiplier;
					ghostEatMultiplier*=2;
					ghost.edibleTime=0;					
					ghost.denTime =(int)(COMMON_LAIR_TIME*(Math.pow(LAIR_DECREASE, levelIndex % LEVEL_RESET_DECREASE)));
					ghost.currentNodeIndex= currentLabyrinth.lairNodeIndex;
					ghost.lastMove =MOVE.STAY;
					
					ghostsEaten.put(ghost.type, true);
				}
				else
				{
					pacMan.numberOfLivesRemaining--;
					pacmanWasEaten=true;
					
					if(pacMan.numberOfLivesRemaining<=0)
						gameOver=true;
					else
						levelReset();
					
					return;
				}
			}
		}
		
		for(Ghost ghost : ghosts.values())
			if(ghost.edibleTime>0)
				ghost.edibleTime--;
	}

	private void checkLevelState()
	{
		if(totalTime+1>MAX_TIME)
		{
			gameOver=true;
			score+= pacMan.numberOfLivesRemaining*AWARD_LIFE_LEFT;
		}
		else if((pills.isEmpty() && powerPills.isEmpty()) || levelTime >=LEVEL_LIMIT)
			newLevelReset();
	}

	public boolean gameOver()
	{
		return gameOver;
	}

	public Labyrinth getCurrentLabyrinth()
	{
		return currentLabyrinth;
	}

	public int getNodeXCood(int nodeIndex)
	{
		return currentLabyrinth.graph[nodeIndex].x;
	}

	public int getNodeYCood(int nodeIndex)
	{
		return currentLabyrinth.graph[nodeIndex].y;
	}

	public int getLabyrinthIndex()
	{
		return labyrinthIndex;
	}

	public int getLevel()
	{
		return levelIndex;
	}

	public boolean isPillAvailable(int pillIndex)
	{
		return pills.get(pillIndex);
	}

	public boolean isPowerPillAvailable(int powerPillIndex)
	{
		return powerPills.get(powerPillIndex);
	}

	public int[] getPillIndex()
	{
		return currentLabyrinth.pillIndices;
	}

	public int[] getPowerPillIndex()
	{
		return currentLabyrinth.powerPillIndices;
	}

	public int getPacmanNodeIndex()
	{
		return pacMan.currentNodeIndex;
	}

	public MOVE getPacmanLastMove()
	{
		return pacMan.lastMove;
	}

	public int getPacmanNumberOfLivesRemaining()
	{
		return pacMan.numberOfLivesRemaining;
	}

	public int getGhostNodeIndex(GHOST ghostType)
	{
		return ghosts.get(ghostType).currentNodeIndex;
	}

	public MOVE getGhostLastMove(GHOST ghostType)
	{
		return ghosts.get(ghostType).lastMove;
	}

	public int getGhostEdibleTime(GHOST ghostType)
	{
		return ghosts.get(ghostType).edibleTime;
	}

	public int getScore()
	{
		return score;
	}

	public int getLevelTime()
	{
		return levelTime;
	}

	public int getTotalTime()
	{
		return totalTime;
	}

	public int getNumberOfPills()
	{
		return currentLabyrinth.pillIndices.length;
	}

	public int getNumberOfPowerPills()
	{
		return currentLabyrinth.powerPillIndices.length;
	}

	public int getNumberOfActivePills()
	{
		return pills.cardinality();
	}

	public int getNumberOfActivePowerPills()
	{
		return powerPills.cardinality();
	}

	public int getGhostLairTime(GHOST ghostType)
	{
		return ghosts.get(ghostType).denTime;
	}

	public int[] getActivePillsIndex()
	{
		int[] ints=new int[pills.cardinality()];
		
		int index=0;
		
		for(int i = 0; i< currentLabyrinth.pillIndices.length; i++)
			if(pills.get(i))
				ints[index++]= currentLabyrinth.pillIndices[i];
			
		return ints;
	}

	public int[] getActivePowerPillsIndex()
	{
		int[] ints=new int[powerPills.cardinality()];
		
		int index=0;
		
		for(int i = 0; i< currentLabyrinth.powerPillIndices.length; i++)
			if(powerPills.get(i))
				ints[index++]= currentLabyrinth.powerPillIndices[i];
			
		return ints;
	}

	public boolean doesGhostRequireAction(GHOST ghostType)
	{
		return ((isCrossroads(ghosts.get(ghostType).currentNodeIndex) || (ghosts.get(ghostType).lastMove ==MOVE.STAY) && ghosts.get(ghostType).currentNodeIndex== currentLabyrinth.initialGhostNodeIndex)
				&& (ghosts.get(ghostType).edibleTime==0 || ghosts.get(ghostType).edibleTime% GHOST_SPEED_DECREASE !=0));
	}

	public boolean isCrossroads(int nodeIndex)
	{
		return currentLabyrinth.graph[nodeIndex].numberOfNeighbouringNodes >2;
	}

	public MOVE[] getPossibleMoves(int nodeIndex)
	{
		return currentLabyrinth.graph[nodeIndex].allPossibleMoves.get(MOVE.STAY);
	}

	public MOVE[] getPossibleMoves(int nodeIndex,MOVE lastModeMade)
	{
		return currentLabyrinth.graph[nodeIndex].allPossibleMoves.get(lastModeMade);
	}

    public int getNeighbour(int nodeIndex, MOVE moveToBeMade)
    {
    	Integer neighbour= currentLabyrinth.graph[nodeIndex].neighbourhood.get(moveToBeMade);
    	
    	return neighbour==null ? -1 : neighbour;
    }

	public MOVE getMoveToMakeToReachDirectNeighbour(int currentNodeIndex,int neighbourNodeIndex)
	{
		for(MOVE move : MOVE.values())
		{
			if(currentLabyrinth.graph[currentNodeIndex].neighbourhood.containsKey(move)
					&& currentLabyrinth.graph[currentNodeIndex].neighbourhood.get(move)==neighbourNodeIndex)
			{
				return move;
			}
		}
		
		return null;
	}

	public int getShortestPathDistance(int fromNodeIndex,int toNodeIndex)
	{
		if(fromNodeIndex==toNodeIndex)
			return 0;		
		else if(fromNodeIndex<toNodeIndex)
			return currentLabyrinth.shortestPathDistances[((toNodeIndex*(toNodeIndex+1))/2)+fromNodeIndex];
		else
			return currentLabyrinth.shortestPathDistances[((fromNodeIndex*(fromNodeIndex+1))/2)+toNodeIndex];
	}

	public double getEuclideanDistance(int fromNodeIndex,int toNodeIndex)
	{
		return Math.sqrt(Math.pow(currentLabyrinth.graph[fromNodeIndex].x- currentLabyrinth.graph[toNodeIndex].x,2)+Math.pow(currentLabyrinth.graph[fromNodeIndex].y- currentLabyrinth.graph[toNodeIndex].y,2));
	}

	public int getManhattanDistance(int fromNodeIndex,int toNodeIndex)
	{
		return (int)(Math.abs(currentLabyrinth.graph[fromNodeIndex].x- currentLabyrinth.graph[toNodeIndex].x)+Math.abs(currentLabyrinth.graph[fromNodeIndex].y- currentLabyrinth.graph[toNodeIndex].y));
	}

	public double getDistance(int fromNodeIndex,int toNodeIndex,DM distanceMeasure)
	{
		switch(distanceMeasure)
		{
			case PATH: return getShortestPathDistance(fromNodeIndex,toNodeIndex);
			case EUCLID: return getEuclideanDistance(fromNodeIndex,toNodeIndex);
			case MANHATTAN: return getManhattanDistance(fromNodeIndex,toNodeIndex);
		}
		
		return -1;
	}

	public int getClosestNodeIndexFromNodeIndex(int fromNodeIndex,int[] targetNodeIndices,DM distanceMeasure)
	{
		double minDistance=Integer.MAX_VALUE;
		int target=-1;
		
		for(int i=0;i<targetNodeIndices.length;i++)
		{				
			double distance=0;
			
			distance=getDistance(targetNodeIndices[i],fromNodeIndex,distanceMeasure);
					
			if(distance<minDistance)
			{
				minDistance=distance;
				target=targetNodeIndices[i];	
			}
		}
		
		return target;
	}

	public MOVE getNextMoveTowardsTarget(int fromNodeIndex,int toNodeIndex,DM distanceMeasure)
	{
		MOVE move=null;

		double minDistance=Integer.MAX_VALUE;

		for(Entry<MOVE,Integer> entry : currentLabyrinth.graph[fromNodeIndex].neighbourhood.entrySet())
		{
			double distance=getDistance(entry.getValue(),toNodeIndex,distanceMeasure);
								
			if(distance<minDistance)
			{
				minDistance=distance;
				move=entry.getKey();	
			}
		}
		
		return move;
	}

	public MOVE getNextMoveAwayFromTarget(int fromNodeIndex,int toNodeIndex,DM distanceMeasure)
	{
		MOVE move=null;

		double maxDistance=Integer.MIN_VALUE;

		for(Entry<MOVE,Integer> entry : currentLabyrinth.graph[fromNodeIndex].neighbourhood.entrySet())
		{
			double distance=getDistance(entry.getValue(),toNodeIndex,distanceMeasure);
								
			if(distance>maxDistance)
			{
				maxDistance=distance;
				move=entry.getKey();	
			}
		}
		
		return move;
	}

	public MOVE getApproximateNextMoveTowardsTarget(int fromNodeIndex,int toNodeIndex,MOVE lastMoveMade, DM distanceMeasure)
	{
		MOVE move=null;

		double minDistance=Integer.MAX_VALUE;

		for(Entry<MOVE,Integer> entry : currentLabyrinth.graph[fromNodeIndex].allNeighbourhoods.get(lastMoveMade).entrySet())
		{
			double distance=getDistance(entry.getValue(),toNodeIndex,distanceMeasure);
								
			if(distance<minDistance)
			{
				minDistance=distance;
				move=entry.getKey();	
			}
		}
		
		return move;
	}

	public MOVE getApproximateNextMoveAwayFromTarget(int fromNodeIndex,int toNodeIndex,MOVE lastMoveMade, DM distanceMeasure)
	{
		MOVE move=null;

		double maxDistance=Integer.MIN_VALUE;

		for(Entry<MOVE,Integer> entry : currentLabyrinth.graph[fromNodeIndex].allNeighbourhoods.get(lastMoveMade).entrySet())
		{
			double distance=getDistance(entry.getValue(),toNodeIndex,distanceMeasure);
								
			if(distance>maxDistance)
			{
				maxDistance=distance;
				move=entry.getKey();	
			}
		}
		
		return move;
	}

	@Deprecated
	public int[] getAStarPath(int fromNodeIndex,int toNodeIndex,MOVE lastMoveMade)
	{
		return getShortestPath(fromNodeIndex,toNodeIndex,lastMoveMade);
	}

	@Deprecated
	public int[] getApproximateShortestPath(int fromNodeIndex,int toNodeIndex,MOVE lastMoveMade)
	{
		return getShortestPath(fromNodeIndex,toNodeIndex,lastMoveMade);
	}

	public int[] getShortestPath(int fromNodeIndex,int toNodeIndex,MOVE lastMoveMade)
	{
		if(currentLabyrinth.graph[fromNodeIndex].neighbourhood.size()==0)//lair
			return new int[0];

		return pathsCaches[labyrinthIndex].getPathFromAtoB(fromNodeIndex,toNodeIndex,lastMoveMade);
	}

	@Deprecated
	public int getApproximateShortestPathDistance(int fromNodeIndex,int toNodeIndex,MOVE lastMoveMade)
	{
		return getShortestPathDistance(fromNodeIndex,toNodeIndex,lastMoveMade);
	}

	public int getShortestPathDistance(int fromNodeIndex,int toNodeIndex,MOVE lastMoveMade)
	{
		if(currentLabyrinth.graph[fromNodeIndex].neighbourhood.size()==0)//lair
			return 0;

		return pathsCaches[labyrinthIndex].getPathDistanceFromAtoB(fromNodeIndex,toNodeIndex,lastMoveMade);
	}


}