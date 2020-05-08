 package be.cegeka.rsvz;
 
 import be.cegeka.rsvz.validator.Insz;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 import javax.faces.event.AjaxBehaviorEvent;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Past;
 import javax.validation.constraints.Size;
 import java.io.Serializable;
 import java.util.Date;
 
 @ManagedBean(name = "richBean")
 @SessionScoped
 public class RichBean implements Serializable {
 
     private static final Logger LOG = LoggerFactory.getLogger(RichBean.class);
     @NotNull
     @Size(min = 4, max = 10, message = "{must-not-be-empty}")
     private String firstName;
     private String lastName;
     @Past(message = "{date-must-be-in-the-past}")
     private Date calendar;
     private String text;
 
     @Insz(message = "{insz-not-valid}")
     private String insz;
 
     public void validateFirstName(AjaxBehaviorEvent event) {
         System.out.println("In validateFirstName ActionListener.");
         System.out.println("First Name: " + getFirstName());
         System.out.println("LastName: " + getLastName());
         FacesMessage msg = new FacesMessage("Succesful is uploaded.");
         FacesContext.getCurrentInstance().addMessage(event.getComponent().getClientId(), msg);
     }
 
     public String getFirstName() {
         return firstName;
     }
 
     public void setFirstName(String firstName) {
         this.firstName = firstName;
     }
 
     public String getLastName() {
         return lastName;
     }
 
     public void setLastName(String lastName) {
         this.lastName = lastName;
     }
 
 
     public Date getCalendar() {
         return calendar;
     }
 
     public void setCalendar(Date calendar) {
         this.calendar = calendar;
     }
 
     public String getText() {
         return text;
     }
 
     public void setText(String text) {
         this.text = text;
     }
 
     public String getInsz() {
         return insz;
     }
 
     public void setInsz(String insz) {
         this.insz = insz;
     }
 
    public void send(ActionEvent actionEvent) {
        LOG.debug("Send action triggered in [{}] phase on [{}] component", actionEvent.getPhaseId(), actionEvent.getComponent());
     }
 }
