 package com.johnburbridge.email;
 
 import static com.johnburbridge.email.EmailSender.*; 
 
 import static org.junit.Assert.*;
 
import java.io.PrintWriter;

 import javax.servlet.http.HttpServletRequest;
 
 import org.junit.Test;
 import org.mockito.Mockito;
import org.mockito.internal.matchers.Any;
 
 /**
  * @author jburbridge
 * @since 
  */
 public class EmailSenderTest {
 
 	/**
 	 * Test method for {@link com.johnburbridge.email.EmailSender#validateParamsNotNull(javax.servlet.http.HttpServletRequest)}.
 	 */
 	@Test
 	public void testValidateParamsNotNull() {
 		
 		assertFalse("The paramters should have been null but the validator says otherwise", 
 				EmailSender.validateParamsNotNull(createMockRequest(false)));
 		
 		assertTrue("Validation should have returned true but something went wrong", 
 				EmailSender.validateParamsNotNull(createMockRequest(true)));
 	}
 
 	/**
 	 * Test method for {@link com.johnburbridge.email.EmailSender#sendMessage(javax.servlet.http.HttpServletRequest)}.
 	 */
 	@Test
 	public void testSendMessageHttpServletRequest() {
 		try {
 			EmailSender.sendMessage(createMockRequest(false));
 			fail("sendMessage should have thrown an exception but didn't");
 		} catch (Exception e) {
 			assertTrue(e instanceof NullPointerException);
 		}
 		try {
 			// FIXME: the smtp server is down and needs to be fixed for this to work
 			// EmailSender.sendMessage(createMockRequest(true));
 		} catch (Exception e) {
 			fail("sendMessage should not have thrown an exception but did");
 		}
 	}
 
 	private HttpServletRequest createMockRequest(boolean valid) {
 		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
 		if (valid) {
 			Mockito.when(request.getParameter(NAME_PARAM)).thenReturn("John Doe");
 			Mockito.when(request.getParameter(EMAIL_PARAM)).thenReturn("jdoe@foo.bar");
 			Mockito.when(request.getParameter(MESSAGE_PARAM)).thenReturn("OHAI DER!");
 		}
 		return request;
 	}
 
 }
