 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package models;
 
 import ext.Ext;
 import utils.Dates;
 
 import java.util.*;
 
 /**
  *
  * @author inf04
  */
 public class LSPStat {
 
     public Map<String, Double> sales = new HashMap<String, Double>();
     public Map<Integer, Integer> nbDays = new HashMap<Integer, Integer>();
     public Map<Integer, Double> avgDay = new HashMap<Integer, Double>();
     public Map<Integer, Double> avgQuarter = new HashMap<Integer, Double>();
     public Map<Integer, Double> goalQuarter = new HashMap<Integer, Double>();
     public Map<Integer, Double> totalMonth = new HashMap<Integer, Double>();
     public Map<Integer, Double> totalDaySum = new HashMap<Integer, Double>();
     public double totalMonthSum = 0;
     public double totalAvgDaySum = 0;
     public Goal goal;
 
     public static Map<Integer, Double> totalByMonth(Calendar date) {
         Map<Integer, Double> totalMap = new HashMap<Integer, Double>();
 
         List results = SaleInfo.totalByMonth(date);
         for (Object result : results) {
             Object line[] = (Object[]) result;
             totalMap.put((Integer) line[0], (Double) line[1]);
         }
 
         return totalMap;
     }
 
     public static LSPStat recapNetByShop(Shop shop, Calendar date) {
         LSPStat lspStat = new LSPStat();
         lspStat.goal = Goal.byShopAndDate(shop,date);
 
         List<SaleInfo> saleInfos = SaleInfo.allYearByShop(shop, date);
         Map<String, Boolean> saleDates = new HashMap<String, Boolean>();
         for (SaleInfo si : saleInfos) {
             lspStat.calcSalesByDay(si);
 
             if (saleDates.get(Ext.format(si.saleDate)) == null) {
                 lspStat.calcNBDays(si);
                 saleDates.put(Ext.format(si.saleDate), Boolean.TRUE);
             }
             lspStat.calcTotalMonth(si);
             lspStat.calcTotalDaySum(si);
         }
 
         lspStat.calcAVGDays();
         lspStat.calcAVGQuarter();
         lspStat.calcGoalPercent();
 
         return lspStat;
     }
 
     private void calcGoalPercent() {
         for (int month = 0; month < 12; month++) {
             Calendar date = new GregorianCalendar();
             date.set(Calendar.MONTH,month);
 
             int quarter = Dates.getQuarter(date);
             switch (quarter){
                 case 1:this.goalQuarter.put(1, this.avgQuarter.get(1) / this.goal.q1 * 100.0);
                     break;
                 case 2:this.goalQuarter.put(2,this.avgQuarter.get(2) / this.goal.q2 * 100.0);
                     break;
                 case 3:this.goalQuarter.put(3,this.avgQuarter.get(3) / this.goal.q3 * 100.0);
                     break;
                 case 4:this.goalQuarter.put(4,this.avgQuarter.get(4) / this.goal.q4 * 100.0);
                     break;
             }
         }
     }
 
     private void calcAVGQuarter() {
         this.avgQuarter.put(1,0.0);
         this.avgQuarter.put(2,0.0);
         this.avgQuarter.put(3,0.0);
         this.avgQuarter.put(4,0.0);
 
         for (int month = 0; month < 12; month++) {
             Calendar date = new GregorianCalendar();
             date.set(Calendar.MONTH,month);
 
             int quarter = Dates.getQuarter(date);
             switch (quarter){
                 case 1:this.avgQuarter.put(1, this.avgQuarter.get(1) + this.avgDay.get(month));
                     break;
                 case 2:this.avgQuarter.put(2,this.avgQuarter.get(2)+this.avgDay.get(month));
                     break;
                 case 3:this.avgQuarter.put(3,this.avgQuarter.get(3)+this.avgDay.get(month));
                     break;
                 case 4:this.avgQuarter.put(4,this.avgQuarter.get(4)+this.avgDay.get(month));
                     break;
             }
         }
 
         this.avgQuarter.put(1,this.avgQuarter.get(1)/3.0);
         this.avgQuarter.put(2,this.avgQuarter.get(2)/3.0);
         this.avgQuarter.put(3,this.avgQuarter.get(3)/3.0);
         this.avgQuarter.put(4,this.avgQuarter.get(4)/3.0);
     }
 
     private void calcNBDays(SaleInfo si) {
         if (this.nbDays.get(si.month) == null) {
             this.nbDays.put(si.month, 0);
         }
 
         this.nbDays.put(si.month, this.nbDays.get(si.month) + 1);
     }
 
     private void calcAVGDays() {
         for (int month = 0; month < 12; month++) {
             this.avgDay.put(month, 0.0);
             for (int day = 1; day < 32; day++) {
                 if (this.sales.get(day + "M" + month) != null) {
                     this.avgDay.put(month, this.avgDay.get(month) + this.sales.get(day + "M" + month));
                 }
             }
 
             if (this.avgDay.get(month) != 0) {
                 this.avgDay.put(month, this.avgDay.get(month) / this.nbDays.get(month));
                 this.totalAvgDaySum += this.avgDay.get(month);
             }
         }
     }
 
     private void calcTotalMonth(SaleInfo si) {
         if (this.totalMonth.get(si.month) == null) {
             this.totalMonth.put(si.month, 0.0);
         }
 
         this.totalMonth.put(si.month, this.totalMonth.get(si.month) + si.totalNet);
         this.totalMonthSum += si.totalNet;
     }
 
     private void calcTotalDaySum(SaleInfo si) {
         if (this.totalDaySum.get(si.day) == null) {
             this.totalDaySum.put(si.day, 0.0);
         }
 
         this.totalDaySum.put(si.day, this.totalDaySum.get(si.day) + si.totalNet);
     }
 
     private void calcSalesByDay(SaleInfo si) {
         if (this.sales.get(si.day + "M" + si.month) == null) {
             this.sales.put(si.day + "M" + si.month, 0.0);
         }
 
         this.sales.put(si.day + "M" + si.month,
                 this.sales.get(si.day + "M" + si.month) + si.totalNet);
     }
 }
