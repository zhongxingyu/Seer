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
 		continentData = ContinentData.init();
 		territoryData = TerritoryData.init(continentData);
		Gfx testwindow = new Gfx();
 	}
 	public RiskAI()
 	{
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
