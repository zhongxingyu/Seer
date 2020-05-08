 package test;
 
 import static org.junit.Assert.*;
 
 import org.junit.*;
 
import members.Competitor;
import members.Time;
 
 public class TestLapRace {
 	private Competitor c;
 	private Time t1, t2, t3, t4;
 	
 	@Before
 	public void setup(){
 		c = new Competitor(1);
 		t1 = new Time(1223);
 		t2 = new Time(2232);
 		t3 = new Time(1500);
 		t4 = new Time(2000);
 		c.addStartTime(t1);
 		c.addFinishTime(t2);
 	}
 	
 	@Test
 	public void TestNoStartorFinish(){
 		Competitor c2 = new Competitor(2);
 		assertEquals(c2.getLapMoments().size(), 0);
 	}
 	
 	@Test
 	public void TestZeroLaps(){
 		assertEquals(c.getLaps().size(), 1);
 		assertEquals(c.getLaps().get(0).getTotal(), t1.difference(t2));
 	}
 	
 	@Test
 	public void TestOneLap(){
 		c.addLapMoment(t3);
 		assertEquals(c.getLaps().get(0).getTotal(), t1.difference(t3));
 		assertEquals(c.getLaps().get(1).getTotal(), t3.difference(t2));
 	}
 	
 	@Test
 	public void TestNumberOfLaps(){
 		c.addLapMoment(t3);
 		c.addLapMoment(t4);
 		assertEquals(c.numberOfLaps(), 2);
 	}
 	
 	@Test
 	public void TestTwoLaps(){
 		c.addLapMoment(t3);
 		c.addLapMoment(t4);
 		assertEquals(c.getLaps().get(0).getTotal(), t1.difference(t3));
 		assertEquals(c.getLaps().get(1).getTotal(), t3.difference(t4));
 		assertEquals(c.getLaps().get(2).getTotal(), t4.difference(t2));
 	}
 	
 }
