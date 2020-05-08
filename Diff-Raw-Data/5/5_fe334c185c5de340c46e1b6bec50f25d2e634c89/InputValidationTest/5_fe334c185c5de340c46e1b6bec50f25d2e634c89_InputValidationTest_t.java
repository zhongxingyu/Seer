 package solarpowertest;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import solarpower.servlets.InputValidation;
 import solarpower.servlets.ValidationException;
 
 public class InputValidationTest{
 	
 	private InputValidation validate;
 	
 	@Before
     public void setUp() {
 		validate = new InputValidation();
     }
 	
 	// Enter a negative system size
 	@Test(expected=ValidationException.class)
 	public void negativeSystemSize() throws ValidationException {
 		validate.systemSize(-3.0);
 	}
 	
 	// Enter a negative system cost
 	@Test(expected=ValidationException.class)
 	public void negativeSystemCost() throws ValidationException {
 		validate.systemCost(-1000.0);
 	}
 	
 	// Enter a negative percentage of panel on North 
 	@Test(expected=ValidationException.class)
 	public void negativePercentNorth() throws ValidationException {
 		validate.northRoofDensity(-15.5);
 	}	
 	
 	// Set the panel's density to be 106%
 	@Test(expected=ValidationException.class)
 	public void invalidPercentNorth() throws ValidationException {
 		validate.northRoofDensity(106.0);
 	}
 	
 	// Enter a negative percentage of panel on West
 	@Test(expected=ValidationException.class)
 	public void negativePercentWest() throws ValidationException {
 		validate.westRoofDensity(-27.5);
 	}	
 		
 	// Set the panel's density to be 150.%
 	@Test(expected=ValidationException.class)
 	public void invalidPercentWest() throws ValidationException {
		validate.westRoofDensity(150.5);
 	}
 	
 	// Enter a negative percentage represented for the Efficiency Loss (North Roof)
 		@Test(expected=ValidationException.class)
 		public void negativeNorthRoofEfficiencyLoss() throws ValidationException {
 			validate.northRoofEfficiencyLoss(-27.1);
 		}
 	
 	// Enter a percentage over 100% for the Efficiency Loss (North Roof)
 		@Test(expected=ValidationException.class)
 		public void invalidNorthRoofEfficiencyLoss() throws ValidationException {
 			validate.northRoofEfficiencyLoss(200.1);
 		}		
 	
 	// Enter a negative percentage represented for the Efficiency Loss (West Roof)
 		@Test(expected=ValidationException.class)
 		public void negativeWestRoofEfficiencyLoss() throws ValidationException {
 			validate.westRoofEfficiencyLoss(-17.5);
 		}
 	
 	// Enter a percentage over 100% for the Efficiency Loss ( Roof)
 		@Test(expected=ValidationException.class)
 		public void invalidWestRoofEfficiencyLoss() throws ValidationException {
			validate.westRoofEfficiencyLoss(210.1);
 		}
 	
 		
 	// Enter a negative percentage represented for the Panel Age Efficiency Loss
 		@Test(expected=ValidationException.class)
 		public void negativePanelAgeEfficiencyLoss() throws ValidationException {
 			validate.panelAgeEfficiencyLoss(-66.5);
 		}
 	
 	// Enter a percentage over 100% for the Panel Age Efficiency Loss
 		@Test(expected=ValidationException.class)
 		public void invalidPanelAgeEfficiencyLoss() throws ValidationException {
 			validate.panelAgeEfficiencyLoss(110.1);
 		}
 		
 	// Enter a negative Inverter Replacement Cost
 	@Test(expected=ValidationException.class)
 	public void negativeInverterReplacementCost() throws ValidationException {
 		validate.inverterReplacementCost(-4000.0);
 	}
 	
 	// Enter a negative Average Daily Hours of Sunlight
 	@Test(expected=ValidationException.class)
 	public void negativeSunlightDailyHours() throws ValidationException {
 		validate.sunlightDailyHours(-3.0);
 	}
 	
 	// Set the Average Daily Hours of Sunlight to be 25 hours
 	@Test(expected=ValidationException.class)
 	public void invalidSunlightDailyHours() throws ValidationException {
 		validate.sunlightDailyHours(25.0);
 	}
 
 	// Enter a negative Daily Average Usage
 	@Test(expected=ValidationException.class)
 	public void negativedailyAvgUsage() throws ValidationException {
 		validate.dailyAvgUsage(-40.0);
 	}
 	
 	// Enter a negative Day Time Hourly Usage
 	@Test(expected=ValidationException.class)
 	public void negativeDayTimeHourlyUsage() throws ValidationException {
 		validate.dayTimeHourlyUsage(-1.0);
 	}
 
 	// Enter a negative Annual Tariff 11 Cost
 	@Test(expected=ValidationException.class)
 	public void negativeAnnualTariff11Cost() throws ValidationException {
 		validate.annualTariff11Cost(-2460.0);
 	}
 
 	// Enter a negative Annual Tariff 33 Cost
 	@Test(expected=ValidationException.class)
 	public void negativeAnnualTariff33Cost() throws ValidationException {
 		validate.annualTariff33Cost(-185.0);
 	}
 
 	// Enter a negative Tariff 11 Fee
 	@Test(expected=ValidationException.class)
 	public void negativeTariff11Fee() throws ValidationException {
 		validate.tariff11Fee(-0.1941);
 	}
 
 	// Enter a negative Tariff 33 Fee
 	@Test(expected=ValidationException.class)
 	public void negativeTariff33Fee() throws ValidationException {
 		validate.tariff33Fee(-0.11);
 	}
 
 	// Enter a negative Feed in Fee
 	@Test(expected=ValidationException.class)
 	public void negativeFeedInFee() throws ValidationException {
 		validate.feedInFee(-0.5);
 	}
 
 	// Enter a negative Annual Tariff Increase
 	@Test(expected=ValidationException.class)
 	public void negativeAnnualTariffIncrease() throws ValidationException {
 		validate.annualTariffIncrease(-5.0);
 	}
 
 	// Enter a negative Investment Return Rate
 	@Test(expected=ValidationException.class)
 	public void negativeInvestmentReturnRate() throws ValidationException {
 		validate.investmentReturnRate(-5.0);
 	}
 
 	
 }
