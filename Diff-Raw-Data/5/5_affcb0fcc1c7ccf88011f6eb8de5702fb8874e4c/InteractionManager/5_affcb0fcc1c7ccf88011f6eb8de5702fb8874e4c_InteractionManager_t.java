 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package stockexclient;
 
 import java.rmi.RemoteException;
 import java.util.List;
 import java.util.Scanner;
 import stockEx.Client;
 import stockEx.IAuthentication;
 import stockEx.IStockQuery;
 import stockEx.Stock;
 
 /**
  *
  * @author Kuntal
  */
 public class InteractionManager
 {
 
     private Scanner scanIn;
     private IStockQuery stockQuery;
     private IAuthentication authentication;
     private Client client;
     private boolean isAdmin;
 
     private void printloginMessage()
     {
         System.out.println("Please log in before you send the request.");
     }
 
     private void printMessage(String message)
     {
         System.out.println(message);
     }
 
     /**
      *
      * @return client type
      */
     private boolean isLoggedIn()
     {
         return client != null;
     }
 
     /**
      *
      * @param commandName
      */
     private void printWarning(String commandName)
     {
         System.out.print("Invalid parameters or Invalid command '" + commandName);
         System.out.println("'. Use help for syntax.");
     }
 
     /**
      *
      * @param stockQuery
      * @param auth
      * @param isAdmin
      */
     public InteractionManager(IStockQuery stockQuery, IAuthentication auth, boolean isAdmin)
     {
         this.isAdmin = isAdmin;
         this.stockQuery = stockQuery;
         this.authentication = auth;
         System.out.println("------------------------------------------------------");
         System.out.println("|             Welcome to dypen Stock Exchange        |");
         System.out.println("------------------------------------------------------");
         System.out.println("");
     }
 
     /**
      *
      * @throws RemoteException
      */
     public void init() throws RemoteException
     {
 
         String command;
         int quantity = 0;
         String[] commandString;
         boolean isExit = false;
 
         do
         {
             System.out.println(isLoggedIn() ? client.getUsername() + " >" : " >");
             command = getInput();
             commandString = command.split(" ");
             switch (commandString[0])
             {
                 case "query":
                     if (isLoggedIn())
                     {
                         if (commandString.length == 2)
                         {
                             try
                             {
                                 System.out.println(stockQuery.query(client, commandString[1]));
                             }
                             catch (Exception ex)
                             {
 
                                 printMessage(ex.getMessage());
                             }
                         }
                         else
                         {
                             printWarning(commandString[0]);
                         }
                     }
                     else
                     {
                         printloginMessage();
                     }
                     break;
 
                 case "list":
                     if (isLoggedIn())
                     {
                         if (commandString.length == 1 && !client.isAdmin())
                         {
 
                             List<Stock> list = stockQuery.list(client);
                             if (list.size() > 0)
                             {
                                 for (Stock stock : list)
                                 {
                                     System.out.println(stock);
                                 }
                             }
                             else
                             {
                                 printMessage("No records found.");
                             }
 
 
                         }
                         else
                         {
                             printWarning(commandString[0]);
                         }
                     }
                     else
                     {
                         printloginMessage();
                     }
 
                     break;
 
                 case "buy":
                     if (isLoggedIn())
                     {
                         if (commandString.length == 3 && !client.isAdmin())
                         {
 
                             try
                             {
                                 quantity = getInteger(commandString[2]);
                                 if (quantity > 0)
                                 {
                                     client = stockQuery.buy(client, commandString[1], quantity);
                                     System.out.println(Integer.toString(quantity) + " " + commandString[1] + " bought.");
                                     System.out.println(" Your current balance is $" + client.getBalance() + " .");
                                 }
                                 else
                                 {
                                     System.out.println("Enter quantity greater than zero.");
                                 }
                             }
                             catch (NumberFormatException ne)
                             {
                                 printWarning(commandString[0]);
                             }
                             catch (Exception ex)
                             {
                                 printMessage(ex.getMessage());
                             }
 
                         }
                         else
                         {
                             printWarning(commandString[0]);
                         }
                     }
                     else
                     {
                         printloginMessage();
                     }
 
                     break;
 
                 case "sell":
                     if (isLoggedIn())
                     {
                         if (commandString.length == 3 && !client.isAdmin())
                         {
 
                             try
                             {
                                 quantity = getInteger(commandString[2]);
                                 if (quantity > 0)
                                 {
                                     stockQuery.sell(client, commandString[1], quantity);
                                     System.out.println(Integer.toString(quantity) + " " + commandString[1] + " sold.");
                                     System.out.println(" Your current balance is $" + client.getBalance() + " .");
                                 }
                                 else
                                 {
                                     System.out.println("Enter quantity great than zero.");
                                 }
                             }
                             catch (NumberFormatException ne)
                             {
                                 printWarning(commandString[0]);
                             }
                             catch (Exception ex)
                             {
                                 printMessage(ex.getMessage());
                             }
                         }
                         else
                         {
                             printWarning(commandString[0]);
                         }
                     }
                     else
                     {
                         printloginMessage();
                     }
 
                     break;
 
                 case "update":
                     if (isLoggedIn())
                     {
                         if (commandString.length == 3 && client.isAdmin())
                         {
                             try
                             {
                                 double price = getInputAmount(commandString[2]);
                                 if (price > 0)
                                 {
                                     stockQuery.update(client, commandString[1], price);
                                     System.out.println("Price of " + commandString[1] + " updated to " + price + " .");
                                 }
                                 else
                                 {
                                     System.out.println("Enter price greater than zero");
                                 }
                             }
                             catch (NumberFormatException ne)
                             {
                                 printWarning(commandString[0]);
                             }
                             catch (Exception ex)
                             {
                                 printMessage(ex.getMessage());
                             }
                         }
                         else
                         {
                             printWarning(commandString[0]);
                         }
                     }
                     else
                     {
                         printloginMessage();
                     }
 
                     break;
                 case "user":
                     if (commandString.length == 2)
                     {
                         client = authentication.init(commandString[1], isAdmin);
                        if (!client.isAdmin())
                        {
                            printMessage("Your Balance is :" + client.getBalance());
                        }
                     }
                     else
                     {
                         printWarning(commandString[0]);
                     }
                     break;
                 case "help":
                     if (commandString.length == 1)
                     {
                         printPrompt();
                     }
                     break;
                 case "quit":
                     client = null;
                     isExit = true;
                     break;
                 default:
                     printWarning(commandString[0]);
 
                     break;
             }
 
 
         }
         while (!isExit);
     }
 
     public void printPrompt()
     {
         System.out.println("-----------------------------------------------");
         System.out.println("|                   COMMANDS                   |");
         System.out.println("-----------------------------------------------");
         System.out.println("|               user <user name>               |");
         System.out.println("| Eg.           user johnsmith                 |");
 
 
         System.out.println("|                                              |");
         if (isLoggedIn())
         {
             System.out.println("|              query <ticker name>             |");
             System.out.println("| Eg.              query goog                  |");
             System.out.println("|                                              |");
             if (!client.isAdmin())
             {
                 System.out.println("|         buy  <ticker name> <quantity>        |");
                 System.out.println("| Eg.             buy  goog 10                 |");
                 System.out.println("|                                              |");
                 System.out.println("|         sell <ticker name> <quantity>        |");
                 System.out.println("| Eg.             sell  goog 10                |");
                 System.out.println("|                                              |");
                 System.out.println("|                    list                      |");
             }
             else
             {
                 System.out.println("|         update  <ticker name> <price>       |");
                 System.out.println("| Eg.          update  goog 999.99            |");
             }
             System.out.println("|                                              |");
             System.out.println("|                    quit                      |");
 
         }
         System.out.println("-----------------------------------------------");
 
     }
 
     /**
      *
      * @return command string
      */
     private String getInput()
     {
         scanIn = new Scanner(System.in);
         String input = scanIn.nextLine().trim().toLowerCase();
         return input;
     }
 
     /**
      *
      * @param bal <p/>
      * @return balance in double format
      * <p/>
      * @throws NumberFormatException
      */
     private double getInputAmount(String bal) throws NumberFormatException
     {
 
         //check if a  valid decimal
         if (!bal.matches("^\\d+$|^[.]?\\d{1,2}$|^\\d+[.]?\\d{1,2}$"))
         {
             throw new NumberFormatException("Invalid entry");
         }
         return Double.valueOf(bal);
     }
 
     /**
      *
      * @param strInt <p/>
      * @return string in integer format
      * <p/>
      * @throws NumberFormatException
      */
     private int getInteger(String strInt) throws NumberFormatException
     {
 
         //check if a  valid decimal
         if (!strInt.matches("^[0-9]+$"))
         {
             throw new NumberFormatException("Invalid entry");
         }
         return Integer.valueOf(strInt);
     }
 }
