 package controllers;
 
 import com.google.checkout.sdk.commands.ApiContext;
 import com.google.checkout.sdk.domain.OrderSummary;
 import com.google.checkout.sdk.notifications.Notification;
 import models.GoogleCheckoutApiFactory;
 import models.GoogleCheckoutInitializationException;
 import models.GoogleCheckoutNotification;
 import models.PlayNotificationDispatcher;
 import play.Logger;
 import play.mvc.Controller;
 
 public class GoogleCheckout extends Controller {
     public static void callback() {
         ApiContext api;
 
         try {
             api = GoogleCheckoutApiFactory.build();
         } catch (GoogleCheckoutInitializationException ex) {
             Logger.error(ex, "Unable to create Google Checkout API");
             badRequest();
             return;
         }
 
         api.handleNotification(new PlayNotificationDispatcher(request, response) {
             @Override
             protected boolean hasAlreadyHandled(String serialNumber, OrderSummary orderSummary, Notification notification) throws Exception {
                 return GoogleCheckoutNotification.existsForSerialNumber(serialNumber);
             }
 
             @Override
             protected void rememberSerialNumber(String serialNumber, OrderSummary orderSummary, Notification notification) throws Exception {
                 GoogleCheckoutNotification notificationToStore = new GoogleCheckoutNotification();
                 notificationToStore.SerialNumber = serialNumber;
                 notificationToStore.save();
             }
 
             @Override
             protected void onAllNotifications(OrderSummary orderSummary, Notification notification) throws Exception {
                 Logger.info("NOTIFICATION: "+notification);
             }
         });
     }
 }
