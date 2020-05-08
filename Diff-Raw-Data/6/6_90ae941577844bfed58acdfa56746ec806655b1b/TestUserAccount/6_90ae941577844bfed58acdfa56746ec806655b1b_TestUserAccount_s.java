 package server;
 
 import static org.junit.Assert.*;
 import java.util.*;

 import org.junit.Before;
 import org.junit.Test;
 import static org.mockito.Mockito.*;
 
 public class TestUserAccount {
 	
 	ServerEmailAttachment a;
 	ServerEmailHead eh;
 	ServerEmail e;
 	ServerEmail e2;
 	User u;
 	UserAccount ua;
 
 	@Before
 	public void setUp() throws Exception {
 
 		ArrayList<ServerEmailAttachment> at = new ArrayList<ServerEmailAttachment>();
 		ArrayList<ServerEmail> em = new ArrayList<ServerEmail>();
 		//ArrayList<UserAccount>uas = new ArrayList<UserAccount>();
		Date d;
		d = new Date();
 		a = mock(ServerEmailAttachment.class);
 		when(a.getFile()).thenReturn("hi");
 		when(a.getFileName()).thenReturn("aName");
 		at.add(a);
 		eh = mock(ServerEmailHead.class);
 		when(eh.getDate()).thenReturn(d);
 		when(eh.getReciver()).thenReturn("Jfflores90@gmail.com");
 		when(eh.getSender()).thenReturn("lalo93@gmail.com");
 		when(eh.getSubject()).thenReturn("I've a question");
 		e = mock(ServerEmail.class);
 		em.add(e);
 		when(e.getBody()).thenReturn("Am I ugly?");
 		when(e.getHead()).thenReturn(eh);
 		when(e.getAttachment()).thenReturn(at);
 		e2 = mock(ServerEmail.class);
 		when(e2.getAttachment()).thenReturn(at);
 		when(e2.getHead()).thenReturn(eh);
 		when(e2.getBody()).thenReturn("Hello");
 		u = mock(User.class);
 		when(u.getName()).thenReturn("Fede");
 		when(u.getPassword()).thenReturn("is secret");
 		when(u.getUserName()).thenReturn("Jfflores90@gmail.com");
 		ua = new UserAccount(u);
 		ua.getUserEmail().add(e);
 	}
 
 	@Test
 	public void testgetUserAccount() {
 	  assertEquals(ua.getUserAccount(),u);
 	}
 	
 	@Test
 	public void testgetUserEmail(){
 		assertEquals(ua.getUserEmail().get(0).getBody(),e.getBody());
 	}
 	
 	@Test
 	public void testsendEmailComplete() throws Exception{
 		assertEquals(ua.sendEmailComplete(eh),e);
 	}
 
 }
