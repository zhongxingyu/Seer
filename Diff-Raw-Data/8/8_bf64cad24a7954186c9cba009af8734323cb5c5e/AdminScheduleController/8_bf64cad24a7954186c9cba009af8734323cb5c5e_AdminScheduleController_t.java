 package pl.agh.enrollme.controller.termmanagement;
 
 import org.primefaces.event.EnrollScheduleEntryMoveEvent;
 import org.primefaces.event.EnrollScheduleEntryResizeEvent;
 import org.primefaces.event.SelectEvent;
 import org.primefaces.model.DefaultEnrollScheduleEvent;
 import org.primefaces.model.DefaultEnrollScheduleModel;
 import org.primefaces.model.EnrollScheduleEvent;
 import org.primefaces.model.EnrollScheduleModel;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import pl.agh.enrollme.model.*;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 import java.io.Serializable;
 import java.util.*;
 
 /**
  * Author: Piotr Turek
  */
 public class AdminScheduleController implements Serializable {
 
     private static final long serialVersionUID = -740843017652008055L;
 
     private static final Logger LOGGER = LoggerFactory.getLogger(AdminScheduleController.class);
 
     //Custom container model for the EnrollSchedule component
     private EnrollScheduleModel eventModel = new DefaultEnrollScheduleModel();
 
     //Custom event model for the EnrollSchedule component; currently selected event
     private DefaultEnrollScheduleEvent event = new DefaultEnrollScheduleEvent();
 
     //Currently selected teacher from the teacher list
     private Teacher teacher = new Teacher();
 
     //Currently selected subject from the subject list
     private Subject subject = new Subject();
 
     //Whether currently selected event is certain or not
     private Boolean certain = false;
 
     //Capacity of the current event
     private Integer capacity = 0;
 
 
     //Enroll data
     private EnrollConfiguration enrollConfiguration;
 
     private List<Subject> subjects;
 
     private List<Term> terms;
 
     private List<Teacher> teachers;
     //Enroll data end
 
     //Mapping from Term to StudentPointsPerTerm
     private Map<Term, StudentPointsPerTerm> termToPointsMap = new HashMap<>();
 
     //Mapping from EventID to Term
     private Map<String, Term> eventToTermMap = new HashMap<>();
 
     //Schedule component attributes
     private boolean periodic = true;
     private Date initialDate = new Date();
     private String leftHeaderTemplate = "prev, next";
     private String centerHeaderTemplate = "title";
     private String rightHeaderTemplate = "month, agendaWeek, agendaDay";
     private int weekViewWidth = 1500;
     private String view = "agendaWeek";
     //Schedule theme: as of today, unused
     private String theme;
 
 
     public AdminScheduleController(EnrollConfiguration enrollConfiguration, List<Subject> subjects,
                                    List<Term> terms, List<Teacher> teachers) {
         this.enrollConfiguration = enrollConfiguration;
         this.subjects = subjects;
         this.terms = terms;
         this.teachers = teachers;
 
         this.periodic = enrollConfiguration.getPeriodic();
         this.weekViewWidth = enrollConfiguration.getWeekViewWidth();
 
         if (!periodic) {
             leftHeaderTemplate += ", today";
         }
 
         preprocessTerms();
     }
 
 
     public EnrollScheduleModel getEventModel() {
         return eventModel;
     }
 
     public void setEventModel(EnrollScheduleModel eventModel) {
         this.eventModel = eventModel;
     }
 
     public DefaultEnrollScheduleEvent getEvent() {
         return event;
     }
 
     public void setEvent(DefaultEnrollScheduleEvent event) {
         this.event = event;
     }
 
     public Teacher getTeacher() {
         return teacher;
     }
 
     public void setTeacher(Teacher teacher) {
         this.teacher = teacher;
     }
 
     public Subject getSubject() {
         return subject;
     }
 
     public void setSubject(Subject subject) {
         this.subject = subject;
     }
 
     public Boolean getCertain() {
         return certain;
     }
 
     public void setCertain(Boolean certain) {
         this.certain = certain;
     }
 
     public Integer getCapacity() {
         return capacity;
     }
 
     public void setCapacity(Integer capacity) {
         this.capacity = capacity;
     }
 
 
 
     private void addMessage(FacesMessage message) {
         FacesContext.getCurrentInstance().addMessage(null, message);
     }
 
     /**
      * Event triggered when user clicks on a term
      * @param selectEvent
      */
     public void onEventSelect(SelectEvent selectEvent) {
         event = (DefaultEnrollScheduleEvent) selectEvent.getObject();
 
         final Term term = eventToTermMap.get(event);
         final Subject subject = term.getSubject();
 
     }
 
     /**
      * Event triggered when user clicks on a date. It creates an empty event at a given time-point.
      * @param selectEvent
      */
     public void onDateSelect(SelectEvent selectEvent) {
         final Date begin = (Date) selectEvent.getObject();
         final GregorianCalendar date = new GregorianCalendar();
         date.setTime(begin);
         date.add(Calendar.MINUTE, 30);
         final Date end = date.getTime();
 
         event = new DefaultEnrollScheduleEvent("", begin, end);
        event.setPlace("test");
 
         LOGGER.debug("Date clicked: " + event.getStartDate() + " - " + event.getEndDate());
     }
 
     /**
      * Event triggered when user moves an event. It updates a given event and corresponding term.
      * @param moveEvent
      */
     public void onEventMove(EnrollScheduleEntryMoveEvent moveEvent) {
         final EnrollScheduleEvent scheduleEvent = moveEvent.getScheduleEvent();
 
         FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Term moved", "Day delta: "
                 + moveEvent.getDayDelta() + ", Minute delta: " + moveEvent.getMinuteDelta());
         addMessage(message);
 
         updateTermTime(scheduleEvent);
     }
 
     /**
      * Event triggered when user resizes an event
      * @param resizeEvent
      */
     public void onEventResize(EnrollScheduleEntryResizeEvent resizeEvent) {
         final EnrollScheduleEvent scheduleEvent = resizeEvent.getScheduleEvent();
 
         FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Term resized", "Day delta: "
             + resizeEvent.getDayDelta() + ", Minute delta: " + resizeEvent.getMinuteDelta());
         addMessage(message);
 
         updateTermTime(scheduleEvent);
     }
 
     /**
      * Updates current event (kept in event field)
      */
     public boolean updateEvent(ActionEvent actionEvent) {
         Term term = eventToTermMap.get(event);
 
         if (term == null) {
             term = new Term(); //TODO: create a fully initialized term corresponding to the current event
             eventModel.addEvent(event);
             eventToTermMap.put(event.getId(), term);
         } else {
             eventModel.updateEvent(event);
         }
 
         event = new DefaultEnrollScheduleEvent();
 
         return true;
     }
 
 
     //Enroll data getters and setters begin
     public EnrollConfiguration getEnrollConfiguration() {
         return enrollConfiguration;
     }
 
     public void setEnrollConfiguration(EnrollConfiguration enrollConfiguration) {
         this.enrollConfiguration = enrollConfiguration;
     }
 
     public List<Subject> getSubjects() {
         return subjects;
     }
 
     public void setSubjects(List<Subject> subjects) {
         this.subjects = subjects;
     }
 
     public List<Term> getTerms() {
         return terms;
     }
 
     public void setTerms(List<Term> terms) {
         this.terms = terms;
     }
 
     public List<Teacher> getTeachers() {
         return teachers;
     }
 
     public void setTeachers(List<Teacher> teachers) {
         this.teachers = teachers;
     }
     //Enroll data getters and setters end
 
 
     //Getters for Schedule attributes begin
     public boolean isPeriodic() {
         return periodic;
     }
 
     public Date getInitialDate() {
         return initialDate;
     }
 
     public String getLeftHeaderTemplate() {
         return leftHeaderTemplate;
     }
 
     public String getCenterHeaderTemplate() {
         return centerHeaderTemplate;
     }
 
     public String getRightHeaderTemplate() {
         return rightHeaderTemplate;
     }
 
     public int getWeekViewWidth() {
         return weekViewWidth;
     }
 
     public String getView() {
         return view;
     }
 
     public String getTheme() {
         return theme;
     }
     //Getters for Schedule attributes end
 
 
     private void preprocessTerms() {
 
         GregorianCalendar minDate = new GregorianCalendar();
         GregorianCalendar startTime = new GregorianCalendar();
         minDate.setTime(new Date());
 
         //preprocess terms, computing scope of enrollment, creating events etc.
         for (Term t : terms) {
 
             startTime.setTime(t.getStartTime());
 
             if (startTime.before(minDate)) {
                 minDate = startTime;
             }
 
             DefaultEnrollScheduleEvent event = new DefaultEnrollScheduleEvent();
 
             //setting points - not used in this view
             event.setPoints(0);
 
             //setting event's place
             event.setPlace(t.getRoom());
 
             Teacher teacher = t.getTeacher();
             String teacherString = teacher.getDegree() + " " + teacher.getFirstName().charAt(0) +
                     ". " + teacher.getSecondName();
             //setting event's teacher
             event.setTeacher(teacherString);
 
             final Subject subject = t.getSubject();
 
             //setting event's title to subjects' name
             event.setTitle(subject.getName());
 
             //setting event's color to that of event's subject
             event.setColor("#" + subject.getColor());
 
             //setting event's type
             event.setActivityType(t.getType());
 
 
             //setting whether to display points or not
             event.setShowPoints(false);
 
             //event's shouldn't be editable
             event.setEditable(false);
 
             //adding event to the model
             eventModel.addEvent(event);
 
             eventToTermMap.put(event.getId(), t);
         }
 
         //update time fields, but only if there were some events added, otherwise use defaults
         if (terms.size() > 0) {
             this.initialDate = minDate.getTime();
         }
     }
 
     private void updateTermTime(EnrollScheduleEvent scheduleEvent) {
         eventModel.updateEvent(scheduleEvent);
         final Term term = eventToTermMap.get(scheduleEvent);
         term.setStartTime(scheduleEvent.getStartDate());
         term.setEndTime(scheduleEvent.getEndDate());
     }
 
 }
