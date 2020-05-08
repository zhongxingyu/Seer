 package nl.jqno.equalsverifier.talk;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import nl.jqno.equalsverifier.EqualsVerifier;
 import nl.jqno.equalsverifier.talk.S05_symmetry.Point;
 import nl.jqno.equalsverifier.talk.helper.Color;
 import nl.jqno.equalsverifier.talk.helper.NonNull;
 
 import org.junit.Ignore;
 import org.junit.Test;
 
 public class S06_transitivity {
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	/*
 	 * We can fix that!
 	 * 
 	 * We'll use the previous slide's Point, and define a new ColorPoint.
 	 */
 	public class ColorPoint extends Point {
 		@NonNull private final Color color;
 		
 		public ColorPoint(int x, int y, Color color) {
 			super(x, y);
 			this.color = color;
 		}
 		
 		@Override
 		public int hashCode() {
 			return 31 * (31 + super.hashCode()) + color.hashCode();
 		}
 		
 		@Override
 		public String toString() {
 			return String.format("ColorPoint: %s,%s,%s", x, y, color);
 		}
 		
 		@Override
 		public boolean equals(Object obj) {
 	        if (obj instanceof ColorPoint) {
 	            ColorPoint other = (ColorPoint)obj;
 	            return color.equals(other.color) && super.equals(other);
 	        }
 	        else if (obj instanceof Point) {
 	        	/*
 	        	 * If it's a Point, but not a ColorPoint,
 	        	 * let's just call the Point's equals method!
 	        	 */
 	            Point other = (Point)obj;
 	            return other.equals(this);
 	        }
 	        return false;
 		}
 	}	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	private Point simplePoint     = new      Point(0, 1);
	private ColorPoint colorPoint = new ColorPoint(0, 1, Color.INDIGO);
 	
 	/*
 	 * It's symmetrical now!
 	 */
 	@Test
 	public void symmetry() {
 		assertTrue(simplePoint.equals(colorPoint));
 		assertTrue(colorPoint.equals(simplePoint));
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	/*
 	 * However... is it transitive?
 	 */
 	private ColorPoint redPoint   = new ColorPoint(0, 1, Color.RED);
 	private ColorPoint greenPoint = new ColorPoint(0, 1, Color.GREEN);
 	
 	@Ignore
 //	@Test
 	public void transitivity() {
 		assertTrue(redPoint.equals(simplePoint));   // x == y
 		assertTrue(simplePoint.equals(greenPoint)); // y == z
 		
 		assertTrue(redPoint.equals(greenPoint));    // x == z
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	/*
 	 * Again... why do we care?
 	 */
 	private List<Point> uniquePointList = new ArrayList<>();
 	public void addToUniquePointList(Point p) {
 		if (!uniquePointList.contains(p)) {
 			uniquePointList.add(p);
 		}
 	}
 	
 	@Ignore
 //	@Test
 	public void how_many_items_in_this_collection() {
 		addToUniquePointList(redPoint);
 		addToUniquePointList(simplePoint);
 		addToUniquePointList(greenPoint);
 		assertEquals(/*???*/ -1, uniquePointList.size());
 	}
 	
 	@Ignore
 //	@Test
 	public void and_how_many_in_this_one() {
 		addToUniquePointList(simplePoint);
 		addToUniquePointList(redPoint);
 		addToUniquePointList(greenPoint);
 		assertEquals(/*???*/ -1, uniquePointList.size());
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	/*
 	 * Again: EqualsVerifier catches this.
 	 */
 	@Ignore
 //	@Test
 	public void equalsverifier() {
 		EqualsVerifier.forClass(ColorPoint.class)
 				.verify();
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 }
