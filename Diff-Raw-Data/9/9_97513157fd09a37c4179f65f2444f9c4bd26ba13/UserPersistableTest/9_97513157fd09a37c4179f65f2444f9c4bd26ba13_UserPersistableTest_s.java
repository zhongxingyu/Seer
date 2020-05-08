 package org.fiteagle.core.userdatabase;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.Date;
 
 import org.fiteagle.core.userdatabase.UserPersistable.DatabaseException;
 import org.fiteagle.core.userdatabase.UserPersistable.DuplicateUsernameException;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public abstract class UserPersistableTest {
 
 	protected static UserPersistable database;
 	
 	private static final ArrayList<String> KEYS1 = new ArrayList<String>();
 	private static final ArrayList<String> KEYS2 = new ArrayList<String>();	
 	private static User USER1;
 	private static User USER2;
 	private static User USER3;
 	private static User USER4;
 	
 	@BeforeClass
 	public static void createUsers() throws DatabaseException, DuplicateUsernameException, NoSuchAlgorithmException, IOException{
 	  KEYS1.add("ssh-rsa AAAAB3NzaC1ydzkACAADAQABAAABAQCybYW812Eb9aTxrXnFgIG7etEijX3/+pWlurrYpvqXi6rl0LZWnotWaC0TeBKWMwDAwPDnSeMxGtYDrZXQJNurrdsmYtzJSL79hhLJqsQCv4s5tK+d/GPRsPSfsGI0A+ckDiQ7yXErUSIgcmGXC4Jo6tuN0QI3x3wIlivDMwkVxZm4m82LwqVECtodnvzbct13a9rIhgjGTRyXXsLVt+X1MB45OlQJ+CWWkaO3emRHDDktZAjkhXNXYKeDtXj4yIhy+jPLTSKwsghCQD79U+sQEDY+RBPu7Td5GzQx8tFdFAjghZaWgeD3iRmpcr8tukR+jG1ynL0zrzumFf4Cg359 mitja@mitja-Precision-WorkStation-370");
     KEYS1.add("ssh-rsa AAAAB3NzaC2yc2EACAADATZCAAABAQCybYW812Eb9aTxrXnFgIG7etEijX3/+pWlurrYpvqXi6rl0LZWnotWaC0TeBKWMwDAwPDnSeMxGtYDrZXQJNurrdsmYtzJSL79hhLJqsQCv4s5tK+d/GPRsPSfsGI0A+ckDiQ7yXErUSIgcmGXC4Jo6tuN0QI3x3wIlivDMwkVxZm4m82LwqVECtodnvzbct13a9rIhgjGTRyXXsLVt+X1MB45OlQJ+CWWkaO3emRHDDktZAjkhXNXYKeDtXj4yIhy+jPLTSKiObJnCQD79U+sQEDY+RBPu7Td5GzQx8tFd34gesatWgeDiRmpcr8tukR+jG1ynL0zrzumFf4Cg359 mitja@mitja-Precision-WorkStation-370");
     KEYS2.add("ssh-rsa AAAAB3NzaC3yc2EACAADAQABAAABAQCybYW812Eb9aTxrXnFgIG7etEijX3/+pWlurrYpvqXi6rl0LZWnotWaC0TeBKWMwDAwPDnSeMxGtYDrZXQJNurrdsmYtzJSL79hhLJqsQCv4s5tK+d/GPRsPSfsGI0A+ckDiQ7yXErUSIgcmGXC4Jo6tuN0QI3x3wIlivDMwkVxZm4m82LwqVECtodnvzbct13a9rIhgjGTRyXXsLVt+X1MB45OlQJ+CWWkaO3emRHDDktZAjkhXNXYKeDtXj4yIhy+jPLTSKiObJnCQD79U+sQEDY+RBPu7Td5GzQx8tFdFAjghZaWgeDiRmpcr8tukR+jG1ynL0zrzumFf4Cg359 mitja@mitja-Precision-WorkStation-370");
 	  USER1 = new User("mnikolaus", "mitja", "nikolaus", "mitja@test.org", "mitjasAffiliation", "mitjasPassword", KEYS1);
 	  USER2 = new User("hschmidt", "hans", "herbert", "hschmidt@test.org", "hansAffiliation", "hansPassword", KEYS2);
 	  USER3 = new User("hschmidt", "herbert", "herbert", "hschmidt@test.org", "herbertAffiliation", "herbertsPassword", KEYS1);
 	  USER4 = new User("mnikolaus", "mitja", "nikolaus", "mitja@test.org", "mitjaAffiliation", "mitjasPassword", new ArrayList<String>());	  
 	}
 	
 	@Before
 	public void setUpAndCleanUp() throws DatabaseException{
 	  setUp();
     cleanUp();
   }
 	
 	protected abstract void setUp();
 	
 	@AfterClass
   public static void cleanUp() {
     database.delete(USER1);
     database.delete(USER2);
     database.delete(USER3);
     database.delete(USER4);
   } 
 
   @Test
 	public void testAdd() throws DatabaseException{
 		int numberOfUsers = database.getNumberOfUsers();
 		database.add(USER1);
 		assertEquals(numberOfUsers+1,database.getNumberOfUsers());
 	}
 	
 	@Test(expected=UserPersistable.DuplicateUsernameException.class)
 	public void testAddFails() throws DatabaseException{
 		database.add(USER2);
 		database.add(USER3);
 	}
 	
 	@Test
 	public void testGet() throws DatabaseException{		
 		database.add(USER1);
 		assertTrue(USER1.equals(database.get(USER1)));
 	}
 
 	@Test
 	public void testGetUserWhoHasNoKeys() throws DatabaseException, DuplicateUsernameException, NoSuchAlgorithmException{
 		database.add(USER4);
 		assertTrue(USER4.equals(database.get(USER4)));
 
 	}
 	
 	@Test(expected=UserPersistable.RecordNotFoundException.class)
 	public void testGetFails() throws DatabaseException{
 		database.add(USER1);
 		database.get(USER2);
 	}
 	
 	@Test(expected=UserPersistable.RecordNotFoundException.class)
 	public void testDelete() throws DatabaseException{
 		database.add(USER1);		
 		database.delete(USER1);		
 		database.get(USER1);
 	}
 		
 	@Test
 	public void testUpdate() throws DatabaseException{
 		database.add(USER2);
 		Date created = USER2.getCreated();
 		database.update(USER3);
 		User updatedUser = database.get(USER3);
 		assertTrue(USER3.equals(updatedUser));
 		assertTrue(created.before(updatedUser.getLast_modified()));
 	}
 	
 	@Test
 	public void testUpdateWithFewArguments(){
 	  database.add(USER1);
 	  database.update(new User("mnikolaus", "martin", null, null, null, null, null, null, null, null));
 	  assertEquals("martin", database.get(USER1.getUsername()).getFirstName());
 	  assertEquals("nikolaus", database.get(USER1.getUsername()).getLastName());
 	}
 	
 	@Test(expected=UserPersistable.RecordNotFoundException.class)
 	public void testUpdateFails() throws DatabaseException{
 		database.update(USER3);
 	}
 	
 	@Test
 	public void testAddKey() throws DatabaseException{
 	  database.add(USER1);		
		database.addKey(USER1.getUsername(), KEYS2.get(0));
		assertTrue(database.get(USER1).getPublicKeys().contains(KEYS2.get(0)));
 	}
 		
 	@Test
 	public void testAddDuplicateKeys() throws DatabaseException{
 		database.add(USER1);		
 		database.addKey(USER1.getUsername(), KEYS1.get(0));
 		assertTrue(USER1.equals(database.get(USER1)));
 	}
 	
 	@Test
 	public void testDeleteKey() throws DatabaseException{
 	  database.add(USER2);
	  int numberOfKeys = database.get(USER2).getPublicKeys().size();
 	  database.deleteKey(USER2.getUsername(), KEYS2.get(0));
	  assertEquals(numberOfKeys-1, database.get(USER2).getPublicKeys().size());
 	}	
 }
