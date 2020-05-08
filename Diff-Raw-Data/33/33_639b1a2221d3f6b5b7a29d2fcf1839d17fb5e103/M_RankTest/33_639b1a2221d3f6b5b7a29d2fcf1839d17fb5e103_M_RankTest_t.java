 package backend.computations.operations;
 
 import backend.blocks.*;
 import static org.junit.Assert.*;
import backend.blocks.Countable.DisplayType;
 import org.junit.*;
 
 import backend.computations.infrastructure.*;
 
 public class M_RankTest
 {
     @Test
 	public void size1() throws Exception
 	{
 		Double[][] v=new Double[1][1];
 		v[0][0]=new Double(5);
		Matrix m=new Matrix(DisplayType.DECIMAL,v);
 		M_Rank r=new M_Rank(m);
 		Solution s=r.getSolution();
 		assertTrue(((Scalar)(s.getAnswer())).getValue()==1);
 	}
 
 	@Test
 	public void size2x2() throws Exception
 	{
 		Double[][] v=new Double[2][2];
 		v[0][0]=new Double(2);
 		v[1][1]=new Double(2);
 		v[0][1]=new Double(0);
 		v[1][0]=new Double(1);
		Matrix m=new Matrix(DisplayType.DECIMAL,v);
 		M_Rank d=new M_Rank(m);
 		Solution s=d.getSolution();
 		assertTrue(((Scalar)(s.getAnswer())).getValue()==2);
 	}
 
 	@Test
 	public void nullvalue()
 	{
 		Double[][] v=new Double[2][2];
 		v[0][0]=new Double(5);
 		v[0][1]=new Double(10);
 		v[1][0]=null;
 		v[1][1]=new Double(2);
		Matrix m=new Matrix(DisplayType.DECIMAL,v);
 		try
 		{
 			new M_Rank(m);
			fail();
 		}
 		catch (Exception e)
		{}
 	}
 
 	@Test
 	public void size2x1() throws Exception
 	{
 		Double[][] v=new Double[2][1];
 		v[0][0]=new Double(2);
 		v[1][0]=new Double(1);
		Matrix m=new Matrix(DisplayType.DECIMAL,v);
 		M_Rank d=new M_Rank(m);
 		Solution s=d.getSolution();
 		assertTrue(((Scalar)(s.getAnswer())).getValue()==1);
 	}
 
 	@Test
 	public void size1x2() throws Exception
 	{
 		Double[][] v=new Double[1][2];
 		v[0][0]=new Double(33);
 		v[0][1]=new Double(6);
		Matrix m=new Matrix(DisplayType.DECIMAL,v);
 		M_Rank d=new M_Rank(m);
 		Solution s=d.getSolution();
 		assertTrue(((Scalar)(s.getAnswer())).getValue()==1);
 	}
 
 	@Test
 	public void ref3x3() throws Exception
 	{
 		Double[][] v=new Double[3][3];
 		v[0][0]=new Double(1);
 		v[1][0]=new Double(2);
 		v[2][0]=new Double(3);
 		v[0][1]=new Double(0);
 		v[1][1]=new Double(3);
 		v[2][1]=new Double(4);
 		v[0][2]=new Double(0);
 		v[1][2]=new Double(0);
 		v[2][2]=new Double(-9);
		Matrix m=new Matrix(DisplayType.DECIMAL,v);
 		M_Rank d=new M_Rank(m);
 		Solution s=d.getSolution();
 		assertTrue(((Scalar)(s.getAnswer())).getValue()==3);
 	}
 
 	@Test
 	public void lowerrank3x3() throws Exception
 	{
 		Double[][] v=new Double[3][3];
 		v[0][0]=new Double(1);
 		v[1][0]=new Double(2);
 		v[2][0]=new Double(3);
 		v[0][1]=new Double(0);
 		v[1][1]=new Double(0);
 		v[2][1]=new Double(4);
 		v[0][2]=new Double(0);
 		v[1][2]=new Double(0);
 		v[2][2]=new Double(-9);
		Matrix m=new Matrix(DisplayType.DECIMAL,v);
 		M_Rank d=new M_Rank(m);
 		Solution s=d.getSolution();
 		assertTrue(((Scalar)(s.getAnswer())).getValue()==2);
 	}
 
 	@Test
 	public void lowestrank3x3() throws Exception
 	{
 		Double[][] v=new Double[3][3];
 		v[0][0]=new Double(1);
 		v[1][0]=new Double(2);
 		v[2][0]=new Double(3);
 		v[0][1]=new Double(0);
 		v[1][1]=new Double(0);
 		v[2][1]=new Double(0);
 		v[0][2]=new Double(0);
 		v[1][2]=new Double(0);
 		v[2][2]=new Double(0);
		Matrix m=new Matrix(DisplayType.DECIMAL,v);
 		M_Rank d=new M_Rank(m);
 		Solution s=d.getSolution();
 		assertTrue(((Scalar)(s.getAnswer())).getValue()==1);
 	}
 }
