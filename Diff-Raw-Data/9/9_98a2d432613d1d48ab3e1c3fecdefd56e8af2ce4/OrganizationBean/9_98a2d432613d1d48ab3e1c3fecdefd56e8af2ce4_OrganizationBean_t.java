 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package roman.cwk.managedbeans;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.ejb.EJB;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIInput;
 import javax.faces.context.FacesContext;
 import javax.servlet.http.HttpSession;
 import roman.cwk.bus.BusinessException;
 import roman.cwk.bus.OrganizationService;
 import roman.cwk.entity.Organization;
 import roman.cwk.entity.UserGroup;
 import roman.cwk.sessionbeans.OrganizationFacade;
 import roman.cwk.sessionbeans.UserGroupFacade;
 
 /**
  *
  * @author Roman Macor
  */
 @ManagedBean
 //@RequestScoped
 @SessionScoped
 public class OrganizationBean extends BaseBean {
 
     //all the organization will fall to this group
     private Organization organization;
     private String repeatPassword;
     private String currentPassword;
     @EJB
     private OrganizationService ejbOrganizationService;
 
     /**
      * Creates a new instance of OrganizationBean
      */
     public OrganizationBean() {
     }
 
     public String getCurrentPassword() {
         return currentPassword;
     }
 
     public void setCurrentPassword(String currentPassword) {
         this.currentPassword = currentPassword;
     }
 
     public Organization getOrganization() {
         if (organization == null) {
             organization = new Organization();
         }
         return organization;
     }
 
     public void setOrganization(Organization organization) {
         this.organization = organization;
     }
 
     public String getRepeatPassword() {
         return repeatPassword;
     }
 
     public void setRepeatPassword(String repeatPassword) {
         this.repeatPassword = repeatPassword;
     }
 
     public String register() {
         try {
             ejbOrganizationService.register(organization);
         } catch (BusinessException ex) {
             Logger.getLogger(OrganizationBean.class.getName()).log(Level.SEVERE, null, ex);
             addErrorMessage(ex.getMessage());
             return "";
         }
         return "confirmation";
     }
 
     public String prepareEditOrganization() {
 
         String organizationName = this.getLogedUsername();
         try {
             organization = ejbOrganizationService.prepareEditOrganization(organizationName);
         } catch (BusinessException ex) {
             Logger.getLogger(OrganizationBean.class.getName()).log(Level.SEVERE, null, ex);
             addErrorMessage(ex.getMessage());
             return "";
         }
         return "/project/edit_organization";
     }
 
     public String edit() {
 
         organization = ejbOrganizationService.edit(organization);
        
         return "/project/index";
     }
 
     public String changePassword() {
         try {
             ejbOrganizationService.changePassword(currentPassword, organization);
         } catch (BusinessException ex) {
             Logger.getLogger(OrganizationBean.class.getName()).log(Level.SEVERE, null, ex);
             addErrorMessage(ex.getMessage());
             return "";
         }
         return "/project/index";
     }
 
     public void checkPasswordsEquality(FacesContext context,
             UIComponent toValidate, Object value) {
 
         UIInput passwordComponent = (UIInput) toValidate.getAttributes().get("password");
         String password = (String) passwordComponent.getValue();
 
         String repeatedPassword = (String) value;
 
         if (!repeatedPassword.equals(password)) {
             ((UIInput) toValidate).setValid(false);
 
             context.addMessage(toValidate.getClientId(context),
                     new FacesMessage("Passwords don't match."));
         }
     }
 }
