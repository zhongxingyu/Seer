 /*   
    Copyright 2011-2012 The Cassandra Consortium (cassandra-fp7.eu)
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
  */
 package eu.cassandra.training.activity;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.Arrays;
 import java.util.Random;
 import java.util.Scanner;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBObject;
 
 import eu.cassandra.training.response.Incentive;
 import eu.cassandra.training.response.IncentiveVector;
 import eu.cassandra.training.response.PricingVector;
 import eu.cassandra.training.utils.Constants;
 
 /**
  * This class is used for implementing a Normal (Gaussian) distribution to use
  * in the activity models that are created in the Training Module of
  * Cassandra Project. The same class has been used, with small alterations in
  * the main Cassandra Platform.
  * 
  * @author Christos Diou, Antonios Chrysopoulos
  * @version 0.9, Date: 29.07.2013
  */
 
 public class Gaussian implements ProbabilityDistribution
 {
 
   /**
    * The name of the Normal distribution.
    */
   private String name = "";
 
   /**
    * The type of the Normal distribution.
    */
   private String type = "";
 
   /**
    * The mean value of the Normal distribution.
    */
   protected double mean;
 
   /**
    * The standard deviation value of the Normal distribution.
    */
   protected double sigma;
 
   /**
    * A boolean variable that shows if the values of the Normal distribution
    * histogram
    * has been precomputed or not.
    */
   protected boolean precomputed;
 
   /**
    * A variable presenting the number of bins that are created for the histogram
    * containing the values of the Normal distribution.
    */
   protected int numberOfBins;
 
   /**
    * The starting point of the bins for the precomputed values.
    */
   protected double precomputeFrom;
 
   /**
    * The ending point of the bins for the precomputed values.
    */
   protected double precomputeTo;
 
   /**
    * An array containing the probabilities of each bin precomputed for the
    * Normal distribution.
    */
   protected double[] histogram;
 
   /** The id of the distribution as given by the Cassandra server. */
   private String distributionID = "";
 
   /**
    * Function that computes the phi of a value.
    * 
    * @param x
    *          The selected value.
    * 
    * @return the phi value of the input value.
    */
   private static double phi (double x)
   {
     return Math.exp(-(x * x) / 2) / Math.sqrt(2 * Math.PI);
   }
 
   /**
    * Function that computes the phi of a value.
    * 
    * @param x
    *          The selected value.
    * @param mu
    *          The mean value parameter of the Normal Distribution.
    * @param s
    *          The standard deviation value parameter of the Normal Distribution.
    * 
    * @return the phi value of the input value.
    */
   private static double phi (int x, double mu, double s)
   {
     return phi((x - mu) / s) / s;
   }
 
   /**
    * Function that computes the standard Gaussian cdf using Taylor
    * approximation.
    * 
    * @param z
    *          The selected value.
    * 
    * @return the phi value of the input value.
    */
   private static double bigPhi (double z)
   {
     if (z < -8.0) {
       return 0.0;
     }
     if (z > 8.0) {
       return 1.0;
     }
 
     double sum = 0.0;
     double term = z;
     for (int i = 3; Math.abs(term) > 1e-5; i += 2) {
       sum += term;
       term *= (z * z) / i;
     }
     return 0.5 + sum * phi(z);
   }
 
   /**
    * Function that computes the standard Gaussian cdf using Taylor
    * approximation.
    * 
    * @param z
    *          The selected value.
    * @param mu
    *          The mean value parameter of the Normal Distribution.
    * @param s
    *          The standard deviation value parameter of the Normal Distribution.
    * 
    * @return the phi value of the input value.
    */
   protected static double bigPhi (double z, double mu, double s)
   {
     return bigPhi((z - mu) / s);
   }
 
   /**
    * Constructor. Sets the parameters of the standard normal distribution,
    * with mean 0 and standard deviation 1.
    */
   public Gaussian ()
   {
     name = "Generic Normal";
     type = "Normal Distribution";
     mean = 0.0;
     sigma = 1.0;
     precomputed = false;
   }
 
   /**
    * Constructor of a Normal distribution with given parameters.
    * 
    * @param mu
    *          Mean value of the Gaussian distribution.
    * @param s
    *          Standard deviation of the Gaussian distribution.
    */
   public Gaussian (double mu, double s)
   {
     name = "Generic";
     type = "Normal Distribution";
     mean = mu;
     sigma = s;
     precomputed = false;
   }
 
   /**
    * Constructor of a Normal distribution with parameters parsed from a file.
    * 
    * @param filename
    *          The file name of the input file.
    */
   public Gaussian (String filename) throws FileNotFoundException
   {
     name = filename;
     type = "Normal Distribution";
     File file = new File(filename);
     Scanner input = new Scanner(file);
     String nextLine = input.nextLine();
 
     String[] temp = nextLine.split(":");
     int maxValue = Integer.parseInt(temp[1]);
 
     nextLine = input.nextLine();
 
     mean = Double.parseDouble(nextLine.replace(",", "."));
 
     nextLine = input.nextLine();
 
     sigma = Double.parseDouble(nextLine.replace(",", "."));
 
     precompute(0, maxValue, maxValue);
 
     input.close();
 
   }
 
   @Override
   public String getName ()
   {
     return name;
   }
 
   @Override
   public String getDistributionID ()
   {
     return distributionID;
   }
 
   @Override
   public void setDistributionID (String id)
   {
     distributionID = id;
   }
 
   @Override
   public String getDescription ()
   {
     String description = "Gaussian probability density function";
     return description;
   }
 
   @Override
   public int getNumberOfParameters ()
   {
     return 2;
   }
 
   @Override
   public double getParameter (int index)
   {
     switch (index) {
     case 0:
       return mean;
     case 1:
       return sigma;
     default:
       return 0.0;
     }
 
   }
 
   @Override
   public void setParameter (int index, double value)
   {
     switch (index) {
     case 0:
       mean = value;
       break;
     case 1:
       sigma = value;
       break;
     default:
       return;
     }
   }
 
   @Override
   public void precompute (int startValue, int endValue, int nBins)
   {
     if ((startValue >= endValue) || (nBins == 0)) {
       System.out.println("Start Value > End Value or Number of Bins = 0");
       return;
     }
     precomputeFrom = startValue;
     precomputeTo = endValue;
     numberOfBins = nBins;
 
     double div = (endValue - startValue) / (double) nBins;
     histogram = new double[nBins];
 
     double residual =
       bigPhi(startValue, mean, sigma) + 1 - bigPhi(endValue, mean, sigma);
    double res2 = 1 - bigPhi(0, mean, sigma);
    residual /= res2;

     for (int i = 0; i < nBins; i++) {
       // double x = startValue + i * div - small_number;
       double x = startValue + i * div;
       histogram[i] =
         bigPhi(x + div / 2.0, mean, sigma) - bigPhi(x - div / 2.0, mean, sigma);
      histogram[i] += (histogram[i] * residual);
     }
     precomputed = true;
   }
 
   @Override
   public double getProbability (int x)
   {
     return phi(x, mean, sigma);
   }
 
   @Override
   public double getPrecomputedProbability (int x)
   {
     if (!precomputed) {
       return -1;
     }
     double div = (precomputeTo - precomputeFrom) / (double) numberOfBins;
     int bin = (int) Math.floor((x - precomputeFrom) / div);
     if (bin == numberOfBins) {
       bin--;
     }
     return histogram[bin];
   }
 
   @Override
   public int getPrecomputedBin ()
   {
     if (!precomputed) {
       return -1;
     }
     Random random = new Random();
     // double div = (precomputeTo - precomputeFrom) / (double) numberOfBins;
     double dice = random.nextDouble();
     double sum = 0;
     for (int i = 0; i < numberOfBins; i++) {
       sum += histogram[i];
       // if(dice < sum) return (int)(precomputeFrom + i * div);
       if (dice < sum)
         return i;
     }
     return -1;
   }
 
   @Override
   public void status ()
   {
     System.out.print("Normal Distribution with");
     System.out.print(" Mean: " + getParameter(0));
     System.out.println(" Sigma: " + getParameter(1));
     System.out.println("Precomputed: " + precomputed);
     if (precomputed) {
       System.out.print("Number of Bins: " + numberOfBins);
       System.out.print(" Starting Point: " + precomputeFrom);
       System.out.println(" Ending Point: " + precomputeTo);
     }
     System.out.println(Arrays.toString(histogram));
 
   }
 
   @Override
   public double[] getHistogram ()
   {
     return histogram;
   }
 
   @Override
   public double getProbabilityGreaterEqual (int x)
   {
     double prob = 0;
 
     int start = (int) x;
 
     for (int i = start; i < histogram.length; i++)
       prob += histogram[i];
 
     return prob;
   }
 
   @Override
   public double getProbabilityLess (int x)
   {
     return 1 - getProbabilityGreaterEqual(x);
   }
 
   @Override
   public void shifting (int shiftingCase, double[] basicScheme,
                         double[] newScheme)
   {
 
     if (shiftingCase == 0) {
 
       histogram = shiftingOptimal(newScheme);
 
     }
     else if (shiftingCase == 1) {
 
       histogram = shiftingNormal(basicScheme, newScheme);
     }
     else if (shiftingCase == 2) {
 
       histogram = shiftingDiscrete(basicScheme, newScheme);
     }
     else {
       System.out.println("ERROR in shifting function");
     }
 
   }
 
   @Override
   public double[] shiftingPreview (int shiftingCase, double[] basicScheme,
                                    double[] newScheme)
   {
 
     double[] result = new double[Constants.MINUTES_PER_DAY];
 
     if (shiftingCase == 0) {
 
       result = shiftingOptimal(newScheme);
     }
     else if (shiftingCase == 1) {
 
       result = shiftingNormal(basicScheme, newScheme);
     }
     else if (shiftingCase == 2) {
 
       result = shiftingDiscrete(basicScheme, newScheme);
     }
     else {
       System.out.println("ERROR in shifting function");
     }
 
     return result;
 
   }
 
   @Override
   public double[] shiftingOptimal (double[] newScheme)
   {
     double[] result = new double[Constants.MINUTES_PER_DAY];
 
     double sum = 0;
 
     for (int i = 0; i < newScheme.length; i++) {
       result[i] = histogram[i] / newScheme[i];
       sum += result[i];
     }
 
     for (int i = 0; i < result.length; i++)
       result[i] /= sum;
 
     return result;
 
   }
 
   @Override
   public double[] shiftingNormal (double[] basicScheme, double[] newScheme)
   {
     double[] result = Arrays.copyOf(histogram, histogram.length);
 
     PricingVector pricingVector = new PricingVector(basicScheme, newScheme);
 
     IncentiveVector inc = new IncentiveVector(basicScheme, newScheme);
 
     if (pricingVector.getPrices().size() > 1)
       for (Incentive incentive: inc.getIncentives()) {
         result = movingAverage(result, incentive);
       }
 
     return result;
 
   }
 
   @Override
   public double[] shiftingDiscrete (double[] basicScheme, double[] newScheme)
   {
     double[] result = Arrays.copyOf(histogram, histogram.length);
 
     PricingVector pricingVector = new PricingVector(basicScheme, newScheme);
 
     if (pricingVector.getPrices().size() > 1)
       result = discreteAverage(result, pricingVector);
 
     return result;
   }
 
   @Override
   public double[] movingAverage (double[] values, Incentive incentive)
   {
     // Initialize the auxiliary variables.
     int side = -1;
     int startIndex = incentive.getStartMinute();
     int endIndex = incentive.getEndMinute();
     double overDiff = 0;
     double temp = 0;
     String type = "";
     // double sum = 0;
 
     // First, the incentive type is checked (penalty or reward) and then the
     // before and after values are checked to see how the residual percentage
     // will be distributed.
     if (incentive.isPenalty()) {
 
       if (incentive.getBeforeDifference() > 0
           && incentive.getAfterDifference() < 0)
         type = "Both";
 
       if (incentive.getBeforeDifference() > 0
           && incentive.getAfterDifference() >= 0)
         type = "Left";
 
       if (incentive.getBeforeDifference() <= 0
           && incentive.getAfterDifference() < 0)
         type = "Right";
 
       if (incentive.getBeforeDifference() < 0
           && incentive.getAfterDifference() > 0)
         type = "None";
     }
     else {
 
       if (incentive.getBeforeDifference() < 0
           && incentive.getAfterDifference() > 0)
         type = "Both";
 
       if (incentive.getBeforeDifference() < 0
           && incentive.getAfterDifference() <= 0)
         type = "Left";
 
       if (incentive.getBeforeDifference() >= 0
           && incentive.getAfterDifference() > 0)
         type = "Right";
 
       if (incentive.getBeforeDifference() > 0
           && incentive.getAfterDifference() < 0)
         type = "None";
 
     }
 
     // System.out.println("Penalty: " + incentive.isPenalty() + " Type: " +
     // type);
 
     if (!type.equalsIgnoreCase("None")) {
       // In case of penalty the residual percentage is moved out of the window
       // to close distance, either on one or both sides accordingly
       if (incentive.isPenalty()) {
 
         for (int i = startIndex; i < endIndex; i++) {
           temp = incentive.getBase() * values[i] / incentive.getPrice();
           overDiff += values[i] - temp;
           values[i] = temp;
 
         }
         // System.out.println("Over Difference = " + overDiff);
         double additive = overDiff / Constants.SHIFTING_WINDOW_IN_MINUTES;
 
         switch (type) {
 
         case "Both":
 
           side = Constants.SHIFTING_WINDOW_IN_MINUTES / 2;
 
           for (int i = 0; i < side; i++) {
 
             int before = startIndex - i;
             if (before < 0)
               before += Constants.MINUTES_PER_DAY;
             int after = endIndex + i;
             if (after > Constants.MINUTES_PER_DAY - 1)
               after %= Constants.MINUTES_PER_DAY;
 
             values[before] += additive;
             values[after] += additive;
 
           }
           break;
 
         case "Left":
 
           side = Constants.SHIFTING_WINDOW_IN_MINUTES;
 
           for (int i = 0; i < side; i++) {
 
             int before = startIndex - i;
             if (before < 0)
               before += Constants.MINUTES_PER_DAY;
             values[before] += additive;
 
           }
           break;
 
         case "Right":
 
           side = Constants.SHIFTING_WINDOW_IN_MINUTES;
 
           for (int i = 0; i < side; i++) {
 
             int after = endIndex + i;
             if (after > Constants.MINUTES_PER_DAY - 1)
               after %= Constants.MINUTES_PER_DAY;
 
             values[after] += additive;
           }
         }
       }
       // In case of reward a percentage of the close distances are moved in the
       // window, either from one or both sides accordingly.
       else {
         side = Constants.SHIFTING_WINDOW_IN_MINUTES * 2;
         switch (type) {
 
         case "Both":
 
           for (int i = startIndex - side; i < startIndex; i++) {
 
             int index = i;
 
             if (index < 0)
               index += Constants.MINUTES_PER_DAY;
 
             temp = incentive.getPrice() * values[index] / incentive.getBase();
             overDiff += values[index] - temp;
             values[index] = temp;
           }
 
           for (int i = endIndex; i < endIndex + side; i++) {
 
             int index = i;
 
             if (index > Constants.MINUTES_PER_DAY - 1)
               index %= Constants.MINUTES_PER_DAY;
 
             temp = incentive.getPrice() * values[index] / incentive.getBase();
             overDiff += values[index] - temp;
             values[index] = temp;
           }
           break;
 
         case "Left":
 
           for (int i = startIndex - 2 * side; i < startIndex; i++) {
 
             int index = i;
 
             if (index < 0)
               index += Constants.MINUTES_PER_DAY;
 
             temp = incentive.getPrice() * values[index] / incentive.getBase();
             overDiff += values[index] - temp;
             values[index] = temp;
           }
           break;
 
         case "Right":
 
           for (int i = endIndex; i < endIndex + 2 * side; i++) {
 
             int index = i;
 
             if (index > Constants.MINUTES_PER_DAY - 1)
               index %= Constants.MINUTES_PER_DAY;
 
             temp = incentive.getPrice() * values[index] / incentive.getBase();
             overDiff += values[index] - temp;
             values[index] = temp;
           }
 
         }
         // System.out.println("Over Difference = " + overDiff);
 
         double additive = overDiff / (endIndex - startIndex);
 
         for (int i = startIndex; i < endIndex; i++)
           values[i] += additive;
 
       }
 
     }
     // for (int i = 0; i < values.length; i++)
     // sum += values[i];
     // System.out.println("Summary: " + sum);
 
     return values;
   }
 
   @Override
   public double[] discreteAverage (double[] values, PricingVector pricing)
   {
 
     // Initialize the auxiliary variables.
     double temp = 0;
     // double sum = 0;
     double overDiff = 0;
     int start, end;
     double newPrice;
 
     // Finding the cheapest window in the day.
     int cheapest = pricing.getCheapest();
     int startCheapest =
       pricing.getPrices(pricing.getCheapest()).getStartMinute();
     int endCheapest = pricing.getPrices(pricing.getCheapest()).getEndMinute();
     int durationCheapest = endCheapest - startCheapest;
     double cheapestPrice = pricing.getPrices(pricing.getCheapest()).getPrice();
 
     // Moving from all the available vectors residual percentages to the
     // cheapest one.
     for (int i = 0; i < pricing.getPrices().size(); i++) {
 
       if (i != cheapest) {
         // sum = 0;
         overDiff = 0;
         start = pricing.getPrices(i).getStartMinute();
         end = pricing.getPrices(i).getEndMinute();
         newPrice = pricing.getPrices(i).getPrice();
 
         for (int j = start; j <= end; j++) {
           temp = cheapestPrice * values[j] / newPrice;
           overDiff += values[j] - temp;
           values[j] = temp;
         }
 
         double additive = overDiff / durationCheapest;
 
         for (int j = startCheapest; j <= endCheapest; j++)
           values[j] += additive;
 
         // for (int j = 0; j < values.length; j++)
         // sum += values[j];
         // System.out.println("Summary after index " + i + ": " + sum);
 
       }
 
     }
 
     return values;
   }
 
   @Override
   public DBObject toJSON (String activityModelID)
   {
 
     double[] values = new double[1];
     DBObject temp = new BasicDBObject();
     DBObject[] param = new BasicDBObject[1];
 
     param[0] = new BasicDBObject();
 
     param[0].put("mean", mean);
     param[0].put("std", sigma);
 
     temp.put("name", name);
     temp.put("type", type);
     temp.put("description", name + " " + type);
     temp.put("distrType", type);
     temp.put("actmod_id", activityModelID);
     temp.put("parameters", param);
     temp.put("values", values);
 
     return temp;
 
   }
 
 }
