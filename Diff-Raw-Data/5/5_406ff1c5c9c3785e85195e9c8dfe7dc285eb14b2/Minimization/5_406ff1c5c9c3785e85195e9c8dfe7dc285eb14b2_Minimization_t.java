 import java.util.*;
 import java.io.*;
 
 /**
  * Created with VIM.
  * User: flevix
 */
 
 class Pair {
     double value;
     double coordinate;
 
     Pair(double value, double coordinate) {
         this.value = value;
         this.coordinate = coordinate;
     }
 }
 
 public class Minimization {
     FastScanner in;
     PrintWriter out;
 
     final static int REQUESTS = 998;
    final static int SEGMENTS = 20;
     final static int ITERATIONS = REQUESTS / (SEGMENTS * 2);
     int n;
 
     double query(int dimension, double coordinate) {
         for (int i = 0; i < n; i++) {
             out.print(i != dimension ? "0.0 " : coordinate + " ");
         }
         out.println();
         out.flush();
         return in.nextDouble();
     }
 
     double findCoordMinF(int dimension) {
         double minF = Double.MAX_VALUE;
         double coordMinF = -1.0;
         for (int i = 0; i < SEGMENTS; i++) {
             double left = 1D / SEGMENTS * i;
             double right = 1D / SEGMENTS * (i + 1);
             Pair p = ternarySearch(dimension, left, right);
             if (Double.compare(minF, p.value) == 1) {
                 minF = p.value;
                 coordMinF = p.coordinate;
             }
         }
         return coordMinF;
     }
 
     Pair ternarySearch(int dimension, double left, double right) {
         double a = 0.0, b = 0.0;
         for (int i = 0; i < ITERATIONS; i++) {
             a = (left * 2 + right) / 3;
             b = (left + right * 2) / 3;
             double fa = query(dimension, a);
             double fb = query(dimension, b);
             if (Double.compare(fa, fb) == -1) {
                 right = b;
             } else {
                 left = a;
             }
         }
         double c = (a + b) / 2;
         return new Pair(query(dimension, c), c);
     }
 
     public void solve() throws IOException {
         n = in.nextInt();
         double[] points = new double[n];
         for (int i = 0; i < n; i++) {
             points[i] = findCoordMinF(i);
         }
         for (int i = 0; i < n - 1; i++) {
             out.print(points[i] + " ");
         }
         out.println(points[n - 1]);
         out.flush();
        out.println("minimum " + in.nextDouble());
         out.flush();
     }
 
     public void run() {
         try {
             in = new FastScanner();
             out = new PrintWriter(System.out);
 
             solve();
 
             out.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     public static void main(String[] args) {
         new Minimization().run();
     }
 
     class FastScanner {
         BufferedReader br;
         StringTokenizer st;
 
         FastScanner() {
             br = new BufferedReader(new InputStreamReader(System.in));
         }
 
         FastScanner(File f) {
             try {
                 br = new BufferedReader(new FileReader(f));
             } catch (FileNotFoundException e) {
                 e.printStackTrace();
             }
         }
 
         String next() {
             while (st == null || !st.hasMoreTokens()) {
                 try {
                     st = new StringTokenizer(br.readLine());
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
             return st.nextToken();
         }
 
         int nextInt() {
             return Integer.parseInt(next());
         }
 
         double nextDouble() {
             return Double.parseDouble(next());
         }
     }
 }
