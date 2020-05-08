 package chapter7.demos;
 
 import convenience.helpers.Input;
 
 /**
 Single dimension array named cost
 Holds 7 costs
 Input 1: hard coded values
 Input 2: a) entered from the user input using a FOR LOOP
          b) accumulator to add costs is done in loop 
             sum = sum + cost[index];
          c) average is then calculated
          d) now, how will we find how many costs are above and below the average?
 
 
  */
 
 public class Demo1 {
 	public static void main (String [] args) {
 		// declare a one dimensional array named cost
 		// the array values are data type double
 		// there are a maximum of seven temps in this array 
 		double[] cost = new double[7];
 
 		//or you could declare and initialize at the same time
 		double[] cost2 = {22,33,67,54.8,11,98.3,3};
 
 		// or you could load the array values like this:
 		cost[0] = 22;
 		cost[1] = 33;
 		cost[2] = 67;
 		cost[3] = 54;
 		cost[4] = 11;
 		cost[5] = 98;
 		cost[6] = 3;
 
 		// what's there?
 		System.out.println ("value of element 3 is " + cost[2]);
 
 		// use the length method to find the length of the array
 		// use the length as a parameter for looping
 		int len = cost.length;
 		System.out.println ("The length of the array is " + len);
 
 		// IT IS MUCH EASIER to load arrays in a loop!
     	// Read costs and compute their average:
 
 		Input input= new Input();
         System.out.println ("Enter 7 costs:");
         input.writeMessage("This program asks for 7 costs and will calculate the average.","Cost Program",3);
         double sum = 0;
 
         // using a FOR loop to enter the array values 
         for (int index = 0 ; index < cost.length ; ++ index)
         {
             int tempNumPrint = index + 1;
         	cost [index] = input.getDouble("Please enter cost # " + tempNumPrint, "ENTER COST", 3);
             System.out.println("The cost entered is " + cost [index]);
 
             // with each iteration, add the new cost to the sum accumulator
             sum = sum + cost[index];
 
         }
 
         // now that we have a total, calculate the average cost
         double average = sum / 7;
         System.out.printf ("The average cost is %5.2f dollars.", average);
         System.out.println ();
         System.out.println ("Program Done.");
 	}
 }
