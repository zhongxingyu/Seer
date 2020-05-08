 package distributions;
 
 import primitives.IStatistical;
 import primitives.Statistic;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 /**
  * Created by IntelliJ IDEA.
  * User: SONY
  * Date: 25.05.12
  * Time: 0:47
  * To change this template use File | Settings | File Templates.
  */
 public class ExponentialDistribution implements IStatistical<Double, Double>{
     private ArrayList<Double> performed;
 
     private double p;
 
     private Random rnd;
 
     public ExponentialDistribution(double p) {
         this.p = p;
         this.performed = new ArrayList<Double>();
         this.rnd = new Random(System.currentTimeMillis());
     }
 
     public double next() {
         return this.next(rnd.nextDouble());
     }
 
     public double next(double urv) {
        double  actual = -Math.log(urv) / p ;
         performed.add(actual);
         return actual;
     }
 
     public Statistic<Double> getMean() {
         int N = performed.size();
         double actual = 0;
         for (double t : performed) {
             actual += t;
         }
         return new Statistic<Double>(1/ p, actual / N);
     }
 
     public Statistic<Double> getDispersion() {
         int N = performed.size() - 1;
         double e = getMean().real;
         double actual = 0;
         for (double t : performed) {
             actual += Math.pow(e - t, 2);
         }
         return new Statistic<Double>(e / p, actual / N);
     }
 }
