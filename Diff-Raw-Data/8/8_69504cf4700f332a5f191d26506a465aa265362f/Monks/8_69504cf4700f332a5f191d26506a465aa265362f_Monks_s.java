 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Scanner;
 
 /**
  * Jar Class to hold an instance of a set of Jars.
  */
 class Jars {
 
   public final static int A = 0;
   public final static int B = 1;
   public final static int C = 2;
   private int[] jars = {0, 0, 0};
   public int count;
 
   @Override
   public int hashCode() {
    int hash = 3;
    hash = 83 * hash + Arrays.hashCode(this.jars);
     return hash;
   }
 
   @Override
   public boolean equals(Object obj) {
     final Jars other = (Jars) obj;
     return ((this.jars[A] == other.jars[A]) 
             && (this.jars[B] == other.jars[B]) 
             && (this.jars[C] == other.jars[C]));
   }
 
   /**
    * Constructor
    *
    * @param JarA the number of beads in Jar A.
    * @param JarB the number of beads in Jar B.
    * @param JarC the number of beads in Jar C.
    */
   public Jars(int JarA, int JarB, int JarC) {
     jars[A] = JarA;
     jars[B] = JarB;
     jars[C] = JarC;
     count = 0;
     Sort();
   }
 
   /**
    * Copy Constructor
    *
    * @param other The Jars to copy.
    */
   public Jars(Jars other) {
     this.jars[A] = other.jars[A];
     this.jars[B] = other.jars[B];
     this.jars[C] = other.jars[C];
     this.count = other.count;
     Sort();
   }
 
   /**
    * Sort the contents of the jars into numerical order. Thus: a < b < c
    */
   public final void Sort() {
     int tmp;
     if (jars[B] < jars[A]) {
       tmp = jars[B];
       jars[B] = jars[A];
       jars[A] = tmp;
     }
     if (jars[C] < jars[B]) {
       tmp = jars[C];
       jars[C] = jars[B];
       jars[B] = tmp;
     }
     if (jars[B] < jars[A]) {
       tmp = jars[B];
       jars[B] = jars[A];
       jars[B] = tmp;
     }
   }
 
   /**
    * Get if there is an empty Jar within the set.
    */
   public boolean EmptyJar() {
     return ((jars[A] == 0) || (jars[B] == 0) || (jars[C] == 0));
   }
 
   /**
    * Move beads from one jar to another.
    *
    * @param source The jar to take beads from.
    * @param destination The jar to add beads to.
    */
   public void moveBeads(int source, int destination) {
     jars[source] -= jars[destination];
     jars[destination] += jars[destination];
     Sort(); // The resulting jar counts MUST be sorted.
     count++; // Increment the move count.
   }
 }
 
 public class Monks {
 
   /**
    * Main.
    */
   public static void main(String[] args) {
 
     // Read in lines as needed.
     Scanner in = new Scanner(System.in);
     String line = in.nextLine();
     // Quit when our input is "0 0 0"
     while (!line.equals("0 0 0")) {
 
       Scanner sc = new Scanner(line);
       // Output the minimum moves required.
       System.out.printf("%s %d\n", line,
               LeastMoves(new Jars(sc.nextInt(), sc.nextInt(), sc.nextInt())));
       line = in.nextLine(); // Get next line.
     }
   }
 
   /**
    * Find the minimum number of moves required to move beads until at least 1
    * jar is empty. This function is a modified BFS algorithm, with each vertex
    * to search next being derived from the current vertex contents.
    *
    * @param jars The starting jar contents.
    * @return The minimum number of moves.
    */
   static int LeastMoves(Jars jars) {
     // Check for empty jar
     if (jars.EmptyJar()) {
       return 0; // If our start set of jars has an empty jar, then exit.
     } else {
       int leastMoves = 0; // result
       LinkedList<Jars> moves = new LinkedList<Jars>(); // create queue
       HashSet<Integer> visited = new HashSet<Integer>();
       moves.offer(jars); // add initial jars state to queue
 
       while ((!moves.isEmpty()) && (leastMoves == 0)) {
         Jars current = new Jars(moves.poll()); // remove first in queue
         visited.add(current.hashCode());
         // Process the current set of Jars, first check for empty jar
         if (current.EmptyJar()) {
           leastMoves = current.count; // store result
 
         } else {
 
           // Visit all neighbouring vertices (or Jars derived from the
           // current Jar).
           // Since all Jars are stored in numerical order (a<b<c), we
           // can reduce the number of tests to perform, and this also
           // ensures that any subtraction doesn't result in a negative
           // bead count.
           // We maintain a hash set with all visited states to ensure
           // don't revisit them in future
           Jars moveBtoA = new Jars(current);
           moveBtoA.moveBeads(Jars.B, Jars.A);
           int hash = moveBtoA.hashCode();
           if (!visited.contains(hash)) {
             moves.offer(moveBtoA);
             visited.add(hash);
           }
 
           Jars moveCtoA = new Jars(current);
           moveCtoA.moveBeads(Jars.C, Jars.A);
           hash = moveCtoA.hashCode();
           if (!visited.contains(hash)) {
             moves.offer(moveCtoA); // add result to queue
             visited.add(hash);
           }
 
           current.moveBeads(Jars.C, Jars.B);
           hash = current.hashCode();
           if (!visited.contains(hash)) {
             moves.offer(current); // add result to queue
             visited.add(hash);
           }
         }
       }
       // We have found a empty jar set
       return leastMoves;
     }
   }
 }
