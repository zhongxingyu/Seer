 package ManagementClients;
 
 import RMI.AnalyticsServerInterface;
 import RMI.BillingServerInterface;
 import RMI.BillingServerSecure;
 import RMI.RMIRegistry;
 import RMI.RMIRegistryException;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.rmi.RemoteException;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.xml.DOMConfigurator;
 
 /**
  *
  * @author Dave
  */
 public class ManagementClient implements Runnable {
 
    
     private static Logger logger = Logger.getLogger(ManagementClient.class.getSimpleName());
     private RMIRegistry registry = null;
     private ManagementClientCallBackInterfaceImpl mccbi = null;
     private AnalyticsServerInterface analytic = null;
     private BillingServerInterface billing = null;
     private BillingServerSecure bss = null;
     private boolean init = true;
     private boolean analyticIsAvaible = false;
     private boolean billingIsAvaible = false;
 
     public ManagementClient(String propertyFile, String analyticBindingName,String billingBindingName) {
         try {
              DOMConfigurator.configure("./src/log4j.xml"); 
            
             /**
              * Get or start Registry(Naming Server)
              *
              */
             registry = new RMIRegistry(propertyFile);
 
             /**
              * ***AnalyticServer Part*******
              */
             try {
                 analytic = registry.getAnalyticsInterface(analyticBindingName);
                 mccbi = new ManagementClientCallBackInterfaceImpl();
                 mccbi.initializeManagementClient();
                 analyticIsAvaible = true;
             } catch (RMIRegistryException ex) {
                 this.logger.error("Error:Analytics Server not avaible.");
                 analyticIsAvaible = false;
             }
             /**
              * ***AnalyticServer Part*******
              */
             /**
              * ***BillingServer initialization Part*******
              */
             try {
                 billing = registry.getBillingInterface(billingBindingName);
                 billingIsAvaible = true;
             } catch (RMIRegistryException ex) {
                 this.logger.error("Error:Billing Server not avaible.");
                 billingIsAvaible = false;
             }
             /**
              * ***BillingServer Part*******
              */
         } catch (Exception ex) {
             this.logger.error("ManagementClient:Constructor:Exception:" + ex.getMessage());
             init = false;
         }
 
     }
 
     public void run() {
         try {
             logger.info("ManagementClientHandle started...");
             String line = null;
             // System.out.print("\n>");
 
 
             BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
             while (!Thread.currentThread().isInterrupted() && init) {
                 /**
                  * ***Analytics und Billing Eingaben****
                  */
                 System.out.print("\n>");
 
                 
                 while ((line = in.readLine()) != null) {
                         
                    try {    
                         
                         System.out.print("\n>");
                         
                         
                         if (line.contains("!subscribe") && this.analyticIsAvaible) {
                             
                             String[] s = line.split(" ");
                             if (s.length >= 2) {
                                 long sub_id = 0;
                                 int position1 = line.indexOf('\'', 0);
                                 int position2 = line.indexOf('\'', position1 + 1);
                                 if((position2 <= position1)||(position1<0)||(position2<0))
                                     throw new Exception(this.printUsageofCommand("subscribe"));
                                 String regex = line.substring(position1 + 1, position2);
                                 try {
                                     logger.debug("Send subscription request to AnalyticServer.");
                                     sub_id = analytic.subscribe(mccbi, regex);
                                     //regular output
                                     System.out.println("Created subscription with ID " + sub_id
                                             + " for events using filter"
                                             + " '"
                                             + regex
                                             + "' ");
                                     logger.debug("ManagementCallback Object was "
                                             +"initialized with ClientID "
                                             +mccbi.getLocalClientID()
                                             +" from AnalyticServer.");
                                 } catch (RemoteException ex) {
                                     System.out.print("Error:managmentClient:"
                                             + "run:subscribe:RemoteException:"
                                             + ex.getMessage());
                                 }
                             }
                             
                         }else if (line.contains("!unsubscribe") && this.analyticIsAvaible) {
                           
 
                             try {
                                 boolean b;
                                 String[] s = line.split(" ");
                                 
                                 long id =Long.parseLong(s[1]);
                                 logger.debug("Send request to unsubscribe ID "
                                         +id);
                                 b = analytic.unsubscribe(id);
                                 if (b) {
                                     System.out.println("subscription "
                                             + id
                                             + " terminated. ");
                                 } else {
                                     System.out.println("Error:unsubscribe id "
                                             + id
                                             + " was not successfull.");
                                 }
                             } catch (RemoteException ex) {
                                 System.out.println("Error:ManagementClient:"
                                         + "run:unsubscribe:RemoteException:"
                                         + ex.getMessage());
                             } catch (NumberFormatException e) {
                                 logger.error("Error:ManagementClient:"
                                         + "run:unsubscribe:NumberFormatException:"
                                         +e.getMessage());
                                 //System.out.print("ERROR:" + e.getMessage());
                             } catch (Exception e) {
                                 logger.error("Error:ManagementClient:"
                                         + "run:unsubscribe:"+e.getMessage());
                                 //System.out.print("ERROR:" + e.getMessage());
                             }
                         
 
                         } else if (line.contains("!print") && this.analyticIsAvaible) {
                             if (!mccbi.getMode()) {
                                 mccbi.printBuffer();
                             }
                         } else if (line.contains("!hide") && this.analyticIsAvaible) {
                             mccbi.setEventPrintMode(false);
 
                         } else if (line.contains("!auto") && this.analyticIsAvaible) {
                             mccbi.setEventPrintMode(true);
 
                         } else if (line.contains("!end")) {
                             //provisorisch
                             Thread.currentThread().interrupt();
                             break;
                         } 
                         /**
                          * Billing Eingaben****
                          */
                         else if (line.contains("!login") && this.billingIsAvaible) {
                            logger.info("INSIDE MANAGEMENT CLIENT - LOGIN METHOD");
                             String[] split = line.split(" ");
                             String login = split[1];
                             String pass = split[2];
                             if (billing.login(login, pass) != null) {
                                 bss = billing.login(login, pass);
                                logger.info("USER " + login + " was sucessfully logged in!");
                             } else {
                                logger.info("USER " + login + " not logged in! Wrong username or password.");
                             }
                         } else if (line.contains("!steps") && this.billingIsAvaible) {
                             System.out.println(bss.getPriceSteps().toString());
                         } else if (line.contains("!addStep") && this.billingIsAvaible) {
                             String[] split = line.split(" ");
                             double min_price = Double.parseDouble(split[1]);
                             double max_price = Double.parseDouble(split[2]);
                             double fee_fix = Double.parseDouble(split[3]);
                             double fee_var = Double.parseDouble(split[4]);
                             bss.createPriceStep(min_price, max_price, fee_fix, fee_var);
                             System.out.println("Price step with values " + min_price + "," + max_price + " was created successfully.");
                         } else if (line.contains("!removeStep") && this.billingIsAvaible) {
                             try {
                                 String[] split = line.split(" ");
                                 double min_price = Double.parseDouble(split[1]);
                                 double max_price = Double.parseDouble(split[2]);
                                 bss.deletePriceStep(min_price, max_price);
                                 System.out.println("Price step with values " + min_price + "," + max_price + " was deleted successfully.");
                             } catch (Exception e) {
                                logger.error("Price step could not be deleted!");
                                
                             }
                         } else if (line.contains("!bill") && this.billingIsAvaible) {
                             try {
                                
                                 String[] split = line.split(" ");
                                 String user = split[1];
                                 String report = bss.getBill(user).toString();
                                 System.out.println("Bill of User " + user + " is as follows: \n" + report);
                                 
                             } catch (NullPointerException e) {
                                logger.info("You must login first.");
                             } catch (Exception e){
                                logger.error("A bill could not be created." + e.getMessage());
                             }
                         } else if (line.contains("!logout") && this.billingIsAvaible) {
                             logger.debug("INSIDE MANAGEMENT CLIENT - LOGOUT METHOD");
                             bss = null;
                            logger.info("USER was sucessfully logged out!");
                         }
                     } catch (Exception ex) {
                      System.out.println("ErrorThere was an error. Check if you are logged in.");
                      logger.error("Exception:"+ex.getMessage());
                     }
                 }// while ((line = in.readLine()) != null) 
 
             }// while (!Thread.currentThread().isInterrupted() && init)
           
 
         } catch (Exception ex) {
           
             logger.error("ERROR:" + ex.getMessage());
         }
     }
 
   
     
     public String printUsageofCommand(String command)
     {
         if(command.contains("subscribe"))
             return "<usage subscribe>:!subscribe '<Regex>'";
         else if(command.contains("unsubcribe"))
             return "<usage unsubscribe>:!unsubcribe <Subscription ID>";
         else if(command.contains("print"))
             return "<usage print>:!print";
         else if(command.contains("auto"))
             return "<usage print>:!auto";
         else if(command.contains("hide"))
             return "<usage print>:!hide";
         /*billing commands*/
         else
             return "<unknown command>";
         
     }
     
 }
