 package com.ubs.gedit.gedex.actor;
 
 import java.util.concurrent.RecursiveTask;
 
 public class TotalPayoffTask extends RecursiveTask<Double> {
 
     public static final int TASK_THRESHOLD = 12 * 10000;
     private final double tau;
     private final double riskFreeRate;
     private final double annualVolatility;
     private final double currentPrice;
     private final int timePeriods;
     private final int simulation;
     private final double strike;
     private final double payoff;
 
     public TotalPayoffTask(double tau, double riskFreeRate, double annualVolatility, double currentPrice, int timePeriods, int simulation, double strike, double payoff) {
         this.tau = tau;
         this.riskFreeRate = riskFreeRate;
         this.annualVolatility = annualVolatility;
         this.currentPrice = currentPrice;
         this.timePeriods = timePeriods;
         this.simulation = simulation;
         this.strike = strike;
         this.payoff = payoff;
     }
 
     @Override
     protected Double compute() {
 
         //Single thread if it's a small task
         if (simulation * timePeriods < TASK_THRESHOLD){
             try {
                 return singleThreadCompute(simulation);
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         //Divide and conqure
         int simDoneByFirstOne = simulation/2;
         int simDoneBySecondOne = simulation - simDoneByFirstOne;
 
         TotalPayoffTask firstTask = new TotalPayoffTask(tau, riskFreeRate,annualVolatility,currentPrice, timePeriods, simDoneByFirstOne, strike, payoff);
         TotalPayoffTask secondTask = new TotalPayoffTask(tau, riskFreeRate,annualVolatility,currentPrice, timePeriods, simDoneBySecondOne, strike, payoff);
 
         firstTask.fork();
         double secondResult = secondTask.compute();
         double firstResult = (Double) firstTask.join();
 
         return firstResult + secondResult;
     }
 
     private Double singleThreadCompute(int simulation) throws Exception {
         FinalPriceCallableSimulation priceSim = new FinalPriceCallableSimulation(tau, riskFreeRate, annualVolatility, currentPrice, timePeriods);
         double totalPayoff = 0;
         for (int i = 0; i< simulation; i++){
             double price = (Double) priceSim.call();
             totalPayoff += price >= strike ? payoff : 0;
         }
         return totalPayoff;
     }
 }
