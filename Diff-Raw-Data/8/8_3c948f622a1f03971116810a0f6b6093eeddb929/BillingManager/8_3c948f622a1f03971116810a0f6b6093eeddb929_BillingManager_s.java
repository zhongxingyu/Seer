 package net.anei.cadpage.billing;
 
 import java.util.Calendar;
 
 import android.app.Activity;
 import android.content.Context;
 import net.anei.cadpage.ManagePreferences;
 import net.anei.cadpage.billing.BillingService.RequestPurchase;
 import net.anei.cadpage.billing.BillingService.RestoreTransactions;
 import net.anei.cadpage.billing.Consts.PurchaseState;
 import net.anei.cadpage.billing.Consts.ResponseCode;
 
 
 public class BillingManager {
   
   private BillingService mService = null;
   private boolean reload = false;
   
   private boolean supported = false;
   
   /**
    * @return true if In-app billing is supported for this system
    */
   public boolean isSupported() {
     return supported;
   }
   
   private BillingManager() {
     reload = ! ManagePreferences.initBilling();
     ResponseHandler.register(new CadpageObserver());
   }
   
   /**
    * Initialize billing manager and associate it with an activity
    * @param activity main activity
    */
   public void initialize(Context context) {
     mService = new BillingService();
     mService.setContext(context);
     mService.checkBillingSupported();
   }
   
   /**
    * Shutdown billing manager
    */
   public void destroy() {
    mService.unbind();
     mService = null;
   }
   
   /**
    * Request purchase of current year product
    * @param activity
    */
   public void startPurchase(Activity activity) {
     int year = Calendar.getInstance().get(Calendar.YEAR);
     String item = "CADPAGE_" + year;
     mService.requestPurchase(activity, item, null); 
   }
   
   
   private class CadpageObserver extends PurchaseObserver {
 
     @Override
     public void onBillingSupported(boolean supported) {
       BillingManager.this.supported = supported;
       if (reload && supported) mService.restoreTransactions();
     }
 
     @Override
     public void onPurchaseStateChange(PurchaseState purchaseState,
                                        String itemId, long purchaseTime, 
                                        String developerPayload) {
       if (itemId.startsWith("CADPAGE_")) {
         int year = Integer.parseInt(itemId.substring(8));
         ManagePreferences.setPaidYear(year, purchaseState == PurchaseState.PURCHASED);
       }
     }
 
     @Override
     public void onRequestPurchaseResponse(RequestPurchase request,
         ResponseCode responseCode) {
     }
 
     @Override
     public void onRestoreTransactionsResponse(RestoreTransactions request,
                                                ResponseCode responseCode) {
       if (responseCode == ResponseCode.RESULT_OK) {
         reload = false;
         ManagePreferences.setInitBilling();
       }
     }
     
   }
   
   private static BillingManager instance = new BillingManager();
   
   public static BillingManager instance() {
     return instance;
   }
 
 }
