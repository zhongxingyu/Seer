 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package BB;
 
 import EJB.WorkerRegistry;
 import Model.Person;
 import Model.Worker;
 import java.io.Serializable;
 import java.util.List;
 import javax.ejb.EJB;
 import javax.enterprise.context.SessionScoped;
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.inject.Named;
 import org.primefaces.event.ToggleEvent;
 
 /**
  *
  * @author lisastenberg
  */
 @Named("tutorBean")
 @SessionScoped
 public class TutorBean implements Serializable{
     
     @EJB
     WorkerRegistry reg;
     
     private Worker selectedTutor;
     
 
     
     
     public TutorBean() {
     }
 
     public Worker getSelectedTutor() {
         return selectedTutor;
     }
     
     public void setSelectedTutor(Worker tutor) {
         selectedTutor = tutor;
 	System.out.println("------ SET SELECTED TUTOR -------"+ tutor);
     }
    
     public void removeTutor(Long id) {
 	reg.remove(id);
 	System.out.println("------ REMOVED TUTOR -------"+ id);
     }
     
     public List<Worker> getTutors() {
         return reg.getRange(0, reg.getCount());
     }
     
     public void onRowToggle(ToggleEvent event) {  
         FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,  
                                             "Row State " + event.getVisibility(),  
                                             "Name:" + ((Worker) event.getData()).getName());  
           
         FacesContext.getCurrentInstance().addMessage(null, msg);  
     } 
 }
