 package br.com.mibsim.basement;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import br.com.etyllica.linear.PointInt2D;
 import br.com.mibsim.building.basement.Basement;
 import br.com.mibsim.planning.PlanningAction;
 import br.com.mibsim.planning.PlanningTask;
 
 public class BasementTest {
 
 	protected Basement basement;
 	
 	@Before
 	public void setUp() throws Exception {
 		
 		basement = new Basement(12,12);		
 	}
 	
 	@Test
 	public void testSectorDesignation() {
 		
 		PointInt2D target1 = basement.nextTarget(0); 
 		assertEquals(-100, target1.getX());
 		assertEquals(-100, target1.getY());
 		
 		PointInt2D target2 = basement.nextTarget(1);
 		assertEquals(-100+64, target2.getX());
 		assertEquals(-100, target2.getY());
 		
 		PointInt2D target3 = basement.nextTarget(2);
 		assertEquals(-100+64*2, target3.getX());
 		assertEquals(-100, target3.getY());
 	}
 	
 	@Test
 	public void testRequestDesignation() {
 			
		PlanningTask earlierTask = new PlanningTask(PlanningAction.REPORT, new PointInt2D(0,0));
		
		PlanningTask task = basement.askForDesignation(earlierTask);
 		
 		assertEquals(PlanningAction.EXPLORE, task.getAction());				
 	}
 	
 }
