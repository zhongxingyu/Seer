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
 import java.beans.PropertyChangeListener;
 import java.math.BigInteger;
 import java.text.DecimalFormatSymbols;
 import java.util.Arrays;
 import java.util.regex.Pattern;
 
 import org.eclipse.core.databinding.BindingException;
 import org.eclipse.core.databinding.conversion.IConverter;
 import org.eclipse.core.databinding.observable.value.IObservableValue;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.FocusAdapter;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.VerifyEvent;
 import org.eclipse.swt.events.VerifyListener;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Text;
 
 import org.eclipse.riena.core.util.RAPDetector;
 import org.eclipse.riena.core.util.StringUtils;
 import org.eclipse.riena.ui.core.marker.NegativeMarker;
 import org.eclipse.riena.ui.ridgets.INumericTextRidget;
 import org.eclipse.riena.ui.ridgets.ITextRidget;
 import org.eclipse.riena.ui.ridgets.swt.AbstractSWTRidget;
 import org.eclipse.riena.ui.ridgets.swt.ToStringConverterFactory;
 import org.eclipse.riena.ui.swt.utils.UIControlsFactory;
 
 /**
  * Ridget for a 'numeric' SWT <code>Text</code> widget.
  * 
  * @see UIControlsFactory#createTextNumeric(org.eclipse.swt.widgets.Composite)
  */
 public class NumericTextRidget extends TextRidget implements INumericTextRidget {
 
 	/**
 	 * This is not API and should not be called by clients. Public for testing
 	 * only.
 	 */
 	public static String group(final String input, final boolean isGrouping, final boolean isDecimal) {
 		String result = input;
 		final int decIndex = input.indexOf(DECIMAL_SEPARATOR);
 		if (isGrouping) {
 			final String left = decIndex == -1 ? input : input.substring(0, decIndex);
 			final String right = decIndex == -1 ? "" : input.substring(decIndex); //$NON-NLS-1$
 			result = NumericTextRidget.group(left) + right;
 		}
 		if (decIndex == -1 && isDecimal) {
 			result += DECIMAL_SEPARATOR;
 		}
 		return result;
 	}
 
 	/**
 	 * This is not API and should not be called by clients. Public for testing
 	 * only.
 	 */
 	public static String ungroup(final String input) {
 		final StringBuilder result = new StringBuilder(input.length());
 		for (int i = 0; i < input.length(); i++) {
 			final char ch = input.charAt(i);
 			if (ch != GROUPING_SEPARATOR) {
 				result.append(ch);
 			}
 		}
 		// System.out.println("ungroup: " + input + " >> " + result.toString());
 		return result.toString();
 	}
 
 	/**
 	 * This is not API and should not be called by clients. Public for testing
 	 * only.
 	 */
 	public static String removeLeadingCruft(final String input) {
 		if (Pattern.matches("0+.", input) && input.endsWith(String.valueOf(DECIMAL_SEPARATOR))) { //$NON-NLS-1$
 			return ZERO_DEC;
 		}
 		if (MINUS_DEC.matches(input)) {
 			return String.valueOf(DECIMAL_SEPARATOR);
 		}
 		if (Pattern.matches(MINUS_SIGN + "0+.", input) && input.endsWith(String.valueOf(DECIMAL_SEPARATOR))) { //$NON-NLS-1$
 			return ZERO_DEC;
 		}
 		if (String.valueOf(MINUS_SIGN).equals(input) || MINUS_ZERO.equals(input) || String.valueOf(ZERO).equals(input)) {
 			return String.valueOf(ZERO);
 		}
 		final StringBuilder result = new StringBuilder(input.length());
 		int start = 0;
 		if (input.indexOf(MINUS_SIGN) == 0) {
 			result.append(MINUS_SIGN);
 			start++;
 		}
 		if (start < input.length() && ZERO == input.charAt(start)) {
 			int newStart = start + 1;
 			while (newStart < input.length() && ZERO == input.charAt(newStart)) {
 				newStart++;
 			}
 			if (newStart == input.length()) {
 				result.append(ZERO);
 				if (MINUS_ZERO.equals(result.toString())) {
 					result.delete(0, 1);
 				}
 			} else {
 				result.append(input.substring(newStart));
 			}
 		} else {
 			result.append(input.substring(start));
 		}
 		return result.toString();
 	}
 
 	/**
 	 * This is not API.
 	 */
 	protected static String removeTrailingPadding(final String text) {
 		String result = text;
 		final int decSep = text.indexOf(DECIMAL_SEPARATOR);
 		if (decSep != -1) {
 			int index = text.length() - 1;
 			char ch = result.charAt(index);
 			while (index >= decSep && (ch == DECIMAL_SEPARATOR || ch == '0')) {
 				result = result.substring(0, index);
 				index--;
 				if (index >= decSep) {
 					ch = result.charAt(index);
 				}
 			}
 		}
 		return result;
 	}
 
 	private static String group(final String input) {
 		final int numLength = input.length();
 		final boolean isNegative = input.indexOf(MINUS_SIGN) == 0;
 		final int groupSize = 3;
 		final int delta = isNegative ? -2 : -1;
 		final int groupCount = (numLength + delta) / groupSize;
 
 		final char[] result = new char[numLength + groupCount];
 		Arrays.fill(result, '#');
 		int availableChars = groupSize + 1;
 		for (int i = result.length - 1; i > 0; i--) {
 			availableChars--;
 			if (availableChars == 0) {
 				result[i] = GROUPING_SEPARATOR;
 				availableChars = groupSize + 1;
 			}
 		}
 		for (int i = 0, j = 0; i < result.length; i++) {
 			if ('#' == result[i]) {
 				result[i] = input.charAt(j);
 				j++;
 			}
 		}
 		// System.out.println("group: " + input + " >> " + String.valueOf(result));
 		return String.valueOf(result);
 	}
 
 	protected static final char DECIMAL_SEPARATOR = new DecimalFormatSymbols().getDecimalSeparator();
 	protected static final char GROUPING_SEPARATOR = new DecimalFormatSymbols().getGroupingSeparator();
 	protected static final char MINUS_SIGN = new DecimalFormatSymbols().getMinusSign();
 	protected static final char ZERO = '0';
 	protected static final String ZERO_DEC = String.valueOf('0') + DECIMAL_SEPARATOR;
 	private static final String MINUS_ZERO = String.valueOf(MINUS_SIGN) + ZERO;
 	private static final String MINUS_DEC = String.valueOf(MINUS_SIGN) + DECIMAL_SEPARATOR;
 
 	private final NumericVerifyListener verifyListener;
 	private final NumericModifyListener modifyListener;
 	private final KeyListener keyListener;
 	private final FocusListener focusListener;
 
 	private boolean isSigned;
 	private boolean isGrouping;
 	private boolean isMarkNegative;
 	private NegativeMarker negativeMarker;
 	private int maxLength;
 	private int precision;
 
 	/**
 	 * True, if this ridget has a custom model-to-control converter
 	 */
 	private boolean hasCustomConverter;
 
 	public NumericTextRidget() {
 		super("0"); //$NON-NLS-1$
 		verifyListener = new NumericVerifyListener();
 		modifyListener = new NumericModifyListener();
 		keyListener = new NumericKeyListener();
 		focusListener = new NumericFocusListener();
 		isSigned = true;
 		isGrouping = true;
 		isMarkNegative = true;
 		maxLength = -1;
 		precision = -1;
 		addPropertyChangeListener(ITextRidget.PROPERTY_TEXT, new PropertyChangeListener() {
 			public void propertyChange(final PropertyChangeEvent evt) {
 				// System.out.println("updateMarkNeg: " + evt.getNewValue());
 				updateMarkNegative();
 			}
 		});
 	}
 
 	/**
 	 * Checks that given String value is a valid number.
 	 * <p>
 	 * Subclasses should override and adjust to their needs without calling
 	 * super().
 	 * 
 	 * @param number
 	 *            a String value; "" is used for the empty value
 	 */
 	protected void checkNumber(final String number) {
 		if (!"".equals(number)) { //$NON-NLS-1$
 			final BigInteger bigInteger = checkIsNumber(number);
 			checkSigned(bigInteger);
 			checkMaxLength(number);
 		}
 	}
 
 	@Override
 	protected void checkUIControl(final Object uiControl) {
 		super.checkUIControl(uiControl);
 		AbstractSWTRidget.assertType(uiControl, Text.class);
 		if (uiControl != null) {
 			final int style = ((Text) uiControl).getStyle();
 			if ((style & SWT.SINGLE) == 0) {
 				throw new BindingException("Text widget must be SWT.SINGLE"); //$NON-NLS-1$
 			}
 		}
 	}
 
 	@Override
 	protected final synchronized void addListeners(final Text control) {
 		control.addModifyListener(modifyListener);
 		control.addVerifyListener(verifyListener);
 		control.addKeyListener(keyListener);
 		control.addFocusListener(focusListener);
 		super.addListeners(control);
 	}
 
 	protected IConverter getConverter(final Class<?> type, final int precision) {
 		if (hasCustomConverter) {
 			return getValueBindingSupport().getModelToUIControlConverter();
 		} else {
 			return ToStringConverterFactory.createNumberConverter(type, precision);
 		}
 	}
 
 	protected synchronized int getPrecision() {
 		return precision;
 	}
 
 	/**
 	 * Converts text to BigInteger and checks if it is less than zero.
 	 * <p>
 	 * Subclasses may override and adjust to their needs without calling
 	 * super().
 	 * 
 	 * @param text
 	 *            a String representing a numeric value; never null
 	 * @return true if text is negative, false otherwise.
 	 */
 	protected boolean isNegative(final String text) {
 		final BigInteger value = new BigInteger(text);
 		return (value.compareTo(BigInteger.ZERO) < 0);
 	}
 
 	@Override
 	protected final synchronized void removeListeners(final Text control) {
 		control.removeModifyListener(modifyListener);
 		control.removeVerifyListener(verifyListener);
 		control.removeFocusListener(focusListener);
 		control.removeKeyListener(keyListener);
 		super.removeListeners(control);
 	}
 
 	protected synchronized void setPrecision(final int precision) {
 		this.precision = precision;
 		final String oldText = getText();
 		final String newText = formatFraction(oldText);
 		if (!oldText.equals(newText)) {
 			setText(newText);
 		}
 	}
 
 	@Override
 	protected void setUIText(final String text) {
 		if (isDecimal() && text.length() > 0) {
 			super.setUIText(beautifyText(text));
 		} else {
 			super.setUIText(text);
 		}
 	}
 
 	@Override
 	public void setModelToUIControlConverter(final IConverter converter) {
 		hasCustomConverter = converter != null;
 		super.setModelToUIControlConverter(converter);
 	}
 
 	// API methods
 	//////////////
 
 	public final synchronized int getMaxLength() {
 		return maxLength;
 	}
 
 	@Override
 	public synchronized String getText() {
 		String result = super.getText();
 		if (isDecimal()) {
 			result = removeTrailingPadding(result);
 		}
 		return result;
 	}
 
 	public synchronized boolean isGrouping() {
 		return isGrouping;
 	}
 
 	public synchronized boolean isMarkNegative() {
 		return isMarkNegative;
 	}
 
 	public synchronized boolean isSigned() {
 		return isSigned;
 	}
 
 	public synchronized void setGrouping(final boolean useGrouping) {
 		if (isGrouping != useGrouping) {
 			isGrouping = useGrouping;
 			updateGrouping();
 		}
 	}
 
 	public synchronized void setMarkNegative(final boolean mustBeMarked) {
 		if (isMarkNegative != mustBeMarked) {
 			isMarkNegative = mustBeMarked;
 			updateMarkNegative();
 		}
 	}
 
 	public final synchronized void setMaxLength(final int maxLength) {
 		Assert.isLegal(maxLength == -1 || maxLength > 0,
 				"maxLength must be greater than zero or -1 (unlimited): " + maxLength); //$NON-NLS-1$
 		final int oldValue = this.maxLength;
 		if (oldValue != maxLength) {
 			this.maxLength = maxLength;
 			firePropertyChange(INumericTextRidget.PROPERTY_MAXLENGTH, oldValue, maxLength);
 		}
 	}
 
 	public final synchronized void setSigned(final boolean signed) {
 		if (isSigned != signed) {
 			final boolean oldValue = isSigned;
 			isSigned = signed;
 			firePropertyChange(PROPERTY_SIGNED, oldValue, isSigned);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * <p>
 	 * If decimal and/or grouping separators are contained in the given
 	 * {@code text} value, they must follow the convention of the current
 	 * locale.
 	 * <p>
 	 * Examples:
 	 * <ul>
 	 * <li>DE - valid text: "1.234,56" or "1234,56"</li>
 	 * <li>US - valid text: "1,234.56" or "1234.56"</li>
 	 * </ul>
 	 * <p>
 	 * Passing a null value is equivalent to {@code setText("")}.
 	 * 
 	 * @see DecimalFormatSymbols#getDecimalSeparator()
 	 */
 	@Override
 	public final synchronized void setText(final String text) {
 		final String value = text != null ? text : ""; //$NON-NLS-1$
 		checkNumber(value);
 		super.setText(group(ungroup(value), isGrouping, isDecimal()));
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * <p>
 	 * 
 	 * @throws RuntimeException
 	 *             if the value obtain from model exceeds the specified maximum
 	 *             length or precision. It is responsibility of application to
 	 *             handle this.
 	 */
 	@Override
 	public synchronized void updateFromModel() {
 		checkValue();
 		super.updateFromModel();
 	}
 
 	// helping methods
 	//////////////////
 
 	private void beautifyText(final Text control) {
 		if (control != null) {
 			final String oldText = control.getText();
 			final String newText = beautifyText(oldText);
 			if (!newText.equals(oldText)) {
 				final Listener[] verifyListeners = control.getListeners(SWT.Verify);
 				for (final Listener listener : verifyListeners) {
 					control.removeListener(SWT.Verify, listener);
 				}
 				stopModifyListener();
 				control.setText(newText);
 				control.setSelection(newText.length());
 				startModifyListener();
 				for (final Listener listener : verifyListeners) {
 					control.addListener(SWT.Verify, listener);
 				}
 			}
 		}
 	}
 
 	private String beautifyText(final String text) {
 		if (String.valueOf(DECIMAL_SEPARATOR).equals(text)) {
 			return text;
 		}
 		String newText = formatFraction(text);
 		if (newText.length() > 1 && newText.charAt(0) == DECIMAL_SEPARATOR) {
 			newText = "0" + newText; //$NON-NLS-1$
 		} else if (newText.startsWith(MINUS_DEC)) {
 			boolean hasValue = false;
 			for (int i = 0; !hasValue && i < newText.length(); i++) {
 				final char ch = newText.charAt(i);
 				hasValue = Character.isDigit(ch) && ch != '0';
 			}
 			if (hasValue) {
 				newText = newText.substring(0, 1) + "0" + newText.substring(1); //$NON-NLS-1$
 			} else {
 				newText = "0" + newText.substring(1); //$NON-NLS-1$
 			}
 		}
 		return newText;
 	}
 
 	private BigInteger checkIsNumber(final String number) {
 		try {
 			return new BigInteger(ungroup(number));
 		} catch (final NumberFormatException nfe) {
 			throw new NumberFormatException("Not a valid number: " + number); //$NON-NLS-1$
 		}
 	}
 
 	private void checkMaxLength(final String number) {
 		if (maxLength == -1) {
 			return;
 		}
 		int length = number.length() - StringUtils.count(number, GROUPING_SEPARATOR);
 		if (number.length() > 0 && number.charAt(0) == MINUS_SIGN) {
 			length -= 1;
 		}
 		if (maxLength < length) {
 			final String msg = String.format("Length (%d) exceeded: %s", maxLength, number); //$NON-NLS-1$
 			throw new NumberFormatException(msg);
 		}
 	}
 
 	private void checkSigned(final BigInteger value) {
 		if (!isSigned() && value.compareTo(BigInteger.ZERO) == -1) {
 			throw new NumberFormatException("Negative numbers not allowed: " + value); //$NON-NLS-1$
 		}
 	}
 
 	private void checkValue() {
 		final IObservableValue modelObservable = getValueBindingSupport().getModelObservable();
 		if (modelObservable == null) {
 			return;
 		}
 		final Object value = modelObservable.getValue();
 		final Class<?> type = (Class<?>) modelObservable.getValueType();
 		if (type == null) {
 			return;
 		}
 		final IConverter converter = getConverter(type, Integer.MAX_VALUE);
 		if (converter != null) {
 			checkNumber((String) converter.convert(value));
 		}
 	}
 
 	private void startModifyListener() {
 		modifyListener.setEnabled(true);
 	}
 
 	private void startVerifyListener() {
 		verifyListener.setEnabled(true);
 	}
 
 	private synchronized String createPattern(final String input) {
 		String result;
 		if (isDecimal()) {
 			final String decSep = DECIMAL_SEPARATOR == '.' ? "\\." : String.valueOf(DECIMAL_SEPARATOR); //$NON-NLS-1$
 			result = String.format("\\d{0,%d}%s\\d{0,%d}", getMaxLength(), decSep, getPrecision()); //$NON-NLS-1$
 			if (isSigned) {
 				result = String.format("%c?", MINUS_SIGN) + result; //$NON-NLS-1$
 			}
 		} else {
 			// -1 => no length limit
 			final int length = getMaxLength() == -1 ? input.length() : getMaxLength();
 			if (isSigned) {
 				result = String.format("%c?\\d{0,%d}", MINUS_SIGN, length); //$NON-NLS-1$
 			} else {
 				result = String.format("\\d{0,%d}", length); //$NON-NLS-1$
 			}
 		}
 		// System.out.println("pattern: " + result);
 		return result;
 	}
 
 	private String formatFraction(final String text) {
 		String result = text;
 		final int decSep = text.indexOf(DECIMAL_SEPARATOR);
 		if (decSep != -1) {
 			final int fractionDigits = text.substring(decSep).length() - 1;
 			final int prec = getPrecision();
 			if (fractionDigits < prec) {
 				final int pad = Math.max(0, getPrecision() - fractionDigits);
 				if (pad > 0) {
 					final char[] zeroes = new char[pad];
 					Arrays.fill(zeroes, '0');
 					result = text + String.valueOf(zeroes);
 				}
 			} else if (fractionDigits > prec) {
 				final int diff = fractionDigits - prec;
 				result = text.substring(0, text.length() - diff);
 			}
 		}
 		return result;
 	}
 
 	private boolean isDecimal() {
 		return getPrecision() != -1;
 	}
 
 	private void stopModifyListener() {
 		modifyListener.setEnabled(false);
 	}
 
 	private void stopVerifyListener() {
 		verifyListener.setEnabled(false);
 	}
 
 	private void updateGrouping() {
 		setText(getText());
 	}
 
 	private void updateMarkNegative() {
 		final String text = ungroup(getText());
 		boolean needMarker = false;
 		if (isMarkNegative()) {
 			try {
 				needMarker = isNegative(text);
 			} catch (final NumberFormatException nfe) {
 				needMarker = false;
 			}
 		}
 		if (needMarker) {
 			if (negativeMarker == null) {
 				negativeMarker = new NegativeMarker();
 			}
 			addMarker(negativeMarker);
 		} else {
 			if (negativeMarker != null) {
 				removeMarker(negativeMarker);
 			}
 		}
 	}
 
 	// helping classes
 	//////////////////
 
 	/**
 	 * This listener handles addition, deletion and replacement of text in the
 	 * Text control. When the text in the control is modified, it will compute
 	 * the new value and match it against a pattern. The pattern is computed
 	 * based on the maxLength, precision and signed settings of this ridget. If
 	 * the new value does not match against the pattern, e.doit is set to false
	 * and the modification is canceled.
 	 */
 	private final class NumericVerifyListener implements VerifyListener {
 
 		private boolean isEnabled = true;
 
 		public synchronized void setEnabled(final boolean isEnabled) {
 			this.isEnabled = isEnabled;
 		}
 
 		public synchronized void verifyText(final VerifyEvent e) {
 			if (!e.doit || !isEnabled) {
 				return;
 			}
 			final Text control = (Text) e.widget;
 			final String oldText = control.getText();
 			String newText = null;
 			int newCursorPos = -1;
 			boolean applyText = false;
 			int start = e.start;
 			int end = e.end;
 			char character = e.character;
 
 			// this is a workaround for RAP bug #327439
 			if (character == 0 && RAPDetector.isRAPavailable()) {
 				// try to get the char from the selection and update start/end positions
 				final Point sel = control.getSelection();
 				if (oldText.length() > e.text.length()) { // delete
 					character = '\b';
 					start = findChangePos(oldText, e.text);
 					end = start + Math.max(sel.y - sel.x, 1);
 				} else { // insert / replace
 					character = e.text.charAt(sel.x);
 					start = sel.x;
 					end = sel.y;
 				}
 			}
 
 			if (Character.isDigit(character) || MINUS_SIGN == character) { // insert / replace
 				final boolean preserveDecSep = oldText.substring(start, end).indexOf(DECIMAL_SEPARATOR) != -1;
 				if (preserveDecSep) {
 					if (oldText.charAt(start) == DECIMAL_SEPARATOR && oldText.length() > end - start) {
 						// #(.#)# -> #.C# or (.#)# -> .C#
 						newText = oldText.substring(0, start) + DECIMAL_SEPARATOR + character + oldText.substring(end);
 					} else {
 						// (#.#) -> C. or #(#.#)# -> #C.# or #(#.)# -> #C.#
 						newText = oldText.substring(0, start) + character + DECIMAL_SEPARATOR + oldText.substring(end);
 					}
 					applyText = true;
 				} else {
 					newText = oldText.substring(0, start) + character + oldText.substring(end);
 				}
 			} else if ('\b' == character || 127 == e.keyCode) { // delete
 				final NumericString ns = new NumericString(oldText, isGrouping());
 				newCursorPos = ns.delete(start, end, character);
 				newText = ns.toString();
 				applyText = true;
 			}
 			boolean doFlash = true;
 			if (newText != null) {
 				final String newTextNoGroup = ungroup(newText);
 				final String regex = createPattern(newTextNoGroup);
 				e.doit = Pattern.matches(regex, newTextNoGroup);
 				doFlash = !e.doit;
 				if (e.doit && applyText) {
 					e.doit = false;
 					stopVerifyListener();
 					setTextAndCursor(control, newTextNoGroup, newCursorPos, e, end);
 					startVerifyListener();
 				}
 			} else {
 				e.doit = false;
 			}
 			if (doFlash) {
 				flash();
 			}
 		}
 
 		/**
 		 * @param oldText
 		 * @param text
 		 * @return
 		 */
 		private int findChangePos(final String oldText, final String newText) {
 			final int length = newText.length();
 			for (int i = 0; i < length; i++) {
 				if (oldText.charAt(i) != newText.charAt(i)) {
 					return i;
 				}
 			}
 			return length;
 		}
 
 		private void setTextAndCursor(final Text control, final String text, final int cursorPos, final VerifyEvent e,
 				final int end) {
 			final String oldText = control.getText();
 			final int oldCursorPos = control.getCaretPosition();
 			control.setText(text);
 			if (text.length() > 0 && text.charAt(0) == DECIMAL_SEPARATOR
 					&& control.getText().startsWith(String.valueOf(ZERO))) {
 				control.setSelection(1);
 			} else if (oldCursorPos != 0 && text.length() == 1 && text.charAt(0) == DECIMAL_SEPARATOR) {
 				control.setSelection(0);
 			} else if (text.length() == 2 && text.charAt(1) == DECIMAL_SEPARATOR) {
 				control.setSelection(1);
 			} else if (cursorPos > -1) {
 				control.setSelection(cursorPos);
 			} else {
 				final int posFromRight = oldText.length() - end;
 				control.setSelection(control.getText().length() - posFromRight);
 			}
 		}
 	}
 
 	/**
 	 * When the user types a key in the text control that modifies it's content
 	 * this listener is invoked. It will replace the string in the text control,
 	 * by the a formatted equivalent, according to the settings in this ridget
 	 * (precision, maxlength, etc.).
 	 */
 	private final class NumericModifyListener implements ModifyListener {
 
 		private boolean isEnabled = true;
 
 		public synchronized void setEnabled(final boolean isEnabled) {
 			this.isEnabled = isEnabled;
 		}
 
 		public synchronized void modifyText(final ModifyEvent e) {
 			if (!isEnabled || !isEnabled()) {
 				return;
 			}
 			// System.out.println(e);
 			final Text control = (Text) e.widget;
 			final String oldText = control.getText();
 			final boolean isDecimal = isDecimal();
 			String newText = group(removeLeadingCruft(ungroup(oldText)), isGrouping(), isDecimal);
 			if (isOutputOnly() && newText.equals(String.valueOf(DECIMAL_SEPARATOR))) {
 				newText = ""; //$NON-NLS-1$
 			}
 			if (isDecimal && newText.startsWith(String.valueOf(DECIMAL_SEPARATOR)) && newText.length() > 1) {
 				newText = ZERO + newText;
 			}
 			if (!oldText.equals(newText)) {
 				stopVerifyListener();
 				final int posFromRight = oldText.length() - control.getCaretPosition();
 				control.setText(newText);
 				final int caretPos = newText.length() - posFromRight;
 				control.setSelection(caretPos);
 				// System.out.println("newText= " + newText + " @ " + caretPos);
 				startVerifyListener();
 			}
 		}
 	}
 
 	/**
 	 * This listener controls which key strokes are allowed by the text control.
 	 * Additionally some keystrokes replaced with special behavior. Currently
 	 * those key strokes are:
 	 * <ol>
 	 * <li>Left & Right arrow - will jump over grouping separators</li>
 	 * <li>
 	 * Shift - ddisables jumping over grouping separators when pressed down</li>
 	 * <li>Decimal separator - will cause the cursor to jump over the decimal
 	 * separator if directly to the right of it. Otherwise ignored</li>
 	 * <li>
 	 * minus ('-') - for signed widgets, it adds the '-' character to the left
 	 * of the widget. Otherwise ignored</li>
 	 * <li>CR ('\r') - for decimal ridgets, it will pad the fractional digits by
 	 * adding '0's until the maximum number of fractional digits is reached (as
 	 * specified by the ridgets precision value)
 	 * </ol>
 	 */
 	private final class NumericKeyListener extends KeyAdapter {
 
 		private boolean shiftDown = false;
 
 		@Override
 		public void keyReleased(final KeyEvent e) {
 			if (131072 == e.keyCode) {
 				shiftDown = false;
 			}
 		}
 
 		@Override
 		public void keyPressed(final KeyEvent e) {
 			final Text control = (Text) e.widget;
 			final String text = control.getText();
 			if (131072 == e.keyCode) {
 				shiftDown = true;
 			} else if (16777219 == e.keyCode && control.getSelectionCount() == 0) {// left arrow
 				final int index = control.getCaretPosition() - 1;
 				if (index > 1 && GROUPING_SEPARATOR == text.charAt(index) && !shiftDown) {
 					e.doit = false;
 					control.setSelection(index - 1);
 				}
 			} else if (16777220 == e.keyCode && control.getSelectionCount() == 0) { //right arrow
 				final int index = control.getCaretPosition() + 1;
 				if (index < text.length() - 1 && GROUPING_SEPARATOR == text.charAt(index) && !shiftDown) {
 					e.doit = false;
 					control.setSelection(index + 1);
 				}
 			} else if (DECIMAL_SEPARATOR == e.character) {
 				e.doit = false;
 				final int index = control.getCaretPosition();
 				if (index < text.length() && text.charAt(index) == DECIMAL_SEPARATOR) {
 					control.setSelection(index + 1);
 				} else {
 					flash();
 				}
 			} else if (MINUS_SIGN == e.character) {
 				e.doit = false;
 				if (isSigned()) {
 					final Event event = new Event();
 					event.type = SWT.Verify;
 					event.character = MINUS_SIGN;
 					event.start = 0;
 					event.end = 0;
 					event.widget = control;
 					event.text = String.valueOf(MINUS_SIGN);
 					control.notifyListeners(SWT.Verify, event);
 					if (event.doit) {
 						final int caret = control.getCaretPosition() + 1;
 						stopVerifyListener();
 						control.setText(MINUS_SIGN + text);
 						control.setSelection(caret);
 						startVerifyListener();
 					} else {
 						flash();
 					}
 				} else {
 					flash();
 				}
 			} else if ('\r' == e.character) {
 				if (isDecimal()) {
 					final String newText = formatFraction(text);
 					if (!newText.equals(text)) {
 						stopVerifyListener();
 						control.setText(newText);
 						control.setSelection(newText.length());
 						startVerifyListener();
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * For decimal ridgets: this focus listener will pad the fractional digits
 	 * by adding '0's until the maximum number of fractional digits is reached
 	 * (as specified by the ridgets precision value)
 	 */
 	private final class NumericFocusListener extends FocusAdapter {
 		@Override
 		public void focusLost(final FocusEvent e) {
 			if (isDecimal()) {
 				final Text control = (Text) e.widget;
 				beautifyText(control);
 			}
 		}
 	}
 
 }
