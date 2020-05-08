 package com.appspot.codsallarts.client;
import com.appspot.codsallarts.client.images.*;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class Festival implements EntryPoint {
 	/**
 	 * The message displayed to the user when the server cannot be reached or
 	 * returns an error.
 	 */
 
 	private Application app = new Application();
 	
 	FestivalIconBundle icons = (FestivalIconBundle)GWT.create(FestivalIconBundle.class);
 	private Anchor signInLink = new Anchor("Admin login");
 	private Anchor signOutLink = new Anchor("Sign Out");
 
 	public void onModuleLoad() {
 		
 		login();
 	}
 
 	private void login(){
 
 	    // Check login status using login service.
 	    LoginServiceAsync loginService = GWT.create(LoginService.class);
 	    loginService.login(GWT.getHostPageBaseURL(), new AsyncCallback<LoginInfo>() {
 	      public void onFailure(Throwable error) {
 	    	  handleError(error);
 	      }
 	      public void onSuccess(LoginInfo result) {
 	    	  Login.setLogin(result);
 	        loadApp();
 	      }
 	    });
 	      
 	}
 	
 	private void loadApp(){
 		RootPanel.get().add(app);
 		
 		app.setLinks(getLinksBar());
 		
 		setTitle();
 		app.setContent(new LiveEditPanel("welcome"));
 		app.setContentTitle(new Hyperlink("Home", "home"));
 				
 	}
 
 	private List<Widget> getLinksBar() {
 		ArrayList<Widget> links = new ArrayList<Widget>();
 		if (! Login.isLoggedIn()){
 			signInLink.setHref(Login.getLoginUrl());
 			links.add(signInLink);
 		} else {
 			signOutLink.setHref(Login.getLogoutUrl());
 			links.add(signOutLink);
 		}
 		return links;
 	}
 
 	private void setTitle() {
 		String pageTitle = "<h1>Codsall Arts Festival 2010</h1><h2>Sat, 6th March thru Sun, 21st March</h2>";
 
 		// Add the title and some images to the title bar
 		HorizontalPanel titlePanel = new HorizontalPanel();
 		titlePanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
 		titlePanel.add(new Image("images/jimmyThumb.jpg"));
 		titlePanel.add(new HTML(pageTitle));
 		app.setTitleWidget(titlePanel);
 //
 	}
 	private void handleError(Throwable error) {
 	    Window.alert(error.getMessage());
 	   
 	  }
 }
