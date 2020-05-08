 package solarPowerPackage;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 public class InsolationCalculationTests {
 
 	InsolationCalculation calculator = new InsolationCalculation();
 	
 	
 	//test various values
 	@Test
 	public void testDegreesToRadians(){
 		double testDegrees = 180;
 		double expectedRadians = (testDegrees * Math.PI) / 180;
 		double actualRadians = calculator.DegreesToRadians(testDegrees);
 		String expectedString = "Expected " + expectedRadians + " but was: " + actualRadians;
 		assertEquals(expectedString, expectedRadians, actualRadians, 0.1);
 	}
 	
 	@Test
 	public void testDegreesToRadians2(){
 		double testDegrees = 0;
 		double expectedRadians = (testDegrees * Math.PI) / 180;
 		double actualRadians = calculator.DegreesToRadians(testDegrees);
 		String expectedString = "Expected " + expectedRadians + " but was: " + actualRadians;
 		assertEquals(expectedString, expectedRadians, actualRadians, 0.1);
 	}
 	
 	@Test
 	public void testDegreesToRadians3(){
 		double testDegrees = 360;
 		double expectedRadians = (testDegrees * Math.PI) / 180;
 		double actualRadians = calculator.DegreesToRadians(testDegrees);
 		String expectedString = "Expected " + expectedRadians + " but was: " + actualRadians;
 		assertEquals(expectedString, expectedRadians, actualRadians, 0.1);
 	}
 	
 	@Test
 	public void testDegreesToRadians4(){
 		double testDegrees = 1.11111111;
 		double expectedRadians = (testDegrees * Math.PI) / 180;
 		double actualRadians = calculator.DegreesToRadians(testDegrees);
 		String expectedString = "Expected " + expectedRadians + " but was: " + actualRadians;
 		assertEquals(expectedString, expectedRadians, actualRadians, 0.1);
 	}
 	
 	@Test
 	public void testYearlyOutput(){
 		double testLatitude = 0;
 		int simulationLength = 1;
 		
 		double totalInsolation = calculator.TotalSolarInsolation(testLatitude, simulationLength);
 		
 		//System.out.println(totalInsolation);
 
 	}
 	
 	@Test
 	public void testYearlyOutput2(){
 		double testLatitude = 45;
 		int simulationLength = 1;
 		
 		double totalInsolation = calculator.TotalSolarInsolation(testLatitude, simulationLength);
 		
 		//System.out.println(totalInsolation);
 
 	}
 	
 	@Test
 	public void testYearlyOutput3(){
 		double testLatitude = 66.5;
 		int simulationLength = 1;
 		
 		double totalInsolation = calculator.TotalSolarInsolation(testLatitude, simulationLength);
 		
 		//System.out.println(totalInsolation);
 
 	}
 	
 	@Test
 	public void testYearlyOutput4(){
 		double testLatitude = -45;
 		int simulationLength = 1;
 		
 		double totalInsolation = calculator.TotalSolarInsolation(testLatitude, simulationLength);
 		
 		//System.out.println(totalInsolation);
 
 	}
 	
 	@Test
 	public void testYearlyOutput5(){
 		double testLatitude = -66.5;
 		int simulationLength = 1;
 		
 		double totalInsolation = calculator.TotalSolarInsolation(testLatitude, simulationLength);
 		
 		//System.out.println(totalInsolation);
 
 	}
 	
 	@Test
 	public void testDailyOutput(){
 		double testLatitude = 67;
 		double dayInsolation = 0;
 		for (int i = 1; i < 365; i = i + 30){
 			dayInsolation = calculator.DailyInsolation(testLatitude, i);
 			
 			//System.out.println(dayInsolation);
 		}
 	}
 	
 	@Test
 	public void testZenithAngleRange(){
 		double testDeclination = calculator.SolarDeclinaton(1);
 		double testHourAngle = calculator.HourToHourAngle(12);
 		double zenithAngle;
 		
 			zenithAngle = calculator.ZenithAngle(67, testDeclination, testHourAngle);
 			//System.out.println(zenithAngle);
 
 	}
 	@Test
 	public void testHourlyInsolationLatitudeRange(){
 		int hour = 12;
 		int day = 180;
 		double insolation;
 		for (int i = -90; i <= 90; i = i +10){
 			insolation = calculator.HourInsolaton(i, hour, day);
 			
 			String aString = "Lat: " + i + ", Insolation: " + insolation;
 			
 			//System.out.println(aString);
 			
 		}
 	}
 	
 	@Test
 	public void exampleProfitCalculation(){
 		double latitude = 28;
 		double panelArea = 10;
 		double cloudCover = 0.2;
 		double tariff = 0.25378;
 		double energyCost = 0.25378;
 		double energyUsage = 6500;
 		double panelEfficiency = 0.25;
 		
 		double profit = calculator.TotalYearlyProfit(latitude, panelArea, energyCost, tariff, energyUsage, cloudCover, panelEfficiency);
 		
 		//System.out.print(profit);
 	}
 	
 	@Test
 	public void exampleDataCalculation(){
 		
 		double latitude = 28;
 		double panelArea = 10;
 		double cloudCover = 0.2;
 		double tariff = 0.25378;
 		double energyCost = 0.25378;
 		double energyUsage = 6500;
 		double panelEfficiency = 0.25;
 		int simulationRange = 10;
 		double systemCost = 5000;
 		
 		calculator.Calculate(latitude, simulationRange, cloudCover, panelEfficiency, panelArea, energyCost, tariff, systemCost, energyUsage);
 		
 		System.out.println("Electricity generation (year): " + calculator.GetYearlyElectricityGeneration());
 		System.out.println("Electricity generation (simulation): " + calculator.GetElectricityGeneration());
 		System.out.println("Savings (year): " + calculator.GetYearlyProfit());
 		System.out.println("Savings (simulation): " + calculator.GetTotalProfit());
 		System.out.println("Return on investment after simulation period: " + calculator.GetROI());
		System.out.println("Break even year: " + calculator.GetBreakEvenYear());
 		
 	}
 	
 }
