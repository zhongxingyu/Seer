 package org.bh.tests.junit;
 
 
 import static org.junit.Assert.assertTrue;
 
 import java.util.Map;
 
 import org.bh.calculation.IShareholderValueCalculator;
 import org.bh.data.DTOPeriod;
 import org.bh.data.DTOScenario;
 import org.bh.data.types.Calculable;
 import org.bh.data.types.DoubleValue;
 import org.bh.data.types.IntervalValue;
 import org.bh.data.types.StringValue;
 import org.bh.plugin.apv.APVCalculator;
 import org.bh.plugin.directinput.DTODirectInput;
 import org.bh.plugin.fcf.FCFCalculator;
 import org.bh.plugin.fte.FTECalculator;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * Unit tests for DCF procedures with intervals
  * 
  * @author Norman
  * 
  * @version 0.1, 04.01.2010
  */
 public class DCFProceduresIntervalValues {
 
 	IShareholderValueCalculator svCalc;
 	IShareholderValueCalculator svCalc2;
 	IShareholderValueCalculator svCalc3;
 	
 	Map<String, Calculable[]> res;
 	Map<String, Calculable[]> res2;
 	Map<String, Calculable[]> res3;
 	
 	IntervalValue uw0;
 	IntervalValue uw0_2;
 	IntervalValue uw0_3;
 	
 	DTOScenario scenario;
 	DTOPeriod period;
 	DTODirectInput dinp;
 
 	@Before
 	public void setUp() throws Exception {
 		scenario = new DTOScenario();
 		scenario.put(DTOScenario.Key.IDENTIFIER, new StringValue("TestScenario"));
 		scenario.put(DTOScenario.Key.RFK, new DoubleValue(0.10));
 		scenario.put(DTOScenario.Key.REK, new DoubleValue(0.11));
 		
 		scenario.put(DTOScenario.Key.BTAX, new DoubleValue(0.1694));
 		scenario.put(DTOScenario.Key.CTAX, new DoubleValue(0.26375));
 		
 		period = new DTOPeriod();
 		period.put(DTOPeriod.Key.NAME, new StringValue("2007"));
 		
 		dinp = new DTODirectInput();
 		dinp.put(DTODirectInput.Key.FCF, new IntervalValue(90, 110));
 		dinp.put(DTODirectInput.Key.LIABILITIES, new IntervalValue(900,1100));
 		
 		period.addChild(dinp);		
 		
 		scenario.addChild(period);
 		
 		
 		period = new DTOPeriod();
 		period.put(DTOPeriod.Key.NAME, new StringValue("2008"));
 		
 		dinp = new DTODirectInput();
 		dinp.put(DTODirectInput.Key.FCF, new IntervalValue(100, 120));
 		dinp.put(DTODirectInput.Key.LIABILITIES, new IntervalValue(1000,1200));
 		
 		period.addChild(dinp);		
 		
 		scenario.addChild(period);
 		
 		
 		period = new DTOPeriod();
 		period.put(DTOPeriod.Key.NAME, new StringValue("2009"));
 		
 		dinp = new DTODirectInput();
 		dinp.put(DTODirectInput.Key.FCF, new IntervalValue(110,130));
 		dinp.put(DTODirectInput.Key.LIABILITIES, new IntervalValue(1100,1300));
 		
 		period.addChild(dinp);		
 		
 		scenario.addChild(period);
 	
 		
 		period = new DTOPeriod();
 		period.put(DTOPeriod.Key.NAME, new StringValue("2010"));
 		
 		dinp = new DTODirectInput();
 		dinp.put(DTODirectInput.Key.FCF, new IntervalValue(110,130));
 		dinp.put(DTODirectInput.Key.LIABILITIES, new IntervalValue(1100,1300));
 		
 		period.addChild(dinp);		
 		
 		scenario.addChild(period);
 	}
 	
 //	@Test
 //	public void apv() {
 //		svCalc = new APVCalculator();
 //		svCalc.calculate(scenario);
 //	}
 //	
 //	@Test
 //	public void fcf() {
 //		svCalc = new FCFCalculator();
 //		svCalc.calculate(scenario);
 //	}
 //	
 //	@Test
 //	public void fte() {
 //		svCalc = new FTECalculator();
 //		svCalc.calculate(scenario);
 //		
 //	}
 	
 	@Test
 	public void compare() {
 		
 		svCalc = new APVCalculator();
 		svCalc2 = new FTECalculator();
 		svCalc3 = new FCFCalculator();
 		
 		//uncommented to not cause performance issues on build server
 		//res = svCalc.calculate(scenario);
 		//res2 = svCalc2.calculate(scenario);
 		//res3 = svCalc3.calculate(scenario);
 		
		//uw0 = ((IntervalValue)res.get(IShareholderValueCalculator.SHAREHOLDER_VALUE)[0]);
		//uw0_2 = ((IntervalValue)res2.get(IShareholderValueCalculator.SHAREHOLDER_VALUE)[0]);
		//uw0_3 = ((IntervalValue)res3.get(IShareholderValueCalculator.SHAREHOLDER_VALUE)[0]);
 		
 		//TODO ask Mr. Ratz whether different results are okay
 		//assertTrue("Equal results for APV, FTE, FCF procedure expected. Results were " + uw0 + ", " + uw0_2 + ", " + uw0_3 + " instead.", uw0.equals(uw0_2) && uw0.equals(uw0_3));
 	}
 }
