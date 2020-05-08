 /**
  * ApplicationWindow.java Created by jhoppmann on Mar 10, 2013
  */
 package com.dhbw_db.view;
 
 import com.dhbw_db.control.MainUI;
 import com.dhbw_db.view.login.LoginWindow;
 import com.vaadin.server.ClassResource;
 import com.vaadin.ui.Alignment;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Image;
 import com.vaadin.ui.Panel;
 import com.vaadin.ui.VerticalLayout;
 
 /**
  * @author jhoppmann
  * @version 0.1
  * @since
  */
 public class ApplicationWindow extends Panel {
 
 	private static final long serialVersionUID = -1232697565973119448L;
 
 	Panel topPanel;
 
 	NavigationBar menu;
 
 	Panel applicationSpace;
 
 	public ApplicationWindow() {
 
 		if (((MainUI) (MainUI.getCurrent())).getController()
 											.getUser() == null) {
 			LoginWindow login = new LoginWindow();
 			MainUI.getCurrent()
 					.addWindow(login);
			login.focus();
 
 		} else {
 
 			VerticalLayout vlo = new VerticalLayout();
 
 			// build the top panel
 			topPanel = new Panel();
 			// topPanel.setHeight(200, Unit.PIXELS);
 			HorizontalLayout topPanelContent = new HorizontalLayout();
 			Image topImage = new Image(	null,
 										new ClassResource("../../../img/dhbwtop.jpg"));
 			topPanelContent.addComponent(topImage);
 			topPanelContent.setComponentAlignment(	topImage,
 													Alignment.MIDDLE_CENTER);
 			topPanelContent.setSizeFull();
 
 			topPanel.setContent(topPanelContent);
 
 			// create the menu bar
 			menu = new NavigationBar();
 
 			// create the main application space
 			applicationSpace = new Panel(new StudentStartPage());
 			applicationSpace.setSizeFull();
 
 			// add all components to the layout
 			vlo.addComponent(topPanel);
 			vlo.addComponent(menu);
 			vlo.addComponent(applicationSpace);
 
 			setContent(vlo);
 
 		}
 	}
 
 	public void replaceView(Component c) {
 		// applicationSpace.setContent(c);
 	}
 
 }
