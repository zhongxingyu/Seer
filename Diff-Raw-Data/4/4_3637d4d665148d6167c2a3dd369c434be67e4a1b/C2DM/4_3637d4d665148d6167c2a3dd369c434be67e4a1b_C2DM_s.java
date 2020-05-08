 package controllers;
 
 import com.google.gson.JsonArray;
 import models.Transaction;
 import models.User;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import play.Play;
 import play.libs.WS;
 import play.mvc.Controller;
 import utils.GsonUtil;
 import utils.ModelHelper;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class C2DM extends Controller {
 
     private static Logger logger = Logger.getLogger(
             C2DM.class.getName());
     private static final String C2DM_PUSH_TOKEN = Play.configuration.
             getProperty("c2dm.push.token");
     private static final String C2DM_PUSH_URL = Play.configuration.
             getProperty("c2dm.push.url");
 
     public static void c2dm(String registrationId, JsonArray json) {
         if (C2DM_PUSH_TOKEN == null) {
             logger.log(Level.ERROR,
                     "Missing c2dm.push.token i application.conf");
             return;
         }
         if (C2DM_PUSH_URL == null) {
             logger.log(Level.ERROR,
                     "Missing c2dm.push.url i application.conf");
             return;
         }
         final List<Transaction> transactions = GsonUtil.parseTransactions(json);
         final List<Transaction> updated = new ArrayList<Transaction>();
         final User user = User.find("deviceId", registrationId).first();
         if (user == null) {
             logger.log(Level.ERROR, "No user found with deviceId: " + registrationId);
             return;
         }
         for (Transaction t : transactions) {
             updated.add(ModelHelper.saveOrUpdate(t, user));
         }
         if (updated.isEmpty()) {
             logger.log(Level.ERROR,
                     "No transactions were saved");
             return;
         }
         WS.WSRequest request = WS.url(C2DM_PUSH_URL);
         request.headers.put("Authorization", String.format(
                 "GoogleLogin auth=%s", C2DM_PUSH_TOKEN));
         request.parameters.put("registration_id", registrationId);
         request.parameters.put("data.message", updated.size());
         request.parameters.put("collapse_key", ",");
         WS.HttpResponse response = request.post();
         if (response.getStatus() != 200) {
             logger.log(Level.ERROR, "Failed to send C2DM message: " +
                     response.getString());
         }
     }
 }
 
