 import static org.junit.Assert.*;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 
 public class TriangleTest {
 
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 	@Test
 	public void testIsItARightTriangle(){
 		double a=-1,b=2,c=2;
 		assertFalse(CarlosTriangleProgram.isItARightTriangle(a,b,c));
 	}
 	@Test
 	public void testIsItIsosceles()
 	{
		double a=3,b=3,c=4;
 		assertEquals("The triangle is isosceles",CarlosTriangleProgram.whatTypeOfTriangleIsThis(a,b,c));
 	}
 	@Test
	public void testIsItScalene()
 	{
 		double a=2,b=3,c=4;
 		assertEquals("The triangle is scalene",CarlosTriangleProgram.whatTypeOfTriangleIsThis(a,b,c));
 	}
 	@Test
 	public void testIsItEquilateral()
 	{
 		double a=5,b=5,c=5;
 		assertEquals("The triangle is equilateral",CarlosTriangleProgram.whatTypeOfTriangleIsThis(a,b,c));
 	}
 	@Test
 	public void testIsARightTriangleWithNegativeLengths()
 	{
 		double a=-5, b=-5,c=-5;
 		assertFalse(CarlosTriangleProgram.isItARightTriangle(a,b,c));
 	}
 	@Test
 	public void testIsARightTriangleWithZeroLengths()
 	{
 		double a=0,b=0,c=0;
 		assertFalse(CarlosTriangleProgram.isItARightTriangle(a,b,c));
 	}
 	@Test
 	public void testIsItIsoscelesWithZeroLengths()
 	{
 		double a=0,b=0,c=0;
 		assertFalse("The triangle is isosceles".equals(CarlosTriangleProgram.whatTypeOfTriangleIsThis(a,b,c)));
 		
 	}
 	@Test
 	public void testIsItScaleneWithZeroLengths()
 	{
 		double a=0,b=0,c=0;
 		assertFalse("The triangle is scalene".equals(CarlosTriangleProgram.whatTypeOfTriangleIsThis(a,b,c)));
 		
 	}
 	@Test
 	public void testIsItEquilateralWithZeroLengths()
 	{
 		double a=0,b=0,c=0;
 		assertFalse("The triangle is equilateral".equals(CarlosTriangleProgram.whatTypeOfTriangleIsThis(a,b,c)));
 		
 	}
 }
