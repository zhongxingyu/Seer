 /*
  * $Id$
  * $Revision$
  * $Date$
  *
  * ====================================================================
  * Copyright (c) 2005, Topicus B.V.
  * All rights reserved.
  */
 package org.apache.wicket.security.hive.config;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.wicket.security.hive.Hive;
 import org.apache.wicket.security.hive.authorization.EverybodyPrincipal;
 import org.apache.wicket.security.hive.authorization.TestPermission;
 import org.apache.wicket.security.hive.authorization.TestPrincipal;
 import org.apache.wicket.security.hive.config.PolicyFileHiveFactory;
 
 import junit.framework.TestCase;
 
 /**
  * @author marrink
  */
 public class PolicyFileHiveFactoryTest extends TestCase
 {
 
 	/**
 	 * @param name
 	 */
 	public PolicyFileHiveFactoryTest(String name)
 	{
 		super(name);
 	}
 
 	/**
 	 * Test method for
 	 * {@link org.apache.wicket.security.hive.config.PolicyFileHiveFactory#addPolicyFile(java.net.URL)}.
 	 */
 	public void testAddPolicyFile()
 	{
 		PolicyFileHiveFactory factory = new PolicyFileHiveFactory();
 		factory.addPolicyFile(getClass().getResource("test-policy.hive"));
 		try
 		{
 			factory.addPolicyFile(new URL("http", "localhost", "foobar"));
 		}
 		catch (MalformedURLException e)
 		{
 			fail(e.getMessage());
 		}
 		factory.addPolicyFile(null);
 	}
 
 	/**
 	 * Test method for
 	 * {@link org.apache.wicket.security.hive.config.PolicyFileHiveFactory#createHive()}.
 	 */
 	public void testCreateHive()
 	{
 		PolicyFileHiveFactory factory = new PolicyFileHiveFactory();
 		factory.addPolicyFile(getClass().getResource("test-policy.hive"));
 		// setup aliases
 		factory.setAlias("perm", "org.apache.wicket.security.hive.authorization.TestPermission");
 		factory.setAlias("nine", "9");
 		factory.setAlias("test", "test");
 		factory.setAlias("auth", "org.apache.wicket.security.hive.authorization");
 		// based on policy content we can expect the following principals/permissions
 		Hive hive = factory.createHive();
 		assertTrue(hive.containsPrincipal(new EverybodyPrincipal()));
 		assertTrue(hive.containsPrincipal(new TestPrincipal("test1")));
 		assertTrue(hive.containsPrincipal(new TestPrincipal("test2")));
 		assertTrue(hive.containsPrincipal(new TestPrincipal("test6")));
 		assertTrue(hive.containsPermission(new TestPermission("A", "inherit, read")));
 		assertTrue(hive.containsPermission(new TestPermission("A", "execute")));
 		assertTrue(hive.containsPermission(new TestPermission("1.A", "inherit, read")));
 		assertTrue(hive.containsPermission(new TestPermission("1.A", "execute")));
 		assertFalse(hive.containsPermission(new TestPermission("2.A", "inherit, read")));
 		assertFalse(hive.containsPermission(new TestPermission("2.A", "execute")));
 		assertTrue(hive.containsPermission(new TestPermission("2.B", "inherit, read, write")));
 		assertTrue(hive.containsPermission(new TestPermission("2.B", "execute")));
 		assertTrue(hive.containsPermission(new TestPermission("2.C", "read, execute")));
 		assertTrue(hive.containsPermission(new TestPermission("2.C.1", "write")));
 		assertTrue(hive.containsPermission(new TestPermission("7.A", "inherit, read")));
 		assertTrue(hive.containsPermission(new TestPermission("7.A", "execute")));
 		assertTrue(hive.containsPermission(new TestPermission("7.B", "inherit, read, write")));
 		assertTrue(hive.containsPermission(new TestPermission("7.B", "execute")));
 		assertTrue(hive.containsPermission(new TestPermission("7.C", "read, execute")));
 		assertTrue(hive.containsPermission(new TestPermission("7.C.1", "write")));
 		assertFalse(hive.containsPermission(new TestPermission("6.A", "inherit, read")));
 		assertFalse(hive.containsPermission(new TestPermission("6.A", "execute")));
 		assertFalse(hive.containsPermission(new TestPermission("6.B", "inherit, read, write")));
 		assertFalse(hive.containsPermission(new TestPermission("6.B", "execute")));
 		assertFalse(hive.containsPermission(new TestPermission("6.C", "read, execute")));
 		assertFalse(hive.containsPermission(new TestPermission("6.C.1", "write")));
 		assertTrue(hive.containsPrincipal(new TestPrincipal("test8")));
 		assertTrue(hive.containsPermission(new TestPermission("8.A")));
 		assertTrue(hive.containsPermission(new TestPermission("8.B")));
 		assertTrue(hive.containsPrincipal(new TestPrincipal("test9")));
 		assertTrue(hive.containsPermission(new TestPermission("9.A")));
 		assertTrue(hive.containsPermission(new TestPermission("9.B", "test")));
 	}
 
 	public void testRegExPrincipalPattern()
 	{
 		Pattern principalPattern = null;
 		try
 		{
 			Field field = PolicyFileHiveFactory.class.getDeclaredField("principalPattern");
 			field.setAccessible(true);
 			principalPattern = (Pattern) field.get(null);
 		}
 		catch (IllegalArgumentException e)
 		{
 			fail(e.getMessage());
 		}
 		catch (SecurityException e)
 		{
 			fail(e.getMessage());
 		}
 		catch (IllegalAccessException e)
 		{
 			fail(e.getMessage());
 		}
 		catch (NoSuchFieldException e)
 		{
 			fail(e.getMessage());
 		}
 		assertNotNull(principalPattern);
 		assertFalse(principalPattern.matcher("").matches());
 		assertTrue(principalPattern.matcher("grant").matches());
 		assertTrue(principalPattern.matcher(
 				"grant principal org.apache.wicket.TestPrincipal \"render\"").matches());
 		assertFalse(principalPattern.matcher("grant foo").matches());
 		assertFalse(principalPattern.matcher(
 				"grant principal org.apache.wicket.TestPrincipal \"render").matches());
 		assertFalse(principalPattern.matcher(
 				"grant principal \"org.apache.wicket.TestPrincipal\" \"render\"").matches());
 		assertTrue(principalPattern.matcher(
 				"grant principal org.apache. wicket.TestPrincipal \"render\"").matches());
 		assertFalse(principalPattern.matcher(
 				"grant principal org.apache.wicket\".TestPrincipal \"render\"").matches());
 		assertFalse(principalPattern.matcher(
 				"grant principal org.apache.wicket.TestPrincipal \"render\" \"enable\"").matches());
 		assertTrue(principalPattern.matcher(
 				"grant principal org.apache.wicket.TestPrincipal \"some 'wicket' actions\"")
 				.matches());
 		assertFalse(principalPattern.matcher("grant principal \"org.apache.wicket.TestPrincipal\"")
 				.matches());
 		assertFalse(principalPattern.matcher(
 				"grant principal org.apache.wicket.TestPrincipal render").matches());
 	}
 
 	public void testRegExPermissionPattern()
 	{
 		Pattern permissionPattern = null;
 		try
 		{
 			Field field = PolicyFileHiveFactory.class.getDeclaredField("permissionPattern");
 			field.setAccessible(true);
 			permissionPattern = (Pattern) field.get(null);
 			assertTrue(permissionPattern.matcher(
 					"permission org.apache.wicket.TestPrincipal \"test\", \"render\";").matches());
 			assertTrue(permissionPattern.matcher(
 					"permission org.apache.wicket.TestPrincipal \"test\";").matches());
 			assertTrue(permissionPattern.matcher(
 					"permission org.apache.wicket.TestPrincipal \"test 123\", \"render\";")
 					.matches());
 			assertTrue(permissionPattern.matcher(
 					"permission org.apache.wicket.TestPrincipal \"test\", \"render 123\";")
 					.matches());
 			assertTrue(permissionPattern.matcher(
 					"permission org.apache.wicket.TestPrincipal \"test\", \"render 'wicket'\";")
 					.matches());
 			assertTrue(permissionPattern.matcher(
 					"permission org.apache. wicket.TestPrincipal \"test\", \"render 'wicket'\";")
 					.matches());
 			assertTrue(permissionPattern.matcher(
 					"permission org.apache.wicket.TestPrincipal \"test 'wicket'\", \"render\";")
 					.matches());
 			assertFalse(permissionPattern.matcher("permission org.apache.wicket.TestPrincipal;")
 					.matches());
 			assertFalse(permissionPattern.matcher("permission ").matches());
 			assertFalse(permissionPattern.matcher(
 					" org.apache.wicket.TestPrincipal \"test\", \"render\";").matches());
 			assertFalse(permissionPattern.matcher(
 					"permission org.apache.wicket.TestPrincipal \"test\" \"test\";").matches());
 			assertFalse(permissionPattern.matcher(
 					"permission org.apache.wicket.TestPrincipal \"test\", ;").matches());
 			assertFalse(permissionPattern.matcher(
 					"permission org.apache.wicket.TestPrincipal \"test\", \"render\"").matches());
 		}
 		catch (IllegalArgumentException e)
 		{
 			fail(e.getMessage());
 		}
 		catch (SecurityException e)
 		{
 			fail(e.getMessage());
 		}
 		catch (IllegalAccessException e)
 		{
 			fail(e.getMessage());
 		}
 		catch (NoSuchFieldException e)
 		{
 			fail(e.getMessage());
 		}
 		assertNotNull(permissionPattern);
 	}
 
 	public void testRegExAliasPattern()
 	{
 		Pattern aliasPattern = null;
 		try
 		{
 			Field field = PolicyFileHiveFactory.class.getDeclaredField("aliasPattern");
 			field.setAccessible(true);
 			aliasPattern = (Pattern) field.get(null);
 			assertFalse(aliasPattern.matcher("no alias used whatsoever").find());
 			Matcher m = aliasPattern.matcher("foo${bar}");
 			assertTrue(m.find());
 			assertEquals("${bar}", m.group());
 			assertFalse(m.find());
 			m = aliasPattern.matcher("${foo}bar");
 			assertTrue(m.find());
 			assertEquals("${foo}", m.group());
 			assertFalse(m.find());
 			m = aliasPattern.matcher("fo${o}bar");
 			assertTrue(m.find());
 			assertEquals("${o}", m.group());
 			assertFalse(m.find());
 			m = aliasPattern.matcher("${foo}${bar}");
 			assertTrue(m.find());
 			assertEquals("${foo}", m.group());
 			assertTrue(m.find());
 			assertEquals("${bar}", m.group());
 			assertFalse(m.find());
 			m = aliasPattern.matcher("${foo bar}foo${bar}");
 			assertTrue(m.find());
 			assertEquals("${foo bar}", m.group());
 			assertTrue(m.find());
 			assertEquals("${bar}", m.group());
 			assertFalse(m.find());
 			assertFalse(aliasPattern.matcher("${$foo}").find());
 			assertFalse(aliasPattern.matcher("${}").find());
 			assertFalse(aliasPattern.matcher("${foo\"bar\"}").find());
 			// regex alone is not enough to detect nested alias
 			// assertFalse(aliasPattern.matcher("${${foo}").find());
 			// assertFalse(aliasPattern.matcher("${${foo}bar}").find());
 		}
 		catch (IllegalArgumentException e)
 		{
 			fail(e.getMessage());
 		}
 		catch (SecurityException e)
 		{
 			fail(e.getMessage());
 		}
 		catch (IllegalAccessException e)
 		{
 			fail(e.getMessage());
 		}
 		catch (NoSuchFieldException e)
 		{
 			fail(e.getMessage());
 		}
 		assertNotNull(aliasPattern);
 	}
 
 	/*
 	 * test handling of nested aliases
 	 */
 	public void testResolveAliases()
 	{
 		try
 		{
 			Method method = PolicyFileHiveFactory.class.getDeclaredMethod("resolveAliases",
 					new Class[] {String.class});
 			method.setAccessible(true);
 			PolicyFileHiveFactory factory = new PolicyFileHiveFactory();
 			factory.setAlias("foo", "foo");
 			factory.setAlias("foobar", "foobar");
 			String result = (String) method.invoke(factory, new Object[] {"${${foo}bar}"});
 			fail("Unable to detect nested aliases: " + result);
 
 		}
 		catch (IllegalArgumentException e)
 		{
 			fail(e.getMessage());
 		}
 		catch (SecurityException e)
 		{
 			fail(e.getMessage());
 		}
 		catch (IllegalAccessException e)
 		{
 			fail(e.getMessage());
 		}
 		catch (NoSuchMethodException e)
 		{
 			fail(e.getMessage());
 		}
 		catch (InvocationTargetException e)
 		{
 			if (e.getCause() instanceof IllegalStateException)
 			{
				assertTrue(e.getCause().getMessage().toLowerCase().contains("nesting"));
 			}
 			else
 				fail(e.getMessage());
 		}
 	}
 	/*
 	 * test handling of nested aliases
 	 */
 	public void testResolveAliases2()
 	{
 		try
 		{
 			Method method = PolicyFileHiveFactory.class.getDeclaredMethod("resolveAliases",
 					new Class[] {String.class});
 			method.setAccessible(true);
 			PolicyFileHiveFactory factory = new PolicyFileHiveFactory();
 			factory.setAlias("foo", "foo");
 			factory.setAlias("foobar", "foobar");
 			String result = (String) method.invoke(factory, new Object[] {"${${foo}"});
 			fail("Unable to detect nested aliases: " + result);
 
 		}
 		catch (IllegalArgumentException e)
 		{
 			fail(e.getMessage());
 		}
 		catch (SecurityException e)
 		{
 			fail(e.getMessage());
 		}
 		catch (IllegalAccessException e)
 		{
 			fail(e.getMessage());
 		}
 		catch (NoSuchMethodException e)
 		{
 			fail(e.getMessage());
 		}
 		catch (InvocationTargetException e)
 		{
 			if (e.getCause() instanceof IllegalStateException)
 			{
				assertTrue(e.getCause().getMessage().toLowerCase().contains("nesting"));
 			}
 			else
 				fail(e.getMessage());
 		}
 	}
 }
