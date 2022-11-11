package pacman.game.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Set;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class PathsCache 
{	
	public HashMap<Integer, Integer> crossroadsIndexConverter;
	public DNode[] nodes;
	public Crossroads[] crossroads;
	public Game game;
		
 	public PathsCache(int labyrinthIndex)
	{
		crossroadsIndexConverter = new HashMap<Integer, Integer>();
		
		this.game=new Game(0,labyrinthIndex);
		Labyrinth m = game.getCurrentLabyrinth();
		
		int[] crsIndex = m.crsIndex;
		
		for (int i = 0; i < crsIndex.length; i++)
			crossroadsIndexConverter.put(crsIndex[i], i);

		nodes = assignCrossroadsToNodes(game);
		crossroads = crossroadDistances(game);
		
		for(int i = 0; i< crossroads.length; i++)
			crossroads[i].computeShortestPaths();
	}

	public int getPathDistanceFromAtoB(int a, int b, MOVE lastMove)
	{
		return getPathFromAtoB(a, b, lastMove).length;
	}
	
	public int[] getPathFromAtoB(int a, int b, MOVE lastMove)
	{
		if(a==b)
			return new int[]{};

		CrossroadData fromCrossroad = nodes[a].getNearestCrossroad(lastMove);

		for (int i = 0; i < fromCrossroad.path.length; i++)
			if (fromCrossroad.path[i] == b)
				return Arrays.copyOf(fromCrossroad.path, i + 1);

		int crossroadFrom = fromCrossroad.nodeID;
		int crossroadFromId = crossroadsIndexConverter.get(crossroadFrom);
		MOVE moveEnteredJunction = fromCrossroad.lastMove.equals(MOVE.STAY) ? lastMove : fromCrossroad.lastMove; //if we are at a junction, consider last move instead

		ArrayList<CrossroadData> crossroadTo=nodes[b].closestCrossroad;
				
		int minDis = Integer.MAX_VALUE;
		int[] shortestPath = null;
		int closestCrossroad = -1;
		
		boolean onTheWay=false;
	
		for (int q = 0; q < crossroadTo.size(); q++)
		{
			int crossroadToId = crossroadsIndexConverter.get(crossroadTo.get(q).nodeID);
			
			if(crossroadFromId==crossroadToId)
			{
				if(!game.getMoveToMakeToReachDirectNeighbour(crossroadFrom, crossroadTo.get(q).reversePath[0]).equals(moveEnteredJunction.opposite()))
				{
					int[] reversePath=crossroadTo.get(q).reversePath;
					int cutoff=-1;
					
					for(int w=0;w<reversePath.length;w++)
						if(reversePath[w]==b)
							cutoff=w;
					
					shortestPath = Arrays.copyOf(reversePath, cutoff+1);
					minDis = shortestPath.length;
					closestCrossroad = q;
					onTheWay=true;
				}
			}
			else
			{				
				EnumMap<MOVE, int[]> paths = crossroads[crossroadFromId].paths[crossroadToId];
				Set<MOVE> set=paths.keySet();
					
				for (MOVE move : set) 
				{				
					if (!move.opposite().equals(moveEnteredJunction) && !move.equals(MOVE.STAY))
					{
						int[] path = paths.get(move);
						
						if (path.length+crossroadTo.get(q).path.length < minDis)//need to take distance from toJunction to target into account
						{							
							minDis = path.length+crossroadTo.get(q).path.length;
							shortestPath = path;
							closestCrossroad = q;
							onTheWay=false;
						}
					}
				}
			}
		}
					
		if(!onTheWay)
			return concat(fromCrossroad.path, shortestPath, crossroadTo.get(closestCrossroad).reversePath);
		else
			return concat(fromCrossroad.path, shortestPath);
	}

	private Crossroads[] crossroadDistances(Game game)
	{
		Labyrinth m = game.getCurrentLabyrinth();
		int[] ints = m.crsIndex;

		Crossroads[] crossroads = new Crossroads[ints.length];

		for (int q = 0; q < ints.length; q++)
		{
			MOVE[] possibleMoves = m.graph[ints[q]].allPossibleMoves.get(MOVE.STAY);

			crossroads[q] = new Crossroads(q, ints[q], ints.length);

			for (int z = 0; z < ints.length; z++)
			{
				for (int i = 0; i < possibleMoves.length; i++) 
				{
					int neighbour = game.getNeighbour(ints[q],possibleMoves[i]);
					int[] p = m.aStar.calcPathsAStar(neighbour,ints[z], possibleMoves[i], game);
					m.aStar.resetGraph();

					crossroads[q].addPath(z, possibleMoves[i], p);
				}
			}
		}

		return crossroads;
	}

	private DNode[] assignCrossroadsToNodes(Game game)
	{
		Labyrinth m = game.getCurrentLabyrinth();
		int numNodes = m.graph.length;

		DNode[] allNodes = new DNode[numNodes];

		for (int i = 0; i < numNodes; i++) 
		{
			boolean isJunction=game.isCrossroads(i);
			allNodes[i] = new DNode(i,isJunction);

			if(!isJunction)
			{
				MOVE[] possibleMoves = m.graph[i].allPossibleMoves.get(MOVE.STAY);
	
				for (int j = 0; j < possibleMoves.length; j++) 
				{
					ArrayList<Integer> path = new ArrayList<Integer>();
	
					MOVE lastMove = possibleMoves[j];
					int currentNode = game.getNeighbour(i, lastMove);
					path.add(currentNode);
	
					while (!game.isCrossroads(currentNode))
					{
						MOVE[] newPossibleMoves = game.getPossibleMoves(currentNode);
	
						for (int q = 0; q < newPossibleMoves.length; q++)
							if (newPossibleMoves[q].opposite() != lastMove) 
							{
								lastMove = newPossibleMoves[q];
								break;
							}
	
						currentNode = game.getNeighbour(currentNode, lastMove);
						path.add(currentNode);
					}
	
					int[] array = new int[path.size()];
					
					for (int w = 0; w < path.size(); w++)
						array[w] = path.get(w);
	
					allNodes[i].addPath(array[array.length - 1], possibleMoves[j], i, array, lastMove);
				}
			}
		}

		return allNodes;
	}
	
	private int[] concat(int[]... arrays) 
	{
		int totalLength = 0;

		for (int i = 0; i < arrays.length; i++)
			totalLength += arrays[i].length;

		int[] fullArray = new int[totalLength];

		int index = 0;

		for (int i = 0; i < arrays.length; i++)
			for (int j = 0; j < arrays[i].length; j++)
				fullArray[index++] = arrays[i][j];

		return fullArray;
	}
}

