 package edu.berkeley.gamesman.util;
 
 public class CoefTable {
 	long[][] posTable = { { 1L } };
 	long[][] negTable = new long[0][];
 	final int degree;
 
 	public static void main(String[] args) {
 		CoefTable ct = new CoefTable(Integer.parseInt(args[2]));
 		System.out.println(ct.get(Integer.parseInt(args[0]), Integer
 				.parseInt(args[1])));
 	}
 
 	public CoefTable() {
 		this(1);
 	}
 
 	public CoefTable(int degree) {
 		if (degree <= 0)
 			throw new ArithmeticException("Degree must be positive");
 		this.degree = degree;
 	}
 
 	public long get(int n, int k) {
 		if (n < 0)
 			return getNeg(-n - 1, k);
 		ensureContained(n);
 		if (k < 0 || k >= posTable[n].length)
 			return 0;
 		return posTable[n][k];
 	}
 
 	private long getNeg(int n, int k) {
 		if (k < 0)
 			return 0;
 		ensureNegLength(n, k);
 		return negTable[n][k];
 	}
 
 	private void ensureNegLength(int n, int k) {
 		if (negTable.length <= n) {
 			long[][] newNegTable = new long[n + 1][];
 			System.arraycopy(negTable, 0, newNegTable, 0, negTable.length);
 			for (int i = negTable.length; i <= n; i++) {
 				newNegTable[i] = new long[0];
 			}
 			negTable = newNegTable;
 		}
 		if (negTable[n].length <= k) {
 			if (negTable[0].length <= k) {
 				long[] newRow = new long[k + 1];
 				System.arraycopy(negTable[0], 0, newRow, 0, negTable[0].length);
 				int r = negTable[0].length;
 				negTable[0] = newRow;
 				for (; r <= k; r++) {
 					negTable[0][r] = (r == 0 ? 1 : (r == 1 ? -1 : getNeg(0, r
 							- (degree + 1))));
 				}
 			}
 			for (int i = 1; i <= n; i++) {
 				if (negTable[i].length <= k) {
 					long[] newRow = new long[k + 1];
 					System.arraycopy(negTable[i], 0, newRow, 0,
 							negTable[i].length);
 					int r = negTable[i].length;
 					negTable[i] = newRow;
 					for (; r <= k; r++) {
 						negTable[i][r] = getNeg(i - 1, r)
 								- getNeg(i - 1, r - 1)
 								+ getNeg(i, r - (degree + 1));
 					}
 				}
 			}
 		}
 	}
 
 	private void ensureContained(int n) {
 		if (posTable.length <= n) {
 			long[][] newTable = new long[n + 1][];
 			System.arraycopy(posTable, 0, newTable, 0, posTable.length);
 			int i = posTable.length;
 			posTable = newTable;
 			for (; i <= n; i++) {
 				int len = i * degree + 1;
 				posTable[i] = new long[len];
 				for (int k = 0; k < len; k++) {
					posTable[i][k] = get(n - 1, k) + get(n, k - 1)
							- get(n - 1, k - (degree + 1));
 				}
 			}
 		}
 	}
 }
