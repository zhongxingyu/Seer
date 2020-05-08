 /**
  * SOEN 490
  * Capstone 2011
  * Test for MessageIdentityMapTest.
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
 
 package tests.domain.message;
 
 import static org.junit.Assert.*;
 
 import java.math.BigInteger;
 import java.sql.Timestamp;
 import java.util.GregorianCalendar;
 
 import org.junit.Test;
 import domain.message.Message;
 import domain.message.MessageFactory;
 import domain.message.MessageIdentityMap;
 
 public class MessageIdentityMapTest {
 	
 	@Test
 	public void testFunctionality()
 	{
 		MessageIdentityMap map = MessageIdentityMap.getUniqueInstance();
		Message message = MessageFactory.createClean(new BigInteger("0"), 0, null, 10.0f, 10.0, 10.0, new Timestamp(GregorianCalendar.getInstance().getTimeInMillis()), 0, 1);
 		map.put(new BigInteger("0"), message);
 		assertEquals(map.get(new BigInteger("0")), message);
 	}
 }
