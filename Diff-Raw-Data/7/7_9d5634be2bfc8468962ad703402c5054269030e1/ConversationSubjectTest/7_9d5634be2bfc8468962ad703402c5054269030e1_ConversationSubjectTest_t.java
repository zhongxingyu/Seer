 package ch9k.chat;
 
import java.net.InetAddress;
import java.net.UnknownHostException;
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 public class ConversationSubjectTest {
     private ConversationSubject conversationSubject;
     private Conversation conversation;
     private String[] subjects;
 
     @Before
    public void setUp() throws UnknownHostException {
        conversation = new Conversation(new Contact("JPanneel", InetAddress.getByName("google.be")), true);
         subjects = new String[] { "subject-a", "subject-b" };
         conversationSubject = new ConversationSubject(conversation, subjects);
     }
 
     /**
      * Test of getSubjects method, of class ConversationSubject.
      */
     @Test
     public void testGetSubjects() {
         assertArrayEquals(subjects, conversationSubject.getSubjects());
     }
 
      /**
      * Test of getConversation method, of class ConversationSubject.
      */
     @Test
     public void testGetConversation() {
         assertEquals(conversation, conversationSubject.getConversation());
     }
 }
