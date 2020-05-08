 package de.fhb.autobday.manager.mail;
 
 import javax.mail.Message;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 
 
 import org.easymock.EasyMock;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.api.easymock.PowerMock;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.legacy.PowerMockRunner;
 
 import com.stvconsultants.easygloss.javaee.JavaEEGloss;
 
 import de.fhb.autobday.dao.AbdContactFacade;
 import de.fhb.autobday.dao.AbdGroupFacade;
 import de.fhb.autobday.dao.AbdUserFacade;
 import de.fhb.autobday.manager.group.GroupManager;
 
 /**
  *
  * @author Michael Koppen <koppen@fh-brandenburg.de>
  */
 
 public class MailManagerTest {
 	
 	private JavaEEGloss gloss;
 	
	private MailManager managerUnderTest;
 	
 	private AbdUserFacade userDAOMock;
 	private Session sessionMock;
 	
 	public MailManagerTest() {
 	}
 
 	@BeforeClass
 	public static void setUpClass() throws Exception {
 	}
 
 	@AfterClass
 	public static void tearDownClass() throws Exception {
 	}
 	
 	@Before
 	public void setUp() {
 		
 		gloss= new JavaEEGloss();
 		
 		//create Mocks
 		userDAOMock = EasyMock.createMock(AbdUserFacade.class);
 		sessionMock = EasyMock.createMock(Session.class);
 		//set Objekts to inject
 		gloss.addEJB(userDAOMock);
 		
 		//create Manager with Mocks
		managerUnderTest=gloss.make(MailManager.class);
		managerUnderTest.setMailSession(sessionMock);
		
 	}
 	
 	@After
 	public void tearDown() {
 		
 	}
 
 	/**
 	 * Test of sendBdayMail method, of class MailManager.
 	 */
 	@Test
 	@Ignore
 	public void testSendBdayMail() throws Exception {
 		System.out.println("testSendBdayMail");
 		
 		InternetAddress address = new InternetAddress("ich@wir.de");
 		String to = "du@wir.de";
 		String subject = "Ein Test";
 		String body = "Der Test sollte funktionieren";
 
 		Message message = new MimeMessage(sessionMock);
 		message.setFrom(address);
 		message.setRecipients(Message.RecipientType.TO,InternetAddress.parse(to));
 		message.setSubject(subject);
 		message.setText(body);
 		
 		/*
 		Transport.send(message);
 		PowerMock.mockStatic(Transport.class);
 		Transport.send(message);
 		PowerMock.replay(Transport.class);
 		PowerMock.verifyAll();
 		*/
 	}
 
 	/**
 	 * Test of sendNotificationMail method, of class MailManager.
 	 */
 	@Test
 	@Ignore
 	public void testSendNotificationMail() throws Exception {
 		System.out.println("sendNotificationMail");
 	}
 
 	/**
 	 * Test of sendForgotPasswordMail method, of class MailManager.
 	 */
 	@Test
 	@Ignore
 	public void testSendForgotPasswordMail() throws Exception {
 		System.out.println("sendForgotPasswordMail");
 	}
 }
