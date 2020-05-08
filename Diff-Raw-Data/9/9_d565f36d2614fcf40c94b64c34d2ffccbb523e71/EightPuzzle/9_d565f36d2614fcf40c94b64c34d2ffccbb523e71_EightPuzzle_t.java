 /**
  * 
  */
 
 /**
  * @author LDS
  * 8-puzzle game with solver and probably a problem generator.
  *Just for fun. 
  */
 
 import java.lang.*;
 import java.util.*;
 
 public class EightPuzzle {
	
 	
 	public static void main(String[] args) {

 		//game starts, welcome to users
 		System.out.println("Welcome to world of 8 puzzle!");
 		System.out.println("Solving the puzzle below with the 0 indicating space");
 		System.out.println("Type in the block number you want to move at each step.");
 		
 		//generate an 8 puzzle question
 		StateNode sn = generator();
 		
 		Scanner scanner = new Scanner(System.in);
 		boolean flag=true;
 		String help="I give up.";
 		
 		while(flag)
 		{
 		//print out current 8 puzzle
 		sn.print();
 		System.out.println("Type "+help+" to see solution."); 
 		//read in user's command
 		int nextmove= scanner.nextInt();
 		if (nextmove!=0)//if user doesn't give up
 		{
 			sn.operate(nextmove);
 			if (sn.rel==0)
 			{
 				flag=false;
 				System.out.println("Congratulations!!");
 			}
 		}
 		else
 		{
 			String str=scanner.nextLine();
 			if (str.equals(help)) 
 			{
 				flag=false;
 				//solve
 				//print solve
 			}
 			else
 				System.out.println("Invalid input");			
 		}
 		}
 		
 	}
 	
	public static final int [] goal = {0,1,2,3,4,5,6,7,8};
	public static final StateNode Goal= new StateNode (goal);
 	public static StateNode generator()
 	{
 		Random rgen = new Random();  // Random number generator
 		StateNode sn;
 		do{
 			//generate a shuffled array
 			int [] arr = {0,1,2,3,4,5,6,7,8};
 			for (int i=0; i<9; i++) {
 				int randomPosition = rgen.nextInt(9);
 				int temp = arr[i];
 				arr[i] = arr[randomPosition];
 				arr[randomPosition] = temp;				
 			}
 			//build up a statenode and return if solvable
 			sn=new StateNode(arr);
 		}while((solver.isSolvable(sn)));
 		return sn;
 	}
 }
