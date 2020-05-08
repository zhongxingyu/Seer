 import java.util.Scanner; // Scanner class
 
 public class CAI
 {
 	public static void main(String [] args)
 	{
 		Scanner in = new Scanner(System.in); // need to read information
 		CAIClass assistance = new CAIClass();
 		while(true)
 		{
 			for(int problemLoop = 0; problemLoop < 10; problemLoop++)
 			{
 				assistance.generateProblem();
 				while(assistance.isIncorrect())
 				{
 					assistance.printProblem();
 					int answer = in.nextInt();
 					if (assistance.checkProblem(answer))
 					{
 						assistance.setIncorrect(false);
 						System.out.println(assistance.generateCorrectMessage());
 					}
 					else
 						System.out.println(assistance.generateIncorrectMessage());
 				}
 				assistance.setIncorrect(true);
 				System.out.println(assistance.getPercent());
 			}
 			System.out.printf("You scored %d/%d, that's a %f%%.\n",
																assistance.getCorrect(),
																assistance.getTotal(),
																assistance.getPercent());
 			assistance.resetGrade();
 		}
 	}
 }
