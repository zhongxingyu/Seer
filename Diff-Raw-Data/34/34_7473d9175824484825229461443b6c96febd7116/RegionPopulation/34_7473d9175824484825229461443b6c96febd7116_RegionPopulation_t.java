 package tddd24.lab.client;
 
 public class RegionPopulation {
 
 	  private String symbol;
	  private double population;
 	  private double change;
 
 	  public RegionPopulation() {
 	  }
 
	  public RegionPopulation(String symbol, double population, double change) {
 	    this.symbol = symbol;
	    this.population = population;
 	    this.change = change;
 	  }
 
 	  public String getSymbol() {
 	    return this.symbol;
 	  }
 
	  public double getPopulation() {
	    return this.population;
 	  }
 
 	  public double getChange() {
 	    return this.change;
 	  }
 
 	  public double getChangePercent() {
	    return 100.0 * this.change / this.population;
 	  }
 
 	  public void setSymbol(String symbol) {
 	    this.symbol = symbol;
 	  }
 
	  public void setPopulation(double population) {
	    this.population = population;
 	  }
 
 	  public void setChange(double change) {
 	    this.change = change;
 	  }
 }
