 package com.ubs.gedit.gedex.actor;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ForkJoinPool;
 import java.util.concurrent.Future;
 
 public class ForkJoinPoolExample {
 
     private double currentPrice;
     private double strike;
     private double riskFreeRate;
     private double tau;
     private double annualVolatility;
 
     public ForkJoinPoolExample(double currentPrice, double strike, double riskFreeRateInPercent, double tau, double annualVolatilityInPercent) {
         this.currentPrice = currentPrice;
         this.strike = strike;
         this.riskFreeRate = riskFreeRateInPercent / 100;
         this.tau = tau;
         this.annualVolatility = annualVolatilityInPercent / 100;
     }
 
     public double digitalOption(double payoff, int timePeriods, int simulation) throws Exception {
 
         ForkJoinPool pool = new ForkJoinPool(); //Use all available processors
        double totalResult = pool.invoke(new TotalPayoffTask<Double>(tau, riskFreeRate, annualVolatility, currentPrice, timePeriods, simulation, strike, payoff));
         pool.shutdown();
 
         double average = totalResult / simulation;
         double pvAverage = average * Math.exp(-riskFreeRate*tau);
 
         return pvAverage;
     }
 
 }
