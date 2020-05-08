 import java.util.*;
 import java.io.*;
 public class juanspdf {
 	public static void main (String[] args)
 			throws FileNotFoundException {
 		Scanner console = new Scanner(System.in);
 		Scanner input = new Scanner(new File("comptia.txt"));
 		boolean[] bool = new boolean[208];
 		String[] answers = new String[208];
 		String[] guesses = new String[208];
 		int[] number = new int[208];
 		
 		String a = " ";
 		String b = " ";
 		String c = " ";
 		String d = " ";
 		String e = " ";
 		String answer = "";
		double right = 0;
 		int count = 0;
 		while(input.hasNextLine()) {
 			String s = input.nextLine();
 			System.out.println(s);
 			if(s.equals("END")) {
 				String choices = input.nextLine();
 				a = choices;
 				System.out.println(a);
 				choices = input.nextLine();
 				b = choices;
 				System.out.println(b);
 				choices = input.nextLine();
 				c = choices;
 				System.out.println(c);
 				choices = input.nextLine();
 				if(choices.startsWith("D.")) {
 					d = choices;
 					System.out.println(d);
 					choices = input.nextLine();
 				}
 				if(choices.startsWith("E.")) {
 					e = choices;
 					System.out.println(e);
 					choices = input.nextLine();
 				}
 				Scanner data = new Scanner(choices);
 				String idk = data.next();
				idk = data.next();
 				answer = idk;
 				
 				System.out.println("what is your guess? (if theres more than 1 answer put a comma with no spaces in between)");
 				String guess = console.next();
 				if(answer.equals(guess)) {
 					right++;
 					count++;
 				} else {
 					count++;
 					bool[count-1] = true;
 					answers[count-1] = answer;
 					guesses[count-1] = guess;
 					number[count-1] = count;
 				}
 			}
 		}
		System.out.println("You got " + right + " right out of 208");
 		System.out.println("Your total is " + right/208);
 		for(int i = 0; i < 208; i++) {
 			if(bool[i]) {
 				System.out.println("For number " + number[i] + " you guessed " + guesses[i] + " but the answer was " + answers[i]);
 			}
 		} 
 	}
 }
