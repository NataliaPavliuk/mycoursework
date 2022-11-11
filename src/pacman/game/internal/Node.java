package pacman.game.internal;

import java.util.EnumMap;
import pacman.game.Constants.MOVE;

public final class Node
{
	public final int x,y,nodeIndex,pillIndex,powerPillIndex, numberOfNeighbouringNodes;
	public final EnumMap<MOVE, Integer> neighbourhood = new EnumMap<MOVE, Integer>(MOVE.class);
	public EnumMap<MOVE,MOVE[]> allPossibleMoves=new EnumMap<MOVE,MOVE[]>(MOVE.class);
	public EnumMap<MOVE,int[]> allNeighbouringNodes=new EnumMap<MOVE,int[]>(MOVE.class);
	public EnumMap<MOVE,EnumMap<MOVE, Integer>> allNeighbourhoods=new EnumMap<MOVE,EnumMap<MOVE, Integer>>(MOVE.class);

	public Node(int nodeIndex,int x,int y,int pillIndex,int powerPillIndex,int[] _neighbourhood)
	{
		this.nodeIndex=nodeIndex;
		this.x=x;
		this.y=y;
		this.pillIndex=pillIndex;
		this.powerPillIndex=powerPillIndex;
		
		MOVE[] moves=MOVE.values();

		for(int i=0;i<_neighbourhood.length;i++)
			if(_neighbourhood[i]!=-1)
				neighbourhood.put(moves[i],_neighbourhood[i]);
		
		numberOfNeighbouringNodes =neighbourhood.size();
		
		for(int i=0;i<moves.length;i++)
			if(neighbourhood.containsKey(moves[i]))
			{
				EnumMap<MOVE, Integer> tmp=new EnumMap<MOVE, Integer>(neighbourhood);
				tmp.remove(moves[i]);
				allNeighbourhoods.put(moves[i].opposite(),tmp);
			}
		
		allNeighbourhoods.put(MOVE.STAY,neighbourhood);
		
		int[] neighbouringNodes=new int[numberOfNeighbouringNodes];
		MOVE[] possibleMoves=new MOVE[numberOfNeighbouringNodes];
		
		int index=0;
		
		for(int i=0;i<moves.length;i++)
			if(neighbourhood.containsKey(moves[i]))
			{
				neighbouringNodes[index]=neighbourhood.get(moves[i]);
				possibleMoves[index]=moves[i];
				index++;
			}
					
		for(int i=0;i<moves.length;i++)
		{
			if(neighbourhood.containsKey(moves[i].opposite()))
			{
				int[] tempNeighbouringNodes=new int[numberOfNeighbouringNodes -1];
				MOVE[] tempPossibleMoves=new MOVE[numberOfNeighbouringNodes -1];

				index=0;
				
				for(int j=0;j<moves.length;j++)
				{
					if(moves[j]!=moves[i].opposite() && neighbourhood.containsKey(moves[j]))
					{
						tempNeighbouringNodes[index]=neighbourhood.get(moves[j]);
						tempPossibleMoves[index]=moves[j];
						index++;
					}
				}
				
				allNeighbouringNodes.put(moves[i],tempNeighbouringNodes);
				allPossibleMoves.put(moves[i],tempPossibleMoves);
			}
		}
		
		allNeighbouringNodes.put(MOVE.STAY,neighbouringNodes);
		allPossibleMoves.put(MOVE.STAY,possibleMoves);
	}
}