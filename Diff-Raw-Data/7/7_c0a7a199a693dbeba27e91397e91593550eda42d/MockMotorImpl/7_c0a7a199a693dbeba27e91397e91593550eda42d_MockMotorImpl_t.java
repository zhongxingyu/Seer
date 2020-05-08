 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.salaboy.rolo.mock;
 
 import com.salaboy.rolo.api.Motor;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author salaboy
  */
 @Mock
 public class MockMotorImpl implements Motor {
 
     private String name;
     private boolean running = false;
    private DIRECTION currentDirection = DIRECTION.NONE;
 
     public MockMotorImpl() {
     }
 
     public MockMotorImpl(String name) {
         this.name = name;
     }
 
     @Override
     public void setName(String name) {
         this.name = name;
     }
 
     @Override
     public String getName() {
         return name;
     }
 
     @Override
     public void forward(int speed, long millisec) {
         this.running = true;
         try {
             for(int i = 0; i < 10; i++){
                 Thread.sleep(millisec / 10);
                 System.out.println(">> Motor " + name + "is running forward ");
             }
         } catch (InterruptedException ex) {
             Logger.getLogger(MockMotorImpl.class.getName()).log(Level.SEVERE, null, ex);
         }
         this.running = false;
     }
 
     @Override
     public void backward(int speed, long millisec) {
         this.running = true;
         try {
             for(int i = 0; i < 10; i++){
                 Thread.sleep(millisec / 10);
                 System.out.println(">> Motor " + name + "is running backward ");
             }
         } catch (InterruptedException ex) {
             Logger.getLogger(MockMotorImpl.class.getName()).log(Level.SEVERE, null, ex);
         }
         this.running = false;
     }
 
     @Override
     public void start(int speed, DIRECTION dir) {
         this.running = true;
     }
 
     @Override
     public void stop() {
         this.running = false;
     }
 
     @Override
     public boolean isRunning() {
         return running;
     }
 
     @Override
     public void setRunning(boolean running) {
         this.running = running;
     }

    @Override
    public DIRECTION getCurrentDirection() {
        return currentDirection;
    }
 }
