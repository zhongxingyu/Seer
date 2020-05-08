 package com.thoughtworks.twu.service;
 import com.thoughtworks.twu.domain.Message;
 import org.junit.Test;
 
 
 import java.util.List;
 
 import static junit.framework.Assert.assertEquals;
 
 public class MessageServiceTest {
         @Test
         public void shouldFetchListOfAllMessages() {
             MessageService service = new MessageService();
             List<Message> messages = service.getAllMessages();
 
            assertEquals(15, messages.size());
         }
 
         @Test
         public void shouldReturnMessageForMessageId(){
             MessageService service = new MessageService();
             Message message = service.getMessageMessageById("NoMatchingActivity");
 
             assertEquals("No matching activity found.",message.getMessage());
 
         }
        @Test
        public void shouldReturnMessageForId()
        {
            MessageService messageService = new MessageService();
            String message = messageService.getMessageForField(4);
            assertEquals("No matching activity found.",message);
 
        }
 }
