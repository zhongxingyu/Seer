 package version1;
 /**
  *
  * @author Nathaniel
  */
 public class Startup {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         //Add multiple seasonal and Quantity discounts
         CashRegister cashRegister = new CashRegister();
         
         //Customer ID's: CBAS1234, JCKA8604, PREM2503
         cashRegister.startSale("CBAS1234");
         
         //Product ID's: ABC123, RAD456, NDS1, SHO83
         cashRegister.inputProduct("ABC123");
        cashRegister.inputProduct("NDS1");
        cashRegister.inputProduct("ABC123");
        cashRegister.inputProduct("RAD456");
         cashRegister.inputProduct("ABC123");
         cashRegister.inputProduct("RAD456");
         cashRegister.inputProduct("SHO83");
        cashRegister.inputProduct("ABC123");
         
         //output receipt
         cashRegister.finalizeSale();
     }
 }
