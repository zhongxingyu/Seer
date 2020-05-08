 package csci331.team.red.example;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 /**
  * Tester class for {@link Example}
 *
 * 
 * 
  * 
  * @author jourmeob
  * 
  */
 public class ExampleTest {
 
 	private static List<Double> testMarks;
 
 	@BeforeClass
 	public static void setUp() {
 		testMarks = new LinkedList<Double>();
 		for (int i = 0; i < 100; i++) {
 			testMarks.add(Math.random());
 		}
 	}
 
 	/**
 	 * Test's the two constructors of {@link Example}<br>
 	 * This is not really necessary, but it is nice to have
 	 */
 	@Test
 	public void creation() {
 
 		int num1 = 1;
 		String name1 = "one";
 
 		int num2 = 1;
 		String name2 = "two";
 
 		Example simple1 = new Example(num1, name1);
 		Example simple2 = new Example(num2, name2);
 		Example full = new Example(num1, name1, testMarks);
 
 		assertEquals("Name did not copy in constructor!", simple1.getName(),
 				name1);
 		assertEquals("Sze of Marks is not correct!", Example.SIZE_OF_MARKS,
 				simple2.getMarks().size());
 		assertNotNull(full.getMarks());
 
 	}
 
 	/**
 	 * @see {@link Object#hashCode()}
 	 */
 	@Test
 	public void testHashCode() {
 
 		Example x = new Example(1, "one");
 		Example y = new Example(1, "one");
 
 		assertEquals("Hashcode is not consistant!", x.hashCode(), x.hashCode());
 
 		if (x.equals(y)) {
 			assertEquals("Hashcode is not equivalent!", x.hashCode(),
 					y.hashCode());
 		}
 	}
 
 	/**
 	 * @see {@link Object#equals(Object)}
 	 */
 	@Test
 	public void testEquals() {
 
 		Example x = new Example(1, "one");
 		Example y = new Example(1, "one");
 		Example z = new Example(1, "one");
 
 		assertEquals("It is NOT reflexive!", x, x);
 		if (x.equals(y)) {
 			assertEquals("It is NOT symmetric!", y, x);
 		}
 
 		if (x.equals(y) && (y.equals(z))) {
 			assertEquals("It is NOT transitive!", x, z);
 		}
 
 		assertEquals("It is NOT consistent!", x.equals(y), x.equals(y));
 
 		assertFalse("It is NOT null safe!", x.equals(null));
 	}
 }
