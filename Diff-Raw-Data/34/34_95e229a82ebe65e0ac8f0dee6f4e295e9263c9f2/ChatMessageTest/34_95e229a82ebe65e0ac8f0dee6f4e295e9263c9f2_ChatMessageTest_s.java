 package ch9k.chat;
 
 import java.util.Date;
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  * @author Jens Panneel
  */
 public class ChatMessageTest {
     private String author;
     private String text;
     private ChatMessage chatMessage;
 
     @Before
     public void setUp() {
         author = "JPanneel";
         text = "Hey! You!";
         chatMessage = new ChatMessage(author, text);
     }
 
     /**
      * Test of getText method, of class ChatMessage.
      */
     @Test
     public void testGetText() {
        assertEquals(chatMessage.getText(), text);
     }
 
     /**
      * Test of getAuthor method, of class ChatMessage.
      */
     @Test
     public void testGetAuthor() {
        assertEquals(chatMessage.getAuthor(), author);
     }
 
     /**
      * Test of getTime method, of class ChatMessage.
      */
     @Test
    public void testGetTime() {
        assertTrue(chatMessage.getTime().before(new Date()) || chatMessage.getTime().equals(new Date()));
     }
 
     /**
      * Test of compareTo method, of class ChatMessage.
      * @throws InterruptedException
      */
     @Test
     public void testCompareTo() throws InterruptedException {
         Thread.sleep(10); // so we get an older message
         ChatMessage newerChatMessage = new ChatMessage("Javache", "JaJa!");
 
         assertEquals(0,chatMessage.compareTo(chatMessage));
         assertTrue(chatMessage.compareTo(newerChatMessage) < 0);
         assertTrue(newerChatMessage.compareTo(chatMessage) > 0);
     }
 
     /**
      * Test of equals method, of class ChatMessage.
      */
     @Test
     public void testEquals() {
         assertTrue(chatMessage.equals(chatMessage));
         assertFalse(chatMessage.equals(new ChatMessage(author, "JaJa!")));
         assertFalse(chatMessage.equals(new ChatMessage("Javache", text)));
         assertFalse(chatMessage.equals(new ChatMessage("Javache", "JaJa!")));
     }
 
     /**
      * Test of hashCode method, of class ChatMessage.
      */
     @Test
     public void testHashCode() {
         ChatMessage differentMessage = new ChatMessage("Javache", "JaJa!");
         assertNotSame(chatMessage.hashCode(), differentMessage.hashCode());
     }
 
      /**
      * Test of toString method, of class ChatMessage.
      */
     @Test
     public void testToString() {
        //System.out.println(chatMessage.toString());
        fail("No assertions.");
     }
 }
