 /*******************************************************************************
  * Copyright (c) 2007, 2009 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.ui.swt;
 
 import java.util.Arrays;
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.DateTime;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.Text;
 
 import org.eclipse.riena.internal.core.test.collect.UITestCase;
 import org.eclipse.riena.ui.common.IComplexComponent;
 import org.eclipse.riena.ui.swt.utils.SwtUtilities;
 import org.eclipse.riena.ui.swt.utils.UIControlsFactory;
 
 /**
  * Tests of the class {@link MasterDetailsComposite}
  */
 @UITestCase
 public class MasterDetailsCompositeTest extends TestCase {
 
 	private Shell shell;
 	private MasterDetailsComposite masterDetails;
 
 	@Override
 	protected void setUp() throws Exception {
 		shell = new Shell();
 		masterDetails = new MasterDetailsComposite(shell, SWT.NONE);
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		SwtUtilities.disposeWidget(shell);
 	}
 
 	public void testGetUIControlsWithBindingProperty() {
 		Composite parent = masterDetails.getDetails();
 		Text txtFirstName = UIControlsFactory.createText(parent, SWT.BORDER, "txtFirstName");
 		Text txtLastName = UIControlsFactory.createText(parent, SWT.BORDER, "txtLastName");
 		Text txtWithoutID = UIControlsFactory.createText(parent, SWT.BORDER);
 
 		List<Object> controls = masterDetails.getUIControls();
 
		assertEquals(6, controls.size());
 		assertTrue(controls.contains(txtFirstName));
 		assertTrue(controls.contains(txtLastName));
 		assertFalse(controls.contains(txtWithoutID));

		List<Object> controls2 = masterDetails.getUIControls();
		assertEquals(6, controls2.size());
 	}
 
 	public void testGetUIControlsWithBindingPropertyFromComposite() {
 		Composite parent = new Composite(masterDetails.getDetails(), SWT.NONE);
 		Text txtFirstName = UIControlsFactory.createText(parent, SWT.BORDER, "txtFirstName");
 		Text txtLastName = UIControlsFactory.createText(parent, SWT.BORDER, "txtLastName");
 		Text txtWithoutID = UIControlsFactory.createText(parent, SWT.BORDER);
 
 		List<Object> controls = masterDetails.getUIControls();
 
 		assertTrue(controls.contains(txtFirstName));
 		assertTrue(controls.contains(txtLastName));
 		assertFalse(controls.contains(txtWithoutID));
 	}
 
 	/**
 	 * As per bug 297524.
 	 */
 	public void testGetUIControlsThatAreComposites() {
 		Composite parent = masterDetails.getDetails();
 		Combo combo = UIControlsFactory.createCombo(parent, "combo");
 		CCombo ccombo = UIControlsFactory.createCCombo(parent, "ccombo");
 		DateTime datetime = UIControlsFactory.createDate(parent, SWT.NONE, "datetime");
 
 		List<Object> controls = masterDetails.getUIControls();
 
 		assertTrue(controls.contains(combo));
 		assertTrue(controls.contains(ccombo));
 		assertTrue(controls.contains(datetime));
 
 		Composite composite = new Composite(parent, SWT.NONE);
 		Combo combo2 = UIControlsFactory.createCombo(composite, "combo2");
 
 		controls = masterDetails.getUIControls();
 
 		assertFalse(controls.contains(composite));
 		assertTrue(controls.contains(combo2));
 	}
 
 	public void testSkipIComplexComponent() {
 		MasterDetailsComposite widget = new MasterDetailsComposite(shell, SWT.NONE) {
 			protected void createDetails(Composite details) {
 				IComplexComponent complex = new TestComplexComponent(details, SWT.NONE);
 				addUIControl(complex, "complex");
 			}
 		};
 		List<Object> controls = widget.getUIControls();
 
 		System.out.println(Arrays.toString(controls.toArray()));
 
 		// should contain the complex control, but not it's children 
 		// even if they have an id
 		assertEquals(5, controls.size());
 		assertTrue(controls.get(0) instanceof Table);
 		assertTrue(controls.get(1) instanceof Button);
 		assertTrue(controls.get(2) instanceof Button);
 		assertTrue(controls.get(3) instanceof Button);
 		assertTrue(controls.get(4) instanceof TestComplexComponent);
 	}
 
 	// helping classes
 	//////////////////
 
 	private static class TestComplexComponent extends Composite implements IComplexComponent {
 		private final Label label;
 
 		public TestComplexComponent(Composite parent, int style) {
 			super(parent, style);
 			label = UIControlsFactory.createLabel(this, "txt", "label");
 		}
 
 		public List<Object> getUIControls() {
 			return Arrays.asList(new Object[] { label });
 		}
 	}
 }
