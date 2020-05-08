 package com.screen.login;
 
 import com.screen.registration.Registration;
 import com.vaadin.ui.*;
 
 import java.io.IOException;
 
 /**
  * User: ASUS
  * Date: 10/3/13
  * Time: 9:00 AM
  */
 public class Login {
 
     TextField mUserName;
     PasswordField mPassword;
     Button mLogin;
     Button mRegistration;
     Window mCtx;
 
      //<editor-fold desc="Constructor">
     public Login(Window ctx){
         mUserName = new TextField();
         mPassword = new PasswordField();
         mLogin = new Button("Login", btn_login_click);
         mRegistration = new Button("Registration", btn_registration_click);
         mCtx = ctx;
     }
     //</editor-fold>
 
     //<editor-fold desc="General Methods">
     public VerticalLayout getLoginScreen(){
         Panel panel = new Panel("Login");
         panel.setSizeUndefined();
 
        CustomLayout customLayout;
         try {
             customLayout = new CustomLayout(getClass().getResourceAsStream("Login.html"));
         } catch (IOException e) {
             e.printStackTrace();
             return new VerticalLayout();
         }
 
         customLayout.addComponent(mUserName, "username");
         customLayout.addComponent(mPassword, "password");
         customLayout.addComponent(mLogin, "ok");
         customLayout.addComponent(mRegistration,"registration");
 
         panel.setContent(customLayout);
 
         VerticalLayout loginTemplate = new VerticalLayout();
         loginTemplate.setSizeFull();
         loginTemplate.addComponent(panel);
         loginTemplate.setComponentAlignment(panel, Alignment.MIDDLE_CENTER);
         return loginTemplate;
     }
     //</editor-fold>
 
     //<editor-fold desc="Listeners">
     Button.ClickListener btn_registration_click = new Button.ClickListener(){
         @Override
         public void buttonClick(Button.ClickEvent clickEvent) {
             Registration registration = new Registration(mCtx);
             mCtx.setContent(registration.getRegistrationScreen());
         }
     };
     Button.ClickListener btn_login_click = new Button.ClickListener(){
         @Override
         public void buttonClick(Button.ClickEvent clickEvent) {
             String yourAcc = String.format("Your name:%s Your password:%s",mUserName,mPassword);
             mCtx.showNotification(yourAcc);
         }
     };
     //</editor-fold>
 }
