 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 import java.util.List;
 
 public class RiskAI{
 	static RiskAI riskAI;
 	static GameData currentGame;
 	static List<Continent> continentData;
 	static List<Territory> territoryData;
 	
 	public static HandleClick clickHandler;
 	
 	private static final int PLAYERS_HUMAN = 0;
 	private static final int PLAYERS_COMP = 4;
 	
 	public static void main(String[] args)
 	{
 		//Initialize system information
 		clickHandler = new HandleClick();
 		
 		riskAI = new RiskAI();
 		continentData = new ArrayList<Continent>(ContinentData.init());
 		territoryData = new ArrayList<Territory>(TerritoryData.init(continentData));		
 		Gfx testwindow = new Gfx();
<<<<<<< HEAD
 		
 		continentData = new ArrayList<Continent>(ContinentData.init());
 		territoryData = new ArrayList<Territory>(TerritoryData.init(continentData));
 		
 		riskAI = new RiskAI();
 	}
 	public RiskAI()
 	{
 		Gfx testwindow = new Gfx();
=======
	}
	public RiskAI()
	{
>>>>>>> d176a461b1466e8a4f82206c454739a3f53cee43
 		currentGame = new GameData(PLAYERS_HUMAN, PLAYERS_COMP);
 	}
 	
 	public static GameData getCurrentGameData()
 	{	
 		return currentGame;
 	}
 	public static List<Player> getPlayerList()
 	{
 		return getCurrentGameData().playerList;
 	}
 }
