 package isnork.g6.tests;
 
 import isnork.g6.NewPlayer;
 import isnork.g6.NewPlayer.Level;
 import isnork.g6.NewPlayer.Risk;
 import isnork.sim.GameConfig;
 import isnork.sim.SeaLifePrototype;
 
 import java.io.File;
 import java.util.HashSet;
 
 import junit.framework.TestCase;
 
 import org.junit.Before;
 
 public class TestNewPlayer extends TestCase {
 
 	NewPlayer player;
 	HashSet<SeaLifePrototype> protos;
 	int d = 20;
 	int r = 5;
 	int penalty = 0;
 	int n = 20;

	@Before
	public void setUp() throws Exception {
		player = new NewPlayer();
	}
 	
 	public void setupBoard(File f){
 		protos = new HashSet<SeaLifePrototype>();
 		GameConfig config = new GameConfig();
 		config.setSelectedBoard(f);
 		config.load();
 		for (SeaLifePrototype t : config.getSeaLifePrototypes()) {
 			protos.add((SeaLifePrototype) t.clone());
 		}
 		player.newGame(protos, penalty, d, r, n);
 	}
 
 	public void testGetBoardFML() {
 		setupBoard(new File("boards/Piranha.xml"));
 		Level myDanger = player.getLevel(player.DANGER);
 		Risk myRisk = player.getRisk();
 		assertEquals(myDanger, Level.OMG);
 		System.out.println(myRisk.getFactor());
 		assertEquals(myRisk, Risk.VERYBAD);
 	}
 	
 	public void testGetBoardNONE() {
 		setupBoard(new File("boards/g5_very_happy.xml"));
 		Level myDanger = player.getLevel(player.DANGER);
 		Risk myRisk = player.getRisk();
 		assertEquals(myDanger, Level.NONE);
 		assertEquals(myRisk, Risk.NONE);
 	}
 
 }
