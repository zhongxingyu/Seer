 /*******************************************************************************
  * Copyright (c) 2007, 2010 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.internal.ui.ridgets.swt;
 
 import java.beans.PropertyChangeEvent;
 import java.util.Iterator;
 
 import org.eclipse.core.databinding.beans.BeansObservables;
 import org.eclipse.core.databinding.observable.value.IObservableValue;
 import org.eclipse.core.databinding.validation.IValidator;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.RowData;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Text;
 
 import org.eclipse.riena.beans.common.DoubleBean;
 import org.eclipse.riena.beans.common.IntegerBean;
 import org.eclipse.riena.beans.common.StringBean;
 import org.eclipse.riena.core.marker.IMarker;
 import org.eclipse.riena.internal.ui.swt.test.TestUtils;
 import org.eclipse.riena.internal.ui.swt.test.UITestHelper;
 import org.eclipse.riena.ui.core.marker.ErrorMarker;
 import org.eclipse.riena.ui.core.marker.IMessageMarker;
 import org.eclipse.riena.ui.core.marker.NegativeMarker;
 import org.eclipse.riena.ui.core.marker.ValidationTime;
 import org.eclipse.riena.ui.ridgets.INumericTextRidget;
 import org.eclipse.riena.ui.ridgets.IRidget;
 import org.eclipse.riena.ui.ridgets.ITextRidget;
 import org.eclipse.riena.ui.ridgets.marker.ValidationMessageMarker;
 import org.eclipse.riena.ui.ridgets.swt.MarkerSupport;
 import org.eclipse.riena.ui.ridgets.swt.uibinding.SwtControlRidgetMapper;
 import org.eclipse.riena.ui.ridgets.validation.MaxLength;
 import org.eclipse.riena.ui.ridgets.validation.MaxNumberLength;
 import org.eclipse.riena.ui.ridgets.validation.MinLength;
 import org.eclipse.riena.ui.ridgets.validation.ValidationFailure;
 import org.eclipse.riena.ui.ridgets.validation.ValidationRuleStatus;
 import org.eclipse.riena.ui.swt.utils.UIControlsFactory;
 
 /**
  * Tests for the class {@link NumericTextRidget}.
  */
 public class NumericTextRidgetTest extends TextRidgetTest {
 
 	private static final Integer INTEGER_ONE = Integer.valueOf(471);
 	private static final Integer INTEGER_TWO = Integer.valueOf(23);
 
 	@Override
 	protected IRidget createRidget() {
 		return new NumericTextRidget();
 	}
 
 	@Override
 	protected INumericTextRidget getRidget() {
 		return (INumericTextRidget) super.getRidget();
 	}
 
 	@Override
 	protected Control createWidget(final Composite parent) {
 		final Control result = new Text(getShell(), SWT.RIGHT | SWT.BORDER | SWT.SINGLE);
 		result.setData(UIControlsFactory.KEY_TYPE, UIControlsFactory.TYPE_NUMERIC);
 		result.setLayoutData(new RowData(100, SWT.DEFAULT));
 		return result;
 	}
 
 	// test methods
 	///////////////
 
 	@Override
 	public void testRidgetMapping() {
 		final SwtControlRidgetMapper mapper = SwtControlRidgetMapper.getInstance();
 		assertSame(NumericTextRidget.class, mapper.getRidgetClass(getWidget()));
 	}
 
 	@Override
 	public void testCreate() throws Exception {
 		assertFalse(getRidget().isDirectWriting());
 		assertEquals("0", getRidget().getText());
 	}
 
 	@Override
 	public void testSetText() throws Exception {
 		final INumericTextRidget ridget = getRidget();
 		ridget.setGrouping(true);
 
 		ridget.setText("");
 
 		assertEquals("", ridget.getText());
 
 		ridget.setText("-1234");
 
 		assertEquals(localize("-1.234"), ridget.getText());
 
 		ridget.setText("1234");
 
 		assertEquals(localize("1.234"), ridget.getText());
 
 		ridget.setText(localize("98.765"));
 
 		assertEquals(localize("98.765"), ridget.getText());
 
 		try {
 			ridget.setText(localize("98.765,12"));
 			fail();
 		} catch (final NumberFormatException nfe) {
 			ok();
 		}
 
 		try {
 			ridget.setText("abcd");
 			fail();
 		} catch (final NumberFormatException nfe) {
 			ok();
 		}
 
 		try {
 			ridget.setText("a,bcd");
 			fail();
 		} catch (final NumberFormatException nfe) {
 			ok();
 		}
 	}
 
 	public void testSetTextNoGroup() throws Exception {
 		final INumericTextRidget ridget = getRidget();
 		ridget.setGrouping(false);
 
 		ridget.setText("");
 
 		assertEquals("", ridget.getText());
 
 		ridget.setText("-1234");
 
 		assertEquals("-1234", ridget.getText());
 
 		ridget.setText("1234");
 
 		assertEquals("1234", ridget.getText());
 
 		ridget.setText(localize("98.765"));
 
 		assertEquals("98765", ridget.getText());
 
 		try {
 			ridget.setText(localize("98.765,12"));
 			fail();
 		} catch (final NumberFormatException nfe) {
 			ok();
 		}
 
 		try {
 			ridget.setText("abcd");
 			fail();
 		} catch (final NumberFormatException nfe) {
 			ok();
 		}
 	}
 
 	/**
 	 * Test that setText(null) clears the number (equiv. to setText("0")).
 	 */
 	@Override
 	public void testSetTextNull() {
 		final ITextRidget ridget = getRidget();
 
 		ridget.setText("42");
 
 		assertEquals("42", ridget.getText());
 
 		ridget.setText(null);
 
 		assertEquals("", ridget.getText());
 	}
 
 	@Override
 	public void testGetText() throws Exception {
 		final ITextRidget ridget = getRidget();
 
 		assertEquals("0", ridget.getText());
 	}
 
 	@Override
 	public void testBindToModelPropertyName() {
 		final ITextRidget ridget = getRidget();
 		final IntegerBean model = new IntegerBean(1337);
 		ridget.bindToModel(model, IntegerBean.PROP_VALUE);
 
 		assertEquals("0", ridget.getText());
 
 		ridget.updateFromModel();
 
 		assertEquals(localize("1.337"), ridget.getText());
 	}
 
 	@Override
 	public void testUpdateFromModel() {
 		final ITextRidget ridget = getRidget();
 		final IntegerBean model = new IntegerBean(1337);
 		ridget.bindToModel(model, IntegerBean.PROP_VALUE);
 
 		model.setValue(-7);
 		ridget.updateFromModel();
 
 		assertEquals(localize("-7"), ridget.getText());
 	}
 
 	@Override
 	public void testBindToModelIObservableValue() throws Exception {
 		final ITextRidget ridget = getRidget();
 
 		final IntegerBean model = new IntegerBean(4711);
 		final IObservableValue modelOV = BeansObservables.observeValue(model, IntegerBean.PROP_VALUE);
 		ridget.bindToModel(modelOV);
 
 		assertEquals("0", ridget.getText());
 
 		ridget.updateFromModel();
 
 		assertEquals(localize("4.711"), ridget.getText());
 	}
 
 	@Override
 	public void testFocusGainedDoesSelectOnSingleText() {
 		final Text control = getWidget();
 
 		assertEquals("0", control.getSelectionText());
 		control.setSelection(0, 0);
 
 		final Event e = new Event();
 		e.type = SWT.FocusIn;
 		e.widget = control;
 		e.widget.notifyListeners(e.type, e);
 
 		assertEquals(0, control.getStyle() & SWT.MULTI);
 		assertEquals("0", control.getSelectionText());
 	}
 
 	@Override
 	public void testFocusGainedDoesNotSelectOnMultiLineText() {
 		// override test in superclass; multiline is not supported
 		assertTrue(true);
 	}
 
 	public void testCheckWidget() {
 		final ITextRidget ridget = getRidget();
 		final Text control = new Text(getShell(), SWT.MULTI);
 
 		try {
 			ridget.setUIControl(control);
 			fail();
 		} catch (final RuntimeException exc) {
 			ok();
 		}
 
 		try {
 			ridget.setUIControl(new Button(getShell(), SWT.PUSH));
 			fail();
 		} catch (final RuntimeException exc) {
 			ok();
 		}
 	}
 
 	public void testSetSignedTrue() {
 		final INumericTextRidget ridget = getRidget();
 		final Text control = getWidget();
 		final IntegerBean model = new IntegerBean(1337);
 		ridget.bindToModel(model, IntegerBean.PROP_VALUE);
 		ridget.updateFromModel();
 
 		assertTrue(ridget.isSigned());
 
 		expectNoPropertyChangeEvent();
 		ridget.setSigned(true);
 
 		verifyPropertyChangeEvents();
 		assertTrue(ridget.isSigned());
 
 		final int caretPos = control.getText().length() - 1;
 		focusIn(control);
 		control.setSelection(caretPos, caretPos);
 
 		assertEquals(localize("1.337"), control.getText());
 		assertEquals(caretPos, control.getCaretPosition());
 
 		UITestHelper.sendString(control.getDisplay(), "-");
 
 		assertEquals(localize("-1.337"), control.getText());
 		assertEquals(caretPos + 1, control.getCaretPosition());
 
 		control.setSelection(1, 1);
 		UITestHelper.sendString(control.getDisplay(), "\b");
 
 		assertEquals(localize("1.337"), control.getText());
 		assertEquals(0, control.getCaretPosition());
 	}
 
 	public void testSetSignedFalse() {
 		final INumericTextRidget ridget = getRidget();
 		final Text control = getWidget();
 		final IntegerBean model = new IntegerBean(1337);
 		ridget.bindToModel(model, IntegerBean.PROP_VALUE);
 		ridget.updateFromModel();
 
 		assertTrue(ridget.isSigned());
 
 		expectPropertyChangeEvent(INumericTextRidget.PROPERTY_SIGNED, Boolean.TRUE, Boolean.FALSE);
 		ridget.setSigned(false);
 
 		verifyPropertyChangeEvents();
 		assertFalse(ridget.isSigned());
 
 		final int caretPos = control.getText().length() - 1;
 		focusIn(control);
 		control.setSelection(caretPos, caretPos);
 
 		assertEquals(localize("1.337"), control.getText());
 		assertEquals(caretPos, control.getCaretPosition());
 
 		UITestHelper.sendString(control.getDisplay(), "-");
 
 		assertEquals(localize("1.337"), control.getText());
 		assertEquals(caretPos, control.getCaretPosition());
 	}
 
 	/**
 	 * As per bug #275134.
 	 */
 	public void testSetSignedThrowsException() {
 		final INumericTextRidget ridget = getRidget();
 		ridget.setText("1234");
 
 		ridget.setSigned(false);
 
 		try {
 			ridget.setText("-4711");
 			fail();
 		} catch (final RuntimeException exc) {
 			ok("expected");
 		}
 		assertEquals(localize("1.234"), ridget.getText());
 	}
 
 	public void testSetGrouping() {
 		final INumericTextRidget ridget = getRidget();
 		final IntegerBean model = new IntegerBean(1337);
 		ridget.bindToModel(model, IntegerBean.PROP_VALUE);
 		ridget.updateFromModel();
 
 		assertTrue(ridget.isGrouping());
 		assertEquals(localize("1.337"), ridget.getText());
 
 		ridget.setGrouping(false);
 
 		assertFalse(ridget.isGrouping());
 		assertEquals("1337", ridget.getText());
 
 		ridget.setGrouping(true);
 
 		assertTrue(ridget.isGrouping());
 		assertEquals(localize("1.337"), ridget.getText());
 	}
 
 	public void testUpdateFromControlUserInput() throws Exception {
 		final Text control = getWidget();
 		final ITextRidget ridget = getRidget();
 		final Display display = control.getDisplay();
 		final IntegerBean bean = new IntegerBean();
 		ridget.bindToModel(bean, IntegerBean.PROP_VALUE);
 
 		assertFalse(ridget.isDirectWriting());
 
 		UITestHelper.sendString(display, "47");
 
 		assertEquals("47", control.getText());
 		assertEquals("0", ridget.getText());
 		assertEquals(Integer.valueOf(0), bean.getValue());
 
 		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, ITextRidget.PROPERTY_TEXT, "0", "47"),
 				new PropertyChangeEvent(ridget, "textAfter", "0", "47"));
 
 		UITestHelper.sendString(display, "\r");
 		UITestHelper.readAndDispatch(control);
 
 		verifyPropertyChangeEvents();
 		assertEquals("47", control.getText());
 		assertEquals("47", ridget.getText());
 		assertEquals(Integer.valueOf(47), bean.getValue());
 
 		expectNoPropertyChangeEvent();
 
 		UITestHelper.sendString(display, "1");
 
 		verifyPropertyChangeEvents();
 		assertEquals("471", control.getText());
 		assertEquals("47", ridget.getText());
 		assertEquals(Integer.valueOf(47), bean.getValue());
 
 		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, ITextRidget.PROPERTY_TEXT, "47", "471"),
 				new PropertyChangeEvent(ridget, "textAfter", "47", "471"));
 
 		UITestHelper.sendString(display, "\t");
 
 		verifyPropertyChangeEvents();
 		assertEquals("471", control.getText());
 		assertEquals("471", ridget.getText());
 		assertEquals(Integer.valueOf(471), bean.getValue());
 	}
 
 	public void testUpdateFromControlUserInputDirectWriting() {
 		final Text control = getWidget();
 		final INumericTextRidget ridget = getRidget();
 
 		//		ridget.addPropertyChangeListener(ITextRidget.PROPERTY_TEXT, new PropertyChangeListener() {
 		//			public void propertyChange(PropertyChangeEvent evt) {
 		//				System.out.println(String.format("%s %s %s", evt.getPropertyName(), evt.getOldValue(), evt
 		//						.getNewValue()));
 		//			}
 		//		});
 
 		final IntegerBean bean = new IntegerBean();
 		ridget.bindToModel(bean, IntegerBean.PROP_VALUE);
 		ridget.setDirectWriting(true);
 
 		final Display display = control.getDisplay();
 		UITestHelper.sendString(display, "4");
 
 		assertEquals("4", control.getText());
 		assertEquals("4", ridget.getText());
 		assertEquals(Integer.valueOf(4), bean.getValue());
 
 		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, ITextRidget.PROPERTY_TEXT, "4", "47"),
 				new PropertyChangeEvent(ridget, "textAfter", "4", "47"));
 
 		UITestHelper.sendString(display, "7");
 
 		verifyPropertyChangeEvents();
 		assertEquals("47", control.getText());
 		assertEquals("47", ridget.getText());
 		assertEquals(Integer.valueOf(47), bean.getValue());
 
 		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, ITextRidget.PROPERTY_TEXT, "47", "471"),
 				new PropertyChangeEvent(ridget, "textAfter", "47", "471"));
 
 		UITestHelper.sendString(display, "1");
 
 		verifyPropertyChangeEvents();
 		assertEquals("471", control.getText());
 		assertEquals("471", ridget.getText());
 		assertEquals(Integer.valueOf(471), bean.getValue());
 
 		expectPropertyChangeEvents(
 				new PropertyChangeEvent(ridget, ITextRidget.PROPERTY_TEXT, "471", localize("4.711")),
 				new PropertyChangeEvent(ridget, "textAfter", "471", localize("4.711")));
 
 		UITestHelper.sendString(display, "1");
 
 		verifyPropertyChangeEvents();
 		assertEquals(localize("4.711"), control.getText());
 		assertEquals(localize("4.711"), ridget.getText());
 		assertEquals(Integer.valueOf(4711), bean.getValue());
 
 		expectPropertyChangeEvents(
 				new PropertyChangeEvent(ridget, ITextRidget.PROPERTY_TEXT, localize("4.711"), "471"),
 				new PropertyChangeEvent(ridget, "textAfter", localize("4.711"), "471"));
 
 		UITestHelper.sendKeyAction(display, SWT.ARROW_LEFT);
 		UITestHelper.sendString(display, "\b");
 
 		verifyPropertyChangeEvents();
 		assertEquals("471", control.getText());
 		assertEquals("471", ridget.getText());
 		assertEquals(Integer.valueOf("471"), bean.getValue());
 
 		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, ITextRidget.PROPERTY_TEXT, "471", "47"),
 				new PropertyChangeEvent(ridget, "textAfter", "471", "47"));
 
 		UITestHelper.sendString(display, String.valueOf(SWT.DEL));
 
 		verifyPropertyChangeEvents();
 		assertEquals("47", control.getText());
 		assertEquals("47", ridget.getText());
 		assertEquals(Integer.valueOf(47), bean.getValue());
 
 		expectNoPropertyChangeEvent();
 
 		bean.setValue(Integer.valueOf(4711));
 
 		verifyPropertyChangeEvents();
 		assertEquals("47", control.getText());
 		assertEquals("47", ridget.getText());
 		assertEquals(Integer.valueOf(4711), bean.getValue());
 
 		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, ITextRidget.PROPERTY_TEXT, "47", "4"),
 				new PropertyChangeEvent(ridget, "textAfter", "47", "4"));
 
 		UITestHelper.sendString(display, "\b");
 
 		verifyPropertyChangeEvents();
 		assertEquals("4", control.getText());
 		assertEquals("4", ridget.getText());
 		assertEquals(Integer.valueOf("4"), bean.getValue());
 	}
 
 	public void testUpdateFromRidgetOnRebind() throws Exception {
 		final Text control = getWidget();
 		final ITextRidget ridget = getRidget();
 
 		final IntegerBean bean = new IntegerBean();
 		ridget.bindToModel(bean, IntegerBean.PROP_VALUE);
 		bean.setValue(INTEGER_ONE);
 		ridget.updateFromModel();
 
 		assertEquals(INTEGER_ONE.toString(), control.getText());
 		assertEquals(INTEGER_ONE.toString(), ridget.getText());
 		assertEquals(INTEGER_ONE, bean.getValue());
 
 		// unbind, e.g. when the view is used by another controller
 		ridget.setUIControl(null);
 
 		control.selectAll();
 		UITestHelper.sendString(control.getDisplay(), "99");
 
 		assertEquals("99", control.getText());
 		assertEquals(INTEGER_ONE.toString(), ridget.getText());
 		assertEquals(INTEGER_ONE, bean.getValue());
 
 		// rebind
 		ridget.setUIControl(control);
 
 		assertEquals(INTEGER_ONE.toString(), control.getText());
 		assertEquals(INTEGER_ONE.toString(), ridget.getText());
 		assertEquals(INTEGER_ONE, bean.getValue());
 
 		// unbind again
 		ridget.setUIControl(null);
 
 		bean.setValue(INTEGER_TWO);
 		ridget.updateFromModel();
 
 		assertEquals(INTEGER_ONE.toString(), control.getText());
 		assertEquals(INTEGER_TWO.toString(), ridget.getText());
 		assertEquals(INTEGER_TWO, bean.getValue());
 
 		// rebind
 		ridget.setUIControl(control);
 
 		assertEquals(INTEGER_TWO.toString(), control.getText());
 		assertEquals(INTEGER_TWO.toString(), ridget.getText());
 		assertEquals(INTEGER_TWO, bean.getValue());
 	}
 
 	public void testValidationOnUpdateToModel() throws Exception {
 		final Text control = getWidget();
 		final ITextRidget ridget = getRidget();
 
 		final IntegerBean bean = new IntegerBean();
 		ridget.bindToModel(bean, IntegerBean.PROP_VALUE);
 
 		ridget.addValidationRule(new MinLength(3), ValidationTime.ON_UPDATE_TO_MODEL);
 
 		bean.setValue(INTEGER_ONE);
 		ridget.updateFromModel();
 
 		assertTrue(ridget.getMarkersOfType(ErrorMarker.class).isEmpty());
 		assertEquals(INTEGER_ONE.toString(), ridget.getText());
 
 		control.selectAll();
 		UITestHelper.sendString(control.getDisplay(), "99\t");
 
 		assertFalse(ridget.getMarkersOfType(ErrorMarker.class).isEmpty());
 		assertEquals("99", ridget.getText());
 
 		focusIn(control);
 		UITestHelper.sendKeyAction(control.getDisplay(), SWT.END);
 		UITestHelper.sendString(control.getDisplay(), "9");
 
 		assertFalse(ridget.getMarkersOfType(ErrorMarker.class).isEmpty());
 
 		UITestHelper.sendString(control.getDisplay(), "\r");
 
 		assertTrue(ridget.getMarkersOfType(ErrorMarker.class).isEmpty());
 		assertEquals("999", ridget.getText());
 	}
 
 	public void testCharactersAreBlockedInControl() throws Exception {
 		final Text control = getWidget();
 		final ITextRidget ridget = getRidget();
 
 		final IntegerBean bean = new IntegerBean();
 		ridget.bindToModel(bean, IntegerBean.PROP_VALUE);
 		ridget.setDirectWriting(true);
 
 		UITestHelper.sendString(control.getDisplay(), "12");
 
 		assertEquals("12", control.getText());
 		assertEquals("12", ridget.getText());
 		assertEquals(Integer.valueOf(12), bean.getValue());
 
 		UITestHelper.sendString(control.getDisplay(), "foo");
 
 		assertEquals("12", control.getText());
 		assertEquals("12", ridget.getText());
 		assertEquals(Integer.valueOf(12), bean.getValue());
 	}
 
 	public void testValidationOnUpdateFromModelWithOnEditRule() {
 		final ITextRidget ridget = getRidget();
 		final IntegerBean bean = new IntegerBean();
 		ridget.bindToModel(bean, IntegerBean.PROP_VALUE);
 
 		assertFalse(ridget.isErrorMarked());
 
 		ridget.addValidationRule(new MaxLength(5), ValidationTime.ON_UI_CONTROL_EDIT);
 		bean.setValue(Integer.valueOf(123456));
 		ridget.updateFromModel();
 
 		assertTrue(ridget.isErrorMarked());
 		assertEquals(localize("123.456"), ridget.getText());
 		assertEquals(localize("123.456"), getWidget().getText());
 		assertEquals(Integer.valueOf(123456), bean.getValue());
 
 		bean.setValue(Integer.valueOf(1234));
 		ridget.updateFromModel();
 
 		assertFalse(ridget.isErrorMarked());
 		assertEquals(localize("1.234"), ridget.getText());
 		assertEquals(localize("1.234"), getWidget().getText());
 		assertEquals(Integer.valueOf(1234), bean.getValue());
 	}
 
 	public void testValidationOnUpdateFromModelWithOnUpdateRule() {
 		final ITextRidget ridget = getRidget();
 		final IntegerBean bean = new IntegerBean(Integer.valueOf(123456));
 		ridget.bindToModel(bean, IntegerBean.PROP_VALUE);
 		ridget.updateFromModel();
 
 		assertFalse(ridget.isErrorMarked());
 
 		ridget.addValidationRule(new MinLength(5), ValidationTime.ON_UPDATE_TO_MODEL);
 		bean.setValue(Integer.valueOf(123));
 		ridget.updateFromModel();
 
 		assertTrue(ridget.isErrorMarked());
 		assertEquals("123", ridget.getText());
 		assertEquals("123", getWidget().getText());
 		assertEquals(Integer.valueOf(123), bean.getValue());
 
 		bean.setValue(Integer.valueOf(1234));
 		ridget.updateFromModel();
 
 		assertFalse(ridget.isErrorMarked());
 		assertEquals(localize("1.234"), ridget.getText());
 		assertEquals(localize("1.234"), getWidget().getText());
 		assertEquals(Integer.valueOf(1234), bean.getValue());
 	}
 
 	public void testUpdateFromRidgetWithValidationOnEditRule() {
 		final Text control = getWidget();
 		final ITextRidget ridget = getRidget();
 
 		final IntegerBean bean = new IntegerBean(Integer.valueOf(1234));
 		ridget.addValidationRule(new MinLength(5), ValidationTime.ON_UI_CONTROL_EDIT);
 		ridget.bindToModel(bean, IntegerBean.PROP_VALUE);
 
 		assertFalse(ridget.isErrorMarked());
 		assertFalse(ridget.isDirectWriting());
 
 		UITestHelper.sendString(control.getDisplay(), "98765\t");
 
 		assertFalse(ridget.isErrorMarked());
 		assertEquals(localize("98.765"), ridget.getText());
 		assertEquals(Integer.valueOf(98765), bean.getValue());
 
 		focusIn(control);
 		control.selectAll();
 		// \t triggers update
 		UITestHelper.sendString(control.getDisplay(), "12\t");
 
 		assertTrue(ridget.isErrorMarked());
 		// MinLength is non-blocking, so we expected '12' in ridget
 		assertEquals("12", ridget.getText());
 		assertEquals(Integer.valueOf(98765), bean.getValue());
 
 		focusIn(control);
 		control.selectAll();
 		UITestHelper.sendString(control.getDisplay(), "43210\t");
 
 		assertFalse(ridget.isErrorMarked());
 		assertEquals(localize("43.210"), ridget.getText());
 		assertEquals(Integer.valueOf(43210), bean.getValue());
 	}
 
 	public void testUpdateFromRidgetWithValidationOnUpdateRule() {
 		final Text control = getWidget();
 		final ITextRidget ridget = getRidget();
 
 		final IntegerBean bean = new IntegerBean();
 		ridget.addValidationRule(new EndsWithFive(), ValidationTime.ON_UPDATE_TO_MODEL);
 		ridget.bindToModel(bean, IntegerBean.PROP_VALUE);
 
 		assertFalse(ridget.isErrorMarked());
 		assertFalse(ridget.isDirectWriting());
 
 		UITestHelper.sendString(control.getDisplay(), "98765\t");
 
 		assertFalse(ridget.isErrorMarked());
 		assertEquals(localize("98.765"), ridget.getText());
 		assertEquals(Integer.valueOf(98765), bean.getValue());
 
 		focusIn(control);
 		control.selectAll();
 		// \t triggers update
 		UITestHelper.sendString(control.getDisplay(), "98\t");
 
 		assertTrue(ridget.isErrorMarked());
 		assertEquals("98", ridget.getText());
 		assertEquals(Integer.valueOf(98765), bean.getValue());
 
 		focusIn(control);
 		control.setSelection(2, 2);
 		UITestHelper.sendString(control.getDisplay(), "555\t");
 
 		assertFalse(ridget.isErrorMarked());
 		assertEquals(localize("98.555"), ridget.getText());
 		assertEquals(Integer.valueOf(98555), bean.getValue());
 	}
 
 	public void testValidationMessageWithOnEditRule() throws Exception {
 		final Text control = getWidget();
 		final ITextRidget ridget = getRidget();
 
 		ridget.addValidationRule(new EvenNumberOfCharacters(), ValidationTime.ON_UI_CONTROL_EDIT);
 		ridget.setDirectWriting(true);
 
 		ridget.addValidationMessage("ValidationErrorMessage");
 
 		assertEquals(0, ridget.getMarkers().size());
 
 		UITestHelper.sendString(control.getDisplay(), "1");
 
 		assertEquals(2, ridget.getMarkers().size());
 		final Iterator<? extends IMarker> iterator = ridget.getMarkers().iterator();
 		while (iterator.hasNext()) {
 			final IMarker next = iterator.next();
 			assertTrue(next instanceof IMessageMarker);
 			final IMessageMarker marker = (IMessageMarker) next;
 			assertTrue(marker.getMessage().equals("ValidationErrorMessage")
 					|| marker.getMessage().equals("Odd number of characters."));
 		}
 
 		UITestHelper.sendString(control.getDisplay(), "2");
 
 		assertEquals(0, ridget.getMarkers().size());
 	}
 
 	public void testValidationMessageWithOnUpdateRule() throws Exception {
 		final Text control = getWidget();
 		final ITextRidget ridget = getRidget();
 
 		ridget.bindToModel(new IntegerBean(12345), IntegerBean.PROP_VALUE);
 		ridget.addValidationRule(new EvenNumberOfCharacters(), ValidationTime.ON_UPDATE_TO_MODEL);
 		ridget.setDirectWriting(true);
 
 		ridget.addValidationMessage("ValidationErrorMessage");
 
 		assertEquals(0, ridget.getMarkers().size());
 
 		// \r triggers update
 		UITestHelper.sendString(control.getDisplay(), "1\r");
 
 		assertEquals(2, ridget.getMarkers().size());
 		TestUtils.assertMessage(ridget, ValidationMessageMarker.class, "ValidationErrorMessage");
 
 		// \r triggers update
 		UITestHelper.sendString(control.getDisplay(), "2\r");
 
 		assertEquals(0, ridget.getMarkers().size());
 	}
 
 	public void testRevalidateOnEditRule() {
 		final ITextRidget ridget = getRidget();
 
 		ridget.bindToModel(new IntegerBean(123), IntegerBean.PROP_VALUE);
 		ridget.updateFromModel();
 
 		assertFalse(ridget.isErrorMarked());
 
 		final IValidator rule = new EvenNumberOfCharacters();
 		ridget.addValidationRule(rule, ValidationTime.ON_UI_CONTROL_EDIT);
 
 		assertFalse(ridget.isErrorMarked());
 
 		final boolean isOk1 = ridget.revalidate();
 
 		assertFalse(isOk1);
 		assertTrue(ridget.isErrorMarked());
 
 		ridget.removeValidationRule(rule);
 
 		assertFalse(ridget.isErrorMarked()); // since 1.2: remove updates immediately
 
 		final boolean isOk2 = ridget.revalidate();
 
 		assertTrue(isOk2);
 		assertFalse(ridget.isErrorMarked());
 	}
 
 	public void testRevalidateOnUpdateRule() {
 		final ITextRidget ridget = getRidget();
 
 		ridget.bindToModel(new IntegerBean(123), IntegerBean.PROP_VALUE);
 		ridget.updateFromModel();
 
 		assertFalse(ridget.isErrorMarked());
 
 		final IValidator rule = new EvenNumberOfCharacters();
 		ridget.addValidationRule(rule, ValidationTime.ON_UPDATE_TO_MODEL);
 
 		assertFalse(ridget.isErrorMarked());
 
 		final boolean isOk1 = ridget.revalidate();
 
 		assertFalse(isOk1);
 		assertTrue(ridget.isErrorMarked());
 
 		ridget.removeValidationRule(rule);
 
 		assertFalse(ridget.isErrorMarked()); // since 1.2: remove updates immediately
 
 		final boolean isOk2 = ridget.revalidate();
 
 		assertTrue(isOk2);
 		assertFalse(ridget.isErrorMarked());
 	}
 
 	public void testRevalidateDoesUpdate() {
 		final ITextRidget ridget = getRidget();
 		final Text control = getWidget();
 		final EvenNumberOfCharacters evenChars = new EvenNumberOfCharacters();
 		ridget.addValidationRule(evenChars, ValidationTime.ON_UI_CONTROL_EDIT);
 
 		final IntegerBean bean = new IntegerBean(12);
 		ridget.bindToModel(bean, IntegerBean.PROP_VALUE);
 		ridget.updateFromModel();
 
 		assertFalse(ridget.isErrorMarked());
 
 		focusIn(control);
 		control.selectAll();
 		UITestHelper.sendString(control.getDisplay(), "345\t");
 		assertEquals("345", control.getText());
 		// non-blocking rule, expect 'abc'
 		assertEquals("345", ridget.getText());
 		assertEquals(Integer.valueOf(12), bean.getValue());
 
 		assertTrue(ridget.isErrorMarked());
 
 		ridget.removeValidationRule(evenChars);
 		ridget.revalidate();
 
 		assertFalse(ridget.isErrorMarked());
 		assertEquals("345", control.getText());
 		assertEquals("345", ridget.getText());
 		assertEquals(Integer.valueOf(345), bean.getValue());
 	}
 
 	public void testReValidationOnUpdateFromModel() {
 		final ITextRidget ridget = getRidget();
 
 		final IntegerBean bean = new IntegerBean(12);
 		ridget.bindToModel(bean, IntegerBean.PROP_VALUE);
 		ridget.updateFromModel();
 
 		assertFalse(ridget.isErrorMarked());
 		assertEquals("12", ridget.getText());
 
 		final IValidator rule = new EvenNumberOfCharacters();
 		ridget.addValidationRule(rule, ValidationTime.ON_UI_CONTROL_EDIT);
 		bean.setValue(Integer.valueOf(321));
 		ridget.updateFromModel();
 
 		assertTrue(ridget.isErrorMarked());
 		assertEquals(Integer.valueOf(321), bean.getValue());
 		assertEquals("321", ridget.getText());
 
 		ridget.removeValidationRule(rule);
 		ridget.updateFromModel();
 
 		assertFalse(ridget.isErrorMarked());
 		assertEquals(Integer.valueOf(321), bean.getValue());
 		assertEquals("321", ridget.getText());
 	}
 
 	public void testControlNotEditableWithOutputMarker() {
 		final ITextRidget ridget = getRidget();
 		final Text control = getWidget();
 
 		assertTrue(control.getEditable());
 
 		ridget.setOutputOnly(true);
 
 		assertFalse(control.getEditable());
 
 		ridget.setOutputOnly(false);
 
 		assertTrue(control.getEditable());
 	}
 
 	public void testOutputMultipleSelectionCannotBeChangedFromUI() {
 		final ITextRidget ridget = getRidget();
 		final Text control = getWidget();
 
 		assertEquals("0", control.getText());
 		assertEquals("0", ridget.getText());
 
 		ridget.setOutputOnly(true);
 		control.selectAll();
 		focusIn(control);
 		UITestHelper.sendString(control.getDisplay(), "123\t");
 
 		assertEquals("0", control.getText());
 		assertEquals("0", ridget.getText());
 
 		ridget.setOutputOnly(false);
 		control.selectAll();
 		focusIn(control);
 		UITestHelper.sendString(control.getDisplay(), "123\t");
 
 		assertEquals("123", control.getText());
 		assertEquals("123", ridget.getText());
 	}
 
 	public void testDisabledHasNoTextFromModel() {
 		if (!MarkerSupport.isHideDisabledRidgetContent()) {
 			System.out.println("Skipping TextRidgetTest2.testDisabledHasNoTextFromModel()");
 			return;
 		}
 
 		final ITextRidget ridget = getRidget();
 		final Text control = getWidget();
 		final IntegerBean bean = new IntegerBean(INTEGER_TWO);
 		ridget.bindToModel(bean, IntegerBean.PROP_VALUE);
 		ridget.updateFromModel();
 
 		assertTrue(ridget.isEnabled());
 		assertEquals(INTEGER_TWO.toString(), control.getText());
 		assertEquals(INTEGER_TWO.toString(), ridget.getText());
 		assertEquals(INTEGER_TWO, bean.getValue());
 
 		ridget.setEnabled(false);
 
 		assertEquals("", control.getText());
 		assertEquals(INTEGER_TWO.toString(), ridget.getText());
 		assertEquals(INTEGER_TWO, bean.getValue());
 
 		bean.setValue(INTEGER_ONE);
 		ridget.updateFromModel();
 
 		assertEquals("", control.getText());
 		assertEquals(INTEGER_ONE.toString(), ridget.getText());
 		assertEquals(INTEGER_ONE, bean.getValue());
 
 		ridget.setEnabled(true);
 
 		assertEquals(INTEGER_ONE.toString(), control.getText());
 		assertEquals(INTEGER_ONE.toString(), ridget.getText());
 		assertEquals(INTEGER_ONE, bean.getValue());
 	}
 
 	public void testMaxLengthWithRule() throws Exception {
 		final ITextRidget ridget = getRidget();
 		final Text control = getWidget();
 
 		ridget.addValidationRule(new MaxNumberLength(5), ValidationTime.ON_UI_CONTROL_EDIT);
 
 		focusIn(control);
 		UITestHelper.sendString(control.getDisplay(), "1234");
 
 		assertEquals(localize("1.234"), control.getText());
 
 		focusOut(control);
 
 		assertEquals(localize("1.234"), ridget.getText());
 
 		focusIn(control);
 		control.setSelection(control.getText().length()); // move cursor to end
 		UITestHelper.sendString(control.getDisplay(), "5");
 
 		assertEquals(localize("12.345"), control.getText());
 
 		focusOut(control);
 
 		assertEquals(localize("12.345"), control.getText());
 
 		focusIn(control);
 		control.setSelection(control.getText().length()); // move cursor to end
 		UITestHelper.sendString(control.getDisplay(), "6");
 
 		assertEquals(localize("12.345"), control.getText());
 
 		focusOut(control);
 
 		assertEquals(localize("12.345"), control.getText());
 	}
 
 	public void testSetMarkNegative() {
 		final INumericTextRidget ridget = getRidget();
 
 		assertTrue(ridget.isMarkNegative());
 
 		ridget.setMarkNegative(false);
 
 		assertFalse(ridget.isMarkNegative());
 
 		ridget.setMarkNegative(true);
 
 		assertTrue(ridget.isMarkNegative());
 	}
 
 	public void testNegativeMarkerFromSetText() {
 		final INumericTextRidget ridget = getRidget();
 		ridget.setMarkNegative(true);
 
 		ridget.setText("100");
 
 		assertEquals(0, ridget.getMarkersOfType(NegativeMarker.class).size());
 
 		ridget.setText("-100");
 
 		assertEquals(1, ridget.getMarkersOfType(NegativeMarker.class).size());
 
 		ridget.setText("0");
 
 		assertEquals(0, ridget.getMarkersOfType(NegativeMarker.class).size());
 
 		ridget.setText("-0");
 
 		assertEquals(0, ridget.getMarkersOfType(NegativeMarker.class).size());
 
 		ridget.setText("-1");
 		ridget.setMarkNegative(false);
 
 		assertEquals(0, ridget.getMarkersOfType(NegativeMarker.class).size());
 
 		ridget.setMarkNegative(true);
 
 		assertEquals(1, ridget.getMarkersOfType(NegativeMarker.class).size());
 	}
 
 	public void testNegativeMarkerFromControl() {
 		final INumericTextRidget ridget = getRidget();
 		final Text control = getWidget();
 		final Display display = control.getDisplay();
 		ridget.setMarkNegative(true);
 
 		assertEquals(0, ridget.getMarkersOfType(NegativeMarker.class).size());
 
 		// direct writing is false, so we update the model pressing '\r' 
 
 		control.setFocus();
 		UITestHelper.sendString(display, "123-\r");
 
 		assertEquals(1, ridget.getMarkersOfType(NegativeMarker.class).size());
 
 		control.setSelection(0, 0);
 		UITestHelper.sendKeyAction(display, UITestHelper.KC_DEL);
 		UITestHelper.sendString(display, "\r");
 
 		assertEquals(0, ridget.getMarkersOfType(NegativeMarker.class).size());
 
 		UITestHelper.sendString(display, "-\r");
 
 		assertEquals(1, ridget.getMarkersOfType(NegativeMarker.class).size());
 	}
 
 	public void testRemoveLeadingCruft() {
 		assertEquals("0", NumericTextRidget.removeLeadingCruft("-"));
 		assertEquals("0", NumericTextRidget.removeLeadingCruft("-0"));
 		assertEquals("0", NumericTextRidget.removeLeadingCruft("0"));
 		assertEquals("-1", NumericTextRidget.removeLeadingCruft("-01"));
 		assertEquals("-10", NumericTextRidget.removeLeadingCruft("-010"));
 		assertEquals("-101", NumericTextRidget.removeLeadingCruft("-0101"));
 		assertEquals("-23", NumericTextRidget.removeLeadingCruft("-0023"));
 		assertEquals("1", NumericTextRidget.removeLeadingCruft("01"));
 		assertEquals("10", NumericTextRidget.removeLeadingCruft("010"));
 		assertEquals("101", NumericTextRidget.removeLeadingCruft("0101"));
 		assertEquals("23", NumericTextRidget.removeLeadingCruft("0023"));
 	}
 
 	public void testDelete() {
 		final INumericTextRidget ridget = getRidget();
 		ridget.setGrouping(true);
 		ridget.setSigned(true);
 
 		assertText("1^.234", UITestHelper.KC_DEL, "1^34");
 		assertText("^1.234", UITestHelper.KC_DEL, "^234");
 		assertText("12^.345", UITestHelper.KC_DEL, "1.2^45");
 		assertText("1.234^.567", UITestHelper.KC_DEL, "123.4^67");
 		assertText("1.234.5^67", UITestHelper.KC_DEL, "123.45^7");
 
 		assertText("-1^.234", UITestHelper.KC_DEL, "-1^34");
 		assertText("-^1.234", UITestHelper.KC_DEL, "-^234");
 		assertText("-1.234.5^67", UITestHelper.KC_DEL, "-123.45^7");
 	}
 
 	public void testBackspace() {
 		final INumericTextRidget ridget = getRidget();
 		ridget.setGrouping(true);
 		ridget.setSigned(true);
 
 		assertText("123.^456", "\b", "12^.456");
 		assertText("1.^456", "\b", "^456");
 		assertText("1.234.^567", "\b", "123^.567");
 		assertText("1.23^4", "\b", "12^4");
 		assertText("1.234.56^7", "\b", "123.45^7");
 
 		assertText("-1.23^4", "\b", "-12^4");
 		assertText("-1^.234", "\b", "-^234");
 		assertText("-1.234.56^7", "\b", "-123.45^7");
 	}
 
 	public void testMandatoryMarker() {
 		final INumericTextRidget ridget = getRidget();
 		ridget.setMandatory(true);
 
 		ridget.setText("123");
 
 		TestUtils.assertMandatoryMarker(ridget, 1, true);
 
 		ridget.setText(null);
 
 		TestUtils.assertMandatoryMarker(ridget, 1, false);
 
 		ridget.setMandatory(false);
 
 		TestUtils.assertMandatoryMarker(ridget, 0, false);
 	}
 
 	public void testGetSetMaxLength() {
 		final INumericTextRidget ridget = getRidget();
 
 		try {
 			ridget.setMaxLength(0);
 			fail();
 		} catch (final RuntimeException rex) {
 			ok();
 		}
 
		try {
			ridget.setMaxLength(-1);
			fail();
		} catch (final RuntimeException rex) {
			ok();
		}

 		final Integer oldValue = Integer.valueOf(ridget.getMaxLength());
 		expectPropertyChangeEvent(INumericTextRidget.PROPERTY_MAXLENGTH, oldValue, Integer.valueOf(5));
 		ridget.setMaxLength(5);
 
 		verifyPropertyChangeEvents();
 		assertEquals(5, ridget.getMaxLength());
 
 		expectNoPropertyChangeEvent();
 		ridget.setMaxLength(5);
 
 		verifyPropertyChangeEvents();
 	}
 
 	public void testMaxLength() {
 		final INumericTextRidget ridget = getRidget();
 		final Text control = getWidget();
 		ridget.setMaxLength(6);
 		final StringBean bean = new StringBean();
 		ridget.bindToModel(bean, StringBean.PROP_VALUE);
 
 		control.setFocus();
 		UITestHelper.sendString(control.getDisplay(), localize("12345678-\r"));
 
 		assertEquals(localize("-123.456"), control.getText());
 		assertEquals(localize("-123.456"), ridget.getText());
 		assertEquals(localize("-123.456"), bean.getValue());
 	}
 
 	public void testExceedMaxLengthWithSetText() {
 		final INumericTextRidget ridget = getRidget();
 		final Text control = getWidget();
 
 		ridget.setMaxLength(3);
 
 		assertEquals(3, ridget.getMaxLength());
 
 		ridget.setText(localize("-123"));
 
 		try {
 			ridget.setText(localize("1234"));
 			fail();
 		} catch (final RuntimeException rex) {
 			// expected
 			assertEquals(localize("-123"), ridget.getText());
 			assertEquals(localize("-123"), control.getText());
 		}
 	}
 
 	public void testExceedMaxLengthWithUpdate() {
 		final INumericTextRidget ridget = getRidget();
 		final Text control = getWidget();
 
 		ridget.setMaxLength(3);
 
 		assertEquals(3, ridget.getMaxLength());
 
 		final DoubleBean value = new DoubleBean(123.00d);
 		ridget.bindToModel(value, DoubleBean.PROP_VALUE);
 		ridget.updateFromModel();
 
 		try {
 			value.setValue(1234.12d);
 			ridget.updateFromModel();
 			fail();
 		} catch (final RuntimeException rex) {
 			// expected
 			assertEquals(localize("123"), ridget.getText());
 			assertEquals(localize("123"), control.getText());
 		}
 
 		value.setValue(-321d);
 		ridget.updateFromModel();
 		try {
 			value.setValue(1234d);
 			ridget.updateFromModel();
 			fail();
 		} catch (final RuntimeException rex) {
 			// expected
 			assertEquals(localize("-321"), ridget.getText());
 			assertEquals(localize("-321"), control.getText());
 		}
 	}
 
 	// helping methods
 	//////////////////
 
 	private void assertText(final String before, final String keySeq, final String after) {
 		TestUtils.assertText(getWidget(), localize(before), keySeq, localize(after));
 	}
 
 	private void assertText(final String before, final int keyCode, final String after) {
 		TestUtils.assertText(getWidget(), localize(before), keyCode, localize(after));
 	}
 
 	private void focusIn(final Text control) {
 		control.setFocus();
 		assertTrue(control.isFocusControl());
 	}
 
 	private void focusOut(final Text control) {
 		// clear focus
 		UITestHelper.sendString(control.getDisplay(), "\t");
 		assertFalse(control.isFocusControl());
 	}
 
 	private String localize(final String number) {
 		return TestUtils.getLocalizedNumber(number);
 	}
 
 	// helping classes
 	//////////////////
 
 	private static final class EvenNumberOfCharacters implements IValidator {
 
 		public IStatus validate(final Object value) {
 			if (value == null) {
 				return ValidationRuleStatus.ok();
 			}
 			if (value instanceof String) {
 				final String string = (String) value;
 				if (string.length() % 2 == 0) {
 					return ValidationRuleStatus.ok();
 				}
 				return ValidationRuleStatus.error(false, "Odd number of characters.");
 			}
 			throw new ValidationFailure(getClass().getName() + " can only validate objects of type "
 					+ String.class.getName());
 		}
 
 	}
 
 	private static final class EndsWithFive implements IValidator {
 
 		public IStatus validate(final Object value) {
 			boolean isOk = false;
 			String s = null;
 			if (value instanceof Number) {
 				s = ((Number) value).toString();
 			} else if (value instanceof String) {
 				s = (String) value;
 			}
 			if (s != null) {
 				final char lastChar = s.charAt(s.length() - 1);
 				isOk = lastChar == '5';
 			}
 			return isOk ? Status.OK_STATUS : Status.CANCEL_STATUS;
 		}
 	}
 }
