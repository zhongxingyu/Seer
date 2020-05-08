 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package commonInfrastructure.ERMS.managedbean;
 
 import ERMS.entity.EmployeeEntity;
 import ERMS.entity.RoleEntity;
 import ERMS.session.EmailSessionBean;
 import ERMS.session.EmployeeSessionBean;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.UUID;
 import javax.annotation.PostConstruct;
 import javax.ejb.EJB;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 
 /**
  *
  * @author Cookie
  */
 @ManagedBean
 @RequestScoped
 public class AddEmployeeManagedBean implements Serializable {
     @EJB
     private EmailSessionBean emailSessionBean;
 
     @EJB
     private EmployeeSessionBean employeeSessionBean;
     
     private RoleEntity superAdmin;
     private EmployeeEntity employee;
     
     
     @PostConstruct
     public void init()
     {
         FacesContext.getCurrentInstance().getExternalContext().getSession(true);
     }
     
 
     public EmployeeEntity getEmployee() {
         return employee;
     }
 
     public void setEmployee(EmployeeEntity employee) {
         this.employee = employee;
     }
 
     /**
      * Creates a new instance of AddEmployeeManagedBean
      */
     public AddEmployeeManagedBean() {
     }
     
     public void saveAdmin(ActionEvent event) throws IOException {
         superAdmin = new RoleEntity();
         //add admin role
         superAdmin.setRoleId(0);
         superAdmin.setRoleName("SuperAdmin");
         superAdmin.addFunctionality(null);//functionalities to be discussed here!!!!!!!!!!!!!!!!!!!!!!!!!
        
         //add admin employee
         employee = new EmployeeEntity();
         employee.setEmployeePassword("0000");
         employee.setEmployeeId("0000");
         employee.setEmployeeName("SuperAdmin");
         employee.addRole(superAdmin);
         employee.setEmployeeEmail("admin.cir@gmail.com");
         employee.setIsFirstTimeLogin(false);
         employee.setEmployeeGender("male");
         try {
             System.out.println("Saving Admin....");
             employeeSessionBean.addEmployee(employee);
             System.out.println("Admin saved.....");
         } catch (Exception e) {
             FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Error occurs when adding admin", ""));
             return;
         }
         FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Admin saved.", ""));
         employee = new EmployeeEntity();
     } 
 
     public void saveNewEmployee(ActionEvent event) throws IOException {
        employee = new EmployeeEntity();
         String initialPwd = "";
         String uuid = UUID.randomUUID().toString();
         String[] sArray = uuid.split("-");
         initialPwd = sArray[0];
         employee.setEmployeePassword(initialPwd);
 //      employee.setEmployeePassword(EPasswordHashSessionBean.hashPassword(employee.getEmployeePassword())); 
         
         try {
             System.out.println("we are in SavaNewEmployee in managedbean");
             employeeSessionBean.addEmployee(employee);
             System.out.println("we are after employee in managedbean");
         } catch (Exception e) {
             FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Error occurs when adding new employee", ""));
             return;
         }
         FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "New Employee saved.", ""));
 //            emailSessionBean.emailInitialPassward(employee.getPersonalEmail(), initialPwd); //send email
         emailSessionBean.emailInitialPassward(employee.getEmployeeEmail(), initialPwd);
         employee = new EmployeeEntity();
     }
 }
