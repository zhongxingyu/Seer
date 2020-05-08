 package fulbot.model.mail.incoming;
 
 import static fulbot.model.mail.MessageTestHelper.*;
 import static org.junit.Assert.*;
 import static org.mockito.Matchers.*;
 import static org.mockito.Mockito.*;
 
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 
 import fulbot.model.Event;
 import fulbot.model.mail.MessageHeaders;
 import fulbot.persistence.EventDao;
 
 public class MessageProcessorTest {
 
 	private EventDao eventDao;
 	private ContentReader contentReader;
 	private ContentProcessor contentProcessor;
 	private MessageProcessor messageProcessor;
 
 	@Before
 	public void setup() {
 		eventDao = mock(EventDao.class);
 		contentReader = mock(ContentReader.class);
 		contentProcessor = mock(ContentProcessor.class);
 		
 		messageProcessor = new MessageProcessor(eventDao, contentReader, contentProcessor);
 	}
 
 	@Test
 	public void shouldCreateAnEventWhenNoEventIsFoundForMessage() throws Exception {
 		MimeMessage message = createDefaultTestMessage();
 		when(eventDao.findForMessageId(anyString())).thenReturn(null);
 
 		messageProcessor.process(message);
 
 		ArgumentCaptor<Event> argCaptor = ArgumentCaptor.forClass(Event.class);
 		verify(eventDao).save(argCaptor.capture());
 		Event savedEvent = argCaptor.getValue();
 		assertEquals(message.getSubject(), savedEvent.getEmailData().getSubject());
 		assertEquals(message.getHeader(MessageHeaders.DELIVERED_TO, null), savedEvent.getEmailData().getAddress());
 		assertEquals(1, savedEvent.getEmailData().getReplyTo().size());
 		assertEquals(message.getFrom()[0].toString(), savedEvent.getEmailData().getReplyTo().get(0));
 		assertEquals(1, savedEvent.getEmailData().getReferences().size());
 		assertEquals(message.getMessageID(), savedEvent.getEmailData().getReferences().get(0));
 		assertTrue(savedEvent.getAttendance().isEmpty());
 	}
 
 	@Test
 	public void shouldFindEventUsingInReplyToHeader() throws Exception {
 		MimeMessage message = createDefaultTestMessage();
 		message.setHeader(MessageHeaders.IN_REPLY_TO, "first-message-id");
 		Event existingEvent = new Event();
 		when(eventDao.findForMessageId(eq("first-message-id"))).thenReturn(existingEvent);
 
 		messageProcessor.process(message);
 
 		ArgumentCaptor<Event> argCaptor = ArgumentCaptor.forClass(Event.class);
 		verify(eventDao).save(argCaptor.capture());
 		Event savedEvent = argCaptor.getValue();
 		assertEquals(existingEvent, savedEvent);
 	}
 
 	@Test
 	public void shouldUpdateEmailReferencesWhenEventIsFoundForMessage() throws Exception {
 		MimeMessage message = createDefaultTestMessage();
 		Event existingEvent = new Event();
 		existingEvent.getEmailData().setReferences(new ArrayList<String>(Arrays.asList("first-message-id", "second-message-id")));
 		when(eventDao.findForMessageId(anyString())).thenReturn(existingEvent);
 		
 
 		messageProcessor.process(message);
 
 		ArgumentCaptor<Event> argCaptor = ArgumentCaptor.forClass(Event.class);
 		verify(eventDao).save(argCaptor.capture());
 		Event savedEvent = argCaptor.getValue();
 		assertEquals(3, savedEvent.getEmailData().getReferences().size());
 		assertEquals(message.getMessageID(), savedEvent.getEmailData().getReferences().get(2));
 	}
 	
 	@Test
 	public void shouldReadMessageContentUsingContentReader() throws Exception {
 		MimeMessage message = createDefaultTestMessage();
 		when(eventDao.findForMessageId(anyString())).thenReturn(null);
 
 		messageProcessor.process(message);
 
 		verify(contentReader).read(eq(message), any(OutputStream.class));
 	}
 
 	@Test
 	public void shouldProcessMessageContentUsingContentProcessor() throws Exception {
 		String sender = "test.sender@domain.com";
 		MimeMessage message = createDefaultTestMessage();
 		message.setFrom(new InternetAddress(sender));
 		Event existingEvent = new Event();
 		existingEvent.setAttendance(new HashSet<String>(Arrays.asList("anAttendee", "anotherAttendee")));
 		when(eventDao.findForMessageId(anyString())).thenReturn(existingEvent);
 
 		messageProcessor.process(message);
 
 		verify(contentProcessor).process(anyString(), eq(sender), eq(existingEvent.getAttendance()));
 	}
 
 }
