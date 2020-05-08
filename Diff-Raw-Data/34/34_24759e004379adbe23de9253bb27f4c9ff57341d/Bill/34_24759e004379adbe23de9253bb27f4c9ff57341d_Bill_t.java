 package BillingServer;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.apache.log4j.Logger;
 
 /**
  *
  * @author Dave
  */
 public class Bill implements Serializable {
 
     private ConcurrentHashMap<String, User> users;
     String billReport = "";
     double fee_fixed;
     double fee_variable;
     double fee_total;
 
     public Bill() {
         users = new ConcurrentHashMap<String, User>();
     }
 
     public void createBill(String user, PriceSteps ps) {
         this.billReport = "";
         if (!this.isUserAvailable(user)) {
             System.out.println("Error:User " + user + " is not available.");
             this.billReport = "Error:User " + user + " is not available.";
         } else {
             double strike_price;
             User u = users.get(user);
             for (Auction a : u.getAuctionList()) {
                 strike_price = a.getPrice();
                 if (calculateFee(strike_price, ps)) {
                     fee_total = fee_fixed + fee_variable;
                     billReport = billReport + "StrikePrice: " + strike_price + " FeeFixed: " + fee_fixed + " FeeVariable: " + fee_variable + " FeeTotal: " + fee_total + "\n";
                 }
             }
         }
     }
 
     public ConcurrentHashMap<String, User> getUsers() {
         return users;
     }
 
     public synchronized void putAuctiontoUser(Auction a, String name) {
         User u = this.users.get(name);
         u.putAuction(a);
     }
 
     public boolean adduser(User u) {
         this.users.put(u.getName(), u);
         if (!this.users.containsKey(u.getName())) {
             return false;
         } else {
             return true;
         }
 
 
     }
 
     public boolean isUserAvailable(String name) {
         return users.containsKey(name);
     }
 
     private boolean calculateFee(double strike_price, PriceSteps ps) {
         //PriceSteps ps_ = ps;
         PriceStep last = null;
         for (PriceStep pS : ps.priceSteps) {
             if ((strike_price >= pS.getMin_value() && strike_price <= pS.getMax_value()) || pS.getMax_value() == 0) {
                 fee_fixed = pS.getFee_fixed();
                 fee_variable = pS.getFee_variable() * strike_price;
                 System.out.println("Fee was set successfully.");
                 last = pS;
                 return true;
             }
         }
         System.out.println("Strike Price is not inside Price Configuration.");
         return false;
     }
 
     public String toString() {
         return billReport;
     }
 }
