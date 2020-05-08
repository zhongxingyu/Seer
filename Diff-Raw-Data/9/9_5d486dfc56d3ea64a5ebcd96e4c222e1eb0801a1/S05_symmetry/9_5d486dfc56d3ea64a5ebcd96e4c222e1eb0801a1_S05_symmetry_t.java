 package nl.jqno.equalsverifier.talk;
 
 import static org.junit.Assert.assertTrue;
 import nl.jqno.equalsverifier.talk.helper.Color;
 import nl.jqno.equalsverifier.talk.helper.EqualsVerifier;
 
 import org.junit.Ignore;
 import org.junit.Test;
 
 public class S05_symmetry {
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	/*
 	 * Let's start with an immutable Point.
 	 */
 	public static class Point {
 		protected final int x;
 		protected final int y;
 		
 		public Point(int x, int y) {
 			this.x = x;
 			this.y = y;
 		}
 
 		@Override
 		public int hashCode() {
 			return 31 * (31 + x) + y;
 		}
 
 		@Override
 		public String toString() {
 			return String.format("Point: %s,%s", x, y);
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (!(obj instanceof Point)) {
 				return false;
 			}
 			Point other = (Point)obj;
 			return x == other.x && y == other.y;
 		}
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	/*
 	 * We'll derive a subclass called ColorPoint.
 	 */
 	public class ColorPoint extends Point {
 		private final Color color;
 		
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
 		
 		/*
 		 * Think a moment: what could be wrong with this equals method?
 		 * It _does_ call super!
 		 */
 		@Override
 		public boolean equals(Object obj) {
 			if (!(obj instanceof ColorPoint)) {
 				return false;
 			}
 			ColorPoint other = (ColorPoint)obj;
 			return color.equals(other.color) && super.equals(obj);
 		}
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	/*
 	 * OK, let's define some test data.
 	 */
 	private Point simplePoint     = new      Point(0, 1);
	private ColorPoint colorPoint = new ColorPoint(0, 1, Color.RED);
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	/*
 	 * Are they symmetrical?
 	 */
 	@Test
 	public void simple_point_is_equal_to_color_point() {
 		assertTrue(simplePoint.equals(colorPoint));
 	}
 	
 	@Test
 	public void color_point_is_equal_to_simple_point() {
 		assertTrue(colorPoint.equals(simplePoint));
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	/*
 	 * Why do we care about symmetry?
 	 * 
 	 * The invocation order now defines whether equals will succeed or not. Ouch!
 	 */
 	
 	
 	/*
 	 * EqualsVerifier catches this, too.
 	 */
 	@Ignore
 //	@Test
 	public void equalsverifier() {
 		EqualsVerifier.forClass(ColorPoint.class)
 				.verify();
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 }
