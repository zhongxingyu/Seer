 /**
  * SOEN 490
  * Capstone 2011
  * Test for MessageTDG.
  * Team members: 	Sotirios Delimanolis
  * 			Filipe Martinho
  * 			Adam Harrison
  * 			Vahe Chahinian
  * 			Ben Crudo
  * 			Anthony Boyer
  * 
  * @author Capstone 490 Team Moving Target
  *
  */
 
 package tests.foundation;
 
 import java.util.GregorianCalendar;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 
 import org.junit.Test;
 
 import foundation.Database;
 import foundation.MessageFinder;
 import foundation.MessageTDG;
 import static org.junit.Assert.*;
 
 public class MessageTDGTest {
 
	static BigInteger mid = new BigInteger("158749857935");
 	static BigInteger uid = new BigInteger("158749857934");
 
 	@Test
 	public void testFunctionality() throws SQLException, IOException {
 		boolean previousDatabase = Database.isDatabaseCreated();
 		if (!previousDatabase)
 			Database.createDatabase();
 		insert();
 		update();
 		delete();
 		if (!previousDatabase)
 			Database.dropDatabase();
 	}
 
 	private void insert() throws SQLException, IOException {
 		byte[] message = { 1, 2, 3, 4, 5, 6 };
 		float speed = 5.5f;
 		double latitude = 29.221;
 		double longitude = 35.134;
 		Timestamp createdDate = new Timestamp(new GregorianCalendar(2011, 9, 10).getTimeInMillis());
 		int user_rating = 6;
 		assertFalse(MessageFinder.find(mid).next());
 		assertEquals(MessageTDG.insert(mid, uid, message, speed,
 				latitude, longitude, createdDate, user_rating), 1);
 		ResultSet rs = MessageFinder.find(mid);
 		assertTrue(rs.next());
 		assertEquals(rs.getBigDecimal("m.mid").toBigInteger(), mid);
 		assertEquals(rs.getBigDecimal("m.uid").toBigInteger(), uid);
 
 		byte[] b = rs.getBytes("m.message");
 		for (int i = 0; i < message.length; i++) {
 			assertEquals(b[i], message[i]);
 		}
 
 		assertEquals(rs.getFloat("m.speed"), speed, 0.0001f);
 		assertEquals(rs.getDouble("m.latitude"), latitude, 0.0000001);
 		assertEquals(rs.getDouble("m.longitude"), longitude, 0.0000001);
 
 		assertEquals(createdDate, rs.getTimestamp("m.created_at"));
 
 		assertEquals(rs.getInt("m.user_rating"), user_rating);
 	}
 
 	private void update() throws SQLException, IOException {
 
 		byte[] message = { 1, 2, 3, 4, 5, 6 };
 		float speed = 5.5f;
 		double latitude = 29.221;
 		double longitude = 35.134;
 		Timestamp createdDate = new Timestamp(new GregorianCalendar(2011, 9, 10).getTimeInMillis());
 		int user_rating = 7;
 		assertEquals(1, MessageTDG.update(mid, user_rating));
 		ResultSet rs = MessageFinder.find(mid);
 		assertTrue(rs.next());
 		assertEquals(mid, rs.getBigDecimal("m.mid").toBigInteger());
 		assertEquals(uid, rs.getBigDecimal("m.uid").toBigInteger());
 		byte[] b = rs.getBytes("m.message");
 		for (int i = 0; i < message.length; i++) {
 			assertEquals(b[i], message[i]);
 		}
 		assertEquals(speed, rs.getFloat("m.speed"), 0.001f);
 		assertEquals(latitude, rs.getDouble("m.latitude"), 0.0000001);
 		assertEquals(longitude, rs.getDouble("m.longitude"), 0.0000001);
 		assertEquals(createdDate, rs.getTimestamp("m.created_at"));
 		assertEquals(user_rating, rs.getInt("m.user_rating"));
 	}
 
 	private void delete() throws SQLException, IOException {
 		assertTrue(MessageFinder.find(mid).next());
 		assertEquals(1, MessageTDG.delete(mid));
 		assertFalse(MessageFinder.find(mid).next());
 	}
 }
