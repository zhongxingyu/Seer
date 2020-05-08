 
 package org.sukrupa.student;
 
 import org.joda.time.LocalDate;
 import org.sukrupa.event.Event;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import static java.util.Arrays.asList;
 
 public class StudentBuilder {
     private static final String FEMALE = "female";
     private static final String MALE = "male";
 
     private String name;
     private String religion;
     private String caste;
     private String subCaste;
     private String area;
     private Set<Talent> talents = new HashSet<Talent>();
     private String studentId;
     private String gender;
     private Caregiver father;
     private Caregiver mother;
     private Caregiver guardian;
     private String studentClass;
     private LocalDate dateOfBirth = new LocalDate();
     private Set<Note> notes = new HashSet<Note>();
     private StudentStatus status = StudentStatus.EXISTING_STUDENT;
     private StudentFamilyStatus familyStatus = null;
     private String sponsored;
     private String sponsor_email;
     private String disciplinary;
     private String performance;
     private Profile profile = new Profile();
     private Set<Event> events = new HashSet<Event>();
 
     public static Student studentWithNoTalent() {
         return new StudentBuilder().build();
     }
 
     public StudentBuilder name(String name) {
         this.name = name;
         return this;
     }
 
     public StudentBuilder religion(String religion) {
         this.religion = religion;
         return this;
     }
 
     public StudentBuilder caste(String caste) {
         this.caste = caste;
         return this;
     }
 
     public StudentBuilder subCaste(String subCaste) {
         this.subCaste = subCaste;
         return this;
     }
 
     public StudentBuilder area(String area) {
         this.area = area;
         return this;
     }
 
     public StudentBuilder studentId(String studentId) {
         this.studentId = studentId;
         return this;
     }
 
     public StudentBuilder gender(String gender) {
         this.gender = gender;
         return this;
     }
 
     public StudentBuilder studentClass(String studentClass) {
         this.studentClass = studentClass;
         return this;
     }
 
     public StudentBuilder dateOfBirth(LocalDate dateOfBirth) {
         this.dateOfBirth = dateOfBirth;
         return this;
     }
 
     public StudentBuilder female() {
         gender = FEMALE;
         return this;
     }
 
     public StudentBuilder male() {
         gender = MALE;
         return this;
     }
 
      public StudentBuilder disciplinary(String disciplinary) {
         this.disciplinary = disciplinary;
         return this;
     }
 
      public StudentBuilder performance(String performance) {
         this.performance = performance;
         return this;
     }
 
     public StudentBuilder profile(Profile profile) {
         this.profile = profile;
         return this;
     }
 
     public StudentBuilder age(int age) {
         dateOfBirth = new LocalDate().minusYears(age);
         return this;
     }
 
     public StudentBuilder talents(Set<Talent> talents) {
         this.talents = talents;
         return this;
     }
 
     public StudentBuilder talents(Talent... talents) {
         talents(new HashSet<Talent>(asList(talents)));
         return this;
     }
 
     public StudentBuilder talents(String... talents) {
         Set<Talent> set = new HashSet<Talent>();
         for (String talent : talents) {
             set.add(new Talent(talent));
         }
         talents(set);
         return this;
     }
 
     public StudentBuilder notes(Note... notes) {
         this.notes = new HashSet(asList(notes));
         return this;
     }
 
     public StudentBuilder father(Caregiver father) {
         this.father = father;
         return this;
     }
 
     public StudentBuilder mother(Caregiver mother) {
         this.mother = mother;
         return this;
     }
 
     public StudentBuilder guardian(Caregiver guardian) {
         this.guardian = guardian;
         return this;
     }
 
     public Student build() {
 
        return new Student(studentId, name, religion, caste, subCaste, area, gender, studentClass, talents, father, mother, guardian, dateOfBirth, notes, this.status, disciplinary, performance, profile, events, familyStatus, sponsored,sponsor_email);
 
     }
 
     public StudentBuilder status(StudentStatus statusIn) {
         this.status = statusIn;
         return this;
     }
 
     public StudentBuilder sponsored (String sponsored) {
         this.sponsored = sponsored;
         return this;
     }
 
     public StudentBuilder events(Set<Event> events) {
         this.events = events;
         return this;
     }
 
     public StudentBuilder events(Event... events) {
         events(new HashSet<Event>(asList(events)));
         return this;
     }
 
     public StudentBuilder familyStatus(StudentFamilyStatus familyStatus) {
         this.familyStatus = familyStatus;
         return this;
     }
 }
 
