 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package BB;
 
 // import com.mycompany.ove.model.OveScheduleEvent;
 import Model.School;
 import EJB.SchoolRegistry;
 import EJB.SessionRegistry;
 import Model.ScheduleEvent;
 import Model.Session;
 import Model.Worker;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 import javax.annotation.PostConstruct;
 
 import javax.enterprise.context.RequestScoped;
 
 import javax.ejb.EJB;
 
 import javax.enterprise.context.SessionScoped;
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 import javax.inject.Named;
 import org.primefaces.event.ScheduleEntryMoveEvent;
 import org.primefaces.event.ScheduleEntryResizeEvent;
 import org.primefaces.event.SelectEvent;
 import org.primefaces.model.DefaultScheduleEvent;
 import org.primefaces.model.DefaultScheduleModel;
 
 
 import org.primefaces.model.ScheduleModel;
 
 /**
  *
  * @author Gustav
  */
 @Named("scheduleBean")
 @SessionScoped
 public class ScheduleBean implements Serializable
 {
     @EJB
     private SchoolRegistry reg;
     
     @EJB
     private SessionRegistry sesReg;
     private ScheduleModel eventModel; 
     private List<School> schoolList;
 
       
     //Overide this to handle events,   comments and time 
     private ScheduleEvent event = new ScheduleEvent();
    
     //Create all sessions for all schools in this list
    @PostConstruct
    public void init()
    {
        eventModel = new DefaultScheduleModel();
        loadModel();
    }
     
     /**
      * 
      * @return the eventModel 
      */
     public ScheduleModel getEventModel() {  
         return eventModel;  
     }  
 
     public ScheduleEvent getEvent() {  
         return event;  
     }  
   
     public void setEvent(ScheduleEvent event) {  
         this.event = event;  
     }  
       
     public void addEvent(ActionEvent actionEvent) {  
         if(event.getId() == null)  
         {
         
             School s = reg.getByName(event.getSchoolName()).get(0);
             List<Session> sessionList = s.getSchedule().getSessions();
             sessionList.add(new Session(event.getStartDate(), event.getEndDate(), event.getNumberOfStudents(), event.getWorkerList(), event.getNotation()));
             s = reg.update(s);
             for(Session ses : s.getSchedule().getSessions())
             {
                 if(ses.getStartTime().compareTo(event.getStartDate())==0 
                         && ses.getEndTime().compareTo(event.getEndDate())==0 
                         && s.getName().equals(event.getSchoolName()))
                 {
                     event.setModelId(ses.getId());
                 }
             }
             eventModel.addEvent(event);
   
         }   
         else  {
             eventModel.updateEvent(event);  
             School s = reg.getByName(event.getSchoolName()).get(0);
             sesReg.update(new Session(event.getModelId(), event.getStartDate(), event.getEndDate(), event.getNumberOfStudents(), event.getWorkerList(), event.getNotation()));
     
         }
         event = new ScheduleEvent();  
     }  
       //Trycker event dialogen
     public void onEventSelect(SelectEvent selectEvent) {  
         event = (ScheduleEvent) selectEvent.getObject();  
     }  
    
     public void onDateSelect(SelectEvent selectEvent) {  
         event = new ScheduleEvent("", (Date) selectEvent.getObject(), (Date) selectEvent.getObject()); 
         event.setAllDay(false);
     }  
     
     public void onEventMove(ScheduleEntryMoveEvent event) {  
         FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Event moved", "Day delta:" + event.getDayDelta() + ", Minute delta:" + event.getMinuteDelta());  
           
         addMessage(message);  
     }  
       
     public void onEventResize(ScheduleEntryResizeEvent event) {  
         FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Event resized", "Day delta:" + event.getDayDelta() + ", Minute delta:" + event.getMinuteDelta());  
           
         addMessage(message);  
     }  
       
     private void addMessage(FacesMessage message) {  
         FacesContext.getCurrentInstance().addMessage(null, message);  
     }  
 
     private void loadModel() {
        List<Session> sessions = sesReg.getRange(0, sesReg.getCount());
        List<School> schList = reg.getRange(0, reg.getCount());
        for(School school : schList)
        {      
             for(Session s : school.getSchedule().getSessions())
             {
                 ScheduleEvent schEvent = new ScheduleEvent(school.getName(), s.getStartTime(), s.getEndTime());
                 schEvent.setModelId(s.getId());
                 schEvent.setNotation(s.getNotation());
                 schEvent.setNumberOfStudents(s.getNbrOfStudents());
                 schEvent.setWorkerList(s.getTutors());
                 schEvent.setSchoolName(school.getName());
                 eventModel.addEvent(schEvent);
             }
        }
     }
 
     /**
      * @return the schoolName
      */
}  

