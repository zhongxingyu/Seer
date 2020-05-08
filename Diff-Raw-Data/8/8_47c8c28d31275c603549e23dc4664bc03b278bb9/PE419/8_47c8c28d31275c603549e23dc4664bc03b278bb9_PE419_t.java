 package assignments.chap4;
 
 public class PE419 {
 
 
 	public static void main(String[] args) {
 		int i;
 		for (i=1; i<9; i++) {
 			for (int j = 8; j>=i; j--){
				System.out.print("    ");
 			}
 			
 			int pwr = 0;
 			for (int k = 0; k < i; k++){
				System.out.printf("%4s", (int)Math.pow(2, pwr));
 				pwr++;
 			
 			}
 			
 			int pwr1 = pwr - 1;
 			for (int l = 0; l < i; l++){
 				pwr1--;
 				if ((int)Math.pow(2, pwr1) != 0)
					System.out.printf("%4s", (int)Math.pow(2, pwr1));
 
 			}
 			
 			System.out.println();
 		}
 
 	}
 
 }
