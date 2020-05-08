 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package crisostomolab4b;
 import java.io.*;
 
 /**
  *
  * @author arscariosus
  */
 public class SeatReservation {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) throws Exception {
         	BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
 	int rseat, hit;
 	char choice = 'y';
 	int seats[][] = { { 1, 2, 3, 4, 5 }, { 6, 7, 8, 9, 10}, { 11, 12, 13, 14, 15 }, { 16, 17, 18, 19, 20 },
 			  { 21, 22, 23, 24, 25 }, { 26, 27, 28, 29, 30 }, { 31, 32, 33, 34, 35 } };
 
 
 	do {
 	    hit = -1;
 	    for(int i = 0; i < 7; i ++) {
 		for(int j = 0; j < 5; j++) {
 		    if(seats[i][j] > 0 )
			System.out.printf("%5d", seats[i][j]);
 		    else
			System.out.printf("%5c", 'x');
 		}
 		System.out.println();
 	    }
 	    System.out.println();
 	    System.out.print("Enter the seat to be reserved : ");
 	    rseat = Integer.parseInt(input.readLine());
 
 	    for(int i = 0; i < 7; i ++) {
 		for(int j = 0; j < 5; j++) {
 		    if(seats[i][j] == rseat) {
 			seats[i][j] = -1;
 			hit = 1;
 		    }
 		}
 	    }
 	    if(hit > 0)
 		System.out.println("Seat reservation for Seat #" + rseat + " successful!");
 	    else
 		System.out.println("Sorry, that seat seems to be already reserved.");
 
 	    System.out.print("Would you like to have another reservation? (y/n)");
 	    choice = (char) System.in.read();
 	    System.in.read();
 	}while(choice == 'y');
     }
 
 }
