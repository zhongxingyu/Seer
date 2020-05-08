 
 /**
  * Represents the cost model to use in finding
  * optimal query plan.
  * @author Jervis
  *
  */
 public class CostModel {
 	/**
 	 * Cost of accessing an array element rj[i]
 	 */
 	public final int r;
 	/**
 	 * Cost of performing an if test
 	 */
 	public final int t;
 	/**
 	 * Cost of performing a logical "and" test
 	 */
 	public final int l; 
 	/**
 	 * Cost of Branch mis-prediction
 	 */
 	public final int m; 
 	/**
 	 * Cost of writing an answer to the answer array
 	 */
 	public final int a;
 	/**
	 * Cost of apply function f to its arguemnt
 	 */
 	public final int f; 
 	
 	/**
	 * Creates a non-modifable Cost Model object. 
 	 * @param a - Cost of writing an answer to the answer array
 	 * @param f - Cost of applying function f to its argument
 	 * @param l - Cost of performing a logical 'and' test
 	 * @param m - Cost of branch mis-prediction
 	 * @param t - Cost of performing an if test
	 * @param r - Cost of accesing an array element rj[i]
 	 */
 	public CostModel(int a, int f, int l, int m, int t, int r) {
 		this.a = a;
 		this.f = f;
 		this.l = l;
 		this.m = m;
 		this.t = t;
 		this.r = r;
 	}
 	
 	/**
 	 * Gets a default cost model that is suitable for running
 	 * on machines in the CLIC lab. 
 	 * @return
 	 */
 	public static CostModel getDefaultCostModel() {
 		
 		int a = 2; 
 		int f = 4; 
 		int l = 1; 
 		int m = 16; 
 		int t = 2; 
 		int r = 1;
 		
 		return new CostModel(a, f, l, m, t, r);
 	}
 }
