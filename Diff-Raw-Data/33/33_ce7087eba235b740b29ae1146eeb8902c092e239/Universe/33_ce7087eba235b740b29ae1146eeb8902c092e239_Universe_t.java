 package universe;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import faction.*;
 
 import settings.Globals;
 import ui.GameFrame;
 
 
 public class Universe 
 {
 	//
 	// DATA
 	//
 	// World Data
 	public static Map<String, World> addressBook = new HashMap<String, World>();
 	private static final Map<String, World> nameBook = new HashMap<String, World>();
 	
 	// Faction Data
 	public static Faction playerFaction;
 	public static final List<Faction> factions = new ArrayList<Faction>();
 	
 	// Book Keeping
 	private static final Random rand = new Random();
 	private static boolean isInitialized = false;
 	private static int roundNumber = 0;
 	
 	//
 	// METHODS
 	//
 	public static void initialize()
 	{
 		if(isInitialized)
 			return;
 		playerFaction = new HumanityFaction();
 		GameFrame.getGameFrame();
 		
 		factions.add(playerFaction);
 		
 		Faction goauld = new GoauldFaction();
 		Faction asgard = new AsgardFaction();
 		Faction tokra = new TokraFaction();
 		
 		playerFaction.decreaseReputation(goauld, 25);
 		playerFaction.increaseReputation(asgard, 10);
 		playerFaction.increaseReputation(tokra, 0);
 		
 		playerFaction.learnGateAddress(goauld.getHomeWorld());	
 		playerFaction.decreaseReputation(goauld, 25);
 		
 		asgard.learnGateAddress(goauld.getHomeWorld());
 		asgard.learnWorldLocation(goauld.getHomeWorld());
 		
 		tokra.learnGateAddress(goauld.getHomeWorld());
 		tokra.learnWorldLocation(goauld.getHomeWorld());
 		
 		goauld.learnGateAddress(playerFaction.getHomeWorld());
 		
 		factions.add(goauld);
 		factions.add(asgard);
 		factions.add(tokra);
 	}
 	
 	public static World generateWorld()
 	{
 		World w = new World();
 		String address = generateUniqueAddress();
 		String name = generateUniqueName();
 		
 		w.address = address;
 		addressBook.put(address, w);
 		
 		w.name = name;
 		nameBook.put(name, w);
 		
 		return w;
 	}
 	
 	public static void elapseTime()
 	{
 		GameFrame.getGameFrame().addToLogI("Begin Round " + roundNumber, "");
 		
 		for(Faction f : factions)
 		{
 			if(f.didWin())
 			{
 				// TODO: Any other victory stuffs.
 				return;
 			}
 		}
 		
 		for(Faction f : factions)
 		{
 			if(!f.isDefeated())
 				f.doTurn();
 		}
		
		for(World w : addressBook.values()) {
			//w.addTroops(w.getControllingFaction(), w.getPassiveTroops());
			w.doCombat();
		}
 		roundNumber++;
 	}
 	
 	public static int getDistance(World w1, World w2)
 	{
 		return getDistance(w1.address, w2.address);
 	}
 	
 	public static int getDistance(String addr1, String addr2)
 	{
 		int dist = 0;
 		for(int i = 0; i < Globals.ADDRESS_LENGTH; i++)
 		{
 			dist += Math.abs(addr1.charAt(i) - addr2.charAt(i));
 		}
 		
 		return dist;
 	}
 	
 	public static void updateName(World w, String newName)
 	{
 		nameBook.remove(w.name);
 		w.name = newName;
 		nameBook.put(newName, w);
 	}
 	
 	private static String generateUniqueName()
 	{
 		String name;
 		String suffix;
 		
 		do
 		{
 			name = "P";
 			suffix = "";
 			
 			int r = rand.nextInt(9);
 			char num = (char)('1' + r);
 			name += num;
 			
 			r = rand.nextInt(26);
 			char letter = (char)('A' + r);
 			name += letter;
 			
 			name += "-";
 			
 			for(int i = 0; i < Globals.WORLD_NAME_SUFFIX_LENGTH - 1; i++)
 			{
 				r = rand.nextInt(10);
 				
 				if(r == 0 && suffix.length() == 0)
 					continue;
 				
 				num = (char)('0' + r);
 				suffix += num;
 			}
 			
 			r = rand.nextInt(10);
 			num = (char)('0' + r);
 			suffix += num;
 			
 			name += suffix;
 			
 		} while(addressBook.containsKey(name));
 		
 		return name;
 	}
 	
 	// Could infinite loop....if we get really unlucky.
 	private static String generateUniqueAddress()
 	{
 		String addr;
 		do
 		{
 			addr = "";
 			
 			for(int i = 0; i < Globals.ADDRESS_LENGTH; i++)
 			{
 				int r = rand.nextInt(26);
 				char letter = (char)('A' + r);
 				addr += letter;
 			}
 		
 		} while(addressBook.containsKey(addr));
 			
 		return addr;
 	}
 }
