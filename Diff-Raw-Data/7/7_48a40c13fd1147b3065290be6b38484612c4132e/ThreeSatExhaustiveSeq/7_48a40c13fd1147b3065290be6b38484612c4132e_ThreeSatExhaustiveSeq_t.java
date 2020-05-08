 import java.util.Arrays;
 import java.util.Scanner;
 import java.io.File;
 import java.io.IOException;
 import edu.rit.pj.Comm;
 import edu.rit.util.Random;
 
 /**
  * Sequential implementation of the 3-SAT exhaustive search algorithm
  * that computes the total number of satisfying solutions.
  *
  * @author Christopher Wood
  * @author Eitan Romanoff
  * @author Ankur Bajoria
  **/
 public class ThreeSatExhaustiveSeq
 {
     // CNF formula parameters
     private int numClauses;
     private int numVars;
 
     // Clause collection
     private Literal[][] formula;
 
     // Boolean configuration collection
     private boolean[] variables;
 
     // Shared num satisfiable variable that is used to control parallel execution.
     private static long numSatisfiable;
     private static boolean[] solution;
 
     /**
      * The main method. Runs the program.
      **/
     public static void main(String args[]) 
     {
         // Start the clock.
         long startTime = System.currentTimeMillis();
         
         // Set up the communication with the job server.
         try
         {
             Comm.init(args);
         }
         catch (IOException e)
         {
             e.printStackTrace();
         }
 
         // Parse the command line arguments
         ThreeSatExhaustiveSeq sat = null;
         try
         {
             if (args.length == 1)
             {
                 sat = new ThreeSatExhaustiveSeq(args[0]); 
             }
             else if (args.length == 3)
             {
                 sat = new ThreeSatExhaustiveSeq(Integer.parseInt(args[0]),
                     Integer.parseInt(args[1]), Long.parseLong(args[2]));
             }
             else
             {
                 showUsage();
             }
         }
         catch (NumberFormatException e) 
         {
             System.err.println("Error: num_vars, num_clauses, and seed must be integers.");
             showUsage();
         } 
         catch (Exception e)
         {
             System.err.println(e.getMessage());
             showUsage();
         }
 
         // Run the decision algorithm and display the result.
         long numSatisfiable = sat.decide();
         if (numSatisfiable > 0)
         {
             System.out.printf("Satisfiable with %d solutions.%n", numSatisfiable);
             for (int i = 0; i < solution.length; i++)
             {
                 System.out.printf("X%d = %b%n", i, solution[i]);
             }
         }
         else
         {
             System.out.printf("Not satisfiable.%n");
         }
 
         // Stop the clock and display the time.
         long endTime = System.currentTimeMillis();
         System.out.printf((endTime - startTime) + " msec%n");
     }
 
     /**
      * Constructor to generate a 3-CNF formula from an external file.
      *
      * @param filename - file to read CNF formula from
      * @throws exception if parameters are invalid
      */
     public ThreeSatExhaustiveSeq(String filename)
     {
         try 
         {
             File file = new File(filename);
             Scanner scanner = new Scanner(file);
 
             // Read in the number of clauses and variables
             this.numVars = Integer.parseInt(scanner.next());
             this.numClauses = Integer.parseInt(scanner.next());
 
             if (numVars < 1 || numClauses < 1)
             {
                 throw new Exception("Error: num_vars and num_clauses must be positive integers.");
             }
 
             // Initialize the formula and variable data structure
             this.formula = new Literal[numClauses][3]; // 3 literals per clause
             this.variables = new boolean[numVars];
 
             // Now read in all of the clauses.
             for (int c = 0; c < numClauses; c++) 
             {
                 Literal[] formula_c = formula[c];
                 for (int i = 0; i < 3; i++) 
                 { 
                     int var = Integer.parseInt(scanner.next());
                     boolean negated = var < 0 ? true : false;
                     var = negated ? var * -1 : var;
                     if (var < 1 || var > numVars)  // fix
                     {
                         System.err.println("Error: variables must be within [1," + numVars + "]");
                         System.exit(-1);
                     }
                     Literal lit = new Literal(negated, var - 1);
                     formula_c[i] = lit;
                 }
             }
         }
         catch (Exception e) 
         {
             System.err.println("Error: " + e.getMessage());
             e.printStackTrace();
             System.exit(-1);
         }
     }
 
     /**
      * Constructor to generate a random 3-CNF formula for solving.
      *
      * @throws exception if parameters are invalid
      */
     public ThreeSatExhaustiveSeq(int numVars, int numClauses, long seed) throws Exception
     {
         this.numVars = numVars;
         this.numClauses = numClauses;
 
         if (numVars < 1 || numClauses < 1)
         {
             throw new Exception("Error: num_vars and num_clauses must be positive integers.");
         }
 
         // Initialize the PRNG and the formula/variable data structures
         Random prng = Random.getInstance(seed);
         formula = new Literal[numClauses][3]; // 3 literals per clause
         variables = new boolean[numVars];
 
         // Randomly generate the formula the formula
         for (int i = 0; i < numClauses; i++) 
         {
             for (int j = 0; j < 3; j++) 
             {
                 formula[i][j] = new Literal(prng.nextBoolean(), prng.nextInt(numVars));
             }
         }
     }
 
     /**
      * Determine the number of satisfying variable configurations
      * that satisfy this formula, if any.
      *
      * @return the number of satisfying instances.
      */
     public long decide() 
     {
         long numSats = 0;
 
         // Initialize all variables to false.
         long numConfigurations = 1L;
         for (int i = 0; i < numVars; i++) 
         {
             variables[i] = false;
             numConfigurations <<= 1;
         }
 
         // Now check all possible configurations for a satisfying assignment
         for (long config = 0L; config < numConfigurations; config++)
         {
             boolean formulaValue = true;
             for (int c = 0; c < numClauses; c++) 
             {
                 boolean clauseValue = false;
                 for (int l = 0; l < 3 && clauseValue == false; l++) 
                 {
                     // Clause is only true if at least one literal is true
                     if (formula[c][l].negated == true && !variables[formula[c][l].id])
                     {
                         clauseValue = true;
                     } 
                     else if (formula[c][l].negated == false && variables[formula[c][l].id])
                     {
                         clauseValue = true; 
                     }
                 }
 
                 formulaValue = formulaValue && clauseValue;
                 if (formulaValue == false) 
                 {
                     break;
                 }
             }
 
             // If satisfiable, return right away. Otherwise, go to
             // the next configuration.
             if (formulaValue) 
             {
                 if (numSats == 0)
                 {
                    solution = new boolean[variables.length];
                    for (int i = 0; i < variables.length; i++) 
                    {
                        solution[i] = variables[i];
                    }
                 }
                 numSats++;
             }
 
             // Propagate the truth value throughout the collection.
             for (int i = numVars - 1; i >= 0; i--)
             {
                 if (variables[i] == true) 
                 {
                     variables[i] = false;
                 }
                 else 
                 {
                     variables[i] = true;
                     break;
                 }
             }
         }
 
         return numSats;
     }
 
     /**
      * Print the usage message and then terminate abnormally.
      */
     public static void showUsage() 
     {
         System.err.println("java ThreeSatExhaustiveSeq [<file> | <num_literals> <num_clauses> <seed>]");
         System.exit(-1);
     }
 }
