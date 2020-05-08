 /*
  * MSc(Biomedical Informatics) Project
  *
  * Development and Implementation of a Web-based Combined Data Repository of Genealogical, Clinical, Laboratory and Genetic Data
  * and
  * a Set of Related Tools
  */
 package gov.health.bean;
 
 import java.io.Serializable;
 import javax.annotation.PostConstruct;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import org.primefaces.component.submenu.Submenu;
 import org.primefaces.model.DefaultMenuModel;
 import org.primefaces.model.MenuModel;
 import javax.faces.bean.ManagedProperty;
 import org.primefaces.component.menuitem.MenuItem;
 
 /**
  *
  * @author Dr. M. H. B. Ariyaratne, MBBS, PGIM Trainee for MSc(Biomedical
  * Informatics)
  */
 @ManagedBean
 @SessionScoped
 public class Menu implements Serializable {
 
     @ManagedProperty(value = "#{sessionController}")
     SessionController sessionController;
     MenuModel model;
     String temIx = "";
 
     private String getLabel(String key) {
         return new MessageProvider().getValue(key);
     }
 
     public Menu() {
     }
 
     public SessionController getSessionController() {
         return sessionController;
     }
 
     public void setSessionController(SessionController sessionController) {
         this.sessionController = sessionController;
     }
 
     public void createMenu() {
         model = new DefaultMenuModel();
 
         if (sessionController.isLogged()==false||sessionController.getLoggedUser()==null){
             return;
         }
         
         MenuItem item;
 
         item = new MenuItem();
         item.setValue("Home");
         item.setUrl("index.xhtml");
         model.addMenuItem(item);
 
 
 
         // View data
         model.addSubmenu(viewData());
 
         // Upload File
         model.addSubmenu(uploadData());
 
         // Metadata management by sysAdmin
         if (sessionController.isSysAdmin()) {
             model.addSubmenu(sysAdminMetadata());
         }
         // Metadata management by sysAdmin
         if (sessionController.isInsAdmin()) {
             model.addSubmenu(insAdminMetadata());
         }
         // Admin mene {admin management)
         if (sessionController.isInsAdmin() || sessionController.isSysAdmin()) {
             model.addSubmenu(adminSubmenu());
         }
         // User menu {Edit Password, Change Profile}
         model.addSubmenu(userSubmenu());
     }
 
     private Submenu sysAdminMetadata() {
 
         Submenu submenu = new Submenu();
         submenu.setLabel("Edit Metadata");
 
         MenuItem item;
 
 
 
         item = new MenuItem();
         item.setValue("Institution Types");
         item.setUrl("institution_type.xhtml");
         submenu.getChildren().add(item);
 
         item = new MenuItem();
         item.setValue("Institutions");
         item.setUrl("institutions.xhtml");
         submenu.getChildren().add(item);
 
         item = new MenuItem();
         item.setValue("Institution Sets");
         item.setUrl("institution_set.xhtml");
         submenu.getChildren().add(item);
 
         item = new MenuItem();
         item.setValue("Designations");
         item.setUrl("designation.xhtml");
         submenu.getChildren().add(item);
 
         item = new MenuItem();
         item.setValue("Manage (Designation) Service Type");
         item.setUrl("designation_category.xhtml");
         submenu.getChildren().add(item);
 
 
 
         return submenu;
 
     }
 
     
     private Submenu insAdminMetadata() {
 
         Submenu submenu = new Submenu();
         submenu.setLabel("Edit Metadata");
 
         MenuItem item;
 
         item = new MenuItem();
         item.setValue("Institution Sets");
         item.setUrl("institution_set.xhtml");
         submenu.getChildren().add(item);
 
         return submenu;
 
     }
 
     
     private Submenu viewData() {
 
         Submenu submenu;
 
         MenuItem item;
 
         submenu = new Submenu();
         submenu.setLabel("View Data");
 
         // for Super user & System admins
 //        if (sessionController.loggedUser.isSystemAdmin() || sessionController.loggedUser.isSuperUser()) {
 //            item = new MenuItem();
 //            item.setValue("Full Summary");
 //            item.setUrl("full_summary.xhtml");
 //            submenu.getChildren().add(item);
 //        }
 
         // for all 4 categories
         item = new MenuItem();
         item.setValue("Institution Summary");
         item.setUrl("inst_summary.xhtml");
         submenu.getChildren().add(item);
 
         item = new MenuItem();
         item.setValue("Records wihout NIC");
         item.setUrl("records_without_nic.xhtml");
         submenu.getChildren().add(item);
 
         item = new MenuItem();
         item.setValue("Records wihout Designations");
         item.setUrl("records_without_designation.xhtml");
         submenu.getChildren().add(item);
 
 
         return submenu;
     }
 
     private Submenu userSubmenu() {
         Submenu submenu = new Submenu();
         submenu.setLabel("User");
 
         MenuItem item;
 
         item = new MenuItem();
         item.setValue("Change Password");
         item.setUrl("change_password.xhtml");
         submenu.getChildren().add(item);
 
         item = new MenuItem();
         item.setValue("Preferances");
         item.setUrl("under_construction.xhtml");
         submenu.getChildren().add(item);
 
         return submenu;
     }
 
     private Submenu uploadData() {
 
         Submenu submenu;
 
         MenuItem item;
 
         submenu = new Submenu();
         submenu.setLabel("Upload");
 
         item = new MenuItem();
         item.setValue("Upload Payroll Database");
         item.setUrl("upload_dbf.xhtml");
         submenu.getChildren().add(item);
 
         return submenu;
     }
 
     private Submenu adminSubmenu() {
         Submenu submenu;
 
         MenuItem item;
 
         submenu = new Submenu();
         submenu.setLabel("Admin");
 
         item = new MenuItem();
         item.setValue("Add Account");
         item.setUrl("register_user.xhtml");
         submenu.getChildren().add(item);
 
         item = new MenuItem();
         item.setValue("Manage Accounts");
         item.setUrl("manage_users.xhtml");
         submenu.getChildren().add(item);
 
         return submenu;
     }
 
     public MenuModel getModel() {
         return model;
     }
 
     public void setModel(MenuModel model) {
         this.model = model;
     }
 
     @PostConstruct
     public void init() {
         try {
             createMenu();
         } catch (Exception e) {
             System.out.println("Error in init method. It is " + e.getMessage());
         }
     }
 }
