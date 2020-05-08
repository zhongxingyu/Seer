 package org.fiteagle.core.userdatabase;
 
 import java.io.IOException;
 import java.security.NoSuchAlgorithmException;
 
 import junit.framework.Assert;
 
 import org.fiteagle.core.userdatabase.UserPersistable.DatabaseException;
 import org.fiteagle.core.userdatabase.UserPersistable.DuplicateUsernameException;
 import org.fiteagle.core.userdatabase.UserPersistable.UserNotFoundException;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class UserDBManagerTest {
   
   UserDBManager userDBManager ;
   
   User testUser;  
   
   @Before
   public void setUp() throws Exception {
     userDBManager = UserDBManager.getInstance();
     testUser = new User("test1", "test", "testName", "test@test.org", "testAffiliation", "password");
     try{
       userDBManager.add(testUser);
    } catch(DuplicateUsernameException e){      
     }
   }
  
   @Test
   public void testCreateUser() throws DuplicateUsernameException, NoSuchAlgorithmException, DatabaseException, IOException { 
     Assert.assertEquals("test", testUser.getFirstName());
     Assert.assertEquals("test1", testUser.getUsername());
     Assert.assertEquals("testName", testUser.getLastName());
     Assert.assertEquals("test@test.org", testUser.getEmail());
     Assert.assertNotNull(testUser.getPasswordHash());
     Assert.assertNotNull(testUser.getPasswordSalt());
   }  
  
   @Test
   public void testVerifyPassword() throws DuplicateUsernameException, NoSuchAlgorithmException, DatabaseException, IOException{    
     Assert.assertTrue(userDBManager.verifyPassword("password",testUser.getPasswordHash(),testUser.getPasswordSalt()));    
   }
   
   @Test
   public void testVerifyCredentials() throws UserNotFoundException, DatabaseException, IOException, NoSuchAlgorithmException{    
     Assert.assertTrue(userDBManager.verifyCredentials(testUser.getUsername(), "password"));    
   } 
   
   @Test
   public void testCreateUserCertAndPrivateKey() throws Exception{
     Assert.assertNotNull(userDBManager.createUserPrivateKeyAndCertAsString(testUser.getUsername(), "my passphrase"));
   }  
   
   @Test
   public void testCreateCertForPublicKey() throws Exception{
     UserPublicKey key = new UserPublicKey("ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAAAgQCarsTCyAf8gYXwei8rhJnLTqYI6P88weRaY5dW9j3DT4mvfQPna79Bjq+uH4drmjbTD2n3s3ytqupFfNko1F0+McstA2EEkO8pAo5NEPcreygUcB2d49So032GKGPETB8chRkDsaBCm/KKL2vXdQoicofli8JJRPK2kXfUW34qww==", "my first key");
     userDBManager.addKey(testUser.getUsername(), key);
     Assert.assertNotNull(userDBManager.createUserCertificateForPublicKey(testUser.getUsername(), key.getDescription()));
   }
   
   @After
   public void deleteTestUser(){
     userDBManager.delete(testUser);
   }
 }
