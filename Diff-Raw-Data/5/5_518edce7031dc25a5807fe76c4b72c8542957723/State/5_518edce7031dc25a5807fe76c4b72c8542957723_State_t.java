 package com.caseyscarborough.puzzle;
 
 import java.util.Arrays;
 
 /**
  * The state class is responsible for holding the current
  * state of the puzzle, the previous state of the puzzle, as
  * well as other information about the current state, such as the
  * index of the blank space as well as g(n) and h(n).
  *
  * @author Casey Scarborough
  */
 class State {
 
   /** The array representing the puzzle's state. */
   public int[] array = new int[9];
 
   /** The index location of the blank tile in the current state. */
   public int blankIndex;
 
   /** The number of moves since the start. */
   private int g;
 
   /** The number of moves to the goal. */
   private int h;
 
   /** The previous state. */
   private State previous;
 
   /**
    * Initial constructor for the com.caseyscarborough.puzzle.State class.
    * @param input An array representing a puzzle.
    */
   public State(int[] input) {
     this.array = input;
     this.blankIndex = getIndex(input, 0);
     this.previous = null;
     this.g = 0;
     this.h = Puzzle.getHeuristic(this.array);
   }
 
   /**
    * This constructor is used to create a new state based on
    * the previous state and a new blank index.
    * @param previous The previous state.
    * @param blankIndex The new blank index.
    */
   public State(State previous, int blankIndex) {
     this.array = Arrays.copyOf(previous.array, previous.array.length);
     this.array[previous.blankIndex] = this.array[blankIndex];
     this.array[blankIndex] = 0;
     this.blankIndex = blankIndex;
     this.g = previous.g + 1;
     this.h = Puzzle.getHeuristic(this.array);
     this.previous = previous;
   }
 
   /**
    * This method gets the index of a particular value in array.
    * It is primarily used to retrieve the index of the blank tile
    * in the constructor of the com.caseyscarborough.puzzle.State class.
    * @param array A puzzle state array.
    * @param value The value in the array to retrieve the index for.
    * @return int - The index of the tile being searched for.
    */
   public static int getIndex(int[] array, int value) {
     for (int i = 0; i < array.length; i++) {
       if (array[i] == value) return i;
     }
     return -1;
   }
 
   /**
    * The f(n) of the current state. This is calculated by
    * retrieving the g + h of the state.
    * @return int - The f(n) of the current state.
    */
   public int f() {
     return g() + h();
   }
 
   /**
    * This method checks to see if the current state is the solved state.
    * @return True if it is in the solved state, false if it is not.
    */
   public boolean isSolved() {
     int[] p = this.array;
    for (int i = 1; i < p.length - 1; i++)
       if(p[i-1] > p[i]) return false;
 
    return (p[0] == 1);
   }
 
   /**
    * This returns a human-readable string representation
    * of the current state of the puzzle it is called on.
    * @return The puzzle as a string.
    */
   public String toString() {
     int[] state = this.array;
     String s = "\n\n";
     for(int i = 0; i < state.length; i++) {
       if(i % 3 == 0 && i != 0) s += "\n";
       if (state[i] != 0)
         s += String.format("%d ", state[i]);
       else
         s += "  ";
     }
     return s;
   }
 
   /**
    * This method returns a string representation of all
    * steps taken to solve the puzzle.
    * @return String - The puzzle steps as a string.
    */
   public String allSteps() {
     StringBuilder sb = new StringBuilder();
     if (this.previous != null) sb.append(previous.allSteps());
     sb.append(this.toString());
     return sb.toString();
   }
 
   /**
    * This method creates a solution message for when the
    * puzzle has been solved using a StringBuilder.
    * @return String - The solution message.
    */
   public String solutionMessage(long startTime) {
     long solveTime = System.currentTimeMillis() - startTime;
     StringBuilder sb = new StringBuilder();
     sb.append("Here are the steps to the goal state:");
     sb.append(this.allSteps());
     sb.append("\n\nGiven puzzle is SOLVED!");
     sb.append("\nSolution took " + solveTime + "ms and " + this.g + " steps.\n");
     return sb.toString();
   }
 
   public int g() {
     return this.g;
   }
 
   public int h() {
     return this.h;
   }
 
   public State getPrevious() {
     return this.previous;
   }
 
 }
