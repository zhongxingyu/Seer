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
 
 import java.math.BigDecimal;
 
 import org.eclipse.core.databinding.beans.BeansObservables;
 import org.eclipse.core.databinding.beans.PojoObservables;
 import org.eclipse.core.databinding.conversion.IConverter;
 import org.eclipse.core.databinding.observable.value.IObservableValue;
 import org.eclipse.core.runtime.Assert;
 
 import org.eclipse.riena.core.util.StringUtils;
 import org.eclipse.riena.ui.ridgets.IDecimalTextRidget;
 import org.eclipse.riena.ui.ridgets.ValueBindingSupport;
 import org.eclipse.riena.ui.ridgets.swt.AbstractSWTRidget;
 import org.eclipse.riena.ui.swt.utils.UIControlsFactory;
 
 /**
  * Ridget for a 'decimal' SWT <code>Text</code> widget.
  * <p>
 * Implementation note: all the logic is in NumericTextRidget. This class justs
  * adds the API mandated by IDecimalTextRidget.
  * 
  * @see UIControlsFactory#createTextDecimal(org.eclipse.swt.widgets.Composite)
  */
 public class DecimalTextRidget extends NumericTextRidget implements IDecimalTextRidget {
 
 	public DecimalTextRidget() {
 		setMaxLength(10);
 		setPrecision(2);
 		setText("0"); //$NON-NLS-1$
 		setSigned(true);
 	}
 
 	@Override
 	protected void checkNumber(final String number) {
 		if (!"".equals(number)) { //$NON-NLS-1$
 			final BigDecimal value = checkIsNumber(number);
 			checkSigned(value);
 			checkMaxLength(number);
 			checkPrecision(number);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * <p>
 	 * The 'empty' value will be replaced with the empty string, if the ridget
 	 * is in output only mode. Otherwise same behavior as super.
 	 */
 	@Override
 	protected final String getTextBasedOnMarkerState(final String value) {
 		if (isOutputOnly() && !isNotEmpty(value)) {
 			return ""; //$NON-NLS-1$
 		}
 		return super.getTextBasedOnMarkerState(value);
 	}
 
 	@Override
 	protected boolean isNegative(final String text) {
 		final BigDecimal value = new BigDecimal(localStringToBigDecimal(text));
 		return value.compareTo(BigDecimal.ZERO) < 0;
 	}
 
 	@Override
 	protected boolean isNotEmpty(final String text) {
 		final String stripped = removeLeadingCruft(removeTrailingPadding(text));
 		return stripped.length() > 0;
 	}
 
 	@Override
 	public void bindToModel(final IObservableValue observableValue) {
 		super.bindToModel(observableValue);
 		// the converter depends on the type of the bound model + precision;
 		// so we update it after the binding has been set-up
 		updateConverter(getPrecision());
 	}
 
 	@Override
 	public void bindToModel(final Object valueHolder, final String valuePropertyName) {
 		if (AbstractSWTRidget.isBean(valueHolder.getClass())) {
 			bindToModel(BeansObservables.observeValue(valueHolder, valuePropertyName));
 		} else {
 			bindToModel(PojoObservables.observeValue(valueHolder, valuePropertyName));
 		}
 	}
 
 	@Override
 	public synchronized int getPrecision() {
 		return super.getPrecision();
 	}
 
 	@Override
 	public final synchronized void setPrecision(final int numberOfFractionDigits) {
 		Assert.isLegal(numberOfFractionDigits > -1, "numberOfFractionDigits must > -1: " + numberOfFractionDigits); //$NON-NLS-1$
 		final int oldValue = getPrecision();
 		if (oldValue != numberOfFractionDigits) {
 			updateConverter(numberOfFractionDigits);
 			super.setPrecision(numberOfFractionDigits);
 			firePropertyChange(IDecimalTextRidget.PROPERTY_PRECISION, oldValue, numberOfFractionDigits);
 		}
 	}
 
 	// helping methods
 	//////////////////
 
 	private BigDecimal checkIsNumber(final String number) {
 		try {
 			return new BigDecimal(localStringToBigDecimal(number));
 		} catch (final NumberFormatException nfe) {
 			throw new NumberFormatException("Not a valid decimal: " + number); //$NON-NLS-1$
 		}
 	}
 
 	private void checkMaxLength(final String number) {
 		final int maxLength = getMaxLength();
 		int length;
 		final int decSepIndex = number.indexOf(DECIMAL_SEPARATOR);
 		if (decSepIndex != -1) {
 			final String decimalPart = number.substring(0, decSepIndex);
 			length = decimalPart.length() - StringUtils.count(decimalPart, GROUPING_SEPARATOR);
 		} else {
 			length = number.length() - StringUtils.count(number, GROUPING_SEPARATOR);
 		}
 		if (number.length() > 0 && number.charAt(0) == MINUS_SIGN) {
 			length -= 1;
 		}
 		if (maxLength < length) {
 			final String msg = String.format("Length (%d) exceeded: %s", maxLength, number); //$NON-NLS-1$
 			throw new NumberFormatException(msg);
 		}
 	}
 
 	private void checkPrecision(final String number) {
 		final int decSepIndex = number.indexOf(DECIMAL_SEPARATOR);
 		if (decSepIndex != -1) {
 			final int precision = getPrecision();
 			final int fractionalDigits = number.substring(decSepIndex).length() - 1;
 			if (precision < fractionalDigits) {
 				final String msg = String.format("Precision (%d) exceeded: %s", precision, number); //$NON-NLS-1$
 				throw new NumberFormatException(msg);
 			}
 		}
 	}
 
 	private void checkSigned(final BigDecimal value) {
 		if (!isSigned() && value.compareTo(BigDecimal.ZERO) == -1) {
 			throw new NumberFormatException("Negative numbers not allowed: " + value); //$NON-NLS-1$
 		}
 	}
 
 	private String localStringToBigDecimal(final String number) {
 		return ungroup(number).replace(DECIMAL_SEPARATOR, '.');
 	}
 
 	private void updateConverter(final int precision) {
 		final ValueBindingSupport vbs = getValueBindingSupport();
 		if (vbs.getModelObservable() != null) {
 			final Class<?> type = (Class<?>) vbs.getModelObservable().getValueType();
 			final IConverter converter = getConverter(type, precision);
 			vbs.setModelToUIControlConverter(converter);
 			vbs.rebindToModel();
 		}
 	}
 
 }
