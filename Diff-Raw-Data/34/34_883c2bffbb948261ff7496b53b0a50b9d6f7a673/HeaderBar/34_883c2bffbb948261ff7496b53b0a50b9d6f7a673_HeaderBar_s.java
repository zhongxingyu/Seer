 package com.java.gwt.libertycinema.client;
 
 
 import java.lang.Throwable;
 
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.MenuBar;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 import com.java.gwt.libertycinema.client.BaseLayout;
 import com.java.gwt.libertycinema.client.services.LoginService;
 import com.java.gwt.libertycinema.client.services.LoginServiceAsync;
 import com.java.gwt.libertycinema.client.views.HomeLinkCommand;
 import com.java.gwt.libertycinema.client.views.ImageUpload;
 import com.java.gwt.libertycinema.client.views.StaticPages;
 import com.java.gwt.libertycinema.shared.LoginInfo;
 
 
 public class HeaderBar extends Composite {
 
     private BaseLayout baseLayout;
     private VerticalPanel headerPanel = new VerticalPanel();
     private Label mainLogo = new Label("Liberty Cinema - Mumbai");
     private final MenuBar mainMenu = new MenuBar();
     private MenuBar adminMenu = new MenuBar(true);
 
     public HeaderBar(BaseLayout baseLayout) {
         this.setBaseLayout(baseLayout);
         setUpMainMenu();
         headerPanel.add(mainLogo);
         headerPanel.add(mainMenu);
         mainLogo.setStyleName("topMainLogo");
         headerPanel.setStyleName("topNavTable");
         initWidget(headerPanel);
     }
 
     public VerticalPanel getHeaderPanel() {
         return headerPanel;
     }
 
     public void setHeaderPanel(VerticalPanel headerPanel) {
         this.headerPanel = headerPanel;
     }
 
     public Label getMainLogo() {
         return mainLogo;
     }
 
     public void setMainLogo(Label mainLogo) {
         this.mainLogo = mainLogo;
     }
 
     public MenuBar getMainMenu() {
         return mainMenu;
     }
 
     public void setAdminMenu(MenuBar adminMenu) {
         this.adminMenu = adminMenu;
     }
 
     private MenuBar getAdminMenu() {
         return adminMenu;
     }
 
     private void setUpMainMenu() {
         mainMenu.addItem("Home", new HomeLinkCommand(baseLayout));
         mainMenu.addItem("Gallery", new HomeLinkCommand(baseLayout));
         mainMenu.addItem("Testimonials", new HomeLinkCommand(baseLayout));
         mainMenu.addItem("Permissions", new HomeLinkCommand(baseLayout));
         mainMenu.addItem("Technical Details", new HomeLinkCommand(baseLayout));;
         mainMenu.addItem("Contact Us", new HomeLinkCommand(baseLayout));
         mainMenu.addItem("Login", new HomeLinkCommand(baseLayout));
 
         LoginServiceAsync loginService = GWT.create(LoginService.class);
         loginService.login(GWT.getHostPageBaseURL(), new AsyncCallback<LoginInfo> () {
                 public void onFailure(Throwable e) {
                     Window.alert("Ajax request failed, couldn't load admin module");
                 }
 
                 public void onSuccess(LoginInfo loginInfo) {
                     if(loginInfo.isAdminUser()) {
                         setUpAdminMenu();
                         mainMenu.addItem("Admin", getAdminMenu());
                     }
                 }
             });
     }
 
     private void setUpAdminMenu() {
         adminMenu.addItem("Static Pages", new StaticPages(baseLayout));
        // adminMenu.addItem("Images", new ImageUpload(baseLayout));
     }
 
     public BaseLayout getBaseLayout() {
         return baseLayout;
     }
 
     public void setBaseLayout(BaseLayout baseLayout) {
         this.baseLayout = baseLayout;
     }
 }
