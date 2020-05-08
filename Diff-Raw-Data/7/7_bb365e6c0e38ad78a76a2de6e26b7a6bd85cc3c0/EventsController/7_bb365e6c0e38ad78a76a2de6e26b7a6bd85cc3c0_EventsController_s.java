 package org.sukrupa.app.events;
 
 import org.hsqldb.lib.Collection;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BeanPropertyBindingResult;
 import org.springframework.validation.Errors;
 import org.springframework.validation.FieldError;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.sukrupa.event.Event;
 import org.sukrupa.event.EventForm;
 import org.sukrupa.event.EventService;
 import org.sukrupa.student.StudentNameComparator;
 import org.sukrupa.student.StudentRepository;
 import org.sukrupa.student.Student;
 
 import java.util.*;
 
 import static java.lang.String.format;
 import static org.springframework.web.bind.annotation.RequestMethod.GET;
 import static org.springframework.web.bind.annotation.RequestMethod.POST;
 
 @SuppressWarnings("unchecked")
 @Controller
 @RequestMapping("/events")
 public class EventsController {
     private final EventService service;
     private StudentRepository studentRepository;
 
     @Autowired
     public EventsController(EventService service, StudentRepository studentRepository) {
         this.service = service;
         this.studentRepository = studentRepository;
     }
 
     @RequestMapping()
     public String getListOfEvents(Map<String, List<Event>> model){
         model.put("events", service.list());
         return "events/list";
     }
 
    //TODO Fix display of attendes
     @RequestMapping(value = "/{eventId}")
     public String getAnEventView(@PathVariable int eventId, Map<String, Object> model) {
         Event event = service.getEvent(eventId);
         model.put("event", event);
         model.put("attendeesList", getListOfAttendingStudents(event));
         return "events/view";
     }
 
     @RequestMapping(value = "/edit/{eventId}", method = GET)
     public String getEditEventPage(@PathVariable int eventId, Map<String, Object> model) {
         Event event = service.getEvent(eventId);
         model.put("attendeesList", getListOfAttendingStudents(event));
         model.put("studentList", getListOfAvailableStudents(event));
         model.put("event", event);
         return "events/edit";
     }
 
 	@RequestMapping(value = "create", method = GET)
 	public String createNewEventPage(Map<String, Object> model) {
         model.put("studentList", getListOfAvailableStudents());
 		return "events/create";
 	}
 
     @RequestMapping(value = "/update/{eventId}", method = POST)
     public String updateAnEvent(@PathVariable String eventId,
             @ModelAttribute("editEvent") EventForm eventForm,
             Map<String, Object> model) {
         Errors errors = new BeanPropertyBindingResult(eventForm, "EventForm");
 
         List<String> attendingStudents = eventForm.getAttendees();
         List<Student> attendingStudentsList = new ArrayList<Student>();
         try{
             for (String attendingStudent : attendingStudents){
                 attendingStudentsList.add(studentRepository.findByStudentId(attendingStudent));
             }
             Collections.sort(attendingStudentsList, new StudentNameComparator());
         } catch (NullPointerException e){
             //lol, there r no attendees yet
         }
 
         if (eventForm.isInvalid(errors)){
             model.put("errors", errors);
             model.put("event", eventForm);
             model.put("attendeesList", attendingStudentsList);
             model.put("studentList", getListOfAvailableStudents(attendingStudentsList));
             addErrorToFields(model, errors);
             return "events/edit";
         }
 
         service.update(eventForm);
         return format("redirect:/events/%s", eventId);
     }
 
     @SuppressWarnings("ToArrayCallWithZeroLengthArrayArgument")
     @RequestMapping(value = "save", method = POST)
     public String saveANewEvent(@ModelAttribute(value = "createEventForm") EventForm eventForm,
                        Map<String, Object> model) {
         Errors errors = new BeanPropertyBindingResult(eventForm, "EventForm");
 
         if (eventForm.isInvalid(errors)) {
             model.put("errors", errors);
             model.put("event", eventForm);
             addErrorToFields(model, errors);
             return "events/create";
         }
         Event event = service.save(eventForm);
         return format("redirect:/events/%s", event.getId());
     }
 
     private void addErrorToFields(Map<String, Object> model, Errors errors) {
           for (FieldError error : errors.getFieldErrors()) {
               model.put(format("%sError", error.getField()), error.getDefaultMessage());
           }
       }
 
     // all students less attending students
     @SuppressWarnings("unchecked")
     private List<Student> getListOfAvailableStudents(Event event){
         List<Student> studentList = studentRepository.getList();
         List<Student> attendeesList = getListOfAttendingStudents(event);
         for (Student student: attendeesList){
             studentList.remove(student);
         }
         Collections.sort(studentList, new StudentNameComparator());
         return studentList;
     }
 
     // all students less list of students passed it.
     @SuppressWarnings("unchecked")
     private List<Student> getListOfAvailableStudents(List<Student> attendeesList) {
         List<Student> studentList = studentRepository.getList();
         try {
             for (Student student : attendeesList) {
                 studentList.remove(student);
             }
             Collections.sort(studentList, new StudentNameComparator());
         } catch (NullPointerException e) {
             // there are no attendees
         }
         return studentList;
     }
 
     // all students
     @SuppressWarnings("unchecked")
     private List<Student> getListOfAvailableStudents(){
         List<Student> studentList = studentRepository.getList();
         Collections.sort(studentList, new StudentNameComparator());
         return studentList;
     }
 
     @SuppressWarnings("unchecked")
     private List<Student> getListOfAttendingStudents(Event event){
         List<Student> attendeesList = new ArrayList<Student>();
         try{
             attendeesList = new ArrayList<Student>(event.getAttendees());
             Collections.sort(attendeesList, new StudentNameComparator());
         } catch (NullPointerException e) {
             // there are no attendess
         }
         return attendeesList;
     }
 }
