 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 /**
  * @author Jared Moore
  * @version Feb 20, 2013
  */
 public class HashTableTest {
 
 	@Test
 	public void testSimplePut() {
 		HashTable<Integer, String> table = new HashTable<Integer, String>();
 		table.put(1, "one");
 		table.put(2, "two");
 		table.put(3, "three");
 		table.put(4, "four");
 
 		assertEquals(
 				"[null, one, two, three, four, null, null, null, null, null, null]",
 				table.toString());
 	}
 
 	@Test
 	public void testSingleOverlap() {
 		HashTable<Integer, String> table = new HashTable<Integer, String>();
 		table.put(1, "one");
 		table.put(2, "two");
 		table.put(3, "three");
 		table.put(4, "four");
		table.put(1, "one again");
 
 		assertEquals(
 				"[null, one, two, three, four, one again, null, null, null, null, null]",
 				table.toString());
 	}
 
 	@Test
 	public void testSimpleResize() {
 		HashTable<Integer, String> table = new HashTable<Integer, String>();
 		table.put(1, "one");
 		table.put(2, "two");
 		table.put(3, "three");
 		table.put(4, "four");
		table.put(1, "one again");
 		table.put(6, "six");
 		table.put(7, "seven");
 		table.put(8, "eight");
 
 		assertEquals(
				"[null, one, two, three, four, one again, six, seven, eight, null, null, null, null, null, null, null, null, null, null, null, null, null, null]",
 				table.toString());
 	}
 	
 	@Test
 	public void testWrapAround() {
 		
 		HashTable<Integer, String> table = new HashTable<Integer, String>();
 		table.put(1, "one");
 		table.put(2, "two");
 		table.put(3, "three");
 		table.put(4, "four");
		table.put(1, "one again");
 		table.put(10, "ten");
		table.put(10, "ten again");
 		
 		assertEquals(
 				"[ten again, one, two, three, four, one again, null, null, null, null, ten]",
 				table.toString());
 	}
 	
 	@Test
 	public void testGet() {
 		
 		HashTable<Integer, String> table = new HashTable<Integer, String>();
 		table.put(1, "one");
 		table.put(2, "two");
 		table.put(3, "three");
 		table.put(4, "four");
		table.put(1, "one again");
 		table.put(10, "ten");
		table.put(10, "ten again");
 		
 		assertEquals("ten", table.get(10));
		assertEquals(null, table.get(21)); // same hash value as previous
 	}
 	
 	@Test
 	public void testContains() {
 		
 		HashTable<Integer, String> table = new HashTable<Integer, String>();
 		table.put(1, "one");
 		table.put(2, "two");
 		table.put(3, "three");
 		table.put(4, "four");
		table.put(1, "one again");
 		table.put(10, "ten");
		table.put(10, "ten again");
 		table.put(7, null);
 		
 		assertEquals(true, table.containsKey(10));
		assertEquals(false, table.containsKey(21));
		assertEquals(true, table.containsKey(null));
 	}
 }
