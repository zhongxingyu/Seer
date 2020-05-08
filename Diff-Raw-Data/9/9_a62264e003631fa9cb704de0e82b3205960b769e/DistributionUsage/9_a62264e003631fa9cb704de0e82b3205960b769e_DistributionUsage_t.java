 package usages; /**
  * Created by IntelliJ IDEA.
  * User: SONY
  * Date: 25.05.12
  * Time: 0:15
  * To change this template use File | Settings | File Templates.
  */
 
 import distributions.*;
 import primitives.Statistic;
 
 import java.io.*;
 import java.util.StringTokenizer;
 
 public class DistributionUsage extends Thread {
     public DistributionUsage(String inputFileName, String outputFileName) {
         try {
             if (inputFileName != null) {
                 this.input = new BufferedReader(new FileReader(inputFileName));
             } else {
                 this.input = new BufferedReader(new InputStreamReader(System.in));
             }
             if (outputFileName != null) {
                 this.output = new PrintWriter(outputFileName);
             } else {
                 this.output = new PrintWriter(System.out);
             }
             this.setPriority(Thread.MAX_PRIORITY);
         } catch (Throwable e) {
             System.err.println(e.getMessage());
             e.printStackTrace();
             System.exit(666);
         }
     }
 
     private void solve() throws Throwable {
        ExponentialDistribution distribution = new ExponentialDistribution(12);
         for (int i = 0; i < 100000; ++i) {
             output.print(distribution.next() + " ");
         } 
         output.println();
         Statistic<Double> mean = distribution.getMean();
         output.println(mean);
         Statistic<Double> dispersion = distribution.getDispersion();
         output.println(dispersion);
     }
 
     public void run() {
         try {
             solve();
         } catch (Throwable e) {
             System.err.println(e.getMessage());
             e.printStackTrace();
             System.exit(666);
         } finally {
             output.close();
         }
     }
 
     public static void main(String... args) {
         new DistributionUsage(null, null).start();
     }
 
     private int nextInt() throws IOException {
         return Integer.parseInt(next());
     }
 
     private double nextDouble() throws IOException {
         return Double.parseDouble(next());
     }
 
     private long nextLong() throws IOException {
         return Long.parseLong(next());
     }
 
     private String next() throws IOException {
         while (tokens == null || !tokens.hasMoreTokens()) {
             tokens = new StringTokenizer(input.readLine());
         }
         return tokens.nextToken();
     }
 
     private StringTokenizer tokens;
     private BufferedReader input;
     private PrintWriter output;
 }
