 package performance;
 
 import mikera.util.FloatMaths;
 
 import com.google.caliper.Runner;
 import com.google.caliper.SimpleBenchmark;
 
 public class Benchmark extends SimpleBenchmark {
 	
 	@SuppressWarnings("unused")
 	public void timeFloatCos(int reps) {
 		for (int i=0; i<reps; i++) {
 			float c=FloatMaths.cos(2.3f);
 		}
 	}
 	
 	@SuppressWarnings("unused")
 	public void timeDoubleCos(int reps) {
 		for (int i=0; i<reps; i++) {
 			double c=Math.cos(2.5);
 		}
 	}
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		new Benchmark().run();
 	}
 
 	private void run() {
 		new Runner().run(new String[] {this.getClass().getCanonicalName()});
 	}
 
 
 }
