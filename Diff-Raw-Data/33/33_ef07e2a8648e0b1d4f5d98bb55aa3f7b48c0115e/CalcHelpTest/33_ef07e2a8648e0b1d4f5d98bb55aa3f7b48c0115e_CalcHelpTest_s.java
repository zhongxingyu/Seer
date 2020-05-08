 package tests;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Test;
 
 import structures.CalcHelp;
 import structures.Point2d;
 
 /**
  * JUnit tests for critical methods in the <code>CalcHelp<code> class.
  * @author Sean Lewis\
  */
 public class CalcHelpTest {
 
 	static final double epsilon = 1e-8;
 
 	@Test
 	public void getAccelerationTest() {
 		Point2d source = new Point2d(0, 0);
 
 		Point2d loc = new Point2d(0, 1);
 		assertEquals(1, CalcHelp.getAcceleration(source, loc, 1, 1), epsilon);
 
 		loc = new Point2d(3, 4);
 		assertEquals(10.0 * 20.0 / 25.0,
 				CalcHelp.getAcceleration(source, loc, 10, 20), epsilon);
 
 		loc = new Point2d(-5, -5);
 		assertEquals(-5.0 * 3.0 / 50.0,
 				CalcHelp.getAcceleration(source, loc, -5, 3), epsilon);
 	}
 
 	@Test
 	public void quadrantTest() {
 		// 45, 135, 225, 315 degree angle tests
 		assertEquals(1, CalcHelp.quadrant(1 * Math.PI / 4), epsilon);
 		assertEquals(2, CalcHelp.quadrant(3 * Math.PI / 4), epsilon);
 		assertEquals(3, CalcHelp.quadrant(5 * Math.PI / 4), epsilon);
 		assertEquals(4, CalcHelp.quadrant(7 * Math.PI / 4), epsilon);
 
 		// -45, 405 degree tests
 		assertEquals(4, CalcHelp.quadrant(-1 * Math.PI / 4), epsilon);
 		assertEquals(1, CalcHelp.quadrant(9 * Math.PI / 4), epsilon);
 
 		// Edge case tests
 		assertEquals(1, CalcHelp.quadrant(0), epsilon);
 		assertEquals(2, CalcHelp.quadrant(Math.PI / 2), epsilon);
 		assertEquals(3, CalcHelp.quadrant(Math.PI), epsilon);
 		assertEquals(4, CalcHelp.quadrant(3 * Math.PI / 2), epsilon);
 	}
 
 	public void angleTest() {
 		double dx, dy, angle;
 		Point2d p1, p2;
 
 		dx = 1.0;
 		dy = 0.0;
 		angle = 0.0;
 		assertEquals(angle, CalcHelp.getAngle(dx, dy), epsilon);
 
 		dx = 0.0;
 		dy = 1.0;
		angle = Math.PI / 2;
 		assertEquals(angle, CalcHelp.getAngle(dx, dy), epsilon);
 
 		dx = -1.0;
 		dy = 0.0;
 		angle = Math.PI;
 		assertEquals(angle, CalcHelp.getAngle(dx, dy), epsilon);
 
 		dx = 0.0;
 		dy = -1.0;
		angle = 3 * Math.PI / 2;
 		assertEquals(angle, CalcHelp.getAngle(dx, dy), epsilon);
 
 		dx = 0.5;
 		dy = Math.sqrt(3) / 2.0;
		angle = Math.PI / 6;
 		assertEquals(angle, CalcHelp.getAngle(dx, dy), epsilon);
 
 		dx = -1.0;
 		dy = -Math.sqrt(3);
		angle = 4 * Math.PI / 3;
 		assertEquals(angle, CalcHelp.getAngle(dx, dy), epsilon);
 
 		dx = 5 * Math.sqrt(3);
 		dy = -5.0;
		angle = 11 * Math.PI / 6;
 		assertEquals(angle, CalcHelp.getAngle(dx, dy), epsilon);
 
 		p1 = new Point2d(0, 0);
 		p2 = new Point2d(1, 0);
 		angle = 0;
 		assertEquals(angle, CalcHelp.getAngle(p1, p2), epsilon);
 
 		p1 = new Point2d(0, 0);
 		p2 = new Point2d(0, 1);
		angle = Math.PI / 2;
 		assertEquals(angle, CalcHelp.getAngle(p1, p2), epsilon);
 
 		p1 = new Point2d(0, 0);
 		p2 = new Point2d(-1, 0);
 		angle = Math.PI;
 		assertEquals(angle, CalcHelp.getAngle(p1, p2), epsilon);
 
 		p1 = new Point2d(0, 0);
 		p2 = new Point2d(0, -1);
		angle = 3 * Math.PI / 2;
		assertEquals(angle, CalcHelp.getAngle(p1, p2), epsilon);

		p1 = new Point2d(0.5, Math.sqrt(3) / 2);
		p2 = new Point2d(0, 1);
		angle = Math.PI / 6;
 		assertEquals(angle, CalcHelp.getAngle(p1, p2), epsilon);
 
 		p1 = new Point2d(1, 1);
 		p2 = new Point2d(0, 1 - Math.sqrt(3));
		angle = 4 * Math.PI / 3;
 		assertEquals(angle, CalcHelp.getAngle(p1, p2), epsilon);
 
 		p1 = new Point2d(6, 6);
 		p2 = new Point2d(6 - 5 * Math.sqrt(3), 1);
		angle = 11 * Math.PI / 6;
 		assertEquals(angle, CalcHelp.getAngle(p1, p2), epsilon);
 	}
 
 }
