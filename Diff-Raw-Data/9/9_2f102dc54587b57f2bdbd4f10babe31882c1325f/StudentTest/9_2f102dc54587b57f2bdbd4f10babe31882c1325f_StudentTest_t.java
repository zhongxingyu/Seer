 package org.sukrupa.student;
 
 import org.joda.time.DateMidnight;
 import org.joda.time.DateTimeUtils;
 import org.joda.time.LocalDate;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.springframework.web.multipart.commons.CommonsMultipartFile;
 import org.sukrupa.event.Event;
 import org.sukrupa.event.EventBuilder;
 import org.sukrupa.event.EventCreateOrUpdateParameter;
 import org.sukrupa.platform.date.Date;
 
 //import java.util.*;
 
 import java.util.*;
 
 import static junit.framework.Assert.assertEquals;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.hasItems;
 import static org.hamcrest.Matchers.is;
 import static org.mockito.Mockito.mock;
 
 public class StudentTest {
 
     @BeforeClass
     public static void classSetUp() {
         DateTimeUtils.setCurrentMillisFixed(new DateMidnight(2010, 3, 02).getMillis());
     }
 
     @AfterClass
     public static void classTearDown() {
         DateTimeUtils.setCurrentMillisSystem();
     }
 
     @Test
     public void shouldBeEqual() {
         assertThat(student("pat", new LocalDate(2005, 3, 01), new Talent("music"), new Talent("sport")),
                 is(student("pat", new LocalDate(2005, 3, 01), new Talent("sport"), new Talent("music"))));
     }
 
     @Test
     public void shouldHaveSameHashCode() {
         assertThat(student("pat", null).hashCode(), is(student("pat", null).hashCode()));
     }
 
 
     @Test
     public void shouldNotBeEqualIfDifferentName() {
         assertThat(student("pat", null).equals(student("mr. jones", null)), is(false));
     }
 
     @Test
     public void shouldReturnStudentIDForImageLink() {
         Student student = new StudentBuilder().studentId("Balaji").build();
         String defaultLink = "BALAJI";
         assertThat(student.getImageLink(),is(defaultLink));
     }
 
     @Test
     public void shouldBe5YearsOld() {
         assertThat(student("pat", new LocalDate(2005, 01, 22)).getAge(), is(5));
     }
 
     @Test
     public void shouldBeOfSameAge() {
         assertThat(student("pat", new LocalDate(2005, 4, 12)).getAge(), is(student("pat", new LocalDate(2005, 6, 10)).getAge()));
     }
 
     @Test
     public void shouldBe5YearOldCurrentDateMonthBeforeDOBMonth() {
         assertThat(student("pat", new LocalDate(2005, 4, 22)).getAge(), is(4));
     }
 
     @Test
     public void shouldBe5YearOldCurrentDateDayBeforeDOBDay() {
         assertThat(student("pat", new LocalDate(2005, 3, 3)).getAge(), is(4));
     }
 
     @Test
     public void shouldNBe5YearOldCurrentDateMonthAfterDOBMonth() {
         assertThat(student("pat", new LocalDate(2005, 2, 01)).getAge(), is(5));
     }
 
     @Test
     public void shouldDisplayTalentsAsCommaSeperated() {
         Student ron = new StudentBuilder().talents("running", "flying").build();
         assertThat(ron.getTalentsForDisplay(), is("running, flying"));
     }
 
     @Test
     public void shouldCaptureNotesAboutAStudent() {
         Note firstNote = new Note("note1");
         Note secondNote = new Note("note2");
         Student suhas = new StudentBuilder().notes(firstNote, secondNote).build();
 
         assertThat(suhas.getNotes(), hasItems(firstNote, secondNote));
     }
 
     @Test
     public void shouldPromoteStudent() {
         assertEquals("2 Std", promoteStudent("1 Std").getStudentClass());
         assertEquals("3 Std", promoteStudent("2 Std").getStudentClass());
         assertEquals("4 Std", promoteStudent("3 Std").getStudentClass());
         assertEquals("10 Std", promoteStudent("9 Std").getStudentClass());
         assertEquals("UKG",promoteStudent("LKG").getStudentClass());
         assertEquals("1 Std",promoteStudent("UKG").getStudentClass());
         assertEquals("LKG", promoteStudent("Preschool").getStudentClass());
     }
 
     @Test
     public void shouldPromoteAStudentOutOfTheSchool() {
         Student tenthStandardStudent = new StudentBuilder().studentClass("10 Std").build();
 
         tenthStandardStudent.promote();
 
         assertEquals(StudentStatus.ALUMNI, tenthStandardStudent.getStatus());
         assertEquals("10 Std", tenthStandardStudent.getStudentClass());
     }
 
     @Test
     public void shouldNotPromoteIfDropout(){
        Student dropoutStudent = new StudentBuilder().studentClass("5 Std").status(StudentStatus.DROPOUT).build();
 
        dropoutStudent.promote();
        assertEquals("5 Std", dropoutStudent.getStudentClass());
     }
 
     @Test
     public void shouldNotPromoteIfNoClass(){
        Student noClassStudent = new StudentBuilder().studentClass("").build();
 
        noClassStudent.promote();
        assertEquals("", noClassStudent.getStudentClass());
     }
 
     @Test
     public void shouldNotChangeAlumni(){
         Student alumnus = new StudentBuilder().status(StudentStatus.ALUMNI).build();
         alumnus.promote();
         assertEquals(alumnus.getStatus(), StudentStatus.ALUMNI);
     }
 
     @Test
     public void shouldHaveStudentIDAsUppercase() {
         Student student = new StudentBuilder().studentId("sk123").build();
         assertThat(student.getStudentId(), is("SK123"));
     }
 
     @Test
     public void shouldUpdateStudent()
     {
         Student student = new StudentBuilder().build();
 
         StudentForm studentForm = new StudentForm();
         Caregiver father = new Caregiver();
         father.setName("someFather");
 
         Caregiver mother = new Caregiver();
         mother.setName("someMother");
 
         studentForm.setFather(father);
         studentForm.setMother(mother);
         studentForm.setDateOfBirth("01-02-2005");
         studentForm.setStatus("Existing Student");
         studentForm.setFamilyStatus("Single");
         studentForm.setSponsored(true);
 
         student.updateFrom(studentForm, Collections.EMPTY_SET );
 
         assertThat(student.getFather().getName(), is("someFather"));
         assertThat(student.getMother().getName(), is("someMother"));
         assertThat(student.getSponsored(), is (true));
         assertThat(student.getFamilyStatus(), is("Single"));
 
     }
 
 
     @Test
     public void testStudentHasAnEvent(){
         Event event = new EventBuilder().title("Spice Girls")
                                         .build();
 
         Set<Event> setOfEvents = new HashSet<Event>();
         setOfEvents.add(event);
 
         Student student = new StudentBuilder().events(setOfEvents).build();
         assertThat(student.getEvents(), is(setOfEvents));
     }
 
     @Test
     public void testStudentHasEvents(){
         Event spiceGirls = new EventBuilder().title("Spice Girls")
                                         .build();
 
         Event backstreetBoys = new EventBuilder().title("Backstreet Boys")
                                         .build();
 
         Set<Event> setOfEvents = new HashSet<Event>();
         setOfEvents.add(spiceGirls);
         setOfEvents.add(backstreetBoys);
 
         Student student = new StudentBuilder().events(setOfEvents).build();
         assertThat(student.getEvents(), is(setOfEvents));
     }
 
     @Test
     public void shouldSetImageLinkToStudentIDIfUpdateParametersHaveAnImage(){
         Student student = new StudentBuilder().studentId("12345").build();
         CommonsMultipartFile image = mock(CommonsMultipartFile.class);
         StudentForm studentUpdateParameters = new StudentCreateOrUpdateParameterBuilder().imageToUpload(image).build();
 
         student.updateFrom(studentUpdateParameters,Collections.<Talent>emptySet());
 
         assertEquals(student.getImageLink(), "12345");
     }
 
     @Test
     public void testStudentShouldDisplayEventsWithCommaFormat()
     {
         Event spiceGirls = new EventBuilder().title("Spice Girls")
                                         .build();
 
         Event backstreetBoys = new EventBuilder().title("Backstreet Boys")
                                         .build();
 
         Set<Event> setOfEvents = new HashSet<Event>();
         setOfEvents.add(spiceGirls);
         setOfEvents.add(backstreetBoys);
 
         Student student = new StudentBuilder().events(setOfEvents).build();
        assertThat(student.getEventsForDisplay(), is("Backstreet Boys, Spice Girls"));
     }
 
     private Student promoteStudent(String studentClass) {
         Student student = new StudentBuilder().studentClass(studentClass).build();
         student.promote();
         return student;
     }
 
     private Student student(String name, LocalDate dateOfBirth) {
         return new StudentBuilder().name(name).dateOfBirth(dateOfBirth).build();
     }
 
     private Student student(String name, LocalDate dateOfBirth, Talent... talents) {
         return new StudentBuilder().name(name).dateOfBirth(dateOfBirth).talents(new HashSet(Arrays.asList(talents))).build();
     }
 
 
 }
