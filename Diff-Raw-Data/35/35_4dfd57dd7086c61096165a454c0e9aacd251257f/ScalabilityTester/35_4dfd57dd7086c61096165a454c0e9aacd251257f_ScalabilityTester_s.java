 package eu.choreos.vv;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import eu.choreos.vv.aggregations.Aggregator;
 import eu.choreos.vv.aggregations.Percentile;
 import eu.choreos.vv.chart.ScalabilityReportChart;
 import eu.choreos.vv.increasefunctions.LinearIncrease;
 import eu.choreos.vv.loadgenerator.LoadGenerator;
 import eu.choreos.vv.loadgenerator.UniformLoadGenerator;
 import eu.choreos.vv.loadgenerator.executable.LatencyMeasurementExecutable;
 
 public abstract class ScalabilityTester implements ScalabilityTestItem {
 	
 	private int numberOfExecutions;
 	
 	private Exception lastException;
 	
 	private List<ScalabilityReport> reports;
 	
 	public void setUp() throws Exception {}
 
 	public void resourceScaling(int resourceQuantity) throws Exception {}
 	
 	public void beforeTest() throws Exception {}
 	
 	public void test() throws Exception {}
 	
 	public void tearDown() throws Exception {}
 	
 	public ScalabilityTester() {
 		reports = new ArrayList<ScalabilityReport>();
 	}
 	
 	
 	@Override
 	public double test(Number... params) throws Exception {
		int requestsPerMinute = (Integer)params[0];
		int resourceQuantity = (Integer)params[1];
 		LoadGenerator loadGen = new UniformLoadGenerator();
 		List<Number> results = new ArrayList<Number>();
 
 		resourceScaling(resourceQuantity);
 		
 		try {
 			results = loadGen.execute(numberOfExecutions, requestsPerMinute, new LatencyMeasurementExecutable() {
 				@Override
 				public void setUp() throws Exception {
 					beforeTest();
 				}
 				
 				@Override
 				public void experiment() throws Exception {
 					test();
 				}
 			});
 			
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		Aggregator percentile = new Percentile(95);
 		return percentile.aggregate(results);
 	}
 	
 	public boolean run(String name, int timesToRun, double latencyLimit, int numberOfExecutionsPerTest, int initialRequestsPerMinute, int inititalResoucesQuantity) {
 		numberOfExecutions = numberOfExecutionsPerTest;
 		ScalabilityTest scalabilityTtest = new ScalabilityTest(this, name, timesToRun, latencyLimit, new LinearIncrease());
 		scalabilityTtest.setInitialParametersValues(initialRequestsPerMinute, inititalResoucesQuantity);
 		
 		ScalabilityReport report;
 		try {
 			setUp();
 			report = scalabilityTtest.executeIncreasingParams();
 			tearDown();
 			reports.add(report);
 		} catch (Exception e) {
 			lastException = e;
 			return false;
 		}
 		return true;
 	}
 	
 	public void showChart() {
 		ScalabilityReportChart chart = new ScalabilityReportChart();
 		chart.createChart(reports);		
 	}
 	
 	public Exception getLastException() {
 		return lastException;
 	}
 
 }
