 package de.fhb.autobday.manager.account;
 
 import static org.easymock.EasyMock.anyObject;
 import static org.easymock.EasyMock.createMock;
 import static org.easymock.EasyMock.expect;
 import static org.easymock.EasyMock.replay;
 import static org.easymock.EasyMock.verify;
 import static org.junit.Assert.assertEquals;
 
 import java.util.ArrayList;
import java.util.Collection;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 
 import org.easymock.EasyMock;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.stvconsultants.easygloss.javaee.JavaEEGloss;
 
 import de.fhb.autobday.dao.AbdAccountFacade;
 import de.fhb.autobday.dao.AbdUserFacade;
 import de.fhb.autobday.data.AbdAccount;
 import de.fhb.autobday.data.AbdGroup;
 import de.fhb.autobday.data.AbdUser;
 import de.fhb.autobday.exception.account.AccountAlreadyExsistsException;
 import de.fhb.autobday.exception.account.AccountException;
 import de.fhb.autobday.exception.account.AccountNotFoundException;
 import de.fhb.autobday.exception.connector.ConnectorException;
 import de.fhb.autobday.exception.user.NoValidUserNameException;
 import de.fhb.autobday.exception.user.UserNotFoundException;
 import de.fhb.autobday.manager.connector.google.GoogleImporter;
 
 public class AccountManagerTestIntegration {
 	
 	private static JavaEEGloss gloss;
 	
 	private static AccountManager managerUnderTest;
 	
 	private static AbdAccountFacade accountDAO;
 	
 	private static AbdUserFacade userDAO;
 	
 	private static GoogleImporter gImporter;
 	
 	private EntityManager emMock;
 	
 	@BeforeClass
 	public static void setUpClass(){
 		accountDAO = new AbdAccountFacade();
 		userDAO = new AbdUserFacade();
 		gImporter = new GoogleImporter();
 	}
 	
 	@Before
 	public void setUp() {
 		
 		gloss= new JavaEEGloss();
 		
 		//create Mocks
 		emMock = createMock(EntityManager.class);
 		
 		//set EntityManagers
 		accountDAO.setEntityManager(emMock);
 		userDAO.setEntityManager(emMock);
 		
 		//set Objekts to inject
 		gloss.addEJB(accountDAO);
 		gloss.addEJB(userDAO);
 		gloss.addEJB(gImporter);
 		
 		//create Manager with Mocks
 		managerUnderTest=gloss.make(AccountManager.class);
 	}
 	
 	/**
 	 * Test of addAccount method, of class AccountManager.
 	 */
 	@Test
 	public void testAddAccount() throws Exception {
 		System.out.println("testAddAccount");
 		
 		//prepare test variables
 		String password="password";
 		String userName="test@googlemail.com";
 		String type="type";	
		
		Collection<AbdAccount> abdAccountCollection = new ArrayList<AbdAccount>();
 
 		//prepare a user object
 		int userId=1;
 		AbdUser user = new AbdUser(userId, "mustermann", "password", "salt", "mustermann", "max");
		user.setAbdAccountCollection(abdAccountCollection);
 
 		// Setting up the expected value of the method call of Mockobject
 		expect(emMock.find(AbdUser.class, user.getId())).andReturn(user);
 		emMock.persist((AbdAccount)anyObject());
 		emMock.refresh((AbdUser)anyObject());
 		
 		// Setup is finished need to activate the mock
 		replay(emMock);
 		
 		// testing Methodcall
 		managerUnderTest.addAccount(userId, password, userName, type);
 		
 		//verify
 		verify(emMock);
 	}
 	
 	
 	/**
 	 * Test of removeAccount method, of class AccountManager.
 	 */
 	@Test
 	public void testRemoveAccount() throws Exception {
 		System.out.println("testRemoveAccountWithClass");
 
 		//prepare test variables
 		AbdAccount account = new AbdAccount(1);
 		
 		// Setting up the expected value of the method call of Mockobject
 		expect(emMock.find(AbdAccount.class, account.getId())).andReturn(account);
 		emMock.remove((AbdUser)anyObject());
 
 		// Setup is finished need to activate the mock
 		replay(emMock);
 		
 		// call method to test
 		managerUnderTest.removeAccount(account);
 		
 		//verify
 		verify(emMock);
 	}
 	
 //	/**
 //	 * Test of getAllContactsFromGroup method, of class GroupManager.
 //	 */
 //	@Test
 //	public void testGetAllContactsFromGroupWithInt() throws Exception {
 //		System.out.println("testGetAllContactsFromGroupWithInt");
 //		
 //		AbdGroup groupOne = new AbdGroup("1");
 //		AbdGroup groupTwo = new AbdGroup("2");
 //		
 //		AbdAccount account = new AbdAccount(22, "itsme", "itsme", "type");
 //		
 //		ArrayList<AbdGroup> outputCollection=new ArrayList<AbdGroup>();
 //		outputCollection.add(groupOne);
 //		outputCollection.add(groupTwo);
 //		
 //		account.setAbdGroupCollection(outputCollection);
 //		
 //
 //		EasyMock.expect(accountDAOMock.find(account.getId())).andStubReturn(account);
 //
 //		EasyMock.replay(accountDAOMock);
 //		
 //		assertEquals(outputCollection, managerUnderTest.getAllGroupsFromAccount(account.getId()));
 //		EasyMock.verify(accountDAOMock);
 //	}
 	
 	
 	/**
 	 * 
 	 * 
 	 */
 	@Test
 	public void testImportGroupsAndContacts(){
 		//TODO implement
 	}
 
 	
 	
 	/**
 	 * 
 	 * 
 	 */
 	@Test
 	public void testGetAllGroupsFromAccount(){
 		//TODO implement
 	}
 
 	
 	
 }
