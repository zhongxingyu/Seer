 package com.screen.registration;
 
 import com.screen.login.Login;
 import com.vaadin.terminal.Sizeable;
 import com.vaadin.ui.*;
 
 import java.io.IOException;
 
 /**
  * User: ASUS
  * Date: 10/3/13
  * Time: 9:10 AM
  */
 public class Registration {
 
     TextField mSurname;
     TextField mForename;
     TextField mAge;
     TextField mUserName;
     PasswordField mPassword;
     PasswordField mRepPassword;
     Button mOk;
     Button mCancel;
     Window mCtx;
 
 
     //<editor-fold desc="Constructor">
     public Registration(Window ctx){
         mCtx = ctx;
         mSurname = new TextField();
         mForename = new TextField();
         mAge = new TextField();
         mUserName = new TextField();
         mPassword = new PasswordField();
         mRepPassword = new PasswordField();
         mOk = new Button("Ok",btn_ok_click);
         mCancel = new Button("Cancel",btn_cancel_click);
     }
     //</editor-fold>
 
     //<editor-fold desc="General Methods">
    public GridLayout getRegistrationScreen(){
         Panel panel = new Panel("Registration");
         panel.setSizeUndefined();
 
        CustomLayout customLayout = null;
         try {
             customLayout = new CustomLayout(getClass().getResourceAsStream("Registration.html"));
         } catch (IOException e) {
            e.printStackTrace();
         }
        GridLayout registrationScreen = new GridLayout(3,3);
        registrationScreen.setWidth(60, Sizeable.UNITS_PERCENTAGE);
        registrationScreen.setHeight(60, Sizeable.UNITS_PERCENTAGE);
 
         customLayout.addComponent(mForename, "forename");
         customLayout.addComponent(mSurname, "surname");
         customLayout.addComponent(mAge,"age");
         customLayout.addComponent(mUserName, "username");
         customLayout.addComponent(mPassword, "password");
         customLayout.addComponent(mRepPassword,"reppassword");
         customLayout.addComponent(mOk, "ok");
         customLayout.addComponent(mCancel, "cancel");
 
         panel.setContent(customLayout);
 
         VerticalLayout registrationTemplate = new VerticalLayout();
         registrationTemplate.addComponent(panel);
         registrationTemplate.setComponentAlignment(panel, Alignment.MIDDLE_CENTER);
 
        registrationScreen.addComponent(registrationTemplate, 2, 2);
        return registrationScreen;
     }
     //</editor-fold>
 
     //<editor-fold desc="Listeners">
     Button.ClickListener btn_cancel_click = new Button.ClickListener(){
 
         @Override
         public void buttonClick(Button.ClickEvent clickEvent) {
             Login loginScreen = new Login(mCtx);
             mCtx.setContent(loginScreen.getLoginScreen());
         }
     };
     Button.ClickListener btn_ok_click = new Button.ClickListener(){
 
         @Override
         public void buttonClick(Button.ClickEvent clickEvent) {
             mCtx.showNotification("Now you can try to login!");
             Login loginScreen = new Login(mCtx);
             mCtx.setContent(loginScreen.getLoginScreen());
         }
     };
     //</editor-fold>
 
 
 }
