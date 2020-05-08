 package cha.domain;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 //Done
 /**
  * @author Malla
  *
  */
 public class NormalTurnTest {
 	private Piece p;
 	private NormalTurn nt;
 	
 	@Before
 	public void setUp() throws Exception {
 		Board.getInstance().setTeamName("1");
 		Board.createNewBoard(1);
 
 		p = Board.getInstance().getPiece(0);
 		p.setPosition(10);
 		nt = new NormalTurn(5);
 	}
 	
 	@Test
 	public void testNormalTurn() {
 		try{
 		new NormalTurn(-2);
 		}
 		catch (Exception IllegalArgumentException) {
 		      assertTrue(true);
		}
		p.
		
 	}
 
 	
 	/**Tests 
 	 * startMission 
 	 * getMission (in TurnType)
 	 */
 	@Test
 	public void testStartMission() {
 		nt.startMission(Category.BACKWARDS);
 		Mission m=nt.getMission();
 		assertTrue(m.getCategory().equals(Category.BACKWARDS));
 	}
 
 	@Test
 	public void testMissionDone() {
 		//Not sure how to test missionDone.
 	}
 }
