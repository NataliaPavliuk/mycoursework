package pacman.game;

public final class Constants
{

	public enum MOVE 
	{
		UP 		{ public MOVE opposite(){return MOVE.DOWN;		};},	
		RIGHT 	{ public MOVE opposite(){return MOVE.LEFT;		};}, 	
		DOWN 	{ public MOVE opposite(){return MOVE.UP;		};},		
		LEFT 	{ public MOVE opposite(){return MOVE.RIGHT;		};}, 	
		STAY { public MOVE opposite(){return MOVE.STAY;	};};
		
		public abstract MOVE opposite();
	};

	public enum GHOST
	{
		PIRAT(40),
		POLICEMAN(60),
		NINJA(80),
		ARMY(100);
		
		public final int initialLairTime;
		
		GHOST(int lairTime)
		{
			this.initialLairTime=lairTime;
		}
	};

	public enum DM {PATH, EUCLID, MANHATTAN};
	
	public static final int PILL=10;
	public static final int POWER_PILL=50;
	public static final int GHOST_EAT_SCORE=200;
	public static final int EDIBLE_TIME=200;
	public static final float EDIBLE_TIME_DECREASE =0.9f;
	public static final float LAIR_DECREASE =0.9f;
	public static final int LEVEL_RESET_DECREASE =6;
	public static final int COMMON_LAIR_TIME=40;
	public static final int LEVEL_LIMIT=4000;
	public static final float GHOST_REVERSAL=0.0015f;
	public static final int MAX_TIME=24000;
	public static final int AWARD_LIFE_LEFT=800;
	public static final int EXTRA_LIFE_SCORE=10000;
	public static final int EAT_DISTANCE=2;
	public static final int NUMBER_OF_GHOSTS =4;
	public static final int LABYRINTHS_NUMBER =4;
	public static final int ARREST =40;
	public static final int NUMBER_OF_LIVES =3;
	public static final int GHOST_SPEED_DECREASE =2;
	public static final int EDIBLE_ALERT=30;

	public static final String pathLabyrinth ="src/resourses/labyrinth";
	public static final String pathDistances="src/resourses/distance";
	public static final String[] nodeNames={"a","b","c","d"};
	public static final String[] disNames ={"disA","disB","disC","disD"};

	public static final int MAG=6;
	public static final int GV_WIDTH=114;
	public static final int GV_HEIGHT=130;

	public static String pathImages="src/resourses/images";
	public static String[] labyrinthNames ={"labyrinthA.png","labyrinthB.png","labyrinthC.png","labyrinthD.png"};
	
	private Constants(){}
}