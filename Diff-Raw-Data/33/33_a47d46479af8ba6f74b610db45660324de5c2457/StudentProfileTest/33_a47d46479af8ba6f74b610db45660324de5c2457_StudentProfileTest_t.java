 package org.sukrupa.student;
 
 import org.joda.time.LocalDate;
 import org.junit.Test;
 
import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.core.StringContains.containsString;
 
 public class StudentProfileTest {
     @Test
     public void shouldCreateAHTMLMessageFromStudentDetails(){
         StudentProfile studentProfile = new StudentProfile("A Name", "10-1-1990", "Male", "Some Background", "A Disciplinary", "Comments", "234524");
         String message = studentProfile.composeHtmlMessage();
         assertThat(message, containsString("<html>"));
         assertThat(message, containsString("<body>"));
         assertThat(message, containsString("A Name"));
         assertThat(message, containsString("Male"));
         assertThat(message, containsString("</body>"));
         assertThat(message, containsString("</html>"));
     }
 
     @Test
     public void shouldContainStudentDetailsWithinHtmlTableTags(){
         StudentProfile studentProfile = new StudentProfile("Bhavani", "22-04-2004", "Female", "Born in India", "Excellent", "Bhavani is a very smart kid!","23462");
         String message = studentProfile.composeHtmlMessage();
         assertThat(message, containsString("<html>"));
         assertThat(message, containsString("<body>"));
         assertThat(message, containsString("<table")); // There may be some table attributes, and hence <table> won't work always
         assertThat(message, containsString("Name"));
         assertThat(message, containsString("Bhavani"));
         assertThat(message, containsString("Bhavani is a very smart kid!"));
         assertThat(message, containsString("</table>"));
         assertThat(message, containsString("</body>"));
         assertThat(message, containsString("</html>"));
        assertThat(message, containsString("<tr><td>"));
        assertThat(message, containsString("</td><td>"));
        assertThat(message, containsString("</td></tr>"));
     }

    @Test
    public void shouldCreateStudentProfile(){
        StudentProfile studentProfile = new StudentProfile("Bhavani", "22-04-2004", "Female", "Born in India", "Excellent", "Bhavani is a very smart kid!","23462");
        assertThat(studentProfile.getStudentName(), is("Bhavani"));
        assertThat(studentProfile.getStudentId(), is("23462"));
        assertThat(studentProfile.getComments(), is("Bhavani is a very smart kid!"));
        assertThat(studentProfile.getGender(), is("Female"));
        assertThat(studentProfile.getDateOfBirth(), is("22-04-2004"));
        assertThat(studentProfile.getStudentBackground(), is("Born in India"));
        assertThat(studentProfile.getStudentDisciplinary(), is("Excellent"));
    }

 }
