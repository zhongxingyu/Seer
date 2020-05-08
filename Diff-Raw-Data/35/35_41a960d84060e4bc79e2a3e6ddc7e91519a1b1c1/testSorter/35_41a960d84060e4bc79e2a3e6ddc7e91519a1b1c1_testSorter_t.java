 package test.unit;
 
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
 
 import java.util.ArrayList;
 
 import members.Competitor;
 import members.Sorter;
 import members.Time;
 
import org.junit.Before;
import org.junit.Test;
 
 public class testSorter {
 	boolean sort;
 	ArrayList<Competitor> list;
 	String racetype;
 	
 	Competitor c1, c2, c3;
 	Time t0, t1, t2, t3;
 	
 	Sorter sorter;
 	
 	@Before
 	public void setup() {
 		list = new ArrayList<Competitor>();
 		c1 = new Competitor(1);
 		c2 = new Competitor(2);
 		c3 = new Competitor(3);
 		
 		t0 = Time.parse(0, "00.00.00");
 		t1 = Time.parse(0, "00.20.15");
 		t2 = Time.parse(0, "01.00.00");
 		t3 = Time.parse(0, "01.45.45");
 		
 		sorter = new Sorter();
 	}
 	
 	@Test
 	public void testNormalRaceAlreadySorted() {
 		c1.addStartTime(t0);
 		c2.addStartTime(t0);
 		c3.addStartTime(t0);
 		
 		c1.addFinishTime(t1);
 		c2.addFinishTime(t2);
 		c3.addFinishTime(t3);
 		
 		list.add(c1);
 		list.add(c2);
 		list.add(c3);
 		
 		sorter.sortList(false, list, "");
 		
 		assertEquals(c1, list.get(0));
 		assertEquals(c2, list.get(1));
 		assertEquals(c3, list.get(2));
 	}
 	
 	@Test
 	public void testNormalRaceNotSorted() {
 		c1.addStartTime(t0);
 		c2.addStartTime(t0);
 		c3.addStartTime(t0);
 		
 		c1.addFinishTime(t1);
 		c2.addFinishTime(t2);
 		c3.addFinishTime(t3);
 		
 		list.add(c3);
 		list.add(c1);
 		list.add(c2);
 		
 		
 		sorter.sortList(false, list, "");
 		
 		assertEquals(c1, list.get(0));
 		assertEquals(c2, list.get(1));
 		assertEquals(c3, list.get(2));
 	}
 	
 	@Test
 	public void testLapRaceAlreadySorted() {
 		c1.addStartTime(t0);
 		c2.addStartTime(t0);
 		c3.addStartTime(t0);
 		
 		c1.addFinishTime(t1);
 		c2.addFinishTime(t2);
 		c3.addFinishTime(t3);
 		
 		list.add(c3);
 		list.add(c1);
 		list.add(c2);
 		
 		
 		sorter.sortList(true, list, "laprace");
 		
 		assertEquals(c1, list.get(0));
 		assertEquals(c2, list.get(1));
 		assertEquals(c3, list.get(2));
 	}
 	
 	@Test
 	public void testEtappRaceAlreadySorted() {
 		c1.addStartTime(t0, 1);
 		c2.addStartTime(t0, 1);
 		c3.addStartTime(t0, 1);
 		
 		c1.addFinishTime(t1, 1);
 		c2.addFinishTime(t2, 1);
 		c3.addFinishTime(t3, 1);
 		
 		list.add(c3);
 		list.add(c1);
 		list.add(c2);
 		
 		
 		sorter.sortList(true, list, "etapprace");
 		
 		assertTrue(c1.equals(list.get(0)));
 		assertTrue(c2.equals(list.get(1)));
 		assertTrue(c3.equals(list.get(2)));
 	}
 	
 
 }
