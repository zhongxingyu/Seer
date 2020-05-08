 package edu.mit.moneyManager.listUtils;
 
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 /**
  * Model for each expense
  *
  */
 public class ExpenseItemEntry {
     private GregorianCalendar date;
     private double amount;
     private String category;
     
     public ExpenseItemEntry(GregorianCalendar date, double amount, String category) {
         this.date = date;
         this.amount = amount;
         this.category = category;
     }
     
     //getters
     public String getDateString() {
 //        return date.toString();
         //TODO: formate date
         return "4/15/2012";
     }
     
     public String getAmount() {
        return String.format("%.02f", new Double(amount));
     }
     
     public String getCategory() {
         return new String(category);
     }
     
     //setters
     public void setDate(GregorianCalendar date) {
         this.date = date;
     }
     
     public void setAmount(int amount) {
         this.amount = amount;
     }
     
     public void setCategory(String category) {
         this.category = category;
     }
 }
