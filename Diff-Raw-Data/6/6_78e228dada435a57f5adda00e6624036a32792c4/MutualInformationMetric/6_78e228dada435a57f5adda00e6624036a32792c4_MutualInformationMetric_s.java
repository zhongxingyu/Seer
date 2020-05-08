 package org.grouplens.ratingvalue;
 
 import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
 import org.grouplens.lenskit.data.pref.PreferenceDomain;
 import org.grouplens.lenskit.eval.AlgorithmInstance;
 import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
 import org.grouplens.lenskit.eval.metrics.AbstractTestUserMetric;
 import org.grouplens.lenskit.eval.metrics.TestUserMetricAccumulator;
 import org.grouplens.lenskit.eval.traintest.TestUser;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  */
 public class MutualInformationMetric extends AbstractTestUserMetric {
     private static final Logger logger = LoggerFactory.getLogger(MutualInformationMetric.class);
     private String name = "MI";
     private PreferenceDomain inDomain;
     private PreferenceDomain outDomain;
     private double sumValues;
     private int numValues;
     private boolean corrected = false;
 
     public MutualInformationMetric(PreferenceDomain domain, boolean corrected) {
         this.inDomain = domain;
         this.outDomain = domain;
         this.corrected = corrected;
     }
 
     public MutualInformationMetric(PreferenceDomain inDomain, PreferenceDomain outDomain, boolean corrected) {
         this.inDomain = inDomain;
         this.outDomain = outDomain;
         this.corrected = corrected;
     }
 
     public MutualInformationMetric(String name, PreferenceDomain inDomain, PreferenceDomain outDomain, boolean corrected) {
         this.name = name;
         this.inDomain = inDomain;
         this.outDomain = outDomain;
         this.corrected = corrected;
     }
 
     @Override
     public TestUserMetricAccumulator makeAccumulator(AlgorithmInstance algorithm, TTDataSet dataSet) {
         return new Accum();
     }
 
     @Override
     public String[] getColumnLabels() {
         return new String[] { name, name + ".ByUser" };
     }
 
     @Override
     public String[] getUserColumnLabels() {
         return new String[] { name };
     }
 
     public double getMean() {
         if (numValues == 0) {
             return 0.0;
         } else {
             return sumValues / numValues;
         }
     }
     
     class Accum implements TestUserMetricAccumulator {
         MutualInformationCounter counter;
 
         private double userMutualInformationSum;
         private int nratings = 0;
         private int nusers = 0;
 
         public Accum() {
             this.counter = new MutualInformationCounter(inDomain, outDomain, corrected);
         }
 
         @Override
         public String[] evaluate(TestUser user) {
             int n = 0;
             
             MutualInformationCounter userCounter = new MutualInformationCounter(inDomain, outDomain, corrected);
 
             // overall
             for (Long2DoubleMap.Entry e: user.getTestRatings().fast()) {
                 if (Double.isNaN(e.getDoubleValue())) continue;
//                double predicted = Utils.binRating(domain, e.getDoubleValue());
                 double actual = e.getDoubleValue();
                 double predicted = user.getPredictions().get(e.getLongKey());
                counter.count(predicted, actual);
                userCounter.count(predicted, actual);
                 n++;
             }
             if (n > 0) {
                 nratings += n;
                 nusers += 1;
                 double uc = userCounter.calculate();
                 userMutualInformationSum += uc;
                 return new String[]{Double.toString(uc)};
             } else {
                 return null;
             }
         }
 
         @Override
         public String[] finalResults() {
             double v = counter.calculate();
             double uv = userMutualInformationSum / nusers;
             logger.info("{}: overall {}, by-user {}", new Object[] {name, v, uv});
             sumValues += v;
             numValues++;
             return new String[]{
                     Double.toString(v),
                     Double.toString(uv)
             };
         }
     }
 }
