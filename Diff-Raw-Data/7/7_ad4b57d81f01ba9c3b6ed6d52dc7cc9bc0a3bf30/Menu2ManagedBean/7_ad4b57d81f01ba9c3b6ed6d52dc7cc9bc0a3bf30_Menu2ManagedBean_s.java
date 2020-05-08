 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package commonInfrastructure.menu.managedbean;
 
 import ERMS.entity.EmployeeEntity;
 import ERMS.session.EmployeeSessionBean;
 import Exception.ExistException;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import javax.ejb.EJB;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 import javax.faces.context.FacesContext;
 import javax.servlet.http.HttpServletRequest;
 import org.primefaces.component.menuitem.MenuItem;
 import org.primefaces.component.submenu.Submenu;
 import org.primefaces.model.DefaultMenuModel;
 import org.primefaces.model.MenuModel;
 
 
 /**
  *
  * @author Ser3na
  */
 import javax.faces.application.NavigationHandler;
 
 @ManagedBean
 @ViewScoped
 public class Menu2ManagedBean implements Serializable {
 
     private MenuModel model;
     @EJB
     EmployeeSessionBean employee;
 
     public Menu2ManagedBean() {
     }
 
     public MenuModel getModel() throws ExistException {
         model = new DefaultMenuModel();
 
         HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
         Boolean isLogin = (Boolean) request.getSession().getAttribute("isLogin");
 
         if (isLogin == null) {
             isLogin = false;
         }
 
         if (isLogin == true) {
             String loginId = (String) request.getSession().getAttribute("userId");
             EmployeeEntity user = employee.getEmployeeById(loginId);
             List<String> userType = new ArrayList<String>();
 
             for (int i = 0; i < user.getRoles().size(); i++) {
                 userType.add(user.getRoles().get(i).getRoleName());
             }
 
             //First submenu
             Submenu submenu = new Submenu();
             MenuItem item = new MenuItem();
 
             if (userType.contains("SuperAdmin")) {
 
                 submenu = new Submenu();
                 submenu.setLabel("Account");
 
                 item = new MenuItem();
                 item.setValue("Create New Account");
                 item.setUrl("/accountManagement/addEmployee.xhtml");
                 item.setIcon("ui-icon ui-icon-plus");
                 submenu.getChildren().add(item);
 
                 item = new MenuItem();
                 item.setValue("Manage Employees");
                 item.setUrl("/accountManagement/manageEmployee.xhtml");
                 item.setIcon("ui-icon ui-icon-pencil");
                 submenu.getChildren().add(item);
 
                 model.addSubmenu(submenu);
 
                 submenu = new Submenu();
                 submenu.setLabel("Role");
                 submenu.setIcon("ui-icon ui-icon-star");
 
                 item = new MenuItem();
                 item.setValue("Create New Role");
                 item.setUrl("/accountManagement/addRole.xhtml");
                 item.setIcon("ui-icon ui-icon-plus");
                 submenu.getChildren().add(item);
 
                 item = new MenuItem();
                 item.setValue("Manage Roles");
                 item.setUrl("/accountManagement/manageRole.xhtml");
                 item.setIcon("ui-icon ui-icon-pencil");
                 submenu.getChildren().add(item);
 
                 model.addSubmenu(submenu);
 
                 submenu = new Submenu();
                 submenu.setLabel("Functionality");
                 submenu.setIcon("ui-icon ui-icon-wrench");
 
                 item = new MenuItem();
                 item.setValue("Create New Functionality");
                 item.setUrl("/accountManagement/addFunctionality.xhtml");
                 item.setIcon("ui-icon ui-icon-plus");
                 submenu.getChildren().add(item);
 
                 item = new MenuItem();
                 item.setValue("Manage Functionality");
                 item.setUrl("/accountManagement/manageFunctionality.xhtml");
                 item.setIcon("ui-icon ui-icon-pencil");
                 submenu.getChildren().add(item);
 
                 model.addSubmenu(submenu);
             }
             if (userType.contains("ACMSAdmin")) { //|| (userType.contains("ACMSFrontDesk")) || (userType.contains("ACMSRoomService"))) {
                 System.out.println("ACMSAdmin menu bar");
                 submenu = new Submenu();
                 submenu.setLabel("Hotel");
 
                 item = new MenuItem();
                 item.setValue("Front Desk");
                 item.setUrl("/acms/checkIncheckOut.xhtml");
                 item.setIcon("ui-icon ui-icon-suitcase");
                 submenu.getChildren().add(item);
 
                 item = new MenuItem();
                 item.setValue("Room Service");
                 item.setUrl("/acms/RoomService.xhtml");
                 item.setIcon("ui-icon ui-icon-script");
                 submenu.getChildren().add(item);
 
                 item = new MenuItem();
                 item.setValue("Overbooking Mgt");
                 item.setUrl("/acms/overbookingManagement.xhtml");
                 item.setIcon("ui-icon ui-icon-calculator");
                 submenu.getChildren().add(item);
 
                 item = new MenuItem();
                 item.setValue("log book");
                 item.setUrl("/acms/shiftLogBook.xhtml");
                 item.setIcon("ui-icon ui-icon-calculator");
                 submenu.getChildren().add(item);
 
                 model.addSubmenu(submenu);
             }
 
             /*             if (userType.contains("ACMSRoomService")){ //|| (userType.contains("ACMSFrontDesk")) || (userType.contains("ACMSRoomService"))) {
              System.out.println("ACMSRoomService menu bar");
              submenu = new Submenu();
              submenu.setLabel("Room Service");
              submenu.setIcon("ui-icon ui-icon-contact");
                 
              item = new MenuItem();
              item.setValue("Room Service");
              item.setUrl("/acms/RoomService.xhtml");
              submenu.getChildren().add(item);
                 
              model.addSubmenu(submenu);
              }
              */
             if (userType.contains("FBMSAdmin")) {
                 System.out.println("FBMSAdmin menu bar");
                 submenu = new Submenu();
                 submenu.setLabel("Restaurant");
                 submenu.setIcon("ui-icon ui-icon-contact");
 
                 item = new MenuItem();
                 item.setValue("Create Restaurant");
                 item.setUrl("/fbms/addRestaurant.xhtml");
                 item.setIcon("ui-icon ui-icon-plus");
                 submenu.getChildren().add(item);
 
                 item = new MenuItem();
                 item.setValue("Manage Restaurants");
                 item.setUrl("/fbms/manageRestaurant.xhtml");
                 item.setIcon("ui-icon ui-icon-pencil");
                 submenu.getChildren().add(item);
 
                 item = new MenuItem();
                 item.setValue("Manage Inventory");
                 item.setUrl("/fbms/addDish.xhtml");
                 item.setIcon("ui-icon ui-icon-pencil");
                 submenu.getChildren().add(item);
                 
                 model.addSubmenu(submenu);
             }
 
             if (userType.contains("ATMSAdmin")) {
                 System.out.println("ATMSAdmin menu bar");
                 submenu = new Submenu();
                 submenu.setLabel("Attractions");
                 submenu.setIcon("ui-icon ui-icon-contact");
 
                 item = new MenuItem();
                 item.setValue("Add Tickets");
                 item.setUrl("/atms/addTicket.xhtml");
                 item.setIcon("ui-icon ui-icon-search");
                 submenu.getChildren().add(item);
 
                 item = new MenuItem();
                 item.setValue("Manage Tickets");
                 item.setUrl("/atms/manageTicket.xhtml");
                 item.setIcon("ui-icon ui-icon-search");
                 submenu.getChildren().add(item);
 
 
                 model.addSubmenu(submenu);
             }
 
             if (userType.contains("CRMSAdmin")) {
                 System.out.println("CRMSAdmin menu bar");
                 submenu = new Submenu();
                 submenu.setLabel("VIP management");
                 submenu.setIcon("ui-icon ui-icon-contact");
 
                 item = new MenuItem();
                 item.setValue("Search VIP");
                 item.setUrl("/crms/searchVIP.xhtml");
                 item.setIcon("ui-icon ui-icon-search");
                 submenu.getChildren().add(item);
 
 
                 model.addSubmenu(submenu);
             }
             
             if (userType.contains("ESMSAdmin")) {
                 System.out.println("ESMSAdmin menu bar");
                 submenu = new Submenu();
                 submenu.setLabel("Show");
                 //submenu.setIcon("ui-icon ui-icon-contact");
 
                 item = new MenuItem();
                 item.setValue("Create new show");
                 item.setUrl("/esms/addShow.xhtml");
                 item.setIcon("ui-icon ui-icon-plus");
                 submenu.getChildren().add(item);
                 
 //                item = new MenuItem();
 //                item.setValue("Manage show");
 //                item.setUrl("/esms/manageShow.xhtml");
 //                item.setIcon("ui-icon ui-icon-pencil");
 //                submenu.getChildren().add(item);
 
                 model.addSubmenu(submenu);
             }
         }
         return model;
     }
 }
