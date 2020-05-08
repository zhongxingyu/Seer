 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package fic;
 
 /**
  *
  * @author shivanth
  */
 /*
  * crystal stores the charge matrix(charge of each atom) for the lattice
  *
  * Primary algorithm - Simulated annealing
  *
  */
  
 import java.awt.Dimension;
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.Scanner;
 import java.util.logging.Logger;
  
  
 public class FIC {
  
     private static int size;
     private static float charge[][];
  
  
  
  
     private static ArrayList Heuristic_data = new ArrayList<Dimension>();
  
     private static float potential_0[][], potential_1[][];
     public final static int HEURISTIC_SIZE = 10;
     private final static int FACTOR_HIGHEST = 1000;
     
  
  
     /**
      * @param args
      */
     public static void main(String[] args) {
         Scanner scan = new Scanner(System.in);
         size = scan.nextInt();
         charge = new float[size + 1][];
  
         potential_0 = new float[size + 1][];
         potential_1 = new float[size + 1][];
         
         for (int i = 1; i <= size; i++) {
             charge[i] = new float[size + 1];
             potential_0[i] = new float[size + 1];
             potential_1[i] = new float[size + 1];
  
         }
  
         for (int i = 1; i <= size; i++) {
             for (int j = 1; j <= size; j++) {
                 charge[i][j] = scan.nextFloat();
  
             }
         }
  
         float result[][]=anneal();
  
         
         
         print_square_matrix(result);
         System.out.println(get_score(result));
  
  
  
  
  
  
     }
  
     /**
      * A function which implements the algorithm for simulated annealing
      * All other functions are used as a helper for simplifying the main loop in this function
      */
     static public float[][] anneal() {//refer http://en.wikipedia.org/wiki/Simulated_annealing
         float best[][], candidate[][], current[][], avg;
         Logger l;
  
         int factor = 0, without_hit = 0;
         Random r = new Random();
         best = new float[size + 1][];
         for (int i = 1; i <= size; i++) {
             best[i] = new float[size + 1];
         }
         best = first_best(best);//Start off the algorithm by finding an optimal best
         current = clone_array(best);
         float current_score;
         float best_score = current_score= get_score(best), score;
         while (factor <= FACTOR_HIGHEST) {  /*
              *SETUP FUNCTION P() AND FIRST BEST.
              * CAN IMPLEMENT OPTIMIZATION ON NEXT NEIGHBOUR .
              * ANNEALING IS DONE USING AN ARGUEMENT FOR P
              */
             candidate = next_neighbor(current);
             score = get_score(candidate);
             if (score < best_score) {
                 best = clone_array(candidate);
                 current = clone_array(candidate);
                 current_score=get_score(current);
                 best_score = score;
                 without_hit = 0;
  
             }
             without_hit++;
             float t=Temperature(factor);
             if (P(t,current_score,score) > r.nextFloat() ) {
                 current = clone_array(candidate);
  
  
             }
  
             factor++;
  
         }
         return best;
     }
  
     static private float P(float t, float current_score, float score) {
         if(score<current_score){
             return 1;
         }
         else
             return (float) Math.pow(Math.E, (current_score-score) / t);
  
     }
  
     static private float[][] next_neighbor(float[][] current_best) {
         //Done- Never resets a charge set to polarity one back to zero,build on it
         //TODO has to improve on the mathematical part of annealing , the limits set are idiotic assumptions :-)
         //findmax1() ideally returns the dimension of point whose value when toggled'll make the  crystal more stable
         int a[] = findmax1(current_best);
         int x = a[0];
         int y = a[1];
         float new_best[][] = clone_array(current_best);
         /*Toggle new_best[x][y]*/
  
             if (new_best[x][y] == 0) {
                 new_best[x][y] = 1;
             } else {
                 new_best[x][y] = 0;
             }
         return new_best;
     }
  
     /**
      * @param polarity The polarity matrix
      * @param x gives the co-ordinates of the atom
      * @return Consults the charge matrix and finds the force contributed by the atom at co-ordinate x,y
      *
      */
     static public float get_force(float polarity[][], float x, float y) {
         float force = 0f;
         if ((int) x % 2 == 0) {
             y = y + .5f;
         }
  
         if ((get(polarity, x - 1, y - .5f) == get(polarity, x, y))) {
             force += get(x - 1, y - .5f) * get(x, y);
         }
         if ((get(polarity, x - 1, y + .5f) == get(polarity, x, y))) {
             force += get(x - 1, y + .5f) * get(x, y);
         }
         if ((get(polarity, x, y - 1) == get(polarity, x, y))) {
             force += get(x, y - 1) * get(x, y);
         }
  
  
         if ((get(polarity, x, y + 1) == get(polarity, x, y))) {
             force += get(x, y + 1) * get(x, y);
         }
         if ((get(polarity, x + 1, y - .5f) == get(polarity, x, y))) {
             force += get(x + 1, y - .5f) * get(x, y);
         }
         if ((get(polarity, x + 1, y + .5f) == get(polarity, x, y))) {
             force += get(x + 1, y + .5f) * get(x, y);
         }
         return force;
     }
  
     static public float get_score(float polarity[][]) {
         float total_force = 0f;
         for (int i = 1; i < polarity.length; i++) {
             for (int j = 1; j < polarity[i].length; j++) {
  
                 total_force += get_force(polarity, i, j);
  
             }
         }
         return total_force / 2;
     }
  
     static public float get(float polarity[][], float x, float y) {
         if (x < polarity.length && x > 0) {
             if (y < polarity[(int) Math.floor(x)].length && y > 0) {
                 return polarity[(int) Math.floor(x)][(int) Math.floor(y)];
             }
         }
         return 0;
     }
  
     static public float get(float x, float y) {
         if (x < charge.length && x > 0) {
             if (y < charge[(int) Math.floor(x)].length && y > 0) {
                 return charge[(int) Math.floor(x)][(int) Math.floor(y)];
             }
         }
         return 0;
     }
  
     private static float[][] first_best(float[][] best) {
         int a[] = findmax(charge);
         int x = a[0];
         int y = a[1];
         for (int i = 1; i < best.length; i++) {
             for (int j = 1; j < best[i].length; j++) {
                 if (i == x && j == y) {
                     best[i][j] = 1;
                     break;
                 }
                 best[i][j] = 0;
             }
         }
  
  
         return best;
     }
  
     private static int[] findmax(float p[][]) {
         float max = 0;
         int maxi = 0, maxj = 0;
         for (int i = 1; i < p.length; i++) {
             for (int j = 1; j < p[i].length; j++) {
                 if (max < p[i][j]) {
                     max = p[i][j];
                 }
                 maxi = i;
                 maxj = j;
             }
         }
         int a[] = {maxi, maxj};
         return a;
     }
  
     /**
      * @param
      * @param p the polarity matrix of the current state
      * @return the array index {x,y} so that toggling p[x][y] gives/leads to better solution
      * The findmax1 finds the atom whose charge when toggled, has a huge effect on the stability of the system .
      * It does so by picking the the atom that has the highest potential in the given scenario . 
      */
     private static int[] findmax1(float p[][]) {
  
         //Bad idea- Implement setting back to zero logic here can use a class wide static flag to implement the changing the polarity
         // A better idea would be to just toggle the value given by findmax1-Idiot
         //TODO all heuristic logic goes in here make use of the heuristc data in the heuristic_data array-make sure you fill the heuristic_data array b4 doin it :-)
         //TODO what heustics to use ? Find it out idiot
         //TODO How about changing to finding the potential of each point in terms of their potential-experimented and tested . Assumed to work fine
  
         float max = 0;
         int maxi = 0, maxj = 0;
  
         Random r = new Random();
         find_potential(p);
  
         for (int i = 1; i < p.length; i++) {
             for (int j = 1; j < p[i].length; j++) {
                 float temp = potential_0[i][j] - potential_1[i][j];
                 if (p[i][j] == 0) {
                     if (temp > max && !Heuristic_data.contains(new Dimension(i, j))) {
                         Heuristic_data.add(new Dimension(i, j));
                         if (Heuristic_data.size() > HEURISTIC_SIZE) {
                             Heuristic_data.remove(Heuristic_data.get(1));
                         }
                         max = temp;
                         maxi = i;
                         maxj = j;
                     }
  
                 } else if (p[i][j] == 1) {
                     if (-temp > max && !Heuristic_data.contains(new Dimension(i, j))) {
                         Heuristic_data.add(new Dimension(i, j));
  
                         if (Heuristic_data.size() > HEURISTIC_SIZE) {
  
                             Heuristic_data.remove(Heuristic_data.get(1));
  
  
                         }
                         max = temp;
                         maxi = i;
                         maxj = j;
                     }
  
                 }
             }
         }
  
  
         //int a[] = {(Math.abs(r.nextInt()) % (p.length - 2)) + 1, (Math.abs(r.nextInt()) % (p.length - 2)) + 1};
            Dimension d=null;
         if (maxi == 0 || maxj == 0) {
            if(Heuristic_data.size()==1){
                d=(Dimension)Heuristic_data.get(0);
                Heuristic_data.remove(d);
            }
            else {
                d = (Dimension) (Heuristic_data.get((Math.abs(r.nextInt()) % (Heuristic_data.size()-1))+1 ));
            //(iter.next());
                maxi = d.width;
                maxj = d.height;
                Heuristic_data.remove(d);
            }
  
  
         }
         int a[] = {maxi, maxj};
         return a;
     }
  
     static private void find_potential(float p[][]) {
         for (int i = 1; i < p.length; i++) {
             for (int j = 1; j < p[i].length; j++) {
                 float a[][] = clone_array(p);
                 a[i][j] = 0;
                 potential_0[i][j] = get_force(a, j, j);
                 a[i][j] = 1;
                 potential_1[i][j] = get_force(a, j, j);
  
             }
         }
     }
  
     static void print_square_matrix(float p[][]) {
         for (int i = 1; i < p.length; i++) {
             for (int j = 1; j < p[i].length; j++) {
                 System.out.print(p[i][j] + " ");
             }
             System.out.println();
         }
     }
  
     static float[][] clone_array(float p[][]) {
         float clone[][];
         clone = new float[p.length][];
         for (int i = 1; i < p.length; i++) {
             clone[i] = p[i].clone();
         }
         return clone;
     }
 
     private static float Temperature(int factor) {
         return factor/ FACTOR_HIGHEST;
         //return 0;
     }
 }
 //BE CAREFUL array indexes starts at 1 for charge and all other square matrices
