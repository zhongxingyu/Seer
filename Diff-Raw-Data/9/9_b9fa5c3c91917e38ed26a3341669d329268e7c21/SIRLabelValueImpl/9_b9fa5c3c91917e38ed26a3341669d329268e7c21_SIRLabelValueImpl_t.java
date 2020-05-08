 package org.eclipse.stem.diseasemodels.standard.impl;
 
 /*******************************************************************************
  * Copyright (c) 2006 IBM Corporation and others.
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
 import org.eclipse.stem.core.graph.IntegrationLabelValue;
 import org.eclipse.stem.core.graph.LabelValue;
 import org.eclipse.stem.diseasemodels.standard.DiseaseModelLabelValue;
 import org.eclipse.stem.diseasemodels.standard.SEIRLabelValue;
 import org.eclipse.stem.diseasemodels.standard.SILabelValue;
 import org.eclipse.stem.diseasemodels.standard.SIRLabelValue;
 import org.eclipse.stem.diseasemodels.standard.StandardDiseaseModelLabelValue;
 import org.eclipse.stem.diseasemodels.standard.StandardPackage;
 
 /**
  * <!-- begin-user-doc --> An implementation of the model object '<em><b>SIR Label Value</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.stem.diseasemodels.standard.impl.SIRLabelValueImpl#getR <em>R</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class SIRLabelValueImpl extends SILabelValueImpl implements
 		SIRLabelValue {
 	/**
 	 * The default value of the '{@link #getR() <em>R</em>}' attribute. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see #getR()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double R_EDEFAULT = 0.0;
 
 	/**
 	 * The cached value of the '{@link #getR() <em>R</em>}' attribute. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see #getR()
 	 * @generated
 	 * @ordered
 	 */
 	protected double r = R_EDEFAULT;
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected SIRLabelValueImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * 
 	 * This is used to create instances for testing purposes.
 	 * 
 	 * @param s
 	 *            the number of susceptible population members
 	 * 
 	 * @param iR
 	 *            the number of recovering infectious population members
 	 * 
 	 * @param iF
 	 *            the number of fatally infectious population members
 	 *            
 	 * @param incidence
 	 *            the new incidence from the interaction term in the compartment model
 	 * 
 	 * @param r
 	 *            the number of recovered population members
 	 * @param births
 	 *            the number of births that have occured in the population
 	 * @param deaths
 	 *            the total number of deaths that have occured in the population
 	 * @param diseaseDeaths
 	 *            the number of deaths due to the disease that have occured in
 	 *            the population
 	 * 
 	 * <!-- end-user-doc -->
 	 */
 	public SIRLabelValueImpl(final double s, final double i, 
 			final double incidence, final double r,
 			final double diseaseDeaths) {
 		super(s, i, incidence, diseaseDeaths);
 		this.r = r;
 	} // SIRLabelValueImpl
 
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * 
 	 * This is used to create instances for testing purposes.
 	 * 
 	 * @param s
 	 *            the number of susceptible population members
 	 * 
 	 * @param iR
 	 *            the number of recovering infectious population members
 	 * 
 	 * @param iF
 	 *            the number of fatally infectious population members
 	 *            
 	 * @param r
 	 *            the number of recovered population members
 	 * @param births
 	 *            the number of births that have occured in the population
 	 * @param deaths
 	 *            the total number of deaths that have occured in the population
 	 * @param diseaseDeaths
 	 *            the number of deaths due to the disease that have occured in
 	 *            the population
 	 * 
 	 * <!-- end-user-doc -->
 	 */
 	public SIRLabelValueImpl(final double s, final double i, 
 			final double r,  final double diseaseDeaths) {
 		super(s, i, 0.0, diseaseDeaths);
 		this.r = r;
 	} // SIRLabelValueImpl
 
 	
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return StandardPackage.Literals.SIR_LABEL_VALUE;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * 
 	 * @return the number of Infectious population members.
 	 * 
 	 * 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getR() {
 		return r;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * 
 	 * @param newR
 	 * 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setR(double newR) {
 		double oldR = r;
 		r = newR;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, StandardPackage.SIR_LABEL_VALUE__R, oldR, r));
 	}
 
 //	/**
 //	 * <!-- begin-user-doc -->
 //	 * 
 //	 * @return the instance with the values rotated S->R->I->S
 //	 * 
 //	 * <!-- end-user-doc -->
 //	 * @generated NOT
 //	 */
 //	public SIRLabelValue rotateLeft() {
 //		final double tempS = getS();
 //		setS(getI());
 //		setI(getR());
 //		setR(tempS);
 //		return this;
 //	} // rotateLeft
 
 	/**
 	 * @see org.eclipse.stem.diseasemodels.standard.impl.DiseaseModelLabelValueImpl#getPopulationCount()
 	 */
 	@Override
 	public double getPopulationCount() {
 		return super.getPopulationCount() + r;
 	} // getPopulationCount
 
 	@Override
 	public void zeroOutPopulationCount() {
 		super.zeroOutPopulationCount();
 		setR(R_EDEFAULT);
 	} // zeroOutPopulationCount
 	
 	/**
 	 * @see org.eclipse.stem.diseasemodels.standard.impl.DiseaseModelLabelValueImpl#set(org.eclipse.stem.diseasemodels.standard.DiseaseModelLabelValue)
 	 */
 	@Override
 	public DiseaseModelLabelValue set(IntegrationLabelValue value) {
 		super.set(value);
 		setR(((SIRLabelValue) value).getR());
 		return this;
 	} // set
 
 	/**
 	 * @see org.eclipse.stem.diseasemodels.standard.impl.DiseaseModelLabelValueImpl#add(org.eclipse.stem.diseasemodels.standard.DiseaseModelLabelValue)
 	 */
 	@Override
 	public DiseaseModelLabelValue add(IntegrationLabelValue value) {
 		super.add(value);
 		setR(getR() + ((SIRLabelValue) value).getR());
 		return this;
 	} // add
 
 	/**
 	 * @see org.eclipse.stem.diseasemodels.standard.impl.DiseaseModelLabelValueImpl#sub(org.eclipse.stem.diseasemodels.standard.DiseaseModelLabelValue)
 	 */
 	@Override
 	public DiseaseModelLabelValue sub(IntegrationLabelValue value) {
 		super.sub(value);
 		setR(getR() - ((SIRLabelValue) value).getR());
 		return this;
 	} // sub
 	
 	@Override
 	public DiseaseModelLabelValue scale(double scaleFactor) {
 		super.scale(scaleFactor);
 		setR(getR() *scaleFactor);
 		return this;
 	} // scale
 	
 	public DiseaseModelLabelValue add(double addition) {
 		super.add(addition);
 		setR(getR() +addition);
 		return this;
 	} // scale
 
 	public DiseaseModelLabelValue abs() {
 		super.abs();
 		setR(Math.abs(getR()));
 		return this;
 	} // scale
 
 
 	
 	public IntegrationLabelValue divide(IntegrationLabelValue d) {
 		SIRLabelValue _scale = (SIRLabelValue)d;
 		double sScaled = Math.abs(s) / Math.abs(_scale.getS());
 		double iScaled = Math.abs(i) / Math.abs(_scale.getI());
 		double rScaled = Math.abs(r) / Math.abs(_scale.getR());
 		setS(sScaled);
 		setI(iScaled);
 		setR(rScaled);
 		return this;
 	}
 	
 	public double max() {
 		double max;
 		if(s > i && s > r)
 			max = s;
 		else if(i > r) max = i;
 		else max = r;
 		return max;  
 	}
 	/**
 	 * Adjust when a delta value is bad, e.g. forces the 
 	 * target to go negative in any state.
 	 * 
 	 * @return boolean
 	 * @override
 	 */
	public boolean adjustDelta(IntegrationLabelValue target) {
 		SIRLabelValue sirValue = (SIRLabelValue)target;
 		boolean adjusted = false;
 		double newS = this.getS() + sirValue.getS();
 		double newI = this.getI() + sirValue.getI();
 		double newR = this.getR() + sirValue.getR();
 		
 		double factor = 1.0;
 		if(newS < newI && newS < newR && newS < 0.0) {
 			// Scale using S
 			adjusted = true;
 			factor = -sirValue.getS()/this.getS();
 		} else if(newI < newR && newI < 0.0) {
 			// Scale using I
 			adjusted = true;
 			factor = -sirValue.getI()/this.getI();
 		} else if(newR < 0.0) {
 			// Scale using R
 			adjusted = true;
 			factor = -sirValue.getR()/this.getR();
 		}
 		if(adjusted) this.scale(factor);
 		return adjusted;
 	}
 	/**
 	 * @see org.eclipse.stem.core.graph.impl.LabelValueImpl#reset()
 	 */
 	@Override
 	public void reset() {
 		super.reset();
 		setR(R_EDEFAULT);
 	} // reset
 
 	/**
 	 * @see org.eclipse.stem.core.common.SanityChecker#sane()
 	 */
 	@Override
 	public boolean sane() {
 		boolean retValue = super.sane();
 
 		retValue = retValue && getR() >= R_EDEFAULT;
 		assert retValue;
 
 		retValue = retValue && getR() <= MAX_POPULATION_VALUE;
 		assert retValue;
 		
 		retValue = retValue && !Double.isInfinite(getR());
 		assert retValue;
 		
 		retValue = retValue && !Double.isNaN(getR());
 		assert retValue;
 		
 		return retValue;
 	} // sane
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * 
 	 * @param featureID
 	 * @param resolve
 	 * @param coreType
 	 * @return
 	 * 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case StandardPackage.SIR_LABEL_VALUE__R:
 				return new Double(getR());
 		}
 		return super.eGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * 
 	 * @param featureID
 	 * @param newValue
 	 * 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void eSet(int featureID, Object newValue) {
 		switch (featureID) {
 			case StandardPackage.SIR_LABEL_VALUE__R:
 				setR(((Double)newValue).doubleValue());
 				return;
 		}
 		super.eSet(featureID, newValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * 
 	 * @param featureID
 	 * 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void eUnset(int featureID) {
 		switch (featureID) {
 			case StandardPackage.SIR_LABEL_VALUE__R:
 				setR(R_EDEFAULT);
 				return;
 		}
 		super.eUnset(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * 
 	 * @param featureID
 	 * @return
 	 * 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public boolean eIsSet(int featureID) {
 		switch (featureID) {
 			case StandardPackage.SIR_LABEL_VALUE__R:
 				return r != R_EDEFAULT;
 		}
 		return super.eIsSet(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * 
 	 * @return
 	 * 
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	@Override
 	public String toString() {
 		if (eIsProxy())
 			return super.toString();
 
 		StringBuffer result = new StringBuffer();
 		result.append("s:"); //$NON-NLS-1$
 		result.append(getFormatter().format(s));
 		result.append(", i:"); //$NON-NLS-1$
 		result.append(getFormatter().format(i));
 		result.append(", r:"); //$NON-NLS-1$
 		result.append(getFormatter().format(r));
 		result.append(", DD:"); //$NON-NLS-1$
 		result.append(getFormatter().format(getDiseaseDeaths()));
 		return result.toString();
 	} // toString
 
 } // SIRLabelValueImpl
