 public class Q {
 	public static void main(String[] args) {
 		Q q = new Q();
 		for (int i = 1; i < 50; i++) {
 			System.out.print(q.q(i) + " ");
 		}
 	}
 
 	public int q(int n) {
		if (n <= 1)
 			return 1;
		if (n == 2)
			return 3;
 
 		return q(n - q(n - 1)) + q(n - q(n - 2));
 	}
 }
