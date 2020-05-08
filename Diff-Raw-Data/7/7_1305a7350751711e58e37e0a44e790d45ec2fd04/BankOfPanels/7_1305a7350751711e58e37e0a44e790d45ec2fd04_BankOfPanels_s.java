 package com.ssc;
 
 public class BankOfPanels {
 	
 	private Double systemSize;                        // System size in kW
 	
 	//private Double percentageOnNorthRoof;		      // percentage in decimal form
 	//private Double percentageOnWestRoof;		 	  // percentage in decimal form	
 	private Double efficiencyLossNorthRoof;		 	  // percentage in decimal form
 	private Double efficiencyLossWestRoof;		 	  // percentage in decimal form
 	private Double panelEfficiency;				 	  // percentage in decimal form
 	private Double panelAgeEfficiencyLoss;		      // percentage in decimal form
 	
 	//private Integer panelLifespan;
 	
 	private Double[] percentagesOnOrientations;       // percentages in decimal form
 	private final Integer NUMBER_OF_ORIENTATIONS = 2; // number of orientations 
 	private final Integer NORTH = 0;			      // constant for index of north
 	private final Integer WEST = 1;                   // constant for index of west
 		
 	public BankOfPanels() {
 		
 	}
 	
 	public void setSystemSize(Double input) throws SolarPowerSystemException {		
 		if (input <= 0)
 			throw new SolarPowerSystemException("System size should be greater than 0.");
 		else
 			this.systemSize = input;		
 	}
 	
 	public void setPercentagesOnOrientations(Double north, 
 			Double west) throws SolarPowerSystemException {
 		if (!isValidPercentage(north) || !isValidPercentage(west)) {
 			throw new SolarPowerSystemException("Percentages on orientations " +
 												"should be valid percentage.");
 		} else if ((north + west) != 1) {
 			throw new SolarPowerSystemException("The sum of the percentages " +
 												"on both orientations should be 1.");
 		} else {
 			this.percentagesOnOrientations = new Double[NUMBER_OF_ORIENTATIONS];
 			this.percentagesOnOrientations[NORTH] = north;
 			this.percentagesOnOrientations[WEST] = west;
 		}		
 	}
 	
 	/*
 	public void setPercentagesOnOrientations(Integer north,
 			Integer west) throws SolarPowerSystemException {		
 		this.setPercentagesOnOrientations(persentageToDecimalForm(north), persentageToDecimalForm(west));
 		
 		
 	}
 	*/
 	
 	public void setEfficiencyLossNorthRoof(Double input) throws SolarPowerSystemException {
 		if (!isValidPercentage(input)) {
 			throw new SolarPowerSystemException("Efficiency loss on north roof " +
 												"should be valid percentage.");
 		} else {
 			this.efficiencyLossNorthRoof = input;
 		}
 		
 	}	
 	
 	public void setEfficiencyLossWestRoof(Double input) throws SolarPowerSystemException {
 		if (!isValidPercentage(input)) {
 			throw new SolarPowerSystemException("Efficiency loss on west roof " +
 												"should be valid percentage.");
 		} else {
 			this.efficiencyLossWestRoof = input;
 		}
 	}
 	
 	public void setPanelEfficiency(Double input) throws SolarPowerSystemException {
 		if (!isValidPercentage(input)) {
 			throw new SolarPowerSystemException("Panel efficiency " +
 												"should be valid percentage.");
 		} else {
 			this.panelEfficiency = input;
 		}
 	}	
 	
 	public void setPanelAgeEfficiencyLoss(Double input) throws SolarPowerSystemException {
 		if (!isValidPercentage(input)) {
 			throw new SolarPowerSystemException("Panel age efficiency loss " +
 												"should be valid percentage.");
 		} else {
 			this.panelAgeEfficiencyLoss = input;
 		}
 	}
 	
 	/*
 	public void setPanelLifespan(Integer input) throws SolarPowerSystemException {
 		if (input < 1) {
 			throw new SolarPowerSystemException("Panel lifespan " +
 												"should be euqal or greater than 1.");
 		} else {
 			this.panelLifespan = input;
 		}
 	}
 	*/
 	
 	@Override
 	public String toString() {		
 		return "\n< Bank of Panels >" +
 			   "\nSystem Size:\t\t\t" +  this.systemSize +
 			   "\nPercentage on North Roof:\t" + this.percentagesOnOrientations[NORTH] +
 			   "\nPercentage on West Roof:\t" + this.percentagesOnOrientations[WEST] +
 			   "\nEfficiency Loss (North roof):\t" + this.efficiencyLossNorthRoof +
 			   "\nEfficiency Loss (west roof):\t" + this.efficiencyLossWestRoof +
 			   "\nPanel Efficiency:\t\t" + this.panelEfficiency +
 			   "\nPanel Age Efficiency Loss:\t" + this.getPanelAgeEfficiencyLoss() +			   
 			   "\n";		
 	}
 	
 	public Double getOutput() {
 		return this.getOutput(1);
 	}
 	
 	/*
 	 public Double getOutput() {
 		return this.getSystemSize() * this.getPanelEfficiency() * 
 				(this.percentagesOnOrientations[NORTH] * (1 - this.efficiencyLossNorthRoof) +
 				 this.percentagesOnOrientations[WEST] * (1 - this.efficiencyLossWestRoof));		
 	}
 	 
 	 public Double getOutPut(Integer year) {
 		return this.getOutput() * this.getPanelEfficiency(year) / this.getPanelEfficiency();
 	}
 	  
 	  
 	 */
 	
 	public Double getOutput(Integer year) {
		return this.getSystemSize() * this.getPanelEfficiency(year) * 
 				(this.getPercentageOnNorthRoof() * (1 - this.getEfficiencyLossNorthRoof()) +
				 this.getPercentageOnWestRoof() * (1 - this.getEfficiencyLossWestRoof()));
 	}
 	
 	public Double getPanelEfficiency() {
 		return this.panelEfficiency;
 	}
 	
 	public Double getPanelEfficiency(Integer year) {
 		return this.getPanelEfficiency() - 
 				this.getPanelAgeEfficiencyLoss() * (year - 1);		
 	}
 	
 	/*
 	public Integer getPanelLifespan() {
 		return this.panelLifespan;
 	}
 	*/
 	
 	public Double getPanelAgeEfficiencyLoss() {
 		return this.panelAgeEfficiencyLoss;
 	}
 	
 	public Double getSystemSize() {
 		return this.systemSize;
 	}
 	
 	public Double getPercentageOnNorthRoof() {
 		return this.percentagesOnOrientations[NORTH];
 	}
 	
 	public Double getPercentageOnWestRoof() {
 		return this.percentagesOnOrientations[WEST];
 	}
 	
 	public Double getEfficiencyLossNorthRoof() {
 		return this.efficiencyLossNorthRoof;
 	}
 	
 	public Double getEfficiencyLossWestRoof() {
 		return this.efficiencyLossWestRoof;
 	}
 	
 	/*
 	private Double persentageToDecimalForm(Integer input) {
 		return input/100.00;
 	}
 	*/
 
 	static boolean isValidPercentage (Double input) {
 		if (input >= 0.0 && input <= 1.0)		
 			return true;
 		else
 			return false;
 	}
 	
 
 	
 }
