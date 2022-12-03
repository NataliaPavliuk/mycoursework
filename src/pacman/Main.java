package pacman;

import java.util.EnumMap;

import pacman.controllers.*;
import pacman.entries.pacman.TreeSearch.AStar;
import pacman.entries.pacman.TreeSearch.DFS;
import pacman.game.Game;
import pacman.game.GameView;

import static pacman.game.Constants.*;

public class Main
{	

	public static void main(String[] args)
	{
		Main execute=new Main();
		execute.runGame(new NearestPillPacMan(),new StarterGhosts());
	}

    public void runGame(Controller<MOVE> pacManController, Controller<EnumMap<GHOST,MOVE>> ghostController)
	{
		Game game=new Game(0);
		
		GameView gv =new GameView(game).showGame();
		
		if(pacManController instanceof HumanController)
			gv.getFrame().addKeyListener(((HumanController)pacManController).getKeyboardInput());
				
		new Thread(pacManController).start();
		new Thread(ghostController).start();
		
		while(!game.gameOver())
		{
			pacManController.update(game.copy(),System.currentTimeMillis()+ ARREST);
			ghostController.update(game.copy(),System.currentTimeMillis()+ ARREST);

			try
			{
				Thread.sleep(ARREST);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}

	        game.advanceGame(pacManController.getMove(),ghostController.getMove());	   

	        	gv.repaint();
		}
		
		pacManController.terminate();
		ghostController.terminate();
	}

	public static EnumMap<GHOST, MOVE> getMove(Game game, long timeDue)
	{
		return null;
	}

}