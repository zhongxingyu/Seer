 /**
  * LoginWindow.java Created by jhoppmann on Mar 12, 2013
  */
 package com.dhbw_db.view.login;
 
 import com.dhbw_db.control.MainUI;
 import com.vaadin.ui.Alignment;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.PasswordField;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.Window;
 
 /**
  * @author jhoppmann
  * @version 0.1
  * @since
  */
 public class LoginWindow extends Window {
 
 	private static final long serialVersionUID = 4371837274170311578L;
 
 	public LoginWindow() {
 		// Note: Tabkey doesn't work right due to a bug in Vaadin
 		super("Login");
 		super.setModal(true);
 		super.setClosable(false);
 
 		VerticalLayout vlo = new VerticalLayout();
 		vlo.setWidth("250px");
 		vlo.setSpacing(true);
 
 		final TextField username = new TextField();
 		username.setInputPrompt("Username");
 		username.setWidth("200px");
 
 		final PasswordField password = new PasswordField();
 		password.setInputPrompt("Password");
 		password.setWidth("200px");
 
 		Button submit = new Button("Submit");
 		// It doesn't really make sense to place this anywhere else
 		submit.addClickListener(new ClickListener() {
 
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void buttonClick(ClickEvent event) {
 				((MainUI) (MainUI.getCurrent())).getController()
 												.authenticate(	username.getValue(),
 																password.getValue());
 				LoginWindow.this.close();
 			}
 		});
 
 		vlo.addComponent(username);
 		vlo.setComponentAlignment(username, Alignment.MIDDLE_CENTER);
 		vlo.addComponent(password);
 		vlo.setComponentAlignment(password, Alignment.MIDDLE_CENTER);
 		vlo.addComponent(submit);
 		vlo.setComponentAlignment(submit, Alignment.MIDDLE_CENTER);
 
 		super.setContent(vlo);
		super.focus();
 	}
 }
