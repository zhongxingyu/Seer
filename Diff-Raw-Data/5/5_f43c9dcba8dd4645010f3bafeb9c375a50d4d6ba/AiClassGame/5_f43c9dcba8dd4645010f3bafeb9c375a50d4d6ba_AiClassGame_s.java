 import java.util.Scanner;
 public class AiClassGame{
   public static void main(String args[]) throws java.util.InputMismatchException{
     Scanner s = new Scanner(System.in);
     int difficulty = 0;
     try{
      System.out.println("Please choose\n1.Easy\n2.Normal\n3.Hard\n4.Impossibile");
       difficulty  = s.nextInt();
       if(difficulty < 1 || difficulty > 4){
         System.err.println("You have entered a wrong value\nProgram will terminate");
         System.exit(0);
       }
     }
 
     catch (Exception e){
       System.err.println("You have entered a wrong value\nProgram will terminate");
       System.exit(0);
     }
 
     startTheGame(difficulty);
   }
 
   public static void startTheGame(int difficulty){
     Scanner userInput = new Scanner(System.in);
     int playerNumber,cpuNumber;int sum = 0;
     boolean checkPlayer = false;
     for(;;){
 
       try {
         System.out.println("Please enter a number between 1 and 10");
         playerNumber = userInput.nextInt();
         if (playerNumber < 1 || playerNumber > 10){
           System.err.println("You have entered a number out of range");
           continue;
         }
 
       }
 
       catch (Exception e) {
         System.err.println("Error You must enter an integer number between 1 and 10");
         userInput.nextLine();
         continue;
       }
 
       checkPlayer = true;
       sum += playerNumber;
       printSum(sum);
       if(sum >= 100)
         break;
       if(sum >= 71)
         cpuNumber = calculateCpuNumber(sum,difficulty);
       else
         cpuNumber = calculateCpuNumber(sum,difficulty);
 
       sum += cpuNumber;
       checkPlayer = false;
      System.out.println("Computer has choosen : "+ cpuNumber);
       printSum(sum);
       if(sum >= 100)
         break;
     }
 
     if (checkPlayer)
       System.out.println("You Win");
     else
       System.out.println("You Lose\nGAME OVER");
   }
 
   public static void printSum(int sum){System.out.println("Sum is : " + sum);}
   public static int calculateCpuNumber(int sum, int difficulty){
     int num;
     if(sum >= 70 && sum <= 79 && (difficulty == 2 || difficulty == 3 || difficulty == 4))
       num = 80 - sum;
     else if (sum >= 81 && sum <= 88 && (difficulty == 3 || difficulty == 4))
       num = 89 - sum;
     else if (sum >= 90 && difficulty == 4)
       num = 100 - sum;
     else num = generateRandomCpuNumber(sum);
     return num;
   }
 
   private static int generateRandomCpuNumber(int sum){return (int)(1 + Math.random()*10);}
 }
