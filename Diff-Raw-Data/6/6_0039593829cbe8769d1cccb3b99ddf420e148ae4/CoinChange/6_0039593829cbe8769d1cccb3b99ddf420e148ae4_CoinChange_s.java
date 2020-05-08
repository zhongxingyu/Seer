 package alex.algorithms.dynamicprogramming;
 
 public class CoinChange {
 	public static int minCoinChange(int[] coins, int total) {
 		int[] counts = new int[total + 1];
 		counts[0] = 0;
 		for (int i = 1; i <= total; i++) {
 			int count = Integer.MAX_VALUE;
 			for (int j = 0; j < coins.length; j++) {
 				if (i - coins[j] >= 0) {
 					count = Math.min(count, counts[i - coins[j]] + 1);
 				}
 			}
 		}
 		return counts[total];
 	}
 
 	public static void main(String[] args) {
		// TODO Auto-generated method stub

 	}
 
 }
