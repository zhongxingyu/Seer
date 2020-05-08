 package com.ekaqu.example.guice.billing.testcase;
 
 import com.ekaqu.example.guice.billing.TestBillingModule;
 import com.google.common.eventbus.EventBus;
 import com.google.common.eventbus.Subscribe;
 import org.testng.annotations.Guice;
 import org.testng.annotations.Test;
 
 import javax.inject.Inject;
 import java.util.concurrent.TimeUnit;
 
import static org.testng.Assert.assertTrue;

 /**
  *
  */
 @Guice(modules = TestBillingModule.class)
 public class TestGlobalEventBus {
   private final EventBus eventBus;
   private boolean called = false;
 
   @Inject
   public TestGlobalEventBus(final EventBus eventBus) {
     this.eventBus = eventBus;
     System.out.println("Got EventBus " + eventBus);
   }
 
   @Test(groups = "Unit")
   public void test() throws InterruptedException {
     System.out.println("Running test");
     eventBus.post("Hello my friend hello");
    assertTrue(called, "Notify was called");
   }
 
   /**
    * Must be public else won't get called
    */
   @Subscribe
   public void notify(Object obj) {
     System.out.println(obj);
     called = true;
   }
 }
