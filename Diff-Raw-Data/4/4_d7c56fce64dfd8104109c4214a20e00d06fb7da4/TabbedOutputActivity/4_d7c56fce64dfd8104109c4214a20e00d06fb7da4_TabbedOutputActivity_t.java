 package au.edu.qut.inn372.greenhat.activity;
 
 import android.app.TabActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.widget.TabHost;
 import android.widget.TabHost.TabSpec;
 import au.edu.qut.inn372.greenhat.bean.Calculation;
 import au.edu.qut.inn372.greenhat.bean.Calculator;
 import au.edu.qut.inn372.greenhat.mediator.CalculatorMediator;
 
 public class TabbedOutputActivity extends TabActivity {
 	
 	private TabHost tabHost;
	public static final int SUMMARY_ID = 0;
 	public static final int POWER_GEN_ID = 1;
	public static final int FINANCIAL_ID = 2;
 	
 	private Calculator calculator;
 	private Calculation[] testCalculations;
 	
 	/**
 	 * Constructor - sets up tabs
 	 */
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_tabbed_output);
         
         calculator = (Calculator)getIntent().getSerializableExtra("Calculator");
         
         testCalculationsSetup();
  
         tabHost = getTabHost();
         
         addTab("Summary", this, OutputSummaryActivity.class);
         addTab("Power Generation", this, PowerGeneration.class);
         addTab("Financial", this, FinancialOutputActivity.class);
     }
 	
 	/**
 	 * Creates a new tab
 	 * @param tabName The name to be displayed for the tab
 	 * @param context The context (should be 'this')
 	 * @param newActivity The activity class to start (eg 'LocationActivity.class')
 	 */
 	private void addTab(String tabName, Context context, Class<?> newActivity) {
 		TabSpec newSpec = tabHost.newTabSpec(tabName);
 		newSpec.setIndicator(tabName);
 		Intent newIntent = new Intent(context, newActivity);
 		newSpec.setContent(newIntent);
 		tabHost.addTab(newSpec);
 	}
 	
 	/**
 	 * Retrieves the calculator bean object
 	 * @return The calculator bean
 	 */
 	public Calculator getCalculator() {
 		return calculator;
 	}
 	
 	/**
 	 * Switches to the specified tab
 	 * @param tabID ID of the tab to be switched to - ID's are public fields for this class
 	 */
 	public void switchTab(int tabID) {
 		tabHost.setCurrentTab(tabID);
 	}
 	
 	
 	/**
 	 * Test data for formulating the output screen - this will be removed when the WS call is implemented properly (and working)
 	 */
 	private void testCalculationsSetup() {
 		testCalculations = calculator.getCalculations();
 		for(int year=0; year<25; year++) {
 			double growthIndex = java.lang.Math.pow(1.05,year);
 			testCalculations[year] = new Calculation();
 			testCalculations[year].setYear(year);
 			testCalculations[year].setTariff11Fee(new Double(0.1*growthIndex));
 			testCalculations[year].setDailySolarPower(new Double(15*(2-growthIndex)));
 			testCalculations[year].setReplacementGeneration(3.0);
 			testCalculations[year].setExportedGeneration(testCalculations[year].getDailySolarPower()-testCalculations[year].getReplacementGeneration());
 			testCalculations[year].setDailySaving(testCalculations[year].getTariff11Fee()*testCalculations[year].getReplacementGeneration() + 0.5*testCalculations[year].getExportedGeneration());
 			testCalculations[year].setAnnualSaving(testCalculations[year].getDailySaving()*365);
 			if (year==0) {
 				testCalculations[year].setCumulativeSaving(testCalculations[year].getAnnualSaving());
 			}
 			else {
 				testCalculations[year].setCumulativeSaving(testCalculations[year].getAnnualSaving()+testCalculations[year-1].getCumulativeSaving());
 			}
 		}
 		
 		calculator.setSolarPower(18.0);
 		calculator.getEquipment().setSize(4.5);
 		calculator.getEquipment().setCost(15000);
 		
 	}
 	
 }
