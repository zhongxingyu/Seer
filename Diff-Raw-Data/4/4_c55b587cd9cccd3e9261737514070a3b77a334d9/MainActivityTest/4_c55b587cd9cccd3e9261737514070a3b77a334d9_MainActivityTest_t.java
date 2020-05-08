 package ru.free0u.calculator.test;
 
 import junit.framework.Assert;
 import ru.free0u.calculator.MainActivity;
 import ru.free0u.calculator.MathParser;
 
 import android.test.ActivityInstrumentationTestCase2;
 
 public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {
 
 	MathParser mp;
 	
 	public MainActivityTest() {
 		super(MainActivity.class);
 	}
 	
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		mp = new MathParser();
 	}
 	
 
 	public void testTrue() {
 		Assert.assertTrue(true);
     }
 	
 	public void testCase0() {
 		String exp = "0";
 		double res = 0;
 		
 		Assert.assertTrue(mp.equalDouble(res, mp.evaluate(exp)));
     }
 	
 	public void testCase1() {
 		String exp = "2+2";
 		double res = 4;
 		
 		Assert.assertTrue(mp.equalDouble(res, mp.evaluate(exp)));
     }
 	
 	public void testCase2() {
 		String exp = "2+2*2";
 		double res = 6;
 		
 		Assert.assertTrue(mp.equalDouble(res, mp.evaluate(exp)));
     }
 	
 	public void testCase3() {
 		String exp = "-1";
 		double res = -1;
 		
 		Assert.assertTrue(mp.equalDouble(res, mp.evaluate(exp)));
     }
 	
 	public void testCase4() {
 		String exp = "+2";
 		double res = 2;
 		
 		Assert.assertTrue(mp.equalDouble(res, mp.evaluate(exp)));
     }
 	
 	
 	public void testCase5() {
 		String exp = "+-2";
		double res = -2;
 		
 		Assert.assertTrue(mp.equalDouble(res, mp.evaluate(exp)));
     }
 	
	
 	public void testCase6() {
 		String exp = "-+2";
 		double res = -2;
 		
 		Assert.assertTrue(mp.equalDouble(res, mp.evaluate(exp)));
     }
 	
 	
 	
 	
 	
 	
 }
