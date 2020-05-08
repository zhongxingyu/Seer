 package no.niths.services;
 
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Mockito.when;
 
 import java.util.GregorianCalendar;
 import java.util.UUID;
 
 import no.niths.domain.Student;
 import no.niths.infrastructure.interfaces.StudentRepository;
 import no.niths.security.User;
 import no.niths.services.auth.RequestAuthenticationServiceImpl;
 
 import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
 @RunWith(MockitoJUnitRunner.class)
 public class RequestAuthenticationServiceTest {
 
 	@Mock
 	private StudentRepository studRepo;
 	
 	@InjectMocks
 	private RequestAuthenticationServiceImpl service = new RequestAuthenticationServiceImpl();
 	
 	@Test
 	public void testAuthenticate(){
 		service.setDecryptionPassword("password");
 		String token = getNormalToken();
 		when(studRepo.getStudentBySessionToken(token)).thenReturn(new Student("mail@nith.no"));
 		
 		User u = service.authenticate(token);
 		assertEquals(1, u.getAuthorities().size());
 		
 		token = getExpiredToken();
 		when(studRepo.getStudentBySessionToken(token)).thenReturn(new Student("mail@nith.no"));
 		
 		u = service.authenticate(token);
 		assertEquals(1, u.getAuthorities().size());
 		
 		u = service.authenticate(null);
 		assertEquals(1, u.getAuthorities().size());
 
		u = service.authenticate("token");
		assertEquals(1, u.getAuthorities().size());
 	}
 	
 	private String getExpiredToken(){
 		long tokenIssued = new GregorianCalendar().getTimeInMillis() - 60000;
 		String generatedToken = UUID.randomUUID().toString().toUpperCase()
 				+ "|" + Long.toString(1) + "|" + Long.toString(tokenIssued);
 		//Encrypt the token
 		StandardPBEStringEncryptor jasypt = new StandardPBEStringEncryptor();
 		jasypt.setPassword("password");
 		return jasypt.encrypt(generatedToken);		
 	}
 	
 	private String getNormalToken(){
 		long tokenIssued = new GregorianCalendar().getTimeInMillis();
 		String generatedToken = UUID.randomUUID().toString().toUpperCase()
 				+ "|" + Long.toString(1) + "|" + Long.toString(tokenIssued);
 		//Encrypt the token
 		StandardPBEStringEncryptor jasypt = new StandardPBEStringEncryptor();
 		jasypt.setPassword("password");
 		return jasypt.encrypt(generatedToken);
 	}
 }
