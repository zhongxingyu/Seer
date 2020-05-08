 package others;
 
 public class Fibonacci {
 	
 	public int[] result;
 	/**
 	 * generate the nth fibonacci number
 	 * @param n
 	 * @return the nth fibonacci number
	 */ 
 	public int generate(int n) {
 		this.result = new int[n+1];
 		this.fibonacci(n);
 		return this.result[n];
 	}
 	
 	public int fibonacci(int n) {
 		if (n == 0) this.result[n] = 0;
 		if (n == 1) this.result[n] = 1;
 		if (this.result[n] != 0) return this.result[n];
 		if (n > 1)
 			this.result[n] = fibonacci(n-1) + fibonacci(n-2);
 		return this.result[n];
 	}
 	
 	public static void main(String[] args) {
 		Fibonacci f = new Fibonacci();
 		System.out.println(f.generate(2));
 		System.out.println(f.generate(3));
 		System.out.println(f.generate(4));
 		System.out.println(f.generate(9));
 		System.out.println(f.generate(10));
 		System.out.println(f.generate(11));
 	}
 }
