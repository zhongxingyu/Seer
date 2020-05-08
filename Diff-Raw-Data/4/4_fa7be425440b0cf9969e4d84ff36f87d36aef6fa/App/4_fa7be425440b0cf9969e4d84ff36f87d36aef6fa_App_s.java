 package com.arvue.apps.projectx;
 
 import com.vaadin.addon.touchkit.ui.TouchKitWindow;
 import com.vaadin.addon.touchkit.ui.TouchKitApplication;
 import com.vaadin.addon.touchkit.ui.NavigationManager;
 
 public class App extends TouchKitApplication {
 
     private TouchKitWindow main;
 
     public void init() {
         main = new TouchKitWindow();
         setMainWindow(main);
     }
 
     public void onBrowserDetailsReady() {
        main.setContent(new NavigationManager(new Main()));
     }
 }
