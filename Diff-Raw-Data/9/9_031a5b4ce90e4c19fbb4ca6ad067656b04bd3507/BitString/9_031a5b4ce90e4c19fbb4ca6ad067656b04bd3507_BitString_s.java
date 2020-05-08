 package nature;
 
 import java.util.Arrays;
 import java.util.Random;
 
 public class BitString {
 	private static final Random random = new Random();
 
 	private boolean[] string;
 
 	public BitString(int length, double probability) {
 		string = new boolean[length];
 		for (int i = 0; i < string.length; i++) {
 			string[i] = (Math.random() < probability);
 		}
 	}
 
 	public BitString(boolean[] string) {
 		this.string = string;
 	}
 
 	public BitString(String textString) {
 		string = new boolean[textString.length()];
 		for (int i = 0; i < textString.length(); i++) {
 			char c = textString.charAt(i);
 			string[i] = (c == '0' ? false : true);
 		}
 	}
 
 	public int length() {
 		return string.length;
 	}
 
 	public int numberOfOnes() {
 		int ones = 0;
 		for (boolean bit : string) {
 			if (bit) ones++;
 		}
 		return ones;
 	}
 
 	public int numberOfLeadingOnes() {
 		int ones = 0;
 		for (boolean bit : string) {
 			ones++;
 			if (!bit) return ones;
 		}
 		return ones;
 	}
 
 	public double positionOfOnes() {
 		int n = string.length;
 		int sum = 0;
 		int ones = 0;
 
 		for (int i = 0; i < n; i++) {
 			if (string[i]) {
 				sum += i;
 				ones++;
 			}
 		}
 
 		double avg = (double) sum / n;
 		double min = (ones - 1) * ones / 2.0 / n;
 		double max = ((n - 1) * n - (n - ones - 1) * (n - ones)) / 2.0 / n;
		double ratio = (avg - min) / (max - min);
 
		return ratio;
 	}
 
 	public BitString globalMutation(double probability) {
 		boolean[] mutation = new boolean[string.length];
 		for (int i = 0; i < string.length; i++) {
 			mutation[i] = (random.nextDouble() >= probability ? string[i] : !string[i]);
 		}
 		return new BitString(mutation);
 	}
 
 	public BitString localMutation() {
 		boolean[] mutation = Arrays.copyOf(string, string.length);
 		int i = random.nextInt(mutation.length);
 		mutation[i] = !string[i];
 		return new BitString(mutation);
 	}
 
 	public BitString constructMutation(double[] pheromone) {
 		boolean[] mutation = new boolean[string.length];
 		for (int i = 0; i < mutation.length; i++) {
 			mutation[i] = random.nextDouble() < pheromone[i];
 		}
 		return new BitString(mutation);
 	}
 
 	@Override
 	public String toString() {
 		StringBuffer sb = new StringBuffer();
 		for (boolean bit : string) {
 			sb.append((bit ? '1' : '0'));
 		}
 		return sb.toString();
 	}
 }
