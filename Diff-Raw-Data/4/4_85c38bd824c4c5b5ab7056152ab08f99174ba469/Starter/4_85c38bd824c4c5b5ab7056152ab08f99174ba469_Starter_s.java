 package com.ronx.coupon.server;
 
 import com.ronx.coupon.entity.CouponSite;
 import com.ronx.coupon.service.CouponWebService;
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.configuration.PropertiesConfiguration;
 
 import javax.xml.ws.Endpoint;
 import java.util.concurrent.SynchronousQueue;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 public class Starter {
 
     public static void main(String[] args) {
 
         CouponSite pokupon = null;
         try {
             pokupon = new CouponSite(new PropertiesConfiguration("properties/pokupon.properties"));
         } catch (ConfigurationException e) {
             e.printStackTrace();
         }
         int numConnections = 1000;
 
         CouponWebService couponService = new CouponWebService();
         couponService.setCouponSite(pokupon);
 
 
        ThreadPoolExecutor executor = new ThreadPoolExecutor(3, 5, 10L, TimeUnit.NANOSECONDS, new SynchronousQueue< Runnable >());
         CouponServer server;
         {
             server = new CouponServer(executor);
             server.runServer();
         }
 
         ServerController serverController = new ServerController(server);
 
 //        ExecutorService threads = Executors.newFixedThreadPool(numConnections);
 
         Endpoint endpoint;
         {
             endpoint = Endpoint.publish("http://localhost:8888/WS/coupon", couponService);
             endpoint.setExecutor(executor);
         }
 
         return;
     }
 
 }
