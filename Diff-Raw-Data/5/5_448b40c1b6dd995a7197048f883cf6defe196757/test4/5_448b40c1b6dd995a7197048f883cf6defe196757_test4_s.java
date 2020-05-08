 public class test4 {
 
 	public static void main(String[] args) {
 		int i;
 		for (i = 0; i < 100; i = i + 10) {
 			while (i%10 != i/10) {
 				if ((i%2) == 1) {
 					System.out.print(i);
					System.out.println(" is even");
 				}
 				else {
 					System.out.print(i);
					System.out.println(" is odd");
 				}
 				i = i+1;
 			}
 			
 		}
 	
 	}
 }
