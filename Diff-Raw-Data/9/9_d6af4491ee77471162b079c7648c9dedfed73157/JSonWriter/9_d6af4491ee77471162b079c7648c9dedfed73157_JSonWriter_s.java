 package reporting;
 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 
 /**
  * The JSonWriter
  * @author dbhage
  */
 public class JSonWriter
 {
     private ArrayList<Transaction> transactionList = null;
     private String email;
     
     private String JSONOutputString = "";
     
     /**
      * Constructor
      * @param tl - a transaction list and the destination email
      */
     public JSonWriter(ArrayList<Transaction> tl, String em)
     {
         email = em;
         transactionList = tl;
     }
 
     /**
      * Makes a string from all transactions
      * @return transactions as a string in JSON format
      */
     private String transactionsJSONString()
     {
         StringBuilder transactionsString = new StringBuilder();
         
         transactionsString.append("[");
         
         for (Transaction t: transactionList)
         {   
             transactionsString.append(t.toJSON());
             transactionsString.append(", ");
         }        
         
         // replace last comma with [ 
         transactionsString.setCharAt(transactionsString.length() -2 , ']');
         
        System.out.println("The last character is now " + transactionsString.charAt(transactionsString.length()-2));
        
         return transactionsString.toString();
     }
     
     private String transactionsJSONString(StringBuilder transactionsString)
     {        
         transactionsString.append("[");
         Transaction t = transactionList.get(0);
         /*
         for (Transaction t: transactionList)
         {   
             transactionsString.append(t.toJSON());
             transactionsString.append(", ");
         }*/
         
         transactionsString.append(t.toJSON());
         transactionsString.append(", ");
         
         // replace last comma with [ 
         transactionsString.setCharAt(transactionsString.length() -1 , ']');
         
         return transactionsString.toString();
     }
     
     /**
      * Generate the JSON file
      */
     public void generateOutput()
     {
 	
         String transactions = transactionsJSONString(new StringBuilder());
         String newString = "{";
         newString += "\"team\": \"Team007\",";
         newString += "\"destination\": \"" + email + "\",";
         newString += "\"transactions\": " + transactions + "}";
         JSONOutputString = newString;
         
         try
         {
             FileWriter file = new FileWriter("codejam.json");
             file.write(newString);
             file.flush();
             file.close();
         } 
         catch (IOException e)
         {
             System.out.println("Error while writing JSON output to file codejam.json");
         }
     }
     
     public String getOutputString()
     {
     	return JSONOutputString;
     }
 }
