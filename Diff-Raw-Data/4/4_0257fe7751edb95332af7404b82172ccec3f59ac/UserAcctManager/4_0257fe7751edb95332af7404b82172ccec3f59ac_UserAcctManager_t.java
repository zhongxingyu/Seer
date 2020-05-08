 package net.anei.cadpage.donation;
 
 import java.security.MessageDigest;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import net.anei.cadpage.HttpService;
 import net.anei.cadpage.HttpService.HttpRequest;
 import net.anei.cadpage.ManagePreferences;
 import net.anei.cadpage.R;
 import net.anei.cadpage.billing.BillingManager;
 import net.anei.cadpage.donation.UserAcctManager;
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.annotation.TargetApi;
 import android.content.Context;
 import android.net.Uri;
 import android.telephony.TelephonyManager;
 
 public abstract class UserAcctManager {
   
   // Authorization recheck interval (30 days in msecs)
   private static final long AUTH_CHECK_INTERVAL = (long)30*24*60*60*1000;
   
   public abstract void setContext(Context context);
   
   public abstract void reloadStatus(Context context);
 
   /**
    * @return true if account management is supported by this version of Android
    */
   public abstract boolean isAcctSupport();
 
   /**
    * @return identified user account name
    */
   public abstract String getUser();
   
   /**
    * @return device phone number
    */
   public abstract String getPhoneNumber();
   
   /**
    * Append account information to support message under construction
    * @param sb String builder holding message being constructed
    */
   public void addAccountInfo(StringBuilder sb) {
     sb.append("\n\nUser:");
     sb.append(getUser());
     sb.append('\n');
     sb.append("Phone:");
     sb.append(getPhoneNumber());
     int overpaidDays = DonationManager.instance().getOverpaidDays();
     if (overpaidDays > 0) {
       sb.append("\nOverpaid:");
       sb.append(overpaidDays);
     }
   }
 
   // This really is used, but we have to invoke the class at runtime because it
   // references API calls that do not exist in Android 1.6
   @TargetApi(5)
   public static class RealUserAcctManager extends UserAcctManager {
     
     private static final DateFormat DATE_FMT = new SimpleDateFormat("MM/dd/yyyy");
     
     Context context;
     private String userEmail = null;
     private String phoneNumber = null;
     
     @Override
     public void setContext(Context context) {
       this.context = context;
       TelephonyManager tMgr =(TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
       phoneNumber = tMgr.getLine1Number();
       if (phoneNumber == null) phoneNumber = tMgr.getVoiceMailNumber();
       phoneNumber = cleanName(phoneNumber);
       
       Account[] accounts = AccountManager.get(context).getAccountsByType("com.google");
       if (accounts.length > 0) userEmail = cleanName(accounts[0].name);
       
       // See if it is time to perform an automatic reload
       // If this is a lifetime user, don't bother
       if (!ManagePreferences.freeRider()) {
         
         // if not, get the current time and last authorization check time
         long lastTime = ManagePreferences.authLastCheckTime();
         long curTime = System.currentTimeMillis();
         
         // If the last check time is zero, we check to see if the initialized flag has been
         // set.  If not, this is the first time the user has launched Cadpage and we should do
         // an immediate payment status reload to see if they have been a subscriber on another phone.
         if (lastTime <= 0 && ManagePreferences.initialized()) {
           
           // Likewise, if Cadpage has been initialized but the purchase and install dates are the same
           // the purchase date is not trustworthy and we need to reload the payment status immediately
           if (ManagePreferences.paidYear() == 0 ||
               !ManagePreferences.purchaseDate().equals(ManagePreferences.installDate())) {
               
             // Otherwise, we will try to spread out the load by timing the next authorization check 
             // at a random time within the next 30 days
             
               lastTime = curTime - (long)(Math.random()*AUTH_CHECK_INTERVAL);
               ManagePreferences.setAuthLastCheckTime(lastTime);
           }
         }
         
         // Having done all of that, if the different between the current time and the
         // latched checked time exceeds the 30 day intervals, perform an automatic reload
         if (curTime - lastTime > AUTH_CHECK_INTERVAL) {
           
           // Reset Android purchase information
           ManagePreferences.setInitBilling(false);
           BillingManager.instance().initialize(context);
           
           // Request status reload from server
           UserAcctManager.instance().reloadStatus(context);
         }
       }
     }
 
     /**
      * Clean extraneous stuff from user name / phone #
      * @param name
      * @return
      */
     private String cleanName(String name) {
       int pt = name.indexOf('<');
       if (pt >= 0) name = name.substring(0,pt);
       name = name.replace("-",  "");
       if (name.startsWith(".")) name = name.substring(1);
       if (name.endsWith(".")) name = name.substring(0,name.length()-1);
       return name;
     }
     
     @Override
     public void reloadStatus(Context context) {
       
       // Build query with all of the possible account and phone ID's
       Uri.Builder builder = Uri.parse(context.getString(R.string.donate_server_url)).buildUpon();
       Account[] accounts = AccountManager.get(context).getAccountsByType("com.google");
       for (Account acct : accounts) {
         builder.appendQueryParameter("id", cleanName(acct.name));
       }
       builder.appendQueryParameter("id", phoneNumber);
       
       // Send it to the server and see what comes back
       HttpService.addHttpRequest(context, new HttpRequest(builder.build(), true){
 
         @Override
         public void processBody(String body) {
           for (String line : body.split("<br>")) {
             
             String flds[] = line.split(",");
             if (flds.length < 2) continue;
             
             // Evaluate the status field.  Value of LIFE translates to year 9999
             int year = -1;
             String stat = flds[1].trim();
            if (stat.toUpperCase().equals("LIFE")) {
               year = 9999;
             } else {
               try {
                 year = Integer.parseInt(stat);
               } catch (NumberFormatException ex) {}
               if (year < 2011 && year > 2050) year = -1;
             }
             
             // get the current status year.  If new status is better, update
             // the user status
             int paidYear = ManagePreferences.freeRider() ? 9999 : ManagePreferences.paidYear();
             if (year > paidYear) {
               if (year == 9999) {
                 ManagePreferences.setFreeRider(true);
               } else {
                 ManagePreferences.setPaidYear(year, true);
               }
             }
             
             // If status is less that current, ignore everything else
             if (year < paidYear) continue;
             
             // Otherwise evaluate and accept purchase date
             if (flds.length <= 2) continue;
             if (year >= paidYear) {
               Date purchaseDate = null;
               try {
                 purchaseDate = DATE_FMT.parse(flds[2].trim());
               } catch (ParseException ex) {}
               if (purchaseDate != null) {
                 ManagePreferences.setPurchaseDate(purchaseDate);
               }
             }
             
             // If sponsor code is "FREE", set the free subscriber flag
             String sponsor = null;
             boolean free = false;
             if (flds.length > 3) {
               sponsor = flds[3].trim();
               if (sponsor.equals("FREE")) { 
                 free = true;
                 sponsor = null;
               } else if (sponsor.length() == 0) sponsor = null;
             }
             ManagePreferences.setFreeSub(free);
             ManagePreferences.setSponsor(sponsor);
           }
           
           // Set the authorization last checked date
           ManagePreferences.setAuthLastCheckTime();
         }
       });
     }
     
     
     /**
      * @return true if account management is supported by this version of Android
      */
     @Override
     public boolean isAcctSupport() {
       return true;
     }
   
     /**
      * @return identified user account name
      */
     @Override
     public String getUser() {
       return userEmail;
     }
 
     @Override
     public String getPhoneNumber() {
       return phoneNumber;
     }
   }
 
   private static class DummyUserAcctManager extends UserAcctManager {
     
     public void setContext(Context context) {}
     
     /**
      * @return true if account management is supported by this version of Android
      */
     @Override
     public boolean isAcctSupport() {
       return false;
     }
 
     @Override
     public void reloadStatus(Context context) {
     }
   
     /**
      * @return identified user account name
      */
     @Override
     public String getUser() {
       return null;
     }
 
     @Override
     public String getPhoneNumber() {
       return null;
     }
   }
   
   /**
    * Convert user account name to MD5 has
    * @param input user account name
    * @return MD5 hash
    */
   public static String calcHash(String input) {
     try {
       MessageDigest md = MessageDigest.getInstance("MD5");
       md.update(input.toLowerCase().getBytes("UTF8"));
       byte[] digest = md.digest();
       StringBuilder sb = new StringBuilder();
       for (byte b : digest) {
         sb.append(hexDigit(b>>4));
         sb.append(hexDigit(b));
       }
       return sb.toString();
     } catch (Exception ex) {
       throw new RuntimeException(ex.getMessage(), ex);
     }
   }
   
   private static char hexDigit(int i) {
     i &= 0xF;
     return (char)(i < 10 ? '0'+i : 'a'+(i-10));
   }
  
   
   private static UserAcctManager instance = null;
   
   public static void setup(Context context) {
     try {
       String className = UserAcctManager.class.getName() + "$RealUserAcctManager";
       instance = (UserAcctManager)Class.forName(className).newInstance();
       instance.setContext(context);
     } catch (Exception ex) {
       instance = new DummyUserAcctManager();
     }
   }
   
   public static UserAcctManager instance() {
     return instance;
   }
 }
