 package org.eclipse.stem.core.modifier.impl;
 
 /*******************************************************************************
  * Copyright (c) 2008 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.stem.core.Constants;
 import org.eclipse.stem.core.modifier.DoubleRangeModifier;
 import org.eclipse.stem.core.modifier.ModifierPackage;
 
 /**
  * <!-- begin-user-doc --> An implementation of the model object '<em><b>Double Range Modifier</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.stem.core.modifier.impl.DoubleRangeModifierImpl#getStartValue <em>Start Value</em>}</li>
  *   <li>{@link org.eclipse.stem.core.modifier.impl.DoubleRangeModifierImpl#getEndValue <em>End Value</em>}</li>
  *   <li>{@link org.eclipse.stem.core.modifier.impl.DoubleRangeModifierImpl#getIncrement <em>Increment</em>}</li>
  *   <li>{@link org.eclipse.stem.core.modifier.impl.DoubleRangeModifierImpl#getNextValue <em>Next Value</em>}</li>
  *   <li>{@link org.eclipse.stem.core.modifier.impl.DoubleRangeModifierImpl#getOriginalValue <em>Original Value</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 @SuppressWarnings("boxing")
 public class DoubleRangeModifierImpl extends RangeModifierImpl implements
 		DoubleRangeModifier {
 	/**
 	 * The default value of the '{@link #getStartValue() <em>Start Value</em>}' attribute.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see #getStartValue()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double START_VALUE_EDEFAULT = -9.9999999E7;
 	/**
 	 * The cached value of the '{@link #getStartValue() <em>Start Value</em>}' attribute.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see #getStartValue()
 	 * @generated
 	 * @ordered
 	 */
 	protected double startValue = START_VALUE_EDEFAULT;
 	/**
 	 * The default value of the '{@link #getEndValue() <em>End Value</em>}' attribute.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see #getEndValue()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double END_VALUE_EDEFAULT = -9.9999999E7;
 	/**
 	 * The cached value of the '{@link #getEndValue() <em>End Value</em>}' attribute.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see #getEndValue()
 	 * @generated
 	 * @ordered
 	 */
 	protected double endValue = END_VALUE_EDEFAULT;
 	/**
 	 * The default value of the '{@link #getIncrement() <em>Increment</em>}' attribute.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see #getIncrement()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double INCREMENT_EDEFAULT = -9.9999999E7;
 	/**
 	 * The cached value of the '{@link #getIncrement() <em>Increment</em>}' attribute.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see #getIncrement()
 	 * @generated
 	 * @ordered
 	 */
 	protected double increment = INCREMENT_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getNextValue() <em>Next Value</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getNextValue()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double NEXT_VALUE_EDEFAULT = 0.0;
 	/**
 	 * The cached value of the '{@link #getNextValue() <em>Next Value</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getNextValue()
 	 * @generated
 	 * @ordered
 	 */
 	protected double nextValue = NEXT_VALUE_EDEFAULT;
 	/**
 	 * The default value of the '{@link #getOriginalValue() <em>Original Value</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getOriginalValue()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double ORIGINAL_VALUE_EDEFAULT = 0.0;
 	/**
 	 * The cached value of the '{@link #getOriginalValue() <em>Original Value</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getOriginalValue()
 	 * @generated
 	 * @ordered
 	 */
 	protected double originalValue = ORIGINAL_VALUE_EDEFAULT;
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected DoubleRangeModifierImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return ModifierPackage.Literals.DOUBLE_RANGE_MODIFIER;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getStartValue() {
 		return startValue;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setStartValue(double newStartValue) {
 		double oldStartValue = startValue;
 		startValue = newStartValue;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ModifierPackage.DOUBLE_RANGE_MODIFIER__START_VALUE, oldStartValue, startValue));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getEndValue() {
 		return endValue;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setEndValue(double newEndValue) {
 		double oldEndValue = endValue;
 		endValue = newEndValue;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ModifierPackage.DOUBLE_RANGE_MODIFIER__END_VALUE, oldEndValue, endValue));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getIncrement() {
 		return increment;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setIncrement(double newIncrement) {
 		double oldIncrement = increment;
 		increment = newIncrement;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ModifierPackage.DOUBLE_RANGE_MODIFIER__INCREMENT, oldIncrement, increment));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public double getNextValue() {
 		// Unset?
 		if (!eIsSet(ModifierPackage.DOUBLE_RANGE_MODIFIER__NEXT_VALUE)) {
 			// Yes
 			nextValue = startValue;
 		} // if
 		
 		final double retValue = nextValue;
		complete = Math.abs(retValue  - endValue) <= Constants.FLOAT_COMPARE;
 		
 		// Still incrementing?
 		if (!complete) {
 			// Yes
 			final double temp = retValue + increment;
 			// Would the new currentValue be equal or "past" the endValue?
 			if ((increment >= 0 && temp < endValue) || (increment < 0 && temp > endValue)) {
 				// No
 				nextValue = temp;
 			} // if
 			else {
 				// Yes
 				nextValue = endValue;
 			} // else 
 		} // if 
 		currentValueText = Double.toString(retValue);
 		return retValue;
 	} // getNextValue
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setNextValue(double newNextValue) {
 		double oldNextValue = nextValue;
 		nextValue = newNextValue;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ModifierPackage.DOUBLE_RANGE_MODIFIER__NEXT_VALUE, oldNextValue, nextValue));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getOriginalValue() {
 		return originalValue;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setOriginalValue(double newOriginalValue) {
 		double oldOriginalValue = originalValue;
 		originalValue = newOriginalValue;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ModifierPackage.DOUBLE_RANGE_MODIFIER__ORIGINAL_VALUE, oldOriginalValue, originalValue));
 	}
 
 	/**
 	 * @see org.eclipse.stem.core.modifier.impl.FeatureModifierImpl#updateFeature()
 	 */
 	
 	@Override
 	public void updateFeature() {
 		// Original value captured yet?
 		if (!eIsSet(ModifierPackage.DOUBLE_RANGE_MODIFIER__ORIGINAL_VALUE)) {
 			// No
 			setOriginalValue((Double)target.eGet(getEStructuralFeature()));
 		} // if
 		target.eSet(getEStructuralFeature(), getNextValue());
 	} // updateFeature
 
 	
 	/**
 	 * @see org.eclipse.stem.core.modifier.impl.RangeModifierImpl#reset()
 	 */
 	@Override
 	public void reset() {
 		super.reset();
 		eUnset(ModifierPackage.DOUBLE_RANGE_MODIFIER__NEXT_VALUE);
 		target.eSet(getEStructuralFeature(), getOriginalValue());
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case ModifierPackage.DOUBLE_RANGE_MODIFIER__START_VALUE:
 				return new Double(getStartValue());
 			case ModifierPackage.DOUBLE_RANGE_MODIFIER__END_VALUE:
 				return new Double(getEndValue());
 			case ModifierPackage.DOUBLE_RANGE_MODIFIER__INCREMENT:
 				return new Double(getIncrement());
 			case ModifierPackage.DOUBLE_RANGE_MODIFIER__NEXT_VALUE:
 				return new Double(getNextValue());
 			case ModifierPackage.DOUBLE_RANGE_MODIFIER__ORIGINAL_VALUE:
 				return new Double(getOriginalValue());
 		}
 		return super.eGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void eSet(int featureID, Object newValue) {
 		switch (featureID) {
 			case ModifierPackage.DOUBLE_RANGE_MODIFIER__START_VALUE:
 				setStartValue(((Double)newValue).doubleValue());
 				return;
 			case ModifierPackage.DOUBLE_RANGE_MODIFIER__END_VALUE:
 				setEndValue(((Double)newValue).doubleValue());
 				return;
 			case ModifierPackage.DOUBLE_RANGE_MODIFIER__INCREMENT:
 				setIncrement(((Double)newValue).doubleValue());
 				return;
 			case ModifierPackage.DOUBLE_RANGE_MODIFIER__NEXT_VALUE:
 				setNextValue(((Double)newValue).doubleValue());
 				return;
 			case ModifierPackage.DOUBLE_RANGE_MODIFIER__ORIGINAL_VALUE:
 				setOriginalValue(((Double)newValue).doubleValue());
 				return;
 		}
 		super.eSet(featureID, newValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void eUnset(int featureID) {
 		switch (featureID) {
 			case ModifierPackage.DOUBLE_RANGE_MODIFIER__START_VALUE:
 				setStartValue(START_VALUE_EDEFAULT);
 				return;
 			case ModifierPackage.DOUBLE_RANGE_MODIFIER__END_VALUE:
 				setEndValue(END_VALUE_EDEFAULT);
 				return;
 			case ModifierPackage.DOUBLE_RANGE_MODIFIER__INCREMENT:
 				setIncrement(INCREMENT_EDEFAULT);
 				return;
 			case ModifierPackage.DOUBLE_RANGE_MODIFIER__NEXT_VALUE:
 				setNextValue(NEXT_VALUE_EDEFAULT);
 				return;
 			case ModifierPackage.DOUBLE_RANGE_MODIFIER__ORIGINAL_VALUE:
 				setOriginalValue(ORIGINAL_VALUE_EDEFAULT);
 				return;
 		}
 		super.eUnset(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public boolean eIsSet(int featureID) {
 		switch (featureID) {
 			case ModifierPackage.DOUBLE_RANGE_MODIFIER__START_VALUE:
 				return startValue != START_VALUE_EDEFAULT;
 			case ModifierPackage.DOUBLE_RANGE_MODIFIER__END_VALUE:
 				return endValue != END_VALUE_EDEFAULT;
 			case ModifierPackage.DOUBLE_RANGE_MODIFIER__INCREMENT:
 				return increment != INCREMENT_EDEFAULT;
 			case ModifierPackage.DOUBLE_RANGE_MODIFIER__NEXT_VALUE:
 				return nextValue != NEXT_VALUE_EDEFAULT;
 			case ModifierPackage.DOUBLE_RANGE_MODIFIER__ORIGINAL_VALUE:
 				return originalValue != ORIGINAL_VALUE_EDEFAULT;
 		}
 		return super.eIsSet(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public String toString() {
 		if (eIsProxy()) return super.toString();
 
 		StringBuffer result = new StringBuffer(super.toString());
 		result.append(" (startValue: "); //$NON-NLS-1$
 		result.append(startValue);
 		result.append(", endValue: "); //$NON-NLS-1$
 		result.append(endValue);
 		result.append(", increment: "); //$NON-NLS-1$
 		result.append(increment);
 		result.append(", nextValue: "); //$NON-NLS-1$
 		result.append(nextValue);
 		result.append(", originalValue: "); //$NON-NLS-1$
 		result.append(originalValue);
 		result.append(')');
 		return result.toString();
 	}
 
 	/**
 	 * @see org.eclipse.stem.core.common.SanityChecker#sane()
 	 */
 	@Override
 	public boolean sane() {
 		boolean retValue = super.sane();
 		assert retValue;
 
 		retValue = retValue && (endValue >= startValue);
 		assert retValue;
 
 		retValue = retValue && (increment > 0.0);
 		assert retValue;
 
 		return retValue;
 	} // sane
 
 } // DoubleRangeModifierImpl
