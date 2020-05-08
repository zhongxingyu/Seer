 package btlshp.junit;
 
 import java.io.*;
 import java.util.ArrayList;
 
 import org.junit.*;
 
 import btlshp.entities.Base;
 import btlshp.entities.ConstructBlock;
 import btlshp.entities.Location;
 import btlshp.enums.Direction;
 import btlshp.entities.Player;
 import btlshp.entities.Ship;
 import btlshp.entities.Map;
 import btlshp.turns.Turn;
 import btlshp.turns.TurnFactory;
 import junit.framework.TestCase;
 
 public class TurnTest extends TestCase {
 	private ArrayList<Turn> t = new ArrayList<Turn>();
 	@Before
 	public void setUp() throws Exception{
 		
 		Player playerOne = new Player();
 		Player playerTwo = new Player();
 		Map m = new Map(playerOne, playerTwo);
 		Ship s = Ship.buildCruiser(playerOne);
 		Direction dir = Direction.South;
 		
 		int distance1 = 5;
 		int distance2 = 10;
 		Base b = new Base(playerOne);
 		Location loc = new Location(distance1, distance2);
 		s.setLocation(loc);
 		ConstructBlock repairBaseBlock = new ConstructBlock(b, null, null, null);
 		ConstructBlock repairBlock = new ConstructBlock(s, null, null, null);
 		
 		t.add(TurnFactory.acceptSurrender());
 		t.add(TurnFactory.confirmPostponeGame());
	//	t.add(TurnFactory.launchTorpedo(m, s));
 		t.add(TurnFactory.loadGameState(null));
 		t.add(TurnFactory.saveGameState(m));
 		t.add(TurnFactory.moveShip(m, s, dir, distance1));
 		t.add(TurnFactory.pass());
 		t.add(TurnFactory.placeMine(m, s, loc));
 		t.add(TurnFactory.repairBase(repairBaseBlock, b));
 		t.add(TurnFactory.repairShip(repairBlock, s));
 		t.add(TurnFactory.requestPostponeGame());
 		t.add(TurnFactory.requestSurrender());
 		t.add(TurnFactory.shoot(m, s, loc));
 		t.add(TurnFactory.takeMine(m, s, loc));
 
 		
 	}
 	@Test
 	public void testExecuteTurn(){
 		for(Turn turn: t){
 			assertNotNull(turn);
 			//turn.executeTurn();
 			
 			System.out.println("Testing " + turn.toString());
 			//assertTrue(turn.wasSuccessful());
 			
 		}
 	}
 	public void testTurnTypes(){
 		assertEquals(t.get(0).toString(),"acceptSurrender");
 		assertEquals(t.get(1).toString(),"confirmPostponeGame");
 		assertEquals(t.get(2).toString(),"launchTorpedo");
 		assertEquals(t.get(3).toString(),"loadGameState");
 		assertEquals(t.get(4).toString(),"saveGameState");
 		assertEquals(t.get(5).toString(),"moveShip");
 		assertEquals(t.get(6).toString(),"pass");
 		assertEquals(t.get(7).toString(),"placeMine");
 		assertEquals(t.get(8).toString(),"repairBase");
 		assertEquals(t.get(9).toString(),"repairShip");
 		assertEquals(t.get(10).toString(),"requestPostponeGame");
 		assertEquals(t.get(11).toString(),"requestSurrender");
 		assertEquals(t.get(12).toString(),"shoot");
 		assertEquals(t.get(13).toString(),"takeMine");
 	}
 	public void testSerialize(){
 		String filename = "turn.ser";
 		FileOutputStream fileout = null;
 		ObjectOutputStream objout = null;
 		FileInputStream filein = null;
 		ObjectInputStream objin = null;
 		Turn serTurn = TurnFactory.pass();
 		Turn serNewTurn = null;
 		for(Turn turn : t){
 			filename = turn.toString()+".ser";
 		
 			try{
 				fileout = new FileOutputStream(filename);
 				objout = new ObjectOutputStream(fileout);
 				objout.writeObject(serTurn);
 				fileout.close();
 			}catch(IOException e){
 				System.err.println(e.getMessage());
 			}
 		
 			try{
 				filein = new FileInputStream(filename);
 				objin = new ObjectInputStream(filein);
 				serNewTurn = (Turn) objin.readObject();
 				filein.close();
 			}catch(IOException e){
 				System.err.println(e.getMessage());
 			}
 			catch(ClassNotFoundException e){
 				System.err.println(e.getMessage());
 			}
 		}
 	}
 	
 }
