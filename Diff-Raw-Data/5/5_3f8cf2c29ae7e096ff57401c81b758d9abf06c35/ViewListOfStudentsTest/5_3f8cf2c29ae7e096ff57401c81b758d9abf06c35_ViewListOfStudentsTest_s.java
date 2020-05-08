 package org.sukrupa.student;
 
 import org.hamcrest.Description;
 import org.hamcrest.Matcher;
 import org.junit.After;
 import org.junit.Test;
 import org.junit.internal.matchers.TypeSafeMatcher;
 import org.junit.runner.RunWith;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.htmlunit.HtmlUnitDriver;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.sukrupa.config.AppConfigForTestsContextLoader;
 import org.sukrupa.page.ListOfStudentsPage;
 import org.sukrupa.page.StudentRow;
 import org.sukrupa.platform.DatabaseHelper;
 
 import java.util.List;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(loader = AppConfigForTestsContextLoader.class)
 public class ViewListOfStudentsTest {
 
     private WebDriver driver = new HtmlUnitDriver();
 
     @Autowired
     private DatabaseHelper databaseHelper;
 
     @After
     public void tearDown() throws Exception {
         databaseHelper.deleteAllCreatedObjects();
     }
 
     @Test
    public void fail() {
        throw new RuntimeException("bang!");
    }

    @Test
     public void shouldDisplayListOfAllStudentsOrderedByGenderAndName() {
         Talent running = new Talent("running");
         Talent jumping = new Talent("jumping");
         Talent flying = new Talent("flying");
         save(running, jumping, flying);
 
         Student rebecca = new StudentBuilder().name("rebecca").studentId("1").female().age(25).talents(running, jumping, flying).build();
         Student bob = new StudentBuilder().name("bob").studentId("2").male().age(22).talents(flying).build();
         Student alex = new StudentBuilder().name("alex").studentId("3").male().age(42).build();
         save(rebecca, bob, alex);
 
         List<StudentRow> students = new ListOfStudentsPage(driver).getStudents();
 
         assertThat(students.get(0), matches(rebecca));
         assertThat(students.get(1), matches(alex));
         assertThat(students.get(2), matches(bob));
     }
 
     public void save(Object... students) {
         databaseHelper.saveAndCommit(students);
     }
 
     private Matcher<StudentRow> matches(final Student student) {
         return new TypeSafeMatcher<StudentRow>() {
 
             private StudentRow studentRow;
 
             public boolean matchesSafely(StudentRow studentRow) {
                 this.studentRow = studentRow;
                 return sameName() && sameStudentId() && sameGender() && sameAge() && sameTalents();
             }
 
             private boolean sameTalents() {
                 return student.getTalentsForDisplay().equals(studentRow.getTalents());
             }
 
             private boolean sameName() {
                 return student.getName().equals(studentRow.getName());
             }
 
             private boolean sameStudentId() {
                 return student.getStudentId().equals(studentRow.getStudentId());
             }
 
             private boolean sameGender() {
                 return student.getGender().equals(studentRow.getGender());
             }
 
             private boolean sameAge() {
                 return student.getAge() == studentRow.getAge();
             }
 
             public void describeTo(Description description) {
                 description.appendValue(student);
             }
         };
     }
 }
