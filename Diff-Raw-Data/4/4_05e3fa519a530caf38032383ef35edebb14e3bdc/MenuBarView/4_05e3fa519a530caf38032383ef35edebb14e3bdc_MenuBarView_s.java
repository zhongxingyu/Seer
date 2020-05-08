 package ru.sgu.csit.inoc.deansoffice.webui.gxt.students.client.mvc.views;
 
 import com.extjs.gxt.ui.client.event.EventType;
 import com.extjs.gxt.ui.client.event.MenuEvent;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.mvc.AppEvent;
 import com.extjs.gxt.ui.client.mvc.Dispatcher;
 import com.extjs.gxt.ui.client.mvc.View;
 import com.extjs.gxt.ui.client.widget.menu.Menu;
 import com.extjs.gxt.ui.client.widget.menu.MenuBar;
 import com.extjs.gxt.ui.client.widget.menu.MenuBarItem;
 import com.extjs.gxt.ui.client.widget.menu.MenuItem;
 import com.google.gwt.user.client.Window;
 import ru.sgu.csit.inoc.deansoffice.webui.gxt.common.shared.mvc.events.CommonEvents;
 import ru.sgu.csit.inoc.deansoffice.webui.gxt.students.client.mvc.controllers.MenuBarController;
 import ru.sgu.csit.inoc.deansoffice.webui.gxt.students.client.mvc.events.StudentEvents;
 
 /**
  * User: Khurtin Denis (KhurtinDN@gmail.com)
  * Date: 1/28/11
  * Time: 8:26 AM
  */
 public class MenuBarView extends View {
     private MenuBar menuBar;
 
     public MenuBarView(MenuBarController controller) {
         super(controller);
     }
 
     @Override
     protected void initialize() {
         super.initialize();
 
         Menu fileMenu = new Menu();
 
         fileMenu.add(new MenuItem("Выход", new SelectionListener<MenuEvent>() {
             @Override
             public void componentSelected(MenuEvent ce) {
                Window.open("j_spring_security_logout", "_self", "");
             }
         }));
 
         Menu referenceMenu = new Menu();
 
         referenceMenu.add(new MenuItem("Очередь справок", new SelectionListener<MenuEvent>() {
             @Override
             public void componentSelected(MenuEvent ce) {
                 Dispatcher.forwardEvent(StudentEvents.ReferenceQueueCall);
             }
         }));
 
         Menu orderMenu = new Menu();
 
         orderMenu.add(new MenuItem("Создать новый приказ", new SelectionListener<MenuEvent>() {
             @Override
             public void componentSelected(MenuEvent ce) {
                 Dispatcher.forwardEvent(StudentEvents.AddNewOrderCall);
             }
         }));
 
         orderMenu.add(new MenuItem("Очередь приказов", new SelectionListener<MenuEvent>() {
             @Override
             public void componentSelected(MenuEvent ce) {
                 Dispatcher.forwardEvent(StudentEvents.OrderQueueCall);
             }
         }));
 
         Menu helpMenu = new Menu();
 
         helpMenu.add(new MenuItem("Справка", new SelectionListener<MenuEvent>() {
             @Override
             public void componentSelected(MenuEvent ce) {
                 Dispatcher.forwardEvent(CommonEvents.Info, "Справочная информация находится в разработке.");
             }
         }));
 
         menuBar = new MenuBar();
         menuBar.add(new MenuBarItem("Файл", fileMenu));
         menuBar.add(new MenuBarItem("Справки", referenceMenu));
         menuBar.add(new MenuBarItem("Приказы", orderMenu));
         menuBar.add(new MenuBarItem("Помощь", helpMenu));
     }
 
     @Override
     protected void handleEvent(AppEvent event) {
         EventType eventType = event.getType();
 
         if (eventType.equals(CommonEvents.Init)) {
             onInit();
         }
     }
 
     private void onInit() {
         Dispatcher.forwardEvent(StudentEvents.MenuBarReady, menuBar);
     }
 }
