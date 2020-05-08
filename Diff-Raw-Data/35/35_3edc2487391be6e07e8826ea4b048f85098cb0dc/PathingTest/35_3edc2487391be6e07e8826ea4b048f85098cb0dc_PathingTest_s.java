 package Testing;
 
 import java.util.LinkedList;
 import java.util.Set;
 
 import junit.framework.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 public class PathingTest {
 	
 	IntBoard myBoard;
 	
 	@Before
 	public void setup() {
 		myBoard = new IntBoard();
 		myBoard.calcAdjacencies();
 	}
 	
 	@Test
 	public void testCalcIndex() {
 		
 		// Tests all four corners, which in turn tests the edges as well, and one general case
 		Assert.assertEquals(0, myBoard.calcIndex(0, 0));
 		Assert.assertEquals(3, myBoard.calcIndex(0, 3));
 		Assert.assertEquals(12, myBoard.calcIndex(3, 0));
 		Assert.assertEquals(15, myBoard.calcIndex(3, 3));
 		Assert.assertEquals(6, myBoard.calcIndex(1, 2));	
 		
 	}
 
 	@Test
 	public void testAdjCorners() {
 		// Tests the top left corner
 		LinkedList<Integer> testList = myBoard.getAdjList(0);
 		Assert.assertTrue(testList.contains(4));
 		Assert.assertTrue(testList.contains(1));
 		Assert.assertEquals(2, testList.size());
 		
 		// Tests the bottom right corner
 		testList = myBoard.getAdjList(15);
 		Assert.assertTrue(testList.contains(11));
 		Assert.assertTrue(testList.contains(14));
 		Assert.assertEquals(2, testList.size());
 	}
 	
 	@Test
 	public void testAdjEdges() {
 		// Tests a right edge
 		LinkedList<Integer> testList = myBoard.getAdjList(7);
 		Assert.assertTrue(testList.contains(11));
 		Assert.assertTrue(testList.contains(3));
 		Assert.assertTrue(testList.contains(6));
 		Assert.assertEquals(3, testList.size());
 		
 		// Tests a left edge
 		testList = myBoard.getAdjList(8);
 		Assert.assertTrue(testList.contains(4));
 		Assert.assertTrue(testList.contains(9));
 		Assert.assertTrue(testList.contains(12));
 		Assert.assertEquals(3, testList.size());
 	}
 
 	@Test
 	public void testAdjOneOff() {
 		// Tests one off the left edge and top
 		LinkedList<Integer> testList = myBoard.getAdjList(5);
 		Assert.assertTrue(testList.contains(1));
 		Assert.assertTrue(testList.contains(4));
 		Assert.assertTrue(testList.contains(6));
 		Assert.assertTrue(testList.contains(9));
 		Assert.assertEquals(4, testList.size());
 		
 		// Tests one off the right edge and bottom
 		testList = myBoard.getAdjList(10);
 		Assert.assertTrue(testList.contains(6));
 		Assert.assertTrue(testList.contains(9));
 		Assert.assertTrue(testList.contains(11));
 		Assert.assertTrue(testList.contains(14));
 		Assert.assertEquals(3, testList.size());		
 	}
 
 	// Tests paths of length 1, 2, 3, and 6 from a corner.
 	@Test
 	public void testTargets0_1()
 	{
 		myBoard.startTargets(0, 1);
 		Set<Integer> targets= myBoard.getTargets();
 		Assert.assertEquals(2, targets.size());
 		Assert.assertTrue(targets.contains(1));
 		Assert.assertTrue(targets.contains(4));
 	}
 	
 	@Test
 	public void testTargets0_2()
 	{
 		myBoard.startTargets(0, 2);
 		Set<Integer> targets= myBoard.getTargets();
 		Assert.assertEquals(3, targets.size());
 		Assert.assertTrue(targets.contains(2));
 		Assert.assertTrue(targets.contains(5));
 		Assert.assertTrue(targets.contains(8));
 	}
 
 	@Test
 	public void testTargets0_3()
 	{
 		myBoard.startTargets(0, 3);
 		Set<Integer> targets= myBoard.getTargets();
 		Assert.assertEquals(6, targets.size());
 		Assert.assertTrue(targets.contains(12));
 		Assert.assertTrue(targets.contains(9));
 		Assert.assertTrue(targets.contains(1));
 		Assert.assertTrue(targets.contains(6));
 		Assert.assertTrue(targets.contains(3));
 		Assert.assertTrue(targets.contains(4));
 	}
 
//								    /;    ;\
//								__  \\____//
//								/{_\_/   `'\____
//								\___   (o)  (o  }
//_____________________________/          :--'   DRINKA
//,-,'`@@@@@@@@       @@@@@@         \_    `__\
//;:(  @@@@@@@@@        @@@             \___(o'o)
//:: )  @@@@          @@@@@@        ,'@@(  `===='        PINTA
//:: : @@@@@:          @@@@         `@@@:
//:: \  @@@@@:       @@@@@@@)    (  '@@@'
//;; /\      /`,    @@@@@@@@@\   :@@@@@)                   MILKA
//::/  )    {_----------------:  :~`,~~;
 //;;'`; :   )                  :  / `; ;
//;;;; : :   ;                  :  ;  ; :                        DAY !!!
//`'`' / :  :                   :  :  : :
//)_ \__;      ";"          :_ ;  \_\       `,','
//:__\  \    * `,'*         \  \  :  \   *  8`;'*  *
//`^'     \ :/           `^'  `-^-'   \v/ :  \/   -Bill Ames-
 //	// NOT DONE
 	@Test
 	public void testTargets0_6()
 	{
 		//I get 2, 5, 8, 10, 15.
 		myBoard.startTargets(0, 6);
 		Set<Integer> targets= myBoard.getTargets();
 		// NEED TO FIND THE RIGHT NODES
 		Assert.assertEquals(5, targets.size());
 		Assert.assertTrue(targets.contains(2));
 		Assert.assertTrue(targets.contains(5));
 		Assert.assertTrue(targets.contains(8));
 		Assert.assertTrue(targets.contains(10));
 		Assert.assertTrue(targets.contains(15));
 		
 	}
 	
 	@Test
 	public void testTargets7_2() {
 		myBoard.startTargets(7, 2);
 		Set<Integer> targets= myBoard.getTargets();
 		Assert.assertEquals(4, targets.size());
 		Assert.assertTrue(targets.contains(15));
 		Assert.assertTrue(targets.contains(5));
 		Assert.assertTrue(targets.contains(10));
 		Assert.assertTrue(targets.contains(2));
 	}
 	
 	@Test
 	public void testTargets8_4() {
 		myBoard.startTargets(8, 4);
 		Set<Integer> targets= myBoard.getTargets();
 		Assert.assertEquals(5, targets.size());
 		Assert.assertTrue(targets.contains(7));
 		Assert.assertTrue(targets.contains(15));
 		Assert.assertTrue(targets.contains(13));
 		Assert.assertTrue(targets.contains(2));
 		Assert.assertTrue(targets.contains(5));
 	}
 	
 	@Test
 	public void testTargets10_2() {
 		myBoard.startTargets(10, 2);
 		Set<Integer> targets= myBoard.getTargets();
 		Assert.assertEquals(6, targets.size());
 		Assert.assertTrue(targets.contains(2));
 		Assert.assertTrue(targets.contains(15));
 		Assert.assertTrue(targets.contains(7));
 		Assert.assertTrue(targets.contains(5));
 		Assert.assertTrue(targets.contains(8));
 		Assert.assertTrue(targets.contains(13));
 	}
 	
 	@Test
 	public void testTargets5_3() {
 		myBoard.startTargets(5, 3);
 		Set<Integer> targets= myBoard.getTargets();
 		Assert.assertEquals(8, targets.size());
 		Assert.assertTrue(targets.contains(3));
 		Assert.assertTrue(targets.contains(11));
 		Assert.assertTrue(targets.contains(14));
 		Assert.assertTrue(targets.contains(12));
 		Assert.assertTrue(targets.contains(4));
 		Assert.assertTrue(targets.contains(6));
 		Assert.assertTrue(targets.contains(1));
 		Assert.assertTrue(targets.contains(9));
 	}
 }
