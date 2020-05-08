 package states;
 
 public class ProbabilityFinder {
 	static int getReadyScale(int gdp, float unemployment, int size, int population, boolean doesBorderCountry){
		if(doesBorderCountry)
			return Math.abs((int) ((gdp + size + (population - (population * unemployment))) * 2)) - 1;
		else
			return Math.abs((int) (gdp + size + (population - (population * unemployment))));
 	}
 }
