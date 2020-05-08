 package com.onb.otp.controller;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.LinkedHashSet;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.onb.otp.domain.OneTimePassword;
 import com.onb.otp.domain.OneTimePasswordList;
 import com.onb.otp.domain.OneTimePasswordListBatch;
 import com.onb.otp.domain.Status;
 import com.onb.otp.domain.User;
 import com.onb.otp.exception.InvalidRequestParameterException;
 import com.onb.otp.service.impl.PasswordService;
 
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 import static org.mockito.Mockito.doNothing;
 
 public class PasswordControllerTest {
 	private PasswordController controller;
 	private PasswordService passwordService;
 	private DateFormat sdf = new SimpleDateFormat("yyyyMMdd");
 	
 	@Before
 	public void setup() {
 		controller = new PasswordController();
 		
 		passwordService = mock(PasswordService.class);
 		
 		controller.passwordService = passwordService;
 	}
 	
 	@Test
 	public void generateOtpWithExpiryDate() throws Exception {
 		String expires = "20120101";
 		Date expiryDate = sdf.parse(expires);
 		
 		OneTimePasswordList otp = new OneTimePasswordList();
 		otp.setPasswords(new LinkedHashSet<OneTimePassword>());
 		
 	    when(passwordService.generatePasswordList(expiryDate)).thenReturn(otp);
 
 		controller.generateOtpWithExpiryDate(expires);
 	}
 	
 	@Test(expected = InvalidRequestParameterException.class)
 	public void generateOtpInvalidExpiryDate() throws Exception {
 		String expires = "";
 
 		controller.generateOtpWithExpiryDate(expires);
 	}
 	
 	@Test
 	public void generateOtpWithMaxAge() throws Exception, ParseException {
 		String expires = "20120101";
 		Date expiryDate = sdf.parse(expires);
 		
 		OneTimePasswordList otp = new OneTimePasswordList();
 		otp.setPasswords(new LinkedHashSet<OneTimePassword>());
 		
 	    when(passwordService.generatePasswordList(expiryDate)).thenReturn(otp);
 
 		controller.generateOtpWithMaxAge(expires);
 	}
 	
 	@Test
 	public void generateBatchOtpWithExpiryDate() throws Exception {
 		String expires = "20120101";
 		Date expiryDate = sdf.parse(expires);
 		String size = "20";
 		Integer batchSize = Integer.parseInt(size);
 		
 		OneTimePasswordListBatch batch = new OneTimePasswordListBatch();
 		batch.setPasswordLists(new LinkedHashSet<OneTimePasswordList>());
 		
 	    when(passwordService.generateBatchPasswordList(expiryDate, batchSize)).thenReturn(batch);
 
 		controller.generateBatchOtpWithExpiryDate(expires, size);
 	}
 	
 	@Test(expected = InvalidRequestParameterException.class)
 	public void generateBatchOtpInvalidExpiryDate() throws Exception {
 		String expires = "";
 		String size = "20";
 
 		controller.generateBatchOtpWithExpiryDate(expires, size);
 	}
 	
 	@Test(expected = InvalidRequestParameterException.class)
 	public void generateBatchOtpInvalidBatchSize() throws Exception {
 		String expires = "20120101";
 		String size = "AEO";
 
 		controller.generateBatchOtpWithExpiryDate(expires, size);
 	}
 	
 	@Test
 	public void generateBatchOtpWithMaxAge() throws Exception {
 		String expires = "20120101";
 		Date expiryDate = sdf.parse(expires);
 		String size = "20";
 		Integer batchSize = Integer.parseInt(size);
 		
 		OneTimePasswordListBatch batch = new OneTimePasswordListBatch();
 		batch.setPasswordLists(new LinkedHashSet<OneTimePasswordList>());
 		
 	    when(passwordService.generateBatchPasswordList(expiryDate, batchSize)).thenReturn(batch);
 
 		controller.generateBatchOtpWithMaxAge(expires, size);
 	}
 	
 	@Test
 	public void associateOtpListWithUser() {
 		User user = new User();
 		
 		OneTimePasswordList list = new OneTimePasswordList();
 		Status status = new Status();
 		list.setStatus(status);
 		list.setUser(user);
 		
 	    when(passwordService.associateOtpListWithUser(list, user)).thenReturn(list);
 	    
 		controller.associateOtpListWithUser(list, user);
 	}
 	
 	@Test
 	public void deleteOtpList() {
 		OneTimePasswordList list = new OneTimePasswordList();
 		
 		doNothing().when(passwordService).deleteOtpList(list);
 		
 		controller.deleteOtpList(list);
 	}
 }
