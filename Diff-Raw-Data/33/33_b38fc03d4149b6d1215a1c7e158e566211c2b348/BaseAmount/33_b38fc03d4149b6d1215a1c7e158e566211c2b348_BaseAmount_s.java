 /**
  * Copyright (c) 2005, 2012, Werner Keil, Ikayzo and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Werner Keil, Ikayzo and others - initial API and implementation
  */
 package org.eclipse.uomo.units.impl;
 
 import static org.eclipse.uomo.core.impl.OutputHelper.println;
 
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.math.MathContext;
 
 import org.eclipse.uomo.units.AbstractConverter;
 import org.eclipse.uomo.units.AbstractUnit;
 import org.eclipse.uomo.units.IMeasure;
 import org.eclipse.uomo.units.QuantityAmount;
 import org.eclipse.uomo.units.impl.converter.RationalConverter;
 import org.unitsofmeasurement.quantity.Dimensionless;
 import org.unitsofmeasurement.quantity.Quantity;
 import org.unitsofmeasurement.unit.Unit;
 import org.unitsofmeasurement.unit.UnitConverter;
 
 import com.ibm.icu.util.MeasureUnit;
 
 /**
  * Represents a generic quantity amount.
  * 
  * @author <a href="mailto:uomo@catmedia.us">Werner Keil</a>
 * @version 1.3.1, ($Revision: 212 $), $Date: 2012-08-16 23:35:44 +0200 (Do, 16
  *          Aug 2012) $
  */
 public class BaseAmount<Q extends Quantity<Q>> extends QuantityAmount<Q>
 		implements Comparable<BaseAmount<Q>> {
 
 	/**
 	 * @param number
 	 * @param unit
 	 */
 	public BaseAmount(Number number, Unit<Q> unit) {
 		super(number, (MeasureUnit) unit);
 	}
 
 	/**
 	 * Returns the amount corresponding to the specified value and unit.
 	 * 
 	 * @param value
 	 *            the value stated in the specified unit.
 	 * @param unit
 	 *            the unit in which the value is stated.
 	 * @return the corresponding amount.
 	 */
 	public static <Q extends Quantity<Q>> BaseAmount<Q> valueOf(Number value,
 			Unit<Q> unit) {
 		BaseAmount<Q> amount = new BaseAmount<Q>(value, unit);
 		return amount;
 	}
 
 	/**
 	 * Holds a dimensionless measure of one (exact).
 	 */
 	protected static final BaseAmount<Dimensionless> ONE = new BaseAmount<Dimensionless>(
 			BigDecimal.ONE, AbstractUnit.ONE);
 
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public IMeasure<Q> add(IMeasure<Q> that) {
 		final IMeasure<Q> thatToUnit = that.to(unit());
 		return new BaseAmount(this.value().doubleValue()
 				+ thatToUnit.value().doubleValue(), unit());
 	}
 
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	@Override
 	public IMeasure<Q> substract(IMeasure<Q> that) {
 		final IMeasure<Q> thatToUnit = that.to(unit());
 		return new BaseAmount(this.getNumber().doubleValue()
 				- thatToUnit.value().doubleValue(), unit());
 
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public IMeasure<Q> multiply(IMeasure<?> that) {
 		Unit<?> unit = unit().multiply(that.unit());
 		return (IMeasure<Q>) valueOf((value().doubleValue() * that.value()
 				.doubleValue()), unit);
 	}
 	
 	@Override
 	public IMeasure<?> multiply(Number that) {
 		return (IMeasure<Q>) valueOf((value().doubleValue() * that
 				.doubleValue()), unit());	
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public IMeasure<Q> divide(IMeasure<?> that) {
 		Unit<?> unit = unit().divide(that.unit());
 		return (IMeasure<Q>) valueOf((value().doubleValue() / that.value()
 				.doubleValue()), unit);
 	}
 
 	@Override
 	public IMeasure<Q> to(Unit<Q> unit) {
		return to(unit, MathContext.DECIMAL32);
 	}
 
 	public IMeasure<Q> to(Unit<Q> unit, MathContext ctx) {
 		if (this.unit().equals(unit))
 			return this;
 		UnitConverter cvtr = this.unit().getConverterTo(unit);
 		if (cvtr == AbstractConverter.IDENTITY)
 			return (IMeasure<Q>) valueOf(this.value(), unit);
 		return (IMeasure<Q>) valueOf(convert(this.value(), cvtr, ctx), unit);
 	}
 
 	// Try to convert the specified value.
 	private static Number convert(Number value, UnitConverter cvtr,
 			MathContext ctx) {
 		if (cvtr instanceof RationalConverter) { // Try converting through Field
 													// methods.
 			RationalConverter rCvtr = (RationalConverter) cvtr;
 			BigInteger dividend = rCvtr.getDividend();
 			BigInteger divisor = rCvtr.getDivisor();
 			if (dividend.abs().compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0)
 				throw new ArithmeticException("Multiplier overflow"); //$NON-NLS-1$
 			if (divisor.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0)
 				throw new ArithmeticException("Divisor overflow"); //$NON-NLS-1$
			return (value.longValue() * dividend.longValue())
 					/ (divisor.longValue());
 		} else if (cvtr instanceof AbstractConverter.Compound
 				&& cvtr.isLinear()) { // Do it in two parts.
 			AbstractConverter.Compound compound = (AbstractConverter.Compound) cvtr;
 			Number firstConversion = convert(value, compound.getRight(), ctx);
 			Number secondConversion = convert(firstConversion,
 					compound.getLeft(), ctx);
 			return secondConversion;
 		} else { // Try using BigDecimal as intermediate.
 			BigDecimal decimalValue = BigDecimal.valueOf(value.doubleValue());
 			BigDecimal newValue = cvtr.convert(decimalValue, ctx);
 			return newValue;
 			// if (((FieldNumber)value) instanceof Decimal)
 			// return (N)((FieldNumber)Decimal.valueOf(newValue));
 			// if (((FieldNumber)value) instanceof Float64)
 			// return (N)((FieldNumber)Float64.valueOf(newValue.doubleValue()));
 			// throw new ArithmeticException(
 			// "Generic amount conversion not implemented for amount of type " +
 			// value.getClass());
 		}
 	}
 
 	/**
 	 * Returns this measure raised at the specified exponent.
 	 * 
 	 * @param exp
 	 *            the exponent.
 	 * @return <code>this<sup>exp</sup></code>
 	 */
 	@SuppressWarnings("unchecked")
 	public IMeasure<? extends IMeasure<Q>> pow(int exp) {
 		if (exp < 0)
 			return (IMeasure<? extends IMeasure<Q>>) this.pow(-exp).inverse();
 		if (exp == 0)
 
 			return (IMeasure<? extends IMeasure<Q>>) ONE;
 
 		IMeasure<? extends IMeasure<Q>> pow2 = (IMeasure<? extends IMeasure<Q>>) this;
 		IMeasure<? extends IMeasure<Q>> result = null;
 		while (exp >= 1) { // Iteration.
 
 			if ((exp & 1) == 1) {
 				result = (IMeasure<? extends IMeasure<Q>>) ((result == null) ? pow2
 						: result.multiply(pow2));
 
 			}
 
 			pow2 = (IMeasure<? extends IMeasure<Q>>) pow2.multiply(pow2);
 
 			exp >>>= 1;
 
 		}
 
 		return result;
 
 	}
 
 	public IMeasure<? extends IMeasure<Q>> inverse() {
 		@SuppressWarnings({ "rawtypes", "unchecked" })
 		final IMeasure<? extends IMeasure<Q>> m = new BaseAmount(value(),
 				unit().inverse());
 		return m;
 	}
 
 	@Override
 	public int compareTo(BaseAmount<Q> o) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (obj == null)
 			return false;
 		if (obj == this)
 			return true;
 		if (obj instanceof BaseAmount<?>) {
 			BaseAmount<?> ba = (BaseAmount<?>) obj;
 			if (this.unit().getClass() == ba.unit().getClass()) {
 				return super.equals(obj);
 			}
 			if (ba.unit() instanceof AlternateUnit<?>) {
 				AlternateUnit<?> baa = (AlternateUnit<?>) ba.unit();
 				if (this.unit() instanceof AlternateUnit<?>) {
 					return super.equals(obj);
 				} else if (this.unit() instanceof AnnotatedUnit<?>) {
 					AnnotatedUnit<?> au = (AnnotatedUnit<?>) this.unit();
 					println("Ann: " + au); //$NON-NLS-1$
 				} else if (this.unit() instanceof BaseUnit<?>) {
 					BaseUnit<?> bu = (BaseUnit<?>) this.unit();
 					println("Bas: " + bu); //$NON-NLS-1$
 				} else if (this.unit() instanceof ProductUnit<?>) {
 					ProductUnit<?> pu = (ProductUnit<?>) this.unit();
 					println("Pro: " + pu); //$NON-NLS-1$
 				} else if (this.unit() instanceof TransformedUnit<?>) {
 					TransformedUnit<?> tu = (TransformedUnit<?>) this.unit();
 					println("Tran: " + tu); //$NON-NLS-1$
 					if (tu.getParentUnit().equals(baa)) {
 						return true; // FIXME use number here, too
 					}
 				} else {
 					return super.equals(obj);
 				}
 			}
 			if (ba.unit() instanceof BaseUnit<?>) {
 				if (this.unit() instanceof AlternateUnit<?>) {
 					AlternateUnit<?> au = (AlternateUnit<?>) this.unit();
 					println("Alt: " + au); //$NON-NLS-1$
 				} else if (this.unit() instanceof AnnotatedUnit<?>) {
 					AnnotatedUnit<?> au = (AnnotatedUnit<?>) this.unit();
 					println("Ann: " + au); //$NON-NLS-1$
 				} else if (this.unit() instanceof BaseUnit<?>) {
 					return super.equals(obj);
 				} else if (this.unit() instanceof ProductUnit<?>) {
 					ProductUnit<?> pu = (ProductUnit<?>) this.unit();
 					println("Pro: " + pu); //$NON-NLS-1$
 				} else if (this.unit() instanceof TransformedUnit<?>) {
 					TransformedUnit<?> tu = (TransformedUnit<?>) this.unit();
 					println("Tran: " + tu); //$NON-NLS-1$
 				} else {
 					return super.equals(obj);
 				}
 			}
 			if (ba.unit() instanceof TransformedUnit<?>) {
 				TransformedUnit<?> bat = (TransformedUnit<?>) ba.unit();
 				if (this.unit() instanceof AlternateUnit<?>) {
 					AlternateUnit<?> au = (AlternateUnit<?>) this.unit();
 					println("Alt: " + au); //$NON-NLS-1$
 					if (bat.getParentUnit().equals(au)) {
 						return true;
 					}
 				} else if (this.unit() instanceof AnnotatedUnit<?>) {
 					AnnotatedUnit<?> au = (AnnotatedUnit<?>) this.unit();
 					System.out.println("Ann: " + au); //$NON-NLS-1$
 				} else if (this.unit() instanceof BaseUnit<?>) {
 					BaseUnit<?> bu = (BaseUnit<?>) this.unit();
 					println("Bas: " + bu); //$NON-NLS-1$
 				} else if (this.unit() instanceof ProductUnit<?>) {
 					ProductUnit<?> pu = (ProductUnit<?>) this.unit();
 					println("Pro: " + pu); //$NON-NLS-1$
 				} else if (this.unit() instanceof TransformedUnit<?>) {
 					return super.equals(obj);
 				} else {
 					return super.equals(obj);
 				}
 			}
 		}
 		return super.equals(obj);
 	}
 }
