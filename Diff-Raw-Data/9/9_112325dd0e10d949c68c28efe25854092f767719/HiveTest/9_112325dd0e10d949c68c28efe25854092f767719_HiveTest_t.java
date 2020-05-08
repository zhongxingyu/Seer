 package org.apache.wicket.security.hive;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.wicket.security.hive.BasicHive;
 import org.apache.wicket.security.hive.authorization.EverybodyPrincipal;
 import org.apache.wicket.security.hive.authorization.TestPermission;
 
 import junit.framework.TestCase;
 
 public class HiveTest extends TestCase
 {
 
 	public HiveTest(String name)
 	{
 		super(name);
 	}
 
 	public void testAddPrincipal()
 	{
 		BasicHive hive = new BasicHive();
 		try
 		{
 			hive.addPrincipal(new EverybodyPrincipal(), null);
 			fail("should not be possible to add null permission.");
 		}
 		catch (IllegalArgumentException e)
 		{
 			assertNotNull(e);
 		}
 		assertFalse(hive.containsPrincipal(new EverybodyPrincipal()));
 		List permissions = new ArrayList();
 		permissions.add(new TestPermission("foo.bar"));
 		permissions.add(new TestPermission("test"));
 		hive.addPrincipal(new EverybodyPrincipal(), permissions);
 		assertTrue(hive.containsPrincipal(new EverybodyPrincipal()));
 	}
 
 	public void testAddPermission()
 	{
 		BasicHive hive = new BasicHive();
 		try
 		{
 			hive.addPermission(new EverybodyPrincipal(), null);
 			fail("should not be possible to add null permission.");
 		}
 		catch (IllegalArgumentException e)
 		{
 			assertNotNull(e);
 		}
 		assertFalse(hive.containsPrincipal(new EverybodyPrincipal()));
 		hive.addPermission(new EverybodyPrincipal(), new TestPermission("foobar"));
 		assertTrue(hive.containsPrincipal(new EverybodyPrincipal()));
 	}
 
 	public void testHasPermision()
 	{
 		BasicHive hive = new BasicHive();
 		assertFalse(hive.containsPrincipal(new EverybodyPrincipal()));
 		hive.addPermission(new EverybodyPrincipal(), new TestPermission("foobar"));
 		assertTrue(hive.containsPrincipal(new EverybodyPrincipal()));
 		assertTrue(hive.hasPermision(null, new TestPermission("foobar")));
 		assertFalse(hive.hasPermision(null, new TestPermission("foo.bar")));
 		
 		hive.addPermission(new EverybodyPrincipal(), new TestPermission("test","read, write"));
 		assertTrue(hive.hasPermision(null, new TestPermission("test","read")));
 		assertTrue(hive.hasPermision(null, new TestPermission("test")));
 	}
 }
