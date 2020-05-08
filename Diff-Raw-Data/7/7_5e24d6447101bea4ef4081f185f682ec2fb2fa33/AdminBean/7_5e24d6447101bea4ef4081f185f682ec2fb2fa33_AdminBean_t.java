 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package BB;
 
 // import com.mycompany.ove.model.OveScheduleEvent;
 import Model.School;
 import EJB.SchoolRegistry;
 import EJB.SessionRegistry;
 import EJB.WorkerRegistry;
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
 import org.primefaces.context.RequestContext;
 import org.primefaces.event.ScheduleEntryMoveEvent;
 import org.primefaces.event.ScheduleEntryResizeEvent;
 import org.primefaces.event.SelectEvent;
 import org.primefaces.model.DefaultScheduleEvent;
 import org.primefaces.model.DefaultScheduleModel;
 import org.primefaces.model.DualListModel;
 
 
 import org.primefaces.model.ScheduleModel;
 
 /**
  * Backing bean for Admin page
  * @author Gustav & kristofferskjutar
  */
 @Named("scheduleBean")
 @SessionScoped
 public class AdminBean implements Serializable
 {
     @EJB
     private SchoolRegistry reg;
     
     @EJB
     private SessionRegistry sesReg;
     
     @EJB
     private WorkerRegistry workReg;
     private ScheduleModel eventModel; 
     private List<School> schoolList;
 
       
     //Overide this to handle events,   comments and time 
     private ScheduleEvent event = new ScheduleEvent();
     private ArrayList<String> target;
     private ArrayList<String> source;
     private DualListModel<String> workerModel;
     private boolean showDelete=true;
    
     //Create all sessions for all schools in this list
    @PostConstruct
    public void init()
    {
        eventModel = new DefaultScheduleModel();
        loadModel();
        loadWorkers();
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
     
   
     /**
      * 
      * @param actionEvent 
      */  
     public void addEvent(ActionEvent actionEvent) { 
         
         ArrayList<Worker> workerList = new ArrayList<Worker>();
         for(String s : workerModel.getTarget())
        {     
          
            Worker w = workReg.getByPNumber(Long.parseLong(s.split(" ")[2])).get(0);
            workerList.add(w);
           // workerList.add(workReg.getById(Long.parseLong(s.split(" ")[2])));
         }
  
         event.setWorkerList(workerList);
         if(event.getId() == null)  
         {
             
             School s = reg.getByName(event.getSchoolName()).get(0);
             event.setTitle(s.getName());
             
             s.getSchedule().getSessions().add(new Session(s,event.getStartDate(), event.getEndDate(), event.getNumberOfStudents(), event.getWorkerList(), event.getNotation()));         
             
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
             sesReg.update(new Session(s,event.getModelId(), event.getStartDate(), event.getEndDate(), event.getNumberOfStudents(), event.getWorkerList(), event.getNotation()));
     
         }
         event = new ScheduleEvent(); 
         loadWorkers();
     }  
     
     public void deleteEvent(ActionEvent actionEvent)
     {
        School s = reg.getByName(event.getSchoolName()).get(0);
        s.getSchedule().getSessions().remove(sesReg.find(event.getModelId()));
        reg.update(s);
        sesReg.remove(event.getModelId());
        eventModel.deleteEvent(event);
        event = new ScheduleEvent(); 
        loadWorkers();
     }
       //Trycker event dialogen
     public void onEventSelect(SelectEvent selectEvent) {  
         setShowDelete(false);
         loadWorkers();
         event = (ScheduleEvent) selectEvent.getObject();
         for(Worker w : event.getWorkerList())
         {
             workerModel.getTarget().add(w.getName()+ " "+w.getIdNumber());
             workerModel.getSource().remove(w.getName()+ " "+w.getIdNumber());
         }
     }  
    
     public void onDateSelect(SelectEvent selectEvent) {  
         setShowDelete(true);
         loadWorkers();
         event = new ScheduleEvent("", (Date) selectEvent.getObject(), (Date) selectEvent.getObject()); 
         event.setAllDay(false);
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
 
     private void loadWorkers() {
         List<Worker> workerList = workReg.getRange(0, workReg.getCount());
         setSource(new ArrayList<String>());
         setTarget(new ArrayList<String>());
         for(Worker w : workerList)
         {
             getSource().add(w.getName()+ " "+w.getIdNumber());
         }  
         setWorkerModel(new DualListModel<String>(source, target));
     }
 
    
 
    
     
 
     /**
      * @return the target
      */
 
     public ArrayList<String> getTarget() {
         return target;
     }
 
     /**
      * @param target the target to set
      */
     public void setTarget(ArrayList<String> target) {
         this.target = target;
     }
 
     /**
      * @return the source
      */
     public ArrayList<String> getSource() {
         return source;
     }
 
     /**
      * @param source the source to set
      */
     public void setSource(ArrayList<String> source) {
         this.source = source;
     }
 
     /**
      * @return the workerModel
      */
     public DualListModel<String> getWorkerModel() {
         return workerModel;
     }
 
     /**
      * @param workerModel the workerModel to set
      */
     public void setWorkerModel(DualListModel<String> workerModel) {
         this.workerModel = workerModel;
     }
 
     /**
      * @return the showDelete
      */
     public boolean isShowDelete() {
         return showDelete;
     }
 
     /**
      * @param showDelete the showDelete to set
      */
     public void setShowDelete(boolean showDelete) {
         this.showDelete = showDelete;
     }
 
     /**
      * @return the workers
      */
    
     
     
 
 
    
 }  
 
