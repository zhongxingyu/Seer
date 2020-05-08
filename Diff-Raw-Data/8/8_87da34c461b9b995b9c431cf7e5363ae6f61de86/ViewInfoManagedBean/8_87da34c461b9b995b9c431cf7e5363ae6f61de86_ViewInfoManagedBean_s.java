 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package commomInfrastructure.utility.managedbean;
 
 import ERMS.entity.EmployeeEntity;
 import ERMS.session.EmployeeSessionBean;
 import Exception.ExistException;
 import javax.ejb.EJB;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 import javax.servlet.http.HttpServletRequest;
 
 /**
  *
  * @author Ser3na
  */
 @ManagedBean
 @ViewScoped
 public class ViewInfoManagedBean {
     @EJB
     private EmployeeSessionBean em;
   
     private EmployeeEntity selectedEmployee;
     private boolean editMode;
     private String employeeId;
     
     /** Creates a new instance of ViewInfoManagedBean */
     public ViewInfoManagedBean() {
         selectedEmployee = new EmployeeEntity();
     }
     
     public boolean isEditMode() {  
         return editMode;  
     }
     
     public EmployeeEntity getEmployees() throws ExistException {
         HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
         String loginId = (String) request.getSession().getAttribute("userId");
        
         return em.getEmployeeById(loginId);
     }
 
     public void deleteEmployee(ActionEvent event) throws ExistException {
         setEmployeeId((String) event.getComponent().getAttributes().get("code1"));
         getEm().removeEmployee(getEmployeeId());
     }
     
     public void saveChanges(ActionEvent event) throws ExistException
     {
         em.updateEmployee(selectedEmployee);
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Changes saved.", ""));        
     }
 
     public EmployeeSessionBean getEm() {
         return em;
     }
 
     public void setEm(EmployeeSessionBean em) {
         this.em = em;
     }
 
     public EmployeeEntity getSelectedEmployee() {
         return selectedEmployee;
     }
 
     public void setSelectedEmployee(EmployeeEntity selectedEmployee) {
         this.selectedEmployee = selectedEmployee;
     }
 
     public void setEditMode(boolean editMode) {
         this.editMode = editMode;
     }
 
     public String getEmployeeId() {
         return employeeId;
     }
 
     public void setEmployeeId(String employeeId) {
         this.employeeId = employeeId;
     }
 }
