 package original;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.HashMap;
 
 import authority.G2Constants;
 
 import messaging.AuthenticationRequest;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Group2
  * Date: 5/21/13
  * Time: 9:48 PM
  * To change this template use File | Settings | File Templates.
  */
 
 
 public class AccountManager {
 
     private final short MAX_FAILED_ATTEMPTS = Short.parseShort(
     		PropertiesFile.getProperty(PropertiesFile.LOGIN_ATTEMPTS, "3"));
     private final long LOCKOUT_DURATION = Integer.parseInt(
     		PropertiesFile.getProperty(PropertiesFile.LOCKOUT_DURATION, "60")) * G2Constants.SEC_TO_MSEC;
     private static int accountNumber = 16849327;
     private static int initialPin = 1095;
     private static HashMap<Integer, Account> acctMap = new HashMap<Integer, Account>();
 
     public void createAccount(String customerName, double initialBalance) throws IOException {
 
         try {
             // Create the account, including an ATM card internal to constructor.
             Account newAcct = new Account(customerName, accountNumber, initialPin, initialBalance);
             
         	// Add Accounts to bank
         	acctMap.put(newAcct.getID(), newAcct);
 
             createAtmCardFile(newAcct.getAtmCard());
 
             // Manipulate account variables
             accountNumber++;
             initialPin += 2578;
 
         } catch (IOException e) {
         	if (PropertiesFile.isDebugMode()) {
                 e.printStackTrace();
         	}
         }
 
     } // end createAccount
 
     public void retrieveAllAccounts() throws IOException {
     	acctMap = (HashMap<Integer, Account>)Disk.load("accountsFile");
     	if (PropertiesFile.isDebugMode()) {
         	System.out.println(acctMap);
         }
     } // end retrieveAllAccounts
     
 
     public void storeAllAccounts() throws IOException {
         Disk.save((Serializable) acctMap, "accountsFile");
         if (PropertiesFile.isDebugMode()) {
         	System.out.println(acctMap);
         }
     } // end storeAllAccounts
 
     public void createAtmCardFile(AtmCardClass atmCard) throws IOException {
         Disk.save(atmCard, atmCard.getName() + ".card");
     } // end createAtmCardFile
 
     public AtmCardClass retrieveAtmCard(String name) throws IOException {
         return (AtmCardClass) Disk.load(name + ".card");
     } // end retrieveCard
 
     public boolean authenticateRequest(AuthenticationRequest msg) {
 
     	boolean authorized = false;
     	
         Account currAcct = acctMap.get(msg.getAccountNumber());
 
         if (currAcct.getNextValidLoginTime() > System.currentTimeMillis()) {
 
             System.out.println("\nRemote command processed.  This account is currently locked out.  Try again later.");
 
             // Fail authentication/validation and return immediately
             authorized = false;
 
         } // end if getNextValidationTime
        else if (currAcct.getCurrentNumOfFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
 
             System.out.println("\nRemote command processed.  MAX_FAILED_LOGIN_ATTEMPTS.");
 
             // Set the valid login time into the future to avoid repetitive false login attempts
             currAcct.setNextValidLoginTime(LOCKOUT_DURATION + System.currentTimeMillis());
 
             // Now that a lockout time has been set, reset the number of failed login attempts
             currAcct.resetCurrentNumOfFailedLoginAttempts();
 
             authorized = false;
         } // end if now MAX_FAILED_ATTEMPTS
 
         // Check the pin
        else if (msg.getPin() != currAcct.getPin()) {
             System.out.println("\nRemote command processed. PIN didn't match.");
             currAcct.incrementCurrentNumOfFailedLoginAttempts();
 
             authorized = false;
 
         } // end if entered pin != pin
         else { // The pin entered must have been good
             System.out.println("\nRemote command processed.  AUTHENTICATED.");
             currAcct.resetCurrentNumOfFailedLoginAttempts();
             authorized = true;
         } // end else -> entered pin is correct
         
         try {
 			this.storeAllAccounts();
 		} catch (IOException e) {
 			if (PropertiesFile.isDebugMode()) {
 				e.printStackTrace();
 			}
 		}
         return authorized;
     } // end authenticateRequest()
     
     public static int lookAcctByName(String name) {
 		int acctNumber = 0;
 		
     	for (Integer acctNo: acctMap.keySet()) {
     		Account user = acctMap.get(acctNo);
     		if (user.getName().equalsIgnoreCase(name)){
     			acctNumber = user.getID();
     		}
     	}
     	return acctNumber;
 	}
     
     public static void processDep(int acctNo, double amt)
     {
     	Account acct = acctMap.get(acctNo);
     	acct.setBalance(acct.getBal() + amt);
     	acctMap.put(acctNo, acct);
     }
 
 
     public static boolean processWith(int acctNo,double amt)
     {
     	Account acct = acctMap.get(acctNo);
     	
     	//check if $$$ in the bank
     	if (acct.getBal() >= amt) {
     		acct.setBalance(acct.getBal() - amt);
     		acctMap.put(acctNo, acct);
         	return true;
     	} else {
     		return false;
     	}
     	
         
     }
 
 
     public static double processBal(int acctNo)
     {
     	if (acctMap.containsKey(acctNo)){
     		return acctMap.get(acctNo).getBal();
     	}
     	
 		return 0;
     }
 
 
     public static boolean validate(int acct_no, int pin_no)
     {
     	Account acct = acctMap.get(acct_no);
         return acct.getPin() == pin_no;
     }
 
 	public static boolean isAcctNumValid(int acctNum) {
 		return acctMap.containsKey(acctNum);
 	}
     
     
 } // class AccountManager
