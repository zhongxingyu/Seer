 import java.io.*;
 import java.util.HashMap;
 import java.util.StringTokenizer;
 
 
 public class Function implements Runnable {
     StringTokenizer st;
     BufferedReader in;
     PrintWriter out;
 
     long mod = 1L << 32;
     HashMap<Integer, Integer> result = new HashMap<Integer, Integer>(1000);
 
     public static void main(String[] args) {
         new Thread(new Function()).start();
     }
 
     public void run() {
         try {
             in = new BufferedReader(new FileReader("function.in"));
             out = new PrintWriter(new FileWriter("function.out"));
             solve();
         } catch (Exception e) {
             e.printStackTrace();
             System.exit(9000);
         } finally {
             out.flush();
             out.close();
         }
     }
 
     String nextToken() throws IOException {
         while (st == null || !st.hasMoreTokens()) {
             st = new StringTokenizer(in.readLine());
         }
         return st.nextToken();
     }
 
     int nextInt() throws NumberFormatException, IOException {
         return Integer.parseInt(nextToken());
     }
 
     long nextLong() throws NumberFormatException, IOException {
         return Long.parseLong(nextToken());
     }
 
     double nextDouble() throws NumberFormatException, IOException {
         return Double.parseDouble(nextToken());
     }
 
     void solve() throws NumberFormatException, IOException {
         int arg = nextInt();
         int res = function(arg);
         out.print(res);
     }
 
     private int function(int arg) {
        if (arg <= 2) return 2;
         if (arg % 2 == 0) {
             int a = arg - 1;
             int b = arg - 3;
             return (int) ((calculate(a) + calculate(b)) % mod);
         }
         if (arg % 2 == 1) {
            int a = (int) Math.floor((6d * arg / 7d));
            int b = (int) Math.floor(2d * arg / 3d);
             return (int) ((calculate(a) + calculate(b)) % mod);
         }
         return 0;
     }
 
     private long calculate(int a) {
         int resA;
         if (result.containsKey(a)) {
             resA = result.get(a);
         } else {
             resA = function(a);
             result.put(a, resA);
         }
         return resA;
     }
 }