class CrossroadData
{
	public int nodeID,nodeStartedFrom;
	public MOVE firstMove, lastMove;
	public int[] path, reversePath;

	public CrossroadData(int nodeID, MOVE firstMove, int nodeStartedFrom, int[] path, MOVE lastMove)
	{
		this.nodeID = nodeID;
		this.nodeStartedFrom=nodeStartedFrom;
		this.firstMove = firstMove;
		this.path = path;
		this.lastMove = lastMove;
		
		if(path.length>0)
			this.reversePath = getReversePath(path);
		else
			reversePath=new int[]{};
	}

	public int[] getReversePath(int[] path) 
	{
		int[] reversePath = new int[path.length];

		for (int i = 1; i < reversePath.length; i++)
			reversePath[i-1] = path[path.length - 1 - i];

		reversePath[reversePath.length-1]=nodeStartedFrom;
				
		return reversePath;
	}

	public String toString() 
	{
		return nodeID + "\t" + firstMove.toString() + "\t" + Arrays.toString(path);
	}
}

class DNode 
{
	public int nodeID;
	public ArrayList<CrossroadData> closestCrossroad;
	public boolean isCrossroad;
	
	public DNode(int nodeID, boolean isCrossroad)
	{
		this.nodeID = nodeID;
		this.isCrossroad =isCrossroad;
		
		this.closestCrossroad = new ArrayList<CrossroadData>();
		
		if(isCrossroad)
			closestCrossroad.add(new CrossroadData(nodeID,MOVE.STAY,nodeID,new int[]{},MOVE.STAY));
	}

	public CrossroadData getNearestCrossroad(MOVE lastMove)
	{
		if(isCrossroad)
			return closestCrossroad.get(0);
		
		int minDis=Integer.MAX_VALUE;
		int bestIndex=-1;
		
		for (int i = 0; i < closestCrossroad.size(); i++)
			if (!closestCrossroad.get(i).firstMove.equals(lastMove.opposite()))
			{
				int newDis= closestCrossroad.get(i).path.length;
				
				if(newDis<minDis)
				{
					minDis=newDis;
					bestIndex=i;
				}
			}

		if(bestIndex!=-1)
			return closestCrossroad.get(bestIndex);
		else
			return null;
	}

	public void addPath(int junctionID, MOVE firstMove, int nodeStartedFrom,int[] path, MOVE lastMove) 
	{
		closestCrossroad.add(new CrossroadData(junctionID, firstMove, nodeStartedFrom,path, lastMove));
	}

	public String toString() 
	{
		return "" + nodeID + "\t" + isCrossroad;
	}
}

class Crossroads
{
	public int crsId, nodeId;
	public EnumMap<MOVE, int[]>[] paths;

	public void computeShortestPaths()
	{
		MOVE[] moves=MOVE.values();
		
		for(int i=0;i<paths.length;i++)
		{
			if(i== crsId)
				paths[i].put(MOVE.STAY,new int[]{});
			else
			{
				int dist=Integer.MAX_VALUE;
				int[] path=null;
				
				for(int j=0;j<moves.length;j++)
				{
					if(paths[i].containsKey(moves[j]))
					{
						int[] tmp=paths[i].get(moves[j]);
					
						if(tmp.length<dist)
						{
							dist=tmp.length;
							path=tmp;
						}
					}
				}
				
				paths[i].put(MOVE.STAY,path);
			}
		}
	}

	public Crossroads(int crsId, int nodeId, int numCrs)
	{
		this.crsId = crsId;
		this.nodeId = nodeId;

		paths = new EnumMap[numCrs];

		for (int i = 0; i < paths.length; i++)
			paths[i] = new EnumMap<MOVE, int[]>(MOVE.class);
	}

	public void addPath(int toCrossroad, MOVE firstMove, int[] path)
	{		
		paths[toCrossroad].put(firstMove, path);
	}

	public String toString() 
	{
		return crsId + "\t" + nodeId;
	}
}