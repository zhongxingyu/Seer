 package src;
 
 public class FBsample {
 	public static void main(String[] args) {
 		String str = null;
 		String str2 = "Object";
 
 		int n = 10;
 		n = n + 1;
 
 		if (str instanceof String) {
 			System.out.println("STRING");
 		} else if (str2.equals(str)) {
 			System.out.println("EQUAL");
 		}
 
 		switch (n) {
 			case 1 :
 				System.out.println(n);
				break;
 			case 2 :
 				System.out.println(n + 1);
				break;
 			case 3 :
 				System.out.println(n + 1);
				break;
 			default :
 				System.out.println(n + 3);
				break;
 		}
 
 		String str3 = "Test";
 
 		Boolean bool = true;
 
 		System.out.println(bool);
 
 		int x = 10;
 
 		boolean ean = true;
 		if (ean == false) {
 			System.out.println("STRING");
 		}
 
 		int b = 100;
 		b = 10;
 	}
 }
