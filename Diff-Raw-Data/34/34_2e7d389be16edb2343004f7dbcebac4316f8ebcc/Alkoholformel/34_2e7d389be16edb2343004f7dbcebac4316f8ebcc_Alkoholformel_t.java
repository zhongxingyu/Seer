 package hyttetur;
 
 import java.lang.reflect.Array;
 import java.util.Scanner;
 
 
 
 public class Alkoholformel {
 	
 	
 	public static void main(String[] args)
 	{
 		final Scanner scanner = new Scanner(System.in);
 		
		System.out.print("Hvor mange dager skal du på hyttetur? Skriv inn som tall \n");
 		int nrOfDays = scanner.nextInt();
 		
		System.out.print("Hvor mange alkoholenheter trenger du på en normal kveld?");
 		int alcoholAmount = scanner.nextInt();
 	
		int[] alcoholYouNeed;
		alcoholYouNeed = new int[10];
		alcoholYouNeed[0] = nrOfDays*alcoholAmount;
 		
		for(int i = 1; i<alcoholYouNeed.length; i++){
			alcoholYouNeed[i] = alcoholYouNeed[i-1]*2; 
		}
 		
		int alcoholYouShouldBring = alcoholYouNeed[nrOfDays];
		System.out.println("Du skal på hyttetur i " + nrOfDays + " dager. For det trenger du " + alcoholYouShouldBring + " enheter alkohol");
 	}
	
 
 }
