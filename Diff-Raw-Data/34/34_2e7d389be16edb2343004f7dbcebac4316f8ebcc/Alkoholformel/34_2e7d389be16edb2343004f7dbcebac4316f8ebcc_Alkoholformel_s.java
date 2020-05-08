 package hyttetur;
 

 import java.lang.reflect.Array;
 import java.util.Scanner;
 
 
 
 public class Alkoholformel {
 	
 	
 	public static void main(String[] args)
 	{
 		final Scanner scanner = new Scanner(System.in);
 		
		System.out.print("Hvor mange dager skal du p hyttetur? Skriv inn som tall");
 		int nrOfDays = scanner.nextInt();
 		
		System.out.print("Hvor mange alkoholenheter trenger du p en normal kveld?");
 		int alcoholAmount = scanner.nextInt();
 	
		int alcoholYouNeed = alcoholNeeded(nrOfDays, alcoholAmount);
		
		System.out.println("Du skal p hyttetur i " + nrOfDays + " dager. For det trenger du " + alcoholYouNeed + " enheter alkohol");
	}
	
	public static int alcoholNeeded(Integer nrOfDays, Integer alcoholAmount){
 		
		int alcoholNeeded = 0;
		int[] recArray = new int[10];
 		
		recArray[0] = alcoholNeeded*2;
		recArray[1] = recArray[1]*2;
		
		alcoholNeeded = recArray[nrOfDays];
		
		return alcoholNeeded;
 	}
 
 }
