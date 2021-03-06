 /**********************************************************************
  * Copyright (c) 2002 IBM Corporation and others.
  * All rights reserved.   This program and the accompanying materials
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors: 
  * IBM - Initial API and implementation
  **********************************************************************/
 package org.eclipse.core.tests.resources;
 
 import java.util.*;
 import junit.framework.Test;
 import junit.framework.TestSuite;
 import org.eclipse.core.resources.*;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.tests.harness.EclipseWorkspaceTest;
 
 /**
  * Tests path variables.
  */
 public class IPathVariableTest extends EclipseWorkspaceTest {
 	IPathVariableManager manager = getWorkspace().getPathVariableManager();
 
 	class PathVariableChangeVerifier implements IPathVariableChangeListener {
 		class VerificationFailedException extends Exception {
 			VerificationFailedException(String message) {
 				super(message);
 			}
 		}
 		class Event {
 			int type;
 			String name;
 			IPath value;
 			Event(int type, String name, IPath value) {
 				this.type = type;
 				this.name = name;
 				this.value = value;
 			}
 			public boolean equals(Object obj) {
 				if (obj == null || !(obj instanceof Event))
 					return false;
 				Event that = (Event) obj;
 				if (this.type != that.type || !this.name.equals(that.name))
 					return false;
 				if (this.value == null)
 					return that.value == null;
 				else
 					return this.value.equals(that.value);
 			}
 			public String toString() {
 				StringBuffer buffer = new StringBuffer();
 				buffer.append("Event(");
 				buffer.append("type: ");
 				buffer.append(stringForType(type));
 				buffer.append(" name: ");
 				buffer.append(name);
 				buffer.append(" value: ");
 				buffer.append(value);
 				buffer.append(")");
 				return buffer.toString();
 			}
 			String stringForType(int type) {
 				switch (type) {
 					case IPathVariableChangeEvent.VARIABLE_CREATED :
 						return "CREATED";
 					case IPathVariableChangeEvent.VARIABLE_CHANGED :
 						return "CHANGED";
 					case IPathVariableChangeEvent.VARIABLE_DELETED :
 						return "DELETED";
 					default :
 						return "UNKNOWN";
 				}
 			}
 		}
 		List expected = new ArrayList();
 		List actual = new ArrayList();
 		void addExpectedEvent(int type, String name, IPath value) {
 			expected.add(new Event(type, name, value));
 		}
 		void verify() throws VerificationFailedException {
 			String message;
 			if (expected.size() != actual.size()) {
 				message = "Expected size: " + expected.size() + " does not equal actual size: " + actual.size() + "\n";
 				message += dump();
 				throw new VerificationFailedException(message);
 			}
 			for (Iterator i = expected.iterator(); i.hasNext();) {
 				Event e = (Event) i.next();
 				if (!actual.contains(e)) {
 					message = "Expected and actual results differ.\n";
 					message += dump();
 					throw new VerificationFailedException(message);
 				}
 			}
 		}
 		void reset() {
 			expected = new ArrayList();
 			actual = new ArrayList();
 		}
 		public void pathVariableChanged(IPathVariableChangeEvent event) {
 			actual.add(new Event(event.getType(), event.getVariableName(), event.getValue()));
 		}
 		String dump() {
 			StringBuffer buffer = new StringBuffer();
 			buffer.append("Expected:\n");
 			for (Iterator i = expected.iterator(); i.hasNext();)
 				buffer.append("\t" + i.next() + "\n");
 			buffer.append("Actual:\n");
 			for (Iterator i = actual.iterator(); i.hasNext();)
 				buffer.append("\t" + i.next() + "\n");
 			return buffer.toString();
 		}
 	}
 
 	/**
 	 * Default constructor for this class. 
 	 */
 	public IPathVariableTest() {
 	}
 	/**
 	 * Constructor for the class. 
 	 */
 	public IPathVariableTest(String name) {
 		super(name);
 	}
 	/**
 	 * Return the tests to run.
 	 *  
 	 * @see org.eclipse.core.tests.harness.EclipseWorkspaceTest#suite()
 	 */
 	public static Test suite() {
 		return new TestSuite(IPathVariableTest.class);
 	}
 	/**
 	 * Test IPathVariableManager#getPathVariableNames
 	 */
 	public void testGetPathVariableNames() {
 		String[] names = null;
 
 		// should be empty to start
 		assertTrue("0.0", manager.getPathVariableNames().length == 0);
 
 		// add one
 		manager.setValue("one", getRandomLocation());
 		names = manager.getPathVariableNames();
 		assertTrue("1.0", names.length == 1);
 		assertTrue("1.1", names[0].equals("one"));
 
 		// add another
 		manager.setValue("two", Path.ROOT);
 		names = manager.getPathVariableNames();
 		assertTrue("2.0", names.length == 2);
 		assertTrue("2.1", contains(names, "one"));
 		assertTrue("2.2", contains(names, "two"));
 
 		// remove one
 		manager.setValue("one", null);
 		names = manager.getPathVariableNames();
 		assertTrue("3.0", names.length == 1);
 		assertTrue("3.1", names[0].equals("two"));
 
 		// remove the last one	
 		manager.setValue("two", null);
 		names = manager.getPathVariableNames();
 		assertTrue("3.0", names.length == 0);
 	}
 	/**
 	 * Test IPathVariableManager#getValue and IPathVariableManager#setValue
 	 */
 	public void testGetSetValue() {
 		IPath pathOne = new Path("c:\\temp");
 		IPath pathTwo = new Path("/tmp/backup");
 		IPath pathOneEdit = new Path("d:/foobar");
 
 		// nothing to begin with	
 		assertNull("0.0", manager.getValue("one"));
 
 		// add a value to the table
 		manager.setValue("one", pathOne);
 		IPath value = manager.getValue("one");
 		assertNotNull("1.0", value);
 		assertTrue("1.1", pathOne.equals(value));
 
 		// add another value
 		manager.setValue("two", pathTwo);
 		value = manager.getValue("two");
 		assertNotNull("2.0", value);
 		assertTrue("2.1", pathTwo.equals(value));
 
 		// edit the first value
 		manager.setValue("one", pathOneEdit);
 		value = manager.getValue("one");
 		assertNotNull("3.0", value);
 		assertTrue("3.1", pathOneEdit.equals(value));
 
 		// setting with value == null will remove
 		manager.setValue("one", null);
 		assertNull("4.0", manager.getValue("one"));
 		// setting with value == Path.EMPTY will remove
 		manager.setValue("two", Path.EMPTY);
 		assertNull("4.1", manager.getValue("two"));
 
 		// set values with bogus names 
 		try {
 			manager.setValue("ECLIPSE$HOME", Path.ROOT);
 			fail("5.0 Accepted invalid variable name in setValue()");
 		} catch (IllegalArgumentException iae) {
 			// success
 		}
 
 	}
 	/**
 	 * Test IPathVariableManager#isDefined
 	 */
 	public void testIsDefined() {
 		assertTrue("0.0", !manager.isDefined("one"));
 		manager.setValue("one", Path.ROOT);
 		assertTrue("0.1", manager.isDefined("one"));
 		manager.setValue("one", null);
 		assertTrue("0.2", !manager.isDefined("one"));
 	}
 	/**
 	 * Test IPathVariableManager#resolvePath
 	 */
 	public void testResolvePath() {
 		IPath pathOne = new Path("c:/temp/foo");
 		IPath pathTwo = new Path("/tmp/backup");
 
 		manager.setValue("one", pathOne);
 		manager.setValue("two", pathTwo);
 
 		// one substitution
 		IPath path = new Path("one/bar");
 		IPath expected = new Path("c:/temp/foo/bar");
 		IPath actual = manager.resolvePath(path);
 		assertEquals("1.0", expected, actual);
 
 		// another substitution
 		path = new Path("two/myworld");
 		expected = new Path("/tmp/backup/myworld");
 		actual = manager.resolvePath(path);
 		assertEquals("2.0", expected, actual);
 
 		// variable not defined
 		path = new Path("three/nothere");
 		expected = path;
 		actual = manager.resolvePath(path);
 		assertEquals("3.0", expected, actual);
 
 		// device
 		path = new Path("c:/one");
 		expected = path;
 		actual = manager.resolvePath(path);
 		assertEquals("4.0", expected, actual);
 
 		// device2
 		path = new Path("c:two");
 		expected = path;
 		actual = manager.resolvePath(path);
 		assertEquals("5.0", expected, actual);
 
 		// absolute
 		path = new Path("/one");
 		expected = path;
 		actual = manager.resolvePath(path);
 		assertEquals("6.0", expected, actual);
 
 		// null
 		path = null;
 		assertNull("7.0", manager.resolvePath(path));
 
 	}
 	/**
 	 * Test IPathVariableManager#testValidateName
 	 */
 	public void testValidateName() {
 
 		// valid names 	
 		assertTrue("0.0", manager.validateName("ECLIPSEHOME").isOK());
 		assertTrue("0.1", manager.validateName("ECLIPSE_HOME").isOK());
 		assertTrue("0.2", manager.validateName("ECLIPSE_HOME_1").isOK());
 		assertTrue("0.3", manager.validateName("_").isOK());
 
 		// invalid names
		assertTrue("1.0", !manager.validateName("1FOO").isOK());
		assertTrue("1.1", !manager.validateName("FOO%BAR").isOK());
		assertTrue("1.2", !manager.validateName("FOO$BAR").isOK());
		assertTrue("1.3", !manager.validateName(" FOO").isOK());
		assertTrue("1.4", !manager.validateName("FOO ").isOK());
 
 	}
 	/**
 	 * Test IPathVariableManager#addChangeListener and IPathVariableManager#removeChangeListener
 	 */
 	public void testListeners() {
 		PathVariableChangeVerifier listener = new PathVariableChangeVerifier();
 		manager.addChangeListener(listener);
 		IPath pathOne = new Path("/tmp/foobar");
 		IPath pathOneEdit = new Path("/tmp/foobar/myworld");
 
 		try {
 
 			// add a variable
 			manager.setValue("one", pathOne);
 			listener.addExpectedEvent(IPathVariableChangeEvent.VARIABLE_CREATED, "one", pathOne);
 			try {
 				listener.verify();
 			} catch (PathVariableChangeVerifier.VerificationFailedException e) {
 				fail("0.0", e);
 			}
 
 			// change a variable
 			listener.reset();
 			manager.setValue("one", pathOneEdit);
 			listener.addExpectedEvent(IPathVariableChangeEvent.VARIABLE_CHANGED, "one", pathOneEdit);
 			try {
 				listener.verify();
 			} catch (PathVariableChangeVerifier.VerificationFailedException e) {
 				fail("2.0", e);
 			}
 
 			// remove a variable
 			listener.reset();
 			manager.setValue("one", null);
 			listener.addExpectedEvent(IPathVariableChangeEvent.VARIABLE_DELETED, "one", null);
 			try {
 				listener.verify();
 			} catch (PathVariableChangeVerifier.VerificationFailedException e) {
 				fail("3.0", e);
 			}
 
 		} finally {
 			manager.removeChangeListener(listener);
 		}
 	}
 
 	boolean contains(Object[] array, Object obj) {
 		for (int i = 0; i < array.length; i++)
 			if (array[i].equals(obj))
 				return true;
 		return false;
 	}
 	/**
 	 * Ensure there are no path variables in the workspace.
 	 * 
 	 * @see junit.framework.TestCase#tearDown()
 	 */
 	protected void tearDown() throws Exception {
 		super.tearDown();
 		String[] names = manager.getPathVariableNames();
 		for (int i = 0; i < names.length; i++) {
 			manager.setValue(names[i], null);
 		}
 	}
 
 }
