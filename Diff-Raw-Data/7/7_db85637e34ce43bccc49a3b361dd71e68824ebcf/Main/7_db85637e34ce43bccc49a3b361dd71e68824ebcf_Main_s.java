 
 public class Main {
 	public static void main(String[] args) {
 		try {
 			int i = 1/0;
 			System.out.println(i);
 		}
		catch(Exception e) {
			log(e);
		}
 		catch(ArithmeticException e) {
 			log(e);
 		}
 	}
 
 	public static void log(Exception e) {
 		System.out.println("Logged: "+e);
 	}
 }
