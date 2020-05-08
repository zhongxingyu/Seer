 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.seldonsystems.demo.javafx.employeeadmin.view;
 
 import com.seldonsystems.demo.javafx.employeeadmin.ApplicationFacade;
 import com.seldonsystems.demo.javafx.employeeadmin.model.UserProxy;
 import com.seldonsystems.demo.javafx.employeeadmin.model.vo.UserVO;
 import com.seldonsystems.demo.javafx.employeeadmin.view.component.IUserList;
 import org.puremvc.java.multicore.interfaces.INotification;
 import org.puremvc.java.multicore.patterns.mediator.Mediator;
 
 /**
  *
  * @author Jhonghee Park @ Seldon Systems, Inc.
  */
 public class UserListMediator extends Mediator {
 
     public static final String LOOKUP_ID = "userList";
     private UserProxy userProxy;
 
     public UserListMediator(Object viewComponent) {
         super(NAME, viewComponent);
     }
 
     private IUserList getUserList() {
         return (IUserList) getViewComponent();
     }
 
     @Override
     public void handleNotification(INotification notification) {
         String note = notification.getName();
 
         if( note.equals(ApplicationFacade.USER_DELETED)) {
             getUserList().setUsers(userProxy.getUsers());
         }
     }
 
     @Override
     public String[] listNotificationInterests() {
         String[] interests = {ApplicationFacade.USER_DELETED};
         return interests;
     }
 
     @Override
     public void onRegister() {
         getUserList().setMediator(this);
 
         userProxy = (UserProxy) getFacade().retrieveProxy(UserProxy.NAME);
         getUserList().setUsers(userProxy.getUsers());
 
     }
 
     public void onDelete(UserVO selected) {
         sendNotification(ApplicationFacade.DELETE_USER, selected);
     }
 
     public void onNew() {
         System.out.println("New button was pressed");
     }
 
     public void onSelect(UserVO selected) {
        System.out.println("Selected row index: " + selected);
     }
 }
