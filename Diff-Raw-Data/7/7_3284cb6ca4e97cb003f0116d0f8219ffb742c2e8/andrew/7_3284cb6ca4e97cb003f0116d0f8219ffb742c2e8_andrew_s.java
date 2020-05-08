 import java.util.ArrayList;
 
 
 public class main {
 	
	static double maxVal = (int)Math.pow(2, 31); //biggest value allowed
 	static ArrayList<Integer> primes = new ArrayList<Integer>(5000);
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		generatePrimes();
 		for (int i = 0; i<15; i++){
 			int randomVal = (int)((Math.random()-.5)*2*maxVal);
 			System.out.format("Value: %d\nFactors:%s\n\n", randomVal, factors(randomVal));
 		}
 		//System.out.println(primes);
 	}
 	
 	public static ArrayList<Integer> factors(int v){
 		ArrayList<Integer> factors = new ArrayList<Integer>();
 		if (v<0){
 			v /= -1;
 			factors.add(-1);
 		}
 		for (int i = 0; i < primes.size(); i++){
 			int p = primes.get(i);
 			if (v%p==0){
 				factors.add(p);
 				v /= p;
 				i = -1;//-1 because it will automatically increment to 0
 			}
 		}
 		factors.add(v);
		return factors;		
 	}
 	
 	public static void generatePrimes(){
 		int n = (int)Math.ceil(Math.sqrt(maxVal)); //largest prime I need to know will be below this
 		boolean[] values = new boolean[n];
 		for (int x = 2; x<n; x++){
 			if (!values[x]){
 				primes.add(x);
 				for (int v = x; v<n; v+=x){
 					values[v] = true;
 				}
 			}
 		}
 	}

 }
