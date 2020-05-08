 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package controller;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.http.HttpSessionBindingEvent;
 import javax.servlet.http.HttpSessionBindingListener;
 import picture.CreatePicture;
 
 /**
  *
  * @author Julian
  */
 public class Task extends Thread implements HttpSessionBindingListener {
 
     private final File deployStream;
 
     public Task(File file) {
         this.deployStream = file;
     }
 
     Task() {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     public void run()  {
         while (true) {
             CreatePicture instance = new CreatePicture();
             File tmp = new File(deployStream.getParent()+"tmp.png");
             instance.paintPicture(tmp);
             try {
                 instance.copyPicture(tmp, deployStream);
             } catch (IOException ex) {
                 Logger.getLogger(Task.class.getName()).log(Level.SEVERE, null, ex);
             }
             try {
                 this.sleep(800);
             } catch (InterruptedException ex) {
                 Logger.getLogger(Task.class.getName()).log(Level.SEVERE, null, ex);
             }
             if (isInterrupted()) {
                 return;
             }
         }
     }
 
     public void valueBound(HttpSessionBindingEvent event) {
         start(); // Will instantly be started when doing session.setAttribute("task", new Task());
     }
 
     public void valueUnbound(HttpSessionBindingEvent event) {
         interrupt(); // Will signal interrupt when session expires.
     }
 }
