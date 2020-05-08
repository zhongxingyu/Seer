 package server.operations;
 
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import server.entities.Form;
 import server.entities.Newsletter;
 import server.exceptions.EmailSendingException;
 import server.queries.NewsletterQuery;
 import server.resources.FormResource;
 
 /**
  * Test for the generation of the newsLetter and URLs.
  * 
  * @author dennis.markmann
  * @since JDK.1.7.0_25
  * @version 1.0
  */
 
 public class NewsLetterHandlerTest extends TestCase {
 
 	private NewsLetterHandler handler;
 
 	private String schoolClass;
 	private String eMailAddress;
 	private String url;
 	private Form form;
 
 	@Override
 	@Before
 	public void setUp() {
 		this.handler = new NewsLetterHandler();
 		this.schoolClass = "it1a";
 		form = FormResource.getForms().get(0);
 		
 		this.eMailAddress = "test@test.de";
 	}
 
 	@Test
 	public void testUrlCreation() {
 
 		// TODO Edit url
		this.url = "nsa blabla/add_" + this.eMailAddress + "_to:" + this.schoolClass;
 
 		NewsLetterHandlerTest.assertEquals(this.url, this.handler.generateRegistrationLink(form, this.eMailAddress));
 
 	}
 
 	@Test
 	public void testAddressCreation() {
 
 		this.testUrlCreation();
 
 		boolean success = false;
 
 		NewsLetterHandlerTest.assertTrue(this.handler.confirmRegistration(form, eMailAddress));
 
 		final List<Newsletter> newsLetterList = new NewsletterQuery().getAllNewsletters();
 
 		for (final Newsletter newsLetter : newsLetterList) {
 			if (newsLetter.getEmail().getEMailAddress().equals(this.eMailAddress)) {
 				success = true;
 			}
 		}
 		NewsLetterHandlerTest.assertEquals(true, success);
 
 		try {
 			NewsLetterHandlerTest.assertTrue(this.handler.removeAddress(newsLetterList.get(0)));
 		} catch (EmailSendingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
