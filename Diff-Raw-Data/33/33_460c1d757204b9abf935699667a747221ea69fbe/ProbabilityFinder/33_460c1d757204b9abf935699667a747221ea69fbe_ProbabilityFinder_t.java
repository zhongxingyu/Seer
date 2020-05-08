 package states;
 
 public class ProbabilityFinder {
 	static int getReadyScale(int gdp, float unemployment, int size, int population, boolean doesBorderCountry){
		if(doesBorderCountry){
			int actual = (int) ((gdp + size + (population * (1.0F - unemployment))) * 2);
			String trimmed = Integer.toString(actual);
			trimmed = trimmed.substring(0, trimmed.length() - 4);
			return Integer.parseInt(trimmed);
		}
		else {
			int actual = (int) (gdp + size + (population * (1.0F - unemployment)));
			String trimmed = Integer.toString(actual);
			trimmed = trimmed.substring(0, trimmed.length() - 4);
			return Integer.parseInt(trimmed);
		}
 	}
 }
