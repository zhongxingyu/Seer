 package com.cqlybest.admin.task;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.UUID;
 
 import org.joda.time.DateTime;
 import org.joda.time.LocalTime;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import com.cqlybest.common.mongo.bean.Customer;
 import com.cqlybest.common.mongo.bean.CustomerEvent;
 import com.cqlybest.common.mongo.dao.MongoDb;
 
 @Component
 public class BirthScan implements InitializingBean {
   private static final Logger LOGGER = LoggerFactory.getLogger(BirthScan.class);
 
   @Autowired
   private MongoDb mongoDb;
 
   @Override
   public void afterPropertiesSet() throws Exception {
     LOGGER.info("Schedule BirthScan.");
     new Timer().schedule(new BirthScanTask(), new LocalTime(23, 30, 0).toDateTimeToday().toDate(),
         86400000);
   }
 
   private class BirthScanTask extends TimerTask {
     @Override
     public void run() {
       LOGGER.info("Start BirthScanTask.");
       DateTime firstDay = DateTime.now().plusDays(1);
       DateTime secondDay = firstDay.plusDays(1);
       List<Customer> firstCustomers =
           mongoDb.createQuery("Customer").eq("birth.month", firstDay.getMonthOfYear())
               .eq("birth.dayOfMonth", firstDay.getDayOfMonth()).findObjects(Customer.class)
               .readAll();
       List<Customer> secondCustomers =
           mongoDb.createQuery("Customer").eq("birth.month", secondDay.getMonthOfYear())
               .eq("birth.dayOfMonth", secondDay.getDayOfMonth()).findObjects(Customer.class)
               .readAll();
       List<CustomerEvent> events = new ArrayList<>();
       for (Customer customer : firstCustomers) {
         CustomerEvent event = new CustomerEvent();
         event.setId(UUID.randomUUID().toString());
         event.setCustomerId(customer.getId());
         event.setName("生日");
         event.setDescription("今天是" + customer.getFullname() + "的生日。");
         event.setEventDate(customer.getBirth());
         event.setCreated(firstDay.toString("yyyyMMdd"));
         // event.setExpire(expire);
         events.add(event);
       }
       for (Customer customer : secondCustomers) {
         CustomerEvent event = new CustomerEvent();
         event.setId(UUID.randomUUID().toString());
         event.setCustomerId(customer.getId());
         event.setName("生日");
         event.setDescription("明天是" + customer.getFullname() + "的生日。");
         event.setEventDate(customer.getBirth());
         event.setCreated(firstDay.toString("yyyyMMdd"));
         // event.setExpire(expire);
         events.add(event);
       }
      if (!events.isEmpty()) {
        mongoDb.createObjects("CustomerEvent", events.toArray());
      }
       LOGGER.info("BirthScanTask is finished.");
     }
   }
 
 }
