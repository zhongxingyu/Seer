 package org.springthing.easyevents;
 
 import static org.junit.Assert.assertSame;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.context.ApplicationEvent;
 import org.springframework.context.annotation.AnnotationConfigApplicationContext;
 import org.springframework.context.annotation.Configuration;
 
 @Configuration
 public class EventPublisherTest {
 
     private AnnotationConfigApplicationContext applicationContext;
     private ObserverBean observer;
     private EventPublisher eventPublisher;
 
     @Before
     public void setUp() {
        applicationContext = new AnnotationConfigApplicationContext(TestContext.class);
         observer = applicationContext.getBean(ObserverBean.class);
         eventPublisher = applicationContext.getBean(EventPublisher.class);
     }
 
     @Test
     public void publishMy() throws Exception {
         Object my = new MyEvent();
         eventPublisher.publish(my);
         assertSame(my, observer.my);
     }
 
     @Test
     public void publishYour() throws Exception {
         Object your = new YourEvent();
         eventPublisher.publish(your);
         assertSame(your, observer.your);
     }
 
     @Test
     public void publishOur() throws Exception {
         Object our = new OurEvent();
         eventPublisher.publish(our);
         assertSame(our, observer.our);
         assertSame(our, observer.my);
     }
 
     @Test
     public void publishApp() throws Exception {
         CustomAppEvent app = new CustomAppEvent();
         applicationContext.publishEvent(app);
         assertSame(app, observer.app);
     }
 }
 
 class MyEvent {
 }
 
 class YourEvent {
 }
 
 class OurEvent extends MyEvent {
 }
 
 class CustomAppEvent extends ApplicationEvent {
     public CustomAppEvent() {
         super("source");
     }
 }
