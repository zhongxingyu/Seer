 package models;
 
 import com.avaje.ebean.Ebean;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import play.libs.Yaml;
 import play.test.WithApplication;
 
 import play.Logger;
 
 import java.util.*;
 
 import static org.junit.Assert.*;
 import static play.test.Helpers.fakeApplication;
 import static play.test.Helpers.inMemoryDatabase;
 
 public class CommentsTest extends WithApplication {
 
 	private User testUser;
 	
 	@Before
 	public void setUp() {
 		start(fakeApplication(inMemoryDatabase()));
 		Ebean.save((List) Yaml.load("test-data.yml"));
 		testUser = User.findByEmail("pingu@notient.com");
 	}
 
 	@Test
 	public void createComment() {
     String title = "My test note";
     String content = "My test content";
 		Note note = Note.create(new Note(title, content), testUser);
 
     String commentContent = "My test comment";
  	Long commentId = Comment.create(note.id, new Comment(commentContent), testUser).id;
 
    Comment commentFromDB = Comment.find.ref(commentId);
   	assertNotNull(commentFromDB);
   	assertEquals(commentContent, commentFromDB.content);
 	}
 
 	@Test
 	public void deleteComment() {
     String title = "My test note";
     String content = "My test content";
     Note note = Note.create(new Note(title, content), testUser);
 
   	String commentContent = "My test comment";
     Long commentId = Comment.create(note.id, new Comment(commentContent), testUser).id;
 
   	Comment.delete(commentId);
  	Comment deletedComment = Comment.find.ref(commentId);
   	assertNull(deletedComment);
 	}
 }
