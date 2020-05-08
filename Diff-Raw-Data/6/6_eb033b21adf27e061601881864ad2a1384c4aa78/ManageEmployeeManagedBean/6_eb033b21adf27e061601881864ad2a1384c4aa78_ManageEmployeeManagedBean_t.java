 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package commonInfrastructure.ERMS.managedbean;
 
 import ERMS.entity.EmployeeEntity;
 import ERMS.entity.RoleEntity;
 import ERMS.session.EmployeeSessionBean;
 import ERMS.session.MessageSessionBean;
 import ERMS.session.RoleSessionBean;
 import Exception.ExistException;
 import java.util.ArrayList;
 import java.util.List;
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
 public class ManageEmployeeManagedBean {
 
     @EJB
     private EmployeeSessionBean em;
     @EJB
     private RoleSessionBean rm;
     @EJB
     private MessageSessionBean mm;
     private EmployeeEntity selectedEmployee;
     private boolean editMode;
     private String id;
     private List<String> selectedRoles;
 
     /**
      * Creates a new instance of ManageEmployeeManagedBean
      */
     public ManageEmployeeManagedBean() {
         selectedEmployee = new EmployeeEntity();
     }
 
     public boolean isEditMode() {
         return editMode;
     }
 
     public List<EmployeeEntity> getEmployees() throws ExistException {
         return em.getAllEmployees();
     }
 
     public void deleteEmployee(ActionEvent event) throws ExistException {
       
        System.err.println("Delete Employee:" + selectedEmployee.getEmployeeId());
        
        getEm().removeEmployee(selectedEmployee.getEmployeeId());
     }
 
     public void saveChanges(ActionEvent event) {
         selectedEmployee.setRoles(new ArrayList<RoleEntity>());
         pushToRoles(selectedEmployee);
         em.updateEmployee(selectedEmployee);
         FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Changes saved.", ""));
     }
 
     /**
      * @return the em
      */
     public EmployeeSessionBean getEm() {
         return em;
     }
 
     /**
      * @param em the em to set
      */
     public void setEm(EmployeeSessionBean em) {
         this.em = em;
     }
 
     /**
      * @return the selectedEmployee
      */
     public EmployeeEntity getSelectedEmployee() {
         return selectedEmployee;
     }
 
     /**
      * @param selectedEmployee the selectedEmployee to set
      */
     public void setSelectedEmployee(EmployeeEntity selectedEmployee) {
         this.selectedEmployee = selectedEmployee;
     }
 
     /**
      * @param editMode the editMode to set
      */
     public void setEditMode(boolean editMode) {
         this.editMode = editMode;
     }
 
     /**
      * @return the id
      */
     public String getId() {
         return id;
     }
 
     /**
      * @param id the id to set
      */
     public void setId(String id) {
         this.id = id;
     }
 
     
     public String toSentence(List<RoleEntity> roles) {
         String output = null;
         StringBuffer sb = new StringBuffer();
         int i = 0;
         while (i < roles.size()) {
             sb.append(roles.get(i).getRoleName());
             sb.append(";");
             System.out.println(roles.get(i).getRoleName());
             i++;
         }
         output = sb.toString();
         return output;
     }
 
     public List<String> getSelectedRoles() {
         return selectedRoles;
     }
 
     public void setSelectedRoles(List<String> selectedRoles) {
         this.selectedRoles = selectedRoles;
     }
 
     public void pushToRoles(EmployeeEntity selectedEmployee) {
         int i = 0;
         int id;
         id = Integer.valueOf(selectedRoles.get(i));
         selectedEmployee.getRoles().add(getRm().getRole(id));
         System.out.println(selectedRoles.get(i));
 
         while (i < (selectedRoles.size() - 1)) {
             i++;
             id = Integer.valueOf(selectedRoles.get(i));
             selectedEmployee.getRoles().add(getRm().getRole(id));
             System.out.println(selectedRoles.get(i));
         }
 
         HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
         String senderId = (String) request.getSession().getAttribute("userId");
 
         System.out.println(senderId);
 
         List<String> rId = new ArrayList<String>();
 
         rId.add(selectedEmployee.getEmployeeId());
 
         StringBuffer sb = new StringBuffer();
         String msg;
         List<RoleEntity> roles = new ArrayList<RoleEntity>();
 
         roles = selectedEmployee.getRoles();
 
         System.out.println("Msg initiated");
 
         sb.append("Your roles have been modified to: ");
 
         int n = 0;
 
         while (n < selectedRoles.size()) {
             sb.append(rm.getRole(Integer.valueOf(selectedRoles.get(n))).getRoleName());
             sb.append(";");
             n++;
         }
 
         /*for(RoleEntity r: roles){
          sb.append(r.getRoleName());
          sb.append(";");
          }*/
 
         msg = sb.toString();
 
         System.out.println(msg);
 
         mm.addSystemMessage(senderId, rId, "Role Changed", msg, "Broadcast");
 
         System.out.println("message sent");
     }
 
     public RoleSessionBean getRm() {
         return rm;
     }
 
     public void setRm(RoleSessionBean rm) {
         this.rm = rm;
     }
 
     public MessageSessionBean getMm() {
         return mm;
     }
 
     public void setMm(MessageSessionBean mm) {
         this.mm = mm;
     }
 }
