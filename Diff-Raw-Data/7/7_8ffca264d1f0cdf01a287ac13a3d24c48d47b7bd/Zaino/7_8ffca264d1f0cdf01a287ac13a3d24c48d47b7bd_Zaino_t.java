 /*
  * Chapter 13
  * Paragraph 6
  *
  * Rucksack problem
  */
 
 public class Zaino {
     private final int DIM = 10000;
     public int D[][] = new int[DIM][DIM];
 
     public int zaino(int p[], int v[], int i, int c) {
        if (i < 0 || c == 0) {
             return 0;
         }
 
        if (c < 0) {
            return Integer.MIN_VALUE;
        }

         if (D[i][c] == -1) {
             D[i][c] = Math.max(zaino(p, v, i-1, c), zaino(p, v, i-1, c-v[i]) + p[i]);
         }
 
         return D[i][c];
     }
 
 
     public static void main(String[] args) {
         int c = 100;
         int n = 9;
         int p[] = {260, 245, 200, 100, 80, 65, 60, 60, 10};
         int v[] = {70, 60, 50, 40, 30, 20, 10, 10, 1};
 
         Zaino z = new Zaino();
 
         for (int i=0; i<=n; i++) {
             for (int j=0; j<=c; j++) {
                 z.D[i][j] = -1;
             }
         }
 
         System.out.println("Result:" + z.zaino(p, v, n-1, c));
     }
 }
