 public class Main {
 	private static final BlockIndexer intDivHeavy = new BlockIndexer("Integer Division (Heavy)") {
 		@Override
 		int blockIndex(int i) {
 			return (i - 27 * (i / 27) - 9 * ((i - 27 * (i / 27)) / 9)) / 3 + 3 * (i / 27);
 		}
 	};
 	private static final BlockIndexer intDivLight = new BlockIndexer("Integer Division (Light)") {
 		@Override
 		int blockIndex(int i) {
 			return (i - 9 * (i / 9)) / 3 + 3 * (i / 27);
 		}
 	};
 	private static final BlockIndexer lookup = new BlockIndexer("Lookup") {
 		private char[] lookup = {0, 0, 0, 1, 1, 1, 2, 2, 2, 0, 0, 0, 1, 1, 1, 2, 2, 2, 0, 0, 0, 1, 1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 4, 5, 5, 5, 3, 3, 3, 4, 4, 4, 5, 5, 5, 3, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6, 6, 7, 7, 7, 8, 8, 8, 6, 6, 6, 7, 7, 7, 8, 8, 8, 6, 6, 6, 7, 7, 7, 8, 8, 8};
 
 		@Override
 		int blockIndex(int i) {
 			return lookup[i];
 		}
 	};
 	private static final BlockIndexer floor = new BlockIndexer("Floor") {
 		@Override
 		int blockIndex(int i) {
 			return (int) Math.floor((i % 9) / 3) + 3 * (int) Math.floor(i / (9 * 3));
 		}
 	};
 
 	public static void main(String[] args) {
 		intDivHeavy.printBenchmark();
 		System.out.println();
 		intDivLight.printBenchmark();
 		System.out.println();
 		lookup.printBenchmark();
 		System.out.println();
 		floor.printBenchmark();
 	}
 
 	private static abstract class BlockIndexer {
 		private final String name;
 
 		protected BlockIndexer(String name) {
 			this.name = name;
 		}
 
 		public void printBenchmark() {
 			System.out.println(name);
 			for (int i = 0; i < 9; i++) {
 				for (int j = 0; j < 9; j++) {
 					System.out.print(blockIndex(9 * i + j));
 					System.out.print(" ");
 				}
 				System.out.println();
 			}
 
			/*
			 * Execution of the following block of code influences how fast the actual
			 * test loops execute.
			 */
			/*
 			int y = 0;
 			for (int k = 0; k < 20; k++) {
				for (int i = 0; i < 10000000; i++) {
					for (int j = 0; j < 81; j++) {
						y += blockIndex(j);
					}
				}
 			}
 			System.out.println(y);
			*/
 
 			long start = System.nanoTime();
 			int x = 0;
 			int numIterations = 10000000;
 			/*
 			 * This is a dangerous test procedure as a sufficiently smart
 			 * compiler/jvm may realize that these loops can be safely
 			 * interchanged enabling the calculation to be lifted out of the
 			 * inner loop leaving essentially no work to be done at runtime.
 			 */
			for (int i = 0; i < numIterations; i++) {
				for (int j = 0; j < 81; j++) {
					x += blockIndex(j);
				}
			}
 			long end = System.nanoTime();
 
 			System.out.println("Summation:\t\t\t\t" + x);
 			System.out.printf("%d iterations:\t%.3fs\n", numIterations, (end - start) / 1e9);
 		}
 
 		abstract int blockIndex(int cellIndex);
 	}
 }
