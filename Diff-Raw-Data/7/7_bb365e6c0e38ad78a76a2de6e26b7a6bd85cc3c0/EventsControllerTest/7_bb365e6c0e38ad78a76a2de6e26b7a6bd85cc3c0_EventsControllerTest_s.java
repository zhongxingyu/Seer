 package org.sukrupa.app.events;
 
 import org.apache.james.mime4j.io.LimitedInputStream;
 import org.hamcrest.CoreMatchers;
 import org.hamcrest.Matcher;
 import org.hibernate.mapping.Array;
 import org.joda.time.LocalDate;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Mock;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.validation.BeanPropertyBindingResult;
 import org.springframework.validation.Errors;
 import org.springframework.validation.FieldError;
 import org.sukrupa.app.students.StudentsController;
 import org.sukrupa.app.events.EventsController;
 import org.sukrupa.event.*;
 import org.sukrupa.platform.date.Date;
 import org.sukrupa.student.Student;
 import org.sukrupa.student.StudentBuilder;
 import org.sukrupa.student.StudentRepository;
 
 import java.security.PrivateKey;
 import java.util.*;
 import java.io.*;
 
 
 import java.util.HashMap;
 import java.util.Map;
 
 import static java.text.MessageFormat.format;
 import static junit.framework.Assert.assertTrue;
 import static junit.framework.Assert.fail;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.contains;
 import static org.hamcrest.Matchers.is;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 public class EventsControllerTest {
 
     @Mock
     private EventService service;
     @Mock
     private StudentRepository studentRepository;
     @Mock
     private EventForm eventForm;
     @Mock
     private Event event;
 
     private Map<String, List<Event>> eventModel = new HashMap<String, List<Event>>();
     private Map<String, Event> model = new HashMap<String, Event>();
     private Map<String, Object> objectModel = new HashMap<String, Object>();
     private List<String> listOfStudentIds = new ArrayList<String>();
    //TODO Remove duplicate "new BeanPropertyBindingResult(eventForm, "EventForm");"
     private Errors errors = new BeanPropertyBindingResult(eventForm, "EventForm");
     private EventsController controller;
     private Student attendee1 = new StudentBuilder().name("TC")
             .dateOfBirth(new LocalDate(1987, 12, 12))
             .studentId("123")
             .build();
     private Student attendee2 = new StudentBuilder().name("Hephzibah")
             .dateOfBirth(new LocalDate(1989, 9, 12))
             .studentId("231")
             .build();
     private Event eventOne = new EventBuilder().title("Spice Girls")
             .attendees(attendee1, attendee2)
             .date(new Date(1, 12, 2011))
             .description("Wahoo Spice Girls")
             .build();
     private Event eventTwo = new EventBuilder().title("Aravind's Event")
             .attendees(attendee1)
             .date(new Date(4, 7, 2011))
             .description("Testing Aravind's event")
             .build();
 
     @Before
     public void setUp() throws Exception {
         initMocks(this);
         listOfStudentIds.add("123");
         listOfStudentIds.add("231");
         controller = new EventsController(service, studentRepository);
     }
 
     @Test
     public void shouldDisplayEventsPage() {
         String list = controller.getListOfEvents(eventModel);
         verify(service).list();
         assertThat("Displays the Events page",list, is("events/list"));
 
     }
 
     @Test
     public void shouldAddEventsToListOfEvents() {
         List<Event> ourEventList = new ArrayList<Event>();
         ourEventList.add(eventOne);
         ourEventList.add(eventTwo);
         when(service.list()).thenReturn(ourEventList);
         controller.getListOfEvents(eventModel);
         List<Event> eventList = eventModel.get("events");
         assertTrue("EventList contains eventOne",eventList.contains(eventOne));
         assertTrue("EventList contains eventTwo",eventList.contains(eventTwo));
     }
 
     @Test
     public void shouldDisplayCreateANewEventPage() {
         String actual = controller.createNewEventPage(objectModel);
         assertThat("Display the Create Event Page", actual, is("events/create"));
     }
 
     @Test
     public void shouldDisplayEditANewEventPage() {
         String edit = controller.getEditEventPage(eventForm.getId(), objectModel);
         assertThat("Display the edit Event Page", edit, is("events/edit"));
     }
 
     @Test
     public void shouldDisplayGivenEventPage() {
         when(service.getEvent(4)).thenReturn(eventOne);
         String view = controller.getAnEventView(4, objectModel);
         assertThat("Displays the Event Page for the given Event Id",view, is("events/view"));
         verify(service).getEvent(4);
         assertThat("Model contains the given event", ((Event) objectModel.get("event")).getTitle(), is("Spice Girls"));
     }
 
     @Test
     public void shouldDisplayTheEditEventPageForTheGivenEventId() {
         when(service.getEvent(4)).thenReturn(eventOne);
         String edit = controller.getEditEventPage(4, objectModel);
         assertThat("Display the Edit Event Page for the Given Id",edit, is("events/edit"));
         verify(service).getEvent(4);
         assertThat("Model contains the given event",((Event) objectModel.get("event")), is(eventOne));
     }
 
     @Test
     public void shouldDisplayTheEditPageWhenUpdatingInvalidEvent() {
         Errors errors = new BeanPropertyBindingResult(eventForm, "EventForm");
         when(eventForm.isInvalid(errors)).thenReturn(true);
         String update = controller.updateAnEvent("4", eventForm, objectModel);
         verify(eventForm).isInvalid(errors);
         assertThat("Display the Edit Event Page",update, is("events/edit"));
     }
 
     @Test
     public void shouldDisplayTheEditPageWhenUpdatingInvalidEventWithAttendees() {
         Errors errors = new BeanPropertyBindingResult(eventForm, "EventForm");
         when(eventForm.isInvalid(errors)).thenReturn(true);
         when(eventForm.getAttendees()).thenReturn(listOfStudentIds);
         String update = controller.updateAnEvent("4", eventForm, objectModel);
         verify(eventForm).getAttendees();
         assertThat("Display the Edit Event Page",update, is("events/edit"));
     }
 
     @Test
     public void shouldDisplayTheEventPageWhenUpdatingEventWithAttendees() {
         Errors errors = new BeanPropertyBindingResult(eventForm, "EventForm");
         when(eventForm.isInvalid(errors)).thenReturn(false);
         when(eventForm.getAttendees()).thenReturn(listOfStudentIds);
         String update = controller.updateAnEvent("4", eventForm, objectModel);
         verify(eventForm).getAttendees();
         assertThat("Display the Edit Event Page",update, is("redirect:/events/4"));
     }
 
     @Test
     public void shouldDisplayTheEventPageWhenUpdatingEventWithoutAttendees() {
         Errors errors = new BeanPropertyBindingResult(eventForm, "EventForm");
         when(eventForm.isInvalid(errors)).thenReturn(false);
         when(eventForm.getAttendees()).thenReturn(null);
         String update = controller.updateAnEvent("4", eventForm, objectModel);
         verify(eventForm).getAttendees();
         assertThat("Display the Edit Event Page",update, is("redirect:/events/4"));
     }
 
     @Test
     public void shouldDisplayEventPageAfterSuccesfullyUpdating() {
         String update = controller.updateAnEvent("4", eventForm, objectModel);
         assertThat("Display the Event Page",update,is("redirect:/events/4"));
         verify(service).update(eventForm);
     }
 
     @Test
     public void shouldDisplayCreatePageWhenThereIsErrorInSaving() {
         Errors errors = new BeanPropertyBindingResult(eventForm, "EventForm");
         when(eventForm.isInvalid(errors)).thenReturn(true);
         String save = controller.saveANewEvent(eventForm, objectModel);
         assertThat("display Create Event Page", save, is("events/create"));
     }
 
     @SuppressWarnings("ToArrayCallWithZeroLengthArrayArgument")
     @Test
     public void shouldDisplayEventsPageAfterSuccesfullySaving() {
         Errors errors= mock(Errors.class);
         when(eventForm.isInvalid(errors)).thenReturn(false);
         when(event.getId()).thenReturn(4);
         when(service.save(eventForm)).thenReturn(event);
         String save = controller.saveANewEvent(eventForm, objectModel);
         assertThat("Display Event Page after Successful Save",save, is("redirect:/events/4"));
         verify(service).save(eventForm);
     }
 }
