 /**
  * 
  */
 package net.sf.gilead.core.serialization;
 
 import net.sf.gilead.core.serialization.SerializationManager;
 import junit.framework.TestCase;
 
 /**
  * Test serialization manager behavior
  * @author bruno.marchesson
  *
  */
 public class SerializationManagerTest extends TestCase
 {
 	//-------------------------------------------------------------------------
 	//
 	// Test methods
 	//
 	//-------------------------------------------------------------------------
 	/**
 	 * Test Integer convertor
 	 */
 	public void testIntegerConversion()
 	{
 	//	Integer conversion
 	//
 		Integer value = new Integer(1);
		byte[] serialized = SerializationManager.getInstance().serialize(value);
 		assertEquals(value, SerializationManager.getInstance().unserialize(serialized));
 		
 	//	int conversion
 	//
 		int intValue = 1;
 		serialized = SerializationManager.getInstance().serialize(intValue);
 		assertEquals(intValue, SerializationManager.getInstance().unserialize(serialized));
 	}
 	
 	/**
 	 * Test Long convertor
 	 */
 	public void testLongConversion()
 	{
 	//	Integer conversion
 	//
 		Long value = new Long(1);
		byte[] serialized = SerializationManager.getInstance().serialize(value);
 		assertEquals(value, SerializationManager.getInstance().unserialize(serialized));
 		
 	//	int conversion
 	//
 		long longValue = 1;
 		serialized = SerializationManager.getInstance().serialize(longValue);
 		assertEquals(longValue, SerializationManager.getInstance().unserialize(serialized));
 	}
 }
