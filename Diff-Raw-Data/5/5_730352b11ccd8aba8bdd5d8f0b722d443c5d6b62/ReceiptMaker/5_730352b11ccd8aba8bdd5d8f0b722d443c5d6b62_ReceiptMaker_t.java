 package salestax;
 
 import java.io.Console;
 import java.util.Arrays;
 import java.io.IOException;
 import java.util.LinkedList;
 
 public class ReceiptMaker {
     private static String line;
     private static String[] tokenArray = {"my", "name", "is", "mark"};
     private static int quantity = 0;
     private static LinkedList<Product> products = new LinkedList<Product>();
     private static double totalTax = 0.0;
     private static double totalAmount = 0.0;
     private static boolean imported = false;
     private static boolean exempt = false;
     private static String name = "";
     private static double price = 0.0;
 
     static boolean parseExempt(String[] inputLine) {
         boolean isExempt = false;
         for (String item : inputLine) {
             if (item.equalsIgnoreCase("pills") || item.equalsIgnoreCase("book") ||
                 item.equalsIgnoreCase("books") || item.equalsIgnoreCase("chocolate") ||
                 item.equalsIgnoreCase("chocolates"))
                 { isExempt = true; } 
 	}
         return isExempt;
     }
 
     static double parsePrice(String[] inputLine) {
         double inputPrice = 0.0;
         int inputSize = inputLine.length;
         try {
             inputPrice = Double.parseDouble(inputLine[inputSize-1]);
 	} catch (NumberFormatException e) {
             System.out.println(e.getMessage());
             System.exit(1);
 	}
         return inputPrice;
     }
 
     static boolean parseImported(String[] inputLine) {
         boolean isImported = false;
         for (String item : inputLine) {
             if (item.equalsIgnoreCase("imported"))
                 isImported = true;
 	}
 	return isImported;
     }
 
     static String parseName(String[] inputLine) {
         String inputName = "";
         String[] input = inputLine;
         for(int i = 1; i <= input.length - 3; i++) {
             inputName += inputLine[i];
         }
         return inputName;
     }
 
     static int parseQuantity(String[] inputLine) {
        int parseInteger = 0;
        try { 
             parseInteger = Integer.parseInt(inputLine[0]);
 	} catch (NumberFormatException e) {
             System.out.println(e.getMessage());
             System.exit(1);
 	}
         return parseInteger;
     }
 
     /*************************************************************
      *
      * @returns the receipt total for all products in a receipt.
      *
      ************************************************************/
     public double receiptTotal() {
         double runningTotal = 0;
         for (Product item : products) {
            runningTotal += item.quantity() * item.price();
 	}
         return runningTotal;
     }
 
     /*************************************************************
      *
      * @returns the total sales tax for all products in a receipt.
      *
      ************************************************************/
     public double totalSalesTax() {
         double runningTotal = 0;
         for (Product item : products) {
            runningTotal += item.quantity() * item.tax();
 	}
         return runningTotal;
     }
 
     public static void productFlush() {
         quantity = 0;
         imported = false;
         exempt = false;
         name = "";
         price = 0.0;
     }
 
     public static void main(String[] args) throws IOException{
 
     Console c = System.console();
     if (c == null) {
         System.err.println("No console.");
         System.exit(1);
     }
 
     System.out.println("How many items on this order?");
     String numberOfItems = c.readLine();
     Integer parsedNumber = 0;
     try {
         parsedNumber = Integer.parseInt(numberOfItems);
     } catch (NumberFormatException e) {
         System.out.println(e.getMessage());
         System.exit(1);
     }
 
     for(int j = 1; j <= parsedNumber; j++) {
         productFlush();
         line = c.readLine();
         tokenArray = line.split(" ");
 
         if (parseExempt(tokenArray)) {
 	    products.add(new ProductExempt(parseQuantity(tokenArray), parseImported(tokenArray), parseName(tokenArray), parsePrice(tokenArray)));
 	} else {
 	    products.add(new ProductNonExempt(parseQuantity(tokenArray), parseImported(tokenArray), parseName(tokenArray), parsePrice(tokenArray)));
 	}
 
     /*
      * Loop through tokenArray, in order to create Product.
      */
     for (int i = 0; i < tokenArray.length; i++) {
 
         /************************************************
         * Parse quantity, if at first index.
         ************************************************/
         if (i == 0) {
             try {
                 quantity = Integer.parseInt(tokenArray[0]);
             } catch (java.lang.NumberFormatException e) {
                 System.err.println("NumberFormatException: string does not contain parsable integer");
                 System.exit(1);
             }
         }
 
 
 	/************************************************
         * Parse price, if at last index.
         * Add Product to products list, if at last index.
         ************************************************/
         else if (i == tokenArray.length-1) {
             try {
                 price = Double.parseDouble(tokenArray[i]);
             } catch (java.lang.NumberFormatException e) {
                 System.err.println("NumberFormatException: String does not contain parsable double");
                 System.exit(1);
             }
         }
 
 	/*************************************************
         * Set variables exempt and imported to true, if token matches specified string.
         * Concatenate string to name variable.
         * Must include conditions for books, food, and medical products.
         *************************************************/
         else {
             if (tokenArray[i].equalsIgnoreCase("imported")) {
                 imported = true;
                 name = name + " " + tokenArray[i];
             } else if ((tokenArray[i].equalsIgnoreCase("pills") ||
                         tokenArray[i].equalsIgnoreCase("book") ||
                         tokenArray[i].equalsIgnoreCase("chocolate") ||
                         tokenArray[i].equalsIgnoreCase("chocolates"))) {
                 exempt = true;
                 name = name + " " + tokenArray[i];
             } else if (tokenArray[i].equalsIgnoreCase("at")) {
                 name = name + "";
             } else {
                 name = name + " " + tokenArray[i];
             }
         }
 
     } // end loop on tokenArray
 
 	}
 
     System.out.println("OUTPUT:");
         for (Product item : products) {
             System.out.println(item.toString());
             totalTax += item.tax();
             totalAmount += item.quantity() * item.price();
         }
 
         System.out.println("Sales Taxes: " + totalTax);
         System.out.println("Total: " + totalAmount);
     }
 }
