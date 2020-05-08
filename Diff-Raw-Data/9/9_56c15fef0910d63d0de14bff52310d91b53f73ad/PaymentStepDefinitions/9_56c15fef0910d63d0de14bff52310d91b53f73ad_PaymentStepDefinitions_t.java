 
 package com.openfeint.qa.ggp.step_definitions;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertTrue;
 import static junit.framework.Assert.fail;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import net.gree.asdk.api.GreePlatform;
 import net.gree.asdk.api.wallet.Payment;
 import net.gree.asdk.api.wallet.Payment.VerifyListener;
 import net.gree.asdk.api.wallet.PaymentItem;
 
 import org.apache.http.HeaderIterator;
 
 import util.Consts;
 import util.PopupHandler;
 import util.PopupUtil;
 import android.app.Instrumentation;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 import android.view.KeyEvent;
 
 import com.openfeint.qa.core.caze.step.Step;
 import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
 import com.openfeint.qa.core.command.And;
 import com.openfeint.qa.core.command.Then;
 import com.openfeint.qa.core.command.When;
 
 public class PaymentStepDefinitions extends BasicStepDefinition {
     private static final String TAG = "Payment_Steps";
 
     private static final String PAYMENT_PARAMS = "payment_params";
 
     private static final String PAYMENT_MSG = "payment_message";
 
     private static final String PAYMENT_ITEM = "payment_item";
 
     private static final String PAYMENT_STATUS = "payment_status";
 
     @SuppressWarnings("serial")
     private HashMap<String, String> statementsToGetPopupElement = new HashMap<String, String>() {
         {
             put("payment-payment items", "fclass('flexible')) + stringify(ftag('img')");
             put("payment-popupTitle", "ftag('title')");
             put("payment-totalAmount", "fclass('solid min')");
             put("payment-message", "fclass('sentence medium minor')");
         }
     };
 
     @SuppressWarnings("unchecked")
     @When("I add payment item with ID (\\w+) and NAME (.+) and UNITPRICE (\\d+) and QUANTITY (\\d+) and IMAGEURL (.+) and DESCRIPTION (.+)")
     @And("I add payment item with ID (\\w+) and NAME (.+) and UNITPRICE (\\d+) and QUANTITY (\\d+) and IMAGEURL (.+) and DESCRIPTION (.+)")
     public void addPaymentItem(String itemId, String itemName, int unitPrice, int quantity,
             String imgUrl, String desc) {
         String[] params = {
                 itemId, itemName, String.valueOf(unitPrice), String.valueOf(quantity), imgUrl, desc
         };
         if (getBlockRepo().get(PAYMENT_PARAMS) == null) {
             ArrayList<String[]> list = new ArrayList<String[]>();
             list.add(params);
             getBlockRepo().put(PAYMENT_PARAMS, list);
         } else {
             ((ArrayList<String[]>) getBlockRepo().get(PAYMENT_PARAMS)).add(params);
         }
 
     }
 
     @And("I set payment popup message (.+)")
     public void initPaymentMessage(String message) {
         getBlockRepo().put(PAYMENT_MSG, message);
     }
 
     @SuppressWarnings("unchecked")
     @And("I did open payment request popup")
     public void sendPaymentRequest() {
 
         Intent intent_open = new Intent(PopupHandler.ACTION_PAYMENT_POPUP);
         intent_open.putExtra(PopupHandler.POPUP_PARAMS,
                 (ArrayList<String[]>) getBlockRepo().get(PAYMENT_PARAMS));
         String payment_message = "";
         if (getBlockRepo().get(PAYMENT_MSG) != null) {
             payment_message = (String) getBlockRepo().get(PAYMENT_MSG);
         }
         intent_open.putExtra(PopupHandler.PAYMENT_MESSAGE, payment_message);
 
         notifyStepWait();
         Step.setTimeout(40000);
 
         PopupHandler.is_popup_opened = false;
         // destroy payment params
         getBlockRepo().remove(PAYMENT_PARAMS);
         // ask action queue to open popup
         GreePlatform.getContext().sendBroadcast(intent_open);
 
         // wait payment popup dialog to open
         while (!PopupHandler.is_popup_opened) {
             Log.d(TAG, "waiting for payment popup open!");
             try {
                 Thread.sleep(1000);
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
         }
 
         Intent intent_check_loaded = new Intent(PopupHandler.ACTION_CHECK_PAYMENT_POPUP_LOADED);
         GreePlatform.getContext().sendOrderedBroadcast(intent_check_loaded, null,
                 new BroadcastReceiver() {
                     @Override
                     public void onReceive(Context context, Intent intent) {
                         if (PopupHandler.RESULT_SUCCESS == getResultCode()) {
                             Log.d(TAG, "load payment popup success!");
                         } else if (PopupHandler.RESULT_FAILURE == getResultCode()) {
                             Log.e(TAG, "load payment popup failed!");
                         } else if (PopupHandler.RESULT_UNKNOWN == getResultCode()) {
                             Log.e(TAG, "something wrong, still unknown status!");
                         }
                         notifyStepPass();
                     }
                 }, null, PopupHandler.RESULT_UNKNOWN, null, null);
     }
 
     @When("I check payment request popup info (.+)")
     @And("I check payment request popup info (.+)")
     public void getPaymentPopupInfo(String column) {
         PopupUtil.getValueFromPopup(statementsToGetPopupElement.get("payment-" + column));
         getBlockRepo().put("payment-" + column, PopupHandler.valueToBeVerified);
     }
 
     @Then("payment request item (\\w+) info (\\w+) should be (.+)")
     @And("payment request item (\\w+) info (\\w+) should be (.+)")
     public void verifyPaymentItemInfoFromPopup(String itemName, String column, String expectValue) {
         String resultValue = (String) getBlockRepo().get("payment-payment items");
         assertTrue("value from payment popup", resultValue.contains(expectValue));
     }
 
     @Then("payment request popup info (\\w+) should be (.+)")
     @And("payment request popup info (\\w+) should be (.+)")
     public void verifyPaymentPopupInfo(String column, String expectValue) {
         String resultValue = (String) getBlockRepo().get("payment-" + column);
         assertTrue("value from payment popup", resultValue.contains(expectValue));
     }
 
     @When("I click back button on device")
     public void clickBackButton() {
         // wait UI thread to show out popup
         try {
            Thread.sleep(5000);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
         PopupHandler.is_popup_canceled = false;
         Instrumentation inst = new Instrumentation();
         inst.sendCharacterSync(KeyEvent.KEYCODE_BACK);
     }
 
     @Then("the payment popup should be closed")
     public void verifyPaymentDialogClosed() {
         int times = 1;
         while (times <= 5) {
             try {
                 Thread.sleep(1000);
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
             if (PopupHandler.is_popup_canceled) {
                 assertTrue(true);
                 return;
             }
             times++;
         }
         fail("payment popup is not closed");
     }
 
     @When("I init payment item with ID (\\w+) and NAME (.+) and UNITPRICE (\\d+) and QUANTITY (\\d+) and IMAGEURL (.+) and DESCRIPTION (.+)")
     public void initPaymentItem(String itemId, String itemName, int unitPrice, int quantity,
             String imgUrl, String desc) {
         PaymentItem item = new PaymentItem(itemId, itemName, unitPrice, quantity);
         item.setImageUrl(imgUrl);
         item.setDescription(desc);
         getBlockRepo().put(PAYMENT_ITEM, item);
     }
 
     @Then("(\\w+) of payment item should be (.+)")
     @And("(\\w+) of payment item should be (.+)")
     public void verifyPaymentItemInfo(String column, String expectValue) {
         PaymentItem item = (PaymentItem) getBlockRepo().get(PAYMENT_ITEM);
         if (item == null)
             fail("payment item is not initialized!");
 
         String actualValue = "";
         if ("ID".equals(column)) {
             actualValue = item.getItemId();
         } else if ("NAME".equals(column)) {
             actualValue = item.getItemName();
         } else if ("UNITPRICE".equals(column)) {
             actualValue = String.valueOf(Double.valueOf(item.getUnitPrice()).intValue());
         } else if ("QUANTITY".equals(column)) {
             actualValue = String.valueOf(item.getQuantity());
         } else if ("IMAGEURL".equals(column)) {
             actualValue = item.getImageUrl();
         } else if ("DESCRIPTION".equals(column)) {
             actualValue = item.getDescription();
         }
         assertEquals("payment item info", expectValue, actualValue);
     }
 
     @When("I verify payment order (.+)")
     public void verifyPaymentOrder(String paymentId) {
         getBlockRepo().remove(PAYMENT_STATUS);
         notifyStepWait();
         Payment.verify(paymentId, new VerifyListener() {
             @Override
             public void onSuccess(int responseCode, HeaderIterator headers, String paymentId) {
                 Log.d(TAG, "payment order status is success!");
                 getBlockRepo().put(PAYMENT_STATUS, Consts.SUCCESS);
                 notifyStepPass();
             }
 
             @Override
             public void onFailure(int responseCode, HeaderIterator headers, String paymentId,
                     String response) {
                 if (response.contains("status:1")) {
                     Log.d(TAG, "payment order status is failed!");
                     getBlockRepo().put(PAYMENT_STATUS, Consts.FAILED);
                 } else {
                     Log.e(TAG, "verify payment order failed!");
                 }
                 notifyStepPass();
             }
 
             @Override
             public void onCancel(int responseCode, HeaderIterator headers, String paymentId) {
                 Log.d(TAG, "payment order status is cancel!");
                 getBlockRepo().put(PAYMENT_STATUS, Consts.CANCEL);
                 notifyStepPass();
             }
         });
     }
 
     @Then("payment order verify result should be (\\w+)")
     public void verifyPaymentOrderResult(String expectResult) {
         String actualResult = (String) getBlockRepo().get(PAYMENT_STATUS);
         if (actualResult == null)
             fail("Don't get result from payment verify!");
 
         assertEquals(expectResult, actualResult);
     }
 }
