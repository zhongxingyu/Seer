 package com.exadel.borsch.notifier;
 
 import com.exadel.borsch.dao.MenuItem;
 import com.exadel.borsch.dao.Order;
 import com.exadel.borsch.dao.User;
 import com.exadel.borsch.managers.ManagerFactory;
 import com.exadel.borsch.managers.OrderManager;
 import com.exadel.borsch.managers.UserManager;
 import com.exadel.borsch.notification.BrowserNotification;
import com.exadel.borsch.util.DateTimeUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.scheduling.annotation.Scheduled;
 import org.springframework.stereotype.Service;
 
 /**
  * @author Andrew Zhilka
  */
 @Service
 public class PayOrderNotifier extends NotifierTask {
     @Autowired
     private ManagerFactory managerFactory;
     private static final int SCHEDULED_HOURS = 2;
 
     public PayOrderNotifier() {
         super(new BrowserNotification());
     }
 
     @Override
    @Scheduled(fixedRate = SCHEDULED_HOURS * DateTimeUtils.MINUTES_IN_HOUR
            * DateTimeUtils.SECOND_IN_MINUTE * DateTimeUtils.MILLIS_IN_SECOND)
     public void runPeriodicCheck() {
         UserManager userManager = managerFactory.getUserManager();
         OrderManager orderManager = managerFactory.getOrderManager();
 
         for (User user : userManager.getAllUsers()) {
             Order weekOrder = orderManager.getCurrentOrderForUser(user);
 
             boolean isOrderPaid = true;
             for (MenuItem item : weekOrder.getOrder()) {
                 if (!item.getIsPaid()) {
                     isOrderPaid = false;
                 }
             }
 
             if (!isOrderPaid) {
                 this.getNotification().setMessage(extractMessage("user.notification.payment", user.getLocale()));
                 this.getNotification().submit(user);
             }
         }
     }
 }
