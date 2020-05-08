 public class palindrome {
 
 
 
 	public static void main(String[] args) {
 			
 			if (args.length < 1) {
 				System.out.println("Please type a number after fibonacci the next time you run this program.");
 				System.exit(0);
 			}
 
 			int n = 0;
 			try {
 				n = Integer.parseInt(args[0]);
 			} catch (NumberFormatException e) {
 				System.out.println(args[0] + " is not a number.");
 				System.exit(0);
 			}
 			if (n < 1) {
 				System.out.println("The number must be greater than 0.");
 				System.exit(0);
 			}
 
 			System.out.println("The " + args[0] + "th fibonacci number is:" + recursiveFibonacci(n));
 			
 			//iterativeFibonacci(Integer.parseInt(args[0]));
 			
 			recursiveFibonacci(args[1]);
 		}
 
 
 
 
 
 		/**
 		 * iterativePalindrome
 		 * 		iteratively computes whether or not a string of characters is a palindrome
 		 *
 		 * @param s is the string of characters that need to be checked
 		 * @return returns true or false based on wheter or not it is a palindrome
 		 */
 	public static boolean iterativePalindrome(String s) {
 		int equalNumbers = 0;
 		for(int i = 0; i < s.length(); i++) {
			if(s.charAt(s[i]) == s.charAt(s.length()-i)){
 				equalNumbers = 1;
 			}
 			equalNumbers = 0;
 		}
 
 		if(equalNumbers = 1) {
 			return true;
 		}
 		return false;
 	}
 
 
 
 
}
