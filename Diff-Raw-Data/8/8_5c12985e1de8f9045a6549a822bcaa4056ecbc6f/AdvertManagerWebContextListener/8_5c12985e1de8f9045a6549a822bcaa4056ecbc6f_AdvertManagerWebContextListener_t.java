 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mne.advertmanager.web.infra;
 
 import javax.servlet.ServletContextEvent;
 import org.springframework.web.context.ContextLoaderListener;
 
 /**
  *
  * @author Nina Eidelshtein and Misha Lebedev
  */
 public class AdvertManagerWebContextListener  extends ContextLoaderListener {
 
     @Override
     public void contextDestroyed(ServletContextEvent event) {
         super.contextDestroyed(event);
     }
 
     @Override
     public void contextInitialized(ServletContextEvent event) {
        int x =0;
        try {
            super.contextInitialized(event);
            x=1;
        }catch(Exception ex) {
            x=2;
        }
     }
     
 }
