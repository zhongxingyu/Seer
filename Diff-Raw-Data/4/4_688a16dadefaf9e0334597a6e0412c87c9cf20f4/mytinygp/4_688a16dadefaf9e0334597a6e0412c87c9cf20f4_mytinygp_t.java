 /**
  * Implementation of tiny gp that evolves a program 
  * representing a booealn expression.
  *
  * Source modified from tiny_gp program, which was developed by 
  * Riccardo Poli (http://cswww.essex.ac.uk/staff/rpoli/MyTinyGP/)
  *
  * Author: Richard To
  * Date: 2/24/13
  */
 
 import java.util.*;
 import java.io.*; 
 import java.text.DecimalFormat; 
 
 public class MyTinyGp 
 {
     int[] fitness;
     char[][] pop;
     static Random rd = new Random();
     static final int NOT = 110; 
     static final int AND = 111; 
     static final int OR = 112; 
     static final int FSET_START = NOT; 
     static final int FSET_END = OR;
     static boolean[] x = new boolean[FSET_START];
     static double minrandom, maxrandom;
     static char[] program;
     static int PC;
     static int varnumber, fitnesscases, randomnumber;
     static int fbestpop = 0;
     static double favgpop = 0;
     static long seed;
     static double avg_len; 
     static final int MAX_LEN = 10000;   
     static final int POPSIZE = 100000;
     static final int DEPTH = 5;
     static final int GENERATIONS = 100;
     static final int TSIZE = 2;
     public static final double PMUT_PER_NODE = 0.05;
     public static final double CROSSOVER_PROB = 0.9;
     static boolean[][] targets;
 
     /**
      * Runs program using preorder tree traversal.
      *
      * Before applying OR or AND operators, 
      * make sure to run results from both branches, 
      * otherwise the short circuit will change the 
      * the program that is being run.
      *
      * This is due to how the program array is set up 
      * with the nodes in preorder already.
      */
     boolean run() { /* Interpreter */
         boolean x1, x2;
         char primitive = program[PC++];
         if (primitive < FSET_START) {
             return(x[primitive]);
         }
         switch (primitive) {
             case NOT : return(!run());
             case OR :
                 x1 = run();
                 x2 = run();
                 return x1 || x2;
             case AND :
                 x1 = run();
                 x2 = run();
                 return x1 && x2;
         }
         return(false); // should never get here
     }
      
     /**
      * Recursively traverse tree to count size of buffer,
      * which should be the size of the array?
      * 
      * This looks like an artificat from porting from c?
      * 
      * Maybe there is a case where the tree terminates at terminals 
      * earlier?
      *
      * Additionally can provide some error detection for invalid trees - 
      * Array index out of bounds exception.
      */         
     int traverse(char[] buffer, int buffercount) {
         if (buffer[buffercount] < FSET_START) {
             return(++buffercount);
         }
         
         switch(buffer[buffercount]) {
             case NOT:
                 return(traverse(buffer, ++buffercount));
             case OR:
             case AND:
                 return(traverse(buffer, traverse(buffer, ++buffercount)));             
         }
         return(0); // should never get here
     }
 
     /**
      * Loads data file and converts "true"| "false" values 
      * in fitness cases to booleans.
      */
     void setupFitness(String fname) {
         try {
             int i,j;
             String line;
 
             BufferedReader in = 
             new BufferedReader(
                                 new
                                 FileReader(fname));
             line = in.readLine();
             StringTokenizer tokens = new StringTokenizer(line);
             varnumber = Integer.parseInt(tokens.nextToken().trim());
             randomnumber = Integer.parseInt(tokens.nextToken().trim());
             minrandom = Double.parseDouble(tokens.nextToken().trim());
             maxrandom = Double.parseDouble(tokens.nextToken().trim());
             fitnesscases = Integer.parseInt(tokens.nextToken().trim());
             targets = new boolean[fitnesscases][varnumber+1];
             if (varnumber + randomnumber >= FSET_START) { 
                 System.out.println("too many variables and constants");
             }
             
             for (i = 0; i < fitnesscases; i ++) {
                 line = in.readLine();
                 tokens = new StringTokenizer(line);
                 for (j = 0; j <= varnumber; j++) {
                     targets[i][j] = (tokens.nextToken().trim().equals("true")) ? true : false;
                 }
             }
             in.close();
         } catch(FileNotFoundException e) {
             System.out.println("ERROR: Please provide a data file");
             System.exit(0);
         } catch(Exception e) {
             System.out.println("ERROR: Incorrect data format");
             System.exit(0);
         }
     }
 
     /**
      * Calculates fitness of boolean equation.
      *
      * Zero is the optimal fitness.
      * 
      * Every time the result does not match the truth table result 
      * 1 will be subtracted from the overall fitness.
      */
     int fitnessFunction(char[] Prog) {
         int i = 0; 
         int len;
         boolean result;
         int fit = 0;
         len = traverse(Prog, 0);
         for (i = 0; i < fitnesscases; i ++) {
             for (int j = 0; j < varnumber; j ++) {
                 x[j] = targets[i][j];
             }
             program = Prog;
             PC = 0;
             result = run();
             fit += (result == targets[i][varnumber]) ? 0 : 1;
         }
         return(-fit);
     }
 
     /**
      * Recursively grows a random tree (equation)
      */
     int grow(char[] buffer, int pos, int max, int depth) {
         char prim = (char) rd.nextInt(2);
         int one_child;
 
         if (pos >= max) {
             return(-1);
         }
         
         if (pos == 0) {
             prim = 1;
         }
         
         if (prim == 0 || depth == 0) {
             prim = (char) rd.nextInt(varnumber + randomnumber);
             buffer[pos] = prim;
             return(pos+1);
         } else {
             prim = (char) (rd.nextInt(FSET_END - FSET_START + 1) + FSET_START);
             switch(prim) {
             case NOT: {
                 buffer[pos] = prim;
                 one_child = grow(buffer, pos+1, max,depth-1);
                 if (one_child < 0) {
                     return -1;
                 } else {
                     return one_child;
                 }
             }
             case AND: 
             case OR: 
                 buffer[pos] = prim;
                 one_child = grow(buffer, pos+1, max,depth-1);
                 if (one_child < 0) {
                     return(-1);
                 }
                 return(grow(buffer, one_child, max,depth-1));
             }
         }
         return(0); // should never get here
     }
     
     /**
      * Prints the generated boolean equation. 
      */
     int printIndiv(char[] buffer, int buffercounter) {
         int a1 = 0; 
         int a2 = 0;
 
         if (buffer[buffercounter] < FSET_START) {
             if (buffer[buffercounter] < varnumber) {
                 System.out.print("X"+ (buffer[buffercounter] + 1));
             } else {
                 System.out.print(x[buffer[buffercounter]]);
             }
             return(++buffercounter);
         }
         
         switch(buffer[buffercounter]) {
             case NOT: System.out.print("!( ");
                 a1=printIndiv(buffer, ++buffercounter);
                 a2=a1;
                 break;
             case AND: System.out.print("( ");
                 a1=printIndiv(buffer, ++buffercounter); 
                 System.out.print(" && "); 
                 a2=printIndiv(buffer, a1); 
                 break;
             case OR: System.out.print("( ");
                 a1=printIndiv(buffer, ++buffercounter); 
                 System.out.print(" || ");
                 a2=printIndiv(buffer, a1);  
                 break;
         }
 
         System.out.print(" )"); 
         return(a2);
     }
     
 
     static char [] buffer = new char[MAX_LEN];
 
     /**
      * Creates a random individual by growing a tree (equation)
      */
     char [] createRandomIndiv(int depth) {
         char [] ind;
         int len;
 
         len = grow(buffer, 0, MAX_LEN, depth);
 
         while (len < 0) {
             len = grow(buffer, 0, MAX_LEN, depth);
         }
 
         ind = new char[len];
 
         System.arraycopy(buffer, 0, ind, 0, len); 
         return(ind);
     }
 
     /**
      * Creates a population of random trees (equations)
      */
     char[][] createRandomPop(int n, int depth, int[] fitness) {
         char[][] pop = new char[n][];
         int i;
         
         for (i = 0; i < n; i ++) {
             pop[i] = createRandomIndiv(depth);
             fitness[i] = fitnessFunction(pop[i]);
         }
         return(pop);
     }
 
     /**
      * Prints out the generation number, avg. fitness,
      * best fitness, avg. size, and the best individual 
      * for the current generation.
      */
     void stats(int[] fitness, char [][] pop, int gen) {
         int i, best = rd.nextInt(POPSIZE);
         int node_count = 0;
         fbestpop = fitness[best];
         favgpop = 0;
 
         for (i = 0; i < POPSIZE; i++) {
             node_count += traverse(pop[i], 0);
             favgpop += fitness[i];
             if (fitness[i] > fbestpop) {
                 best = i;
                 fbestpop = fitness[i];
             }
         }
         avg_len = (double) node_count / POPSIZE;
         favgpop /= POPSIZE;
         System.out.print("Generation="+gen+" Avg Fitness="+(-favgpop)+
                          " Best Fitness="+(-fbestpop)+" Avg Size="+avg_len+
                          "\nBest Individual: ");
         printIndiv(pop[best], 0);
         System.out.print("\n");
         System.out.flush();
     }
 
     /**
      * Parent selection using tournament selection.
      * Best fitness is a maximum.
      */
     int tournament(int[] fitness, int tsize) {
         int best = rd.nextInt(POPSIZE), i, competitor;
         int fbest = Integer.MIN_VALUE;
         
         for (i = 0; i < tsize; i ++) {
             competitor = rd.nextInt(POPSIZE);
             if (fitness[competitor] > fbest) {
                 fbest = fitness[competitor];
                 best = competitor;
             }
         }
         return(best);
     }
  
     /**
      * Parent selection using tournament selection.
      * Best fitness is a minimum.
      */   
     int negativeTournament(int[] fitness, int tsize) {
         int worst = rd.nextInt(POPSIZE), i, competitor;
         int fworst = Integer.MAX_VALUE;
         
         for (i = 0; i < tsize; i ++) {
             competitor = rd.nextInt(POPSIZE);
             if (fitness[competitor] < fworst) {
                 fworst = fitness[competitor];
                 worst = competitor;
             }
         }
         return(worst);
     }
     
     /**
      * Subtree crossover
      */
     char[] crossover(char[] parent1, char[] parent2) {
         int xo1start, xo1end, xo2start, xo2end;
         char [] offspring;
         int len1 = traverse(parent1, 0);
         int len2 = traverse(parent2, 0);
         int lenoff;
         
         xo1start =    rd.nextInt(len1);
         xo1end = traverse(parent1, xo1start);
         
         xo2start =    rd.nextInt(len2);
         xo2end = traverse(parent2, xo2start);
         
         lenoff = xo1start + (xo2end - xo2start) + (len1-xo1end);
 
         offspring = new char[lenoff];
 
         System.arraycopy(parent1, 0, offspring, 0, xo1start);
         System.arraycopy(parent2, xo2start, offspring, xo1start,    
                             (xo2end - xo2start));
         System.arraycopy(parent1, xo1end, offspring, 
                             xo1start + (xo2end - xo2start), 
                             (len1-xo1end));
 
         return(offspring);
     }
     
     /**
      * Point mutation.
      *
      * Due to the preorder tree traversal array, other types of 
      * mutation algorithms not easy to implement?
      *
      * The NOT operator cannot be mutated since we only have 
      * one unary operator. An invalid tree will occur if we 
      * try to swap in an AND or OR operator. 
      */
     char[] mutation(char[] parent, double pmut) {
         int len = traverse(parent, 0), i;
         int mutsite;
         char [] parentcopy = new char [len];
         
         System.arraycopy(parent, 0, parentcopy, 0, len);
         for (i = 0; i < len; i ++) {    
             if (rd.nextDouble() < pmut) {
                 mutsite = i;
                 if (parentcopy[mutsite] < FSET_START) {
                     parentcopy[mutsite] = (char) rd.nextInt(varnumber+randomnumber);
                 } else {
                     switch(parentcopy[mutsite]) {
                     case AND: 
                     case OR: 
                          parentcopy[mutsite] = 
                                (char) (rd.nextInt(FSET_END - FSET_START) 
                                             + FSET_START + 1);
                     }
                 }
             }
         }
         return(parentcopy);
     }
     
     /**
      * Prints parameters/settings for the run
      */
     void printParams() {
         System.out.print("-- MY TINY GP (Java version) --\n");
         System.out.print("SEED="+seed+"\nMAX_LEN="+MAX_LEN+
                 "\nPOPSIZE="+POPSIZE+"\nDEPTH="+DEPTH+
                         "\nCROSSOVER_PROB="+CROSSOVER_PROB+
                         "\nPMUT_PER_NODE="+PMUT_PER_NODE+
                         "\nMIN_RANDOM="+minrandom+
                         "\nMAX_RANDOM="+maxrandom+
                         "\nGENERATIONS="+GENERATIONS+
                         "\nTSIZE="+TSIZE+
                         "\n----------------------------------\n");
     }
 
     /**
      * Constructor (loads data and builds initial population)
      */
     public MyTinyGp(String fname, long s) {
         fitness = new int[POPSIZE];
         seed = s;
         if (seed >= 0) {
             rd.setSeed(seed);
         }
         setupFitness(fname);
         
         pop = createRandomPop(POPSIZE, DEPTH, fitness);
     }
 
     /**
      * This function evolves the population through the generations 
      * using subtree crossover and point mutation.
      * 
      * Evolution stops after best fitness found or max generations reached.
      */
     void evolve() {
         int gen = 0, indivs, offspring, parent1, parent2, parent;
         int newfit;
         char []newind;
         printParams();
         stats(fitness, pop, 0);
         for (gen = 1; gen < GENERATIONS; gen ++) {
             if (fbestpop == 0) {
                 System.out.print("PROBLEM SOLVED\n");
                 System.exit(0);
             }
             for (indivs = 0; indivs < POPSIZE; indivs ++) {
                 if (rd.nextDouble() < CROSSOVER_PROB) {
                     parent1 = tournament(fitness, TSIZE);
                     parent2 = tournament(fitness, TSIZE);
                     newind = crossover(pop[parent1],pop[parent2]);
                 } else {
                     parent = tournament(fitness, TSIZE);
                     newind = mutation(pop[parent], PMUT_PER_NODE);
                 }
                 newfit = fitnessFunction(newind);
                 offspring = negativeTournament(fitness, TSIZE);
                 pop[offspring] = newind;
                 fitness[offspring] = newfit;
             }
             stats(fitness, pop, gen);
         }
         System.out.print("PROBLEM *NOT* SOLVED\n");
         System.exit(1);
     }
 
     /**
      * Main program method.
      *
      * Optioanl command line arg formats:
      * - filename 
      * - seed filename
      */
     public static void main(String[] args) {
         String fname = "problem.dat";
         long s = -1;
         
         if (args.length == 2) {
             s = Integer.valueOf(args[0]).intValue();
             fname = args[1];
         }
         if (args.length == 1) {
             fname = args[0];
         }
         
         MyTinyGp gp = new MyTinyGp(fname, s);
         gp.evolve();
     }
 };
