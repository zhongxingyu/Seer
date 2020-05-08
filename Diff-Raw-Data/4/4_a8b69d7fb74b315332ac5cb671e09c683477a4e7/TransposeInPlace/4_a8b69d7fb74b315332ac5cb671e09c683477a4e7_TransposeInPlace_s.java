 package google.top150.transposeinplace;
 
 public class TransposeInPlace {
 	/*
 	 * input: a1, a2, ..., an, b1, b2, ..., bn, c1, c2, ..., cn
 	 * output: a1, b1, c1, a2, b2, c2, ..., an, bn, cn
 	 * the transpose should be done in place
 	 */
 	static int[] transpose(int[] input, int width, int height) {
 		int processCounter = width * height;
 		int inx2;
 		
 		for (int inx=0; inx < width*height && processCounter > 0; inx++) {
 			inx2 = inx;
 			if (isNotVisitedLoop(width, height, inx2, inx)) {
 				System.out.print("[");
 				processCounter -= processLoop(width, height, inx2, inx, input);
 				System.out.println("]");
 			}
 		}
 		
 		return input;
 	}
 	
 	static boolean isNotVisitedLoop(int width, int height, int inx2, int inx) {
 		do {
 			inx2 = TransposeInPlace.next(inx2, width, height);
 			if (inx2 < inx) 
 				return false;
 		} while (inx2!=inx);
 		return true;
 	}
 
 	static int processLoop(int width, int height, int inx2, int inx, int[] input) {
 		int counter = 0;
 		int inx3, tmp2;
 		int tmp1 = input[inx2];
 		do {
 			inx3 = TransposeInPlace.next(inx2, width, height);
			System.out.print(String.format("%d <- %d, ", inx3, inx2));
 			tmp2 = input[inx3];
 			input[inx3] = tmp1;
 			tmp1 = tmp2;
 			inx2 = inx3;
 			counter++;
 		} while (inx2!=inx);
 		return counter;
 	}
 
 	static int next(int inx, int columnSize, int rowSize) {
 		return inx / columnSize + (inx % columnSize)*rowSize;
 	}
 }
