 package euler;
 
 import static org.junit.Assert.*;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class Solver019Test
 {
 
   @Before
   public void setUp() throws Exception
   {
   }
 
   @After
   public void tearDown() throws Exception
   {
   }
 
   @Test
   public void testGetDays()
   {
     Solver019 solver = new Solver019();
     assertEquals(366, solver.getDays(1901, 1, 1));
     assertEquals(397, solver.getDays(1901, 2, 1));
   }
 
 }
