 package com;
 
 import java.io.IOException;
 import java.util.InputMismatchException;
 import java.util.Scanner;
 
 
 // Step 1: Watch Lesson 7 and create Monster.java and JavaLessonSeven.java -- COMPLETED
 
 public class Heather_MonsterDriver // <-- Step 2: Rename class
 {
 	//Declaring Variables
 	public static String attacked;
 	
 	//Additional Credit for using Arrays (the For loop is later)
 	public static String[] myMonstersNames = new String[3];
 	public static int[] myMonstersHP = new int[3];
 	public static int[] myMonstersAttack = new int[3];
 	public static boolean[] myMonsterAlive = new boolean[3];
 
 	public static void main(String[] args)
 	{
 		userInput();
 		initialMonsters();
 		monsterAttack();
 	}
 	
 // -- METHODS START HERE -- \\	
 	
 	//Step 3: Add Two New Monsters
 	public static void initialMonsters()
 	{
 		//Default Monster
 		Monster first = new Monster();
 		first.name = myMonstersNames[0];
 		first.setHealth(myMonstersHP[0]);
 		first.setAttack(myMonstersAttack[0]);
 		myMonsterAlive[0] = first.getAlive();
 	
 		//New Monster #1
 		Monster second = new Monster();
 		second.name = myMonstersNames[1];
 		second.setHealth(myMonstersHP[1]);
 		second.setAttack(myMonstersAttack[1]);
 		myMonsterAlive[1] = second.getAlive();
 	
 		//New Monster #2
 		Monster third = new Monster();
 		third.name = myMonstersNames[2];
 		third.setHealth(myMonstersHP[2]);
 		third.setAttack(myMonstersAttack[2]);
 		myMonsterAlive[2] = third.getAlive();
 		
 		displayBoard();
 	}
 	
 	//Step 8: Ask for User Input (with Validation)
 	public static void userInput()
 	{
 		try
 		{
 			System.out.println("============================================================================");
 			System.out.println("Welcome to Monsters Inc. Our Motto: Scaring Kids Is A Thing Of The Past");
 			System.out.println("This program will contain three monsters. Please enter their initial values.");
 			System.out.println("============================================================================");
 			System.out.println();
 			Scanner user_input = new Scanner(System.in);
 			
 			System.out.print("Enter the first monster's name: ");
 			myMonstersNames[0] = user_input.next();
 
 			System.out.print("Enter the first monster's starting HP: ");
 			myMonstersHP[0] = user_input.nextInt();
 			
 			System.out.print("Enter the amount of HP the monster will lose when attacked: ");
 			myMonstersAttack[0] = user_input.nextInt();
 			System.out.println();
 			
 			if (myMonstersAttack[0] > myMonstersHP[0])
 			{
 				System.out.println();
 				System.err.println("Invalid Entry Was Detected. Let's Start Over...");
 				sleepMe();
 				clearScreen();
 				userInput();
 			}
 			
 			System.out.print("Enter the second monster's name: ");
 			myMonstersNames[1] = user_input.next();
 			
 			System.out.print("Enter the second monster's starting HP: ");
 			myMonstersHP[1] = user_input.nextInt();
 			
 			System.out.print("Enter the amount of HP the monster will lose when attacked: ");
 			myMonstersAttack[1] = user_input.nextInt();
 			System.out.println();
 			
 			if (myMonstersAttack[1] > myMonstersHP[1])
 			{
 				System.out.println();
 				System.err.println("Invalid Entry Was Detected. Let's Start Over...");
 				sleepMe();
 				clearScreen();
 				userInput();
 			}
 			
 			System.out.print("Enter the third monster's name: ");
 			myMonstersNames[2] = user_input.next();
 			
 			System.out.print("Enter the third monster's starting HP: ");
 			myMonstersHP[2] = user_input.nextInt();
 			
 			System.out.print("Enter the amount of HP the monster will lose when attacked: ");
 			myMonstersAttack[2] = user_input.nextInt();
 			System.out.println();
 			
 			if (myMonstersAttack[2] > myMonstersHP[2])
 			{
 				System.out.println();
 				System.err.println("Invalid Entry Was Detected. Let's Start Over...");
 				sleepMe();
 				clearScreen();
 				userInput();
 			}
 			clearScreen();
 		}
 		catch (Exception e)
 		{
 			System.out.println();
 			System.err.println("Invalid Entry Was Detected. Let's Start Over...");
 			sleepMe();
 			clearScreen();
 			userInput();
 		}
 	}
 	
 	//Step 9: Ask if Monsters have been attacked (with Validation)
 	public static void monsterAttack()
 	{
 		try
 		{
 			if ((myMonsterAlive[0] == false) && (myMonsterAlive[1] == false) && (myMonsterAlive[2] == false))
 			{
 				System.out.println();
 				System.out.println("Game Over. You Lose!");
 			}
 			else
 			{
 				System.out.println();
 				System.out.println("Has any of your monsters been attacked?");
 				Scanner user_input = new Scanner(System.in);
 				
 				System.out.print("Enter YES or NO: ");
 				attacked = user_input.next();
 				String changeAttackedCase = attacked.toUpperCase();
 		
 				if (changeAttackedCase.equals("YES"))
 				{
 					System.out.println();
 					System.out.print("Which monster was attacked (ie: 1, 2, 3)? ");
 					int elementAttack = user_input.nextInt();
 					
 					if (elementAttack == 0 || elementAttack > 3)
 					{
 						System.out.println();
 						System.err.println("Invalid Entry Was Detected. Let's Try Again...");
 						sleepMe();
 						clearScreen();
 						displayBoard();
 						monsterAttack();	
 					}
 	
 					System.out.println();
 					System.out.print("How many times was he/she attacked? ");
 					int elementTimesOfAttack = user_input.nextInt();
 					
 					myMonstersHP[elementAttack-1] = myMonstersHP[elementAttack-1] - (elementTimesOfAttack * myMonstersAttack[elementAttack-1]);
 					
 					if (myMonstersHP[elementAttack-1] <= 0)
 					{
 						myMonstersHP[elementAttack-1] = 0;
 						myMonsterAlive[elementAttack-1] = false;
 					}
 					
 					clearScreen();
 					displayBoard();
 					monsterAttack();	
 				}
 				else if (changeAttackedCase.equals("NO"))
 				{
 					System.out.println();
 					System.out.println("The program has ended. Thanks for playing Monsters Inc.");
 				}
 				else
 				{
 					System.out.println();
 					System.err.println("Invalid Entry Was Detected. Let's Start Over...");
 					sleepMe();
 					clearScreen();
 					displayBoard();
 					monsterAttack();		
 				}
 			}
 		}
 		catch (InputMismatchException ime)
 		{
 			System.out.println();
 			System.err.println("Invalid Entry Was Detected. Let's Try Again...");
 			sleepMe();
 			clearScreen();
 			displayBoard();
 			monsterAttack();
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 
 	//Step 10: Display the Table
 	public static void displayBoard()
 	{
 		System.out.println("My Monsters");
 		System.out.println("======================================================================");
 		System.out.println("ID\t\tName\t\tHealth\t\tAttack\t\tAlive");
 		System.out.println("----------------------------------------------------------------------");
 		
 		//Here is the for loop (for the Arrays)
 		for (int i = 0; i < myMonstersNames.length; i++)
 		{
			if (myMonstersNames[i].length() <= 7)
			{
				System.out.println(i+1 + "\t\t" + myMonstersNames[i] + "\t\t" + myMonstersHP[i] + "\t\t" + myMonstersAttack[i] + "\t\t" + myMonsterAlive[i]);
			}
			else
			{
				System.out.println(i+1 + "\t\t" + myMonstersNames[i].substring(0,7) + "\t\t" + myMonstersHP[i] + "\t\t" + myMonstersAttack[i] + "\t\t" + myMonsterAlive[i]);
			}
 		}
 	}
 	
 	//Clear Screen Method
 	public static void clearScreen()
 	{
 		Process exitCode = null;
 		try
 		{
 			if(System.getProperty("os.name").startsWith("Window")) 
 			{
 				exitCode = Runtime.getRuntime().exec("cls");
 			} 
 			else 
 			{
 			    exitCode = Runtime.getRuntime().exec("clear");
 			}
 		} 
 		catch (IOException e) 
 		{
 			for(int i = 0; i < 1000; i++) 
 			{
 				System.out.println();
 			}
 		}
 	}
 	
 	//Sleep Method
 	public static void sleepMe()
 	{
 	   try
 		{
 			Thread.sleep(4000);
 		}
 		catch(InterruptedException e)
 		{
 			Thread.currentThread().interrupt();
 		}
 	}
 }
