 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package commonInfrastructure.ERMS.managedbean;
 
 import ERMS.entity.EmployeeEntity;
 import ERMS.session.EPasswordHashSessionBean;
 import ERMS.session.EmailSessionBean;
import ERMS.session.EmployeeSessionBean;
 import Exception.ExistException;
 import java.io.IOException;
 import java.util.UUID;
 import javax.ejb.EJB;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 
 /**
  *
  * @author Ser3na
  */
 @ManagedBean
 @ViewScoped
 public class ResetPasswordManagedBean {
    
     @EJB
    private EmployeeSessionBean employeeSessionBean;
     @EJB
     private EPasswordHashSessionBean passowordHashSessionBean;
     @EJB
     private EmailSessionBean emailSessionBean;
     private String employeeId;
     private String employeeDob;
    
     private EmployeeEntity employee;
     private String securityQuestion;
     private String answer;
 
     public ResetPasswordManagedBean() {
     }
 
     public void doVerify(ActionEvent event) throws IOException, ExistException {
        employee = employeeSessionBean.getEmployeeById(employeeId);
         System.out.println(employee.getEmployeeId());
         if (employee == null) {
             FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Invalid UserName", ""));
 
         } else {
 
             if (employee.getEmployeeDob().toString().equals(employeeDob) && employee.getSecurityQuestion().equals(securityQuestion) && employee.getAnswer().equals(answer)) {
                 //send email to private email
                 String uuid = UUID.randomUUID().toString();
                 String[] sArray = uuid.split("-");
                 String initialPwd = sArray[0];
                 employee.setEmployeePassword(initialPwd);
                 employee.setEmployeePassword(passowordHashSessionBean.hashPassword(employee.getEmployeePassword()));
                 employee.setIsFirstTimeLogin(true);
                employeeSessionBean.updateEmployee(employee);
                 emailSessionBean.emailInitialPassward(employee.getEmployeeEmail(), initialPwd); //send email
                FacesContext.getCurrentInstance().getExternalContext().redirect("resetPasswordResult.xhtml");
             } else {
                 FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Verification Failed.", ""));
             }
         }
     }
 
    public EmployeeSessionBean getEmployeeManagerSessionBean() {
        return employeeSessionBean;
     }
 
    public void setEmployeeManagerSessionRemote(EmployeeSessionBean employeeManagerSessionRemote) {
        this.employeeSessionBean = employeeManagerSessionRemote;
     }
 
     public void retrieveEmployee() throws ExistException {
        employee = employeeSessionBean.getEmployeeById(employeeId);
     }
 
     public String getEmployeeId() {
         return employeeId;
     }
 
     public void setEmployeeId(String employeeId) {
         this.employeeId = employeeId;
     }
 
     public String getEmployeeDob() {
         return employeeDob;
     }
 
     public void setEmployeeDob(String employeeDob) {
         this.employeeDob = employeeDob;
     }
 
     public EmployeeEntity getEmployee() {
         return employee;
     }
 
     public void setEmployee(EmployeeEntity employee) {
         this.employee = employee;
     }
 
     public String getSecurityQuestion() {
         return securityQuestion;
     }
 
     public void setSecurityQuestion(String securityQuestion) {
         this.securityQuestion = securityQuestion;
     }
 
     public String getAnswer() {
         return answer;
     }
 
     public void setAnswer(String answer) {
         this.answer = answer;
     }
 }
