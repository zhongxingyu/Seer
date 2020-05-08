 package ch9k.chat;
 
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 public class ConversationSubjectTest {
     private ConversationSubject conversationSubject;
     private Conversation conversation;
     private String[] subjects;
 
     @Before
    public void setUp() {
        conversation = new Conversation(new Contact("JPanneel", null), true);
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
