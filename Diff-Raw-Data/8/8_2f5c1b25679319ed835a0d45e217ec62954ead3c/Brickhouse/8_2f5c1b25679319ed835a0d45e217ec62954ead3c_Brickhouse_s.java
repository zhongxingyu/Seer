 /*
 Your niece was given a set of blocks for her birthday, and she has decided to build a panel using
 3”×1” and 4.5”×1" blocks. For structural integrity, the spaces between the blocks must not line up
 in adjacent rows. For example, the 13.5”×3” panel below is unacceptable, because some of the
 spaces between the blocks in the first two rows line up (as indicated by the dotted line).
 
 There are 2 ways in which to build a 7.5”×1” panel, 2 ways to build a 7.5”×2” panel, 4 ways to
 build a 12”×3” panel, and 7958 ways to build a 27”×5” panel. How many different ways are there
 for your niece to build a 48”×10” panel? The answer will fit in a 64-bit integer. Write a program to
 calculate the answer.
 
 The program should be non-interactive and run as a single-line command which takes two
 command-line arguments, width and height, in that order. Given any width between 3 and 48 that
 is a multiple of 0.5, inclusive, and any height that is an integer between 1 and 10, inclusive, your
 program should calculate the number of valid ways there are to build a wall of those dimensions.
 Your program’s output should simply be the solution as a number, with no line-breaks or white
 spaces.
 
 Your program will be judged on how fast it runs and how clearly the code is written. We will be
 running your program as well as reading the source code, so anything you can do to make this
 process easier would be appreciated.
 
 Send the source code and let us know the value that your program computes, your program’s
 running time, and the kind of machine on which you ran it.
  */
 
 import java.util.ArrayList;
 
 /** 
  * 
  * @author david
  */
 public class Brickhouse {
 
     /**
      * @param args the command line arguments
      */
     static double height;
     static double length;
     
     static final int rowLength = 100;
     static final int numberOfRows = 1000;
     
     static int row_number = 0;
     static ArrayList row_array = new ArrayList();
     
     static int row_perm_index = 0;
     static int row_perm[][];
 
     static double brick1 = 3;
     static double brick2 = 4.5;
     
     public static void main(String[] args) {
        
         // Start Clock
         time_counter count = new time_counter();
         count.start();
         
         // variables to return
         double correct = 0;
         double runtime;
         
         // Set Input
         height = Double.parseDouble(args[0]);
         length = Double.parseDouble(args[1]);
         
         // Create Permutations or Rows
         number_counter row_create = new number_counter();
         row_array = row_create.row_generator((int)length,2);
         row_number = row_array.size();
                 
         // Find permutations of Row Cominations, if Height > 1
         if (height > 1){
             
             //  Create Counter to Create All Perms of Row Combinations
             number_counter roper = new number_counter();
             row_perm = roper.roper((int)height,row_number);
             row_perm_index = roper.get_starting_size((int)height,row_number);
             
             // Create Validator Object to Pass All Row Perms to
             validator validate = new validator(row_array,row_perm);
             
             // Check All Row Perms and ...
             for(int index = 1; index < row_perm_index; index++){
                 //Increment Correct when Valid
                 if (validate.validate_all(index,(int)height)){
                     correct ++;
                 }
             }
         }
         // Otherwise, all rows of correct length are correct
         else{
             correct = row_number;
         }
         
         // Get run time and finish up
         count.end();
         runtime = count.runTime();
         
         System.out.println("Number of Correct Answers"+correct);
        System.out.println("Run Time:"+runtime);
     }
 }
