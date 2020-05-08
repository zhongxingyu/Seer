 package testStorage;
 import java.util.ArrayList;
 import junit.framework.*;
 import storage.*;
 
 public class TestStorage extends TestCase{
 	private int expectedValue;
 	private int actualValue;
 	boolean failed;
 	
 	public void testAddNewPlayer() {
 		Statistics jack = Storage.loadPlayer("jack","Black");
 
 		assertEquals(Storage.savePlayer(jack), true);
 	}
 	
 	public void testSavePlayer() {
 		Statistics abc = Storage.loadPlayer("abc","123");
 		
 		abc.setChips(5);
 		Storage.savePlayer(abc);
 		abc = Storage.loadPlayer("abc","123");
 		actualValue = abc.getChips();
 		expectedValue = 5;
 		
 		assertEquals(actualValue, expectedValue);
 	}
 	
 	public void testLoadPlayerPeriod() {
 		failed = false;
 		try {
 			Storage.loadPlayer("John.Test", "password");
 		} catch (IllegalArgumentException e) {
 			failed = true;
 		} finally {
 			assertEquals(failed, true);
 		}
 		
 	}
 	public void testLoadPlayerComma() {
 		failed = false;
 		try {
 			Storage.loadPlayer("john,test", "password");
 		} catch (IllegalArgumentException e) {
 			failed = true;
 		} finally{
 			assertEquals(failed, true);
 		}
 	}
 	
 	public void testAddToHallOfFame() {
 		Statistics billyjoe = Storage.loadPlayer("billyjoe","abc");
 		billyjoe.addWin(100000);
 		Storage.savePlayer(billyjoe);
 		
 		ArrayList<Statistics> hallOfFame = new ArrayList<Statistics>();
 		hallOfFame = HallOfFame.getHallOfFame();
 		
 		assertEquals(hallOfFame.get(0).getUsername(), "billyjoe");
 	}
 	
 	public void testAddEqualScore() {
 		Statistics equalScoreOne = Storage.loadPlayer("one", "one");
 		Statistics equalScoreTwo = Storage.loadPlayer("two", "two");
 		
 		equalScoreOne.addWin(100000);
 		equalScoreTwo.addWin(100000);
 		
 		ArrayList<Statistics> hallOfFame = new ArrayList<Statistics>();
 		hallOfFame = HallOfFame.getHallOfFame();
 		
		assertEquals(hallOfFame.get(0).getUsername(), "two");
		assertEquals(hallOfFame.get(1).getUsername(), "one");
 	}
 	
 	public void testDisplayHallOfFame() {
 		HallOfFame.display();
 	}
 }
