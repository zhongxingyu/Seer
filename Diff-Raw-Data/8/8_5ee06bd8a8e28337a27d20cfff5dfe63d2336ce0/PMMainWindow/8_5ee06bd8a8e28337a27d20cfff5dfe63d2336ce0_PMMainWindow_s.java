 package org.jpos.ee.pm.vaadin.components;
 
 import com.vaadin.terminal.ExternalResource;
 import com.vaadin.ui.*;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.MenuBar.Command;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import org.jpos.ee.pm.core.*;
 import org.jpos.ee.pm.menu.*;
 import org.jpos.ee.pm.vaadin.PMVaadinApp;
 
 /**
  *
  * @author jpaoletti
  */
 public class PMMainWindow extends Window {
 
     private VerticalLayout mainExpand;
     private HorizontalLayout topBar;
     private HorizontalLayout mainBar;
     private HorizontalLayout bottomBar;
     private MenuBar mainMenu;
 
     public PMMainWindow() throws PMException {
         mainExpand = new VerticalLayout();
         mainExpand.setHeight("100%");
         mainExpand.setWidth("100%");
         setContent(mainExpand);
         setSizeFull();
         mainExpand.setSizeFull();
         setCaption(PresentationManager.getMessage(PresentationManager.getPm().getTitle()));
         setTheme(PresentationManager.getPm().getTemplate());
 
         configureTopBar();
         configureMainBar();
         configureBottomBar();
     }
 
     protected final void configureTopBar() {
         topBar = new HorizontalLayout();
         topBar.setHeight("50px");
         topBar.setWidth("100%");
         topBar.setStyleName("topbar");
 
         final Component logo = createLogo();
         topBar.addComponent(logo);
         topBar.setComponentAlignment(logo, Alignment.MIDDLE_LEFT);
 
         if (PresentationManager.getPm().isLoginRequired()) {
             final HorizontalLayout usrbar = new HorizontalLayout();
 
             //We need 3 things, username, profile link and logout link
             final Label uname = new Label(getPMApplication().getPMSession().getUser().getName());
             final Link profile = new Link(PresentationManager.getMessage("user.profile"), null);
             final Link logout = new Link(PresentationManager.getMessage("logout"), null);
 
             usrbar.addComponent(uname);
             usrbar.addComponent(profile);
             usrbar.addComponent(logout);
 
             topBar.addComponent(usrbar);
             topBar.setComponentAlignment(usrbar, Alignment.BOTTOM_LEFT);
         }
 
         mainExpand.addComponent(topBar);
         mainExpand.setComponentAlignment(topBar, Alignment.TOP_CENTER);
     }
 
     protected final void configureMainBar() {
         mainMenu = new MenuBar();
         mainExpand.addComponent(mainMenu);
 
         mainBar = new HorizontalLayout();
         mainExpand.addComponent(mainBar);
         setMainScreen(mainBar);
     }
 
     protected final void configureBottomBar() {
         bottomBar = new HorizontalLayout();
         bottomBar.setHeight("30px");
         bottomBar.setWidth("100%");
         bottomBar.setStyleName("topbar");
 
         final Label label = new Label("v" + PresentationManager.getPm().getAppversion());
         bottomBar.addComponent(label);
         bottomBar.setComponentAlignment(label, Alignment.MIDDLE_LEFT);
 
         final Label label2 = new Label(
                 PresentationManager.getMessage("footer.copyright.pre")
                 + ((new SimpleDateFormat(" yyyy ")).format(new Date()))
                 + PresentationManager.getMessage(PresentationManager.getPm().getCopyright())
                 + PresentationManager.getMessage("footer.copyright.post"));
         bottomBar.addComponent(label2);
         bottomBar.setComponentAlignment(label2, Alignment.MIDDLE_CENTER);
 
         mainExpand.addComponent(bottomBar);
         mainExpand.setComponentAlignment(bottomBar, Alignment.BOTTOM_CENTER);
 
         final Link l = new Link();
         l.setResource(new ExternalResource("mailto:" + PresentationManager.getPm().getContact()));
         l.setCaption(PresentationManager.getMessage("contact"));
         bottomBar.addComponent(l);
         bottomBar.setComponentAlignment(l, Alignment.MIDDLE_RIGHT);
 
     }
 
     public void init() throws PMException {
         // Main top/expanded-bottom layout
         Menu menu = MenuSupport.getMenu(getSession().getUser().getPermissionList());
         getSession().setMenu(menu);
         recMenu(getSession().getMenu(), null);
     }
 
     @Override
     public boolean handleError(ComponentErrorEvent error) {
         PresentationManager.getPm().error(error);
         return super.handleError(error);
     }
 
     public Layout getMainExpand() {
         return mainExpand;
     }
 
     public MenuBar getMainMenu() {
         return mainMenu;
     }
 
     private void recMenu(Menu menu, MenuBar.MenuItem mitem) {
         final String caption = PresentationManager.getMessage(menu.getText());
         if (menu instanceof MenuItem) {
             MenuItem item = (MenuItem) menu;
             MenuBar.Command defcommand = new MenuBar.Command() {
 
                 public void menuSelected(MenuBar.MenuItem selection) {
                     System.out.println(selection.getText());
                 }
             };
             Command command = (Command) item.getLocation().build(item, this, item.getLocation_value());
             if (command == null) {
                 command = defcommand;
             }
             if (mitem == null) {
                 mainMenu.addItem(caption, command);
             } else {
                 mitem.addItem(caption, command);
             }
         } else {
             MenuList list = (MenuList) menu;
             MenuBar.MenuItem subitem = mitem;
             if (caption != null) {
                 if (subitem != null) {
                     subitem = subitem.addItem(caption, null);
                 } else {
                     subitem = mainMenu.addItem(caption, null);
                 }
             }
             for (Menu m : list.getSubmenus()) {
                 recMenu(m, subitem);
             }
         }
     }
 
     private PMSession getSession() {
         return PresentationManager.getPm().getSession(getPMApplication().getSessionId());
     }
 
     public PMVaadinApp getPMApplication() {
         return (PMVaadinApp) getApplication();
     }
 
     private Component createLogo() {
         Button logo = new NativeButton("", new Button.ClickListener() {
 
             public void buttonClick(ClickEvent event) {
                 System.out.println("GO TO INDEX");
             }
         });
         logo.setDescription("↶ Home");
         logo.addStyleName("logo");
         return logo;
     }
 
     public void setMainScreen(HorizontalLayout l) {
         mainExpand.replaceComponent(mainBar, l);
         mainBar = l;
         mainExpand.setExpandRatio(l, 1);
     }
 }
