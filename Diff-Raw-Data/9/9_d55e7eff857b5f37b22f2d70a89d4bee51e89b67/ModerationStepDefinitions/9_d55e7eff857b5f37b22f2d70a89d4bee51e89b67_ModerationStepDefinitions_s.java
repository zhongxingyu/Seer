 
 package com.openfeint.qa.ggp.step_definitions;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertTrue;
 import static junit.framework.Assert.fail;
 
 import java.lang.reflect.Array;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 
 import net.gree.asdk.api.ModeratedText;
 import net.gree.asdk.api.ModeratedText.ModeratedTextListener;
 import net.gree.asdk.api.ModeratedText.SuccessListener;
 
 import org.apache.http.HeaderIterator;
 
 import android.util.Log;
 
 import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
 import com.openfeint.qa.core.command.After;
 import com.openfeint.qa.core.command.And;
 import com.openfeint.qa.core.command.Given;
 import com.openfeint.qa.core.command.Then;
 import com.openfeint.qa.core.command.When;
 
 /*
  * Template: https://git.gree-dev.net/ggpautomation/common-steps/wiki/TS-Moderation
  */
 public class ModerationStepDefinitions extends BasicStepDefinition {
     private static final String TAG = "Moderation_Steps";
 
     private static String MODERATION_LIST = "moderationlist";
 
    private static String MODERATION_TEXT = "moderationtest";
 
     @When("I send to moderation server with text (.+)")
     @Given("I make sure moderation server INCLUDES text (.+)")
     public void sendModeration(String text) {
         getBlockRepo().remove(MODERATION_TEXT);
         notifyStepWait();
         ModeratedText.create(text, new ModeratedTextListener() {
             @Override
             public void onSuccess(ModeratedText[] textInfo) {
                 Log.d(TAG, "Create moderated text success!");
                 ModeratedText.logTextInfo(textInfo);
                Log.e(TAG, "new id is: " + textInfo[0].getTextId());
                 getBlockRepo().put(MODERATION_TEXT, textInfo[0]);
                 notifyStepPass();
             }
 
             @Override
             public void onFailure(int responseCode, HeaderIterator headers, String response) {
                 Log.e(TAG, "Create moderated text failed!");
                 notifyStepPass();
             }
         });
     }
 
     private int transStatus(String status) {
         if ("CHECKING".equals(status))
             return ModeratedText.STATUS_BEING_CHECKED;
         else if ("APPROVED".equals(status))
             return ModeratedText.STATUS_RESULT_APPROVED;
         else if ("DELETED".equals(status))
             return ModeratedText.STATUS_DELETED;
         else if ("REJECTED".equals(status))
             return ModeratedText.STATUS_RESULT_REJECTED;
         else if ("UNKNOWN".equals(status))
             return ModeratedText.STATUS_UNKNOWN;
         else
             fail("Unknow status of the moderation text!");
         return -100;
     }
 
     private ModeratedText getTextIfExist() {
         if (getBlockRepo().get(MODERATION_TEXT) == null)
             fail("No moderation text stored in the Repo!");
         return (ModeratedText) getBlockRepo().get(MODERATION_TEXT);
     }
 
     @Then("status of text (.+) should be (\\w+)")
     public void verifyModerationStatus(String text, String status) {
         ModeratedText t = getTextIfExist();
         assertEquals("Text status", transStatus(status), t.getStatus());
     }
 
     @When("I load from SERVER with moderation text (.+)")
     public void loadTextWithId(String text) {
         ModeratedText old_text = getTextIfExist();
         String[] ids = {
             old_text.getTextId()
         };
         notifyStepWait();
         getBlockRepo().remove(MODERATION_TEXT);
         ModeratedText.loadFromIds(ids, new ModeratedTextListener() {
 
             @Override
             public void onSuccess(ModeratedText[] textInfo) {
                 Log.d(TAG, "Get moderated text form server success!");
                 getBlockRepo().put(MODERATION_TEXT, textInfo[0]);
                 notifyStepPass();
             }
 
             @Override
             public void onFailure(int responseCode, HeaderIterator headers, String response) {
                 Log.e(TAG, "Get moderated text form server failed! " + response);
                 notifyStepPass();
             }
         });
     }
 
     private ModeratedText getTextFromList(String text_id) {
         ArrayList<ModeratedText> list = (ArrayList<ModeratedText>) getBlockRepo().get(
                 MODERATION_LIST);
        Log.e(TAG, "id in the list is: " + list.get(0).getTextId() + " and text: "
                + list.get(0).getContent());
         for (ModeratedText text : list) {
             if (text_id.equals(text.getTextId()))
                 return text;
         }
         fail("Can not find text with id: " + text_id + " in the list!");
         return null;
     }
 
     @When("I load from NATIVE_CACHE with moderation text (.+)")
     public void loadModerationText(String text) {
         getBlockRepo().put(MODERATION_LIST, new ArrayList<ModeratedText>());
         ModeratedText.loadFromLocalCache(false, new ModeratedTextListener() {
 
             @Override
             public void onSuccess(ModeratedText[] textInfo) {
                 Log.d(TAG, "Get moderated text form native cache success!");
                 Log.d(TAG, "Get " + textInfo.length + " text!");
                 ((ArrayList<ModeratedText>) getBlockRepo().get(MODERATION_LIST)).addAll(Arrays
                         .asList(textInfo));
                 notifyAsyncInStep();
             }
 
             @Override
             public void onFailure(int responseCode, HeaderIterator headers, String response) {
                 Log.e(TAG, "Get moderated text form native cache failed! " + response);
                 notifyAsyncInStep();
             }
         });
         waitForAsyncInStep();
         ModeratedText old_text = getTextIfExist();
         // Get text from native cache will return a list, so find text we want
         // and store for verifing
         getBlockRepo().put(MODERATION_TEXT, getTextFromList(old_text.getTextId()));
     }
 
     @After("I make sure moderation server NOTINCLUDES text (.+)")
     public void cleanUpText(String text) {
         ModeratedText t = getTextIfExist();
        Log.e(TAG, "text to be delete is: " + t.getTextId());
         notifyStepWait();
         t.delete(new SuccessListener() {
             @Override
             public void onSuccess() {
                 Log.d(TAG, "Clean up success!");
                 notifyStepPass();
             }
 
             @Override
             public void onFailure(int responseCode, HeaderIterator headers, String response) {
                 Log.e(TAG, "Clean up failed, " + response);
                 notifyStepPass();
             }
         });
     }
 
     @When("I update text (.+) with new text (.+)")
     public void updateText(String text, final String newText) {
         final ModeratedText t = getTextIfExist();
         notifyStepWait();
         t.update(newText, new SuccessListener() {
             @Override
             public void onSuccess() {
                 Log.i(TAG, "Update moderation text success!");
                 Log.i(TAG, "Local text object also updated to: " + t.getContent());
                 assertEquals("local moderation text", newText, t.getContent());
                 notifyStepPass();
             }
 
             @Override
             public void onFailure(int responseCode, HeaderIterator headers, String response) {
                 Log.e(TAG, "Update moderation text failed!");
                 notifyStepPass();
             }
         });
     }
 
     @Then("new text should be (.+)")
     public void verifyTextUpdated(String newText) {
         ModeratedText t = getTextIfExist();
         assertEquals("Updated text", newText, t.getContent());
     }
 }
