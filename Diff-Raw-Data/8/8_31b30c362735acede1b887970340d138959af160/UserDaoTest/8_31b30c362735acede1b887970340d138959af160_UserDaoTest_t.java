 package com.nearme;
 
 import static org.junit.Assert.*;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
 
 public class UserDaoTest {
 	
 	private UserDAO uf = null;
 	private MysqlConnectionPoolDataSource dataSource = null;
 	
 	@Before
 	public void setUp() {
 		uf = new UserDAODummyImpl();
 
 		dataSource = new MysqlConnectionPoolDataSource();
 		dataSource.setUser("nearme");
 		dataSource.setPassword("nearme");
 		dataSource.setUrl("jdbc:mysql://localhost/nearme");
 		
 		/* Reset the database to a known good state before each test */
 
 		try {
 			ArbitrarySQLRunner asr = new ArbitrarySQLRunner(dataSource);
 			asr.runSQL(getClass().getResourceAsStream("/Schema.sql"));
 	
 			uf = new UserDAOImpl(dataSource);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Test
 	public void testReadUser() throws SQLException {
 		Calendar c = Calendar.getInstance();
 		c.set(Calendar.YEAR, 2011);
 		c.set(Calendar.MONTH, 10);
 		c.set(Calendar.DAY_OF_MONTH, 11);
 		c.set(Calendar.HOUR_OF_DAY, 0);
 		c.set(Calendar.MINUTE, 0);
 		c.set(Calendar.SECOND, 0);
 		c.set(Calendar.MILLISECOND, 0);
 		System.err.println(c.getTime());
 
 		User testUser = new User(1, "android-123456", "hash-1234567890", new Position(-123.45, 67.89, c.getTime()));
 
 		assertEquals(testUser, uf.read(1));
 		
 		assertEquals(testUser, uf.readByHash("hash-1234567890"));
 		assertNull(uf.readByHash("non-existent-hash"));
 		assertNull(uf.readByHash("android-123456")); 
 
 		assertEquals(testUser, uf.readByDeviceId("android-123456"));
 		assertNull(uf.readByDeviceId("hash-1234567890"));
 		assertNull(uf.readByDeviceId("non-existent-id"));
 	}
 
 	@Test
 	public void testReadAddressBookNonExistentUser() throws SQLException {
 		List<AddressBookEntry> book = uf.getAddressBook(12345); // non-existent user
 		assertNull(book);
 	}
 
 	@Test
 	public void testReadAddressBookEmpty()  throws SQLException {
 		List<AddressBookEntry> book = uf.getAddressBook(2); // existing user, no book
 		assertNotNull(book);
 		assertEquals(0, book.size());
 	}
 
 	@Test
 	public void testReadAddressBookFull() throws SQLException {
 		User u = uf.read(1);
 		List<AddressBookEntry> book = uf.getAddressBook(1); // existing user, with book
 		assertNotNull(book);
 		assertEquals(3, book.size());
 		
 		IdentityHash hash3 = new IdentityHash(3, "hash-aaaaaaaaaa");
 		IdentityHash hash4 = new IdentityHash(4, "hash-bbbbbbbbbb");
 		IdentityHash hash5 = new IdentityHash(5, "hash-cccccccccc");
 
 		ArrayList<IdentityHash> list3 = new ArrayList<IdentityHash>();
 		list3.add(hash3);
 		
 		ArrayList<IdentityHash> list4 = new ArrayList<IdentityHash>();
 		list4.add(hash4);
 		
 		ArrayList<IdentityHash> list5 = new ArrayList<IdentityHash>();
 		list5.add(hash5);
 		
 		assertEquals(new AddressBookEntry(2, u, "Dick", AddressBookEntry.PERM_HIDDEN, list4), book.get(0));
 		assertEquals(new AddressBookEntry(3, u, "Harry", AddressBookEntry.PERM_HIDDEN, list5), book.get(1));
 		assertEquals(new AddressBookEntry(1, u, "Tom", AddressBookEntry.PERM_HIDDEN, list3), book.get(2));
 	}
 
 	@Test
 	public void testAddNewUser() throws SQLException {
 		User u = new User();
 		u.setDeviceId("device-id");
 		u.setMsisdnHash("msisdn-hash");
 		assertEquals(User.NO_ID, u.getId());
 		u = uf.write(u);
 		
 		// check the returned User has been allocated a new ID
 		assert(User.NO_ID!=u.getId());
 		
 		// check we can read the user from the database, and it's the same one
 		
 		User u2 = uf.read(u.getId());
 		assertEquals (u2, u);
 	}
 
 	@Test
 	public void testUpdateExistingUser() throws SQLException {
 		String userCountSQL = "SELECT COUNT(id) FROM user";
 		ArbitrarySQLRunner asr = new ArbitrarySQLRunner(dataSource);
 		int numUsersBefore = asr.runQuery(userCountSQL);
 		String newHash = "updated-hash";
 		User u = uf.read(2);
 		u.setMsisdnHash(newHash);
 		uf.write(u);
 		
 		/* Check we have read the same user out */
 		
 		User u2 = uf.read(2);
 		assertEquals(newHash, u2.getMsisdnHash());
 		assertEquals(u, u2);
 		
 		/* Check there are no new users */
 		
 		int numUsersAfter = asr.runQuery(userCountSQL);
 		assertEquals(numUsersAfter, numUsersBefore);
 	}
 
 }
