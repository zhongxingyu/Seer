 /*******************************************************************************
  * Copyright (C) 2011 by Harry Blauberg
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package org.jaml.tests.junit;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 
 import org.jaml.api.IDelegate;
 import org.jaml.util.Delegate;
 import org.junit.Test;
 
 /**
  * Unit test case for delegates
  */
 public class DelegateTest {
 
 	/**
 	 * Test method for
 	 * {@link org.jaml.util.Delegate#create(java.lang.Object, java.lang.String, java.lang.Class)}
 	 * .
 	 */
 	@Test
 	public void testCreate() {
 		JButton btn = new JButton("JUNIT");
 		IDelegate<JButton, String> delegate = Delegate.create(btn, "getText",
 				String.class);
 		assertEquals(btn.getText(), delegate.invoke());
 	}
 
 	/**
 	 * Test method for
 	 * {@link org.jaml.util.Delegate#Delegate(java.lang.Object, java.lang.String, java.lang.Class)}
 	 * .
 	 */
 	@Test
 	public void testDelegate() {
 		JButton btn = new JButton("JUNIT");
 		IDelegate<JButton, String> delegate = new Delegate<JButton, String>(
 				btn, "getText", String.class);
 		assertEquals(btn.getText(), delegate.invoke());
 	}
 
 	/**
 	 * Test method for {@link org.jaml.util.Delegate#isValid()}.
 	 */
 	@Test
 	public void testIsValid() {
 		JFrame frame = new JFrame("Test");
 		// Have to be okay
 		assertTrue(Delegate.create(frame, "getTitle", String.class).isValid());
 		assertTrue(Delegate.create(frame, "setTitle", String.class).isValid());
 		// Have to fail
 		assertFalse(Delegate.create(null, "getTitle", String.class).isValid());
 		assertFalse(Delegate.create(frame, "failTest", String.class).isValid());
 		assertFalse(Delegate.create(frame, "getTitle", int.class).isValid());
 	}
 
 	/**
 	 * Test method for {@link org.jaml.util.Delegate#invoke(java.lang.Object)}.
 	 */
 	@Test
 	public void testInvokeP() {
 		String text = "Test";
 		JFrame frame = new JFrame();
 		IDelegate<JFrame, String> delegate = Delegate.create(frame, "setTitle",
 				String.class);
 		delegate.invoke(text);
 		assertEquals(frame.getTitle(), text);
 	}
 
 	/**
 	 * Test method for {@link org.jaml.util.Delegate#invoke()}.
 	 */
 	@Test
 	public void testInvoke() {
 		JFrame frame = new JFrame("Test");
 		IDelegate<JFrame, String> delegate = Delegate.create(frame, "getTitle",
 				String.class);
 		assertEquals(frame.getTitle(), delegate.invoke());
 	}
 
 	/**
 	 * Test method for {@link org.jaml.util.Delegate#needsParameter()}.
 	 */
 	@Test
 	public void testNeedsParameter() {
 		JFrame frame = new JFrame();
 		assertFalse(Delegate.create(frame, "getTitle", String.class)
 				.needsParameter());
 		IDelegate<JFrame, String> delegate = Delegate.create(frame, "setTitle",
 				String.class);
 		assertTrue(delegate.isValid());
 		assertTrue(delegate.needsParameter());
 	}
 }
