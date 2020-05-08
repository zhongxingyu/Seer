 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.marcel.web.mavenjsfcdi;
 
 import java.io.Serializable;
 import javax.enterprise.context.RequestScoped;
 import javax.enterprise.event.Event;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 /**
  *
  * @author Marcel
  */
 
 @Named
 @RequestScoped
 public class ActionBean implements Serializable{
     
     @Inject
     private Event<MyEvent> eventQueue;
     
     public void doAction() {
        System.out.println("fire Event");
         eventQueue.fire( new MyEvent() );
     }
     
 }
