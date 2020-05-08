 package tk.rebornlegendmc.random;
import java.util.Scanner; 
 
 public class Number_Adder {
 	public static void main(String[] args) {
		System.out.println("Adder Test! :D");
		System.out.println("Type the first integer: ");
		Scanner sc = new Scanner(System.in);
	     int i = sc.nextInt();
		Integer firstNumber = i;
		System.out.println("Type the second integer: ");
		Scanner pc = new Scanner(System.in);
	     int t = pc.nextInt();
		Integer secondNumber = t;
		System.out.println(firstNumber + secondNumber);
}
}
