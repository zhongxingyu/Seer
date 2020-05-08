 package com;
 
 import com.screen.login.Login;
 import com.vaadin.Application;
 import com.vaadin.terminal.Sizeable;
 import com.vaadin.ui.*;
 
 /**
  * User: ASUS
  * Date: 9/18/13
  * Time: 9:45 PM
  */
 public class TwitterOnVaadin extends Application {
     Window mWindow;
 
 
     //<editor-fold desc="Constructor">
     public TwitterOnVaadin(){
         mWindow = new Window("Twitter Application");
 
     }
     //</editor-fold>
 
     //<editor-fold desc="General method">
     @Override
     public void init() {



         Login loginScreen = new Login(mWindow);
 
         mWindow.setContent(loginScreen.getLoginScreen());
 
         setMainWindow(mWindow);
     }
     //</editor-fold>
 
 
 }
