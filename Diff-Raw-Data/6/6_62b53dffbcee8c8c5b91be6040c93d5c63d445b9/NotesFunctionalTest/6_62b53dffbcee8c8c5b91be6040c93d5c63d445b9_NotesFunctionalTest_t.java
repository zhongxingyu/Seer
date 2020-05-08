 import com.google.common.collect.ImmutableMap;
 
 import models.Note;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import play.mvc.Result;
 import play.test.WithApplication;
 
 import java.util.*;
 
 import static org.fest.assertions.Assertions.assertThat;
 import static org.junit.Assert.*;
 import static play.test.Helpers.*;
 
 public class NotesFunctionalTest extends WithApplication {
 
   @Before
   public void setUp() {
     start(fakeApplication(inMemoryDatabase()));
   }
 
   @Test
   public void createNewNote() {
     Result result = callAction(controllers.routes.ref.Application.newNote());
 
     // Should return bad request if no data is given
     assertThat(status(result)).isEqualTo(BAD_REQUEST);
 
     result = callAction(
         controllers.routes.ref.Application.newNote(),
         fakeRequest().withFormUrlEncodedBody(ImmutableMap.of("text", "My note"))
     );
 
     // Should return redirect status if successful
     assertThat(status(result)).isEqualTo(SEE_OTHER);
     assertThat(redirectLocation(result)).isEqualTo("/");
 
     Note newNote = Note.find.where().eq("text", "My note").findUnique();
 
     // Should be saved to DB
     assertNotNull(newNote);
     assertEquals("My note", newNote.text);
   }
 
 
   @Test
   public void createInvalidNote() {
     Result result = callAction(
         controllers.routes.ref.Application.newNote(),
         fakeRequest().withFormUrlEncodedBody(ImmutableMap.of("text", ""))
     );
 
    // Should return bas request since text is required
     assertThat(status(result)).isEqualTo(BAD_REQUEST);
   }
}
