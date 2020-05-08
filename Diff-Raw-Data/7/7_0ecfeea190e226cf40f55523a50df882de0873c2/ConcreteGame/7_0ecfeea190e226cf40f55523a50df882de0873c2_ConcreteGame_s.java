 package superFunGameMethod;
 
 import java.util.Scanner;
 
 public class ConcreteGame extends AbstractGame {
 	
 	public String PlayerOneName;
 	public String PlayerTwoName;
 			
 	protected void setupGame() {
 		System.out.println("Welcome to The Super Fun Game");
 		System.out.println("Prepare for super fun!");
 		System.out.print("Player One enter your name: ");
 		Scanner scanner = new Scanner(System.in);
 		PlayerOneName = scanner.nextLine();
		System.out.print("Your name is:");
 		System.out.print(PlayerOneName);
 		System.out.println(". I've never heard that one before.");
 		System.out.print("Player Two enter your name: ");
 		PlayerTwoName = scanner.nextLine();
		System.out.print("Your name is:");
		System.out.print(PlayerOneName);
 		System.out.println(". My mother told me if I couldn't say anything nice...");
 		System.out.println("Wow guys, those are great names!");
 		
 	}
 
 	protected boolean playerOneTurn() {
 		return (Math.random()>0.5);
 	}
 
 	protected boolean playerTwoTurn() {
 		return (Math.random()>0.5);		
 	}
 
 	protected void finishGame(int i) {
 		if (i == 1){
 			System.out.print(PlayerOneName);
 			System.out.println(", you are the best! You win!");
 		}
 		if (i == 2){
 			System.out.print(PlayerTwoName);
 			System.out.println(", you are the best! You win!");
 		}
 		System.out.println("Truly, you are both winners.");
 	}
 
 }
